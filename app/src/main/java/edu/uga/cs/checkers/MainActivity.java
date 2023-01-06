package edu.uga.cs.checkers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "MainActivity";

    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d( DEBUG_TAG, "MainActivity.onCreate()" );

        Button signInButton = findViewById( R.id.button1 );
        Button registerButton = findViewById( R.id.button2 );

        signInButton.setOnClickListener( new SignInButtonClickListener() );
        registerButton.setOnClickListener( new RegisterButtonClickListener() );

        // Check đã đăng nhập hay chưa nếu chưa thì phải đăng xuất
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if( currentUser != null )
            mAuth.signOut();

        Button playOffline =findViewById( R.id.button);
        playOffline.setOnClickListener(new PlayOfflineListener());

        Button rule=findViewById(R.id.button7);
        rule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), RuleActivity.class);
                v.getContext().startActivity(intent);
            }
        });
    }

    private class PlayOfflineListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), CheckersActivity2.class);
            v.getContext().startActivity(intent);
        }
    }

    private class SignInButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick( View v ) {

            // Đăng nhập bằng email,password
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build()
            );

            // Chạy sign in activity, trả về kết quả đăng nhập
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    private class RegisterButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            // chạy về phần đăng kí
            Intent intent = new Intent(view.getContext(), RegisterActivity.class);
            view.getContext().startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d( DEBUG_TAG, "JobLead: MainActivity.onActivityResult()" );

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Đăng nhập thành công
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                Log.i( "FireBase Test", "Signed in as: " + user.getEmail() );

                // chạy về home của người dùng
                Intent intent = new Intent( this, HomeActivity.class );
                startActivity( intent );

            } else {
                // Đăng nhập thất bại
                Toast.makeText( getApplicationContext(),
                        "Đăng nhập thất bại!!!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}