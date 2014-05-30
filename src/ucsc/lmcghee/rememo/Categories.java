package ucsc.lmcghee.rememo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class Categories extends Activity {
    ListView listView ;
    ArrayAdapter<String> adapter;
    String create;
    String[] values;
    ArrayList<String> values2;
    int i;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_screen);
        
        //Set volume control
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        

    	}
    @Override
    protected void onResume(){
    	super.onResume();
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.listview2);
        values2 = new ArrayList<String>(
                Arrays.asList(getExternalFilesDir(null).list()));
        create = "Create New Category";
        values2.add(create);
        adapter = new ArrayAdapter<String>(this, R.layout.saved_recordings_row, R.id.nameTextView, values2);
        listView.setAdapter(adapter); 
        listView.setOnItemClickListener(new OnItemClickListener() {

              @Override
              public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {
               int itemPosition     = position;
               String  itemValue    = (String) listView.getItemAtPosition(position);
               
               if(itemValue.equals(create)){
            	   LayoutInflater inflater = (LayoutInflater) getSystemService(
            	            Context.LAYOUT_INFLATER_SERVICE);
            	   
            	         // inflate name_edittext.xml to create an EditText
            	         View v1 = inflater.inflate(R.layout.name_edittext, null);
            	         final EditText nameEditText = 
            	            new EditText(Categories.this);
            	         nameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            	         
            	            
            	         // create an input dialog to get recording name from user
            	         new AlertDialog.Builder(Categories.this)
            	         .setTitle("New Category")
            	         .setView(nameEditText)
            	         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            	             public void onClick(DialogInterface dialog, int whichButton) {
            	                 String value = nameEditText.getText().toString().trim();
            	                 if(value.length() != 0){
            	                	 createDir(value);
            	                	 values2.clear();
            	                	 values2 = new ArrayList<String>(
            	                             Arrays.asList(Categories.this.getExternalFilesDir(null).list()));
            	                	 values2.add(create);
            	                     adapter = new ArrayAdapter<String>(Categories.this, R.layout.saved_recordings_row, R.id.nameTextView, values2);
            	                     listView.setAdapter(adapter); 
            	                 }else{
            	                	
            	                 }
            	             }
            	         }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            	             public void onClick(DialogInterface dialog, int whichButton) {
            	                 // Do nothing.
            	             }
            	         }).show();
            	         
            	         
               }else{
               Intent intent = new Intent(Categories.this, SavedRecordings.class);
               intent.putExtra("KEY", itemValue);
               startActivity(intent);
               }
              }
         });
        registerForContextMenu(listView);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
super.onCreateContextMenu(menu, v, menuInfo);
MenuInflater inflater = getMenuInflater();
inflater.inflate(R.menu.categories_menu, menu);
}
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.editC:
            	
            	String temp3 = listView.getItemAtPosition((int) info.id).toString();
            	if(!temp3.equals("Create New Category")){
            	
            	
            	
            	i = (int) info.id;
          	  
          	  
          	  
          	  LayoutInflater inflater = (LayoutInflater) getSystemService(
        	            Context.LAYOUT_INFLATER_SERVICE);
          	  View v1 = inflater.inflate(R.layout.name_edittext, null);
   	         final EditText nameEditText = 
   	            new EditText(Categories.this);
   	         nameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
   	         
   	            
   	         // create an input dialog to get recording name from user
   	         new AlertDialog.Builder(Categories.this)
   	         .setTitle("Rename Category")
   	         .setView(nameEditText)
   	         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
   	             public void onClick(DialogInterface dialog, int whichButton) {
   	                 String value = nameEditText.getText().toString().trim();
   	                 if(value.length() != 0){
   	                	String from = listView.getItemAtPosition(i).toString();
   	                	String to = value;
   	                	File ffrom = new File(Categories.this.getExternalFilesDir(null), from);
   	                	
   	                	File fto = new File(Categories.this.getExternalFilesDir(null), value);
   	                	
   	                	ffrom.renameTo(fto);
   	                	//fto.delete();
   	                	values2 = new ArrayList<String>(
   	                         Arrays.asList(getExternalFilesDir(null).list()));
   	                 create = "Create New Category";
   	                 values2.add(create);
   	                 adapter = new ArrayAdapter<String>(Categories.this, R.layout.saved_recordings_row, R.id.nameTextView, values2);
   	                 listView.setAdapter(adapter); 
   	                 }else{
   	                	 
   	                 }
   	             }
   	         }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
   	             public void onClick(DialogInterface dialog, int whichButton) {
   	                 // Do nothing.
   	             }
   	         }).show();
            	}
            	
            	
            	
            	
            	
            	return true;
            case R.id.deleteC:
            	i = (int) info.id;
            	String temp = listView.getItemAtPosition((int) info.id).toString();
            	if(!temp.equals("Create New Category")){
                adapter.remove(temp);
                adapter.notifyDataSetChanged();
                File f = new File(Categories.this.getExternalFilesDir(null), temp);
                if (f.isDirectory()) {
                	Log.d("WHAT","is directory");
                    String[] children = f.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(f, children[i]).delete();
                    }
                }
                f.delete();
            	}
            	return true;
            default:
                return super.onContextItemSelected(item);
        }
        
    }
    private void createDir(String s){
		File direct =  getExternalFilesDir(s);
			//direct.mkdir();
		}
    }

