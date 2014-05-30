/*
 * Andrew Fisher
 * Logan McGhee
 * Taylor Owen
 */

package ucsc.lmcghee.rememo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import ucsc.lmcghee.rememo.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class VoiceRecorder extends Activity 
{
   private static final String TAG = VoiceRecorder.class.getName();	
   private MediaRecorder recorder; // used to record audio
   private boolean recording; // are we currently recording
   public File tmpFile;
   AudioManager am;
   boolean speakerON;
   Button bt;
   Button bt2;
   View v2;
   
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      
      //Set volume control
      setVolumeControlStream(AudioManager.STREAM_MUSIC);

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
       bt = (Button) findViewById(R.id.recordButton);
       bt2 = (Button) findViewById(R.id.viewSaved);
       v2 = v;
	   if(recording){
		   stopRecording(v);
           bt.setText(R.string.rec);
           bt.setTextColor(getResources().getColor(R.color.white));
           bt2.setEnabled(true);
	   }
	   else{
		   ArrayList<String> values2 = new ArrayList<String>(
                   Arrays.asList(VoiceRecorder.this.getExternalFilesDir(null).list()));
     	  String create = "Create New Category";
     	  values2.add(create);
     	  final CharSequence[] cs = values2.toArray(new CharSequence[values2.size()]);
           

           AlertDialog.Builder builder = new AlertDialog.Builder(this);
           builder.setTitle("Make your selection");
           builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	             public void onClick(DialogInterface dialog, int whichButton) {
	                 // Do nothing.
	             }
	         });
           builder.setItems(cs, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int item) {
                    String temp = (String) cs[item];
                    
                 	   
                    if (temp.equals("Create New Category")){
                 	   
                 	   
                 	   
                 	   LayoutInflater inflater = (LayoutInflater) getSystemService(
                  	            Context.LAYOUT_INFLATER_SERVICE);
                    	  View v1 = inflater.inflate(R.layout.name_edittext, null);
             	         final EditText nameEditText = 
             	            new EditText(VoiceRecorder.this);
             	         nameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
             	         
             	            
             	         // create an input dialog to get recording name from user
             	         new AlertDialog.Builder(VoiceRecorder.this)
             	         .setTitle("Create New Category")
             	         .setView(nameEditText)
             	         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             	             public void onClick(DialogInterface dialog, int whichButton) {
             	                 String value = nameEditText.getText().toString().trim();
             	                 if(value.length() != 0){
             	                	startRecording(v2);
             	                   bt.setText(R.string.stp);
             	                   bt.setTextColor(getResources().getColor(R.color.red));
             	                   bt2.setEnabled(false);
             	                	 try{
             	                	File newFile = new File(
             	                  		 getExternalFilesDir(value).getAbsolutePath() + 
             	                  		 File.separator + getTime() + ".3gp");
             	                	recorder.setOutputFile(newFile.getAbsolutePath());
             	                   recorder.prepare(); // prepare to record   
             	                   recorder.start(); // start recording
             	                   recording = true; // we are currently recording
             	                   TextView tv = (TextView) findViewById(R.id.statusText);
             	                   tv.setText("recording");
             	                	 }       catch (IllegalStateException e){
             	                        Log.e(TAG, e.toString());
             	                    } // end catch 
             	                    catch (IOException e){
             	                       Log.e(TAG, e.toString());
             	                    }
             	                 }else{
            	                	 
            	                 }
            	             }
            	         }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            	             public void onClick(DialogInterface dialog, int whichButton) {
            	                 // Do nothing.
            	             }
            	         }).show();
                    }else{
                    	startRecording(v2);
  	                   bt.setText(R.string.stp);
  	                   bt.setTextColor(getResources().getColor(R.color.red));
  	                   bt2.setEnabled(false);
                    	
                    	try{
                    	
                    	File newFile = new File(
                       		 getExternalFilesDir(temp).getAbsolutePath() + 
                       		 File.separator + getTime() + ".3gp");
                    	recorder.setOutputFile(newFile.getAbsolutePath());
                        recorder.prepare(); // prepare to record   
                        recorder.start(); // start recording
                        recording = true; // we are currently recording
                        TextView tv = (TextView) findViewById(R.id.statusText);
                        tv.setText("recording");
                    	}catch (IllegalStateException e){
                            Log.e(TAG, e.toString());
                         } // end catch 
                         catch (IOException e){
                            Log.e(TAG, e.toString());
                         }
                    }
               }
               }).show();
               
		  
	   }
   }
   
   public void startRecording(View v){
       Log.d("startRecording", "Start Button Pressed.");
       if (recorder == null)
          recorder = new MediaRecorder(); // create MediaRecorder 
       recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
       recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
       recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
       recorder.setAudioEncodingBitRate(96000);
       recorder.setAudioSamplingRate(44100);       
       
    	   
    	   
    	   
    	  
    	   
    	   
    	   
    	  // Create a file for the audio to be saved into
    	  /*File newFile = new File(
            		 getExternalFilesDir("New Memos").getAbsolutePath() + 
            		 File.separator + getTime() + ".3gp");*/
         
          
       } // end try
 // end catch               
    
   
   
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
			  Integer.toString(myCalendar.get(Calendar.HOUR_OF_DAY));

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


