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
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class Categories extends Activity {
    ListView listView ;
    ArrayAdapter<String> adapter;
    String create;
    String[] values;
    ArrayList<String> values2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.categories_screen);
        
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.listview);

        
        // Defined Array values to show in ListView
        values = getExternalFilesDir(null).list();
        values2 = new ArrayList<String>(
                Arrays.asList(getExternalFilesDir(null).list()));
        create = "Create New Category";
        values2.add(create);
        values = values2.toArray(new String[0]);
        

        
    	
       

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        adapter = new ArrayAdapter<String>(this, R.layout.saved_recordings_row, R.id.nameTextView, values2);



        // Assign adapter to ListView
        listView.setAdapter(adapter); 

        
        // ListView Item Click Listener

        listView.setOnItemClickListener(new OnItemClickListener() {

              @Override
              public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {
                
               // ListView Clicked item index
               int itemPosition     = position;
               
               // ListView Clicked item value
               String  itemValue    = (String) listView.getItemAtPosition(position);
               Log.d("WHAT", itemValue);
               if(itemValue.equals(create)){
            	   values[position] = "New Category Here!";
            	   values2.clear();
            	   values2.addAll(Arrays.asList(values));
            	   values2.add(create);
            	   values = values2.toArray(new String[0]);
            	   adapter = new ArrayAdapter<String>(Categories.this, R.layout.saved_recordings_row, R.id.nameTextView, values2);
                   listView.setAdapter(adapter);
               }else{
               Intent intent = new Intent(Categories.this, SavedRecordings.class);
               intent.putExtra("KEY", itemValue);
               startActivity(intent);
               }
                // Show Alert
             
              }
				
			

         }); 
    }

}