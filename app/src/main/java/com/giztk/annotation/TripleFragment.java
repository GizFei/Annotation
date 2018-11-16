package com.giztk.annotation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
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

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripleFragment extends Fragment {

    private static final String TAG = "TripleFragment";
    private static final int BANG_REQUEST = 5;

    private TripleAnnotation mTripleAnnotation;
    private TripleAdapter mAdapter;
    private RecyclerView mTripleRv;
    private TextView mTripleTitleTv;
    private TextView mTripleContentTv;
    //
//    private AppBarLayout mAppBarLayout;
//    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageView mContentSwitch;
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
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_triple, container, false);

        mTripleRv = view.findViewById(R.id.triple_rv);
        mTripleRv.setLayoutManager(new LinearLayoutManager(getContext()));

        mTripleTitleTv = view.findViewById(R.id.triple_title);
        mTripleContentTv = view.findViewById(R.id.triple_content);
//        mCollapsingToolbarLayout = view.findViewById(R.id.ctl);
//        mAppBarLayout = view.findViewById(R.id.appbar);
        mContentSwitch = view.findViewById(R.id.contentSwitch);

        mTripleContentTv.setMovementMethod(ScrollingMovementMethod.getInstance());
//        mCollapsingToolbarLayout.setExpandedTitleColor(getActivity().getResources().getColor(android.R.color.transparent));
//        mCollapsingToolbarLayout.setContentScrimColor(getActivity().getResources().getColor(R.color.gray));

        queryTriples();

        FloatingActionButton mNextFab = view.findViewById(R.id.fab_next);
        FloatingActionButton mFinishFab = view.findViewById(R.id.fab_finish);
        FloatingActionButton mNewFab = view.findViewById(R.id.fab_new);

        mNextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("确定已经提交了吗？")
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTripleAnnotation.clear();
                                updateUI();
                                queryTriples();
                            }
                        }).show();
            }
        });

        mFinishFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 判断是否所以的关系都判断过
                for(Triple t : mTripleAnnotation.getTriples()){
                    if(t.getStatus() == -2){
                        Toast.makeText(getContext(), "还有未判断正确与否的关系", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                uploadTriples();
            }
        });

        mNewFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                letItBigBang();
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

