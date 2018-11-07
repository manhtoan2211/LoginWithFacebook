package phamm.toann.testloginapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telecom.Call;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    TextView txtName, txtBirth, txtFriend;
    ProgressDialog progressDialog;
    ImageView imgAvatar;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*getHashKey("phamm.toann.testloginapp");*/

        callbackManager = CallbackManager.Factory.create();
        txtBirth = (TextView) findViewById(R.id.txtBirth);
        txtName = (TextView) findViewById(R.id.txtName);
        txtFriend = (TextView) findViewById(R.id.txtFriend);
        imgAvatar = (ImageView) findViewById(R.id.avatar);

        LoginButton loginButton = (LoginButton) findViewById(R.id.btnlogin);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Waiting data...");
                progressDialog.show();

                String acessToken = loginResult.getAccessToken().getToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        progressDialog.dismiss();
                        Log.d("Toan", "respone " + response.toString());
                        getFacebookData(object);
                    }
                });

                Bundle paramester = new Bundle();
                paramester.putString("fields", "id,email,birthday,friends");
                request.setParameters(paramester);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        // if was login
        if (AccessToken.getCurrentAccessToken() != null) {
            txtName.setText(AccessToken.getCurrentAccessToken().getUserId());
        }
    }

    private void getFacebookData(JSONObject object) {
        try {
            URL profilePicture = new URL("https://graph.facebook.com/" + object.getString("id") + "/picture?width=250&height=250");
            Picasso.get().load(profilePicture.toString()).into(imgAvatar);
            txtName.setText(object.getString("email"));
            txtBirth.setText(object.getString("birthday"));
            txtFriend.setText("Friend: " + object.getJSONObject("friend").getJSONObject("summary").getString("total_count"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getHashKey(String packagename) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(packagename, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                Log.d("Toan", "show key Hash" + Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
