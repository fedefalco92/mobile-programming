package it.unibs.appwow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.models.UserModel;
import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.notifications.FirebaseInstanceIDService;
import it.unibs.appwow.services.WebServiceRequest;
import it.unibs.appwow.services.WebServiceUri;
import it.unibs.appwow.utils.Validator;
import it.unibs.appwow.utils.graphicTools.Messages;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private final String TAG_LOG = LoginActivity.class.getSimpleName();

    /**
     * Extra tag
     */

    public static final String PASSING_USER_EXTRA = "user";

    /**
     * Stringhe per gestire connessione al server
     */
    private static final int CONN_ERROR = 0;
    private static final int USER_EXISTS = 1;
    private static final int USER_NOT_EXISTS = 2;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View mViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mViewContainer = findViewById(R.id.main_container);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!WebServiceRequest.checkNetwork()){
                    Messages.showSnackbarWithAction(mViewContainer,R.string.error_no_connection,R.string.retry,new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            attemptLogin();
                        }
                    });
                    return;
                }
                attemptLogin();

                /*
                if(WebServiceRequest.checkNetwork()){
                    attemptLogin();
                } else {
                    String err_no_connection_message = getResources().getString(R.string.err_no_connection);
                    Toast.makeText(LoginActivity.this, err_no_connection_message,
                            Toast.LENGTH_LONG).show();
                }*/
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

       /* Button goUnreg = (Button) findViewById(R.id.offilne_mode);
        goUnreg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
               final Intent intent = new Intent(LoginActivity.this,NavigationActivity.class);
                startActivity(intent);
                finish();
            }
        });*/
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.error_permission_contacts, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the mLocalUser entered one.
        if (!TextUtils.isEmpty(password) && !Validator.isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!Validator.isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the mLocalUser login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device mLocalUser's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the mLocalUser hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the mLocalUser.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mMsgToken;
        private JSONObject mResjs = null;
        private boolean mNewUser = false;
        private boolean mConnError = false;
        private LocalUser mLocalUser = null;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            mMsgToken = FirebaseInstanceId.getInstance().getToken();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            int userExists = checkUser(mEmail);
            switch (userExists){
                case USER_EXISTS:
                    String response = "";
                    Uri uri = WebServiceUri.LOGIN_URI;
                    try {
                        URL url = new URL(uri.toString());
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(10000);
                        conn.setConnectTimeout(15000);
                        conn.setRequestMethod("POST");
                        conn.setDoInput(true);
                        conn.setDoOutput(true);

                        Uri.Builder builder;
                        if(mMsgToken != null){
                            builder = new Uri.Builder()
                                    .appendQueryParameter("email", mEmail)
                                    .appendQueryParameter("password", mPassword)
                                    .appendQueryParameter("msg_token", mMsgToken);
                        } else {
                            builder = new Uri.Builder()
                                    .appendQueryParameter("email", mEmail)
                                    .appendQueryParameter("password", mPassword);
                        }


                        String query = builder.build().getEncodedQuery();
                        OutputStream os = conn.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        writer.write(query);
                        writer.flush();
                        writer.close();
                        os.close();

                        conn.connect();
                        int responseCode = conn.getResponseCode();
                        if(responseCode == HttpURLConnection.HTTP_OK){
                            String line = "";
                            BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            while ((line=br.readLine()) != null) {
                                response+=line;
                            }
                        } else {
                            response = "";
                        }
                        if(!response.isEmpty()){
                            //response = response.substring(1,response.length()-1);
                            mResjs = new JSONObject(response);
                            Log.d(TAG_LOG,"Risposta" + mResjs.toString(1));
                        } else {
                            return false;
                        }
                        Log.d(TAG_LOG, "Risposta String: "+ response);

                        UserModel user = UserModel.create(mResjs);
                        UserDAO dao = new UserDAO();
                        dao.open();
                        dao.insertUser(user);
                        dao.close();

                        //int id = mResjs.getInt("id");
                        //String fullname = mResjs.getString("fullName");
                        //mLocalUser = LocalUser.create(id).withEmail(mEmail).withFullName(fullname);
                        mLocalUser = LocalUser.create(user.getId()).withEmail(user.getEmail()).withFullName(user.getFullName());
                        mLocalUser.save(MyApplication.getAppContext());

                    } catch (MalformedURLException e){
                        return false;
                    } catch (IOException e){
                        return false;
                    } catch (JSONException e){
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                case USER_NOT_EXISTS:
                    mNewUser = true;
                    //costruisco un LocalUser con i dati inseriti nel form
                    mLocalUser = new LocalUser(mEmail, mPassword);
                    return true;

                case CONN_ERROR:
                    mConnError = true;
                    return false;

                default: return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                if(mNewUser){
                    Intent ri = new Intent(LoginActivity.this, RegistrationActivity.class);
                    ri.putExtra(PASSING_USER_EXTRA, mLocalUser);
                    startActivity(ri);
                } else {
                    Intent i = new Intent(LoginActivity.this, NavigationActivity.class);
                    startActivity(i);
                    finish();
                }
            } else {
                if(!mConnError){
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                } else {
                    Toast.makeText(getBaseContext(), getString(R.string.error_server_connection), Toast.LENGTH_SHORT).show();
                }
                
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        /**
         * checks if the mLocalUser with email exists
         * @param email
         * @return CONN_ERR if there is a connection error
         *         USER_NOT_EXISTS if mLocalUser doesn't exists on database
         *         USER_EXISTS if mLocalUser exists
         */
        private int checkUser(String email){
            String response = "";
            Uri uri = WebServiceUri.CHECK_USER_URI;
            try {
                URL url = new URL(uri.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("email", mEmail);
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();
                int responseCode = conn.getResponseCode();
                Log.i(TAG_LOG,"Response code = " + responseCode);
                if(responseCode == HttpURLConnection.HTTP_OK){
                    String line = "";
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                } else {
                    return CONN_ERROR;
                }

                if(!response.isEmpty()){
                    Log.d(TAG_LOG,"RISPOSTA_CHECK_USER" + response);
                    return USER_EXISTS;
                } else {
                    return USER_NOT_EXISTS;
                }

            } catch (MalformedURLException e){
                return CONN_ERROR;
            } catch (IOException e){
                return CONN_ERROR;
            }
        }
    }
}

