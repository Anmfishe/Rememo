package ucsc.lmcghee.rememo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.IntentService;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SavedRecordings extends ListActivity {
	//ALOT OF THE PLAYBACK WAS FROM AN ONLINE TUTORIAL
	private static final String TAG = SavedRecordings.class.getName();

	// SavedRecordingsAdapter displays list of saved recordings in ListView
	private static SavedRecordingsAdapter savedRecordingsAdapter;

	private MediaPlayer mediaPlayer; // plays saved recordings
	private SeekBar progressSeekBar; // controls audio playback
	private Handler handler; // updates the SeekBar thumb position
	private TextView nowPlayingTextView; // displays audio name
	private ToggleButton playPauseButton; // displays audio name
	public static ListView listView; //The lv we use

	ListView lv; //Like an extra one because reasons
	Bundle extra; //How we get the string from the last activity
	static String location = null; //location
	int i;
	static int bID;//ID of the notifications we make
	int day, month, hour, mMinute, mYear;//The day month and year shtuff

	static Context context;

	// called when the activity is first created
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.saved_recordings);
		extra = getIntent().getExtras();
		context = SavedRecordings.this;
		if (extra != null) {
			location = extra.getString("KEY");

		}//Get the file location

		listView = getListView();
		savedRecordingsAdapter = new SavedRecordingsAdapter(this,
				new ArrayList<String>(Arrays.asList(getExternalFilesDir(
						location).list())));//Now we are looking in getExternalFilesDir(location)
		listView.setAdapter(savedRecordingsAdapter);
		registerForContextMenu(listView);

		handler = new Handler(); // updates SeekBar thumb position

		progressSeekBar = (SeekBar) findViewById(R.id.progressSeekBar);//Set the button
		progressSeekBar.setOnSeekBarChangeListener(progressChangeListener);
		playPauseButton = (ToggleButton) findViewById(R.id.playPauseButton);
		playPauseButton.setOnCheckedChangeListener(playPauseButtonListener);
		nowPlayingTextView = (TextView) findViewById(R.id.nowPlayingTextView);

		VoiceRecorder.initiate();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		bID = 1; //one because 0 is our main notification

	} // end method onCreate

	// create the MediaPlayer object
	@Override
	protected void onResume() {
		super.onResume();
		mediaPlayer = new MediaPlayer(); // plays recordings
	} // end method onResume

	// release the MediaPlayer object
	@Override
	protected void onPause() {

		if (mediaPlayer != null) {
			handler.removeCallbacks(updater); // stop updating GUI
			mediaPlayer.stop(); // stop audio playback
			mediaPlayer.release(); // release MediaPlayer resources
			mediaPlayer = null;
		}
		super.onPause();
	} // end method onPause

	private static class ViewHolder {
		TextView nameTextView;
	} // end class ViewHolder

	// ArrayAdapter displaying recording names and delete buttons
	private class SavedRecordingsAdapter extends ArrayAdapter<String> {
		private List<String> items; // list of file names
		private LayoutInflater inflater;

		public SavedRecordingsAdapter(Context context, List<String> items) {
			super(context, -1, items); // -1 indicates we're customizing view
			Collections.sort(items, String.CASE_INSENSITIVE_ORDER);
			this.items = items;
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		} // end SavedRecordingsAdapter constructor

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder; // holds references to current item's GUI

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.saved_recordings_row,
						null);

				// set up ViewHolder for this ListView item
				viewHolder = new ViewHolder();
				viewHolder.nameTextView = (TextView) convertView
						.findViewById(R.id.nameTextView);
				convertView.setTag(viewHolder); // store as View's tag
			} // end if
			else
				// get the ViewHolder from the convertView's tag
				viewHolder = (ViewHolder) convertView.getTag();

			// get and display name of recording file
			String item = items.get(position);
			viewHolder.nameTextView.setText(item);
			viewHolder.nameTextView.setTextColor(getResources().getColor(
					R.color.purp));

			return convertView;
		} // end method getView
	} // end class SavedRecordingsAdapter


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//What to do onClick
		playPauseButton.setChecked(true); // checked state
		handler.removeCallbacks(updater); // stop updating progressSeekBar

		// get the item that was clicked
		TextView nameTextView = ((TextView) v.findViewById(R.id.nameTextView));
		String name = nameTextView.getText().toString();

		// get path to file
		String filePath = getExternalFilesDir(location).getAbsolutePath()
				+ File.separator + name;

		// set nowPlayingTextView's text
		nowPlayingTextView.setText(getResources().getString(
				R.string.now_playing_prefix)
				+ " " + name);

		try {
			// set the MediaPlayer to play the file at filePath
			mediaPlayer.reset(); // reset the MediaPlayer
			mediaPlayer.setDataSource(filePath);
			mediaPlayer.prepare(); // prepare the MediaPlayer
			progressSeekBar.setMax(mediaPlayer.getDuration());
			progressSeekBar.setProgress(0);
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					playPauseButton.setChecked(false); // unchecked state
					mp.seekTo(0);
				} // end method onCompletion
			} // end OnCompletionListener
					); // end call to setOnCompletionListener
			mediaPlayer.start();
			updater.run(); // start updating progressSeekBar
		} // end try
		catch (Exception e) {
			Log.e(TAG, e.toString()); // log exceptions
		} // end catch
	} // end method onListItemClick

	// reacts to events created when the Seekbar's thumb is moved
	OnSeekBarChangeListener progressChangeListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser)
				mediaPlayer.seekTo(seekBar.getProgress());
		} // end method onProgressChanged

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		} // end method onStartTrackingTouch

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		} // end method onStopTrackingTouch
	}; // end OnSeekBarChangeListener

	// updates the SeekBar every second
	Runnable updater = new Runnable() {
		@Override
		public void run() {
			if (mediaPlayer.isPlaying()) {
				// update the SeekBar's position
				progressSeekBar.setProgress(mediaPlayer.getCurrentPosition());
				handler.postDelayed(this, 100);
			} // end if
		} // end method run
	}; // end Runnable

	// called when the user touches the "Play" Button
	OnCheckedChangeListener playPauseButtonListener = new OnCheckedChangeListener() {
		// toggle play/pause
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked) {
				mediaPlayer.start(); // start the MediaPlayer
				updater.run(); // start updating progress SeekBar

			} else {
				mediaPlayer.pause(); // pause the MediaPlayer

			}
		} // end method onCheckedChanged
	}; // end OnCheckedChangedListener

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		//Create our context menu
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.edit:
			i = (int) info.id;
			//If edit
			final EditText nameEditText = new EditText(SavedRecordings.this);
			nameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);//Inflate a new textView
			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
					.toggleSoftInput(InputMethodManager.SHOW_FORCED,
							InputMethodManager.HIDE_IMPLICIT_ONLY);

			// create an input dialog to get recording name from user
			new AlertDialog.Builder(SavedRecordings.this)
					.setTitle("Rename File")
					.setView(nameEditText)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
											.hideSoftInputFromWindow(
													nameEditText
															.getWindowToken(),
													0);
									String value = nameEditText.getText()
											.toString().trim();
									//Get the string
									if (value.length() != 0) {
										String from = listView
												.getItemAtPosition(i)
												.toString();
										String to = value;
										//Original File
										File ffrom = new File(
												SavedRecordings.this
														.getExternalFilesDir(location),
												from);
										//Rename shtuff
										File fto = new File(
												SavedRecordings.this
														.getExternalFilesDir(location),
												value + ".amr");
										//New file
										ffrom.renameTo(fto);
										// fto.delete();
										savedRecordingsAdapter = new SavedRecordingsAdapter(
												SavedRecordings.this,
												new ArrayList<String>(
														Arrays.asList(SavedRecordings.this
																.getExternalFilesDir(
																		location)
																.list())));
										listView.setAdapter(savedRecordingsAdapter);
										//Reset the Adapter
									} else {

									}
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
											.hideSoftInputFromWindow(
													nameEditText
															.getWindowToken(),
													0);
									// Do nothing.
								}
							}).show();

			return true;
		case R.id.delete:
			String temp = listView.getItemAtPosition((int) info.id).toString();
			savedRecordingsAdapter.remove(temp);
			savedRecordingsAdapter.notifyDataSetChanged();//Notify change
			File f = new File(
					SavedRecordings.this.getExternalFilesDir(location), temp);
			boolean h = f.delete();//Delete it
			return true;
		case R.id.move:
			//if Move
			i = (int) info.id;

			ArrayList<String> values2 = new ArrayList<String>(
					Arrays.asList(SavedRecordings.this
							.getExternalFilesDir(null).list()));//Values of our categories screen
			String create = "Create New Category";
			values2.add(create);
			final CharSequence[] cs = values2.toArray(new CharSequence[values2
					.size()]);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Make your selection");//Inflate Categories screen
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Do nothing.
						}
					});
			builder.setItems(cs, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					String temp = (String) cs[item];
					if (temp.equals(location)) {

					} else if (temp.equals("Create New Category")) {
						//If create new inflate a textView and let them name it
						//Then save the file to that location
						LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						View v1 = inflater
								.inflate(R.layout.name_edittext, null);
						final EditText nameEditText = new EditText(
								SavedRecordings.this);
						nameEditText
								.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
								.toggleSoftInput(
										InputMethodManager.SHOW_FORCED,
										InputMethodManager.HIDE_IMPLICIT_ONLY);

						new AlertDialog.Builder(SavedRecordings.this)
								.setTitle("Create New Category")
								.setView(nameEditText)
								.setPositiveButton("Ok",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
														.hideSoftInputFromWindow(
																nameEditText
																		.getWindowToken(),
																0);
												String value = nameEditText
														.getText().toString()
														.trim();
												if (value.length() != 0) {
													//We have done this like 1000 times now
													String temperino = listView
															.getItemAtPosition(
																	i)
															.toString();
													String source2 = SavedRecordings.this
															.getExternalFilesDir(location)
															+ "/" + temperino;
													String target2 = SavedRecordings.this
															.getExternalFilesDir(value)
															+ "/" + temperino;
													try {
														//We have to do an in out stream because a rename does not work here
														InputStream in2 = new FileInputStream(
																source2);
														OutputStream out2 = new FileOutputStream(
																target2);

														byte[] buf = new byte[1024];
														int len;

														while ((len = in2
																.read(buf)) > 0) {
															//Write all the bytes
															out2.write(buf, 0,
																	len);
														}

														in2.close();
														out2.close();

													} catch (NullPointerException e) {
														e.printStackTrace();
													} catch (Exception e) {
														e.printStackTrace();
													}
													savedRecordingsAdapter
															.remove(temperino);
													savedRecordingsAdapter
															.notifyDataSetChanged();// Remove, notify, then delete
													File deleteFile = new File(
															SavedRecordings.this
																	.getExternalFilesDir(location),
															temperino);
													deleteFile.delete();

												} else {

												}
											}
										})
								.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
														.hideSoftInputFromWindow(
																nameEditText
																		.getWindowToken(),
																0);
												// Do nothing.
											}
										}).show();

					} else {
						String temp2 = listView.getItemAtPosition(i).toString();
						String source = SavedRecordings.this
								.getExternalFilesDir(location) + "/" + temp2;
						String target = SavedRecordings.this
								.getExternalFilesDir(temp) + "/" + temp2;
						//2 files locations
						try {
							InputStream in = new FileInputStream(source);
							OutputStream out = new FileOutputStream(target);
							//just move it to new file location

							byte[] buf = new byte[1024];
							int len;

							while ((len = in.read(buf)) > 0) {
								out.write(buf, 0, len);
							}

							in.close();
							out.close();
							//Rewrite the file again

						} catch (NullPointerException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						savedRecordingsAdapter.remove(temp2);
						savedRecordingsAdapter.notifyDataSetChanged();
						File deleteFile = new File(SavedRecordings.this
								.getExternalFilesDir(location), temp2);
						deleteFile.delete();
					}
				}
			});
			AlertDialog alert = builder.create();
			alert.show();

			return true;
		case R.id.share:
			int j = (int) info.id;
			String temp2 = listView.getItemAtPosition(j).toString();
			File f2 = new File(getExternalFilesDir(location) + "/" + temp2);
			Uri uri = Uri.fromFile(f2);//Our file
			Intent k = new Intent(android.content.Intent.ACTION_SEND);
			k.setType("audio/amr");
			k.putExtra(android.content.Intent.EXTRA_TEXT,
					"Recorded from Rememo!");//Extra text
			k.putExtra(Intent.EXTRA_STREAM, uri);
			startActivity(Intent.createChooser(k, "Share via"));//Start a share intent based on amr files
			//amr is the only file type I could find that could be texted :(
			return true;
		case R.id.alert:
			i = (int) info.id;
			String name = listView.getItemAtPosition(i).toString();
			startAlarm(name);//Call start alarm
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}



	public static void refresh(String s) {
		if (location.equals("New Memos")) {
			//Refresh when we finish recording from the notificaiton
			//Only applies to new memos
			savedRecordingsAdapter.clear();
			File f = new File(context.getExternalFilesDir(null), "New Memos");
			String[] children = f.list();
			int i;
			for (i = 0; i < children.length; i++) {
				savedRecordingsAdapter.add(children[i]);
			}
			savedRecordingsAdapter.notifyDataSetChanged();
		}

	}

	public void startAlarm(final String n) {
		//The great alarm setter
		Calendar c = Calendar.getInstance();
		//Make a new DatePickerDialog
		new DatePickerDialog(
				SavedRecordings.this,
				new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						//On Set, set all of the values to the right vales

						day = dayOfMonth;
						Log.d("TIMER", "Day:" + day);
						month = monthOfYear;
						Log.d("TIMER", "Month:" + month);
						mYear = year;
						Log.d("TIMER", "Year:" + mYear);
						Calendar c = Calendar.getInstance();
						//Then start a time Picker Dialog
						new TimePickerDialog(SavedRecordings.this,
								new TimePickerDialog.OnTimeSetListener() {

									@Override
									public void onTimeSet(TimePicker view,
											int hourOfDay, int minute) {
										//On Set set all the times to the right times
										// TODO Auto-generated method stub
										hour = hourOfDay;
										Log.d("TIMER", "Hour:" + hour);
										mMinute = minute;
										Log.d("TIMER", "Minute:" + mMinute);
										//Make alarm manager
										AlarmManager alarmManager = (AlarmManager) SavedRecordings.this
												.getSystemService(SavedRecordings.this.ALARM_SERVICE);
										Calendar calendar = Calendar
												.getInstance();
										calendar.set(mYear, month, day, hour, mMinute, 0);
										//Set a Calendar to the users input
										long when = calendar.getTimeInMillis();
										//Get the time of the clanedar

										Intent intent = new Intent(
												SavedRecordings.this,
												ReminderService.class);
										//make a new intent
										intent.putExtra("NAME", n);
										//save the name in the intent
										PendingIntent pendingIntent = PendingIntent
												.getBroadcast(
														SavedRecordings.this,
														bID, intent, 0);
										//Set up a broadcast
										alarmManager.set(AlarmManager.RTC,
												when, pendingIntent);
										bID++;
										Toast toast = Toast.makeText(SavedRecordings.this, "Alert Set",
												Toast.LENGTH_SHORT);
										toast.show();
										//Toast em

									}
								}, c.get(Calendar.HOUR_OF_DAY), c
										.get(Calendar.MINUTE), false).show();//Sets Current hour minute

					}
				}, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH)).show();//Sets current month year

	}

	public static class ReminderService extends BroadcastReceiver {
	

		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context context, Intent intent) {
			//The Receiver
			String name = "Name not found";
			//If for some reason the name was not attached
			Bundle bb = intent.getExtras();
			if(bb!=null){
				name = bb.getString("NAME");
			}
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			long when = System.currentTimeMillis(); // notification time
			Notification notification = new Notification(
					R.drawable.ic_launcher, "Rememo Reminder", when);
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.flags |= notification.FLAG_AUTO_CANCEL;
			Intent notificationIntent = new Intent(context,
					SavedRecordings.class);//Make a generic notification
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);
			notification.setLatestEventInfo(context.getApplicationContext(),
					name, "Click to see file",
					contentIntent);//Set the name and subText
			nm.notify(bID, notification);//Notify
		}

	}
} // end class SavedRecordings

