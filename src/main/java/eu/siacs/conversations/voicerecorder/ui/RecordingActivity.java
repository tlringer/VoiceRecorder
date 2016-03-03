package eu.siacs.conversations.voicerecorder.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.acg.lib.ACGResourceAccessException;
import com.acg.lib.impl.AudioACG;
import com.acg.lib.listeners.ACGActivity;
import com.acg.lib.listeners.ACGListeners;
import com.acg.lib.listeners.ResourceReadyListener;
import eu.siacs.conversations.voicerecorder.R;

import java.io.File;

public class RecordingActivity extends Activity implements View.OnClickListener, ACGActivity {

	protected AudioACG audioACG;

	private TextView mTimerTextView;

	private long mStartTime = 0;

	private Handler mHandler = new Handler();
	private Runnable mTickExecutor = new Runnable() {
		@Override
		public void run() {
			tick();
			mHandler.postDelayed(mTickExecutor,100);
		}
	};
	private File mOutputFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recording);
		this.mTimerTextView = (TextView) this.findViewById(R.id.timer);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.audioACG = (AudioACG) getFragmentManager().findFragmentById(R.id.audio_acg_fragment_id);

        View acgFrame = findViewById(R.id.acg_frame);
        acgFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartTime == 0) {
                    mStartTime = SystemClock.elapsedRealtime();
                    mHandler.postDelayed(mTickExecutor, 100);
                    Log.d("Voice Recorder","started recording to "+mOutputFile.getAbsolutePath());
                }
            }
        });
	}

	private void tick() {
		long time = (mStartTime < 0) ? 0 : (SystemClock.elapsedRealtime() - mStartTime);
		int minutes = (int) (time / 60000);
		int seconds = (int) (time / 1000) % 60;
		int milliseconds = (int) (time / 100) % 10;
		mTimerTextView.setText(minutes+":"+(seconds < 10 ? "0"+seconds : seconds)+"."+milliseconds);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.share_button:
				Uri uri = Uri.parse("file://"+mOutputFile.getAbsolutePath());
				Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				scanIntent.setData(uri);
				sendBroadcast(scanIntent);
				setResult(Activity.RESULT_OK, new Intent().setData(uri));
				finish();
				break;
		}
	}

	@Override
	public ACGListeners buildACGListeners() {
		return new ACGListeners.Builder().withResourceReadyListener(audioACG, new ResourceReadyListener() {
			@Override
			public void onResourceReady() {
				try {
                    mOutputFile = audioACG.getResource();
                    mStartTime = 0;
                    mHandler.removeCallbacks(mTickExecutor);
				} catch (ACGResourceAccessException e) {
					throw new RuntimeException("Unexpected error getting file");
				}
			}
		}).build();
	}
}
