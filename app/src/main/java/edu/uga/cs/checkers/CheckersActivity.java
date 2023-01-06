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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.Integer.parseInt;


public class CheckersActivity extends AppCompatActivity {

    private static final String TAG = "Checkers: ";
    Checker[][] board;
    ImageView[][] imageViews;
    TextView red;
    TextView blue;
    int redScore = 0;
    int blueScore = 0;
    final String[] myColor = new String[1];
    final String[] gameID = new String[1];
    String turn = "";
    Boolean chainning = true;
    final Map<String, String> locations = new HashMap<>();
    float rotation = 0;
    Button forfeit;

    /**
     * ---- onCreate() --------------------------------------------------------------------------------------------------------------------------------------------------
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkers_main);

        forfeit = findViewById(R.id.forfeit_button);

        Log.d(TAG, "CheckersActivity.onCreate()");


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance(); /* <-- FireBase Jumbo */

        DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length() - 4) + "/updateScore");

        /* Quản lý điểm trong một trận */
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String value = dataSnapshot.getValue(String.class);
                if (value.equals("true")) {
                    UpdateScores();
                }

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length() - 4)
                        + "/updateScore");

                myRef.setValue("false");

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length() - 4) + "/currentGameID");

        /* Event cho game hiện tại */
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);

                gameID[0] = value;

                red = findViewById(R.id.score1);
                blue = findViewById(R.id.score2);
                UpdateScores();

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length() - 4) + "/playerColor");

                /* Event liên quan đến màu người chơi(set game thắng theo ai, lượt chơi )*/
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String value = dataSnapshot.getValue(String.class);
                        myColor[0] = value;
                        LinearLayout gameLayout = findViewById(R.id.gamelayout);
                        if (myColor[0].equals("blue")) {
                            gameLayout.setRotation(180);
                            rotation = 180;
                        }
                        forfeit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/gameOver");

                                if (myColor[0].equals("red")) {
                                    myRef.setValue("red");
                                } else {
                                    myRef.setValue("blue");
                                }
                            }
                        });

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/nextMove");

                        Log.d(TAG, "PATH: games/" + gameID[0] + "/nextMove");

                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                TextView currentTurn = findViewById(R.id.currentTurn);

                                if (dataSnapshot.getValue().toString().equals("red") && myColor[0].equals("red")) {
                                    currentTurn.setTextColor(getResources().getColor(R.color.red));
                                    currentTurn.setText("Đến lượt của bạn!");
                                    turn = "red";
                                } else if (dataSnapshot.getValue().toString().equals("red") && myColor[0].equals("blue")) {
                                    currentTurn.setTextColor(getResources().getColor(R.color.red));
                                    currentTurn.setText("Lượt của đỏ!");
                                    turn = "red";
                                } else if (dataSnapshot.getValue().toString().equals("blue") && myColor[0].equals("red")) {
                                    currentTurn.setTextColor(getResources().getColor(R.color.blue));
                                    currentTurn.setText("Lượt của xanh!");
                                    turn = "blue";
                                } else if (dataSnapshot.getValue().toString().equals("blue") && myColor[0].equals("blue")) {
                                    currentTurn.setTextColor(getResources().getColor(R.color.blue));
                                    currentTurn.setText("Đến lượt của bạn!");
                                    turn = "blue";
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        myRef = database.getReference("games/" + gameID[0] + "/gameOver");
                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue().toString().equals("true")) {
                                    calculateWinner();
                                } else if (dataSnapshot.getValue().toString().equals("red")) {
                                    //Đỏ đầu hàng

                                    forfeited("red");
                                } else if (dataSnapshot.getValue().toString().equals("blue")) {
                                    //Xanh đầu hàng

                                    forfeited("blue");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        updateBoard();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length() - 4) + "/updateBoard");
        /* Event cho cập nhật lại bảng */
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                try {
                    if (value.equals("false")) {

                    } else {
                        // Cập nhật lại rồi chỉnh về false
                        updateBoard();

                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("users/" + currentUser.getEmail().substring(0, currentUser.getEmail().length()
                                - 4) + "/updateBoard");
                        myRef.setValue("false");
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }

    /**
     * ---- resetOrInitiateArrays() -------------------------------------------------------------------------------------------------------------------------------------
     */
    public void resetOrInitiateArrays() {
        imageViews = new ImageView[9][9];
        board = new Checker[9][9];

        imageViews[0][0] = findViewById(R.id.hidden);
        imageViews[1][8] = findViewById(R.id.circle81);
        imageViews[3][8] = findViewById(R.id.circle83);
        imageViews[5][8] = findViewById(R.id.circle85);
        imageViews[7][8] = findViewById(R.id.circle87);
        imageViews[2][7] = findViewById(R.id.circle72);
        imageViews[4][7] = findViewById(R.id.circle74);
        imageViews[6][7] = findViewById(R.id.circle76);
        {imageViews[8][7] = findViewById(R.id.circle78);
        imageViews[1][6] = findViewById(R.id.circle61);
        imageViews[3][6] = findViewById(R.id.circle63);
        imageViews[5][6] = findViewById(R.id.circle65);
        imageViews[7][6] = findViewById(R.id.circle67);
        imageViews[2][5] = findViewById(R.id.circle52);
        imageViews[4][5] = findViewById(R.id.circle54);
        imageViews[6][5] = findViewById(R.id.circle56);
        imageViews[8][5] = findViewById(R.id.circle58);
        imageViews[1][4] = findViewById(R.id.circle41);
        imageViews[3][4] = findViewById(R.id.circle43);
        imageViews[5][4] = findViewById(R.id.circle45);
        imageViews[7][4] = findViewById(R.id.circle47);
        imageViews[2][3] = findViewById(R.id.circle32);
        imageViews[4][3] = findViewById(R.id.circle34);
        imageViews[6][3] = findViewById(R.id.circle36);
        imageViews[8][3] = findViewById(R.id.circle38);
        imageViews[1][2] = findViewById(R.id.circle21);
        imageViews[3][2] = findViewById(R.id.circle23);
        imageViews[5][2] = findViewById(R.id.circle25);
        imageViews[7][2] = findViewById(R.id.circle27);
        imageViews[2][1] = findViewById(R.id.circle12);
        imageViews[4][1] = findViewById(R.id.circle14);
        imageViews[6][1] = findViewById(R.id.circle16);
        imageViews[8][1] = findViewById(R.id.circle18);}
    }

    /**
     * ---- updateBoard() -----------------------------------------------------------------------------------------------------------------------------------------------
     */
    public void updateBoard() {
        resetOrInitiateArrays();
        getPieceLocationsFromDB();
    }

    /**
     * ---- getPieceLocationsFromDB() -----------------------------------------------------------------------------------------------------------------------------------
     */
    public void getPieceLocationsFromDB() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef;
        for (int j = 1; j < 9; j += 1) {
            for (int i = 1; i < 9; i += 2) {
                if (i == 1 && j % 2 == 1) {
                    i += 1;
                }
                final int ii = i;
                final int jj = j;
                /* Chạy qua bàng cờ */
                if (j > 5) { // Xanh
                    myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/" + i + j);

                    /* Event cho cờ xanh */
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String value = dataSnapshot.getValue(String.class);
                            updateLocationsMap("" + ii + jj, value);

                            /* Lấy vị trí hiện tại của cờ */
                            final int vali = Integer.parseInt(value.substring(0, 1));
                            final int valj = Integer.parseInt(value.substring(1, 2));

                            /* Chỉnh display cho cờ ở vị trí hiện tại */
                            imageViews[vali][valj].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (board[vali][valj] != null) {
                                        DisplayMovingOptions(board[vali][valj].getX(), board[vali][valj].getY(), board[vali][valj].getColor());
                                    }
                                }
                            });

                            /* Chỉ cờ vào vị trí */
                            Checker checker = new Checker(imageViews[vali][valj], vali, valj, "Blue");
                            if (rotation == 0)
                                checker.getImageView().setImageResource(R.drawable.blue_piece);
                            else checker.getImageView().setImageResource(R.drawable.blue_piecer);
                            board[vali][valj] = checker;
                            clearUpNonLocations();
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.d(TAG, "Không lấy được dữ liệu");
                        }
                    });

                } else if (j < 4) { // Đỏ
                    myRef = database.getReference("games/" + gameID[0] + "/redPlayer/" + i + j);

                    /* Event cho cờ đỏ */
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String value = dataSnapshot.getValue(String.class);
                            updateLocationsMap("" + ii + jj, value);
                            /* Lấy vị trí hiện tại của cờ */
                            final int vali = Integer.parseInt(value.substring(0, 1));
                            final int valj = Integer.parseInt(value.substring(1, 2));

                            /* Chỉnh display cho cờ ở vị trí hiện tại */
                            imageViews[vali][valj].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (board[vali][valj] != null) {
//                                        Log.d("TAG", "CLICKED ON imageViews[" + vali + "][" + valj + "]");
                                        DisplayMovingOptions(board[vali][valj].getX(), board[vali][valj].getY(), board[vali][valj].getColor());
                                    }
                                }
                            });

                            /* Chỉ cờ vào vị trí */
                            Checker checker = new Checker(imageViews[vali][valj], vali, valj, "Red");
                            if (rotation == 0)
                                checker.getImageView().setImageResource(R.drawable.red_piece);
                            else checker.getImageView().setImageResource(R.drawable.red_piecer);
                            board[vali][valj] = checker;
