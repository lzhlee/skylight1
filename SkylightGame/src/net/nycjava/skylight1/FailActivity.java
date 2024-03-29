package net.nycjava.skylight1;

import java.util.Locale;
import java.util.concurrent.Executors;

import net.nycjava.skylight1.dependencyinjection.Dependency;
import net.nycjava.skylight1.dependencyinjection.DependencyInjectingObjectFactory;
import skylight1.util.Adverts;
import skylight1.util.Assets;
import skylight1.util.HighScoreService;
import skylight1.util.PhoneIdHasher;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * reporting unsteady hand; report acknowledged; reporting slow hand; report acknowledged; go to welcome
 */
public class FailActivity extends SkylightActivity {

	private static final String TAG = FailActivity.class.getName();
	
	@Dependency
	private RelativeLayout view;
	private MediaPlayer mp;

	@Override
	protected void addDependencies(DependencyInjectingObjectFactory dependencyInjectingObjectFactory) {

		dependencyInjectingObjectFactory.registerImplementationObject(RelativeLayout.class,
				(RelativeLayout) getLayoutInflater().inflate(R.layout.failmsg, null));
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.failed);
		LinearLayout linearLayout =  (LinearLayout)view.getChildAt(0);
		linearLayout.addView(imageView);
		view.requestLayout();
		
        ViewGroup layout = (ViewGroup)view.findViewById(R.id.layout_ad);
		Adverts.insertAdBanner(this,layout);

		setContentView(view);

		mp = MediaPlayer.create(getBaseContext(), R.raw.failed);
		if(mp!=null) {
			mp.start();
		}

		final int failedLevel = getIntent().getIntExtra(DIFFICULTY_LEVEL, 0);
		new HighScoreService().recordScore(failedLevel, false, this);

		//TODO: review
		Executors.defaultThreadFactory().newThread(new Runnable() {
			@Override
			public void run() {
				final String hashedPhoneId = new PhoneIdHasher().getHashedPhoneId(FailActivity.this);
        		final int azimuthVariance = calculateAzimuth();
            	if(tracker!=null) {
            		tracker.trackEvent("fail", "hashedPhoneId", hashedPhoneId, 0); //TODO: review
            		tracker.trackEvent("fail", "locale", Locale.getDefault().toString(), 0); //TODO: review
            		tracker.trackEvent("fail", "azimuthVariance", Integer.toString(azimuthVariance),azimuthVariance);
            	}
			}
			private int calculateAzimuth() {
				final float compassReadings[] = getIntent().getFloatArrayExtra(COMPASS_READINGS);

				// need at least two readings to get a variance
				if (compassReadings.length < 2) {
					Log.i(TAG, "returning az = 0");
					return 0;
				}

				// using two-pass algorithm from http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
				float mean = 0f;
				for (float compassReading : compassReadings) {
					mean += compassReading;
				}
				mean = mean / (float) compassReadings.length;
				float sumOfSquares = 0f;
				for (float compassReading : compassReadings) {
					final float distance = compassReading - mean;
					sumOfSquares += distance * distance;
				}
				final double variance = sumOfSquares / (float) (compassReadings.length - 1);

				final int standardDeviation = (int) Math.sqrt(variance);

				Log.i(TAG, String.format("az sd is %d", standardDeviation));
				return standardDeviation;
			}
		}).start();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mp!=null) {
			mp.stop();
			mp.release();
			mp = null;
			System.gc();
		}
	}

	void nextLevel() {
		Intent intent = new Intent();
		intent.setClass(FailActivity.this, WelcomeActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			nextLevel();
			finish();
		}
		return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			nextLevel();
			finish();
		}
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			nextLevel();
			finish();
			return true;
		}
		return false;
	}
}
