package pers.kaoru.rfsclient.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;

import pers.kaoru.rfsclient.R;

public class MyService extends Service {

    public static final String ACTION_INIT = "ACTION_INIT";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PAUSE_ALL = "ACTION_PAUSE_ALL";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_RESUME_ALL = "ACTION_RESUME_ALL";
    public static final String ACTION_CANCEL = "ACTION_CANCEL";
    public static final String ACTION_CANCEL_ALL = "ACTION_CANCEL_ALL";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FAIL = "ACTION_FAIL";
    public static final String ACTION_FINISH = "ACTION_FINISH";

    private static final String NOTIFICATION_ID = "RFS4Android";
    private static final String NOTIFICATION_NAME = "MyService";
    private NotificationManager notifyManager;

    private final TaskListener listener = new TaskListener() {
        @Override
        public void onProgress(TaskRecord record, long speed) {
            record.setSpeed(speed);
            Intent intent = new Intent();
            intent.setAction(ACTION_UPDATE);
            intent.putExtra("id", record.getUid());
            sendBroadcast(intent);
        }

        @Override
        public void onFailed(TaskRecord record, String error) {
            Intent intent = new Intent();
            intent.setAction(ACTION_FAIL);
            intent.putExtra("id", record.getUid());
            intent.putExtra("error", error);
            sendBroadcast(intent);
            Notification notification = new Notification( );
            notification = new Notification.Builder(MyService.this, NOTIFICATION_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.task_string))
                    .setContentText( record.getName() + " " +getString(R.string.failed_string))
                    .build();
            notifyManager.notify(0, notification);
        }

        @Override
        public void onPaused(TaskRecord record) {
            Intent intent = new Intent();
            intent.setAction(ACTION_PAUSE);
            intent.putExtra("id", record.getUid());
            sendBroadcast(intent);
        }

        @Override
        public void onResume(TaskRecord record) {
            Intent intent = new Intent();
            intent.setAction(ACTION_RESUME);
            intent.putExtra("id", record.getUid());
            sendBroadcast(intent);
        }

        @Override
        public void onFinish(TaskRecord record) {
            Intent intent = new Intent();
            intent.setAction(ACTION_FINISH);
            intent.putExtra("id", record.getUid());
            sendBroadcast(intent);
            Notification notification = new Notification.Builder(MyService.this, NOTIFICATION_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.task_string))
                    .setContentText( record.getName() + " " +getString(R.string.finished_string))
                    .build();
            notifyManager.notify(0, notification);
        }

        @Override
        public void onCanceled(TaskRecord record) {
            Intent intent = new Intent();
            intent.setAction(ACTION_CANCEL);
            intent.putExtra("id", record.getUid());
            sendBroadcast(intent);
        }
    };

    private final LinkedList<TaskRecord> otherTasks = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_ID, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_LOW);
        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.createNotificationChannel(channel);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_INIT: {
                String host = intent.getStringExtra("host");
                int port = intent.getIntExtra("port", 0);
                String token = intent.getStringExtra("token");
                TaskDispatcher.init(2, listener);

                LinkedList<TaskRecord> list;
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "tasks.data");
                try {
                    InputStream inputStream = new FileInputStream(file);
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    list = (LinkedList<TaskRecord>) objectInputStream.readObject();
                    objectInputStream.close();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    list = new LinkedList<>();
                }

                for (TaskRecord record : list) {
                    if (host.equals(record.getHost())) {
                        record.setPort(port);
                        record.setToken(token);
                        TaskDispatcher.get().add(new Task(record));
                    } else {
                        otherTasks.add(record);
                    }
                }
                break;
            }
            case ACTION_PAUSE: {
                String taskId = intent.getStringExtra("id");
                TaskDispatcher.get().pause(taskId);
                break;
            }
            case ACTION_PAUSE_ALL: {
                TaskDispatcher.get().pauseAll();
                Intent i = new Intent();
                i.setAction(ACTION_PAUSE_ALL);
                sendBroadcast(i);
                break;
            }
            case ACTION_RESUME: {
                String taskId = intent.getStringExtra("id");
                TaskDispatcher.get().resume(taskId);
                break;
            }
            case ACTION_RESUME_ALL: {
                TaskDispatcher.get().resumeAll();
                Intent i = new Intent();
                i.setAction(ACTION_RESUME_ALL);
                sendBroadcast(i);
                break;
            }
            case ACTION_CANCEL: {
                String taskId = intent.getStringExtra("id");
                TaskDispatcher.get().cancel(taskId);
                break;
            }
            case ACTION_CANCEL_ALL: {
                TaskDispatcher.get().cancelAll();
                Intent i = new Intent();
                i.setAction(ACTION_CANCEL_ALL);
                sendBroadcast(i);
                break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        TaskDispatcher.get().quit();
        TaskDispatcher.get().refresh();
        Collection<Task> tasks = TaskDispatcher.get().getTasks();
        for (Task task : tasks) {
            otherTasks.add(task.getRecord());
        }

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "tasks.data");
        try {
            OutputStream outputStream = new FileOutputStream(file, false);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(otherTasks);
            objectOutputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
            Log.w("Test", "onDestroy: ", exception);
        }

        super.onDestroy();
    }
}