//                            UpdateScores();
                            clearUpNonLocations();
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.d(TAG, "FAILED TO READ VALUE RED");
                        }
                    });
                }
            }
        }

    }

    /**
     * ---- updateLocationsMap() ----------------------------------------------------------------------------------------------------------------------------------------
     */
    private void updateLocationsMap(String key, String value) {
        locations.put(key, value);
    }

    /**
     * ---- sendPieceLocationsToDB() ------------------------------------------------------------------------------------------------------------------------------------
     */
    public void sendPieceLocationsToDB(int previ, int prevj, int newi, int newj) {
        /* Đi tới ô king */
        boolean touchdown = false;
        if (newj == 1 || newj == 8) {
            touchdown = true;
        }
        /* Nhảy */
        boolean jumped = false;
        if (newi - previ == 2 || previ - newi == 2) {
            jumped = true;
        }
        /* Chuổi */
        if (!jumped || touchdown) chainning = false;

        if (touchdown && !jumped) {
            Iterator it = locations.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getValue().toString().equals("" + previ + prevj)) {
                    locations.put(pair.getKey().toString(), "00");
                }

                if (myColor[0].equals("blue")) {
                    /*Tăng điểm xanh +5 */
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/score");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String value = dataSnapshot.getValue(String.class);

                            myRef.setValue(((Integer.parseInt(value)) + 5) + "");

                            final String[] username = new String[1];

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/redPlayer/username");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    username[0] = snapshot.getValue().toString();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users/" + username[0].substring(0, username[0].length() - 4) + "/updateScore");

                                    myRef.setValue("true");

                                    Log.d(TAG, "Red more moves? .. " + checkIfPlayerHasAvailableMoves("red"));
                                    Log.d(TAG, "Blue more moves? .. " + checkIfPlayerHasAvailableMoves("blue"));

                                    /* Kiểm tra xem game kết thúc chưa */
                                    if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                                        Log.d(TAG, "game over");
                                        myRef = database.getReference("games/" + gameID[0] + "/gameOver");
                                        myRef.setValue("true");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });

                            final String[] username2 = new String[1];

                            database = FirebaseDatabase.getInstance();
                            myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/username");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    username2[0] = snapshot.getValue().toString();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users/" + username2[0].substring(0, username2[0].length() - 4) + "/updateScore");

                                    myRef.setValue("true");

                                    Log.d(TAG, "Red more moves? .. " + checkIfPlayerHasAvailableMoves("red"));
                                    Log.d(TAG, "Blue more moves? .. " + checkIfPlayerHasAvailableMoves("blue"));

                                    /* Kiểm tra xem game kết thúc chưa */
                                    if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                                        Log.d(TAG, "game over");
                                        myRef = database.getReference("games/" + gameID[0] + "/gameOver");
                                        myRef.setValue("true");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.d(TAG, "Error");
                        }
                    });
                } else if (myColor[0].equals("red")) {
                    /*Tăng điểm đỏ +5 */
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef2 = database.getReference("games/" + gameID[0] + "/redPlayer/score");
                    myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String value = dataSnapshot.getValue(String.class);

                            myRef2.setValue(((Integer.parseInt(value)) + 5) + "");

                            final String[] username = new String[1];

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/redPlayer/username");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    username[0] = snapshot.getValue().toString();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users/" + username[0].substring(0, username[0].length() - 4) + "/updateScore");

                                    myRef.setValue("true");

                                    /* Kiểm tra xem game kết thúc chưa */
                                    if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                                        Log.d(TAG, "game over");
                                        myRef = database.getReference("games/" + gameID[0] + "/gameOver");
                                        myRef.setValue("true");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });

                            final String[] username2 = new String[1];

                            database = FirebaseDatabase.getInstance();
                            myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/username");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    username2[0] = snapshot.getValue().toString();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users/" + username2[0].substring(0, username2[0].length() - 4) + "/updateScore");

                                    myRef.setValue("true");

                                    /* Kiểm tra xem game kết thúc chưa */
                                    if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                                        Log.d(TAG, "game over");
                                        myRef = database.getReference("games/" + gameID[0] + "/gameOver");
                                        myRef.setValue("true");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.d(TAG, "Error");
                        }
                    });

                }
            }
        }

        if (touchdown && jumped) {
            Iterator it = locations.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getValue().toString().equals("" + previ + prevj)) {
                    locations.put(pair.getKey().toString(), "00");
                }

                int iOfJumpee = 0;
                int jOfJumpee = 0;

                if (myColor[0].equals("blue")) {
                    jOfJumpee = prevj - 1;

                    if (previ > newi) {
                        Log.d("GET JUMPED KID", "BLUE-JUMP-LEFT");
                        iOfJumpee = previ - 1;
                    } else {
                        Log.d("GET JUMPED KID", "BLUE-JUMP-RIGHT");
                        iOfJumpee = previ + 1;
                    }

                    /*Tăng điểm xanh +6 */
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/score");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String value = dataSnapshot.getValue(String.class);

                            myRef.setValue(((Integer.parseInt(value)) + 6) + "");

                            final String[] username = new String[1];

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/redPlayer/username");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    username[0] = snapshot.getValue().toString();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users/" + username[0].substring(0, username[0].length() - 4) + "/updateScore");

                                    myRef.setValue("true");

                                    /* Kiểm tra xem game kết thúc chưa */
                                    if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                                        Log.d(TAG, "game over");
                                        myRef = database.getReference("games/" + gameID[0] + "/gameOver");
                                        myRef.setValue("true");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });

                            final String[] username2 = new String[1];

                            database = FirebaseDatabase.getInstance();
                            myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/username");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    username2[0] = snapshot.getValue().toString();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users/" + username2[0].substring(0, username2[0].length() - 4) + "/updateScore");

                                    myRef.setValue("true");

                                    /* Kiểm tra xem game kết thúc chưa */
                                    if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                                        Log.d(TAG, "game over");
                                        myRef = database.getReference("games/" + gameID[0] + "/gameOver");
                                        myRef.setValue("true");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.d(TAG, "Error");
                        }
                    });
                } else if (myColor[0].equals("red")) {

                    jOfJumpee = prevj + 1;


                    if (previ > newi) {
                        Log.d("GET JUMPED KID", "RED-JUMP-LEFT");

                        iOfJumpee = previ - 1;
                    } else {
                        Log.d("GET JUMPED KID", "RED-JUMP-RIGHT");
                        iOfJumpee = previ + 1;
                    }

                    /*Tăng điểm đỏ + 6 */
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef2 = database.getReference("games/" + gameID[0] + "/redPlayer/score");
                    myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String value = dataSnapshot.getValue(String.class);

                            myRef2.setValue(((Integer.parseInt(value)) + 6) + "");

                            final String[] username = new String[1];

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/redPlayer/username");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    username[0] = snapshot.getValue().toString();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users/" + username[0].substring(0, username[0].length() - 4) + "/updateScore");

                                    myRef.setValue("true");

                                    /* Kiểm tra game kết thúc chưa */
                                    if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                                        Log.d(TAG, "game over");
                                        myRef = database.getReference("games/" + gameID[0] + "/gameOver");
                                        myRef.setValue("true");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });

                            final String[] username2 = new String[1];

                            database = FirebaseDatabase.getInstance();
                            myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/username");
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    username2[0] = snapshot.getValue().toString();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("users/" + username2[0].substring(0, username2[0].length() - 4) + "/updateScore");

                                    myRef.setValue("true");

                                    /* Kiểm tra game kết thúc chưa */
                                    if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                                        Log.d(TAG, "game over");
                                        myRef = database.getReference("games/" + gameID[0] + "/gameOver");
                                        myRef.setValue("true");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.d(TAG, "Error");
                        }
                    });

                }

                /* Updates vị trí cờ nhảy và tăng điểm */
                Iterator it2 = locations.entrySet().iterator();
                while (it2.hasNext()) {
                    Map.Entry pair2 = (Map.Entry) it2.next();

                    if (pair2.getValue().toString().equals("" + iOfJumpee + jOfJumpee)) {
                        locations.put(pair2.getKey().toString(), "" + "00");
                    }
                }

            }
        }

        /* Updates vị trí cờ đi */
        if (!touchdown) {
            Iterator it = locations.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getValue().toString().equals("" + previ + prevj)) {
                    locations.put(pair.getKey().toString(), "" + newi + newj);
                }
                //Log.d(TAG, "Locations: " + pair.getKey().toString() + ", " + pair.getValue().toString());
            }

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef;

            /* Kiểm tra game kết thúc */
            if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                Log.d(TAG, "game over");
                myRef = database.getReference("games/" + gameID[0] + "/gameOver");
                myRef.setValue("true");
            }

        }

        if (jumped && !touchdown) {
            int iOfJumpee = 0;
            int jOfJumpee = 0;

            if (myColor[0].equals("blue")) {
                jOfJumpee = prevj - 1;

                if (previ > newi) {
                    Log.d("GET JUMPED KID", "BLUE-JUMP-LEFT");
                    iOfJumpee = previ - 1;
                } else {
                    Log.d("GET JUMPED KID", "BLUE-JUMP-RIGHT");
                    iOfJumpee = previ + 1;
                }


                /*Tăng điểm Blue +1*/
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/score");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class);

                        myRef.setValue(((Integer.parseInt(value)) + 1) + "");

                        final String[] username = new String[1];

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/redPlayer/username");
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                username[0] = snapshot.getValue().toString();

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("users/" + username[0].substring(0, username[0].length() - 4) + "/updateScore");

                                myRef.setValue("true");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                        final String[] username2 = new String[1];

                        database = FirebaseDatabase.getInstance();
                        myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/username");
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                username2[0] = snapshot.getValue().toString();

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("users/" + username2[0].substring(0, username2[0].length() - 4) + "/updateScore");

                                myRef.setValue("true");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.d(TAG, "FAILED TO Update Score");
                    }
                });


            } else if (myColor[0].equals("red")) {
                jOfJumpee = prevj + 1;
                if (previ > newi) {
                    Log.d("GET JUMPED KID", "RED-JUMP-LEFT");
                    iOfJumpee = previ - 1;
                } else {
                    Log.d("GET JUMPED KID", "RED-JUMP-RIGHT");
                    iOfJumpee = previ + 1;
                }

                /*Tăng điểm Red +1*/
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                final DatabaseReference myRef2 = database.getReference("games/" + gameID[0] + "/redPlayer/score");
                myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class);

                        myRef2.setValue(((Integer.parseInt(value)) + 1) + "");

                        final String[] username = new String[1];

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/redPlayer/username");
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                username[0] = snapshot.getValue().toString();

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("users/" + username[0].substring(0, username[0].length() - 4) + "/updateScore");

                                myRef.setValue("true");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                        final String[] username2 = new String[1];

                        database = FirebaseDatabase.getInstance();
                        myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/username");
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                username2[0] = snapshot.getValue().toString();

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("users/" + username2[0].substring(0, username2[0].length() - 4) + "/updateScore");

                                myRef.setValue("true");
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.d(TAG, "Error");
                    }
                });

            } else {
                Log.d("Error", "Error");
            }

            /* Updates vị trí cờ nhảy và cộng điểm */

            Iterator it2 = locations.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pair = (Map.Entry) it2.next();

                if (pair.getValue().toString().equals("" + iOfJumpee + jOfJumpee)) {
                    locations.put(pair.getKey().toString(), "" + "00");
                }
            }
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef;

        /* Sets Database Cho vị trí cờ */
        {
            for (int j = 1; j < 4; j += 1) {
                for (int i = 1; i < 9; i += 2) {
                    if (i == 1 && j % 2 == 1) {
                        i += 1;
                    }

                    myRef = database.getReference("games/" + gameID[0] + "/redPlayer/" + i + j);
                    myRef.setValue(locations.get("" + i + j));
                }
            }
            for (int j = 6; j < 9; j += 1) {
                for (int i = 1; i < 9; i += 2) {
                    if (i == 1 && j % 2 == 1) {
                        i += 1;
                    }

                    myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/" + i + j);
                    myRef.setValue(locations.get("" + i + j));
                }
            }

        }
    }

    /**
     * ---- printBoard() ------------------------------------------------------------------------------------------------------------------------------------------------
     */
    public void PrintBoard() {
        for (int j = 8; j > 0; j -= 1) {
            String one, two, three, four, five, six, seven, eight;
            if (board[1][j] == null) {
                one = "[ ] ";
            } else if (board[1][j].getColor().equals("Blue")) {
                one = "[B] ";
            } else {
                one = "[R] ";
            }

            if (board[2][j] == null) {
                two = "[ ] ";
            } else if (board[2][j].getColor().equals("Blue")) {
                two = "[B] ";
            } else {
                two = "[R] ";
            }

            if (board[3][j] == null) {
                three = "[ ] ";
            } else if (board[3][j].getColor().equals("Blue")) {
                three = "[B] ";
            } else {
                three = "[R] ";
            }

            if (board[4][j] == null) {
                four = "[ ] ";
            } else if (board[4][j].getColor().equals("Blue")) {
                four = "[B] ";
            } else {
                four = "[R] ";
            }

            if (board[5][j] == null) {
                five = "[ ] ";
            } else if (board[5][j].getColor().equals("Blue")) {
                five = "[B] ";
            } else {
                five = "[R] ";
            }

            if (board[6][j] == null) {
                six = "[ ] ";
            } else if (board[6][j].getColor().equals("Blue")) {
                six = "[B] ";
            } else {
                six = "[R] ";
            }

            if (board[7][j] == null) {
                seven = "[ ] ";
            } else if (board[7][j].getColor().equals("Blue")) {
                seven = "[B] ";
            } else {
                seven = "[R] ";
            }

            if (board[8][j] == null) {
                eight = "[ ] ";
            } else if (board[8][j].getColor().equals("Blue")) {
                eight = "[B] ";
            } else {
                eight = "[R] ";
            }

            Log.i("", one + two + three + four + five + six + seven + eight);
        }
    }

    /**
     * ---- UpdateScores() ----------------------------------------------------------------------------------------------------------------------------------------------
     */
    public void UpdateScores() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/score");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                blue.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "FAILED TO READ blueScore");
            }
        });

        myRef = database.getReference("games/" + gameID[0] + "/redPlayer/score");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                red.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "FAILED TO READ redScore");
            }
        });
    }

    /**
     * ---- ClearImageViewsOfPotentialMoves() ---------------------------------------------------------------------------------------------------------------------------
     */
    public void ClearImageViewsOfPotentialMoves() {
        for (int j = 1; j < 9; j += 1) {
            for (int i = 1; i < 9; i += 2) {
                if (imageViews[i][j] == null) {
                    i += 1;
                }
                if (board[i][j] == null) {
                    imageViews[i][j].setImageResource(android.R.color.transparent);
                }
            }
        }
    }

    /**
     * ---- MovePieceToLocation() ---------------------------------------------------------------------------------------------------------------------------------------
     */
    public void MovePieceToLocation(int curri, int currj, int nexti, int nextj) {
        Log.d(TAG, "ENTERING MOVE PIECE TO LOCATION");
        boolean doNotRespawn = false;
        String col;
        /*cập nhật lại ảnh cờ*/
        if (board[curri][currj].getColor().equals("Blue")) {
            imageViews[curri][currj].setImageResource(android.R.color.transparent);
            imageViews[curri][currj].setClickable(false);
            if (rotation == 0)
                imageViews[nexti][nextj].setImageResource(R.drawable.blue_piece);
            else imageViews[nexti][nextj].setImageResource(R.drawable.blue_piecer);
            col = "Blue";
            /* Blue-King + 5 */
            if (nextj == 1) {
                //blueScore += 5;
                doNotRespawn = true;
            }

        } else
        {
            imageViews[curri][currj].setImageResource(android.R.color.transparent);
            imageViews[curri][currj].setClickable(false);
            if (rotation == 0)
                imageViews[nexti][nextj].setImageResource(R.drawable.red_piece);
            else imageViews[nexti][nextj].setImageResource(R.drawable.red_piecer);

            col = "Red";

            /* Red-King + 5 */
            if (nextj == 8) {
                //redScore += 5;
                doNotRespawn = true;
            }
        }
        board[curri][currj] = null;
        /* doNotRespawn = true thì cờ không hiển thị */
        if (doNotRespawn == false) {
            Checker checker = new Checker(imageViews[nexti][nextj], nexti, nextj, col);
            board[nexti][nextj] = checker;
        }

        ClearImageViewsOfPotentialMoves();
        chainning = true;
        sendPieceLocationsToDB(curri, currj, nexti, nextj);
        // kiểm tra nextmove và cập nhật
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/nextMove");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (checkIfPlayerHasChain(turn) && chainning && turn.equals(myColor[0]))
                    myRef.setValue(turn);
                else if (!checkIfPlayerHasAvailableMoves("red"))
                    myRef.setValue("blue");
                else if (!checkIfPlayerHasAvailableMoves("blue"))
                    myRef.setValue("red");
                else if (dataSnapshot.getValue().equals("red"))
                    myRef.setValue("blue");
                else if (dataSnapshot.getValue().equals("blue"))
                    myRef.setValue("red");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Log.d(TAG, "EXITING MOVE PIECE TO LOCATION");
    }

    /**
     * ---- DisplayMovingOptions() --------------------------------------------------------------------------------------------------------------------------------------
     */
    public void DisplayMovingOptions(final int i, final int j, final String color) {
        // Tạo chấm vàng hiển thị nơi có thể đi
        Log.d(TAG, "ENTERING DISPLAY MOVING OPTIONS");
        ClearImageViewsOfPotentialMoves();
        final int curri = i;
        final int currj = j;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/nextMove");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                //Xanh
                if (color.equals("Blue") && myColor[0].equals("blue") && value.equals(myColor[0])) {
                    final int nextj = j - 1;
                    if (i - 1 > 0 && j - 1 > 0 && board[i - 1][j - 1] == null) {
                        imageViews[i - 1][j - 1].setImageResource(R.drawable.yellow_p2);
                        final int nexti = i - 1;
                        imageViews[i - 1][j - 1].setClickable(true);
                        imageViews[i - 1][j - 1].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "You clicked the BLUE-LEFT option.");
                                MovePieceToLocation(curri, currj, nexti, nextj);
                                imageViews[curri - 1][currj - 1].setClickable(true);
                                imageViews[curri - 1][currj - 1].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (board[curri - 1][currj - 1] != null) {
                                            Log.d(TAG, "YOU CLICKED ON A BLUE_CHECKER");
                                            DisplayMovingOptions(board[curri - 1][currj - 1].getX(), board[curri - 1][currj - 1].getY(), board[curri - 1][currj - 1].getColor());
                                        }
                                    }
                                });
                            }
                        });
                    }
                    if (i - 2 > 0 && j - 2 > 0 && board[i - 2][j - 2] == null && board[i - 1][j - 1] != null && (board[i - 1][j - 1].getColor().equals("Red"))) {
                        imageViews[i - 2][j - 2].setImageResource(R.drawable.yellow_p2);
                        final int nextiJumping = i - 2;
                        final int nextjJumping = j - 2;
                        imageViews[i - 2][j - 2].setClickable(true);
                        imageViews[i - 2][j - 2].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "You clicked the BLUE-JUMP-LEFT option.");

                                imageViews[nextiJumping + 1][nextjJumping + 1].setImageResource(android.R.color.transparent);
                                imageViews[nextiJumping + 1][nextjJumping + 1].setClickable(false);
                                board[nextiJumping + 1][nextjJumping + 1] = null;

                                MovePieceToLocation(curri, currj, nextiJumping, nextjJumping);
                                imageViews[curri - 2][currj - 2].setClickable(true);
                                imageViews[curri - 2][currj - 2].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (board[curri - 2][currj - 2] != null) {
                                            Log.d(TAG, "YOU CLICKED ON A BLUE_CHECKER");
                                            DisplayMovingOptions(board[curri - 2][currj - 2].getX(), board[curri - 2][currj - 2].getY(), board[curri - 2][currj - 2].getColor());
                                        }
                                    }
                                });
                            }
                        });
                    }
                    if (i + 1 < 9 && j - 1 > 0 && board[i + 1][j - 1] == null) {
                        imageViews[i + 1][j - 1].setImageResource(R.drawable.yellow_p2);
                        final int nexti = i + 1;
                        imageViews[i + 1][j - 1].setClickable(true);
                        imageViews[i + 1][j - 1].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "You clicked the BLUE-RIGHT option.");
                                MovePieceToLocation(curri, currj, nexti, nextj);
                                imageViews[curri + 1][currj - 1].setClickable(true);
                                imageViews[curri + 1][currj - 1].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (board[curri + 1][currj - 1] != null) {
                                            Log.d(TAG, "YOU CLICKED ON A BLUE_CHECKER");
                                            DisplayMovingOptions(board[curri + 1][currj - 1].getX(), board[curri + 1][currj - 1].getY(), board[curri + 1][currj - 1].getColor());
                                        }
                                    }
                                });
                            }
                        });
                    }
                    if (i + 2 < 9 && j - 2 > 0 && board[i + 2][j - 2] == null && board[i + 1][j - 1] != null && (board[i + 1][j - 1].getColor().equals("Red"))) {
                        imageViews[i + 2][j - 2].setImageResource(R.drawable.yellow_p2);
                        final int nextiJumping = i + 2;
                        final int nextjJumping = j - 2;
                        imageViews[i + 2][j - 2].setClickable(true);
                        imageViews[i + 2][j - 2].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "You clicked the BLUE-JUMP-RIGHT option.");

                                imageViews[nextiJumping - 1][nextjJumping + 1].setImageResource(android.R.color.transparent);
                                imageViews[nextiJumping - 1][nextjJumping + 1].setClickable(false);
                                board[nextiJumping - 1][nextjJumping + 1] = null;

                                MovePieceToLocation(curri, currj, nextiJumping, nextjJumping);
                                imageViews[curri + 2][currj - 2].setClickable(true);
                                imageViews[curri + 2][currj - 2].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (board[curri + 2][currj - 2] != null) {
                                            Log.d(TAG, "YOU CLICKED ON A BLUE_CHECKER");
                                            DisplayMovingOptions(board[curri + 2][currj - 2].getX(), board[curri + 2][currj - 2].getY(), board[curri + 2][currj - 2].getColor());
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
                else if (color.equals("Red") && myColor[0].equals("red") && value.equals(myColor[0])) {
                    //Đỏ
                    final int nextj = j + 1;
                    if (i - 1 > 0 && j + 1 < 9 && board[i - 1][j + 1] == null) {
                        imageViews[i - 1][j + 1].setImageResource(R.drawable.yellow_p2);
                        final int nexti = i - 1;
                        imageViews[i - 1][j + 1].setClickable(true);
                        imageViews[i - 1][j + 1].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "You clicked the RED-LEFT option.");
                                MovePieceToLocation(curri, currj, nexti, nextj);
                                imageViews[curri - 1][currj + 1].setClickable(true);
                                imageViews[curri - 1][currj + 1].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (board[curri - 1][currj + 1] != null) {
                                            Log.d(TAG, "YOU CLICKED ON A RED CHECKER");
                                            DisplayMovingOptions(board[curri - 1][currj + 1].getX(), board[curri - 1][currj + 1].getY(), board[curri - 1][currj + 1].getColor());
                                        }
                                    }
                                });
                            }
                        });
                    }
                    if (i - 2 > 0 && j + 2 < 9 && board[i - 2][j + 2] == null && board[i - 1][j + 1] != null && (board[i - 1][j + 1].getColor().equals("Blue"))) {
                        imageViews[i - 2][j + 2].setImageResource(R.drawable.yellow_p2);
                        final int nextiJumping = i - 2;
                        final int nextjJumping = j + 2;
                        imageViews[i - 2][j + 2].setClickable(true);
                        imageViews[i - 2][j + 2].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "You clicked the RED-JUMP-LEFT option.");

                                imageViews[nextiJumping + 1][nextjJumping - 1].setImageResource(android.R.color.transparent);
                                imageViews[nextiJumping + 1][nextjJumping - 1].setClickable(false);
                                board[nextiJumping + 1][nextjJumping - 1] = null;

                                MovePieceToLocation(curri, currj, nextiJumping, nextjJumping);
                                imageViews[curri - 2][currj + 2].setClickable(true);
                                imageViews[curri - 2][currj + 2].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (board[curri - 2][currj + 2] != null) {
                                            Log.d(TAG, "YOU CLICKED ON A BLUE_CHECKER");
                                            DisplayMovingOptions(board[curri - 2][currj + 2].getX(), board[curri - 2][currj + 2].getY(), board[curri - 2][currj + 2].getColor());
                                        }
                                    }
                                });
                            }
                        });
                    }
                    if (i + 1 < 9 && j + 1 < 9 && board[i + 1][j + 1] == null) {
                        imageViews[i + 1][j + 1].setImageResource(R.drawable.yellow_p2);
                        final int nexti = i + 1;
                        imageViews[i + 1][j + 1].setClickable(true);
                        imageViews[i + 1][j + 1].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "You clicked the RED-RIGHT option.");
                                MovePieceToLocation(curri, currj, nexti, nextj);
                                imageViews[curri + 1][currj + 1].setClickable(true);
                                imageViews[curri + 1][currj + 1].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (board[curri + 1][currj + 1] != null) {
                                            Log.d(TAG, "YOU CLICKED ON A RED CHECKER");
                                            DisplayMovingOptions(board[curri + 1][currj + 1].getX(), board[curri + 1][currj + 1].getY(), board[curri + 1][currj + 1].getColor());
                                        }
                                    }
                                });
                            }
                        });
                    }
                    if (i + 2 < 9 && j + 2 < 9 && board[i + 2][j + 2] == null && board[i + 1][j + 1] != null && (board[i + 1][j + 1].getColor().equals("Blue"))) {
                        imageViews[i + 2][j + 2].setImageResource(R.drawable.yellow_p2);
                        final int nextiJumping = i + 2;
                        final int nextjJumping = j + 2;
                        imageViews[i + 2][j + 2].setClickable(true);
                        imageViews[i + 2][j + 2].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "You clicked the RED-JUMP-RIGHT option.");

                                imageViews[nextiJumping - 1][nextjJumping - 1].setImageResource(android.R.color.transparent);
                                imageViews[nextiJumping - 1][nextjJumping - 1].setClickable(false);
                                board[nextiJumping - 1][nextjJumping - 1] = null;

                                MovePieceToLocation(curri, currj, nextiJumping, nextjJumping);
                                imageViews[curri + 2][currj + 2].setClickable(true);
                                imageViews[curri + 2][currj + 2].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (board[curri + 2][currj + 2] != null) {
                                            Log.d(TAG, "YOU CLICKED ON A BLUE_CHECKER");
                                            DisplayMovingOptions(board[curri + 2][currj + 2].getX(), board[curri + 2][currj + 2].getY(), board[curri + 2][currj + 2].getColor());
                                        }
                                    }
                                });
                            }
                        });
                    }
                } else {
                    Log.d("Error", "Error");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * ---- clearUpNonLocations() ---------------------------------------------------------------------------------------------------------------------------------------
     */
    public void clearUpNonLocations() {

        for (int j = 1; j < 9; j += 1) {
            for (int i = 1; i < 9; i += 2) {
                if (imageViews[i][j] == null) {
                    i += 1;
                }

                Iterator test = locations.entrySet().iterator();
                boolean refresh = true;
                while (test.hasNext() && refresh) {
                    Map.Entry pair = (Map.Entry) test.next();

                    int vali = Integer.parseInt(pair.getValue().toString().substring(0, 1));
                    int valj = Integer.parseInt(pair.getValue().toString().substring(1, 2));

                    if (i == vali && j == valj) {
                        refresh = false;
                    }
                }
                if (refresh) {
                    imageViews[i][j].setImageResource(android.R.color.transparent);
                }
            }
        }
    }

     /**
     * ---- checkIfPlayerHasAvailableMoves() ---------------------------------------------------------------------------------------------------------------------------------------
     */
    public boolean checkIfPlayerHasAvailableMoves(String color) {
        Iterator it = locations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            int keyi = Integer.parseInt(pair.getKey().toString().substring(0, 1));
            int keyj = Integer.parseInt(pair.getKey().toString().substring(1, 2));
            int vali = Integer.parseInt(pair.getValue().toString().substring(0, 1));
            int valj = Integer.parseInt(pair.getValue().toString().substring(1, 2));
            if (keyj < 4 && color.equals("red")) // red
            {
                if (vali == 0) { continue; }
                if (vali - 1 > 0 && valj + 1 < 9 && board[vali - 1][valj + 1] == null) { return true; }
                if (vali - 2 > 0 && valj + 2 < 9 && board[vali - 2][valj + 2] == null && board[vali - 1][valj + 1] != null && (board[vali - 1][valj + 1].getColor().equals("Blue"))) { return true; }
                if (vali + 1 < 9 && valj + 1 < 9 && board[vali + 1][valj + 1] == null) { return true; }
                if (vali + 2 < 9 && valj + 2 < 9 && board[vali + 2][valj + 2] == null && board[vali + 1][valj + 1] != null && (board[vali + 1][valj + 1].getColor().equals("Blue"))) { return true; }
            } else if (keyj > 5 && color.equals("blue")) // blue
            {
                if (vali == 0) {
                    continue;
                }

                if (vali - 1 > 0 && valj - 1 > 0 && board[vali - 1][valj - 1] == null) {
                    return true;
                }
                if (vali - 2 > 0 && valj - 2 > 0 && board[vali - 2][valj - 2] == null && board[vali - 1][valj - 1] != null && (board[vali - 1][valj - 1].getColor().equals("Red"))) {
                    return true;
                }
                if (vali + 1 < 9 && valj - 1 > 0 && board[vali + 1][valj - 1] == null) {
                    return true;
                }
                if (vali + 2 < 9 && valj - 2 > 0 && board[vali + 2][valj - 2] == null && board[vali + 1][valj - 1] != null && (board[vali + 1][valj - 1].getColor().equals("Red"))) {
                    return true;
                }
            }
        }

        // check xem thử có đi được thêm bước nào
        return false;
    }

    /**
     * ---- checkIfPlayerHasChain() ---------------------------------------------------------------------------------------------------------------------------------------
     */
    public boolean checkIfPlayerHasChain(String color) {
        Iterator it = locations.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            int keyi = Integer.parseInt(pair.getKey().toString().substring(0, 1));
            int keyj = Integer.parseInt(pair.getKey().toString().substring(1, 2));
            int vali = Integer.parseInt(pair.getValue().toString().substring(0, 1));
            int valj = Integer.parseInt(pair.getValue().toString().substring(1, 2));
            if (keyj < 4 && color.equals("red")) // red
            {
                if (vali == 0) { continue; }
                if (vali - 2 > 0 && valj + 2 < 9 && board[vali - 2][valj + 2] == null && board[vali - 1][valj + 1] != null && (board[vali - 1][valj + 1].getColor().equals("Blue"))) { return true; }
                if (vali + 2 < 9 && valj + 2 < 9 && board[vali + 2][valj + 2] == null && board[vali + 1][valj + 1] != null && (board[vali + 1][valj + 1].getColor().equals("Blue"))) { return true; }
            } else if (keyj > 5 && color.equals("blue")) // blue
            {
                if (vali == 0) {
                    continue;
                }

                if (vali - 2 > 0 && valj - 2 > 0 && board[vali - 2][valj - 2] == null && board[vali - 1][valj - 1] != null && (board[vali - 1][valj - 1].getColor().equals("Red"))) {
                    return true;
                }
                if (vali + 2 < 9 && valj - 2 > 0 && board[vali + 2][valj - 2] == null && board[vali + 1][valj - 1] != null && (board[vali + 1][valj - 1].getColor().equals("Red"))) {
                    return true;
                }
            }
        }

        // check xem thử có đi được thêm bước nào
        return false;
    }

    /**
     * ---- gameOver() ---------------------------------------------------------------------------------------------------------------------------------------
     */
    public void gameOver(String color) {

        if (color.equals("red"))
            Toast.makeText(getApplicationContext(),
                    "Đỏ thắng " + parseInt(red.getText().toString()) + " - " + parseInt(blue.getText().toString()),
                    Toast.LENGTH_LONG).show();
        else if (color.equals("blue"))
            Toast.makeText(getApplicationContext(),
                    "Xanh thắng " + parseInt(blue.getText().toString()) + " - " + parseInt(red.getText().toString()),
                    Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getApplicationContext(),
                    "Huề " + parseInt(blue.getText().toString()) + " - " + parseInt(red.getText().toString()),
                    Toast.LENGTH_LONG).show();

    }

    /**
     * ---- calculateWinner() ---------------------------------------------------------------------------------------------------------------------------------------
     */
    public void calculateWinner() {
        final String[] finalScoreBlue = new String[1];final String[] finalScoreRed = new String[1];final ImageView topText = findViewById(R.id.welcome);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/score");
        Log.d(TAG, "games/" + gameID[0] + "/bluePlayer/score");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                finalScoreBlue[0] = dataSnapshot.getValue(String.class);
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/redPlayer/score");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        finalScoreRed[0] = dataSnapshot.getValue(String.class);
                        if (parseInt(finalScoreBlue[0]) > parseInt(finalScoreRed[0])) {
                            topText.setImageResource(R.drawable.blue_win);
                            gameOverPopup("blue", false, parseInt(blue.getText().toString()), parseInt(red.getText().toString()));
                        }
                        else if (parseInt(finalScoreBlue[0]) < parseInt(finalScoreRed[0])) {
                            topText.setImageResource(R.drawable.red_win);
                            gameOverPopup("red", false, parseInt(blue.getText().toString()), parseInt(red.getText().toString()));
                        }
                        else {
                            topText.setImageResource(R.drawable.tie);
                            gameOverPopup("tie", false, parseInt(blue.getText().toString()), parseInt(red.getText().toString()));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * ---- forfeited(final String colorOfForfeited) ---------------------------------------------------------------------------------------------------------------------------------------
     */
    public void forfeited(final String colorOfForfeited) {
        final String[] finalScoreBlue = new String[1];
        final String[] finalScoreRed = new String[1];

        final ImageView topText = findViewById(R.id.welcome);

        FirebaseDatabase database = FirebaseDatabase.getInstance(); /* <-- FireBase Jumbo */
        DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/bluePlayer/score");

        Log.d(TAG, "games/" + gameID[0] + "/bluePlayer/score");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                finalScoreBlue[0] = dataSnapshot.getValue(String.class);

                FirebaseDatabase database = FirebaseDatabase.getInstance();

                DatabaseReference myRef = database.getReference("games/" + gameID[0] + "/redPlayer/score");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        finalScoreRed[0] = dataSnapshot.getValue(String.class);

                        if (colorOfForfeited.equals("blue")) {

                            topText.setImageResource(R.drawable.red_win);

                            gameOverPopup("red", true, 0, 0);
                        } else if (colorOfForfeited.equals("red")) {

                            topText.setImageResource(R.drawable.blue_win);

                            gameOverPopup("blue", true, 0, 0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * ---- forfeited(final String colorOfForfeited) ---------------------------------------------------------------------------------------------------------------------------------------
     */
    public void gameOverPopup(final String color, boolean forfeited, int blueScore, int redScore) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_endgame, null);

        TextView backgroundColor = popupView.findViewById(R.id.textView6);
        backgroundColor.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        final PopupWindow endgameWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        endgameWindow.setOutsideTouchable(false);

        final TextView endgameText = popupView.findViewById(R.id.endgameText);

        endgameWindow.getContentView().findViewById(R.id.returnHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endgameWindow.dismiss();

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                getApplicationContext().startActivity(intent);
            }
        });


        if (!forfeited) {
            if (color.equals("red")) {
                // popup đỏ thắng

                endgameText.setText("Đỏ thắng " + redScore + " - " + blueScore + "!");
                endgameText.setTextColor(getResources().getColor(R.color.red));

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    endgameWindow.showAtLocation(findViewById(R.id.checkersLandscape), Gravity.CENTER, 0, 0);
                else
                    endgameWindow.showAtLocation(findViewById(R.id.checkersPortrait), Gravity.CENTER, 0, 0);
            } else if (color.equals("blue")) {
                // popup xanh thắng

                endgameText.setText("Xanh thắng " + blueScore + " - " + redScore + "!");
                endgameText.setTextColor(getResources().getColor(R.color.blue));

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    endgameWindow.showAtLocation(findViewById(R.id.checkersLandscape), Gravity.CENTER, 0, 0);
                else
                    endgameWindow.showAtLocation(findViewById(R.id.checkersPortrait), Gravity.CENTER, 0, 0);
            } else {
                // popup huề

                endgameText.setText("Huề " + blueScore + " - " + redScore);
                endgameText.setTextColor(getResources().getColor(R.color.colorPrimaryLight));

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    endgameWindow.showAtLocation(findViewById(R.id.checkersLandscape), Gravity.CENTER, 0, 0);
                else
                    endgameWindow.showAtLocation(findViewById(R.id.checkersPortrait), Gravity.CENTER, 0, 0);
            }

        } else {
            if (color.equals("red")) {
                // Đỏ thắng bởi đầu hàng

                endgameText.setText("Đỏ thắng bởi đầu hàng!");
                endgameText.setTextColor(getResources().getColor(R.color.red));

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    endgameWindow.showAtLocation(findViewById(R.id.checkersLandscape), Gravity.CENTER, 0, 0);
                else
                    endgameWindow.showAtLocation(findViewById(R.id.checkersPortrait), Gravity.CENTER, 0, 0);
            } else {
                // Xanh thắng bởi đầu hàng

                endgameText.setText("Xanh thắng bởi đầu hàng!");
                endgameText.setTextColor(getResources().getColor(R.color.blue));

                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    endgameWindow.showAtLocation(findViewById(R.id.checkersLandscape), Gravity.CENTER, 0, 0);
                else
                    endgameWindow.showAtLocation(findViewById(R.id.checkersPortrait), Gravity.CENTER, 0, 0);
            }
        }
    }
}
