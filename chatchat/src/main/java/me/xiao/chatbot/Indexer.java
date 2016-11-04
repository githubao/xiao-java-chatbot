package me.xiao.chatbot;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

/**
 * 索引器
 *
 * @author BaoQiang
 * @version 2.0
 * @Create at 2016/11/4 18:18
 */
public class Indexer {
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static String hexString(byte[] b) {
        String ret = "";

        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xF);
            ret += hex.toUpperCase();
        }
        return ret;
    }

    public static void train(String corpusPath, String indexPath) throws IOException, NoSuchAlgorithmException {
//        if (args.length != 2) {
//            System.err.println("Usage: " + Indexer.class.getSimpleName() + " corpus_path index_path");
//            System.exit(-1);
//        }

//        String corpusPath = args[0];
//        String indexPath = args[1];

        Analyzer analyzer = new IKAnalyzer(true);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        iwc.setUseCompoundFile(true);

        IndexWriter indexWriter = new IndexWriter(FSDirectory.open(new File(indexPath)), iwc);

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(corpusPath), "UTF-8"));
        String line;
        String last = "";
        long linNum = 0;
        MessageDigest md = MessageDigest.getInstance("MD5");
        HashSet<String> mc = new HashSet<>();
        int dupCount = 0;
        int totalCount = 0;
        long lastT = 0;

        while ((line = br.readLine()) != null) {
            totalCount++;
            if (totalCount % 15000000 == 0) {
                System.out.println("clear set");
                mc.clear();
            }

            line = line.trim();

            if (0 == line.length()) {
                continue;
            }

            String[] attr = line.split("\\t");
            if (attr.length != 2) {
                continue;
            }

            String q = attr[0];
            String a = attr[1];

            if (q.length() == 0 || a.length() == 0) {
                continue;
            }

            byte[] md5 = md.digest(line.getBytes(UTF8));
            String md5Str = hexString(md5);

            if (mc.contains(md5Str)) {
                dupCount++;
                continue;
            } else {
                mc.add(md5Str);
            }

            Document doc = new Document();
            doc.add(new TextField("question", q, Field.Store.YES));
            doc.add(new StoredField("answer", a));
            indexWriter.addDocument(doc);

            linNum++;
            if (linNum % 100000 == 0) {
                long t = System.currentTimeMillis();
                System.out.println(String.format("elapse second: %s add doc: %s totalCount: %s dup: %s", (t - lastT) / 1000, linNum, totalCount, dupCount));
                lastT = t;
            }
        }

        br.close();
        indexWriter.forceMerge(1);
        indexWriter.close();
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
//        String classPath = Indexer.class.getClassLoader().getResource("").getPath();
//
//        String iinFile = classPath + "/me/xiao/chatbot/corpus.txt";
//        String outPath = classPath + "/me/xiao/chatbot/index/";

        String iinFile = "/mnt/lucene/corpus.txt";
        String outPath = "/mnt/lucene/index/";

        train(iinFile, outPath);
    }

}
