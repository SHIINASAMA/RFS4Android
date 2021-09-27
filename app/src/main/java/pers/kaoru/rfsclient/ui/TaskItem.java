package pers.kaoru.rfsclient.ui;

import android.annotation.SuppressLint;
import android.widget.ProgressBar;
import android.widget.TextView;

import pers.kaoru.rfsclient.core.BitCount;
import pers.kaoru.rfsclient.service.TaskDispatcher;
import pers.kaoru.rfsclient.service.TaskRecord;
import pers.kaoru.rfsclient.service.TaskState;
import pers.kaoru.rfsclient.service.TaskType;

public class TaskItem {

    private final TextView taskName;
    private final ProgressBar taskProgress;
    private final TextView taskState;
    private final TextView taskFraction;
    private final TextView taskSpeed;
    private final TextView taskType;
    private final TaskRecord record;

    public TaskItem(
            TextView taskName,
            ProgressBar taskProgress,
            TextView taskState,
            TextView taskFraction,
            TextView taskSpeed,
            TextView taskType,
            TaskRecord record
    ) {
        this.taskName = taskName;
        this.taskProgress = taskProgress;
        this.taskSpeed = taskSpeed;
        this.taskState = taskState;
        this.taskFraction = taskFraction;
        this.taskType = taskType;
        this.record = record;
    }

    @SuppressLint("SetTextI18n")
    public void updateSpeed() {
        this.taskSpeed.setText(BitCount.ToString(record.getSpeed()) + "/S");
    }

    public void setState(TaskState state) {
        this.taskState.setText(state.name());
    }

    @SuppressLint("SetTextI18n")
    public void setProgress() {
        int v = (int) ((float) record.getCurrent() / record.getLength() * 100);
        taskProgress.setProgress(v);
        taskFraction.setText(BitCount.ToString(record.getCurrent()) + " / " + BitCount.ToString(record.getLength()));
    }

    public void setName(String name) {
        this.taskName.setText(name);
    }

    public void updateType() {
        this.taskType.setText(record.getType().name());
    }

    public void updateState() {
        this.taskState.setText(TaskDispatcher.get().getState(record.getUid()).name());
    }

    public TextView getTaskName() {
        return taskName;
    }

    public ProgressBar getTaskProgress() {
        return taskProgress;
    }

    public TextView getTaskState() {
        return taskState;
    }

    public TextView getTaskFraction() {
        return taskFraction;
    }

    public TextView getTaskSpeed() {
        return taskSpeed;
    }

    public TextView getTaskType() {
        return taskType;
    }

    public TaskRecord getRecord() {
        return record;
    }
}
