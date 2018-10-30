package com.giztk.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EntityAnnotation {
    // 保存已标注实体的单例
    public static final String ENTITY_NAME = "EntityName";
    public static final String START = "Start";
    public static final String END = "End";
    public static final String ENTITY_TYPE = "NerTag";
    private static final String DOC_ID = "doc_id";
    private static final String SENT_ID = "sent_id";
    private static final String ENTITIES = "entities";

    // entity types
    public static final String ENTITY_PERSON = "PERSON";
    public static final String ENTITY_STATUS = "TITLE";

    // query exist message
    public static final String ENTITY_EXISTS = "exists";
    public static final String ENTITY_EXISTS_BUT_DIFF = "different";
    public static final String ENTITY_ADD_SUCCESS = "success";
    public static final String ENTITY_ADD_ERROR = "error";

    private static EntityAnnotation sInstance;
    private List<JSONObject> mEntityList;

    private JSONObject removedObjectDueToDiff;

    private EntityAnnotation(){
        mEntityList = new ArrayList<>();
    }

    public static EntityAnnotation getInstance(){
        if(sInstance == null){
            sInstance = new EntityAnnotation();
        }
        return sInstance;
    }

    public String add(String entityName, String entityType, int startOffset, int endOffset){
        JSONObject object = new JSONObject();
        try{
            for(JSONObject o: mEntityList){
                String tempName = o.getString(ENTITY_NAME);
                String tempType = o.getString(ENTITY_TYPE);
                if(tempName.equals(entityName) && tempType.equals(entityType)){
                    // 已经标注过了
                    return ENTITY_EXISTS;
                }else if(tempName.equals(entityName) && !tempType.equals(entityType)){
                    // 名称有过但类型不同
                    removedObjectDueToDiff = o;
                    mEntityList.remove(o);
                    return ENTITY_EXISTS_BUT_DIFF;
                }
            }
            // 添加新的标注
            object.put(ENTITY_NAME, entityName);
            object.put(START, startOffset);
            object.put(END, endOffset);
            object.put(ENTITY_TYPE, entityType);
            Log.d("EntityAnnotation", object.toString(4));
            mEntityList.add(object);
            return ENTITY_ADD_SUCCESS;
        }catch (Exception e){
            e.printStackTrace();
            return ENTITY_ADD_ERROR;
        }
    }

    public JSONObject getRemovedObjectDueToDiff() {
        return removedObjectDueToDiff;
    }

    public void delete(String entityName){
        try{
            for(JSONObject object: mEntityList){
                if(object.getString(ENTITY_NAME).equals(entityName)){
                    mEntityList.remove(object);
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<JSONObject> getEntityList() {
        return mEntityList;
    }

    public JSONObject getEntityResult(String docId, int sentId){
        try {
            JSONObject result = new JSONObject();
            JSONArray entities = new JSONArray();
            for(JSONObject object: mEntityList)
                entities.put(object);
            result.put(DOC_ID, docId);
            result.put(SENT_ID, sentId);
            result.put(ENTITIES, entities);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clear(){
        mEntityList.clear();
    }
}
