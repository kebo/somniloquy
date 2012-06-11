package info.iamkebo.somniloquy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Somniloquy extends Activity {
	
	private static final String LOG_TAG = "Somniloquy";
	
	private Button recordBtn;
	private ListView recordList;
	private boolean isRecord, isPlay;
	
	private static String START_RECORDING;
	private static String STOP_RECORDING;
	
	private String[] records;
	private ListAdapter adapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        recordBtn = (Button)findViewById(R.id.record);
        recordBtn.setOnClickListener(recordListener);
        
        recordList = (ListView)findViewById(R.id.recordlist);
        recordList.setOnItemClickListener(itemClickListener);
        recordList.setOnItemLongClickListener(itemLongClickListener);
        
        isRecord = false;
        isPlay = false;
        
        Resources r = getResources();
        START_RECORDING = r.getString(R.string.start_record);
        STOP_RECORDING  = r.getString(R.string.stop_record);
        
        Log.i(LOG_TAG, "onCreate");
    }
    
    @Override
    protected void onStart() {
    	plenishList();
    	super.onStart();
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	
    	Log.i(LOG_TAG, "onPause");
    }
    
    private void plenishList(){
    	//TODO:get the list from db
        records = new File(Constants.dir).list();
        adapter = new ArrayAdapter<String>(
        		this, android.R.layout.simple_list_item_1, records);
        recordList.setAdapter(adapter);
    }
    
    private void startRecord(){
    	Intent service = new Intent(this, MediaService.class);
    	Bundle bundle = new Bundle();
    	bundle.putInt(MediaService.MEDIA_TYPE, MediaService.RECORD);
    	service.putExtras(bundle);
    	startService(service);
    	Log.i(LOG_TAG, "Start Record");
    }
    
    private void stopRecord(){
    	stopMediaService(MediaService.RECORD);
    	plenishList();
    	Log.i(LOG_TAG, "Stop Record");
    }
    
    private void startPlay(String filename){
    	Intent service = new Intent(this, MediaService.class);
    	Bundle bundle = new Bundle();
    	bundle.putInt(MediaService.MEDIA_TYPE, MediaService.PLAY);
    	bundle.putString(MediaService.MEDIA_FILE, filename);
    	service.putExtras(bundle);
    	startService(service);
    	
    	Log.i(LOG_TAG, "Start PLay");
    }
    
    private void stopPlay(){
    	stopMediaService(MediaService.PLAY);
    	Log.i(LOG_TAG, "Stop Play");
    }
    
    private void stopMediaService(int type){
    	Intent service = new Intent(this, MediaService.class);
    	Bundle bundle = new Bundle();
    	bundle.putInt(MediaService.MEDIA_TYPE, type);
    	service.putExtras(bundle);
    	stopService(service);
    }
    
    private OnClickListener recordListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(isRecord){
				stopRecord();
				recordBtn.setText(START_RECORDING);
				
			}else{
				startRecord();
				recordBtn.setText(STOP_RECORDING);
			}
			isRecord = !isRecord;
		}
	};
    
	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			if(isPlay){
				stopPlay();
			}else{
				String filename = records[position];
				startPlay(filename);
			}
			
			isPlay = !isPlay;
		}
	};
	
	private OnItemLongClickListener itemLongClickListener = 
			new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view,
						int position, long id) {
					String filepath = Constants.dir + records[position];
					boolean delete = Utils.removeFile(filepath);
					if(delete){
						plenishList();
					}
					
					return true;
				}
			};
	
}