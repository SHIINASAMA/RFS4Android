package pers.kaoru.rfsclient.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pers.kaoru.rfsclient.R;
import pers.kaoru.rfsclient.core.BitCount;
import pers.kaoru.rfsclient.core.FileInfo;

public class FileListAdapter extends BaseAdapter {

    private List<FileInfo> fileInfoList;
    private final LayoutInflater inflater;

    public FileListAdapter(Context context, List<FileInfo> fileInfoList) {
        this.fileInfoList = fileInfoList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return fileInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return fileInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        FileInfo info = fileInfoList.get(i);
        view = inflater.inflate(R.layout.file_item_layout, null);
        ImageView icon = view.findViewById(R.id.fileItemIcon);
        TextView name = view.findViewById(R.id.fileItemName);
        TextView length = view.findViewById(R.id.fileItemLength);
        TextView date = view.findViewById(R.id.fileItemDate);

        if (info.isDirectory()) {
            icon.setImageResource(R.drawable.dir);
        } else {
            icon.setImageResource(R.drawable.file);
        }
        name.setText(info.getName());
        length.setText(BitCount.ToString(info.getSize()));

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date.setText(dateFormat.format(new Date(info.getLast())));

        return view;
    }

    public void clear() {
        fileInfoList.clear();
    }

    public void reset(List<FileInfo> fileInfoList) {
        this.fileInfoList = fileInfoList;
    }
}
