package com.giztk.annotation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.giztk.util.EntityAnnotation;
import com.giztk.util.TextUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BigBangActivity extends AppCompatActivity {

    private static final String TAG = "BigBangActivity";
    private static final String EXTRA_CONTENT = "extra_content";

    private RecyclerView mRecyclerView;
    private CharacterAdapter mAdapter;
    private View mScrim;
    private TextView mEntityPersonBtn;
    private TextView mEntityStatusBtn;
    private TextView mEntityDoneBtn;
    private TextView mEntityCancelBtn;
    private TextView mEntityBackBtn;

    private List<Integer> mSelectedCharIndexList;
    private List<Character> mCharacterList;
    private List<String> mChars;

    /**
     * 初始化进入该活动的Intent
     * @param context 上下文
     * @param content 句子
     * @return Intent实例
     */
    public static Intent newIntent(Context context, String content){
        Intent intent = new Intent(context, BigBangActivity.class);
        intent.putExtra(EXTRA_CONTENT, content);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bang);

        // 初始化列表实例
        mCharacterList = new ArrayList<>();
        mRecyclerView = findViewById(R.id.bang_chars);
        mRecyclerView.setLayoutManager(new GridLayoutManager(BigBangActivity.this, 8));
        mChars = TextUtil.splitSentence(getIntent().getStringExtra(EXTRA_CONTENT));
        for(int i = 0; i < mChars.size(); i++){
            Character character = new Character();
            character.mChar = mChars.get(i);
            character.index = i;
            mCharacterList.add(character);
        }
        initMarkSituation();
        mAdapter = new CharacterAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // 初始化遮罩
        mScrim = findViewById(R.id.scrim);
        // 初始化已选择的单字索引列表
        mSelectedCharIndexList = new ArrayList<>();
        // 初始化按钮
        mEntityPersonBtn = findViewById(R.id.bang_entity_one);
        mEntityStatusBtn = findViewById(R.id.bang_entity_two);
        mEntityDoneBtn = findViewById(R.id.bang_entity_done);
        mEntityCancelBtn = findViewById(R.id.bang_entity_cancel);
        mEntityBackBtn = findViewById(R.id.bang_entity_back);

        // 初始化事件
        initEvents();
    }

    // 初始化标注情况，防止重复标注
    private void initMarkSituation(){
        List<JSONObject> entityList = EntityAnnotation.getInstance().getEntityList();
        if(entityList.size() != 0){
            try {
                for(int i = 0; i < entityList.size(); i++){
                    int startOffset = entityList.get(i).getInt(EntityAnnotation.START);
                    int endOffset = entityList.get(i).getInt(EntityAnnotation.END);
                    String entityType = entityList.get(i).getString(EntityAnnotation.ENTITY_TYPE);
                    int colorId = getEntityTypeColor(entityType);
                    for(int j = startOffset; j < endOffset; j++){
                        mCharacterList.get(j).isEnabled = false;
                        mCharacterList.get(j).disabledColorId = colorId;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 在这里面初始化各种事件
     */
    private void initEvents(){
        //设置遮罩点击事件
//        mScrim.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });
        mEntityBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mEntityPersonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEntity(EntityAnnotation.ENTITY_PERSON);
            }
        });

        mEntityStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEntity(EntityAnnotation.ENTITY_STATUS);
            }
        });

        mEntityDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, null);
                finish();
            }
        });

        mEntityCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Integer i: mSelectedCharIndexList){
                    mCharacterList.get(i).isSelected = false;
                    mAdapter.notifyItemChanged(i);
                }
                mSelectedCharIndexList.clear();
            }
        });
    }

    /**
     * 单字视图类
     */
    private class CharacterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mCharView;

        /**
         * 单字视图的构造函数
         * @param view 视图
         */
        private CharacterHolder(View view){
            super(view);
            mCharView = itemView.findViewById(R.id.item_char);
            itemView.setOnClickListener(this);
        }

        private void bind(int i){
            Character c = mCharacterList.get(i);
            mCharView.setText(c.mChar);
//            Log.d(TAG, "bind " + String.valueOf(i));
            if(!c.isEnabled){
                // 无法选中状态
                mCharView.setBackgroundResource(c.disabledColorId);
                mCharView.setTextColor(Color.BLACK);
                return;
            }
            if(c.isSelected){
                // 选中状态
                mCharView.setTextColor(Color.WHITE);
                mCharView.setBackgroundResource(R.color.colorPrimary);
            }else{
                mCharView.setBackgroundResource(android.R.color.white);
                mCharView.setTextColor(Color.BLACK);
            }
        }

        @Override
        public void onClick(View v) {
            int n = getAdapterPosition();
            Log.d(TAG, String.valueOf(n));
            Character c = mCharacterList.get(n);
            if(!c.isEnabled){
                return;
            }
            if(c.isSelected){
                // 取消选中状态
//                Log.d(TAG, "unselect" + String.valueOf(c.index));
                mCharView.setBackgroundResource(android.R.color.white);
                mCharView.setTextColor(Color.BLACK);
                mSelectedCharIndexList.remove(Integer.valueOf(c.index));
                c.isSelected = !c.isSelected;
            }else{
                // 选中状态
//                Log.d(TAG, "select" + String.valueOf(c.index));
                // 首尾选中
                if(mSelectedCharIndexList.size() == 1){ // 已经选中一个
                    if(mSelectedCharIndexList.get(0) <= c.index){
                        for(int i = mSelectedCharIndexList.get(0)+1; i <= c.index; i++){
                            mSelectedCharIndexList.add(i);
                            mCharacterList.get(i).isSelected = true;
                            mAdapter.notifyItemChanged(i);
                        }
                    }else{
                        for(int i = c.index; i < mSelectedCharIndexList.get(0); i++){
                            mSelectedCharIndexList.add(i);
                            mCharacterList.get(i).isSelected = true;
                            mAdapter.notifyItemChanged(i);
                        }
                    }
                }else {
                    mCharView.setTextColor(Color.WHITE);
                    mCharView.setBackgroundResource(R.color.colorPrimary);
                    mSelectedCharIndexList.add(c.index);
                    c.isSelected = !c.isSelected;
                }
            }
//            Log.d(TAG, String.valueOf(c.isSelected));
//            Log.d(TAG, String.valueOf(mCharacterList.get(c.index).isSelected));
        }
    }

    /**
     * 列表的适配器
     */
    private class CharacterAdapter extends RecyclerView.Adapter<CharacterHolder>{

        @NonNull
        @Override
        public CharacterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            // i是类型
            View view = LayoutInflater.from(BigBangActivity.this).inflate(R.layout.item_character, viewGroup, false);
            return new CharacterHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CharacterHolder characterHolder, int i) {
            characterHolder.bind(i);
//            characterHolder.setIsRecyclable(false);
        }

        @Override
        public int getItemCount() {
            return mCharacterList.size();
        }

        @Override
        public int getItemViewType(int position) {
            // 区别不同的view（一个个都是单独的）
            return position;
        }

        @Override
        public void onViewRecycled(@NonNull CharacterHolder holder) {
            super.onViewRecycled(holder);
        }
    }

    private class Character{
        String mChar;
        int index;
        boolean isEnabled = true;
        boolean isSelected = false;

        int disabledColorId;
    }

    private void handleEntity(String entityType){
        Collections.sort(mSelectedCharIndexList);
        int length = mSelectedCharIndexList.size();
        if (length == 0){
            Toast.makeText(this, "未选词", Toast.LENGTH_SHORT).show();
            return;
        }
        int endOffset = mSelectedCharIndexList.get(length-1);
        int startOffset = mSelectedCharIndexList.get(0);
        Log.d(TAG, "StartOffset" + String.valueOf(startOffset));
        Log.d(TAG, "EndOffset" + String.valueOf(endOffset));
        // 不连续的话清除
        if((endOffset - startOffset) != (length-1)){
            Toast.makeText(BigBangActivity.this, "选中的词要连续", Toast.LENGTH_SHORT).show();
            for(Integer i: mSelectedCharIndexList){
                Log.d(TAG, String.valueOf(i));
                mCharacterList.get(i).isSelected = false;
                mAdapter.notifyItemChanged(i);
            }
            mSelectedCharIndexList.clear();
        }else{
            int colorId = getEntityTypeColor(entityType);
            for(int i: mSelectedCharIndexList){
                mCharacterList.get(i).isSelected = false;
                mCharacterList.get(i).isEnabled = false;
                mCharacterList.get(i).disabledColorId = colorId;
                mAdapter.notifyItemChanged(i);
            }
            String entityName = TextUtil.formEntity(mChars, startOffset, endOffset + 1);
            String msg = EntityAnnotation.getInstance().add(entityName, entityType,
                    startOffset, endOffset + 1);
            switch (msg){
                case EntityAnnotation.ENTITY_EXISTS:
                    Toast.makeText(this, "亲，你已经标注过了。", Toast.LENGTH_SHORT).show();
                    mSelectedCharIndexList.clear();
                    break;
                case EntityAnnotation.ENTITY_EXISTS_BUT_DIFF:
                    Toast.makeText(this, "亲，你之前不是这么标注的。", Toast.LENGTH_SHORT).show();
                    showChooseTypeDialog(entityName, startOffset, endOffset + 1);
                    break;
                case EntityAnnotation.ENTITY_ADD_SUCCESS:
                    mSelectedCharIndexList.clear();
                    break;
                case EntityAnnotation.ENTITY_ADD_ERROR:
                    mSelectedCharIndexList.clear();
                    Toast.makeText(this, "添加标注错误。", Toast.LENGTH_SHORT).show();
                    break;
            }
//            Log.d(TAG, "entity name" + entity);
        }
    }

    private void showChooseTypeDialog(final String entityName, final int startOffset, final int endOffset) {
        // TODO: 2018/10/29 选择类型框
        new AlertDialog.Builder(this)
                .setTitle("请选择你要标注的类型")
                .setNegativeButton("人名", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateWhileChooseNewType(EntityAnnotation.ENTITY_PERSON);
                        EntityAnnotation.getInstance().add(entityName, EntityAnnotation.ENTITY_PERSON, startOffset, endOffset);
                    }
                }).setPositiveButton("职位", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateWhileChooseNewType(EntityAnnotation.ENTITY_STATUS);
                EntityAnnotation.getInstance().add(entityName, EntityAnnotation.ENTITY_STATUS, startOffset, endOffset);
            }
        }).show();
    }

    private void updateWhileChooseNewType(String entityType){
        try {
            // 更新当前选择的类型
            int colorId = getEntityTypeColor(entityType);
            for(int i: mSelectedCharIndexList){
                mCharacterList.get(i).isSelected = false;
                mCharacterList.get(i).isEnabled = false;
                mCharacterList.get(i).disabledColorId = colorId;
                mAdapter.notifyItemChanged(i);
            }
            mSelectedCharIndexList.clear();

            // 把之前重复的去年样式
            JSONObject object = EntityAnnotation.getInstance().getRemovedObjectDueToDiff();
            int startOffset = object.getInt(EntityAnnotation.START);
            int endOffset = object.getInt(EntityAnnotation.END);
            for(int i = startOffset; i < endOffset; i++){
                mCharacterList.get(i).disabledColorId = android.R.color.white;
                mAdapter.notifyItemChanged(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int getEntityTypeColor(String entityType){
        int colorId = android.R.color.darker_gray;
        switch (entityType){
            case EntityAnnotation.ENTITY_PERSON:
                colorId = R.color.entity_person;
                break;
            case EntityAnnotation.ENTITY_STATUS:
                colorId = R.color.entity_status;
                break;
        }
        return colorId;
    }
}
