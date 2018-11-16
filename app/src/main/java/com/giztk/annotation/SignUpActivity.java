package com.giztk.annotation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.giztk.util.HttpUtil;
import com.giztk.util.TextUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// 注册活动
public class SignUpActivity extends AppCompatActivity implements TextWatcher {

    private static final String TAG = "SignUpActivity";

    private ImageView mBackIcon;
    private TextView mUsernameText;
    private TextView mPasswordText;
    private TextView mPassword2Text;
    private TextView mEmailText;
    private Button mSignUpBtn;
    private TextView mErrorText;

    private RequestQueue mRequestQueue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mBackIcon = findViewById(R.id.back);
        mUsernameText = findViewById(R.id.sign_up_username);
        mPasswordText = findViewById(R.id.sign_up_password);
        mPassword2Text = findViewById(R.id.sign_up_password2);
        mEmailText = findViewById(R.id.sign_up_email);
        mSignUpBtn = findViewById(R.id.btn_sign_up);
        mErrorText = findViewById(R.id.error_text);

        mRequestQueue = Volley.newRequestQueue(this);

        initEvents();
    }

    private void initEvents() {
        mBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = mUsernameText.getText().toString();
                final String password = mPasswordText.getText().toString();
                String password2 = mPassword2Text.getText().toString();
                final String email = mEmailText.getText().toString();

                if(username.equals("") || password.equals("") || email.equals("")){
                    showError("用户名或密码或邮箱未输入");
                    return;
                }

                if(!password.equals(password2)){
                    showError("再次输入密码不同");
                    return;
                }

                StringRequest request = new StringRequest(Request.Method.POST, HttpUtil.getRegisterUrl(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try{
                                    Log.d(TAG, new JSONObject(response).toString(4));
                                    String msg = new JSONObject(response).getString("msg");
                                    if(msg.equals("用户名已注册")){
                                        showError("用户名已注册");
                                    }else{
                                        onBackPressed();
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                    showError(e.getMessage());
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                showError(error.getMessage());
                            }
                        }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("username", username);
                        params.put("email", email);
                        params.put("password", password);
                        Log.d(TAG, params.toString());
                        return params;
                    }
                };
                mRequestQueue.add(request);
            }
        });

        mUsernameText.addTextChangedListener(this);
        mPasswordText.addTextChangedListener(this);
        mEmailText.addTextChangedListener(this);
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
}
