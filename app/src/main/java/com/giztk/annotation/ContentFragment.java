package com.giztk.annotation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.giztk.util.EntityAnnotation;
import com.giztk.util.HttpSingleTon;
import com.giztk.util.HttpUtil;
import com.giztk.util.TextUtil;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentFragment extends Fragment {

    private static final String TAG = "ContentFragment";

    private static final int BANG_REQUEST = 5;
    private EntityObject mEntityObject;

    private TextView mContentText;
    private TextView mTitleText;
    private CardView mEntityContainer;
    private FloatingActionButton mNextFab;
    private FloatingActionButton mFinishFab;
    private RecyclerView mAnnotationRv;
    private EntityAdapter mEntityAdapter;

    public static ContentFragment newInstance() {

        Bundle args = new Bundle();

        ContentFragment fragment = new ContentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_content, container, false);

        mContentText = view.findViewById(R.id.entity_content);
        mTitleText = view.findViewById(R.id.entity_title);
        mEntityContainer = view.findViewById(R.id.content_container);
        mNextFab = view.findViewById(R.id.fab_next);
        mFinishFab = view.findViewById(R.id.fab_finish);
        mAnnotationRv = view.findViewById(R.id.annotation_rv);
        mAnnotationRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateEntityRv();

        queryContent();

        mContentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                letItBigBang();
            }
        });

        mNextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(EntityAnnotation.getInstance().getEntityList().size() > 0){
                    new AlertDialog.Builder(getActivity())
                            .setTitle("不提交当前标注结果吗")
                            .setPositiveButton("提交", null)
                            .setNegativeButton("不提交", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EntityAnnotation.getInstance().clear();
                                    updateEntityRv();
                                    queryContent();
                                }
                            }).show();
                }else{
//                    EntityAnnotation.getInstance().clear();
//                    updateEntityRv();
                    queryContent();
                }
            }
        });

        mFinishFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(EntityAnnotation.getInstance().getEntityList().size() == 0){
                    Toast.makeText(getContext(), "还没标注呢", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    JSONObject object = EntityAnnotation.getInstance().getEntityResult(mEntityObject.docId,
                            mEntityObject.sentId);
                    uploadEntity(object.toString());
                    Log.d(TAG, object.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_font_size:
                showFontSizeDialog();
                return true;
        }
        return false;
    }

    private void uploadEntity(final String result) {
        StringRequest request = new StringRequest(Request.Method.POST, HttpUtil.getUploadEntityUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject r = new JSONObject(response);
                            String msg = r.getString("msg");
                            if(msg.equals("上传成功")){
                                Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
                                // 清除已提交的标注信息
                                EntityAnnotation.getInstance().clear();
                                updateEntityRv();
                                // 上传成功后自动跳转下一页
                                queryContent();
                            }else{
                                Toast.makeText(getContext(), "提交失败", Toast.LENGTH_SHORT).show();
                            }
                            Log.d(TAG, "upload response" + r.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("token", HttpUtil.getToken());
                params.put("entities", result);
                return params;
            }
        };
        HttpSingleTon.getInstance(getActivity()).addToRequestQueue(request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BANG_REQUEST){
            updateEntityRv();
        }
    }

    private void letItBigBang(){
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                mEntityContainer, getResources().getString(R.string.transition_name));
        Intent intent = BigBangActivity.newIntent(getContext(), mEntityObject.content);
        startActivityForResult(intent, BANG_REQUEST, options.toBundle());
    }

    private void queryContent(){
        StringRequest request = new StringRequest(Request.Method.POST, HttpUtil.getGetEntityUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(TAG, response);
                            JSONObject object = new JSONObject(response);
                            mEntityObject = new EntityObject(object);
                            mTitleText.setText(mEntityObject.title);
                            mContentText.setText(mEntityObject.content);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("token", HttpUtil.getToken());
                Log.d(TAG, params.toString());
                return params;
            }
        };
        HttpSingleTon.getInstance(getActivity()).addToRequestQueue(request);
//        mEntityObject = new EntityObject(TextUtil.getTestContent());
    }

    private void showWaitingDialog(){

    }

    private class EntityObject{
        String title;
        String content;
        String docId;
        int sentId;

        private EntityObject(JSONObject entity){
            try {
                title = entity.getString("title");
                content = entity.getString("content");
                docId = entity.getString("doc_id");
                sentId = entity.getInt("sent_id");
                mContentText.setClickable(true);
            } catch (JSONException e) {
                title = "标题";
                content = "请求错误，点击下一段按钮重试";
                docId = "DOC_ID";
                sentId = -1;
                mContentText.setClickable(false);
                e.printStackTrace();
            }
        }
    }

    private class EntityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mDeleteIcon;
        private JSONObject mEntity;
        private TextView mEntityNameTv;
        private TextView mEntityTypeTv;

        private EntityHolder(View view){
            super(view);
            mDeleteIcon = itemView.findViewById(R.id.entity_delete);
            mDeleteIcon.setOnClickListener(this);
            mEntityNameTv = itemView.findViewById(R.id.entity_name);
            mEntityTypeTv = itemView.findViewById(R.id.entity_type);
        }

        private void bind(JSONObject object){
            mEntity = object;
            try {
                Log.d(TAG, object.toString(4));
                mEntityNameTv.setText(object.getString(EntityAnnotation.ENTITY_NAME));
                mEntityTypeTv.setText(object.getString(EntityAnnotation.ENTITY_TYPE));
            } catch (JSONException e) {
                mEntityNameTv.setText("");
                mEntityTypeTv.setText("");
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("确定删除该标注吗？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                EntityAnnotation.getInstance().delete(mEntity.getString(EntityAnnotation.ENTITY_NAME));
                                updateEntityRv();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).setNegativeButton("取消", null).show();
        }
    }

    private void updateEntityRv() {
        Log.d(TAG, "updateEntityRv()");
        if(mEntityAdapter == null){
            mEntityAdapter = new EntityAdapter(EntityAnnotation.getInstance().getEntityList());
            mAnnotationRv.setAdapter(mEntityAdapter);
        }else{
            mEntityAdapter.setEntityList(EntityAnnotation.getInstance().getEntityList());
            mEntityAdapter.notifyDataSetChanged();
        }
    }

    private class EntityAdapter extends RecyclerView.Adapter<EntityHolder>{

        List<JSONObject> mEntityList;

        private EntityAdapter(List<JSONObject> list){
            mEntityList = list;
        }

        @NonNull
        @Override
        public EntityHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_entity_annotation, viewGroup, false);
            return new EntityHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EntityHolder entityHolder, int i) {
            entityHolder.bind(mEntityList.get(i));
            entityHolder.setIsRecyclable(false);
        }

        @Override
        public int getItemCount() {
            return mEntityList.size();
        }

        private void setEntityList(List<JSONObject> list){
            mEntityList = list;
        }
    }

    private void showFontSizeDialog(){
        final Dialog dialog = new Dialog(getContext());

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_font_size, null);
        final TextView model = view.findViewById(R.id.model);
        final DiscreteSeekBar slider = view.findViewById(R.id.slider);
        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContentText.setTextSize(slider.getProgress());
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        slider.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                model.setTextSize(value);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
        dialog.setCancelable(false);
        dialog.setContentView(view);
        dialog.show();
    }
}
