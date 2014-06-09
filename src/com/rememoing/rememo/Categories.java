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
//You're going to love and hate this one at the same time
public class Categories extends Activity {
	ListView listView;
	static ArrayAdapter<String> adapter;
	String create;
	static ArrayList<String> values;
	static ArrayList<String> values2;
	static Context context;
	int i;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.categories_screen);
		context = Categories.this;
		VoiceRecorder.initiate2();//Lets voice recorder know its up and running
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Get ListView object from xml
		listView = (ListView) findViewById(R.id.listview2);
		values = new ArrayList<String>();
		values2 = new ArrayList<String>(Arrays.asList(getExternalFilesDir(null)
				.list()));//All the directories in getExternalFilesDirNull
		Collections.sort(values2, String.CASE_INSENSITIVE_ORDER);
		create = "Create New Category";//String we use to remember "Create new Category"
		for (String str : values2) {
			int i = 0;
			File f = getExternalFilesDir(str);
			if (f.isDirectory()) {
				String[] children = f.list();
				for (i = 0; i < children.length; i++) {
				}
				values.add(str + " (" + Integer.toString(i) + ")");
				//SINCE WE NEED ONE WITH THE NUMBER OF ITEMS NEXT TO IT, we create another one with the right values
			}
		}
		Log.d("WHAT", values.toString());
		values2.add(create);
		values.add(create);//Add create to the end of each one
		adapter = new ArrayAdapter<String>(this, R.layout.saved_recordings_row,
				R.id.nameTextView, values);
		//Inflate the ones with the values we want
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//What to do on itemClick
				int itemPosition = position;
				String itemValue = values2.get(position);
				//Get the name and position
				if (itemValue.equals(create)) {
					LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					//If it was create a new category
					// inflate name_edittext.xml to create an EditText
					View v1 = inflater.inflate(R.layout.name_edittext, null);
					final EditText nameEditText = new EditText(Categories.this);
					nameEditText
							.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
							.toggleSoftInput(InputMethodManager.SHOW_FORCED,
									InputMethodManager.HIDE_IMPLICIT_ONLY);
					// create an input dialog to get recording name from user
					new AlertDialog.Builder(Categories.this)
							.setTitle("New Category")
							.setView(nameEditText)
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											String value = nameEditText
													.getText().toString()
													.trim();
											//Get the name they input
											if (value.length() != 0) {
												((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
														.hideSoftInputFromWindow(
																nameEditText
																		.getWindowToken(),
																0);
												createDir(value);
												values2.clear();
												values.clear();
												values2 = new ArrayList<String>(
														Arrays.asList(Categories.this
																.getExternalFilesDir(
																		null)
																.list()));
												Collections
														.sort(values2,
																String.CASE_INSENSITIVE_ORDER);
												for (String str : values2) {
													int i = 0;
													File f = getExternalFilesDir(str);
													if (f.isDirectory()) {
														String[] children = f
																.list();
														for (i = 0; i < children.length; i++) {
														}
														values.add(str
																+ " ("
																+ Integer
																		.toString(i)
																+ ")");
													}
												}
												values2.add(create);
												values.add(create);
												//WE HAVE TO NOW UPDATE BOTH LISTS
												adapter = new ArrayAdapter<String>(
														Categories.this,
														R.layout.saved_recordings_row,
														R.id.nameTextView,
														values);
												listView.setAdapter(adapter);
												//Reset the adapter
											} else {
												((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
														.hideSoftInputFromWindow(
																nameEditText
																		.getWindowToken(),
																0);//Just hide the keyboard
											}
										}
									})
							.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
													nameEditText
															.getWindowToken(),
													0);
											// Do nothing.
										}
									}).show();

				} else {
					Intent intent = new Intent(Categories.this,
							SavedRecordings.class);
					intent.putExtra("KEY", itemValue);
					startActivity(intent);
					//If they just clicked on an item then start the intent 
				}
			}
		});
		registerForContextMenu(listView);//Important
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		//Define the context menu
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.categories_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		//What to do on each one
		case R.id.editC:

			String temp3 = listView.getItemAtPosition((int) info.id).toString();
			if (!temp3.equals("Create New Category")) {

				i = (int) info.id;

				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v1 = inflater.inflate(R.layout.name_edittext, null);
				final EditText nameEditText = new EditText(Categories.this);
				nameEditText
						.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
						.toggleSoftInput(InputMethodManager.SHOW_FORCED,
								InputMethodManager.HIDE_IMPLICIT_ONLY);

				// create an input dialog to get recording name from user
				new AlertDialog.Builder(Categories.this)
						.setTitle("Rename Category")
						.setView(nameEditText)
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
												nameEditText.getWindowToken(),
												0);
										String value = nameEditText.getText()
												.toString().trim(); //Get the new text AGAIN
										if (value.length() != 0) {
											String from = values2.get(i);
											String to = value;
											File ffrom = new File(
													Categories.this
															.getExternalFilesDir(null),
													from);

											File fto = new File(Categories.this
													.getExternalFilesDir(null),
													value);

											ffrom.renameTo(fto);
											//Rename the file
											// fto.delete();
											values.clear();
											values2 = new ArrayList<String>(
													Arrays.asList(getExternalFilesDir(
															null).list()));
											Collections
													.sort(values2,
															String.CASE_INSENSITIVE_ORDER);
											create = "Create New Category";
											for (String str : values2) {

												int i = 0;
												File f = getExternalFilesDir(str);
												if (f.isDirectory()) {
													String[] children = f
															.list();
													for (i = 0; i < children.length; i++) {
													}
													values.add(str
															+ " ("
															+ Integer
																	.toString(i)
															+ ")");
												}
											} //Do the thing again because programming is hard
											values2.add(create);
											values.add(create); //Add creates
											adapter = new ArrayAdapter<String>(
													Categories.this,
													R.layout.saved_recordings_row,
													R.id.nameTextView, values);
											listView.setAdapter(adapter);
											//Finally reset the adapter
										} else {
											((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
													nameEditText
															.getWindowToken(),
													0);
										}
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										// Do nothing.
										((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
												nameEditText.getWindowToken(),
												0);
									}
								}).show();
			}

			return true;
		case R.id.deleteC:
			//if delete
			i = (int) info.id;

			String temp = values2.get((int) info.id);
			String temp2 = listView.getItemAtPosition(i).toString();
			if (!temp.equals("Create New Category")) {
				//Do nothing if it was Create New Category
				adapter.remove(temp2);
				adapter.notifyDataSetChanged();
				File f = new File(Categories.this.getExternalFilesDir(null),
						temp);
				if (f.isDirectory()) {
					//Delete all the Children
					String[] children = f.list();
					for (int i = 0; i < children.length; i++) {
						new File(f, children[i]).delete();
					}
				}
				values2.remove(i);//Remove and delete from the list
				f.delete();
			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}

	private void createDir(String s) {
		File direct = getExternalFilesDir(s);//Creating Directories is EZ
	}

	public static void refresh() {
		//If called on to refresh from the notification we just reset the adapter to accurately represent how many files are present
		int j = 0;
		for (String s : values2) {
			if (s.equals("New Memos")) {
				File f = new File(context.getExternalFilesDir(null),
						"New Memos");
				String[] children = f.list();
				int i;
				for (i = 0; i < children.length; i++) {
				}

				values.remove(j);
				values.add(j, s + " (" + Integer.toString(i) + ")");
				adapter.notifyDataSetChanged();
				return;
			}
			j++;
		}
		values2.add(values2.size() - 1, "New Memos");
		values.add(values.size() - 1, "New Memos (1)");
		adapter.notifyDataSetChanged();
	}

}
