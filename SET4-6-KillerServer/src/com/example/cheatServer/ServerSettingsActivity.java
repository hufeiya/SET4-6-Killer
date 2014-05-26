package com.example.cheatServer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ServerSettingsActivity extends Activity implements OnSeekBarChangeListener{

	SeekBar sensetivityBar;
	float keySensetivity = 3;//重力感应灵敏度，范围2.1-9.1
	Intent intent;
	Bundle bundle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serversettings);
		sensetivityBar = (SeekBar)findViewById(R.id.seekBar1);
		sensetivityBar.setOnSeekBarChangeListener(this);
		intent = getIntent();
		keySensetivity = intent.getFloatExtra("formerSensetivity", 3.0f);
		sensetivityBar.setProgress((int)(keySensetivity * 10));
		bundle = new Bundle();
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		keySensetivity = (seekBar.getProgress() + 21) / 10.0f;//最大值是70，为保持处于2.1~9.1之间
		Log.d("fuck", "改变灵敏度");
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	public void cancel(View view)
	{
		finish();
		
	}
	public void confirm(View view)
	{
		bundle.putFloat("newSensetivity", keySensetivity);
		intent.putExtras(bundle);
		setResult(0x111,intent);
		Log.d("fuck", "停止设置");
		finish();
	}

}
