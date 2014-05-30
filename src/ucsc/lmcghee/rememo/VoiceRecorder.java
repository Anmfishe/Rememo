/*
 * Andrew Fisher
 * Logan McGhee
 * Taylor Owen
 */

package ucsc.lmcghee.rememo;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class VoiceRecorder extends Activity 
{
   private static final String TAG = VoiceRecorder.class.getName();	
   private MediaRecorder recorder; // used to record audio
   private boolean recording; // are we currently recording
   public File tmpFile;
   AudioManager am;
   boolean speakerON;
   
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

	  //createDir("Homework");
	  //createDir("New Memos");
	  //createDir("Groceries");
   } 
   
   // create the MediaRecorder
   @Override
   protected void onResume()
   {
      super.onResume();
   } // end method onResume
   
   @Override
   protected void onPause()
   {
      super.onPause();
      if (recorder != null)
      {
         recorder.release(); // release MediaRecorder resources
         recording = false; // we are no longer recording
         recorder = null; 
      } 
   } 
   
   private void createDir(String s){
		File direct =  getExternalFilesDir(s);
			//direct.mkdir();
		}
   
   
   public void startStop(View v){
       TextView tv = (TextView) findViewById(R.id.recordButton);
	   if(recording){
		   stopRecording(v);
           tv.setText(R.string.rec);
	   }
	   else{
		   startRecording(v);
          tv.setText(R.string.stp);
	   }
   }
   
   public void startRecording(View v){
       Log.d("startRecording", "Start Button Pressed.");
       if (recorder == null)
          recorder = new MediaRecorder(); // create MediaRecorder 
       recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
       recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
       recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
       recorder.setAudioEncodingBitRate(16);
       recorder.setAudioSamplingRate(44100);       
       try{
    	  // Create a file for the audio to be saved into
    	  File newFile = new File(
            		 getExternalFilesDir("New Memos").getAbsolutePath() + 
            		 File.separator + getTime() + ".3gp");
         
          recorder.setOutputFile(newFile.getAbsolutePath());
          recorder.prepare(); // prepare to record   
          recorder.start(); // start recording
          recording = true; // we are currently recording
          TextView tv = (TextView) findViewById(R.id.statusText);
          tv.setText("recording");
       } // end try
       catch (IllegalStateException e){
          Log.e(TAG, e.toString());
       } // end catch 
       catch (IOException e){
          Log.e(TAG, e.toString());
       } // end catch               
   } 
   
   
   public void stopRecording(View v){
	   if(!recording) return; //breaks here if not currently recording
       recorder.stop(); // stop recording
       recorder.reset(); // reset the MediaRecorder
       recording = false; // we are no longer recording
       TextView tv = (TextView) findViewById(R.id.statusText);
       tv.setText("stopped");
   }
   
   public String getTime(){
	   // Returns time in form of a string formatted as m-d hr:mn 
	   String time = "";
	   Calendar myCalendar = Calendar.getInstance();
	   time = Integer.toString(1 + myCalendar.get(Calendar.MONTH)) 		+ "-" +
			  Integer.toString(myCalendar.get(Calendar.DAY_OF_MONTH)) 	+ " " +
			  Integer.toString(myCalendar.get(Calendar.HOUR_OF_DAY)) 	+ ":";

	   if(myCalendar.get(Calendar.MINUTE) > 9)
		   time = time + ":" + Integer.toString(myCalendar.get(Calendar.MINUTE));
	   else
		   time = time + ":0" + Integer.toString(myCalendar.get(Calendar.MINUTE));
	   
	   if(myCalendar.get(Calendar.SECOND) > 9)
		   time = time + ":" + Integer.toString(myCalendar.get(Calendar.SECOND));
	   else
		   time = time + ":0" + Integer.toString(myCalendar.get(Calendar.SECOND));
			  
	   return time;
   }
   
   public void viewSaved(View v){ 
	   Intent intent = 
           new Intent(VoiceRecorder.this, Categories.class);
        startActivity(intent);
   }
} 


