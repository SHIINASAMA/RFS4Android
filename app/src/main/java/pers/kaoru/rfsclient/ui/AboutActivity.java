package pers.kaoru.rfsclient.ui;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import pers.kaoru.rfsclient.R;

public class AboutActivity extends AppCompatActivity {

    private TextView versionText;
    private TextView deveolperText;
    private TextView repoText;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        versionText = findViewById(R.id.versionText);
        deveolperText = findViewById(R.id.developerText);
        repoText = findViewById(R.id.websiteText);

        PackageInfo info = null;
        try {
            info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (info != null) {
            versionText.setText(info.versionName + "(" + info.versionCode + ")");
        } else {
            versionText.setText(R.string.unknown_version_string);
        }

        deveolperText.setMovementMethod(LinkMovementMethod.getInstance());
        repoText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        this.finish();
        return true;
    }
}
