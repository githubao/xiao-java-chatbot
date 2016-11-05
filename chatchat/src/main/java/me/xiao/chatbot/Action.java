package me.xiao.chatbot;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.PriorityQueue;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 实际的请求处理类
 *
 * @author BaoQiang
 * @version 2.0
 * @Create at 2016/11/4 23:18
 */
public class Action {
    private static final int MAX_RESULT = 10;
    private static final int MAX_TOTAL_HINTS = 1_000_000;

    private static Logger logger = Logger.getLogger(Action.class);
    private static Logger logChat = Logger.getLogger("chat");

    private static IndexSearcher indexSearcher = null;

    static {
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(new File(ChatConstant.INDEX_PATH)));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        indexSearcher = new IndexSearcher(reader);
    }

    public static void doServlet(FullHttpRequest request, NettyHttpServletResponse response) throws IOException, ParseException {
        ByteBuf buf;
        QueryStringDecoder qsd = new QueryStringDecoder(request.uri());
        Map<String, List<String>> mapParameters = qsd.parameters();
        List<String> list = mapParameters.get("q");

        if (null != list && list.size() == 1) {
            String q = list.get(0);
            if (q.equals("monitor")) {
                JSONObject json = new JSONObject();
                json.put("total", 1);
                JSONObject item = new JSONObject();
                JSONArray result = new JSONArray();
                item.put("answer", "alive");
                result.add(item);
                json.put("result", result);
                buf = Unpooled.copiedBuffer(json.toJSONString().getBytes());
                response.setContent(buf);
                return;
            }
            logger.info("question: " + q);

            List<String> clientIps = mapParameters.get("clientIps");
            String clientIp = null;
            if (null != clientIps && clientIps.size() == 1) {
                clientIp = clientIps.get(0);
                logger.info("clientIp: " + clientIp);
            }

            Query query;
            PriorityQueue<ScoreDoc> pq = new PriorityQueue<ScoreDoc>(MAX_RESULT) {
                @Override
                protected boolean lessThan(ScoreDoc a, ScoreDoc b) {
                    return a.score < b.score;
                }
            };

            MyCollector collector = new MyCollector(pq);

            JSONObject json = new JSONObject();
            TopDocs topDocs = collector.topDocs();

            Analyzer analyzer = new IKAnalyzer(true);
            QueryParser qp = new QueryParser(Version.LUCENE_4_9, "question", analyzer);
            if (topDocs.totalHits == 0) {
                qp.setDefaultOperator(QueryParser.Operator.AND);
                query = qp.parse(q);
                logger.info("lucene query: " + query.toString());
                topDocs = indexSearcher.search(query, 20);
                logger.info("elapse " + collector.getElapse() + " " + collector.getElapse2());
            }
            if (topDocs.totalHits == 0) {
                qp.setDefaultOperator(QueryParser.Operator.OR);
                query = qp.parse(q);
                logger.info("lucene query: " + query.toString());
                topDocs = indexSearcher.search(query, 20);
                logger.info("elapse " + collector.getElapse() + " " + collector.getElapse2());
            }

            json.put("total", topDocs.totalHits);
            json.put("q", q);
            JSONArray results = new JSONArray();
            String firstAns = "";
            for (ScoreDoc d : topDocs.scoreDocs) {
                Document doc = indexSearcher.doc(d.doc);
                String question = doc.get("question");
                String answer = doc.get("answer");
                JSONObject item = new JSONObject();
                if (firstAns.equals("")) {
                    firstAns = answer;
                }
                item.put("answer", answer);
                item.put("question", question);
                item.put("doc", d.doc);
                item.put("score", d.score);

                results.add(item);
            }

            json.put("result", results);
            logger.info("response: " + json);
            logChat.info(String.format("%s [%s] -> [%s]", clientIp, q, firstAns));
            buf = Unpooled.copiedBuffer(json.toJSONString().getBytes());
        } else {
            buf = Unpooled.copiedBuffer("error".getBytes());
        }
        response.setContent(buf);

    }

    public static class MyCollector extends TopDocsCollector<ScoreDoc> {
        protected Scorer scorer;
        protected AtomicReader reader;
        protected int baseDoc;
        protected HashSet<Integer> set = new HashSet<>();
        protected long elapse = 0;
        protected long elapse2 = 0;

        public MyCollector(PriorityQueue<ScoreDoc> pq) {
            super(pq);
        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            this.scorer = scorer;
        }

        @Override
        public void collect(int doc) throws IOException {
            long t1 = System.currentTimeMillis();

            if (this.totalHits > MAX_TOTAL_HINTS) {
                return;
            }

            String answer = this.getAnswer(doc);
            long t3 = System.currentTimeMillis();

            this.elapse2 += t3 - t1;
            if (set.contains(answer.hashCode())) {
                return;
            } else {
                set.add(answer.hashCode());
                ScoreDoc sd = new ScoreDoc(doc, this.scorer.score());
                if (this.pq.size() >= MAX_RESULT) {
                    this.pq.updateTop();
                    this.pq.top();
                }
                this.pq.add(sd);
                this.totalHits++;
            }
            long t2 = System.currentTimeMillis();

            this.elapse += t2 - t1;

        }

        private String getAnswer(int doc) throws IOException {
            Document d = indexSearcher.doc(doc);
            return d.get("answer");
        }

        @Override
        public void setNextReader(AtomicReaderContext context) throws IOException {
            this.reader = context.reader();
            this.baseDoc = context.docBase;
        }

        @Override
        public boolean acceptsDocsOutOfOrder() {
            return false;
        }

        public long getElapse() {
            return elapse;
        }

        public long getElapse2() {
            return elapse2;
        }
    }

}
