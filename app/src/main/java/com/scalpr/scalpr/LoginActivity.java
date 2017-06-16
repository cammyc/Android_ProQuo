package com.scalpr.scalpr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.HttpResponseListener;

import org.json.JSONObject;

import java.util.Arrays;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{


    HttpResponseListener createAccountResponseListener, loginResponseListener, facebookCreateAccountOrLoginResponseListener, googleCreateAccountOrLoginResponseListener;
    public static final int RC_SIGN_IN = 1;
    public static final String GOOGLE_LOGIN = "GOOGLE_LOGIN";
    public static final String FB_LOGIN = "FB_LOGIN";

    // UI references.
    private EditText etLoginUN, etLoginPW, etFirstName, etLastName, etEmailorPhoneInitialCA, etEmailOrPhoneConfirmCA, etPasswordInitialCA, etPasswordConfirmCA;
    private Button bLogin, bCreateAccount;
    private LoginButton fbLoginButton;
    private View mProgressView;
    private UserHelper loginHelp;
    private VideoView mVideoView;
    Context c;
    String fbFirstName, fbLastName, fbEmail, fbID;
    String googleFirstName, googleLastName, googleEmail, googleDisplayPic, googleID;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!FacebookSdk.isInitialized()){
            FacebookSdk.sdkInitialize(getApplicationContext()); //needs to go before setContentView so can't put in initializeFB()
        }
        AppEventsLogger.activateApp(getApplication());

        setContentView(R.layout.activity_login);
        c = this;
        loginHelp = new UserHelper(c);
        initializeFB();
        initializeGoogleSignIn();
        initializeViews();

        initializeHttpListeners();

        etLoginPW.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.etLoginPassword || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        bLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


        bCreateAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(c, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        mVideoView = (VideoView) findViewById(R.id.login_background);

        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.login_video);

        mVideoView.setVideoURI(uri);
        mVideoView.start();


        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
            @Override
            public void onPrepared(MediaPlayer mediaPlayer){
                mediaPlayer.setLooping(true);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

        mVideoView.start();
    }

    private void initializeVideo(){

    }

    private void initializeFB(){

        fbLoginButton = (LoginButton) findViewById(R.id.login_button);
        fbLoginButton.setReadPermissions(Arrays.asList("public_profile", "email"));


        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                //Application code
                                try{
                                    fbFirstName = object.getString("first_name");
                                    fbLastName = object.getString("last_name");
                                    fbID = object.getString("id");
                                    fbEmail = (!object.isNull("email")) ? object.getString("email") : "";

                                    loginHelp.CreateOrLoginFacebookAccountRequest(facebookCreateAccountOrLoginResponseListener, fbFirstName, fbLastName, fbEmail, fbID);

                                }catch (Exception ex){
                                    Toast.makeText(getApplicationContext(), "Unable to login with Facebook. Please Try Again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Log.v(FB_LOGIN, exception.toString());
            }
        });
    }

    private void initializeGoogleSignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.google_sign_in_button);

        try{
            for (int i = 0; i < signInButton.getChildCount(); i++) {
                View v = signInButton.getChildAt(i);

                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setPadding(0, 0, 20, 0);
                    tv.setText("Sign in with Google");
                    tv.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                            startActivityForResult(signInIntent, RC_SIGN_IN);
                        }
                    });
                    return;
                }
            }
        }catch(Exception ex){
            Log.d("BAD_IDEA", ex.toString());
        }

        //signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(GOOGLE_LOGIN, "handleSignInResult:" + result.getStatus().getStatusCode() + " - " + result.getStatus().getStatusMessage());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            googleFirstName = acct.getGivenName();
            googleLastName = acct.getFamilyName();
            googleEmail = acct.getEmail();
            try {
                googleDisplayPic = acct.getPhotoUrl().toString();
            }catch (Exception ex){
                googleDisplayPic = "";
            }
            googleID = acct.getId();

            loginHelp.CreateOrLoginGoogleAccountRequest(googleCreateAccountOrLoginResponseListener, googleFirstName, googleLastName, googleEmail, googleDisplayPic, googleID);

        } else {
            Toast.makeText(getApplicationContext(), "Google login failed. Please Try Again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeHttpListeners(){
        createAccountResponseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
                showProgress(true);
            }

            @Override
            public void requestCompleted(String response) {
                showProgress(false);
                if(response.equals("-1")){
                    Toast.makeText(getApplicationContext(), "Your Email or Phone # Was Already Taken. Please Try Another One", Toast.LENGTH_SHORT).show();
                }else if(response.equals("0")) {
                    Toast.makeText(getApplicationContext(), "Error Creating Account. Please Try Again.", Toast.LENGTH_SHORT).show();
                }else{
                    loginHelp.confirmLoginAndSaveUser(response);
                    Toast.makeText(getApplicationContext(), "Account Successfully Created", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                showProgress(false);
                Toast.makeText(getApplicationContext(), "Unable to create account. Please try again.", Toast.LENGTH_SHORT).show();
            }
        };

        facebookCreateAccountOrLoginResponseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
                showProgress(true);
            }

            @Override
            public void requestCompleted(String response) {
                showProgress(false);//swith this UI - looks terrible

                Log.d(FB_LOGIN,response);

                if(isNumeric(response)){
                    int intResponse = Integer.parseInt(response);
                    if(intResponse == -1 || intResponse == 0) {
                        LoginManager.getInstance().logOut();
                        Toast.makeText(getApplicationContext(), "Error Creating Account. Please Try Again.", Toast.LENGTH_SHORT).show();
                    }else if(intResponse == -2) {
                        LoginManager.getInstance().logOut();
                        emailTakenAlert("Facebook");
                    }else if(intResponse == -3) {
                        emailRequiredAlert(fbFirstName, fbLastName, fbID);
                    }else{//account created
                        JSONObject jObjectData = new JSONObject();
                        try {

                            jObjectData.put("userID", response);
                            jObjectData.put("firstName", fbFirstName);
                            jObjectData.put("lastName", fbLastName);
                            jObjectData.put("email", fbEmail);
                            jObjectData.put("phoneNumber","");
                            jObjectData.put("password", "");
                            jObjectData.put("displayPicURL", "http://graph.facebook.com/"+fbID+"/picture"); //?type=large

                        }catch (Exception ex){
                            Log.d(FB_LOGIN, ex.toString());
                        }

                        loginHelp.confirmLoginAndSaveUser(jObjectData.toString());
                        Toast.makeText(getApplicationContext(), "Account Successfully Created", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else{
                    loginHelp.confirmLoginAndSaveUser(response);
                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                    finish();
                }

            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                LoginManager.getInstance().logOut();
                showProgress(false);
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        googleCreateAccountOrLoginResponseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {
                showProgress(true);
            }

            @Override
            public void requestCompleted(String response) {
                showProgress(false);//swith this UI - looks terrible

                Log.d(GOOGLE_LOGIN,response);

                if(isNumeric(response)){
                    int intResponse = Integer.parseInt(response);
                    if(intResponse == -1 || intResponse == 0) {
                        Toast.makeText(getApplicationContext(), "Error Creating Account. Please Try Again.", Toast.LENGTH_SHORT).show();
                    }else if(intResponse == -2) {
                        emailTakenAlert("Google");
                    }else{//account created
                        JSONObject jObjectData = new JSONObject();
                        try {

                            jObjectData.put("userID", response);
                            jObjectData.put("firstName", googleFirstName);
                            jObjectData.put("lastName", googleLastName);
                            jObjectData.put("email", googleEmail);
                            jObjectData.put("phoneNumber","");
                            jObjectData.put("password", "");
                            jObjectData.put("displayPicURL", googleDisplayPic);

                        }catch (Exception ex){
                            Log.d(FB_LOGIN, ex.toString());
                        }
                        loginHelp.confirmLoginAndSaveUser(jObjectData.toString());
                        Toast.makeText(getApplicationContext(), "Account Successfully Created", Toast.LENGTH_SHORT).show();
                        finish();
                    }
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

    private void initializeViews() {
        mProgressView = findViewById(R.id.login_progress);

        etLoginUN = (EditText) findViewById(R.id.etLoginUN);
        etLoginPW = (EditText) findViewById(R.id.etLoginPassword);

        bLogin = (Button) findViewById(R.id.bLogin);

/*        etFirstName = (EditText) findViewById(R.id.etFirstNameCreateAccount);
        etLastName = (EditText) findViewById(R.id.etLastNameCreateAccount);
        etEmailorPhoneInitialCA = (EditText) findViewById(R.id.etEmailOrPhoneInitialCreateAccount);
        etEmailOrPhoneConfirmCA = (EditText) findViewById(R.id.etEmailOrPhoneConfirmCreateAccount);
        etPasswordInitialCA = (EditText) findViewById(R.id.etPasswordCreateAccount);
        etPasswordConfirmCA = (EditText) findViewById(R.id.etPasswordConfirmCreateAccount);
*/

        bCreateAccount = (Button) findViewById(R.id.sign_up_button);

        TextView tvForgotPW = (TextView) findViewById(R.id.tvForgotPassword);

        String linkTxt=getResources().getString(R.string.forgotPWLink);

        tvForgotPW.setText(Html.fromHtml(linkTxt));
        tvForgotPW.setClickable(true);
        tvForgotPW.setLinksClickable(true);
        tvForgotPW.setMovementMethod (LinkMovementMethod.getInstance());
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    /*private void attemptCreateAccount(){
        etFirstName.setError(null);
        etLastName.setError(null);
        etEmailorPhoneInitialCA.setError(null);
        etEmailOrPhoneConfirmCA.setError(null);
        etPasswordInitialCA.setError(null);
        etPasswordConfirmCA.setError(null);

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String emailPhoneInitial =  etEmailorPhoneInitialCA.getText().toString().trim();
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
        if(!emailPhoneInitial.contentEquals(emailPhoneConfirm)){
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
        if(!pwInitial.contentEquals(pwConfirm)){
            etPasswordConfirmCA.setError(getString(R.string.error_password_nomatch));
            focusView = etPasswordConfirmCA;
            focusView.requestFocus();
            return;
        }

        loginHelp.CreateAccountRequest(createAccountResponseListener,firstName,lastName,emailPhoneInitial,pwInitial);
    }*/



    private void attemptLogin() {
        // Reset errors.
        etLoginUN.setError(null);
        etLoginPW.setError(null);

        // Store values at the time of the login attempt.
        String email = etLoginUN.getText().toString();
        String password = etLoginPW.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(email.trim())) {
            etLoginUN.setError(getString(R.string.error_field_required));
            focusView = etLoginUN;
            cancel = true;
        } else if (!isEmailOrPhoneValid(email.trim())) {
            etLoginUN.setError(getString(R.string.error_invalid_email_or_phone));
            focusView = etLoginUN;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            etLoginPW.setError(getString(R.string.error_invalid_password));
            focusView = etLoginPW;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
           loginHelp.LoginRequest(loginResponseListener,email,password);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void emailRequiredAlert(final String firstName, final String lastName, final String facebookID){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);
        alertDialog.setTitle("Email Required");
        alertDialog.setMessage("Facebook didn't give us an email address for your account. Please enter one below.");

        final EditText input = new EditText(c);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(20,20,20,20);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setPositiveButton("Done",null);
        alertDialog.setNegativeButton("Cancel",null);

        final AlertDialog mAlertDialog = alertDialog.create();


        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String email = input.getText().toString().trim();
                        if (!isEmailOrPhoneValid(email)) {
                            input.setError(getString(R.string.error_invalid_email_or_phone));
                        }else{
                            loginHelp.CreateOrLoginFacebookAccountRequest(facebookCreateAccountOrLoginResponseListener, firstName, lastName, email, facebookID);
                            mAlertDialog.dismiss();
                        }
                    }
                });

                Button bNeg = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                bNeg.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if(!FacebookSdk.isInitialized()){
                            FacebookSdk.sdkInitialize(c.getApplicationContext());
                        }
                        LoginManager.getInstance().logOut();
                        mAlertDialog.dismiss();
                    }
                });
            }
        });

        mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }
        });

        mAlertDialog.show();
    }

    private void emailTakenAlert(String acctType){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);
        alertDialog.setTitle("Email already in use");
        alertDialog.setMessage("The email associated with your "+acctType+" account is already in our database.\n\nIf you think someone else might be using it please contact us to resolve the issue.");

        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(!FacebookSdk.isInitialized()){
            FacebookSdk.sdkInitialize(this);
        }

        LoginManager.getInstance().logOut();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Unable to connect to Google. Please Try Again.", Toast.LENGTH_SHORT).show();
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

