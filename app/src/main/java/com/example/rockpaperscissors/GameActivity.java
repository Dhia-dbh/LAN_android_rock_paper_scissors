package com.example.rockpaperscissors;

import android.animation.ObjectAnimator;
import android.os.Handler;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class GameActivity extends AppCompatActivity {
    private Button btnConfirmButton;
    private RadioGroup radioGroup;
    private String playerChoice = "";
    private boolean isPlayerChoiceConfirmed = false;
    private boolean isOpponentChoiceConfirmed = false;
    private ServerSocket serverSocket = null;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String opponentChoice = "";
    private ProgressBar progressBar;
    private ProgressDialog connectionProgressDialog;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        btnConfirmButton = findViewById(R.id.confirmButton);
        progressBar = findViewById(R.id.progressBar);
        statusTextView = findViewById(R.id.statusTextView);
        radioGroup = findViewById(R.id.radioGroup);
        btnConfirmButton.setEnabled(false);

        connectionProgressDialog = new ProgressDialog(GameActivity.this);
        connectionProgressDialog.setMessage("Waiting for opponent...");
        connectionProgressDialog.setCancelable(false);
        connectionProgressDialog.show();

        if (HostActivity.clientSocket != null) {
            clientSocket = HostActivity.clientSocket;
            serverSocket = HostActivity.serverSocket;
        } else if (JoinActivity.socket != null) {
            clientSocket = JoinActivity.socket;
        }

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Simulate connection establishment lag
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String message = in.readLine();
                        if (message != null) {
                            Log.d("GameActivity", "Received opponent's choice: " + message);
                            synchronized (GameActivity.this) {
                                opponentChoice = message;
                                isOpponentChoiceConfirmed = true;
                                runOnUiThread(() -> {
                                    statusTextView.setText("Opponent has made their choice!");
                                });
                                if (isPlayerChoiceConfirmed && isOpponentChoiceConfirmed) {
                                    Log.d("GameActivity", "Both choices confirmed. Player choice: " + playerChoice + ", Opponent choice: " + opponentChoice);
                                    determineAndDisplayWinner();
                                    cleanUpInterface();
                                    Log.d("GameActivity", "Called cleanUpInterface after determining winner.");
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectionProgressDialog.dismiss();
            }
        }, 3000);
    }

    private void cleanUpInterface() {
        Log.d("GameActivity", "Cleaning up interface method invoked");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("GameActivity", "Cleaning up interface: Hiding confirm button, radio group, and progress bar.");
                btnConfirmButton.setVisibility(View.GONE);
                radioGroup.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public synchronized void onChoiceSelected(View view) {
        // Check which radio button was clicked
        RadioButton radioButton = (RadioButton) view;
        playerChoice = radioButton.getText().toString();
        btnConfirmButton.setEnabled(true);

        // Add animation for visual feedback
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1f, 1.2f, // Start and end values for the X axis scaling
                1f, 1.2f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f // Pivot point of Y scaling
        );
        scaleAnimation.setDuration(300);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setRepeatCount(1);
        radioButton.startAnimation(scaleAnimation);
    }

    public void onConfirmChoice(View view) {
        synchronized (this) {
            Log.d("GameActivity", "Player confirmed choice: " + playerChoice);
            isPlayerChoiceConfirmed = true;
            view.setEnabled(false); // Disable the confirm button

            // Send choice to opponent
            out.println(playerChoice);

            // Show progress bar while waiting for opponent
            progressBar.setVisibility(View.VISIBLE);
            statusTextView.setText("Waiting for opponent's choice...");

            // If both choices are made, determine the winner
            if (isPlayerChoiceConfirmed && isOpponentChoiceConfirmed) {
                Log.d("GameActivity", "Player choice: " + playerChoice);
                Log.d("GameActivity", "Opponent choice: " + opponentChoice);
                determineAndDisplayWinner();
                cleanUpInterface();
            }
        }
    }

    private synchronized void determineAndDisplayWinner() {
        String result = determineWinner(playerChoice, opponentChoice);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusTextView.setText(result);
                alert("Game Result", result);
            }
        });
    }

    private void alert(String title, String msg) {
        new AlertDialog.Builder(GameActivity.this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        findViewById(R.id.goHomeButton).setVisibility(View.VISIBLE);
                    }
                })
                .setCancelable(false)
                .show();
    }

    private synchronized String determineWinner(String player, String opponent) {
        if (player.equals(opponent)) {
            return "It's a Draw!";
        } else if ((player.equals("Rock") && opponent.equals("Scissors")) ||
                (player.equals("Scissors") && opponent.equals("Paper")) ||
                (player.equals("Paper") && opponent.equals("Rock"))) {
            return "You Win!";
        } else {
            return "You Lose!";
        }
    }

    public void onGoHome(View view) {
        try {
            synchronized (this) {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                    Log.d("GameActivity", "Client Socket closed successfully");
                }
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                    Log.d("GameActivity", "Server Socket closed successfully");
                }
            }
        } catch (IOException e) {
            alert("ERROR", "Error closing sockets");
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
