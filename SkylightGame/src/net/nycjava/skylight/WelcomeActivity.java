package net.nycjava.skylight;

import java.io.IOException;

import net.nycjava.skylight.dependencyinjection.Dependency;
import net.nycjava.skylight.dependencyinjection.DependencyInjectingObjectFactory;
import net.nycjava.skylight.view.TypeFaceTextView;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WelcomeActivity extends SkylightActivity {
	public class HighlightTextFocusChangeListener implements OnFocusChangeListener {

		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			int unfocusedColor = arg0.getContext().getResources().getColor(R.color.button_font_color);
			int focusedColor = arg0.getContext().getResources().getColor(R.color.button_font_color_focused);
			((TextView) arg0).setTextColor(arg1 ? focusedColor : unfocusedColor);
		}
	}

	private SurfaceView preview;

	private SurfaceHolder holder;

	private final class DifficultyClickListener implements OnClickListener {
		final private int difficulty;

		public DifficultyClickListener(int aDifficulty) {
			super();
			difficulty = aDifficulty;
		}

		@Override
		public void onClick(View aView) {
			aView.performHapticFeedback(HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

			final Intent intent = new Intent(WelcomeActivity.this, SkillTestActivity.class);
			intent.putExtra(SkylightActivity.DIFFICULTY_LEVEL, difficulty);
			startActivity(intent);
		}
	}

	@Dependency
	private LinearLayout contentView;

	protected void addDependencies(DependencyInjectingObjectFactory aDependencyInjectingObjectFactory) {
		aDependencyInjectingObjectFactory.registerImplementationObject(LinearLayout.class,
				(LinearLayout) getLayoutInflater().inflate(R.layout.welcome, null));
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		((TypeFaceTextView) contentView.findViewById(R.id.easy)).setOnClickListener(new DifficultyClickListener(
				SOBER_DIFFICULTY_LEVEL));
		((TypeFaceTextView) contentView.findViewById(R.id.normal)).setOnClickListener(new DifficultyClickListener(
				BUZZED_DIFFICULTY_LEVEL));
		((TypeFaceTextView) contentView.findViewById(R.id.hard)).setOnClickListener(new DifficultyClickListener(
				SMASHED_DIFFICULTY_LEVEL));

		((TypeFaceTextView) contentView.findViewById(R.id.easy))
				.setOnFocusChangeListener(new HighlightTextFocusChangeListener());
		((TypeFaceTextView) contentView.findViewById(R.id.normal))
				.setOnFocusChangeListener(new HighlightTextFocusChangeListener());
		((TypeFaceTextView) contentView.findViewById(R.id.hard))
				.setOnFocusChangeListener(new HighlightTextFocusChangeListener());

		preview = (SurfaceView) contentView.findViewById(R.id.videoview);
		preview.setBackgroundResource(R.drawable.background_table);
		holder = preview.getHolder();
		holder.addCallback(new Callback() {
			private MediaPlayer mp;

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				mp = new MediaPlayer();
				mp.setDisplay(preview.getHolder());

				mp.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						Log.i(WelcomeActivity.class.getName(), "mp is prepared");

						preview.setBackgroundResource(0);

						// start the video
						mp.start();
					}
				});

				try {
					AssetFileDescriptor afd = getAssets().openFd("passthedrink.mp4");
					mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
					mp.prepareAsync();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				mp.stop();
				mp.release();
				Log.i(WelcomeActivity.class.getName(), "surface destroyed");
			}
		});

		setContentView(contentView);

		// encourage a garbage collection, to minimize the change that the skill
		// test activity stutters from GCs
		System.gc();
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		final TypeFaceTextView easyButton = (TypeFaceTextView) contentView.findViewById(R.id.easy);
		final TypeFaceTextView normalButton = (TypeFaceTextView) contentView.findViewById(R.id.normal);
		final TypeFaceTextView hardButton = (TypeFaceTextView) contentView.findViewById(R.id.hard);

		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				final int colour = Color.rgb(255, (int) (255 * interpolatedTime),
						(int) (255 * interpolatedTime));
				if (!easyButton.isFocused()) {
					easyButton.setTextColor(colour);
				}
				if (!normalButton.isFocused()) {
					normalButton.setTextColor(colour);
				}
				if (!hardButton.isFocused()) {
					hardButton.setTextColor(colour);
				}
			}
		};
		anim.setDuration(900);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(Animation.INFINITE);
		anim.setInterpolator(new CycleInterpolator(0.5f));
		easyButton.setAnimation(anim);

		anim.start();
	}
}
