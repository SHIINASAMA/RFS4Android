package pers.kaoru.rfsclient.service;

public interface TaskListener {

    void onProgress(TaskRecord record, long speed);

    void onFailed(TaskRecord record, String error);

    void onPaused(TaskRecord record);

    void onStart(TaskRecord record);

    void onResume(TaskRecord record);

    void onFinish(TaskRecord record);

    void onCanceled(TaskRecord record);
}
