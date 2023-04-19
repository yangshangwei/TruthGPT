package com.tto.gpt.common;

import com.alibaba.fastjson2.JSON;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class TextChatRecordManager {

    public final static String chatDir = System.getProperty("user.home") + File.separator + "GPT_HIS_CHAT" + File.separator  + "%s";
    public final static String INDEX_DIRECTORY = chatDir + File.separator + "records";
    private static final String batchTitleFile = chatDir + File.separator + "titles.json";
    private static final String FIELD_Q = "question";
    private static final String FIELD_A = "answer";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_BATCH_ID = "batch_id";

    private Directory directory;
    private String indexDirectory;
    private String titleFilePath;
    private IndexWriter indexWriter;


    public TextChatRecordManager(String ip) {
        try {
            indexDirectory = String.format(INDEX_DIRECTORY, ip);
            titleFilePath = String.format(batchTitleFile,ip);
            directory = FSDirectory.open(Paths.get(indexDirectory));
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
            indexWriter = new IndexWriter(directory, config);
        }catch (Exception e){
            System.out.println("客户IP[" + ip + "]无法开启聊天记录的存储功能");
        }
    }

    public void appendJsonToFile(ChatBatchInfo title) {
        try {
            Path path = Paths.get(titleFilePath);
            // 如果文件不存在，则创建一个新文件
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            // 以追加模式打开文件，并写入 JSON 数据
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
                writer.write(JSON.toJSONString(title));
                writer.newLine(); // 在 JSON 数据后添加换行符，以便在读取时可以正确分隔各个 JSON 字符串
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to append JSON to file.", e);
        }
    }

    public void rewriteJsonToFile(List<ChatBatchInfo> list) {
        try {
            Path path = Paths.get(titleFilePath);
            // 如果文件不存在，则创建一个新文件
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            // 以追加模式打开文件，并写入 JSON 数据
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING)) {
                if(null != list && list.size() > 0){
                    for(ChatBatchInfo cbi:list){
                        writer.write(JSON.toJSONString(cbi));
                        writer.newLine(); // 在 JSON 数据后添加换行符，以便在读取时可以正确分隔各个 JSON 字符串
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to append JSON to file.", e);
        }
    }

    public List<ChatBatchInfo> readJsonListFromFile() {
        List<ChatBatchInfo> jsonList = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(titleFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonList.add(JSON.parseObject(line, ChatBatchInfo.class));
            }
        } catch (IOException e) {
        }

        Collections.reverse(jsonList);
        return jsonList;
    }

    public String createBatchTitle(String batchTitle,String scope) {
        try {
            String batchId = UUID.randomUUID().toString();
            ChatBatchInfo batchInfo = new ChatBatchInfo(batchTitle,batchId);
            batchInfo.setSystemScope(scope);
            appendJsonToFile(batchInfo);
            return batchId;
        }catch (Exception e){
            return null;
        }
    }
    public void addChatRecordToBatch(String batchId, ChatRecord chatRecord)  {
        Document document = new Document();
        document.add(new TextField(FIELD_Q, chatRecord.getQuestion(), Field.Store.YES));
        document.add(new TextField(FIELD_A, chatRecord.getAnswer(), Field.Store.YES));
        document.add(new LongPoint(FIELD_TIMESTAMP, chatRecord.getTimestamp().getTime()));
        document.add(new StoredField(FIELD_TIMESTAMP, chatRecord.getTimestamp().getTime()));
        document.add(new StringField(FIELD_BATCH_ID, batchId, Field.Store.YES));

        try {
            indexWriter.addDocument(document);
            indexWriter.commit();
        }catch (Exception e){

        }
    }

    public void deleteChatRecordByBatch(String batchId){
        Term term = new Term(FIELD_BATCH_ID, batchId);
        try {
            indexWriter.deleteDocuments(term);
            indexWriter.commit();
        }catch (Exception e){

        }
    }

    public List<ChatRecord> searchChatRecordsByBatchId(String batchId) {
        // 创建查询，根据 batchId 匹配聊天记录
        Query query = new TermQuery(new Term(FIELD_BATCH_ID, batchId));

        // 打开索引
        try (Directory directory = FSDirectory.open(Paths.get(indexDirectory));
             IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            // 执行搜索
            TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);

            // 从搜索结果中提取聊天记录
            List<ChatRecord> chatRecords = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                String question = doc.get(FIELD_Q);
                String answer = doc.get(FIELD_A);
                String time = doc.get(FIELD_TIMESTAMP);

                ChatRecord record = new ChatRecord();
                record.setQuestion(question);
                record.setAnswer(answer);
                chatRecords.add(record);
            }

            for(ChatRecord record:chatRecords){
                record.setTmpSN(UUID.randomUUID().toString());
            }
            return chatRecords;
        } catch (IOException e) {
            throw new RuntimeException("Failed to search chat records by batchId.", e);
        }
    }
}
