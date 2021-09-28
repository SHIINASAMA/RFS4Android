package pers.kaoru.rfsclient.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pers.kaoru.rfsclient.ui.TaskListAdapter;

public class TaskDispatcher extends Thread {

    // 单例
    private static TaskDispatcher instance;

    public static TaskDispatcher init(int maxTaskAmount, TaskListener listener) {
        if (instance == null) {
            instance = new TaskDispatcher(maxTaskAmount, listener);
        }
        return instance;
    }

    public static TaskDispatcher get() {
        return instance;
    }

    // 核心
    private final Semaphore permit;
    private final ExecutorService executorService;
    private final HashMap<String, Task> taskHashMap = new HashMap<>();
    private TaskListener listener;

    private final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();
    private volatile boolean isQuit = false;

    private TaskDispatcher(int maxTaskAmount, TaskListener listener) {
        permit = new Semaphore(maxTaskAmount);
        executorService = new ThreadPoolExecutor(maxTaskAmount, maxTaskAmount, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> new Thread(r, "task_thread"));
        this.listener = listener;
        start();
    }

    public void quit() {
        isQuit = true;
        interrupt();
    }

    public Semaphore getPermit() {
        return permit;
    }

    public String add(Task task) {
        String id = task.getRecord().getUid();
        taskHashMap.put(id, task);
        taskQueue.add(task);
        return id;
    }

    public boolean pause(String taskId) {
        Task task = taskHashMap.get(taskId);
        if (task != null) {
            if (task.getState() == TaskState.RUNNING) {
                task.setState(TaskState.PAUSED);
                onPaused(task.getRecord());
                return true;
            }
        }
        return false;
    }

    public void pauseAll() {
        for (Task task : taskHashMap.values()) {
            if (task.getState() == TaskState.RUNNING) {
                task.setState(TaskState.PAUSED);
            }
        }
    }

    public boolean cancel(String taskId) {
        Task task = taskHashMap.get(taskId);
        if (task != null) {
            task.setState(TaskState.CANCELED);
            onCanceled(task.getRecord());
            return true;
        }
        return false;
    }

    public void cancelAll() {
        for (Task task : taskHashMap.values()) {
            if (task.getState() == TaskState.RUNNING || task.getState() == TaskState.PAUSED) {
                task.setState(TaskState.CANCELED);
            }
        }
    }

    public boolean resume(String taskId) {
        Task task = taskHashMap.get(taskId);
        if (task != null && task.getState() == TaskState.PAUSED) {
            task.setState(TaskState.RUNNING);
            onResume(task.getRecord());
            taskQueue.add(task);
            return true;
        }
        return false;
    }

    public void resumeAll() {
        for (Task task : taskHashMap.values()) {
            if (task.getState() == TaskState.PAUSED) {
                task.setState(TaskState.RUNNING);
            }
        }
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Task task = taskQueue.take();
                getPermit().acquire();
                start(task);
            } catch (InterruptedException exception) {
//                exception.printStackTrace();
                if (isQuit) {
                    return;
                }
            }
        }
    }

    private void start(Task task) {
        task.setState(TaskState.RUNNING);
        executorService.execute(task);
    }

    public TaskState getState(String taskId) {
        Task task = taskHashMap.get(taskId);
        if (task != null) {
            return task.getState();
        }
        return TaskState.FAILED;
    }

    // 任务回调
    public void onProgress(TaskRecord record, long speed) {
        listener.onProgress(record, speed);
    }

    public void onFailed(TaskRecord record, String error) {
        permit.release();
        listener.onFailed(record, error);
    }

    public void onPaused(TaskRecord record) {
        listener.onPaused(record);
    }

    public void onResume(TaskRecord record) {
        listener.onResume(record);
    }

    public void onFinish(TaskRecord record) {
        permit.release();
        listener.onFinish(record);
    }

    public void onCanceled(TaskRecord record) {
        permit.release();
        listener.onCanceled(record);
    }

    public Collection<Task> getTasks() {
        return taskHashMap.values();
    }

    public void remove(String taskId) {
        taskHashMap.remove(taskId);
    }

    public void refresh() {
        for (Task task : taskHashMap.values()) {
            if (task.getState() == TaskState.FINISH || task.getState() == TaskState.FAILED || task.getState() == TaskState.CANCELED) {
                taskHashMap.remove(task.getRecord().getUid());
            }
        }
    }
}
