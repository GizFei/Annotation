package com.giztk.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TripleAnnotation {
    // 保存关系标注信息的类

    private static final String TRIPLE_DOC_ID = "doc_id";
    private static final String TRIPLE_SENT_ID = "sent_id";
    private static final String TRIPLE_TITLE = "title";
    private static final String TRIPLE_SENT_CTX = "sent_ctx";
    private static final String TRIPLE_TRIPLES = "triples";

    private String mDocId;
    private int mSentId;
    private String mTitle;
    private String mSentContent;
    private List<Triple> mTriples;

    /**
     * 传入服务端返回的json对象，构造TripleAnnotation对象
     * @param tripleObject 服务端返回的json对象
     */
    public TripleAnnotation(JSONObject tripleObject){
        mTriples = new ArrayList<>();
        try{
            mDocId = tripleObject.getString(TRIPLE_DOC_ID);
            mSentId = tripleObject.getInt(TRIPLE_SENT_ID);
            mTitle = tripleObject.getString(TRIPLE_TITLE);
            mSentContent = tripleObject.getString(TRIPLE_SENT_CTX);
            JSONArray triples = tripleObject.getJSONArray(TRIPLE_TRIPLES);
            for(int i = 0; i < triples.length(); i++){
                Triple t;
                if(triples.getJSONObject(i).has("original")){
                    t = new Triple(triples.getJSONObject(i), triples.getJSONObject(i).getBoolean("original"));
                }else{
                    t = new Triple(triples.getJSONObject(i), true);
                }
                mTriples.add(t);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getDocId() {
        return mDocId;
    }

    public void setDocId(String docId) {
        mDocId = docId;
    }

    public int getSentId() {
        return mSentId;
    }

    public void setSentId(int sentId) {
        mSentId = sentId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getSentContent() {
        return mSentContent;
    }

    public void setSentContent(String sentContent) {
        mSentContent = sentContent;
    }

    public List<Triple> getTriples() {
        return mTriples;
    }

    public Triple getTriple(int i){
        return mTriples.get(i);
    }

    public void setTriples(List<Triple> triples) {
        mTriples = triples;
    }

    // 添加新标注的Triple
    public void addTriple(Triple t){
        mTriples.add(t);
    }

    // 删除Triple
    public void removeTriple(int index){
        mTriples.remove(index);
    }

    // 更新原来的Triple
    public void updateTriple(Triple t){
        for(int i = 0; i < mTriples.size(); i++){
            if(mTriples.get(i).getId().equals(t.getId())){
                mTriples.set(i, t);
                break;
            }
        }
    }

    public String toJSONString(){
        try {
            JSONObject result = new JSONObject();
            result.put(TRIPLE_DOC_ID, mDocId);
            result.put(TRIPLE_SENT_ID, mSentId);
            result.put(TRIPLE_TITLE, mTitle);
            result.put(TRIPLE_SENT_CTX, mSentContent);
            JSONArray array = new JSONArray();
            for(Triple triple: mTriples){
                array.put(triple.toJSONObject());
            }
            result.put(TRIPLE_TRIPLES, array);
            return result.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String rotateToString(){
        try {
            JSONObject result = new JSONObject();
            result.put(TRIPLE_DOC_ID, mDocId);
            result.put(TRIPLE_SENT_ID, mSentId);
            result.put(TRIPLE_TITLE, mTitle);
            result.put(TRIPLE_SENT_CTX, mSentContent);
            JSONArray array = new JSONArray();
            for(Triple triple: mTriples){
                JSONObject object = triple.toJSONObject();
                object.put("original", triple.isOriginal());
                array.put(object);
            }
            result.put(TRIPLE_TRIPLES, array);
            return result.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void clear(){
        mDocId = "";
        mSentContent = "";
        mTriples.clear();
        mSentId = -1;
        mTitle = "";
    }
}
