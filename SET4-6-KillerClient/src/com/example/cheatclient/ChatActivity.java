package com.example.cheatclient;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;


public class ChatActivity extends Activity {
	Socket socket;
	Handler handler;
	EditText name ;
	 EditText sentence ;
	 TextView show;
	 Button send ;
	 ScrollView scroll;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		name = (EditText)findViewById(R.id.name);
		sentence = (EditText)findViewById(R.id.sentence);
		show = (TextView)findViewById(R.id.editText3);
		send  = (Button)findViewById(R.id.send);
		scroll = (ScrollView)findViewById(R.id.scrollView1);
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
	@Override
	protected void onResume() {
		super.onResume();
		scrollToBottom(scroll,show);
	}
}
