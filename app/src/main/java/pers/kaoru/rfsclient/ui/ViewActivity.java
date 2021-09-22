package pers.kaoru.rfsclient.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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

public class ViewActivity extends AppCompatActivity {

    private String host;
    private int port;
    private String token;

    private final Router router = new Router();
    private volatile boolean isRefresh = false;
    private long firstTime;

    private ListView fileList;
    private FileListAdapter fileListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_layout);

        Intent intent = getIntent();
        host = intent.getStringExtra("host");
        port = intent.getIntExtra("port", 0);
        token = intent.getStringExtra("token");

        fileListAdapter = new FileListAdapter(this, new LinkedList<>());
        fileList = findViewById(R.id.fileList);
        fileList.setAdapter(fileListAdapter);
        fileList.setOnItemClickListener(this::onListItemClick);

        refresh(false, "/");
    }

    public void onListItemClick(AdapterView<?> parent, View view, long i, long l) {
        FileInfo info = (FileInfo) fileListAdapter.getItem((int) i);
        if (info.isDirectory()) {
            onward(info.getName());
        }
    }

    @Override
    public void onBackPressed() {
        if (router.isEmpty()) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 1500) {
                Toast.makeText(ViewActivity.this, R.string.exit_again, Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
                finish();
            }
        }
        back();
    }

    public void refresh(boolean isBack, String subName) {
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
                    fileListAdapter.notifyDataSetChanged();
                }
                isRefresh = false;
            }
        }.execute();
    }

    public void back() {
        if (!router.isEmpty()) {
            refresh(true, null);
        }
    }

    public void onward(String subName) {
        refresh(false, subName);
    }
}
