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

    public TripleAnnotation(JSONObject tripleObject){
        mTriples = new ArrayList<>();
        try{
            mDocId = tripleObject.getString(TRIPLE_DOC_ID);
            mSentId = tripleObject.getInt(TRIPLE_SENT_ID);
            mTitle = tripleObject.getString(TRIPLE_TITLE);
            mSentContent = tripleObject.getString(TRIPLE_SENT_CTX);
            JSONArray triples = tripleObject.getJSONArray(TRIPLE_TRIPLES);
            for(int i = 0; i < triples.length(); i++){
                Triple t = new Triple(triples.getJSONObject(i));
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

    public void setTriples(List<Triple> triples) {
        mTriples = triples;
    }

    private class Triple{
        String id;
        int leftEStart;
        int leftEENd;
        int rightEStart;
        int rightEEnd;
        int relationStart;
        int relationEnd;
        String leftEntity;
        String rightEntity;
        int relationID;
        int status;

        Triple(JSONObject object){
            try {
                id = object.getString("id");
                leftEStart = object.getInt("left_e_start");
                leftEENd = object.getInt("left_e_end");
                rightEStart = object.getInt("right_e_start");
                rightEEnd = object.getInt("right_e_end");
                relationStart = object.getInt("relation_start");
                relationEnd = object.getInt("relation_end");
                leftEntity = object.getString("left_entity");
                rightEntity = object.getString("right_entity");
                relationID = object.getInt("relation_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject toJSONObject(){
            try {
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("left_e_start", leftEStart);
                object.put("left_e_end", leftEENd);
                object.put("right_e_start", rightEStart);
                object.put("right_e_end", rightEEnd);
                object.put("relation_start", relationStart);
                object.put("relation_end", relationEnd);
                object.put("left_entity", leftEntity);
                object.put("right_entity", rightEntity);
                object.put("relation_id", relationID);
                object.put("status", status);
                return object;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
