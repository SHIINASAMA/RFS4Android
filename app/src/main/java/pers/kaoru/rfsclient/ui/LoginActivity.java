package pers.kaoru.rfsclient.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import pers.kaoru.rfsclient.R;
import pers.kaoru.rfsclient.core.ClientUtils;
import pers.kaoru.rfsclient.core.Response;
import pers.kaoru.rfsclient.core.ResponseCode;

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

        // 测试用
        setHost("192.168.3.2");
        setPort(8080);
        setName("root");
        setPassword("123");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(LoginActivity.this);
        inflater.inflate(R.menu.login_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
        return true;
    }

    private void onLogin() {
        String host = getHost();
        int port = getPort();
        String name = getName();
        String pwdStr = getPassword();

        if (host.isEmpty()
                || port < 1025
                || port > 65535
                || name.isEmpty()
                || pwdStr.isEmpty()) {
            return;
        }

        setAllEnabled(false);

        new AsyncTask<Void, Void, Response>() {
            @Override
            protected Response doInBackground(Void... voids) {
                Response response = null;
                try {
                    response = ClientUtils.Verify(host, port, name, pwdStr);
                } catch (IOException exception) {
                    return null;
                }
                return response;
            }

            @Override
            protected void onPostExecute(Response response) {
                if (response == null) {
                    setAllEnabled(true);
                    Toast.makeText(LoginActivity.this, R.string.net_error_string, Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.getCode() == ResponseCode.OK) {
                    setAllEnabled(true);
                    Intent intent = new Intent(LoginActivity.this, ViewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("host", host);
                    bundle.putInt("port", port);
                    bundle.putString("token", response.getHeader("token"));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(LoginActivity.this, response.getHeader("error"), Toast.LENGTH_LONG).show();
                }
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
        String portStr = portTextBox.getText().toString();
        if (portStr.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(portStr);
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
