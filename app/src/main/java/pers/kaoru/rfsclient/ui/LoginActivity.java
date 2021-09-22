package pers.kaoru.rfsclient.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import pers.kaoru.rfsclient.R;

public class LoginActivity extends AppCompatActivity {

    private EditText hostTextBox;
    private EditText portTextBox;
    private EditText nameTextBox;
    private EditText pwdTextBox;
    private Button loginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        hostTextBox = findViewById(R.id.hostTextBox);
        portTextBox = findViewById(R.id.portTextBox);
        nameTextBox = findViewById(R.id.nameTextBox);
        pwdTextBox = findViewById(R.id.pwdTextBox);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(view -> onLogin());
    }

    private void onLogin() {
        setAllEnabled(false);

        new AsyncTask<Void,Void,Boolean>(){
            @Override
            protected Boolean doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                setAllEnabled(true);
            }
        }.execute();
    }

    private String getHost() {
        return hostTextBox.getText().toString();
    }

    private void setHost(String host) {
        hostTextBox.setText(host);
    }

    private int getPort() {
        return Integer.parseInt(portTextBox.getText().toString());
    }

    private void setPort(int port) {
        portTextBox.setText(String.valueOf(port));
    }

    private String getName() {
        return nameTextBox.getText().toString();
    }

    private void setName(String name) {
        nameTextBox.setText(name);
    }

    private String getPassword() {
        return pwdTextBox.getText().toString();
    }

    private void setPassword(String pwd) {
        pwdTextBox.setText(pwd);
    }

    private void setAllEnabled(boolean isEnable) {
        hostTextBox.setEnabled(isEnable);
        portTextBox.setEnabled(isEnable);
        nameTextBox.setEnabled(isEnable);
        pwdTextBox.setEnabled(isEnable);
        loginButton.setEnabled(isEnable);
    }
}
