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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class VoiceRecorder extends Activity {
	private static final String TAG = VoiceRecorder.class.getName();
	private static MediaRecorder recorder; // used to record audio
	private static boolean recording; // are we currently recording
	public static Activity activity;
	public File tmpFile;
	static File newFile;
	AudioManager am;
	boolean speakerON;
	boolean notificationOn;
	static boolean initiated;
	static boolean initiated2;
	static boolean named;
	Button bt;
	Button bt2;
	static Button bt3;
	View v2;
	View v3;
	Thread myThread;
	static TextView textView;
	static Context ctx;
	ViewGroup root;
	LayoutInflater inflater;
	AudioManager audioManager;
	Boolean helper;
	static Intent service;
	static Notification notification;
	static NotificationManager nm;
	static RemoteViews contentView;
	String name;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		activity = this;
		bt = (Button) findViewById(R.id.recordButton);
		bt2 = (Button) findViewById(R.id.viewSaved);
		createNotification();
		notificationOn = true;
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		ctx = VoiceRecorder.this;

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
				final String text = ("Rememo");
				contentView.setTextViewText(R.id.textView, text);
				contentView.setTextColor(R.id.textView, getResources().getColor(R.color.blue));
				notification.contentView = contentView;
				nm.notify(0, notification);
			}
			newFile.delete();
			TextView tv = (TextView) findViewById(R.id.statusText);
			tv.setText("Stopped");
			tv.setTextColor(getResources().getColor(R.color.blue));
			bt.setText(R.string.rec);
			bt.setTextColor(getResources()
					.getColor(R.color.white));
			bt2.setEnabled(true);
		}
	}

	public static void initiate() {
		initiated = true;
	}

	public static void initiate2() {
		initiated2 = true;
	}
	
	public void startStop(View v) {

		v2 = v;
		if (recording) {
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


			final EditText nameEditText = new EditText(VoiceRecorder.this);
			nameEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
					.toggleSoftInput(InputMethodManager.SHOW_FORCED,
							InputMethodManager.HIDE_IMPLICIT_ONLY);

			new AlertDialog.Builder(VoiceRecorder.this)
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
													0);

									String value = nameEditText.getText()
											.toString().trim();
									if (value.length() != 0) {
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
														TextView tv = (TextView) findViewById(R.id.statusText);
														tv.setText("Stopped");
														tv.setTextColor(getResources().getColor(R.color.blue));
														bt.setText(R.string.rec);
														bt.setTextColor(getResources()
																.getColor(
																		R.color.white));
														bt2.setEnabled(true);
													}
												});
										builder.setItems(
												cs,
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int item) {
														String temp = (String) cs[item];

														if (temp.equals("Create New Category")) {


															final EditText nameEditText = new EditText(
																	VoiceRecorder.this);
															nameEditText
																	.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

															((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
																	.toggleSoftInput(
																			InputMethodManager.SHOW_FORCED,
																			InputMethodManager.HIDE_IMPLICIT_ONLY);

															// create an input
															// dialog to get
															// recording name
															// from
															// user
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
																						tmpFile = new File(
																								getExternalFilesDir(
																										value)
																										.getAbsolutePath()
																										+ File.separator
																										+ name
																										+ ".3gp");
																						newFile.renameTo(tmpFile);
																						TextView tv = (TextView) findViewById(R.id.statusText);
																						tv.setText("Stopped");
																						tv.setTextColor(getResources().getColor(R.color.blue));
																						bt.setText(R.string.rec);
																						bt.setTextColor(getResources()
																								.getColor(
																										R.color.white));
																						bt2.setEnabled(true);

																					} else {
																						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
																								.hideSoftInputFromWindow(
																										nameEditText
																												.getWindowToken(),
																										0);
																						newFile.delete();
																						TextView tv = (TextView) findViewById(R.id.statusText);
																						tv.setText("Stopped");
																						tv.setTextColor(getResources().getColor(R.color.blue));
																						bt.setText(R.string.rec);
																						bt.setTextColor(getResources()
																								.getColor(
																										R.color.white));
																						bt2.setEnabled(true);

																					}
																				}
																			})
																	.setNegativeButton(
																			"Cancel",
																			new DialogInterface.OnClickListener() {
																				public void onClick(
																						DialogInterface dialog,
																						int whichButton) {
																					// Do
																					// nothing.
																					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
																							.hideSoftInputFromWindow(
																									nameEditText
																											.getWindowToken(),
																									0);
																					newFile.delete();
																					TextView tv = (TextView) findViewById(R.id.statusText);
																					tv.setText("Stopped");
																					tv.setTextColor(getResources().getColor(R.color.blue));
																					bt.setText(R.string.rec);
																					bt.setTextColor(getResources()
																							.getColor(
																									R.color.white));
																					bt2.setEnabled(true);
																				}
																			})
																	.show();
														} else {
															try {

																tmpFile = new File(
																		getExternalFilesDir(
																				temp)
																				.getAbsolutePath()
																				+ File.separator
																				+ name
																				+ ".3gp");
																newFile.renameTo(tmpFile);
																TextView tv = (TextView) findViewById(R.id.statusText);
																tv.setText("Stopped");
																tv.setTextColor(getResources().getColor(R.color.blue));
																bt.setText(R.string.rec);
																bt.setTextColor(getResources()
																		.getColor(
																				R.color.white));
																bt2.setEnabled(true);
															} catch (IllegalStateException e) {
																Log.e(TAG,
																		e.toString());
															} // end catch

														}
													}
												}).show();

										TextView tv = (TextView) findViewById(R.id.statusText);
										tv.setText("Stopped");
										tv.setTextColor(getResources().getColor(R.color.blue));
										bt.setText(R.string.rec);
										bt.setTextColor(getResources()
												.getColor(R.color.white));
										bt2.setEnabled(true);
									} else {
										newFile.delete();
										TextView tv = (TextView) findViewById(R.id.statusText);
										tv.setText("Stopped");
										tv.setTextColor(getResources().getColor(R.color.blue));
										bt.setText(R.string.rec);
										bt.setTextColor(getResources()
												.getColor(R.color.white));
										bt2.setEnabled(true);
										named = false;
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
									newFile.delete();
									TextView tv = (TextView) findViewById(R.id.statusText);
									tv.setText("Stopped");
									tv.setTextColor(getResources().getColor(R.color.blue));
									bt.setText(R.string.rec);
									bt.setTextColor(getResources().getColor(
											R.color.white));
									bt2.setEnabled(true);
									named = false;
								}
							}).show();

		}

		else {
			if (notificationOn) {
				final String text = ("Rememoing");
				contentView.setTextViewText(R.id.textView, text);
				contentView.setTextColor(R.id.textView, Color.RED);
				notification.contentView = contentView;
				nm.notify(0, notification);
			}
			startRecording(v2);
			bt.setText(R.string.stp);
			bt.setTextColor(getResources().getColor(R.color.red));
			bt2.setEnabled(false);
			try {
				newFile = new File(getExternalFilesDir("New Memos")
						.getAbsolutePath()
						+ File.separator
						+ getTime()
						+ ".3gp");
				recorder.setOutputFile(newFile.getAbsolutePath());
				recorder.prepare(); // prepare to record
				recorder.start(); // start recording
				recording = true; // we are currently recording
				TextView tv = (TextView) findViewById(R.id.statusText);
				tv.setText("Recording");
				tv.setTextColor(getResources().getColor(R.color.red));

			} catch (IllegalStateException e) {
				Log.e(TAG, e.toString());
			} // end catch
			catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}

	}

	public void startRecording(View v) {
		Log.d("startRecording", "Start Button Pressed.");
		if (recorder == null)
			recorder = new MediaRecorder(); // create MediaRecorder
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		recorder.setAudioEncodingBitRate(96000);
		recorder.setAudioSamplingRate(44100);

	}

	public void stopRecording(View v) {
		if (!recording)
			return;
		recorder.stop();
		recorder.reset();
		recording = false;
	}

	public static Boolean getRecording() {
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

	public static String getTime() {
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

	public void viewSaved(View v) {
		Intent intent = new Intent(VoiceRecorder.this, Categories.class);
		startActivity(intent);
	}

	public void showNotificationClicked(View v) {
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

	public static void setNotRecording(Activity activity) {

		final String text = ("Rememo");
		contentView.setTextViewText(R.id.textView, text);
		contentView.setTextColor(R.id.textView, activity.getResources().getColor(R.color.white) );
		notification.contentView = contentView;
		nm.notify(0, notification);

		Button b2 = (Button) activity.findViewById(R.id.recordButton);
		b2.setText(R.string.rec);
		b2.setTextColor(activity.getResources().getColor(R.color.white));
		Button b3 = (Button) activity.findViewById(R.id.viewSaved);
		b3.setEnabled(true);
		TextView tv = (TextView) activity.findViewById(R.id.statusText);
		tv.setText("Stopped");
		
		tv.setTextColor(activity.getResources().getColor(R.color.blue));
		Toast toast = Toast.makeText(ctx, "Saved to New Memos",
				Toast.LENGTH_SHORT);
		toast.show();

	}

	public static void setRecording(Activity activity) {

		final String text = ("Rememoing");
		contentView.setTextViewText(R.id.textView, text);
		contentView.setTextColor(R.id.textView, Color.RED);
		notification.contentView = contentView;
		nm.notify(0, notification);
 
		Button b2 = (Button) activity.findViewById(R.id.recordButton);
		b2.setText(R.string.stp);
		b2.setTextColor(activity.getResources().getColor(R.color.red));
		Button b3 = (Button) activity.findViewById(R.id.viewSaved);
		b3.setEnabled(false);
		TextView tv = (TextView) activity.findViewById(R.id.statusText);
		tv.setText("Recording");
		tv.setTextColor(activity.getResources().getColor(R.color.red));
	}

	@SuppressLint("NewApi")
	private void createNotification() {
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

		Intent buttonIntent = new Intent(this, switchButtonListener.class);
		PendingIntent pbuttonIntent = PendingIntent.getBroadcast(this, 1,
				buttonIntent, 0);
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

			if (recording) {
				setNotRecording(VoiceRecorder.activity);
				recorder.stop(); // stop recording
				recorder.reset(); // reset the MediaRecorder
				recording = false; // we are no longer recording
				if (initiated) {
					SavedRecordings.refresh(newFile.getName());
				}
				if (initiated2) {
					Categories.refresh();
				}

			} else {
				setRecording(VoiceRecorder.activity);

				if (recorder == null)
					recorder = new MediaRecorder(); // create MediaRecorder
				recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
				recorder.setAudioEncodingBitRate(96000);
				recorder.setAudioSamplingRate(44100);
				try {
					newFile = new File(context.getExternalFilesDir("New Memos")
							.getAbsolutePath()
							+ File.separator
							+ getTime()
							+ ".3gp");
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
}
