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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class VoiceRecorder extends Activity{
   private static final String TAG = VoiceRecorder.class.getName();	
   private MediaRecorder recorder; // used to record audio
   private boolean recording; // are we currently recording
   public File tmpFile;
   
   
   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main); // set the Activity's layout
      
   } 
   
   
   @Override
   protected void onResume(){
      super.onResume();
   } 
   
   // release the MediaRecorder
   @Override
   protected void onPause(){
      super.onPause();
      
      if (recorder != null)
      {
         recorder.release(); // release MediaRecorder resources
         recording = false; // we are no longer recording
         recorder = null; 
      } 
   } 

   
   public void startRecording(View v){
       Log.d("startRecording", "Start Button Pressed.");
       
	// create MediaRecorder and configure recording options
       if (recorder == null)
          recorder = new MediaRecorder(); // create MediaRecorder 
       recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
       recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
       recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
       recorder.setAudioEncodingBitRate(16);
       recorder.setAudioSamplingRate(44100);
       
       
       try{
    	  // Create a file for the audio to be saved into
    	  File newFile = new File(
            		 getExternalFilesDir("New Memos").getAbsolutePath() + 
            		 File.separator + getTime() + ".aac");
         
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
       recorder.stop();   // stop recording
       recorder.reset();  // reset the MediaRecorder
       recording = false; // we are no longer recording
       TextView tv = (TextView) findViewById(R.id.statusText);
       tv.setText("stopped");
	   
   }
   
   public String getTime(){
	   //returns time formatted as "year-m-d_hr:mn:sc"
	   String time = "";
	   Calendar myCalendar = Calendar.getInstance();
	   time = Integer.toString(myCalendar.get(Calendar.YEAR)) 			+ "-" +
			  Integer.toString(1 + myCalendar.get(Calendar.MONTH)) 		+ "-" +
			  Integer.toString(myCalendar.get(Calendar.DAY_OF_MONTH)) 	+ "_" +
			  Integer.toString(myCalendar.get(Calendar.HOUR_OF_DAY)) 	+ ":" +
			  Integer.toString(myCalendar.get(Calendar.MINUTE)) 		+ ":" +
			  Integer.toString(myCalendar.get(Calendar.SECOND));
	   
	   return time;
   }
   
   // Open saveRecordings class on button press.
   public void viewSaved(View v){ 
	   Intent intent = new Intent(VoiceRecorder.this, SavedRecordings.class);
       startActivity(intent);
   }
   

} 

