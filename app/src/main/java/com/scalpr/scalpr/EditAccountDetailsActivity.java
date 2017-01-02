package com.scalpr.scalpr;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.Objects.User;

public class EditAccountDetailsActivity extends AppCompatActivity {

    UserHelper helper;
    HttpResponseListener getUserDetailsResponseListener, updateUserContactInfoResponseListener, updateUserPasswordResponseListener;

    private EditText etFirstName, etLastName, etEmail, etPhone, etPasswordInitial, etPasswordConfirm;
    private Button bUpdateAccount, bChangePassword;
    private ProgressBar pbEditAccount;
    private ScrollView svFields;
    private User user;
    private Context c;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account_details);
        getSupportActionBar().setTitle("Update Profile");

        c = this;

        pbEditAccount = (ProgressBar) findViewById(R.id.pbEditAccount);
        svFields = (ScrollView) findViewById(R.id.svFields);

        etFirstName = (EditText) findViewById(R.id.etFirstNameEditAccount);
        etLastName = (EditText) findViewById(R.id.etLastNameEditAccount);
        etEmail = (EditText) findViewById(R.id.etEmailEditAccount);
        etPhone = (EditText) findViewById(R.id.etPhoneNumberEditAccount);
        etPasswordInitial = (EditText) findViewById(R.id.etPasswordEditAccount);
        etPasswordConfirm = (EditText) findViewById(R.id.etPasswordConfirmEditAccount);

        bUpdateAccount = (Button) findViewById(R.id.bUpdateAccount);
        bChangePassword = (Button) findViewById(R.id.bChangePassword);

        bUpdateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptUpdateProfile();
                View view = ((Activity) c).getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        bChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptUpdatePassword();
                View view = ((Activity) c).getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        helper = new UserHelper(this);

        getUserDetailsResponseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {

            }

            @Override
            public void requestCompleted(String response) {
                pbEditAccount.setVisibility(View.GONE);
                svFields.setVisibility(View.VISIBLE);
                if(!response.equals("0")){
                    user = helper.getUserDetailsFromJson(response);
                    etFirstName.setText(user.getFirstName());
                    etLastName.setText(user.getLastName());
                    etEmail.setText(user.getEmail());
                    etPhone.setText(user.getPhone());
                }else{
                    Toast.makeText(c, "Unable to retrieve your account info. Please try again.",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                pbEditAccount.setVisibility(View.GONE);
                Toast.makeText(c, "Unable to retrieve your account info. Please try again.",Toast.LENGTH_LONG).show();
            }
        };

        updateUserContactInfoResponseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {

            }

            @Override
            public void requestCompleted(String response) {
                if (response.equals("1")) {
                    Toast.makeText(c, "Account info successfully updated", Toast.LENGTH_LONG).show();
                    helper.updateLoggedInUserOnPhone(user);
                } else if (response.equals("-1")){
                    Toast.makeText(c,"Email is already being used", Toast.LENGTH_LONG).show();
                } else if (response.equals("-2")){
                    Toast.makeText(c,"Phone number is already being used", Toast.LENGTH_LONG).show();
                } else{
                    Toast.makeText(c,"Unable to update account. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                Toast.makeText(c,"Unable to update account. Please try again.", Toast.LENGTH_LONG).show();
            }
        };

        updateUserPasswordResponseListener = new HttpResponseListener() {
            @Override
            public void requestStarted() {

            }

            @Override
            public void requestCompleted(String response) {
                if(response.equals("1")){
                    Toast.makeText(c,"Password Changed", Toast.LENGTH_LONG).show();
                    helper.updateLoggedInUserOnPhone(user);
                }else{
                    Toast.makeText(c,"Unable to change password. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void requestEndedWithError(VolleyError error) {
                Toast.makeText(c,"Unable to change password. Please try again.", Toast.LENGTH_LONG).show();
            }
        };

        helper.getUserDetailsRequest(getUserDetailsResponseListener, helper.getLoggedInUser().getUserID());
    }

    private void attemptUpdateProfile(){
        etFirstName.setError(null);
        etLastName.setError(null);
        etEmail.setError(null);
        etPhone.setError(null);

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email =  etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

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
        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(phone)) {
            etPhone.setError(getString(R.string.error_one_method_contact));
            focusView = etPhone;
            focusView.requestFocus();
            return;
        } else {
            if (!TextUtils.isEmpty(email) && !isEmailValid(email)) {
                etEmail.setError(getString(R.string.error_email_invalid));
                focusView = etEmail;
                focusView.requestFocus();
                return;
            }

            if (!TextUtils.isEmpty(phone) && !isPhoneValid(phone)) {
                etPhone.setError(getString(R.string.error_phone_invalid));
                focusView = etPhone;
                focusView.requestFocus();
                return;
            }

        }

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhone(phone);
            user.setEmail(email);

        helper.updateUserContactInfoRequest(updateUserContactInfoResponseListener, user);
    }

    private void attemptUpdatePassword(){
        etPasswordInitial.setError(null);
        etPasswordConfirm.setError(null);

        String pwInitial = etPasswordInitial.getText().toString().trim();
        String pwConfirm = etPasswordConfirm.getText().toString().trim();

        View focusView = null;


        //make sure passwords are valid
        if (TextUtils.isEmpty(pwInitial) || !isPasswordValid(pwInitial)) {
            etPasswordInitial.setError(getString(R.string.error_invalid_password));
            focusView = etPasswordInitial;
            focusView.requestFocus();
            return;
        }

        //make sure passwords are the same
        if(!pwInitial.contentEquals(pwConfirm)){
            etPasswordConfirm.setError(getString(R.string.error_password_nomatch));
            focusView = etPasswordConfirm;
            focusView.requestFocus();
            return;
        }

        helper.updateUserPasswordRequest(updateUserPasswordResponseListener, helper.getLoggedInUser().getUserID(), pwInitial);

    }

    private boolean isPhoneValid(String text) {

        String regexStr = "^[+]?[0-9]{6,20}$";
        //TODO: Replace this with your own logic
        String finalText = text.replaceAll("-", "");
        return finalText.matches(regexStr);
    }

    private boolean isEmailValid(String text) {

        //TODO: Replace this with your own logic
        return text.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
}
