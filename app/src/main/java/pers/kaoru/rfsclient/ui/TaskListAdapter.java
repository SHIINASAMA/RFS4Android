package pers.kaoru.rfsclient.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import pers.kaoru.rfsclient.R;
import pers.kaoru.rfsclient.service.TaskRecord;

public class TaskListAdapter extends BaseAdapter {

    private List<TaskRecord> recordList;
    private final LayoutInflater inflater;
    private final HashMap<String, TaskItem> taskItemHashMap = new HashMap<>();

    public TaskListAdapter(Context context, List<TaskRecord> recordList) {
        this.recordList = recordList;
        inflater = LayoutInflater.from(context);
    }

    public TaskItem getTaskView(String taskId){
        return taskItemHashMap.get(taskId);
    }

    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Object getItem(int i) {
        return recordList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TaskRecord record = recordList.get(i);
        view = inflater.inflate(R.layout.task_item_layout, null);

        TextView name = view.findViewById(R.id.taskListName);
        TextView type = view.findViewById(R.id.taskListType);
        TextView speed = view.findViewById(R.id.taskListSpeed);
        TextView state = view.findViewById(R.id.taskListState);
        ProgressBar progress = view.findViewById(R.id.taskListProgress);
        TextView fraction = view.findViewById(R.id.taskListFraction);

        TaskItem item = new TaskItem(name, progress, state, fraction, speed, type, record);
        item.setName(record.getName());
        item.updateType();
        item.updateProgress();
        item.updateSpeed();
        item.updateState();

        this.taskItemHashMap.put(record.getUid(), item);
        return view;
    }

    public HashMap<String, TaskItem> getTaskItemHashMap() {
        return taskItemHashMap;
    }

    public void reset(List<TaskRecord> recordList) {
        this.recordList = recordList;
        this.taskItemHashMap.clear();
    }

    public void add(TaskRecord record) {
        this.recordList.add(record);
    }
}
