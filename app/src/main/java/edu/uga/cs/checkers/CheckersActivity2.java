package edu.uga.cs.checkers;

import static java.lang.Integer.parseInt;

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

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class CheckersActivity2 extends AppCompatActivity {

    private static final String TAG = "Checkers: ";
    Checker[][] board;
    ImageView[][] imageViews;
    TextView red;
    TextView blue;
    int redScore = 0;
    int blueScore = 0;
    final String[] myColor = new String[1];
    final String[] gameID = new String[1];
    String turn = "red";
    Boolean chainning = true;
    final Map<String, String> locations = new HashMap<>();
    Boolean gameover = false;
    Button forfeit;
    Boolean init = true;

    /**
     * ---- onCreate() --------------------------------------------------------------------------------------------------------------------------------------------------
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkers_main);
        if (init) {
            myColor[0] = "red";
            initBoard();
        }
        forfeit = findViewById(R.id.forfeit_button);

        Log.d(TAG, "CheckersActivity.onCreate()");

        red = findViewById(R.id.score1);
        blue = findViewById(R.id.score2);
        UpdateScores();

        forfeit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameover = true;
                if (gameover == true) {
                    if (turn.equals("true")) {
                        calculateWinner();
                    } else if (turn.equals("red")) {
                        //Đỏ đầu hàng
                        forfeited("red");
                    } else if (turn.equals("blue")) {
                        //Xanh đầu hàng
                        forfeited("blue");
                    }
                }
            }
        });

        TextView currentTurn = findViewById(R.id.currentTurn);

        if (turn.equals("red")) {
            currentTurn.setTextColor(getResources().getColor(R.color.red));
            currentTurn.setText("Lượt của đỏ!");
            turn = "red";
            myColor[0] = "red";
            LinearLayout gameLayout=findViewById(R.id.gamelayout);
            if(myColor[0].equals("red"))
                gameLayout.setRotation(0);
        } else if (turn.equals("blue")) {
            currentTurn.setTextColor(getResources().getColor(R.color.blue));
            currentTurn.setText("Lượt của xanh!");
            turn = "blue";
            myColor[0] = "blue";
            LinearLayout gameLayout=findViewById(R.id.gamelayout);
            if(myColor[0].equals("blue"))
                gameLayout.setRotation(180);
        }

        updateBoard();
    }

    private void initBoard() {
        LinearLayout gameLayout=findViewById(R.id.gamelayout);
        if(myColor[0].equals("blue"))
        gameLayout.setRotation(180);
        for (int j = 1; j < 9; j += 1) {
            for (int i = 1; i < 9; i += 2) {
                if (i == 1 && j % 2 == 1) {
                    i += 1;
                }
                final int ii = i;
                final int jj = j;
                if (j > 5) {
                    updateLocationsMap(Integer.toString(ii) + Integer.toString(jj), Integer.toString(ii) + Integer.toString(jj));
                } else if (j < 4) {
                    updateLocationsMap(Integer.toString(ii) + Integer.toString(jj), Integer.toString(ii) + Integer.toString(jj));
                }
            }
        }
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
        imageViews[8][7] = findViewById(R.id.circle78);
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
        imageViews[8][1] = findViewById(R.id.circle18);
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

        Iterator it = locations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String tmp = pair.getValue().toString();
            final int i = Integer.parseInt(tmp.substring(0, 1));
            final int j = Integer.parseInt(tmp.substring(1, 2));

            if (j > 5) { // Xanh

                String value = pair.getValue().toString();
                updateLocationsMap("" + i + j, value);

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
                checker.getImageView().setImageResource(R.drawable.blue_piece);
                board[vali][valj] = checker;
                clearUpNonLocations();

            } else if (j < 4) { // Đỏ

                String value = pair.getValue().toString();
                updateLocationsMap("" + i + j, value);
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
                checker.getImageView().setImageResource(R.drawable.red_piece);
                board[vali][valj] = checker;
                clearUpNonLocations();

            }
        }

        /* Chạy qua bàng cờ */

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
        Log.d(TAG, "sendPieceLocationsToDB: Enter");
        /* Đi tới ô king */
        boolean touchdown = false;
        if (newj == 1 || newj == 8) {
            touchdown = true;
        }
        /* Nhảy */
        boolean jumped = false;
        if (Math.abs(newi - previ) == 2 || Math.abs(previ - newi) == 2) {
            jumped = true;
        }
        if (!jumped || touchdown) chainning = false;
        if (touchdown && !jumped) {
            Log.d(TAG, "sendPieceLocationsToDB: Enter 1");
            Iterator it = locations.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getValue().toString().equals("" + previ + prevj)) {
                    locations.put(pair.getKey().toString(), "00");
                }
            }
            if (myColor[0].equals("blue")) {
                /*Tăng điểm xanh +5 */
                blueScore = blueScore + 5;
                Log.d(TAG, "sendPieceLocationsToDB: " + blueScore);
                UpdateScores();
                /* Kiểm tra game kết thúc */
                if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                    gameover = true;
                    turn = "true";
                    if (gameover == true) {
                        if (turn.equals("true")) {
                            calculateWinner();
                        } else if (turn.equals("red")) {
                            //Đỏ đầu hàng
                            forfeited("red");
                        } else if (turn.equals("blue")) {
                            //Xanh đầu hàng
                            forfeited("blue");
                        }
                    }
                }
            } else if (myColor[0].equals("red")) {
                /*Tăng điểm đỏ +5 */
                redScore = redScore + 5;
                Log.d(TAG, "sendPieceLocationsToDB: " + redScore);
                UpdateScores();
                /* Kiểm tra game kết thúc */
                if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                    gameover = true;
                    turn = "true";
                    if (gameover == true) {
                        if (turn.equals("true")) {
                            calculateWinner();
                        } else if (turn.equals("red")) {
                            //Đỏ đầu hàng
                            forfeited("red");
                        } else if (turn.equals("blue")) {
                            //Xanh đầu hàng
                            forfeited("blue");
                        }
                    }
                }
            }
        }

        if (jumped && !touchdown) {
            Log.d(TAG, "sendPieceLocationsToDB: Enter 2");
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
                blueScore += 1;
                UpdateScores();

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
                redScore += 1;
                UpdateScores();
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
        if (touchdown && jumped) {
            Log.d(TAG, "sendPieceLocationsToDB: Enter 3");
            Iterator it = locations.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getValue().toString().equals("" + previ + prevj)) {
                    locations.put(pair.getKey().toString(), "00");
                }
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
                Log.d(TAG, "sendPieceLocationsToDB: " + blueScore);
                blueScore += 6;
                Log.d(TAG, "sendPieceLocationsToDB: " + blueScore);
                UpdateScores();
                /* Kiểm tra game kết thúc */
                if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                    gameover = true;
                    turn = "true";
                    if (gameover == true) {
                        if (turn.equals("true")) {
                            calculateWinner();
                        } else if (turn.equals("red")) {
                            //Đỏ đầu hàng
                            forfeited("red");
                        } else if (turn.equals("blue")) {
                            //Xanh đầu hàng
                            forfeited("blue");
                        }
                    }
                }
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
                Log.d(TAG, "sendPieceLocationsToDB: " + redScore);
                redScore += 6;
                Log.d(TAG, "sendPieceLocationsToDB: " + redScore);
                UpdateScores();
                /* Kiểm tra game kết thúc */
                if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                    gameover = true;
                    turn = "true";
                    if (gameover == true) {
                        if (turn.equals("true")) {
                            calculateWinner();
                        } else if (turn.equals("red")) {
                            //Đỏ đầu hàng
                            forfeited("red");
                        } else if (turn.equals("blue")) {
                            //Xanh đầu hàng
                            forfeited("blue");
                        }
                    }
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
            /* Kiểm tra game kết thúc */
            if (!checkIfPlayerHasAvailableMoves("red") && !checkIfPlayerHasAvailableMoves("blue")) {
                gameover = true;
                turn = "true";
                if (gameover == true) {
                    if (turn.equals("true")) {
                        calculateWinner();
                    } else if (turn.equals("red")) {
                        //Đỏ đầu hàng
                        forfeited("red");
                    } else if (turn.equals("blue")) {
                        //Xanh đầu hàng
                        forfeited("blue");
                    }
                }
            }

        }

        Log.d(TAG, "sendPieceLocationsToDB: Exit");
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
        TextView blueScored = findViewById(R.id.score2);
        blueScored.setText(Integer.toString(blueScore));
        TextView redScored = findViewById(R.id.score1);
        redScored.setText(Integer.toString(redScore));
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
        if (board[curri][currj] != null) {

            boolean doNotRespawn = false;
            String col;
            if (board[curri][currj].getColor().equals("Blue")) {
                imageViews[curri][currj].setImageResource(android.R.color.transparent);
                imageViews[curri][currj].setClickable(false);
                imageViews[nexti][nextj].setImageResource(R.drawable.blue_piece);
                col = "Blue";

                /* Blue-King + 5 */
                if (nextj == 1) {
                    //blueScore += 5;
                    // UpdateScores();
                    doNotRespawn = true;
                }

            } else {
                imageViews[curri][currj].setImageResource(android.R.color.transparent);
                imageViews[curri][currj].setClickable(false);
                imageViews[nexti][nextj].setImageResource(R.drawable.red_piece);
                col = "Red";

                /* Red-King + 5 */
                if (nextj == 8) {
                    // redScore += 5;
                    // UpdateScores();
                    doNotRespawn = true;
                }
            }

            board[curri][currj] = null;

            if (doNotRespawn == false) {
                Checker checker = new Checker(imageViews[nexti][nextj], nexti, nextj, col);
                board[nexti][nextj] = checker;
            }

            ClearImageViewsOfPotentialMoves();
            chainning = true;
            sendPieceLocationsToDB(curri, currj, nexti, nextj);
            if (checkIfPlayerHasChain(turn) && chainning && turn.equals(myColor[0])) {
                turn = turn;
                myColor[0] = turn;
                TextView currentTurn = findViewById(R.id.currentTurn);
                if (turn.equals("red")) {
                    currentTurn.setTextColor(getResources().getColor(R.color.red));
                    currentTurn.setText("Lượt của đỏ!");
                    turn = "red";
                    myColor[0] = "red";
                    LinearLayout gameLayout=findViewById(R.id.gamelayout);
                    if(myColor[0].equals("red"))
                        gameLayout.setRotation(0);
                } else if (turn.equals("blue")) {
                    currentTurn.setTextColor(getResources().getColor(R.color.blue));
                    currentTurn.setText("Lượt của xanh!");
                    turn = "blue";
                    myColor[0] = "blue";
                    LinearLayout gameLayout=findViewById(R.id.gamelayout);
                    if(myColor[0].equals("blue"))
                        gameLayout.setRotation(180);
                }
            } else if (!checkIfPlayerHasAvailableMoves("red")) {
                turn = "blue";
                myColor[0] = "blue";
                LinearLayout gameLayout=findViewById(R.id.gamelayout);
                if(myColor[0].equals("blue"))
                    gameLayout.setRotation(180);
                TextView currentTurn = findViewById(R.id.currentTurn);
                if (turn.equals("red")) {
                    currentTurn.setTextColor(getResources().getColor(R.color.red));
                    currentTurn.setText("Lượt của đỏ!");
                    turn = "red";
                    myColor[0] = "red";
                     if(myColor[0].equals("red"))
                        gameLayout.setRotation(0);
                } else if (turn.equals("blue")) {
                    currentTurn.setTextColor(getResources().getColor(R.color.blue));
                    currentTurn.setText("Lượt của xanh!");
                    turn = "blue";
                    myColor[0] = "blue";
                     if(myColor[0].equals("blue"))
                        gameLayout.setRotation(180);
                }
            } else if (!checkIfPlayerHasAvailableMoves("blue")) {
                turn = "red";
                myColor[0] = "red";
                LinearLayout gameLayout=findViewById(R.id.gamelayout);
                if(myColor[0].equals("red"))
                    gameLayout.setRotation(0);
                TextView currentTurn = findViewById(R.id.currentTurn);
                if (turn.equals("red")) {
                    currentTurn.setTextColor(getResources().getColor(R.color.red));
                    currentTurn.setText("Lượt của đỏ!");
                    turn = "red";
                    myColor[0] = "red";
                     if(myColor[0].equals("red"))
                        gameLayout.setRotation(0);
                } else if (turn.equals("blue")) {
                    currentTurn.setTextColor(getResources().getColor(R.color.blue));
                    currentTurn.setText("Lượt của xanh!");
                    turn = "blue";
                    myColor[0] = "blue";
                     if(myColor[0].equals("blue"))
                        gameLayout.setRotation(180);
                }
            } else if (turn.equals("red")) {
                turn = "blue";
                myColor[0] = "blue";
                LinearLayout gameLayout=findViewById(R.id.gamelayout);
                if(myColor[0].equals("blue"))
                    gameLayout.setRotation(180);
                TextView currentTurn = findViewById(R.id.currentTurn);
                if (turn.equals("red")) {
                    currentTurn.setTextColor(getResources().getColor(R.color.red));
                    currentTurn.setText("Lượt của đỏ!");
                    turn = "red";
                    myColor[0] = "red";
                     if(myColor[0].equals("red"))
                        gameLayout.setRotation(0);
                } else if (turn.equals("blue")) {
                    currentTurn.setTextColor(getResources().getColor(R.color.blue));
                    currentTurn.setText("Lượt của xanh!");
                    turn = "blue";
                    myColor[0] = "blue";
                     if(myColor[0].equals("blue"))
                        gameLayout.setRotation(180);
                }
            } else if (turn.equals("blue")) {
                turn = "red";
                myColor[0] = "red";
                LinearLayout gameLayout=findViewById(R.id.gamelayout);
                if(myColor[0].equals("red"))
                    gameLayout.setRotation(0);
                TextView currentTurn = findViewById(R.id.currentTurn);
                if (turn.equals("red")) {
                    currentTurn.setTextColor(getResources().getColor(R.color.red));
                    currentTurn.setText("Lượt của đỏ!");
                    turn = "red";
                    myColor[0] = "red";
                     if(myColor[0].equals("red"))
                        gameLayout.setRotation(0);
                } else if (turn.equals("blue")) {
                    currentTurn.setTextColor(getResources().getColor(R.color.blue));
                    currentTurn.setText("Lượt của xanh!");
                    turn = "blue";
                    myColor[0] = "blue";
                    if(myColor[0].equals("blue"))
                        gameLayout.setRotation(180);
                }
            }
        }
        Log.d(TAG, "EXITING MOVE PIECE TO LOCATION");
    }

    /**
     * ---- DisplayMovingOptions() --------------------------------------------------------------------------------------------------------------------------------------
     */
    public void DisplayMovingOptions(final int i, final int j, final String color) {
        // Tạo chấm vàng hiển thị nơi có thể đi

        Log.d(TAG, "ENTERING DISPLAY MOVING OPTIONS " + blueScore + " " + redScore + " " + color + " " + turn);

        ClearImageViewsOfPotentialMoves();
        final int curri = i;
        final int currj = j;

        String value = turn;
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
        } else if (color.equals("Red") && myColor[0].equals("red") && value.equals(myColor[0])) {
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
                if (vali == 0) {
                    continue;
                }

                if (vali - 1 > 0 && valj + 1 < 9 && board[vali - 1][valj + 1] == null) {
                    return true;
                }
                if (vali - 2 > 0 && valj + 2 < 9 && board[vali - 2][valj + 2] == null && board[vali - 1][valj + 1] != null && (board[vali - 1][valj + 1].getColor().equals("Blue"))) {
                    return true;
                }
                if (vali + 1 < 9 && valj + 1 < 9 && board[vali + 1][valj + 1] == null) {
                    return true;
                }
                if (vali + 2 < 9 && valj + 2 < 9 && board[vali + 2][valj + 2] == null && board[vali + 1][valj + 1] != null && (board[vali + 1][valj + 1].getColor().equals("Blue"))) {
                    return true;
                }
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
     * ---- checkIfPlayerHasAvailableMoves() ---------------------------------------------------------------------------------------------------------------------------------------
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
                if (vali == 0) {
                    continue;
                }

                if (vali - 2 > 0 && valj + 2 < 9 && board[vali - 2][valj + 2] == null && board[vali - 1][valj + 1] != null && (board[vali - 1][valj + 1].getColor().equals("Blue"))) {
                    return true;
                }

                if (vali + 2 < 9 && valj + 2 < 9 && board[vali + 2][valj + 2] == null && board[vali + 1][valj + 1] != null && (board[vali + 1][valj + 1].getColor().equals("Blue"))) {
                    return true;
                }
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
        final ImageView topText = findViewById(R.id.welcome);
        if (blueScore > redScore) {
            topText.setImageResource(R.drawable.blue_win);
            gameOverPopup("blue", false, blueScore, redScore);
        } else if (blueScore < redScore) {
            topText.setImageResource(R.drawable.red_win);
            gameOverPopup("red", false, blueScore, redScore);
        } else {
            topText.setImageResource(R.drawable.tie);
            gameOverPopup("tie", false, blueScore, redScore);
        }
    }

    /**
     * ---- forfeited(final String colorOfForfeited) ---------------------------------------------------------------------------------------------------------------------------------------
     */
    public void forfeited(final String colorOfForfeited) {
        final ImageView topText = findViewById(R.id.welcome);
        if (colorOfForfeited.equals("blue")) {
            topText.setImageResource(R.drawable.red_win);
            gameOverPopup("red", true, blueScore, redScore);
        } else if (colorOfForfeited.equals("red")) {
            topText.setImageResource(R.drawable.blue_win);
            gameOverPopup("blue", true, blueScore, redScore);
        }
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

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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
