package com.example.khan.spyrecorder;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import com.google.android.gms.drive.Drive;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Shahjahan";
    private MediaRecorder myAudioRecorder;
    private String outputFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("SPY RECORDER");
        setSupportActionBar(toolbar);
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";

        myAudioRecorder = new MediaRecorder();

        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        myAudioRecorder.setOutputFile(outputFile);



    }


    public void record(View view) {
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
            Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TAG","Something went wrong while preparing the recorder");
        }
    }

    public void stop(View view) {
        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder  = null;
        Log.d("TAG","Something went wrong while preparing the recorder");

    }

    public void play(View view) {
        MediaPlayer m = new MediaPlayer();
        try {
            m.setDataSource(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            m.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        m.start();
        Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
    }

    /**
     * Create a new file and save it to Drive.
     */
    private void saveFiletoDrive(final File file, final String mime) {
        // Start by creating a new contents, and setting a callback.
        Drive.DriveApi.newDriveContents(mDriveClient).setResultCallback(
                new ResultCallback<DriveContentsResult>() {
                    @Override
                    public void onResult(DriveContentsResult result) {
                        // If the operation was not successful, we cannot do
                        // anything
                        // and must
                        // fail.
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        Log.i(TAG, "Connection successful, creating new contents...");
                        // Otherwise, we can write our data to the new contents.
                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents()
                                .getOutputStream();
                        FileInputStream fis;
                        try {
                            fis = new FileInputStream(file.getPath());
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buf = new byte[1024];
                            int n;
                            while (-1 != (n = fis.read(buf)))
                                baos.write(buf, 0, n);
                            byte[] photoBytes = baos.toByteArray();
                            outputStream.write(photoBytes);

                            outputStream.close();
                            outputStream = null;
                            fis.close();
                            fis = null;

                        } catch (FileNotFoundException e) {
                            Log.w(TAG, "FileNotFoundException: " + e.getMessage());
                        } catch (IOException e1) {
                            Log.w(TAG, "Unable to write file contents." + e1.getMessage());
                        }

                        String title = file.getName();
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType(mime).setTitle(title).build();

                        if (mime.equals(MIME_PHOTO)) {
                            if (VERBOSE)
                                Log.i(TAG, "Creating new photo on Drive (" + title
                                        + ")");
                            Drive.DriveApi.getFolder(mDriveClient,
                                    mPicFolderDriveId).createFile(mDriveClient,
                                    metadataChangeSet,
                                    result.getDriveContents());
                        } else if (mime.equals(MIME_VIDEO)) {
                            Log.i(TAG, "Creating new video on Drive (" + title
                                    + ")");
                            Drive.DriveApi.getFolder(mDriveClient,
                                    mVidFolderDriveId).createFile(mDriveClient,
                                    metadataChangeSet,
                                    result.getDriveContents());
                        }

                        if (file.delete()) {
                            if (VERBOSE)
                                Log.d(TAG, "Deleted " + file.getName() + " from sdcard");
                        } else {
                            Log.w(TAG, "Failed to delete " + file.getName() + " from sdcard");
                        }
                    }
                });
    }

}
