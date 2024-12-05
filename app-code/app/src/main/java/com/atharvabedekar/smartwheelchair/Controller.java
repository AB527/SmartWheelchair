package com.atharvabedekar.smartwheelchair;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class Controller extends AppCompatActivity {

    public static ProgressDialog progressDialog;
    private static BluetoothDevice targetDevice;
    private static BluetoothSocket BTSocket;
    private boolean isBTConnected = false ;
    private ReadInput readThread = null;
    private String requestedCommandToArduino = "1";
    private final UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_CODE_SPEECH_INPUT = 2;
    private Button moveForward, moveBackward, moveLeft, moveRight, btnStop, btnHorn, btnUseAudio, btnUseGesture, btnDisconnect;
    TextView tempAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_controller);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tempAlert = findViewById(R.id.tempAlert);

        moveForward = findViewById(R.id.moveForward);
        moveForward.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    executeCommand(1);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    executeCommand(0);
                }
                return true;
            }
        });
        moveBackward = findViewById(R.id.moveBackward);
        moveBackward.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    executeCommand(2);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    executeCommand(0);
                }
                return true;
            }
        });
        moveLeft = findViewById(R.id.moveLeft);
        moveLeft.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    executeCommand(3);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    executeCommand(0);
                }
                return true;
            }
        });
        moveRight = findViewById(R.id.moveRight);
        moveRight.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    executeCommand(4);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    executeCommand(0);
                }
                return true;
            }
        });
        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeCommand(0);
            }
        });
        btnHorn = findViewById(R.id.btnExit);
        btnHorn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeCommand(10);
            }
        });
        btnUseAudio = findViewById(R.id.btnUseAudio);
        btnUseAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                useAudioInput();
            }
        });
        btnUseGesture = findViewById(R.id.btnUseGesture);
        btnUseGesture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                useGestureInput();
            }
        });
        btnDisconnect = findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DisconnectBT().execute();
            }
        });

        Intent intent = getIntent();
        targetDevice = Objects.requireNonNull(intent.getExtras()).getParcelable("targetDevice");

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        new ConnectBT().execute();
    }

    private void handleResponse(String data) {
        switch(requestedCommandToArduino) {
            case "1":
                break;
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean connectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(Controller.this, "Hold on", "Connecting");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (BTSocket == null || !isBTConnected) {
                    try {
                        BTSocket = targetDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
                        BTSocket.connect();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }

                }
            } catch (IOException e) {
                connectSuccessful = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!connectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device", Toast.LENGTH_LONG).show();
//                finish();
            } else {
                isBTConnected = true;
                readThread = new ReadInput();
            }
            progressDialog.dismiss();
        }

    }

    private class DisconnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {}

        @Override
        protected Void doInBackground(Void... params) {

            if (readThread != null) {
                readThread.stop();
                while (readThread.isRunning());
                readThread = null;
            }

            try {
                BTSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            isBTConnected = false;
            finish();
        }

    }

    private class ReadInput implements Runnable {

        private boolean btStop = false;
        private Thread thread;

        public ReadInput() {
            thread = new Thread(this, "Input Thread");
            thread.start();
        }

        public boolean isRunning() {
            return thread.isAlive();
        }

        @Override
        public void run() {

            InputStream inputStream;

            try {
                inputStream = BTSocket.getInputStream();
                while (!btStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) { }
                        final String strInput = new String(buffer, 0, i);
                        try {
                            handleResponse(strInput);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void stop()
        {
            btStop = true;
        }

    }

    private void destroyThreads() {
        if(readThread != null)
            readThread.stop();
    }

    private void executeCommand(int cmd) {
        tempAlert.setText(String.valueOf(cmd));
//            Toast.makeText(getApplicationContext(), String.valueOf(cmd), Toast.LENGTH_SHORT).show();
        try {
            BTSocket.getOutputStream().write(String.valueOf(cmd).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int decodeTextInput(String msg) {
        msg = msg.toLowerCase();
        String[] msgTokens = msg.split(" ");
        if (msgTokens[0].equals("stop"))
            return 0;
        else if (msgTokens[0].equals("move")) {
            if (msgTokens[1].equals("forward"))
                return 1;
            else if (msgTokens[1].equals("backward"))
                return 2;
            else if (msgTokens[1].equals("left"))
                return 3;
            else if (msgTokens[1].equals("right"))
                return 4;
        }
        return -1;
    }

    private void useAudioInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }
        catch (Exception e) {
            Toast.makeText(Controller.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void useGestureInput() {
        Intent intent = new Intent(getApplicationContext(), GestureControl.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                executeCommand(decodeTextInput(Objects.requireNonNull(result).get(0)));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyThreads();
    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyThreads();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        destroyThreads();
    }
}