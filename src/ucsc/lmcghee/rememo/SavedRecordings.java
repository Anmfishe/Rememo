package ucsc.lmcghee.rememo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class SavedRecordings extends ListActivity 
{
   private static final String TAG = SavedRecordings.class.getName();
	
   // SavedRecordingsAdapter displays list of saved recordings in ListView
   private SavedRecordingsAdapter savedRecordingsAdapter;
	
   private MediaPlayer mediaPlayer; // plays saved recordings
   private SeekBar progressSeekBar; // controls audio playback
   private Handler handler; // updates the SeekBar thumb position
   private TextView nowPlayingTextView; // displays audio name
   private ToggleButton playPauseButton; // displays audio name
   ListView listView;
   ListView lv;
   Bundle extra;
   String location = null;
   ArrayAdapter<String> adapter;
   int i;

   // called when the activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.saved_recordings);
      extra = getIntent().getExtras();
      if(extra != null){
    	  location = extra.getString("KEY");
      }

      // get ListView and set its listeners and adapter 
      listView = getListView();
      savedRecordingsAdapter = new SavedRecordingsAdapter(this, 
         new ArrayList<String>(
            Arrays.asList(getExternalFilesDir(location).list())));
      listView.setAdapter(savedRecordingsAdapter);
      registerForContextMenu(listView);
      
      handler = new Handler(); // updates SeekBar thumb position

      // get other GUI components and register listeners
      progressSeekBar = (SeekBar) findViewById(R.id.progressSeekBar);
      progressSeekBar.setOnSeekBarChangeListener(
         progressChangeListener);
      playPauseButton = (ToggleButton) findViewById(R.id.playPauseButton);
      playPauseButton.setOnCheckedChangeListener(playPauseButtonListener);
      nowPlayingTextView = 
         (TextView) findViewById(R.id.nowPlayingTextView);
      
      
      /*ArrayList<String> values2 = new ArrayList<String>(
              Arrays.asList(getExternalFilesDir(null).list()));
      //values2.add();
      adapter = new ArrayAdapter<String>(SavedRecordings.this, R.layout.saved_recordings_row, R.id.nameTextView, values2);
      Log.d("WHAT", adapter.toString());
      lv = (ListView) findViewById(R.id.listview2);
      Log.d("WHAT", lv.toString());
      lv.setAdapter(adapter);
      Log.d("WHAT", "yo4");*/
      
      
      
   } // end method onCreate
   
   // create the MediaPlayer object
   @Override
   protected void onResume()
   {
      super.onResume();
      mediaPlayer = new MediaPlayer(); // plays recordings
   } // end method onResume

   // release the MediaPlayer object
   @Override
   protected void onPause()
   {
      super.onPause();
      
      if (mediaPlayer != null)
      {
         handler.removeCallbacks(updater); // stop updating GUI
         mediaPlayer.stop(); // stop audio playback
         mediaPlayer.release(); // release MediaPlayer resources
         mediaPlayer = null; 
      } // end if
   } // end method onPause

   // Class for implementing the "ViewHolder pattern"
   // for better ListView performance
   private static class ViewHolder
   {
      TextView nameTextView; 
   } // end class ViewHolder
   
   // ArrayAdapter displaying recording names and delete buttons
   private class SavedRecordingsAdapter extends ArrayAdapter<String>
   {
      private List<String> items; // list of file names
      private LayoutInflater inflater;
      
      public SavedRecordingsAdapter(Context context, List<String> items)
      {
         super(context, -1, items); // -1 indicates we're customizing view
         Collections.sort(items, String.CASE_INSENSITIVE_ORDER);
         this.items = items;
         inflater = (LayoutInflater) 
            getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      } // end SavedRecordingsAdapter constructor

      @Override
      public View getView(int position, View convertView, ViewGroup parent)
      {
         ViewHolder viewHolder; // holds references to current item's GUI
         
         // if convertView is null, inflate GUI and create ViewHolder;
         // otherwise, get existing ViewHolder
         if (convertView == null)
         {
            convertView = 
               inflater.inflate(R.layout.saved_recordings_row, null);
            
            // set up ViewHolder for this ListView item
            viewHolder = new ViewHolder();
            viewHolder.nameTextView = 
               (TextView) convertView.findViewById(R.id.nameTextView);
            convertView.setTag(viewHolder); // store as View's tag
         } // end if
         else // get the ViewHolder from the convertView's tag
            viewHolder = (ViewHolder) convertView.getTag();
         
         // get and display name of recording file 
         String item = items.get(position);
         viewHolder.nameTextView.setText(item); 


         return convertView;
      } // end method getView
   } // end class SavedRecordingsAdapter
   
   // sends specified recording as email attachment
   OnClickListener emailButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(final View v) 
      {
         // get Uri to the recording's location on disk
         Uri data = Uri.fromFile(
            new File(getExternalFilesDir(null), (String) v.getTag()));
         
         // create Intent to send Email
         Intent intent = new Intent(Intent.ACTION_SEND);
         intent.setType("text/plain");
         intent.putExtra(Intent.EXTRA_STREAM, data);
         startActivity(Intent.createChooser(intent,
            getResources().getString(R.string.emailPickerTitle)));
      } // end method onClick
   }; // end OnClickListener

   // deletes the specified recording
   OnClickListener deleteButtonListener = new OnClickListener() 
   {
      @Override
      public void onClick(final View v) 
      {
         // create an input dialog to get recording name from user
         AlertDialog.Builder confirmDialog = 
            new AlertDialog.Builder(SavedRecordings.this);
         confirmDialog.setTitle(R.string.dialog_confirm_title); 
         confirmDialog.setMessage(R.string.dialog_confirm_message); 

         confirmDialog.setPositiveButton(R.string.button_delete, 
            new DialogInterface.OnClickListener()
            { 
               public void onClick(DialogInterface dialog, int which) 
               {
                  File fileToDelete = new File(getExternalFilesDir(null)+
                     File.separator + (String) v.getTag()); 
                  fileToDelete.delete();
                  savedRecordingsAdapter.remove((String) v.getTag());
               } // end method onClick 
            } // end anonymous inner class
         ); // end call to setPositiveButton
         
         confirmDialog.setNegativeButton(R.string.button_cancel, null);
         confirmDialog.show();         
      } // end method onClick
   }; // end OnClickListener

   @Override
   protected void onListItemClick(ListView l, View v, int position, 
      long id) 
   {
      super.onListItemClick(l, v, position, id);
      playPauseButton.setChecked(true); // checked state
      handler.removeCallbacks(updater); // stop updating progressSeekBar
      
      // get the item that was clicked 
      TextView nameTextView = 
         ((TextView) v.findViewById(R.id.nameTextView));
      String name = nameTextView.getText().toString(); 
      
      // get path to file
      String filePath = getExternalFilesDir(location).getAbsolutePath() + 
         File.separator + name;
      
      // set nowPlayingTextView's text
      nowPlayingTextView.setText(getResources().getString(
         R.string.now_playing_prefix) + " " + name);
    
      try 
      {
         // set the MediaPlayer to play the file at filePath
         mediaPlayer.reset(); // reset the MediaPlayer
         mediaPlayer.setDataSource(filePath);
         mediaPlayer.prepare(); // prepare the MediaPlayer
         progressSeekBar.setMax(mediaPlayer.getDuration());
         progressSeekBar.setProgress(0);
         mediaPlayer.setOnCompletionListener(
            new OnCompletionListener()
            {
               @Override
               public void onCompletion(MediaPlayer mp)
               {
                  playPauseButton.setChecked(false); // unchecked state
                  mp.seekTo(0);
               } // end method onCompletion
            } // end OnCompletionListener
         ); // end call to setOnCompletionListener
         mediaPlayer.start();
         updater.run(); // start updating progressSeekBar
      } // end try
      catch (Exception e) 
      {
         Log.e(TAG, e.toString()); // log exceptions
      } // end catch
   } // end method onListItemClick   
   
   // reacts to events created when the Seekbar's thumb is moved
   OnSeekBarChangeListener progressChangeListener = 
      new OnSeekBarChangeListener() 
      {
         @Override
         public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) 
         {
            if (fromUser)
               mediaPlayer.seekTo(seekBar.getProgress());
         } // end method onProgressChanged 
   
         @Override
         public void onStartTrackingTouch(SeekBar seekBar) 
         {
         } // end method onStartTrackingTouch
   
         @Override
         public void onStopTrackingTouch(SeekBar seekBar) 
         {
         } // end method onStopTrackingTouch
      }; // end OnSeekBarChangeListener

   // updates the SeekBar every second
   Runnable updater = new Runnable() 
   {
      @Override
      public void run() 
      {
         if (mediaPlayer.isPlaying())
         {
            // update the SeekBar's position
            progressSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            handler.postDelayed(this, 100);
         } // end if
      } // end method run
   }; // end Runnable

   // called when the user touches the "Play" Button
   OnCheckedChangeListener playPauseButtonListener = 
      new OnCheckedChangeListener() 
      {
         // toggle play/pause
         @Override
         public void onCheckedChanged(CompoundButton buttonView,
            boolean isChecked)
         {
            if (isChecked)
            {
               mediaPlayer.start(); // start the MediaPlayer
               updater.run(); // start updating progress SeekBar
            }
            else
               mediaPlayer.pause(); // pause the MediaPlayer
         } // end method onCheckedChanged
      }; // end OnCheckedChangedListener
      @Override
      public void onCreateContextMenu(ContextMenu menu, View v,
                                      ContextMenuInfo menuInfo) {
          super.onCreateContextMenu(menu, v, menuInfo);
          MenuInflater inflater = getMenuInflater();
          inflater.inflate(R.menu.context_menu, menu);
      }
      @Override
      public boolean onContextItemSelected(MenuItem item) {
          AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
          switch (item.getItemId()) {
              case R.id.edit:
            	  i = (int) info.id;
            	  
            	  
            	  
            	  LayoutInflater inflater = (LayoutInflater) getSystemService(
          	            Context.LAYOUT_INFLATER_SERVICE);
            	  View v1 = inflater.inflate(R.layout.name_edittext, null);
     	         final EditText nameEditText = 
     	            new EditText(SavedRecordings.this);
     	         nameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
     	         
     	            
     	         // create an input dialog to get recording name from user
     	         new AlertDialog.Builder(SavedRecordings.this)
     	         .setTitle("Rename File")
     	         .setView(nameEditText)
     	         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
     	             public void onClick(DialogInterface dialog, int whichButton) {
     	                 String value = nameEditText.getText().toString().trim();
     	                 if(value.length() != 0){
     	                	String from = listView.getItemAtPosition(i).toString();
     	                	String to = value;
     	                	File ffrom = new File(SavedRecordings.this.getExternalFilesDir(location), from);
     	                	
     	                	File fto = new File(SavedRecordings.this.getExternalFilesDir(location), value + ".3gp");
     	                	
     	                	ffrom.renameTo(fto);
     	                	//fto.delete();
     	                	savedRecordingsAdapter = new SavedRecordingsAdapter(SavedRecordings.this, 
     	                	         new ArrayList<String>(
     	                	            Arrays.asList(SavedRecordings.this.getExternalFilesDir(location).list())));
     	                	      listView.setAdapter(savedRecordingsAdapter);
     	                 }else{
     	                	 
     	                 }
     	             }
     	         }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
     	             public void onClick(DialogInterface dialog, int whichButton) {
     	                 // Do nothing.
     	             }
     	         }).show();
            	  
            	  
            	  
                  
                  return true;
              case R.id.delete:
                  String temp = listView.getItemAtPosition((int) info.id).toString();
                  savedRecordingsAdapter.remove(temp);
                  savedRecordingsAdapter.notifyDataSetChanged();
                  File f = new File(SavedRecordings.this.getExternalFilesDir(location), temp);
                  boolean h = f.delete();
                  if (h){ Log.d("WHAT", "deleted");}
                  return true;
              case R.id.move:
            	  i = (int) info.id;
            	  
            	  ArrayList<String> values2 = new ArrayList<String>(
                          Arrays.asList(SavedRecordings.this.getExternalFilesDir(null).list()));
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
                           if(temp.equals(location)){
                        	   
                           }else if (temp.equals("Create New Category")){
                        	   
                        	   
                        	   
                        	   LayoutInflater inflater = (LayoutInflater) getSystemService(
                         	            Context.LAYOUT_INFLATER_SERVICE);
                           	  View v1 = inflater.inflate(R.layout.name_edittext, null);
                    	         final EditText nameEditText = 
                    	            new EditText(SavedRecordings.this);
                    	         nameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                    	         
                    	            
                    	         // create an input dialog to get recording name from user
                    	         new AlertDialog.Builder(SavedRecordings.this)
                    	         .setTitle("Create New Category")
                    	         .setView(nameEditText)
                    	         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    	             public void onClick(DialogInterface dialog, int whichButton) {
                    	                 String value = nameEditText.getText().toString().trim();
                    	                 if(value.length() != 0){
                    	                	 
                    	                	 String temperino = listView.getItemAtPosition(i).toString();
                                             String source2 = SavedRecordings.this.getExternalFilesDir(location)+"/"+ temperino;
                                             String target2 = SavedRecordings.this.getExternalFilesDir(value) +"/"+ temperino;
                                             try {
                     							InputStream in2 = new FileInputStream(source2);
                     							OutputStream out2 = new FileOutputStream(target2);
                     						
                                                byte[] buf = new byte[1024];
                                                int len;
                                                 
                                                while ((len = in2.read(buf)) > 0) {
                                                    out2.write(buf, 0, len);
                                                }
                                                 
                                                in2.close();
                                                out2.close();
                                                
                                                }catch (NullPointerException e) {
                                                    e.printStackTrace();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                savedRecordingsAdapter.remove(temperino);
                                                savedRecordingsAdapter.notifyDataSetChanged();
                                                File deleteFile = new File(SavedRecordings.this.getExternalFilesDir(location), temperino);
                                                deleteFile.delete();
                    	                	 
                    	                	 
                    	                	 
                    	                	 
                    	                 }else{
                    	                	 
                    	                 }
                    	             }
                    	         }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    	             public void onClick(DialogInterface dialog, int whichButton) {
                    	                 // Do nothing.
                    	             }
                    	         }).show();
                        	   
                        	   
                        	   
                        	   
                        	   
                           }else{
                           String temp2 = listView.getItemAtPosition(i).toString();
                           String source = SavedRecordings.this.getExternalFilesDir(location)+"/"+ temp2;
                           String target = SavedRecordings.this.getExternalFilesDir(temp) +"/"+ temp2;
                           try {
							InputStream in = new FileInputStream(source);
							OutputStream out = new FileOutputStream(target);
						
                           byte[] buf = new byte[1024];
                           int len;
                            
                           while ((len = in.read(buf)) > 0) {
                               out.write(buf, 0, len);
                           }
                            
                           in.close();
                           out.close();
                           
                           }catch (NullPointerException e) {
                               e.printStackTrace();
                           } catch (Exception e) {
                               e.printStackTrace();
                           }
                           savedRecordingsAdapter.remove(temp2);
                           savedRecordingsAdapter.notifyDataSetChanged();
                           File deleteFile = new File(SavedRecordings.this.getExternalFilesDir(location), temp2);
                           deleteFile.delete();
                           }
                      }
                  });
                  AlertDialog alert = builder.create();
                  alert.show();
                  
          	         
          	         
          	         
          	         
          	         
            	  
              	  return true;
              default:
                  return super.onContextItemSelected(item);
          }
      }

	private Dialog getActivity() {
		// TODO Auto-generated method stub
		return null;
	}
} // end class SavedRecordings

