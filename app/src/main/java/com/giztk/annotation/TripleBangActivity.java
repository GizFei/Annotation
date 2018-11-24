package com.giztk.annotation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.print.PrinterId;
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

import com.giztk.util.TextUtil;
import com.giztk.util.Triple;
import com.giztk.util.TripleAnnotation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TripleBangActivity extends AppCompatActivity {

    private static final String ARGS_ANNOTATION = "annotation";
    private static final String TAG = "TripleBangActivity";
    private TripleAnnotation mTripleAnnotation;
    private enum ENTITY_TYPE{
        LEFT_ENTITY, RIGHT_ENTITY
    }

    private RecyclerView mRecyclerView;
    private TextView mEntityOneBtn;
    private TextView mEntityTwoBtn;
    private TextView mEntityDoneBtn;
    private TextView mEntityCancelBtn;
    private TextView mEntityBackBtn;
    private TextView mEntityClearBtn;

    private CharacterAdapter mAdapter;

    private List<Integer> mSelectedCharIndexList;
    private List<Character> mCharacterList;
    private List<String> mChars;

    private Triple mTriple;

    public static Intent newIntent(Context context, String annotation){
        Intent intent = new Intent(context, TripleBangActivity.class);
        intent.putExtra(ARGS_ANNOTATION, annotation);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triple_bang);

        String annotation = getIntent().getStringExtra(ARGS_ANNOTATION);
        try {
            mTripleAnnotation = new TripleAnnotation(new JSONObject(annotation));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 初始化变量
        mCharacterList = new ArrayList<>();
        mSelectedCharIndexList = new ArrayList<>();
        mTriple = new Triple();

        mRecyclerView = findViewById(R.id.bang_chars);
        // 横竖屏每行显示字数
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            mRecyclerView.setLayoutManager(new GridLayoutManager(TripleBangActivity.this, 8));
        }
        else {
            //横屏
            mRecyclerView.setLayoutManager(new GridLayoutManager(TripleBangActivity.this,16));
        }
        mChars = TextUtil.splitSentence(mTripleAnnotation.getSentContent());
        for(int i = 0; i < mChars.size(); i++){
            Character character = new Character();
            character.mChar = mChars.get(i);
            character.index = i;
            mCharacterList.add(character);
        }
        initMarkSituation();
        mAdapter = new CharacterAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mEntityOneBtn = findViewById(R.id.bang_entity_one);
        mEntityTwoBtn = findViewById(R.id.bang_entity_two);
        mEntityDoneBtn = findViewById(R.id.bang_entity_done);
        mEntityCancelBtn = findViewById(R.id.bang_entity_cancel);
        mEntityBackBtn = findViewById(R.id.bang_entity_back);
        mEntityClearBtn = findViewById(R.id.bang_entity_clear);

        initEvents();
    }

    private void initEvents() {
        mEntityBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mEntityOneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEntity(ENTITY_TYPE.LEFT_ENTITY);
            }
        });

        mEntityTwoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleEntity(ENTITY_TYPE.RIGHT_ENTITY);
            }
        });

        mEntityDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTriple.getLeftEntity().equals("")){
                    Toast.makeText(TripleBangActivity.this, "还没有选择实体1呢", Toast.LENGTH_SHORT).show();
                }else if(mTriple.getRightEntity().equals("")){
                    Toast.makeText(TripleBangActivity.this, "还没有选择实体2呢", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent();
                    intent.putExtra("NEW_ANNOTATION", mTriple.toJSONObject().toString());
                    Log.d(TAG, intent.getStringExtra("NEW_ANNOTATION"));
                    setResult(RESULT_OK, intent);
                    finish();
                }
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

        mEntityClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Triple> tripleList = mTripleAnnotation.getTriples();
                if(tripleList.size() != 0){
                    for(int i = 0; i < tripleList.size(); i++){
                        // 实体一
                        int leftEStart = tripleList.get(i).getLeftEStart();
                        int leftEENd = tripleList.get(i).getLeftEENd();
                        for(int j = leftEStart; j < leftEENd; j++){
                            mCharacterList.get(j).isEnabled = true;
                            mCharacterList.get(j).isSelected = false;
                            mAdapter.notifyItemChanged(j);
                        }
                        // 实体二
                        int rightEStart = tripleList.get(i).getRightEStart();
                        int rightEEnd = tripleList.get(i).getRightEEnd();
                        for(int j = rightEStart; j < rightEEnd; j++){
                            mCharacterList.get(j).isEnabled = true;
                            mCharacterList.get(j).isSelected = false;
                            mAdapter.notifyItemChanged(j);
                        }
                    }
                }
                for(Integer i: mSelectedCharIndexList){
                    mCharacterList.get(i).isSelected = false;
                    mAdapter.notifyItemChanged(i);
                }
                mSelectedCharIndexList.clear();
            }
        });
    }

    // 初始化标注情况，防止重复标注
    private void initMarkSituation(){
        List<Triple> tripleList = mTripleAnnotation.getTriples();
        if(tripleList.size() != 0){
            for(int i = 0; i < tripleList.size(); i++){
                // 实体一
                int leftEStart = tripleList.get(i).getLeftEStart();
                int leftEENd = tripleList.get(i).getLeftEENd();
                int colorId = getEntityTypeColor(ENTITY_TYPE.LEFT_ENTITY);
                for(int j = leftEStart; j < leftEENd; j++){
                    mCharacterList.get(j).isEnabled = false;
                    mCharacterList.get(j).disabledColorId = colorId;
                }
                // 实体二
                int rightEStart = tripleList.get(i).getRightEStart();
                int rightEEnd = tripleList.get(i).getRightEEnd();
                int colorId2 = getEntityTypeColor(ENTITY_TYPE.RIGHT_ENTITY);
                for(int j = rightEStart; j < rightEEnd; j++){
                    mCharacterList.get(j).isEnabled = false;
                    mCharacterList.get(j).disabledColorId = colorId2;
                }
            }
        }
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
                            Log.d(TAG, String.valueOf(i));
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
            View view = LayoutInflater.from(TripleBangActivity.this).inflate(R.layout.item_character, viewGroup, false);
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

    private void handleEntity(ENTITY_TYPE entityType){
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
            Toast.makeText(TripleBangActivity.this, "选中的词要连续", Toast.LENGTH_SHORT).show();
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

            switch (entityType){
                case LEFT_ENTITY:
                    if(mTriple.getLeftEntity().equals("")){ // 为空
                        mTriple.setLeftEntity(entityName);
                        mTriple.setLeftEStart(startOffset);
                        mTriple.setLeftEENd(endOffset + 1);
                        // 清空
                        mSelectedCharIndexList.clear();
                    }else{
                        Toast.makeText(this, "亲，你之前标注了另一个实体1。", Toast.LENGTH_SHORT).show();
                        showChooseTypeDialog(ENTITY_TYPE.LEFT_ENTITY, entityName, startOffset, endOffset);
                    }
                    break;
                case RIGHT_ENTITY:
                    if(mTriple.getRightEntity().equals("")){
                        mTriple.setRightEntity(entityName);
                        mTriple.setRightEStart(startOffset);
                        mTriple.setRightEEnd(endOffset + 1);
                        mSelectedCharIndexList.clear();
                    }else{
                        Toast.makeText(this, "亲，你之前标注了另一个实体2。", Toast.LENGTH_SHORT).show();
                        showChooseTypeDialog(ENTITY_TYPE.RIGHT_ENTITY, entityName, startOffset, endOffset);
                    }
                    break;
            }
//            Log.d(TAG, "entity name" + entity);
        }
    }

    private class Character{
        String mChar;
        int index;
        boolean isEnabled = true;
        boolean isSelected = false;

        int disabledColorId;
    }

    private int getEntityTypeColor(ENTITY_TYPE entityType){
        int colorId = android.R.color.darker_gray;
        switch (entityType){
            case LEFT_ENTITY:
                colorId = R.color.left_entity;
                break;
            case RIGHT_ENTITY:
                colorId = R.color.right_entity;
                break;
        }
        return colorId;
    }

    private void showChooseTypeDialog(ENTITY_TYPE type, final String entityName, final int startOffset, final int endOffset) {
        // TODO: 2018/10/29 选择类型框
        switch (type){
            case LEFT_ENTITY:
                new AlertDialog.Builder(this)
                        .setTitle("你要更改实体1吗？")
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for(int i: mSelectedCharIndexList){
                                    mCharacterList.get(i).isSelected = false;
                                    mCharacterList.get(i).isEnabled = true;
                                    mAdapter.notifyItemChanged(i);
                                }
                                mSelectedCharIndexList.clear();
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int originLeftStart = mTriple.getLeftEStart();
                                int originLeftEnd = mTriple.getLeftEENd();
                                for(int i = originLeftStart; i < originLeftEnd; i++){
                                    mCharacterList.get(i).isSelected = false;
                                    mCharacterList.get(i).isEnabled = true;
                                    mAdapter.notifyItemChanged(i);
                                }

                                mTriple.setLeftEntity(entityName);
                                mTriple.setLeftEStart(startOffset);
                                mTriple.setLeftEENd(endOffset + 1);

                                mSelectedCharIndexList.clear();
                        }
                    }).show();
                break;
            case RIGHT_ENTITY:
                new AlertDialog.Builder(this)
                        .setTitle("你要更改实体2吗？")
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for(int i: mSelectedCharIndexList){
                                    mCharacterList.get(i).isSelected = false;
                                    mCharacterList.get(i).isEnabled = true;
                                    mAdapter.notifyItemChanged(i);
                                }
                                mSelectedCharIndexList.clear();
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int originRightStart = mTriple.getRightEStart();
                                int originRightEnd = mTriple.getRightEEnd();
                                for(int i = originRightStart; i < originRightEnd; i++){
                                    mCharacterList.get(i).isSelected = false;
                                    mCharacterList.get(i).isEnabled = true;
                                    mAdapter.notifyItemChanged(i);
                                }

                                mTriple.setRightEntity(entityName);
                                mTriple.setRightEStart(startOffset);
                                mTriple.setRightEEnd(endOffset + 1);

                                mSelectedCharIndexList.clear();
                            }
                    }).show();
                break;
        }

    }
}
