package com.example.cheatServer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
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


public class ChatActivityClient extends Activity {
	Socket socket;
	Handler handler;
	EditText name ;
	 EditText sentence ;
	 TextView show;
	 Button send ;
	 ScrollView scroll;
	 Vibrator vibrator;//震动棒
	 boolean shakingOn = false;
	 long pattern[][] = {{100,100},{100,100,100,100},{100,100,100,100,100,100},{100,100,100,100,100,100,100,100}};//震动模式
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		name = (EditText)findViewById(R.id.name);
		sentence = (EditText)findViewById(R.id.sentence);
		show = (TextView)findViewById(R.id.editText3);
		send  = (Button)findViewById(R.id.send);
		scroll = (ScrollView)findViewById(R.id.scrollView1);
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
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
				show.append("\n\n" + newMsg);
				scrollToBottom(scroll,show);
				shaking(newMsg);//发送震动
			}
		};
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

		default:
			break;
		}
	}
}
