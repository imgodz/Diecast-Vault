package com.imgodz.diecastvault;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportTask extends AsyncTask<Void, Void, File> {
    private final Context context;
    private final ExportListener listener;

    public interface ExportListener {
        void onExportStarted();
        void onExportComplete(File exportedFile);
    }

    public ExportTask(Context context, ExportListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onExportStarted();
    }

    @Override
    protected File doInBackground(Void... voids) {
        try {
            File dbFile = context.getDatabasePath("diecast_database");
            File exportDir = new File(context.getExternalFilesDir(null), "backups");

            if (!exportDir.exists()) exportDir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            File backupFile = new File(exportDir, "diecast_backup_" + timestamp + ".db");

            // Close database before copying
            DiecastDatabase.getInstance(context).close();

            // Copy file
            try (InputStream in = new FileInputStream(dbFile);
                 OutputStream out = new FileOutputStream(backupFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }

            return backupFile;
        } catch (IOException e) {
            Log.e("ExportTask", "Export failed", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(File file) {
        listener.onExportComplete(file);
    }
}