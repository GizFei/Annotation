package com.giztk.annotation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.giztk.util.HttpSingleTon;
import com.giztk.util.HttpUtil;
import com.giztk.util.Triple;
import com.giztk.util.TripleAnnotation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripleFragment extends Fragment {

    private static final String TAG = "TripleFragment";

    private TripleAnnotation mTripleAnnotation;
    private TripleAdapter mAdapter;
    private RecyclerView mTripleRv;
    private TextView mTripleTitleTv;
    private TextView mTripleContentTv;
    //
    private AppBarLayout mAppBarLayout;
    private TextView mCollapseTitle;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    //
    private String mOpenId = "";

    public static TripleFragment newInstance() {

        Bundle args = new Bundle();

        TripleFragment fragment = new TripleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test, container, false);

        mTripleRv = view.findViewById(R.id.triple_rv);
        mTripleRv.setLayoutManager(new LinearLayoutManager(getContext()));

        mTripleTitleTv = view.findViewById(R.id.triple_title);
        mTripleContentTv = view.findViewById(R.id.triple_content);
        mCollapseTitle = view.findViewById(R.id.collapseTitle);
        mTripleContentTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        mAppBarLayout = view.findViewById(R.id.appbar);
        mCollapsingToolbarLayout = view.findViewById(R.id.ctl);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                // Log.d(TAG, "appbar offset" + String.valueOf(i));
//                if(mCollapsingToolbarLayout.getContentScrim() != null){
//                    int alpha = mCollapsingToolbarLayout.getContentScrim().getAlpha();
//                    Log.d(TAG, "appbar offset" + String.valueOf(alpha));
//                    if(alpha < 50)
//                    {
//                        Log.d(TAG, "appbar offset no scrim");
//                        mCollapseTitle.setAlpha(0);
//                    }else{
//                        mCollapseTitle.setAlpha(((float)alpha)/255);
//                    }
//                }
                if(appBarLayout.getTotalScrollRange()+i < mCollapsingToolbarLayout.getScrimVisibleHeightTrigger()){
                    mCollapseTitle.animate().alpha(1).setDuration(mCollapsingToolbarLayout.getScrimAnimationDuration())
                            .setInterpolator(new LinearInterpolator()).start();
                }else{
                    mCollapseTitle.animate().alpha(0).setDuration(mCollapsingToolbarLayout.getScrimAnimationDuration())
                            .setInterpolator(new LinearInterpolator()).start();
                }
            }
        });

        queryTriples();

        FloatingActionButton mNextFab = view.findViewById(R.id.fab_next);
        FloatingActionButton mFinishFab = view.findViewById(R.id.fab_finish);

        mNextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryTriples();
            }
        });

        mFinishFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadTriples();
            }
        });

        mTripleRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(newState == 1){
                    mAdapter.mBinderHelper.closeLayout(mOpenId);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        return view;
    }

    /**
     * 从服务端获取一个Triples
     */
    private void queryTriples(){
        StringRequest request = new StringRequest(Request.Method.POST, HttpUtil.getGetTripleUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject triplesObject = new JSONObject(response);
                            Log.d(TAG, triplesObject.toString(4));
                            mTripleAnnotation = new TripleAnnotation(triplesObject);
                            mTripleTitleTv.setText(mTripleAnnotation.getTitle());
                            mTripleContentTv.setText(mTripleAnnotation.getSentContent());
                            //
                            mCollapseTitle.setAlpha(0);
                            mCollapseTitle.setText(mTripleAnnotation.getTitle());
                            //
                            updateUI();
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
    }

    private void updateUI(){
        if(mAdapter == null){
            mAdapter = new TripleAdapter(mTripleAnnotation.getTriples());
            mTripleRv.setAdapter(mAdapter);
        }else{
            mAdapter.setTripleList(mTripleAnnotation.getTriples());
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 上传标注好的Triples
     */
    private void uploadTriples(){
        StringRequest request = new StringRequest(Request.Method.POST, HttpUtil.getUploadTripleUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject msg = new JSONObject(response);
                            Log.d(TAG, msg.toString(4));
                            if(msg.getString("msg").equals("上传成功")){
                                Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
                                // todo 上传成功之后的相关操作
                                queryTriples();
                            }else{
                                // todo 上传失败后的相关操作
                                Toast.makeText(getContext(), "提交失败", Toast.LENGTH_SHORT).show();
                            }
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
                params.put("triples", mTripleAnnotation.toJSONString());
                Log.d(TAG, params.toString());
                return params;
            }
        };
        HttpSingleTon.getInstance(getActivity()).addToRequestQueue(request);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null) {
            mAdapter.saveStates(outState);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mAdapter != null){
            mAdapter.restoreStates(savedInstanceState);
        }
    }

    private class TripleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTripleLeftEntityTv;
        private TextView mTripleRightEntityTv;
        private SwipeRevealLayout mRevealLayout;
        private Spinner mRelationSpinner;
        private ImageView mTripleNew;
        private ImageView mCorrectIcon;
        private ImageView mWrongIcon;

        private TripleHolder(View view){
            super(view);

            mTripleLeftEntityTv = itemView.findViewById(R.id.triple_left_entity);
            mTripleRightEntityTv = itemView.findViewById(R.id.triple_right_entity);
            mRevealLayout = itemView.findViewById(R.id.swipe_rl);
            mRelationSpinner = itemView.findViewById(R.id.triple_relation);
            mTripleNew = itemView.findViewById(R.id.triple_new);
            mCorrectIcon = itemView.findViewById(R.id.triple_correct);
            mWrongIcon = itemView.findViewById(R.id.triple_wrong);
        }

        private void bind(final Triple triple){
            mTripleLeftEntityTv.setText(triple.getLeftEntity());
            mTripleRightEntityTv.setText(triple.getRightEntity());
            mRelationSpinner.setSelection(triple.getRelationID());
            if(triple.isOriginal()){
                mRelationSpinner.setEnabled(false);
            }else{
                // 新标注的图标提示
                mTripleNew.setVisibility(View.VISIBLE);
                mRevealLayout.setLockDrag(true);
            }

            mCorrectIcon.setOnClickListener(this);
            mWrongIcon.setOnClickListener(this);
            mRevealLayout.setSwipeListener(new SwipeRevealLayout.SwipeListener(){
                @Override
                public void onClosed(SwipeRevealLayout view) {

                }

                @Override
                public void onOpened(SwipeRevealLayout view) {
                    Log.d(TAG, "swipe");
                    mOpenId = triple.getId();
                }

                @Override
                public void onSlide(SwipeRevealLayout view, float slideOffset) {

                }
            });
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d(TAG, "triple click");
            switch (v.getId()){
                case R.id.triple_correct:
                    Log.d(TAG, "第几项 "+String.valueOf(pos) + "correct");
                    break;
                case R.id.triple_wrong:
                    Log.d(TAG, "第几项 " + String.valueOf(pos) + "wrong");
                    break;
            }
        }
    }

    private class TripleAdapter extends RecyclerView.Adapter<TripleHolder>{

        private final ViewBinderHelper mBinderHelper;

        private LayoutInflater mInflater;
        private List<Triple> mTripleList;

        private TripleAdapter(List<Triple> tripleList){
            mBinderHelper = new ViewBinderHelper();
            // 只能打开一个
            mBinderHelper.setOpenOnlyOne(true);
            mTripleList = tripleList;
            mInflater = LayoutInflater.from(getContext());
        }

        @NonNull
        @Override
        public TripleHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = mInflater.inflate(R.layout.item_triple_annotation, viewGroup, false);
            return new TripleHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TripleHolder tripleHolder, int i) {
            Triple t = mTripleList.get(i);
            mBinderHelper.bind(tripleHolder.mRevealLayout, t.getId());

            tripleHolder.bind(t);
        }

        @Override
        public int getItemCount() {
            return mTripleList.size();
        }

        private void setTripleList(List<Triple> list){
            mTripleList = list;
        }

        public void saveStates(Bundle outState) {
            mBinderHelper.saveStates(outState);
        }

        public void restoreStates(Bundle inState) {
            mBinderHelper.restoreStates(inState);
        }
    }
}
