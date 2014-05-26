package com.example.cheatServer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;


public class ChatActivityServer extends Activity implements SensorEventListener{
	Socket socket;
	Handler handler;
	EditText name ;
	 EditText sentence ;
	 TextView show;
	 Button send ;
	 ScrollView scroll;
	 String ip;
	 SensorManager sensorManager;
	 Sensor sensor;
	 boolean typed = false;//已经输出答案
	 boolean resented = false;//已经输出重发信号
	 Vibrator vibrator;//输出答案震动
	 int answerNum = 0;//题号
	 float sensetivity = 3;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		name = (EditText)findViewById(R.id.name);
		sentence = (EditText)findViewById(R.id.sentence);
		show = (TextView)findViewById(R.id.editText3);
		send  = (Button)findViewById(R.id.send);
		scroll = (ScrollView)findViewById(R.id.scrollView1);
		
		sensorManager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_UI);
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		new ChatServerThread().start();
		//4.0之后联网不能写入主线程内
		Runnable  getTouch = new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					ip = getLocalIPAddress();
					Log.d("fuck", ip);//调试打印下ip地址
					//经测试发现貌似开热点的手机ip都是这个，不排除一些ROM不是
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
		handler = new Handler(){
			public void handleMessage(Message msg){
				super.handleMessage(msg);
				String tmp = show.getText().toString();
				show.setText(tmp + "\n\n" + msg.getData().getString("chat"));
				scrollToBottom(scroll,show);
			}
		};
	}
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
	// 获取本地IP函数,暂未使用
	 public String getLocalIPAddress() {  
	        try {  
	            for (Enumeration<NetworkInterface> en = NetworkInterface  
	                    .getNetworkInterfaces(); en.hasMoreElements();) {  
	                NetworkInterface intf = en.nextElement();  
	                for (Enumeration<InetAddress> enumIpAddr = intf  
	                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {  
	                    InetAddress inetAddress = enumIpAddr.nextElement();  
	                    if (!inetAddress.isLoopbackAddress()) {  
	                        return inetAddress.getHostAddress().toString();  
	                    }  
	                }  
	            }  
	        } catch (SocketException ex) {  
	            Log.d("fuck", ex.toString()+ "2");  
	        }  
	        return null;  
	    }
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSensorChanged(SensorEvent ev) {
		//如果基本摆正，方可输出下一题答案
		if(Math.abs(ev.values[0]) < 2 && Math.abs(ev.values[1]) < 2)
		{
			typed = false;
		}
		//如果重新翻正，则可重新发送
		if(ev.values[2] > 8)
		{
			resented = false;
		}
		//把手机翻过了就是重新输入
		if(ev.values[2] < -4  && ! resented)
		{
			 setTypedToTrue();
			answerNum = 0;
			 String s1 = name.getText().toString();
			 String s2 = "---重新发一遍---R";
			 String s = s1 + s2 + "\n";
				if(socket != null)
				{
					try {
						socket.getOutputStream().write(s.getBytes("utf-8"));
						vibrator.vibrate(1000);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}	
				resented = true;
				typed = true;
		}
		if(ev.values[0] > sensetivity && ! typed)//输出A
		{
			 setTypedToTrue();
			 String s1 = name.getText().toString();
			 String s2 = "A";
			 String s = s1 + " : [" + (++answerNum) + "] "+ s2 + "\n";
				if(socket != null)
				{
					try {
						socket.getOutputStream().write(s.getBytes("utf-8"));
						vibrator.vibrate(100);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}	
		}
		else if(ev.values[0] < 0 - sensetivity && ! typed)//输出B
		{
			setTypedToTrue();
			String s1 = name.getText().toString();
			String s2 = "B";
			String s = s1 + " : [" + (++answerNum) + "] "+ s2 + "\n";
			if(socket != null)
			{
				try {
					socket.getOutputStream().write(s.getBytes("utf-8"));
					vibrator.vibrate(100);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		else if(ev.values[1] <0 - sensetivity && ! typed)//输出C
		{
			setTypedToTrue();
			String s1 = name.getText().toString();
			String s2 = "C";
			String s = s1 + " : [" + (++answerNum) + "] "+ s2 + "\n";
			if(socket != null)
			{
				try {
					socket.getOutputStream().write(s.getBytes("utf-8"));
					vibrator.vibrate(100);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		else if(ev.values[1] > sensetivity && ! typed)//输出D
		{
			setTypedToTrue();
			String s1 = name.getText().toString();
			String s2 = "D";
			String s = s1 + " : [" + (++answerNum) + "] "+ s2 + "\n";
			if(socket != null)
			{
				try {
					socket.getOutputStream().write(s.getBytes("utf-8"));
					vibrator.vibrate(100);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		
	}  
		private void setTypedToTrue()
		{
			typed = true;
		}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		System.exit(0);
		super.onDestroy();
	}
	@Override//创建学霸端菜单
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.servermenu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			Intent intent = new Intent(this,ServerSettingsActivity.class);
			intent.putExtra("formerSensetivity", sensetivity);
			Log.d("fuck", "启动设置前");
			startActivityForResult(intent, 0x111);
			break;

		default:
			break;
		}
		return true;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 0x111 && resultCode == 0x111)
		{
			Log.d("fuck", "返回参数");
			Bundle bundle = data.getExtras();
			sensetivity = bundle.getFloat("newSensetivity");
			Log.d("fuck", "返回灵敏度，改变它到主界面");
		}
	}
}
