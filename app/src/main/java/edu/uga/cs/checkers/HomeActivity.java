package edu.uga.cs.checkers;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import static java.lang.Integer.parseInt;


public class HomeActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "HomeActivity";

    EditText friendEmail;
    Button challengeButton;
    TextView checkRec;
    Button logoutButton;

    /**
     * ------- onCreate() ----------------------------------------------------------------------------------
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        friendEmail = findViewById( R.id.friendEmail );
        challengeButton = findViewById( R.id.challengeButton );
        checkRec = findViewById( R.id.checkRec );
        logoutButton = findViewById( R.id.logoutButton );

        /* Popup thách đấu*/
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_challenged, null);
        final View popupView2 = inflater.inflate(R.layout.popup_challenging, null);

        TextView backgroundColor = popupView.findViewById(R.id.challengedBackground);
        backgroundColor.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        backgroundColor = popupView2.findViewById(R.id.textView6);
        backgroundColor.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        final PopupWindow challengedWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        challengedWindow.setOutsideTouchable(false);

        final PopupWindow challengingWindow = new PopupWindow(popupView2, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        challengingWindow.setOutsideTouchable(false);

        final TextView challengingText = popupView2.findViewById(R.id.challengingText);
        final TextView challengedText = popupView.findViewById(R.id.challengedText);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        /* Set Email vào Home Screen */
        checkRec.setText(currentUser.getEmail());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/challenging");
        myRef.setValue("");

        /* Event cho thách đấu*/
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                final String value = dataSnapshot.getValue(String.class);
                Log.d(DEBUG_TAG, "Value is: " + value);

                if (value.equals("")) { }
                else if (value.equals("-"))
                {
                    /* Chấp nhận thách đấu */
                    challengingWindow.dismiss();

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = mAuth.getCurrentUser();

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/challenging");
                    myRef.setValue("");

                    Intent intent = new Intent(getApplicationContext(), CheckersActivity.class);
                    startActivity(intent);
                }
                else if (value.equals("--"))
                {
                    /* Từ chối thách đấu */
                    challengingWindow.dismiss();

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/challenging");
                    myRef.setValue("");

                    Toast.makeText(getApplicationContext(),
                            "Người chơi sợ và bỏ chạy!",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(DEBUG_TAG, "Error", databaseError.toException());
            }
        });

        /* Init khi vừa mới đăng nhập "" */
        myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/playingAgainst");
        myRef.setValue("");
        myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/currentGameID");
        myRef.setValue("");
        myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/updateBoard");
        myRef.setValue("false");
        myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/updateScore");
        myRef.setValue("false");
        myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/playerColor");
        myRef.setValue("");
        myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/challengedBy");
        myRef.setValue("");

        /* Event cho bị thách đấu*/
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String value = dataSnapshot.getValue(String.class);
                Log.d(DEBUG_TAG, "Value is: " + value);
                if (value.equals("")) { }
                else if (value.equals("-"))
                {
                    /* - Hủy thách đấu */
                    Toast.makeText(getApplicationContext(),
                            "Hủy thách đấu!",
                            Toast.LENGTH_LONG).show();

                    challengedWindow.dismiss();

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = mAuth.getCurrentUser();

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/challengedBy");
                    myRef.setValue("");
                }
                else
                {

                    /* Bị thách đấu */
                    challengedText.setText("Bạn được thách đấu bởi " + value + "!");
                    challengedText.setTextColor(getResources().getColor(R.color.colorPrimaryLight));

                    /* Chỉnh ORIENTATION LANDSCAPE(dọc, ngang) */
                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        challengedWindow.showAtLocation(findViewById(R.id.landHome), Gravity.CENTER, 0, 0);
                    else
                        challengedWindow.showAtLocation(findViewById(R.id.homeLayout), Gravity.CENTER, 0, 0);
                          challengedWindow.getContentView().findViewById( R.id.acceptChallenge ).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            challengedWindow.dismiss();
                            /* chấp nhận thách đấu */
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/playingAgainst");
                            myRef.setValue(value);
                            myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/challengedBy");
                            myRef.setValue("");
                            myRef = database.getReference("users/" + value.substring(0, value.length()-4) + "/playingAgainst");
                            myRef.setValue(currentUser.getEmail());
                            myRef = database.getReference("users/" + value.substring(0, value.length()-4) + "/challenging");
                            myRef.setValue("-");
                            myRef = database.getReference("nextID");
                            /* Event cho phòng tiếp theo (NextID) */
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    String value2 = dataSnapshot.getValue(String.class);
                                    int newVal;

                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    FirebaseUser currentUser = mAuth.getCurrentUser();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("nextID");

                                    if (value2 == null)
                                    {
                                        newVal = 1;
                                    }
                                    else
                                    {
                                        newVal = parseInt(value2)+1;
                                    }

                                    /* Game setup */
                                    myRef.setValue(Integer.toString(newVal));

                                    myRef = database.getReference("users/" + value.substring(0, value.length()-4) + "/currentGameID");
                                    myRef.setValue(Integer.toString(newVal));

                                    myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/currentGameID");
                                    myRef.setValue(Integer.toString(newVal));

                                    myRef = database.getReference("users/" + value.substring(0, value.length()-4) + "/playerColor");
                                    myRef.setValue("red");

                                    myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/playerColor");
                                    myRef.setValue("blue");

                                    myRef = database.getReference("games/" + newVal + "/redPlayer/username");
                                    myRef.setValue(value);

                                    myRef = database.getReference("games/" + newVal + "/bluePlayer/username");
                                    myRef.setValue(currentUser.getEmail());

                                    myRef = database.getReference("games/" + newVal + "/gameOver");
                                    myRef.setValue("false");

                                    myRef = database.getReference("games/" + newVal + "/bluePlayer/score");
                                    myRef.setValue("0");

                                    myRef = database.getReference("games/" + newVal + "/redPlayer/score");
                                    myRef.setValue("0");

                                    Random rand = new Random();
                                    int randNum = rand.nextInt(20)+1;

                                    myRef = database.getReference("games/" + newVal + "/nextMove");
                                    if (randNum > 10)
                                        myRef.setValue("red");
                                    else
                                        myRef.setValue("blue");

                                    /* Init vị trí cở trong database */
                                    for (int j = 1; j < 9; j += 1) {
                                        for (int i = 1; i < 9; i += 2) {
                                            if (i == 1 && j % 2 == 1) {
                                                i += 1;
                                            }

                                            final int ii = i;
                                            final int jj = j;

                                            Log.d(DEBUG_TAG, currentUser.getEmail().substring(0, currentUser.getEmail().length()-4));

                                            /* J > 5, cờ xanh */
                                            if (j > 5) {
                                                myRef = database.getReference("games/" + newVal + "/bluePlayer/" + i + j);
                                                myRef.setValue("" + i + j);

                                                myRef.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        String value3 = dataSnapshot.getValue(String.class);

                                                        if (value3.equals("" + ii + jj))
                                                        {

                                                        }
                                                        else
                                                        {

                                                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                                            FirebaseUser currentUser = mAuth.getCurrentUser();

                                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                            DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/updateBoard");
                                                            myRef.setValue("true");

                                                            myRef = database.getReference("users/" + value.substring(0, value.length()-4) + "/updateBoard");
                                                            myRef.setValue("true");
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                                /* J > 5, cờ đỏ */
                                            } else if (j < 4) {
                                                myRef = database.getReference("games/" + newVal + "/redPlayer/" + i + j);
                                                myRef.setValue("" + i + j);

                                                myRef.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        String value4 = dataSnapshot.getValue(String.class);

                                                        if (value4.equals("" + ii + jj))
                                                        {

                                                        }
                                                        else
                                                        {

                                                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                                            FirebaseUser currentUser = mAuth.getCurrentUser();

                                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                            DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/updateBoard");
                                                            myRef.setValue("true");

                                                            myRef = database.getReference("users/" + value.substring(0, value.length()-4) + "/updateBoard");
                                                            myRef.setValue("true");

                                                            //Log.d(DEBUG_TAG, "Red: users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/updateBoard");
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    /* Đến lúc để chơi cờ */
                                    Intent intent = new Intent(getApplicationContext(), CheckersActivity.class);
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    // Không đọc được dữ liệu
                                    Log.w(DEBUG_TAG, "Failed to read value.", error.toException());
                                }
                            });
                        }
                    });
                    //Từ chối
                    challengedWindow.getContentView().findViewById( R.id.declineChallenge ).setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            challengedWindow.dismiss();

                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/challengedBy");
                            myRef.setValue("");

                            myRef = database.getReference("users/" + value.substring(0, value.length()-4) + "/challenging");
                            myRef.setValue("--");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Không đọc được dữ liệu
                Log.w(DEBUG_TAG, "Failed to read value.", error.toException());
            }
        });

        /* Đăng xuất */
        logoutButton.setOnClickListener( new HomeActivity.LogoutButtonClickListener() );

        challengeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = friendEmail.getText().toString();

                try {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("users/" + email.substring(0, email.length() - 4) + "/updateBoard");

                    /* Event cho cập nhật lại bảng */
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {


                                final boolean[] busy = new boolean[1];

                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                FirebaseUser currentUser = mAuth.getCurrentUser();

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("users/" + email.substring(0, email.length() - 4) + "/playingAgainst");
                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        busy[0] = !(snapshot.getValue().toString().equals(""));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                boolean challengedSelf = (email.equals(currentUser.getEmail()));

                                /* Thách đấu thành công */
                                if (!busy[0] && !challengedSelf) {
                                    myRef = database.getReference("users/" + email.substring(0, email.length() - 4) + "/challengedBy");
                                    myRef.setValue(currentUser.getEmail());

                                    myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length() - 4) + "/challenging");
                                    myRef.setValue(email);

                                    challengingText.setText("Thách đấu " + email + "...");
                                    challengingText.setTextColor(getResources().getColor(R.color.colorPrimaryLight));

                                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                                        challengingWindow.showAtLocation(findViewById(R.id.landHome), Gravity.CENTER, 0, 0);
                                    else
                                        challengingWindow.showAtLocation(findViewById(R.id.homeLayout), Gravity.CENTER, 0, 0);


                                    challengingWindow.getContentView().findViewById(R.id.cancelChallenge).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            challengingWindow.dismiss();

                                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                            FirebaseUser currentUser = mAuth.getCurrentUser();

                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                            DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length() - 4) + "/challenging");
                                            myRef.setValue("");

                                            myRef = database.getReference("users/" + email.substring(0, email.length() - 4) + "/challengedBy");
                                            myRef.setValue("-");
                                        }
                                    });
                                }
                                /* Nếu bận */
                                else if (busy[0]) {
                                    Toast.makeText(getApplicationContext(),
                                            "Người chơi đang bận!",
                                            Toast.LENGTH_LONG).show();
                                }
                                /* Nếu từ chối */
                                else {
                                    Toast.makeText(getApplicationContext(),
                                            "Người chơi sợ và bỏ chạy!!!",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                            /* Không thấy người chơi */
                            else {
                                Toast.makeText(getApplicationContext(),
                                        "Người chơi đăng xuất hoặc không tồn tại.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),
                            "Thông tin điền sai!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /**
     * ------- LogoutButtonClickListener ----------------------------------------------------------------------------------
     */
    private class LogoutButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick( View v ) {
            final View view = v;

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()-4) + "/updateBoard");
            myRef.setValue(null);

            AuthUI.getInstance()
                    .signOut(view.getContext())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(view.getContext(), MainActivity.class);
                            view.getContext().startActivity(intent);
                        }
                    });
        }
    }
}
