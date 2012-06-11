package info.iamkebo.somniloquy;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.accounts.Account;
import android.accounts.OnAccountsUpdateListener;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Media Service 
 * @author kebo
 *
 */

public class MediaService extends Service {
	private final static String LOG_TAG = "RecordService";
	
	private ExecutorService exec;
	private MediaRecorder mediaRecorder;
	private MediaPlayer mediaPlayer;
	private String filename = null;
	private String dir;
	
	public static final String MEDIA_TYPE = "mediaType";
	public static final String MEDIA_FILE = "mediaFile";
	public static final int RECORD = 1;
	public static final int PLAY = 2;
	
	private int mediaType;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		dir = Constants.dir;
		buildDir(dir);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		exec = Executors.newSingleThreadExecutor();
		
		Bundle bundle = intent.getExtras();
		mediaType = MediaService.RECORD;
		if(bundle != null && bundle.containsKey(MEDIA_TYPE)){
			mediaType = bundle.getInt(MEDIA_TYPE);
		}
		
		if(mediaType == MediaService.RECORD){
			//create a new media file
			setFilenameByTime();
			exec.execute(new RecordSound());
		}else if(mediaType == MediaService.PLAY){
			//check the media file exists
			if(bundle.containsKey(MEDIA_FILE)){
				filename = bundle.getString(MEDIA_FILE);
				filename = dir + filename;
				File f = new File(filename);
				if(f.exists() && f.isFile()){
					exec.execute(new PlaySound());
				}
			}
		}
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		exec.shutdownNow();
		if(mediaType == MediaService.RECORD){
			stopRecording();
		}else if(mediaType == MediaService.PLAY){
			stopPlaying();
		}
	}
	
	private class RecordSound implements Runnable{

		@Override
		public void run() {
			startRecording();
		}
	};
	
	private void startRecording(){
		mediaRecorder = new MediaRecorder();
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    	mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    	mediaRecorder.setOutputFile(filename);
    	mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    	
    	try{
    		mediaRecorder.prepare();
    	}catch (IOException e) {
    		Log.e(LOG_TAG, "MediaRecorder failed!", e);
		}
    	mediaRecorder.start();
	}
	
	private void stopRecording(){
		if(mediaRecorder != null){
			mediaRecorder.stop();
			mediaRecorder.release();
			mediaRecorder = null;
		}
	}

	private class PlaySound implements Runnable{

		@Override
		public void run() {
			startPlaying();
		}
		
	}
	
	private void startPlaying(){
    	mediaPlayer = new MediaPlayer();
    	try{
    		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    		mediaPlayer.setDataSource(filename);
    		mediaPlayer.prepare();
    		mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    		mediaPlayer.setOnCompletionListener(completionListener);
    		mediaPlayer.start();
    	}catch(IllegalArgumentException e){
    		Log.e(LOG_TAG, "mediaPlayer failed", e);
    	}catch (IOException e) {
    		Log.e(LOG_TAG, "MediaPlayer failed", e);
		}
    }
    
    private void stopPlaying(){
    	if(mediaPlayer != null){
	    	mediaPlayer.stop();
	    	mediaPlayer.release();
			mediaPlayer = null;
    	}
    }
	
    
    private void setFilenameByTime(){
    	Calendar today = Calendar.getInstance();
    	String name = String.format("%d.3pg", today.getTimeInMillis());
    	filename = dir + name;
    }
    
    private void buildDir(String dir){
    	File f = new File(dir);
    	if(!f.exists()){
    		f.mkdir();
    	}
    }
    
    private OnCompletionListener completionListener = 
    		new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer arg0) {
					mediaPlayer.release();
					mediaPlayer = null;
				}
			};
}
