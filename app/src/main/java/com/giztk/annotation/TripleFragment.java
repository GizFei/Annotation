package com.giztk.annotation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.giztk.util.HttpSingleTon;
import com.giztk.util.HttpUtil;
import com.giztk.util.TripleAnnotation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TripleFragment extends Fragment {

    private static final String TAG = "TripleFragment";

    private TripleAnnotation mTripleAnnotation;

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
        return super.onCreateView(inflater, container, savedInstanceState);
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
                            Log.d(TAG, response);
                            JSONObject triplesObject = new JSONObject(response);
                            mTripleAnnotation = new TripleAnnotation(triplesObject);
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
     * 上传标注好的Triples
     */
    private void uploadTriples(){
        StringRequest request = new StringRequest(Request.Method.POST, HttpUtil.getUploadEntityUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(TAG, response);
                            JSONObject msg = new JSONObject(response);
                            if(msg.getString("msg").equals("上传成功")){
                                Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
                                // todo 上传成功之后的相关操作
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
}
