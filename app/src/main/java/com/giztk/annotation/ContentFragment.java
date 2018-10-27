package com.giztk.annotation;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ContentFragment extends Fragment {

    private static final String ARGS_CONTENT = "args_content";

    private String mContent;

    public static ContentFragment newInstance(String content) {

        Bundle args = new Bundle();
        args.putString(ARGS_CONTENT, content);

        ContentFragment fragment = new ContentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContent = getArguments().getString(ARGS_CONTENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content, container, false);

        final TextView content = view.findViewById(R.id.content);
        content.setText(mContent);

        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        content, getResources().getString(R.string.transition_name));
                Intent intent = BigBangActivity.newIntent(getContext(), mContent);
                startActivity(intent, options.toBundle());
            }
        });

        return view;
    }

    public void changeFontSize(float size){
        TextView content = getView().findViewById(R.id.content);
        content.setTextSize(size);
    }
}
