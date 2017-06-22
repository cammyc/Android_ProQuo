package com.scalpr.scalpr;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.HttpResponseListener;

/**
 * Created by anorris on 2017-06-15.
 */

public class CreateAccountActivity extends AppCompatActivity{

    HttpResponseListener createAccountResponseListener, loginResponseListener;

    public static final int CREATE_ACCOUNT = 2;

    private EditText etFirstName, etLastName, etEmailorPhoneInitialCA, etEmailOrPhoneConfirmCA, etPasswordInitialCA, etPasswordConfirmCA;
    private Button bCreateAccount;
    private UserHelper loginHelp;
    private Context c;
    private View mProgressView;
    private SurfaceView mSurfaceView;
    private MediaPlayer mMediaPlayer;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_account);
        c=this;
        loginHelp = new UserHelper(c);
        initializeViews();
        initializeHttpListeners();


        bCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreateAccount();
            }
        });


        mSurfaceView = (SurfaceView) findViewById(R.id.create_account_background);

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceCreated(SurfaceHolder holder){
                Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.login_video);

                mMediaPlayer = MediaPlayer.create(getApplicationContext(), video, holder);

                mMediaPlayer.setDisplay(holder);

                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.setVolume(0,0);
                mMediaPlayer.start();

            }


            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });
    }




    private void initializeHttpListeners() {
        createAccountResponseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
                showProgress(true);
            }

            @Override
            public void requestCompleted(String response) {
                showProgress(false);
                if (response.equals("-1")) {
                    Toast.makeText(getApplicationContext(), "Your Email or Phone # Was Already Taken. Please Try Another One", Toast.LENGTH_SHORT).show();
                } else if (response.equals("0")) {
                    Toast.makeText(getApplicationContext(), "Error Creating Account. Please Try Again.", Toast.LENGTH_SHORT).show();
                } else {
                    loginHelp.confirmLoginAndSaveUser(response);
                    Toast.makeText(getApplicationContext(), "Account Successfully Created", Toast.LENGTH_SHORT).show();

                    setResult(CREATE_ACCOUNT);
                    finish();
                }
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Unable to create account. Please try again.", Toast.LENGTH_SHORT).show();
            }
        };

        loginResponseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
                showProgress(true);
            }

            @Override
            public void requestCompleted(String response) {
                showProgress(false);
                if(response.equals("1")){
                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                }else if(response.equals("0")) {
                    Toast.makeText(getApplicationContext(), "Incorrect Password", Toast.LENGTH_SHORT).show();
                }else{
                    loginHelp.confirmLoginAndSaveUser(response);
                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                showProgress(false);
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }




    private void initializeViews(){
        etFirstName = (EditText) findViewById(R.id.first_name_edit);
        etLastName = (EditText) findViewById(R.id.last_name_edit);
        etEmailorPhoneInitialCA = (EditText) findViewById(R.id.email_edit);
        etEmailOrPhoneConfirmCA = (EditText) findViewById(R.id.email_confirm_edit);
        etPasswordInitialCA = (EditText) findViewById(R.id.password_edit);
        etPasswordConfirmCA = (EditText) findViewById(R.id.password_confirm_edit);

        bCreateAccount = (Button) findViewById(R.id.create_account_button);

        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptCreateAccount() {
        etFirstName.setError(null);
        etLastName.setError(null);
        etEmailorPhoneInitialCA.setError(null);
        etEmailOrPhoneConfirmCA.setError(null);
        etPasswordInitialCA.setError(null);
        etPasswordConfirmCA.setError(null);

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String emailPhoneInitial = etEmailorPhoneInitialCA.getText().toString().trim();
        String emailPhoneConfirm = etEmailOrPhoneConfirmCA.getText().toString().trim();
        String pwInitial = etPasswordInitialCA.getText().toString().trim();
        String pwConfirm = etPasswordConfirmCA.getText().toString().trim();

        View focusView = null;

        //make sure user entered first and last name
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError(getString(R.string.error_field_required));
            focusView = etFirstName;
            focusView.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError(getString(R.string.error_field_required));
            focusView = etLastName;
            focusView.requestFocus();
            return;
        }


        // Check for a valid email address.
        if (TextUtils.isEmpty(emailPhoneInitial)) {
            etEmailorPhoneInitialCA.setError(getString(R.string.error_field_required));
            focusView = etEmailorPhoneInitialCA;
            focusView.requestFocus();
            return;
        } else if (!isEmailOrPhoneValid(emailPhoneInitial)) {
            etEmailorPhoneInitialCA.setError(getString(R.string.error_invalid_email_or_phone));
            focusView = etEmailorPhoneInitialCA;
            focusView.requestFocus();
            return;
        }

        //make sure emails match, no need to check validity because first one has to be valid
        if (!emailPhoneInitial.contentEquals(emailPhoneConfirm)) {
            etEmailOrPhoneConfirmCA.setError(getString(R.string.error_email_nomatch));
            focusView = etEmailOrPhoneConfirmCA;
            focusView.requestFocus();
            return;
        }


        //make sure passwords are valid
        if (TextUtils.isEmpty(pwInitial) || !isPasswordValid(pwInitial)) {
            etPasswordInitialCA.setError(getString(R.string.error_invalid_password));
            focusView = etPasswordInitialCA;
            focusView.requestFocus();
            return;
        }

        //make sure passwords are the same
        if (!pwInitial.contentEquals(pwConfirm)) {
            etPasswordConfirmCA.setError(getString(R.string.error_password_nomatch));
            focusView = etPasswordConfirmCA;
            focusView.requestFocus();
            return;
        }

        loginHelp.CreateAccountRequest(createAccountResponseListener, firstName, lastName, emailPhoneInitial, pwInitial);
    }


    private boolean isEmailOrPhoneValid(String text) {

        String regexStr = "^[+]?[0-9]{6,20}$";
        //TODO: Replace this with your own logic
        return text.contains("@") || text.replaceAll("-", "").matches(regexStr);
    }


    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
}
