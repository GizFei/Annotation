package com.giztk.annotation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.giztk.util.HttpUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// 登录活动
public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private static final String PREFERENCES_KEY = "username_psd";
    private static final String PREFERENCES_USERNAME = "username";
    private static final String PREFERENCES_PASSWORD = "password";

    private Button mSignInButton;
    private Button mSignUpButton;
    private TextView mUsernameText;
    private TextView mPasswordText;
    private TextView mErrorText;

    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mSignInButton = findViewById(R.id.btn_sign_in);
        mSignUpButton = findViewById(R.id.btn_sign_up);

        mUsernameText = findViewById(R.id.sign_in_username);
        mPasswordText = findViewById(R.id.sign_in_password);
        mErrorText = findViewById(R.id.error_text);

        mRequestQueue = Volley.newRequestQueue(this);

        // 判断是否有登录过的记录，有则显示用户名密码
        SharedPreferences preferences = this.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        String username = preferences.getString(PREFERENCES_USERNAME, "");
        String password = preferences.getString(PREFERENCES_PASSWORD, "");
        mUsernameText.setText(username);
        mPasswordText.setText(password);

        initEvents();
    }

    private void initEvents(){
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = mUsernameText.getText().toString();
                final String password = mPasswordText.getText().toString();
                if(username.equals("") || password.equals("")){
                    showError("用户名或密码未输入");
                    return;
                }

                StringRequest request = new StringRequest(Request.Method.POST, HttpUtil.getLoginUrl(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    Log.d(TAG, object.toString(4));
                                    String msg = object.getString("msg");
                                    String token = object.getString("token");
                                    HttpUtil.setToken(token);
                                    SharedPreferences preferences = SignInActivity.this.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
                                    preferences.edit().putString(PREFERENCES_USERNAME, username)
                                            .putString(PREFERENCES_PASSWORD, password).apply();
                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } catch (JSONException e) {
                                    showError("用户名或密码错误");
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showError("网络请求错误");
                        error.printStackTrace();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("username", username);
                        params.put("password", password);
                        return params;
                    }
                };
                mRequestQueue.add(request);
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordText.setText("");
                mUsernameText.setText("");
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        mUsernameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mPasswordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void showError(String msg){
        mErrorText.setVisibility(View.VISIBLE);
        mErrorText.setText(msg);
    }

    private void hideError(){
        if(mErrorText.getVisibility() == View.VISIBLE){
            mErrorText.setText("");
            mErrorText.setVisibility(View.GONE);
        }
    }
}
