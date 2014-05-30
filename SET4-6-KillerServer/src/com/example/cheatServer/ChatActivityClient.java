package com.example.cheatServer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public class ChatActivityClient extends Activity  implements SensorEventListener {
	Socket socket;
	Handler handler;
	EditText name ;
	 EditText sentence ;
	 TextView show;
	 Button send ;
	 ScrollView scroll;
	 Vibrator vibrator;//震动棒
	 SensorManager sensorManager;
	 Sensor sensor;
	 boolean resented = false;//重新发送开关
	 boolean delayShakingOn = false;
	 boolean shakingOn = false;
	 char[]answerNumbers;
	 int currentNum =  0;
	 long pattern[][] = {{100,100},{100,100,100,100},{100,100,100,100,100,100},{100,100,100,100,100,100,100,100}};//震动模式
	 long pattern2[][] = {{800,100},{800,100,100,100},{800,100,100,100,100,100},{800,100,100,100,100,100,100,100}};//延迟发送的震动模式
	 long pattern3[][] = {{2000,100},{2000,100,100,100},{2000,100,100,100,100,100},{2000,100,100,100,100,100,100,100}};//每隔5个题延迟两秒
	 long finalPattern[];//最终生成的震动数组
	 int currentSizeOfFinalPattern = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		name = (EditText)findViewById(R.id.name);
		sentence = (EditText)findViewById(R.id.sentence);
		show = (TextView)findViewById(R.id.editText3);
		send  = (Button)findViewById(R.id.send);
		scroll = (ScrollView)findViewById(R.id.scrollView1);
		finalPattern = new long[8 * 500];
		sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		answerNumbers = new char[500];//还没见过一场考试超过500个选择题咕~~(╯﹏╰)b
		for(int i = 0; i < 500;i ++){answerNumbers[i] = 'C';}//防止网络问题传不到前几题的答案
		//4.0之后联网不能写入主线程内
		Runnable  getTouch = new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					socket = new Socket("192.168.43.1",12345);
					new ChatThread(socket).start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(getTouch).start();
		send.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String s1 = name.getText().toString();
				String s2 = sentence.getText().toString();
				String s = s1 + " : " + s2 + "\n";
				if(socket != null)
				{
					try {
						socket.getOutputStream().write(s.getBytes("utf-8"));
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					sentence.setText("");
				}
			}
		});
		//接受网络线程传回的信息
		handler = new Handler(){
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				String newMsg = msg.getData().getString("chat");
				saveAnswers(newMsg);
				show.append("\n\n" + newMsg);
				scrollToBottom(scroll,show);
				shaking(newMsg);//发送震动
			}
		};
	}
	//保存答案到answerNumbers，以便延迟发送
	private void saveAnswers(String msg)
	{
		int index = msg.indexOf(": [") + 3;
		if(index == 2)
			return;
		int i =1;
		int ind = index + 1;
		for(; msg.length() > ind && msg.charAt(ind) >='0' && msg.charAt(ind) <='9';i++){ind ++;}
		int no = Integer.parseInt(msg.substring(index, index + i));
		if(no > currentNum)
			currentNum = no;
		if(no >= 500)
			return;
		char answer = msg.charAt(index + i + 2);
		answerNumbers[no] =  answer;
	}
	//网络线程，用户接收信息
	class ChatThread extends Thread{
		Socket s;
		public ChatThread(Socket client) {
			s = client;
		}
		public void run(){
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(),"utf-8"));
				while(true)
				{
					String sentence = in.readLine();
					if(sentence != null)
					{
						Bundle b = new Bundle();
						b.putString("chat", sentence);
						Message msg = Message.obtain();
						msg.setData(b);
						handler.sendMessage(msg);
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	//使滚动条每次滚到最新的消息
	public static void scrollToBottom(final View scroll, final View inner) {
		Handler mHandler = new Handler();
		mHandler.post(new Runnable() {
			public void run() {
					if (scroll == null || inner == null) {
							return;
					}
					int offset = inner.getMeasuredHeight() - scroll.getHeight();
					if (offset < 0) {
						offset = 0;
					}
					scroll.scrollTo(0, offset);
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
		scrollToBottom(scroll,show);
	}
	@Override//创建学渣端菜单
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.clientmenu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.shake:
			if( ! shakingOn)
			{
				shakingOn = true;
				item.setChecked(true);
			}
			else
			{
				shakingOn = false;
				item.setChecked(false);
			}
			break;
		case R.id.delayShake:
			if( ! delayShakingOn)
			{
				delayShakingOn = true;
				item.setChecked(true);
			}
			else
			{
				delayShakingOn = false;
				item.setChecked(false);
			}
			break;
		default:
			break;
		}
		return true;
	}
	//答案震动提醒功能，A震动一次，B震动两次。。
	public void shaking(String newMsg)
	{
		if( ! shakingOn)
		{
			return;
		}
		char key = newMsg.charAt(newMsg.length()-1);//最后一个字符即为答案
		
		switch (key) {
		case 'A':
			vibrator.vibrate(pattern[0], -1);
			break;
		case 'B':
			vibrator.vibrate(pattern[1], -1);
			break;
		case 'C':
			vibrator.vibrate(pattern[2], -1);
			break;
		case 'D':
			vibrator.vibrate(pattern[3], -1);
			break;
		case 'R'://重新发送信号，长振一秒
			vibrator.vibrate(1000);
		default:
			break;
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		if( ! delayShakingOn)//菜单没打开此选项的话
			return;
		//如果重新翻正，则可重新发送
		if(event.values[2] > 8)
		{
			resented = false;
		}
		//如果翻过来手机，就震动发送存储的答案。
		if(event.values[2] < -4  && ! resented)
		{
			boolean flag = false;
			if(shakingOn)
			{
				shakingOn = false;//先把实时震动关闭，以免同时震动
				flag = true;
			}
			int perBreak = 0;//每隔5个题停止一段时间
			char answer;
			for(int i = 1 ;i <= currentNum;i++)
			{
				answer = answerNumbers[i];
				perBreak++;
				if((perBreak - 1)%5 != 0)//第6,11,16...个答案时暂停时间长
				{
					switch (answer) {
					case 'A':
						 iterateSubarray(pattern2[0]);
						break;
					case 'B':
						iterateSubarray(pattern2[1]);
						break;
					case 'C':
						iterateSubarray(pattern2[2]);
						break;
					case 'D':
						iterateSubarray(pattern2[3]);
						break;
					default:
						break;
					}
				}
				else
				{
					switch (answer) {
					case 'A':
						iterateSubarray(pattern3[0]);
						break;
					case 'B':
						iterateSubarray(pattern3[1]);
						break;
					case 'C':
						iterateSubarray(pattern3[2]);
						break;
					case 'D':
						iterateSubarray(pattern3[3]);
						break;
					default:
						break;
					}
				}
			}
			resented = true;
			if(flag)
				shakingOn = true;//最后再开开实时震动
			finalPattern[currentSizeOfFinalPattern++] = 1000 *3600 * 3;//发完答案静默3小时，因为数组长度不可控制
			vibrator.vibrate(finalPattern, -1);
		}
	}
	//遍历子数组，用于生成最后的震动数组
	void iterateSubarray(long subarray[])
	{
		for(long sub : subarray)
		{
			finalPattern[currentSizeOfFinalPattern++] = sub;
		}
	}
}
