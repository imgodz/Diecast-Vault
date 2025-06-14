package com.imgodz.diecastvault;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImportTask extends AsyncTask<Uri, Void, Boolean> {
    public interface ImportListener {
        void onImportStarted();
        void onImportComplete(boolean success);
    }

    private final Context context;
    private final ImportListener listener;

    public ImportTask(Context context, ImportListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onImportStarted();
    }

    @Override
    protected Boolean doInBackground(Uri... uris) {
        if (uris.length == 0 || uris[0] == null) return false;

        try {
            // 1. Get input stream from URI
            InputStream inputStream = context.getContentResolver().openInputStream(uris[0]);
            if (inputStream == null) return false;

            // 2. Create temp file
            File tempFile = new File(context.getCacheDir(), "temp_import.db");

            // 3. Copy to temp location
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            // 4. Validate database structure
            if (!isValidDatabase(tempFile.getAbsolutePath())) {
                tempFile.delete();
                return false;
            }

            // 5. Get current database path
            File currentDb = context.getDatabasePath("diecast_database");

            // 6. Close database connection
            DiecastDatabase.getInstance(context).close();

            // 7. Replace current database
            copyFile(tempFile, currentDb);

            // 8. Clean up
            tempFile.delete();

            return true;

        } catch (IOException e) {
            Log.e("ImportTask", "Import failed", e);
            return false;
        }
    }

    private boolean isValidDatabase(String path) {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            // Check if required tables exist
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='diecast_table'", null);
            boolean isValid = cursor != null && cursor.getCount() > 0;
            if (cursor != null) cursor.close();
            return isValid;
        } catch (SQLiteException e) {
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        listener.onImportComplete(success);
    }
}