//        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
//                Log.d(TAG, String.valueOf(mCollapsingToolbarLayout.getContentScrim().getAlpha()));
//            }
//        });

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK){
            if(requestCode == BANG_REQUEST){
                Log.d("Activity Result", data.getStringExtra("NEW_ANNOTATION"));
                try {
                    Triple triple = new Triple(new JSONObject(data.getStringExtra("NEW_ANNOTATION")), false);
                    mTripleAnnotation.addTriple(triple);
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
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
                            if(triplesObject.has("msg")){
                                queryTriples();
                            }else{
                                mTripleAnnotation = new TripleAnnotation(triplesObject);
                                mTripleTitleTv.setText(mTripleAnnotation.getTitle());
                                mTripleContentTv.setText(mTripleAnnotation.getSentContent());
//                                mCollapsingToolbarLayout.setTitle(mTripleAnnotation.getTitle());

                                measureContentHeight();
                                updateUI();
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
                Log.d(TAG, params.toString());
                return params;
            }
        };
        HttpSingleTon.getInstance(getActivity()).addToRequestQueue(request);
    }

    /**
     * 标注新的关系
     */
    private void letItBigBang(){
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                mTripleContentTv, getResources().getString(R.string.transition_name));
        Intent intent = TripleBangActivity.newIntent(getContext(), mTripleAnnotation.toJSONString());
        startActivityForResult(intent, BANG_REQUEST, options.toBundle());
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
     * 测量文本高度
     */
    private void measureContentHeight(){
        ViewTreeObserver observer = mTripleContentTv.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTripleContentTv.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final int height = Math.max(mTripleContentTv.getMeasuredHeight(), 500);
                Log.d(TAG, String.valueOf(height));
                final int originHeight = 120;
                mTripleContentTv.setHeight(originHeight);

                mContentSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTripleContentTv.getHeight() == originHeight) {
                            AnimatedVectorDrawableCompat drawableCompat = AnimatedVectorDrawableCompat.create(
                                    getContext(), R.drawable.av_down_to_up);
                            mContentSwitch.setImageDrawable(drawableCompat);
                            ((Animatable)mContentSwitch.getDrawable()).start();

                            ValueAnimator animator = ObjectAnimator.ofInt(mTripleContentTv, "height", originHeight, height);
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.setDuration(400);
                            animator.start();
                        } else {
                            AnimatedVectorDrawableCompat drawableCompat = AnimatedVectorDrawableCompat.create(
                                    getContext(), R.drawable.av_up_to_down);
                            mContentSwitch.setImageDrawable(drawableCompat);
                            ((Animatable)mContentSwitch.getDrawable()).start();

                            ValueAnimator animator = ObjectAnimator.ofInt(mTripleContentTv, "height", height, originHeight);
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.setDuration(400);
                            animator.start();
                        }
                    }
                });
            }
        });
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

                                queryTriples();
                            }else{
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
        private ImageView mTripleWrong;
        private ImageView mTripleCorrect;
        private ImageView mCorrectIcon;
        private ImageView mWrongIcon;
        private ImageView mDeleteIcon;

        private TripleHolder(View view){
            super(view);

            mTripleLeftEntityTv = itemView.findViewById(R.id.triple_left_entity);
            mTripleRightEntityTv = itemView.findViewById(R.id.triple_right_entity);
            mRevealLayout = itemView.findViewById(R.id.swipe_rl);
            mRelationSpinner = itemView.findViewById(R.id.triple_relation);
            mTripleNew = itemView.findViewById(R.id.triple_new);
            mTripleCorrect = itemView.findViewById(R.id.triple_judge_correct);
            mTripleWrong = itemView.findViewById(R.id.triple_judge_wrong);
            mCorrectIcon = itemView.findViewById(R.id.triple_correct);
            mWrongIcon = itemView.findViewById(R.id.triple_wrong);
            mDeleteIcon = itemView.findViewById(R.id.triple_delete);
        }

        private void bind(final Triple triple){
            mTripleLeftEntityTv.setText(triple.getLeftEntity());
            mTripleRightEntityTv.setText(triple.getRightEntity());
            mRelationSpinner.setSelection(triple.getRelationID());
            Log.d(TAG, "triple is original? " + String.valueOf(triple.isOriginal()));
            if(triple.isOriginal()){
                mRelationSpinner.setEnabled(false);
                if(triple.getStatus() == 1){
                    // 判断正确
                    mTripleWrong.setVisibility(View.GONE);
                    mTripleCorrect.setVisibility(View.VISIBLE);
                }else if(triple.getStatus() == -1){
                    mTripleWrong.setVisibility(View.VISIBLE);
                    mTripleCorrect.setVisibility(View.GONE);
                }else{
                    mTripleWrong.setVisibility(View.GONE);
                    mTripleCorrect.setVisibility(View.GONE);
                    mTripleNew.setVisibility(View.GONE);
                }
            }else{
                // 新标注的图标提示
                mTripleNew.setVisibility(View.VISIBLE);
                mTripleWrong.setVisibility(View.GONE);
                mTripleCorrect.setVisibility(View.GONE);
                // 显示删除按钮
//                mRevealLayout.setLockDrag(true);
                mRelationSpinner.setEnabled(true);
                mWrongIcon.setVisibility(View.GONE);
                mCorrectIcon.setVisibility(View.GONE);
                mDeleteIcon.setVisibility(View.VISIBLE);

                mRelationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        int pos = getAdapterPosition();
                        Triple t = mTripleAnnotation.getTriple(pos);
                        t.setRelationID(position);
                        Log.d(TAG, " " + String.valueOf(position) + "item pos" + String.valueOf(pos) + t.toJSONObject().toString());
                        mTripleAnnotation.updateTriple(t);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                mDeleteIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int pos = getAdapterPosition();
                        new AlertDialog.Builder(getContext())
                                .setTitle("你确定要删除该标注吗？")
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d(TAG, "item pos" + String.valueOf(pos));
                                        mTripleAnnotation.removeTriple(pos);
                                        updateUI();
                                    }
                                }).show();
                    }
                });


                return;
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
                    Triple t = mTripleAnnotation.getTriple(pos);
                    t.setStatus(1);
                    Log.d(TAG, "第几项 "+String.valueOf(pos) + " correct" + t.toJSONObject().toString());
                    mTripleAnnotation.updateTriple(t);
                    mRevealLayout.close(false);
                    mTripleWrong.setVisibility(View.GONE);
                    mTripleCorrect.setVisibility(View.VISIBLE);
                    break;
                case R.id.triple_wrong:
                    Triple t2 = mTripleAnnotation.getTriple(pos);
                    t2.setStatus(-1);
                    Log.d(TAG, "第几项 "+String.valueOf(pos) + " wrong" + t2.toJSONObject().toString());
                    mTripleAnnotation.updateTriple(t2);
                    mRevealLayout.close(false);
                    mTripleWrong.setVisibility(View.VISIBLE);
                    mTripleCorrect.setVisibility(View.GONE);
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

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onViewRecycled(@NonNull TripleHolder holder) {
            super.onViewRecycled(holder);
        }

        private void setTripleList(List<Triple> list){
            mTripleList = list;
        }

        private void saveStates(Bundle outState) {
            mBinderHelper.saveStates(outState);
        }

        private void restoreStates(Bundle inState) {
            mBinderHelper.restoreStates(inState);
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
                mTripleContentTv.setTextSize(slider.getProgress());
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
