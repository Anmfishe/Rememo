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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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
    ArrayList<String> values;
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
        values = new ArrayList<String>();
        values2 = new ArrayList<String>(
                Arrays.asList(getExternalFilesDir(null).list()));
        Collections.sort(values2, String.CASE_INSENSITIVE_ORDER);
        create = "Create New Category";
        for(String str : values2){
        	int i = 0;
        	File f = getExternalFilesDir(str);
        	if(f.isDirectory()){
        		String[] children = f.list();
                for (i = 0; i < children.length; i++){}
                values.add(str + " (" + Integer.toString(i)+")");
        	}
        }
        Log.d("WHAT",values.toString());
        values2.add(create);
        values.add(create);
        //adapter = new MyRecordingsAdapter(this, values2, values);
        adapter = new ArrayAdapter<String>(this, R.layout.saved_recordings_row, R.id.nameTextView, values);

        listView.setAdapter(adapter); 
        listView.setOnItemClickListener(new OnItemClickListener() {

              @Override
              public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {
               int itemPosition     = position;
               String  itemValue    = values2.get(position);
            		   //(String) listView.getItemAtPosition(position);
               
               
               if(itemValue.equals(create)){
            	   LayoutInflater inflater = (LayoutInflater) getSystemService(
            	            Context.LAYOUT_INFLATER_SERVICE);
            	   
            	         // inflate name_edittext.xml to create an EditText
            	         View v1 = inflater.inflate(R.layout.name_edittext, null);
            	         final EditText nameEditText = 
            	            new EditText(Categories.this);
            	         nameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

            	         
            	         
            	         ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            	         // create an input dialog to get recording name from user
            	         new AlertDialog.Builder(Categories.this)
            	         .setTitle("New Category")
            	         .setView(nameEditText)
            	         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            	             public void onClick(DialogInterface dialog, int whichButton) {
            	                 String value = nameEditText.getText().toString().trim();
            	                 if(value.length() != 0){
            	                	 ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
            	                	 createDir(value);
            	                	 values2.clear();
            	                	 values.clear();
            	                	 values2 = new ArrayList<String>(
            	                             Arrays.asList(Categories.this.getExternalFilesDir(null).list()));
            	                	 Collections.sort(values2, String.CASE_INSENSITIVE_ORDER);
            	                	 for(String str : values2){
            	                     	int i = 0;
            	                     	File f = getExternalFilesDir(str);
            	                     	if(f.isDirectory()){
            	                     		String[] children = f.list();
            	                             for (i = 0; i < children.length; i++){}
            	                             values.add(str + " (" + Integer.toString(i)+")");
            	                     	}
            	                     }
            	                     Log.d("WHAT",values.toString());
            	                     values2.add(create);
            	                     values.add(create);
            	                	 
            	                     adapter = new ArrayAdapter<String>(Categories.this, R.layout.saved_recordings_row, R.id.nameTextView, values);
            	                     listView.setAdapter(adapter); 
            	                 }else{
            	                	 ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
            	                	
            	                 }
            	             }
            	         }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            	             public void onClick(DialogInterface dialog, int whichButton) {
            	            	 ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
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
   	      ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
   	         
   	            
   	         // create an input dialog to get recording name from user
   	         new AlertDialog.Builder(Categories.this)
   	         .setTitle("Rename Category")
   	         .setView(nameEditText)
   	         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
   	             public void onClick(DialogInterface dialog, int whichButton) {
   	            	((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
   	                 String value = nameEditText.getText().toString().trim();
   	                 if(value.length() != 0){
   	                	String from = values2.get(i);
   	                	String to = value;
   	                	File ffrom = new File(Categories.this.getExternalFilesDir(null), from);
   	                	
   	                	File fto = new File(Categories.this.getExternalFilesDir(null), value);
   	                	
   	                	ffrom.renameTo(fto);
   	                	//fto.delete();
   	                	values.clear();
   	                	values2 = new ArrayList<String>(
   	                         Arrays.asList(getExternalFilesDir(null).list()));
   	                	Collections.sort(values2, String.CASE_INSENSITIVE_ORDER);
   	                	create = "Create New Category";
   	                	for(String str : values2){
   	                		
   	                			int i = 0;
   	                			File f = getExternalFilesDir(str);
   	                			if(f.isDirectory()){
   	                				String[] children = f.list();
   	                				for (i = 0; i < children.length; i++){}
   	                				values.add(str + " (" + Integer.toString(i)+")");
   	                			}
   	                	}
   	                 values2.add(create);
   	                 values.add(create);
   	                 adapter = new ArrayAdapter<String>(Categories.this, R.layout.saved_recordings_row, R.id.nameTextView, values);
   	                 listView.setAdapter(adapter); 
   	                 }else{
   	                	((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
   	                 }
   	             }
   	         }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
   	             public void onClick(DialogInterface dialog, int whichButton) {
   	                 // Do nothing.
   	            	((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
   	             }
   	         }).show();
            	}
            	
            	
            	
            	
            	
            	return true;
            case R.id.deleteC:
            	i= (int) info.id;
            	
            	String temp = values2.get((int) info.id);
            	String temp2 = listView.getItemAtPosition(i).toString();
            	if(!temp.equals("Create New Category")){
                adapter.remove(temp2);
                adapter.notifyDataSetChanged();
                File f = new File(Categories.this.getExternalFilesDir(null), temp);
                if (f.isDirectory()) {
                	Log.d("WHAT","is directory");
                    String[] children = f.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(f, children[i]).delete();
                    }
                }
                values2.remove(i);
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

