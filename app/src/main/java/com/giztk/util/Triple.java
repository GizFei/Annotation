package com.giztk.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Triple {

    private String id;
    private int leftEStart;
    private int leftEENd;
    private int rightEStart;
    private int rightEEnd;
    private int relationStart;
    private int relationEnd;
    private String leftEntity;
    private String rightEntity;
    private int relationID;
    private int status;

    /**
     * 默认构造函数，会为这个Triple生成一个随机的id值，不能改变Triple的id值
     */
    public Triple(){
        // 随机生成值
        id = UUID.randomUUID().toString().substring(0, 16);
        leftEStart = leftEENd = rightEStart = rightEEnd = relationStart = relationEnd = -1;
        leftEntity = rightEntity = "";
        relationID = -1;
        status = 1;  // 对新添加的默认正确
    }


    /**
     * 根据传入的object的构造Triple
     * @param object JSON对象
     */
    public Triple(JSONObject object){
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
            status = 1; // 默认正确，为1
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSONObject(){
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

    public String getId() {
        return id;
    }

    public int getLeftEStart() {
        return leftEStart;
    }

    public int getLeftEENd() {
        return leftEENd;
    }

    public int getRightEStart() {
        return rightEStart;
    }

    public int getRightEEnd() {
        return rightEEnd;
    }

    public int getRelationStart() {
        return relationStart;
    }

    public int getRelationEnd() {
        return relationEnd;
    }

    public String getLeftEntity() {
        return leftEntity;
    }

    public String getRightEntity() {
        return rightEntity;
    }

    public int getRelationID() {
        return relationID;
    }

    public int getStatus() {
        return status;
    }

    public void setLeftEStart(int leftEStart) {
        this.leftEStart = leftEStart;
    }

    public void setLeftEENd(int leftEENd) {
        this.leftEENd = leftEENd;
    }

    public void setRightEStart(int rightEStart) {
        this.rightEStart = rightEStart;
    }

    public void setRightEEnd(int rightEEnd) {
        this.rightEEnd = rightEEnd;
    }

    public void setRelationStart(int relationStart) {
        this.relationStart = relationStart;
    }

    public void setRelationEnd(int relationEnd) {
        this.relationEnd = relationEnd;
    }

    public void setLeftEntity(String leftEntity) {
        this.leftEntity = leftEntity;
    }

    public void setRightEntity(String rightEntity) {
        this.rightEntity = rightEntity;
    }

    public void setRelationID(int relationID) {
        this.relationID = relationID;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
