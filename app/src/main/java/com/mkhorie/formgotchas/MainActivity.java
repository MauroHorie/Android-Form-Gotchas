package com.mkhorie.formgotchas;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener
        , View.OnClickListener {

    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.layoutRoot);
        coordinatorLayout.setOnClickListener(this);
        EditText repeatPasswordEditText = (EditText) findViewById(R.id.editTextRepeatPassword);
        repeatPasswordEditText.setOnEditorActionListener(this);
        Button signUpButton = (Button) findViewById(R.id.buttonSignUp);
        signUpButton.setOnClickListener(this);
        FrameLayout touchContainer = (FrameLayout) findViewById(R.id.touchContainer);
        touchContainer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.layoutRoot:
            case R.id.touchContainer:
                closeSoftKeyboard();
                break;
            case R.id.buttonSignUp:
                signUp();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch(v.getId()) {
            case R.id.editTextRepeatPassword:
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    signUp();
                    return true;
                }
                break;
        }
        return false;
    }

    private void closeSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    private void signUp() {
        Snackbar.make(coordinatorLayout, "Sign Up!", Snackbar.LENGTH_LONG).show();
    }
}
