package pers.kaoru.rfsclient.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collection;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemLongClick;
import butterknife.Unbinder;
import pers.kaoru.rfsclient.R;
import pers.kaoru.rfsclient.service.MyService;
import pers.kaoru.rfsclient.service.Task;
import pers.kaoru.rfsclient.service.TaskDispatcher;
import pers.kaoru.rfsclient.service.TaskRecord;
import pers.kaoru.rfsclient.service.TaskState;

public class TaskActivity extends AppCompatActivity {

    private Unbinder unbinder;

    private BroadcastReceiver broadcastReceiver;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.taskSwipe)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.noTask)
    TextView noTaskText;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.taskList)
    ListView taskList;
    private TaskListAdapter taskListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_layout);
        unbinder = ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.task_view_string);


        initBroadcastReceiver();

        swipeRefreshLayout.setOnRefreshListener(this::refresh);
        taskListAdapter = new TaskListAdapter(TaskActivity.this, new LinkedList<>());
        refresh();
        taskList.setAdapter(taskListAdapter);
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(MyService.ACTION_FINISH)) {
                    String taskId = intent.getStringExtra("id");
                    TaskItem item = taskListAdapter.getTaskView(taskId);
                    item.getRecord().setSpeed(0);
                    item.setState(TaskState.FINISH);
                    TaskDispatcher.get().remove(taskId);
                    Toast.makeText(getApplicationContext(), item.getRecord().getName() + " " + getResources().getString(R.string.done_string), Toast.LENGTH_LONG).show();
                } else if (intent.getAction().equals(MyService.ACTION_PAUSE)) {
                    String taskId = intent.getStringExtra("id");
                    TaskItem item = taskListAdapter.getTaskView(taskId);
                    item.getRecord().setSpeed(0);
                    item.setState(TaskState.PAUSED);
                    TaskDispatcher.get().remove(taskId);
                    Toast.makeText(getApplicationContext(), item.getRecord().getName() + " " + getResources().getString(R.string.failed_string), Toast.LENGTH_LONG).show();
                } else if (intent.getAction().equals(MyService.ACTION_RESUME)) {
                    String taskId = intent.getStringExtra("id");
                    TaskItem item = taskListAdapter.getTaskView(taskId);
                    item.setState(TaskState.RUNNING);
                } else if (intent.getAction().equals(MyService.ACTION_PAUSE_ALL)) {
                    for (TaskItem item : taskListAdapter.getTaskItemHashMap().values()) {
                        if (item.getTaskState().getText().equals(TaskState.RUNNING.name())) {
                            item.setState(TaskState.PAUSED);
                            item.getRecord().setSpeed(0);
                        }
                    }
                } else if (intent.getAction().equals(MyService.ACTION_RESUME_ALL)) {
                    for (TaskItem item : taskListAdapter.getTaskItemHashMap().values()) {
                        if (item.getTaskState().getText().equals(TaskState.PAUSED.name())) {
                            item.setState(TaskState.RUNNING);
                        }
                    }
                } else if (intent.getAction().equals(MyService.ACTION_CANCEL_ALL)) {
                    for (TaskItem item : taskListAdapter.getTaskItemHashMap().values()) {
                        String stateStr = item.getTaskState().getText().toString();
                        if (stateStr.equals(TaskState.PAUSED.name()) || stateStr.equals(TaskState.RUNNING.name())) {
                            item.setState(TaskState.CANCELED);
                            item.getRecord().setSpeed(0);

                        }
                    }
                }
                taskListAdapter.notifyDataSetChanged();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyService.ACTION_PAUSE);
        intentFilter.addAction(MyService.ACTION_PAUSE_ALL);
        intentFilter.addAction(MyService.ACTION_RESUME);
        intentFilter.addAction(MyService.ACTION_RESUME_ALL);
        intentFilter.addAction(MyService.ACTION_CANCEL);
        intentFilter.addAction(MyService.ACTION_CANCEL_ALL);
        intentFilter.addAction(MyService.ACTION_UPDATE);
        intentFilter.addAction(MyService.ACTION_FAIL);
        intentFilter.addAction(MyService.ACTION_FINISH);

        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void refresh() {
        this.swipeRefreshLayout.setRefreshing(true);
        Collection<Task> tasks = TaskDispatcher.get().getTasks();

        LinkedList<TaskRecord> taskRecords = new LinkedList<>();

        if (tasks.isEmpty()) {
            noTaskText.setVisibility(View.VISIBLE);
        } else {
            for (Task task : tasks) {
                if (task.getState() == TaskState.FAILED || task.getState() == TaskState.CANCELED || task.getState() == TaskState.FINISH) {
                    TaskDispatcher.get().remove(task.getRecord().getUid());
                    continue;
                }
                taskRecords.push(task.getRecord());
            }

            Animation animation = AnimationUtils.loadAnimation(TaskActivity.this, R.anim.item_slide_down);
            LayoutAnimationController controller = new LayoutAnimationController(animation);
            taskList.setLayoutAnimation(controller);

            taskListAdapter.reset(taskRecords);
            noTaskText.setVisibility(View.INVISIBLE);
        }
        this.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NonConstantResourceId")
    @OnItemLongClick(R.id.taskList)
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        TaskRecord record = (TaskRecord) taskListAdapter.getItem(i);
        PopupMenu popupMenu = new PopupMenu(TaskActivity.this, view);
        popupMenu.inflate(R.menu.task_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.taskPause: {
                    Intent intent = new Intent(this, MyService.class);
                    intent.setAction(MyService.ACTION_PAUSE);
                    intent.putExtra("id", record.getUid());
                    startService(intent);
                    break;
                }
                case R.id.pauseAllTask: {
                    Intent intent = new Intent(this, MyService.class);
                    intent.setAction(MyService.ACTION_PAUSE_ALL);
                    startService(intent);
                    break;
                }
                case R.id.resumeTask: {
                    Intent intent = new Intent(this, MyService.class);
                    intent.setAction(MyService.ACTION_RESUME);
                    intent.putExtra("id", record.getUid());
                    startService(intent);
                    break;
                }
                case R.id.resumeAllTask: {
                    Intent intent = new Intent(this, MyService.class);
                    intent.setAction(MyService.ACTION_RESUME_ALL);
                    startService(intent);
                    break;
                }
                case R.id.cancelTask: {
                    Intent intent = new Intent(this, MyService.class);
                    intent.setAction(MyService.ACTION_CANCEL);
                    intent.putExtra("id", record.getUid());
                    startService(intent);
                    break;
                }
                case R.id.cancelAllTask: {
                    Intent intent = new Intent(this, MyService.class);
                    intent.setAction(MyService.ACTION_CANCEL_ALL);
                    startService(intent);
                    break;
                }
            }
            return false;
        });
        popupMenu.show();
        return false;
    }
}
