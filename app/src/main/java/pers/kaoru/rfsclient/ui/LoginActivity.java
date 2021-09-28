package pers.kaoru.rfsclient.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.Unbinder;
import pers.kaoru.rfsclient.R;
import pers.kaoru.rfsclient.core.ClientUtils;
import pers.kaoru.rfsclient.core.Response;
import pers.kaoru.rfsclient.core.ResponseCode;

public class LoginActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.hostTextBox)
    EditText hostTextBox;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.portTextBox)
    EditText portTextBox;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.nameTextBox)
    EditText nameTextBox;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.pwdTextBox)
    EditText pwdTextBox;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.loginButton)
    Button loginButton;

    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        unbinder = ButterKnife.bind(this);

        load();
//        setPassword("123");
    }

    private void load() {
        LoginInfo info = null;
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "login.data");
        try {
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            info = (LoginInfo) objectInputStream.readObject();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (info != null) {
            setHost(info.host);
            setPort(info.port);
            setName(info.userName);
        }
    }

    private void save() {
        LoginInfo info = new LoginInfo();
        info.host = getHost();
        info.port = getPort();
        info.userName = getName();

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "login.data");
        try{
            OutputStream outputStream = new FileOutputStream(file, false);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(info);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
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

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.loginButton)
    public void onLogin() {
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
                    Intent intent = new Intent(LoginActivity.this, ViewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("host", host);
                    bundle.putInt("port", port);
                    bundle.putString("token", response.getHeader("token"));
                    intent.putExtras(bundle);
                    startActivity(intent);
                    save();
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, response.getHeader("error"), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
        setAllEnabled(true);
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

class LoginInfo implements Serializable {
    public String host;
    public int port;
    public String userName;
}
