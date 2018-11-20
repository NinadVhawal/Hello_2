package com.a300288873.hello_2;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ListActivity {
    private Firebase mFirebaseRef;
    private EditText mMessageEdit;
    private FirebaseListAdapter<ChatMessage> mListAdapter;
    Button btnSignOut;
    FirebaseAuth auth;
    FirebaseUser user;
    ProgressDialog PD;
    public static int SIGN_IN_REQUEST_CODE = 10;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        PD = new ProgressDialog(this);
        PD.setMessage("Loading...");
        PD.setCancelable(true);
        PD.setCanceledOnTouchOutside(false);

        try {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                // Start sign in/sign up activity
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .build(),
                        SIGN_IN_REQUEST_CODE
                );
            } else {
                // User is already signed in. Therefore, display
                // a welcome Toast
                Toast.makeText(this,
                        "Welcome ",
                        Toast.LENGTH_LONG)
                        .show();

                // Load chat room contents
                displayChatMessages();
            }
        }catch (Exception e){
            Log.d("Authentication"," " + e.toString());
        }
    }

    private void displayChatMessages() {
        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase("https://hellomessagingapp-101b9.firebaseio.com/");

        mMessageEdit = (EditText) this.findViewById(R.id.message_text);

        mListAdapter = new FirebaseListAdapter<ChatMessage>(mFirebaseRef, ChatMessage.class,
                R.layout.message_layout, this) {

            @Override
            protected void populateView(View v, ChatMessage model) {
                ((TextView)v.findViewById(R.id.username_text_view)).setText(model.getName());
                ((TextView)v.findViewById(R.id.message_text_view)).setText(model.getMessage());
            }
        };

        setListAdapter(mListAdapter);
    }



    public void onSendButtonClick(View v) {
        String message = mMessageEdit.getText().toString();
        mUsername = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        mFirebaseRef.push().setValue(new ChatMessage(mUsername, message));
        mMessageEdit.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG)
                        .show();
                displayChatMessages();
            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();

                // Close the app
                finish();
            }
        }

    }

    @Override    protected void onResume() {
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        super.onResume();
    }

    public void onLogoutButtonClick(View v) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
