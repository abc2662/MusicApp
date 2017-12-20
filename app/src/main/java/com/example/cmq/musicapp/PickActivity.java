package com.example.cmq.musicapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class PickActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.cmq.musicapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        View contentview = findViewById(R.id.id_content_pick);
        ListView lv = (ListView) contentview.findViewById(R.id.listViewResults);

        final ArrayList<String> FilesInFolder = GetFiles("/sdcard/Music");

        if (FilesInFolder != null)
            lv.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, FilesInFolder));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(PickActivity.this, PlayMusicActivity.class);
                intent.putExtra(EXTRA_MESSAGE, "/sdcard/Music/" + FilesInFolder.get(position));
                startActivity(intent);
            }
        });




    }

    public ArrayList<String> GetFiles(String DirectoryPath) {
        ArrayList<String> MyFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);

        f.mkdirs();
        File[] files = f.listFiles();
        if (files.length == 0)
            return null;
        else {
            for (int i=0; i<files.length; i++)
                MyFiles.add(files[i].getName());
        }

        return MyFiles;
    }
}
