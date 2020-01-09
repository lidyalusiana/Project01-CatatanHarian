package id.ac.poliban.mi.project01;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_STORAGE = 100; //konstanta
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar); //mengenalkan view toolbar
        setSupportActionBar(toolbar); //pasang toolbar sebagai ActionBar di MainActivity
        getSupportActionBar().setTitle("Aplikasi Catatan Proyek 1");
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //buat object intent untuk menjalankan activity InsertAndViewActivity
                Intent intent = new Intent(MainActivity.this, InsertAndViewActivity.class);
                //ambil data yang telah anda klik pada list, kemudian simpan ke data
                Map<String, Object> data = (Map<String, Object>) parent.getAdapter().getItem(position);
                //letakkan data pada intent dengan key "filename" data yang dikirim adalah data index "name"
                intent.putExtra("filename", data.get("name").toString());

                //tampilkan pesan "you clicked" dan list yang telah diklik
                Toast.makeText(MainActivity.this, "You clicked " + data.get("name"), Toast.LENGTH_SHORT).show();

                //jalankan activity sesuai activity yang telah ditentukan melalui intent (InsertAndViewActivity)
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Map<String, Object> data = (Map<String, Object>) parent.getAdapter().getItem(position);
            tampilkanDialogKonfirmasiHapusCatatan(data.get("name").toString());
            return true;
        });
    }

    void tampilkanDialogKonfirmasiHapusCatatan(final String filename) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Catatan ini?")
                .setMessage("Apakah Anda yakin ingin menghapus Catatan "+ filename +"?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("YES", (dialog, whichButton) -> hapusFile(filename))
                .setNegativeButton("NO", null).show();
    }

    private void hapusFile(String filename) {
        String path = getExternalFilesDir(null) + "/proyek01.kominfo";

        File file = new File(path, filename);
        if (file.exists()) {
            file.delete();
        }
        mengambilListFilePadaFolder();
    }

    private void mengambilListFilePadaFolder() {
        String path = getExternalFilesDir(null) + "/proyek01.kominfo";
        File directory = new File(path);

        if (directory.exists()) {
            File[] files = directory.listFiles();
            String[] filenames = new String[files.length];
            String[] dateCreated = new String[files.length];
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
            ArrayList<Map<String, Object>> itemDataList = new ArrayList<Map<String, Object>>();

            for (int i = 0; i < files.length; i++) {
                filenames[i] = files[i].getName();
                Date lastModDate = new Date(files[i].lastModified());
                dateCreated[i] = simpleDateFormat.format(lastModDate);
                Map<String, Object> listItemMap = new HashMap<>();
                listItemMap.put("name", filenames[i]);
                listItemMap.put("date", dateCreated[i]);
                itemDataList.add(listItemMap);

            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(this, itemDataList, android.R.layout.simple_list_item_2,
                    new String[]{"name", "date"}, new int[]{android.R.id.text1, android.R.id.text2});
            listView.setAdapter(simpleAdapter);
            simpleAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tambah:
                Intent intent = new Intent(this, InsertAndViewActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            if (periksaIzinPenyimpanan()) {
                mengambilListFilePadaFolder();
            }
        } else {
            mengambilListFilePadaFolder();
        }
    }

    private boolean periksaIzinPenyimpanan() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mengambilListFilePadaFolder();
                }
                break;
        }
    }
}