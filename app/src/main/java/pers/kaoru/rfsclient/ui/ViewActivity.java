package pers.kaoru.rfsclient.ui;

import static android.content.Intent.*;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import butterknife.Unbinder;
import pers.kaoru.rfsclient.R;
import pers.kaoru.rfsclient.core.ClientUtils;
import pers.kaoru.rfsclient.core.FileInfo;
import pers.kaoru.rfsclient.core.Response;
import pers.kaoru.rfsclient.core.ResponseCode;
import pers.kaoru.rfsclient.core.Router;
import pers.kaoru.rfsclient.service.MyService;
import pers.kaoru.rfsclient.service.Task;
import pers.kaoru.rfsclient.service.TaskDispatcher;
import pers.kaoru.rfsclient.service.TaskRecord;
import pers.kaoru.rfsclient.service.TaskType;

public class ViewActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_MOVE = 100;
    public static final int REQUEST_CODE_COPY = 200;

    private String host;
    private int port;
    private String token;

    private final Router router = new Router();
    private volatile boolean isRefresh = false;
    private long firstTime;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.pathText)
    TextView pathText;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.fileList)
    ListView fileList;
    private FileListAdapter fileListAdapter;

    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_layout);
        unbinder = ButterKnife.bind(this);

        Intent intent = getIntent();
        host = intent.getStringExtra("host");
        port = intent.getIntExtra("port", 0);
        token = intent.getStringExtra("token");

        fileListAdapter = new FileListAdapter(this, new LinkedList<>());
        fileList.setAdapter(fileListAdapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            refresh(false, "/");
        });

        refresh(false, "/");

        Intent startIntent = new Intent(this, MyService.class);
        startIntent.setAction(MyService.ACTION_INIT);
        startIntent.putExtra("host", host);
        startIntent.putExtra("port", port);
        startIntent.putExtra("token", token);
        startService(startIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent stopIntent = new Intent(this, MyService.class);
        stopService(stopIntent);
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(ViewActivity.this);
        inflater.inflate(R.menu.view_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newMenu: {
                EditText nameText = new EditText(ViewActivity.this);
                AlertDialog.Builder inputDialog = new AlertDialog.Builder(ViewActivity.this);
                inputDialog.setTitle(R.string.new_dir_string);
                inputDialog.setView(nameText);
                inputDialog.setPositiveButton(R.string.yes_string, (dialogInterface, i1) -> {
                    String newName = nameText.getText().toString();
                    char[] chars = {'\"', '*', '?', '<', '>', '|'};
                    for (char c : chars) {
                        if (newName.indexOf(c) != -1) {
                            Toast.makeText(ViewActivity.this, R.string.illegal_name_string, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    new AsyncTask<Void, Void, Response>() {
                        @Override
                        protected Response doInBackground(Void... voids) {
                            try {
                                return ClientUtils.MakeDirectory(host, port, router + newName, token);
                            } catch (IOException exception) {
                                exception.printStackTrace();
                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(Response response) {
                            if (response == null) {
                                Toast.makeText(ViewActivity.this, R.string.net_error_string, Toast.LENGTH_SHORT);
                                return;
                            }
                            if (response.getCode() == ResponseCode.OK) {
                                refresh(false, "/");
                            } else {
                                Toast.makeText(ViewActivity.this, response.getHeader("error"), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.execute();
                });
                inputDialog.show();
                break;
            }
            case R.id.taskMenu: {
                Intent intent = new Intent(this, TaskActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.aboutMenu: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NonConstantResourceId")
    @OnItemClick(R.id.fileList)
    public void onListItemClick(AdapterView<?> parent, View view, int i, long l) {
        FileInfo info = (FileInfo) fileListAdapter.getItem(i);
        if (info.isDirectory()) {
            onward(info.getName());
        }
    }

    @SuppressLint({"ShowToast", "NonConstantResourceId"})
    @OnItemLongClick(R.id.fileList)
    public boolean onListItemLongClick(AdapterView<?> parent, View view, int i, long l) {
        FileInfo info = (FileInfo) fileListAdapter.getItem(i);
        PopupMenu popupMenu = new PopupMenu(ViewActivity.this, view);
        popupMenu.inflate(R.menu.file_menu);
        popupMenu.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.renameMenu: {
                    EditText nameText = new EditText(ViewActivity.this);
                    nameText.setText(info.getName());
                    AlertDialog.Builder inputDialog = new AlertDialog.Builder(ViewActivity.this);
                    inputDialog.setTitle(R.string.input_new_name_string);
                    inputDialog.setView(nameText);
                    inputDialog.setPositiveButton(R.string.yes_string, (dialogInterface, i1) -> {
                        String newName = nameText.getText().toString();
                        char[] chars = {'\"', '*', '?', '<', '>', '|'};
                        for (char c : chars) {
                            if (newName.indexOf(c) != -1) {
                                Toast.makeText(ViewActivity.this, R.string.illegal_name_string, Toast.LENGTH_SHORT);
                                return;
                            }
                        }

                        new AsyncTask<Void, Void, Response>() {
                            @Override
                            protected Response doInBackground(Void... voids) {
                                try {
                                    return ClientUtils.Move(host, port, router + info.getName(), router + newName, token);
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                    return null;
                                }
                            }

                            @Override
                            protected void onPostExecute(Response response) {
                                if (response == null) {
                                    Toast.makeText(ViewActivity.this, R.string.net_error_string, Toast.LENGTH_SHORT);
                                    return;
                                }
                                if (response.getCode() == ResponseCode.OK) {
                                    refresh(false, "/");
                                } else {
                                    Toast.makeText(ViewActivity.this, response.getHeader("error"), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }.execute();
                    });
                    inputDialog.show();
                    break;
                }
                case R.id.removeMenu: {
                    AlertDialog.Builder confirmDialog = new AlertDialog.Builder(ViewActivity.this);
                    confirmDialog.setTitle(R.string.ask_string);
                    confirmDialog.setMessage(R.string.confirm_remove_string);
                    confirmDialog.setPositiveButton(R.string.yes_string, ((dialogInterface, i1) -> {
                        new AsyncTask<Void, Void, Response>() {
                            @Override
                            protected Response doInBackground(Void... voids) {
                                try {
                                    return ClientUtils.Remove(host, port, router + info.getName(), token);
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                    return null;
                                }
                            }

                            @Override
                            protected void onPostExecute(Response response) {
                                if (response == null) {
                                    Toast.makeText(ViewActivity.this, R.string.net_error_string, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (response.getCode() == ResponseCode.OK) {
                                    refresh(false, "/");
                                } else {
                                    Toast.makeText(ViewActivity.this, response.getHeader("error"), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }.execute();
                    }));
                    confirmDialog.show();
                    break;
                }
                case R.id.moveMenu: {
                    Intent intent = new Intent(ViewActivity.this, SelectActivity.class);
                    intent.putExtra("host", host);
                    intent.putExtra("port", port);
                    intent.putExtra("token", token);
                    intent.putExtra("name", info.getName());
                    intent.putExtra("title", R.string.select_move_location_string);
                    startActivityForResult(intent, REQUEST_CODE_MOVE);
                    break;
                }
                case R.id.copyMenu: {
                    Intent intent = new Intent(ViewActivity.this, SelectActivity.class);
                    intent.putExtra("host", host);
                    intent.putExtra("port", port);
                    intent.putExtra("token", token);
                    intent.putExtra("name", info.getName());
                    intent.putExtra("title", R.string.select_copy_location_string);
                    startActivityForResult(intent, REQUEST_CODE_COPY);
                    break;
                }
                case R.id.downloadMenu:{
                    if(info.isDirectory()){
                       break;
                    }

                    File file = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    TaskRecord record = new TaskRecord(host,port,token,router + info.getName(), file.getAbsolutePath(), TaskType.DOWNLOAD);
                    TaskDispatcher.get().add(new Task(record));
                    break;
                }
                default:
                    Toast.makeText(this, R.string.unknown_error_string, Toast.LENGTH_SHORT);
                    break;
            }
            return false;
        });
        popupMenu.show();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MOVE && resultCode == RESULT_OK) {
            assert data != null;
            String name = data.getStringExtra("name");
            String des = data.getStringExtra("des") + name;
            String src = router + name;

            new AsyncTask<Void, Void, Response>() {
                @Override
                protected Response doInBackground(Void... voids) {
                    try {
                        return ClientUtils.Move(host, port, src, des, token);
                    } catch (IOException exception) {
                        exception.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Response response) {
                    if (response == null) {
                        Toast.makeText(ViewActivity.this, R.string.net_error_string, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (response.getCode() == ResponseCode.OK) {
                        refresh(false, "/");
                    } else {
                        Toast.makeText(ViewActivity.this, response.getHeader("error"), Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        } else if (requestCode == REQUEST_CODE_COPY && resultCode == RESULT_OK) {
            assert data != null;
            String name = data.getStringExtra("name");
            String des = data.getStringExtra("des") + name;
            String src = router + name;

            new AsyncTask<Void, Void, Response>() {
                @Override
                protected Response doInBackground(Void... voids) {
                    try {
                        return ClientUtils.Copy(host, port, src, des, token);
                    } catch (IOException exception) {
                        exception.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Response response) {
                    if (response == null) {
                        Toast.makeText(ViewActivity.this, R.string.net_error_string, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (response.getCode() == ResponseCode.OK) {
                        refresh(false, "/");
                    } else {
                        Toast.makeText(ViewActivity.this, response.getHeader("error"), Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        }
    }

    @Override
    public void onBackPressed() {
        if (router.isEmpty()) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 1500) {
                Toast.makeText(ViewActivity.this, R.string.exit_again_string, Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
                finish();
            }
        }
        back();
    }

    private void refresh(boolean isBack, String subName) {
        if (isRefresh) {
            return;
        }
        isRefresh = true;
        swipeRefreshLayout.setRefreshing(true);

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
                swipeRefreshLayout.setRefreshing(false);
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
