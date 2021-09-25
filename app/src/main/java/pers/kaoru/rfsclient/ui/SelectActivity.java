package pers.kaoru.rfsclient.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.LinkedList;

import pers.kaoru.rfsclient.R;
import pers.kaoru.rfsclient.core.ClientUtils;
import pers.kaoru.rfsclient.core.FileInfo;
import pers.kaoru.rfsclient.core.Response;
import pers.kaoru.rfsclient.core.ResponseCode;
import pers.kaoru.rfsclient.core.Router;

public class SelectActivity extends AppCompatActivity {

    private String host;
    private int port;
    private String token;
    private String name;

    private volatile boolean isRefresh = false;
    private final Router router = new Router();

    private TextView pathText;
    private ListView fileList;
    private FileListAdapter fileListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_layout);


        Intent intent = getIntent();
        host = intent.getStringExtra("host");
        port = intent.getIntExtra("port", 0);
        token = intent.getStringExtra("token");
        name = intent.getStringExtra("name");
        getSupportActionBar().setTitle(intent.getIntExtra("title", R.string.select_location_string));

        pathText = findViewById(R.id.selectPathText);
        fileListAdapter = new FileListAdapter(this, new LinkedList<>());
        fileList = findViewById(R.id.selectFileList);
        fileList.setAdapter(fileListAdapter);
        fileList.setOnItemClickListener(this::onListItemClick);

        refresh(false, "/");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(SelectActivity.this);
        inflater.inflate(R.menu.select_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.selectCancelMenu:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.selectMenu:
                Intent result = new Intent();
                result.putExtra("name", name);
                result.putExtra("des", router.toString());
                setResult(RESULT_OK, result);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onListItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        FileInfo info = (FileInfo) fileListAdapter.getItem(i);
        if (info.isDirectory()) {
            onward(info.getName());
        }
    }

    @Override
    public void onBackPressed() {
        if (router.isEmpty()) {
            setResult(RESULT_CANCELED);
            finish();
        } else {
            back();
        }
    }

    private void refresh(boolean isBack, String subName) {
        if (isRefresh) {
            return;
        }
        isRefresh = true;

        String path;
        if (isBack) {
            path = router.preback();
        } else {
            path = router.toString() + subName;
        }

        new AsyncTask<Void, Void, Response>() {
            @Override
            protected Response doInBackground(Void... voids) {
                try {
                    return ClientUtils.ListShow(host, port, path, token);
                } catch (IOException exception) {
                    exception.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Response response) {
                if (response == null) {
                    isRefresh = false;
                    return;
                }

                if (response.getCode() == ResponseCode.OK) {

                    if (isBack) {
                        router.back();
                    } else {
                        router.enter(subName);
                    }

                    LinkedList<FileInfo> fileInfoList = FileInfo.FileInfoBuild(response.getHeader("list"));
                    fileListAdapter.reset(fileInfoList);
                    pathText.setText(router.toString());
                    fileListAdapter.notifyDataSetChanged();
                }
                isRefresh = false;
            }
        }.execute();
    }

    private void back() {
        if (!router.isEmpty()) {
            refresh(true, null);
        }
    }

    private void onward(String subName) {
        refresh(false, subName);
    }
}