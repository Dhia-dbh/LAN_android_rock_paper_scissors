package com.example.rockpaperscissors;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

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
    private String playerChoice = "";
    private boolean isPlayerChoiceConfirmed = false;
    private boolean isOpponentChoiceConfirmed = false;
    private ServerSocket serverSocket = null;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String opponentChoice = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        btnConfirmButton = findViewById(R.id.confirmButton);
        btnConfirmButton.setEnabled(false);

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String message = in.readLine();
                        if (message != null) {
                            synchronized (GameActivity.this) {
                                opponentChoice = message;
                                isOpponentChoiceConfirmed = true;
                                if (isPlayerChoiceConfirmed) {
                                    determineAndDisplayWinner();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public synchronized void onChoiceSelected(View view) {
        // Check which radio button was clicked
        RadioButton radioButton = (RadioButton) view;
        playerChoice = radioButton.getText().toString();
        btnConfirmButton.setEnabled(true);
    }

    public void onConfirmChoice(View view) {
        synchronized (this) {
            isPlayerChoiceConfirmed = true;
            view.setEnabled(false); // Disable the confirm button

            // Send choice to opponent
            out.println(playerChoice);

            // If both choices are made, determine the winner
            if (isPlayerChoiceConfirmed && isOpponentChoiceConfirmed) {
                Log.d("GameActivity", "Player choice: " + playerChoice);
                Log.d("GameActivity", "Opponent choice: " + opponentChoice);
                determineAndDisplayWinner();
            }
        }
    }

    private synchronized void determineAndDisplayWinner() {
        String result = determineWinner(playerChoice, opponentChoice);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
