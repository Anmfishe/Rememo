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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
//This class does all the things on the main screen as well as handles the notification
public class VoiceRecorder extends Activity {
	private static final String TAG = VoiceRecorder.class.getName();
	private static MediaRecorder recorder; // used to record audio
	public static boolean recording; // are we currently recording
	public static Activity activity; //need a static activity
	public static File tmpFile; //used to make new correct path
	public static File newFile; //used to make output file
	public static int bitRate; //var to save quality
	boolean notificationOn; //see if notification is on
	static boolean initiated; //important for calling functions to other activities
	static boolean initiated2; //same
	Button bt; //references to out on screen buttons
	Button bt2;
	View v2; //View references we pass in to other functions
	View v3;
	static Context ctx; //our context reference
	LayoutInflater inflater; //to inflate things
	Boolean helper; //spaghetti
	static Notification notification; //our notification
	static NotificationManager nm; //the manager
	static RemoteViews contentView; //the notification view
	String name; //The name of a file we clicked on

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//initialize everything
		activity = this;
		bt = (Button) findViewById(R.id.recordButton);
		bt2 = (Button) findViewById(R.id.viewSaved);
		createNotification();
		notificationOn = true;
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		ctx = VoiceRecorder.this;
		bitRate = 96000;

	}

	// create the MediaRecorder
	@Override
	protected void onResume() {

		super.onResume();
	} // end method onResume

	@Override
	protected void onPause() {
		super.onPause();
		if (recorder != null) {
			recorder.release(); // release MediaRecorder resources
			recording = false; // we are no longer recording

			recorder = null;
			if (notificationOn) {
				//Change the notification to stopped
				final String text = ("Rememo");
				contentView.setTextViewText(R.id.textView, text);
				contentView.setTextColor(R.id.textView, getResources().getColor(R.color.blue));
				notification.contentView = contentView;
				nm.notify(0, notification);
			}
			newFile.delete();
			//Change the buttons to stopped
			TextView tv = (TextView) findViewById(R.id.statusText);
			tv.setText("Stopped");
			tv.setTextColor(getResources().getColor(R.color.blue));
			bt.setText(R.string.rec);
			bt.setTextColor(getResources()
					.getColor(R.color.white));
			bt2.setEnabled(true);
		}
	}

	public static void initiate() { //These help with seeing if an activity has been started yet
		initiated = true;
	}

	public static void initiate2() {
		initiated2 = true;
	}
	
	public void startStop(View v) { //Our start stop recording button

		v2 = v;
		if (recording) {
			//If we are, stop recording and set all the text back
			stopRecording(v);
			if (notificationOn) {
				final String text = ("Rememo");
				contentView.setTextViewText(R.id.textView, text);
				contentView.setTextColor(R.id.textView, getResources().getColor(R.color.blue));
				notification.contentView = contentView;
				nm.notify(0, notification);
			}
			TextView tv = (TextView) findViewById(R.id.statusText);
			tv.setText("Stopped");
			tv.setTextColor(getResources().getColor(R.color.blue));
			bt.setText(R.string.rec);
			bt.setTextColor(getResources()
					.getColor(
							R.color.white));
			bt2.setEnabled(true);
			

			//The edit text we inflate for the name
			final EditText nameEditText = new EditText(VoiceRecorder.this);
			nameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
					.toggleSoftInput(InputMethodManager.SHOW_FORCED,
							InputMethodManager.HIDE_IMPLICIT_ONLY);//forces keyboard to come up

			new AlertDialog.Builder(VoiceRecorder.this)//set the builder, define OK and Cancel buttons
					.setTitle("Name")
					.setView(nameEditText)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
											.hideSoftInputFromWindow(
													nameEditText
															.getWindowToken(),
													0); //this the keyboard on OK
									
									String value = nameEditText.getText()
											.toString().trim();
									//Get the name they input
									if (value.length() != 0) {
										//if not no response
										name = value;
										ArrayList<String> values2 = new ArrayList<String>(
												Arrays.asList(VoiceRecorder.this
														.getExternalFilesDir(
																null).list()));
										String create = "Create New Category";
										values2.add(create);
										final CharSequence[] cs = values2
												.toArray(new CharSequence[values2
														.size()]);
										//Save the name and inflate all the categories in getExternalFilesDir(null)
										AlertDialog.Builder builder = new AlertDialog.Builder(
												ctx);
										builder.setTitle("Make your selection");
										builder.setNegativeButton(
												"Cancel",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int whichButton) {

														newFile.delete();
														
													}
												});
										builder.setItems(
												cs,
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int item) {
														//The onclick for all the items in the next alert
														String temp = (String) cs[item];

														if (temp.equals("Create New Category")) {

															//If new category was choosen inflate one more editView to get a new category name
															final EditText nameEditText = new EditText(
																	VoiceRecorder.this);
															nameEditText
																	.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

															((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
																	.toggleSoftInput(
																			InputMethodManager.SHOW_FORCED,
																			InputMethodManager.HIDE_IMPLICIT_ONLY);


															new AlertDialog.Builder(
																	VoiceRecorder.this)
																	.setTitle(
																			"Create New Category")
																	.setView(
																			nameEditText)
																	.setPositiveButton(
																			"Ok",
																			new DialogInterface.OnClickListener() {
																				public void onClick(
																						DialogInterface dialog,
																						int whichButton) {
																					String value = nameEditText
																							.getText()
																							.toString()
																							.trim();
																					if (value
																							.length() != 0) {
																						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
																								.hideSoftInputFromWindow(
																										nameEditText
																												.getWindowToken(),
																										0);
																						//The tmp File is the actual representation of where we want to save
																						//newFile contains the actual sound file which has finished recording
																						tmpFile = new File(
																								getExternalFilesDir(
																										value)
																										.getAbsolutePath()
																										+ File.separator
																										+ name
																										+ ".amr");
																						newFile.renameTo(tmpFile);
																						

																					} else {
																						//if no input
																						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
																								.hideSoftInputFromWindow(
																										nameEditText
																												.getWindowToken(),
																										0);
																						newFile.delete();
																						

																					}
																				}
																			})
																	.setNegativeButton(
																			"Cancel",
																			new DialogInterface.OnClickListener() {
																			
																				public void onClick(
																						DialogInterface dialog,
																						int whichButton) {
																					// Just delete new file
																					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
																							.hideSoftInputFromWindow(
																									nameEditText
																											.getWindowToken(),
																									0);
																					newFile.delete();
																					
																				}
																			})
																	.show();
														} else {
															try {
																//If they just chose a category then save it there
																tmpFile = new File(
																		getExternalFilesDir(
																				temp)
																				.getAbsolutePath()
																				+ File.separator
																				+ name
																				+ ".amr");
																newFile.renameTo(tmpFile);
																
															} catch (IllegalStateException e) {
																Log.e(TAG,
																		e.toString());
															} // end catch

														}
													}
												}).show();

										
										bt2.setEnabled(true);
									} else {
										newFile.delete();
										
									}

								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									//Last negative from the first alert!
									((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
											.hideSoftInputFromWindow(
													nameEditText
															.getWindowToken(),
													0);
									newFile.delete();
									
								}
							}).show();

		}

		else {
			//If we are not recording, then we need to start recording
			if (notificationOn) {
				//Change the notification to be recording
				final String text = ("Rememoing");
				contentView.setTextViewText(R.id.textView, text);
				contentView.setTextColor(R.id.textView, Color.RED);
				notification.contentView = contentView;
				nm.notify(0, notification);
			}
			startRecording(v2);//sets up recorder
			bt.setText(R.string.stp);
			bt.setTextColor(getResources().getColor(R.color.red));
			bt2.setEnabled(false);
			try {
				newFile = new File(getExternalFilesDir("New Memos")
						.getAbsolutePath()
						+ File.separator
						+ getTime()
						+ ".amr");
				recorder.setOutputFile(newFile.getAbsolutePath());
				recorder.prepare(); // prepare to record
				recorder.start(); // start recording
				recording = true; // we are currently recording
				TextView tv = (TextView) findViewById(R.id.statusText);
				tv.setText("Recording");
				tv.setTextColor(getResources().getColor(R.color.red));//Change the rest of the buttons

			} catch (IllegalStateException e) {
				Log.e(TAG, e.toString());
			} // end catch
			catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}

	}

	public void startRecording(View v) { //Sets up the recorder
		Log.d("startRecording", "Start Button Pressed.");
		if (recorder == null)
			recorder = new MediaRecorder(); // create MediaRecorder
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		recorder.setAudioEncodingBitRate(bitRate);//at a certain bitrate
		recorder.setAudioSamplingRate(44100);

	}

	public void stopRecording(View v) { //Stop the recorder and reset it
		if (!recording)
			return;
		recorder.stop();
		recorder.reset();
		recording = false;
	}

	public static Boolean getRecording() {//getters and setters
		return recording;
	}

	public static void setRecoder(MediaRecorder rec) {
		recorder = rec;
	}

	public static void setRecording(Boolean rec) {
		recording = rec;
	}

	public static MediaRecorder getRecorder() {
		return recorder;
	}

	public static String getTime() {//The get time function for if we name it from the notification
		// Returns time in form of a string formatted as m-d hr:mn
		String time = "";
		Calendar myCalendar = Calendar.getInstance();
		time = Integer.toString(1 + myCalendar.get(Calendar.MONTH)) + "-"
				+ Integer.toString(myCalendar.get(Calendar.DAY_OF_MONTH)) + " "
				+ Integer.toString(myCalendar.get(Calendar.HOUR_OF_DAY));
		if (myCalendar.get(Calendar.MINUTE) > 9)
			time = time + ":"
					+ Integer.toString(myCalendar.get(Calendar.MINUTE));
		else
			time = time + ":0"
					+ Integer.toString(myCalendar.get(Calendar.MINUTE));

		if (myCalendar.get(Calendar.SECOND) > 9)
			time = time + ":"
					+ Integer.toString(myCalendar.get(Calendar.SECOND));
		else
			time = time + ":0"
					+ Integer.toString(myCalendar.get(Calendar.SECOND));

		return time;
	}

	public void viewSaved(View v) {//This does something I imagine
		Intent intent = new Intent(VoiceRecorder.this, Categories.class);
		startActivity(intent);
	}

	public void showNotificationClicked(View v) {//The create and delete for our notification
		if (notificationOn) {
			notificationOn = false;
			Button but = (Button) findViewById(R.id.notifbutton);
			but.setText("Create Notification");
			nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(0);
		} else {
			notificationOn = true;
			Button but = (Button) findViewById(R.id.notifbutton);
			but.setText("Delete Notification");
			createNotification();
		}
	}

	public void closeNotificationClicked(View v) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}

	public static void setNotRecording(Activity activity) {//Set things from notification function

		final String text = ("Rememo");
		contentView.setTextViewText(R.id.textView, text);
		contentView.setTextColor(R.id.textView, activity.getResources().getColor(R.color.blue) );
		notification.contentView = contentView;
		nm.notify(0, notification);//Set the content View 

		Button b2 = (Button) activity.findViewById(R.id.recordButton);
		b2.setText(R.string.rec);
		b2.setTextColor(activity.getResources().getColor(R.color.white));
		Button b3 = (Button) activity.findViewById(R.id.viewSaved);
		b3.setEnabled(true);
		TextView tv = (TextView) activity.findViewById(R.id.statusText);
		tv.setText("Stopped");//Set the buttons within this class
		
		tv.setTextColor(activity.getResources().getColor(R.color.blue));
		Toast toast = Toast.makeText(ctx, "Saved to New Memos",
				Toast.LENGTH_SHORT);
		toast.show();//Quick toast

	}

	public static void setRecording(Activity activity) {

		final String text = ("Rememoing");
		contentView.setTextViewText(R.id.textView, text);
		contentView.setTextColor(R.id.textView, Color.RED);
		notification.contentView = contentView;
		nm.notify(0, notification);//Start recording text
 
		Button b2 = (Button) activity.findViewById(R.id.recordButton);
		b2.setText(R.string.stp);
		b2.setTextColor(activity.getResources().getColor(R.color.red));
		Button b3 = (Button) activity.findViewById(R.id.viewSaved);
		b3.setEnabled(false);
		TextView tv = (TextView) activity.findViewById(R.id.statusText);
		tv.setText("Recording");
		tv.setTextColor(activity.getResources().getColor(R.color.red));//Set the buttons
	}

	@SuppressLint("NewApi")
	private void createNotification() {//Build the notification
		Notification.Builder builder = new Notification.Builder(this);

		// Create Intent to launch this Activity again if the notification is
		// clicked.
		Intent i = new Intent(this, VoiceRecorder.class);
		i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(this, 0, i, 0);
		builder.setContentIntent(intent);

		// Sets the ticker text
		builder.setTicker("Rememo On");

		// Sets the small icon for the ticker
		builder.setSmallIcon(R.drawable.abc_ic_ab_back_holo_dark);

		// Cancel the notification when clicked
		builder.setAutoCancel(true);

		// Build the notification
		notification = builder.build();

		// Inflate the notification layout as RemoteViews
		contentView = new RemoteViews(getPackageName(), R.layout.notification);

		// Set text on a TextView in the RemoteViews programmatically.

		final String text = ("Rememo");
		contentView.setTextViewText(R.id.textView, text);
		//This is how we set the button to listen
		Intent buttonIntent = new Intent(this, switchButtonListener.class);
		PendingIntent pbuttonIntent = PendingIntent.getBroadcast(this, 1,
				buttonIntent, 0);//use a broadcast
		contentView.setOnClickPendingIntent(R.id.imageButton1, pbuttonIntent);

		notification.contentView = contentView;
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.icon = R.drawable.ic_launcher;

		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.notify(0, notification);

	}

	public static class switchButtonListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			//Our receiver
			//Decides what to do depending on if we are recording

			if (recording) {

				setNotRecording(VoiceRecorder.activity);

				recorder.stop(); // stop recording
				recorder.reset(); // reset the MediaRecorder
				recording = false; // we are no longer recording
				tmpFile = new File(ctx.getExternalFilesDir("New Memos")
						.getAbsolutePath()
						+ File.separator
						+ getTime()
						+ ".amr");
				newFile.renameTo(tmpFile);
				
				

				if (initiated) {
					SavedRecordings.refresh(newFile.getName());
					//Referesh the other screens if they were initialized
				}
				if (initiated2) {
					Categories.refresh();
				}

			} else {

				
				setRecording(VoiceRecorder.activity);

				if (recorder == null)
					recorder = new MediaRecorder(); // create MediaRecorder
				recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
				recorder.setAudioEncodingBitRate(bitRate);
				recorder.setAudioSamplingRate(44100); //Start recording
				try {
					
					
					newFile = new File(ctx.getExternalFilesDir("New Memos")
							.getAbsolutePath()
							+ File.separator
							+ getTime()
							+ ".amr");


					//newFile.renameTo(tmpFile);
					recorder.setOutputFile(newFile.getAbsolutePath());
					recorder.prepare(); // prepare to record
					recorder.start(); // start recording
					recording = true; // we are currently recording


				} catch (IllegalStateException e) {
					Log.e(TAG, e.toString());
				} // end catch
				catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		//Destroy our notification onDestroy
		if(notificationOn){
			nm.cancel(0);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//My custom options bring all the boys to the yard
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		// Sets bitrates
	    switch (item.getItemId()) {
	        case R.id.low:
	        	bitRate = 24000;
	            return true;
	        case R.id.med:
	        	bitRate = 48000;
	            return true;
	        case R.id.high:
	        	bitRate = 96000;
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}//done
