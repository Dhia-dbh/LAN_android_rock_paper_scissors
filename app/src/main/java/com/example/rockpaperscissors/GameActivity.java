package com.example.rockpaperscissors;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class GameActivity extends AppCompatActivity {
    private String playerChoice = "";
    private boolean isChoiceConfirmed = false;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String opponentChoice = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (HostActivity.clientSocket != null) {
            socket = HostActivity.clientSocket;
        } else if (JoinActivity.socket != null) {
            socket = JoinActivity.socket;
        }

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
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
                            opponentChoice = message;
                            if (!playerChoice.isEmpty() && !opponentChoice.isEmpty()) {
                                determineAndDisplayWinner();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void onChoiceSelected(View view) {
        // Check which radio button was clicked
        RadioButton radioButton = (RadioButton) view;
        playerChoice = radioButton.getText().toString();
    }

    public void onConfirmChoice(View view) {
        if (!playerChoice.isEmpty()) {
            isChoiceConfirmed = true;
            view.setEnabled(false); // Disable the confirm button

            // Send choice to opponent
            out.println(playerChoice);

            // If both choices are made, determine the winner
            if (!opponentChoice.isEmpty()) {
                determineAndDisplayWinner();
            }
        } else {
            Toast.makeText(this, "Please select an option.", Toast.LENGTH_SHORT).show();
        }
    }

    private void determineAndDisplayWinner() {
        String result = determineWinner(playerChoice, opponentChoice);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GameActivity.this, result, Toast.LENGTH_LONG).show();
                findViewById(R.id.goHomeButton).setVisibility(View.VISIBLE);
            }
        });
    }

    private String determineWinner(String player, String opponent) {
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
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}