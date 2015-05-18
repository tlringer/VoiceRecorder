package eu.siacs.conversations.voicerecorder.ui;

import android.app.Activity;
import android.os.Bundle;

import eu.siacs.conversations.voicerecorder.R;

public class RecordingActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recording);
	}
}
