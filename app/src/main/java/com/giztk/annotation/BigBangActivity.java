package com.giztk.annotation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.giztk.util.TextUtil;

import java.util.List;

public class BigBangActivity extends AppCompatActivity {

    private static final String EXTRA_CONTENT = "extra_content";

    private RecyclerView mRecyclerView;
    private CharacterAdapter mAdapter;
    private View mScrim;

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
        mRecyclerView = findViewById(R.id.bang_chars);
        mRecyclerView.setLayoutManager(new GridLayoutManager(BigBangActivity.this, 8));
        mAdapter = new CharacterAdapter(TextUtil.splitSentence(getIntent().getStringExtra(EXTRA_CONTENT)));
        mRecyclerView.setAdapter(mAdapter);

        // 初始化遮罩
        mScrim = findViewById(R.id.scrim);

        // 初始化事件
        initEvents();
    }

    /**
     * 在这里面初始化各种事件
     */
    private void initEvents(){
        //设置遮罩点击事件
        mScrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /**
     * 单字视图类
     */
    private class CharacterHolder extends RecyclerView.ViewHolder{

        private TextView mCharView;

        /**
         * 单字视图的构造函数
         * @param view 视图
         */
        private CharacterHolder(View view){
            super(view);
            mCharView = itemView.findViewById(R.id.item_char);
        }

        /**
         * 绑定视图，赋字
         * @param text 单字
         */
        private void bind(String text){
            mCharView.setText(text);
        }
    }

    /**
     * 列表的适配器
     */
    private class CharacterAdapter extends RecyclerView.Adapter<CharacterHolder>{

        private List<String> mChars;

        /**
         * 适配器构造函数
         * @param chars 单字列表
         */
        private CharacterAdapter(List<String> chars){
            mChars = chars;
        }

        @NonNull
        @Override
        public CharacterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(BigBangActivity.this).inflate(R.layout.item_character, viewGroup, false);
            return new CharacterHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CharacterHolder characterHolder, int i) {
            characterHolder.bind(mChars.get(i));
        }

        @Override
        public int getItemCount() {
            return mChars.size();
        }
    }
}
