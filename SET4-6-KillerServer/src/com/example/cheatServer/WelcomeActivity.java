package com.example.cheatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends Activity {
	TextView text;
	Button changeText;
	ImageView instruction;
	boolean textChanged = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		text = (TextView)findViewById(R.id.text);
		changeText = (Button)findViewById(R.id.changeText);
		instruction = (ImageView)findViewById(R.id.imageView1);
	}
	public  void xueBaMode(View view)
	{
		if(!ping("192.168.43.1"))
		{
			Toast.makeText(this, "学霸，你还没有创建热点", Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(this,ChatActivityServer.class);
		startActivity(intent);
	}
	public void xueZhaMode(View view)
	{
		if(!ping("192.168.43.1"))
		{
			Toast.makeText(this, "学渣，你还没有连接学霸的wifi", Toast.LENGTH_LONG).show();
			return;
		} else if( ! isServerConnected())
		{
			Toast.makeText(this, "学渣，学霸大人还没进入学霸模式!", Toast.LENGTH_LONG).show();
			return;
		}
			
		Intent intent = new Intent(this,ChatActivityClient.class);
		startActivity(intent);
	}
	//先检查连接上了没
	public boolean ping(String str) {
		boolean resault = false;
			Process p;
			try {
				//ping -c 3 -w 100  中  ，-c 是指ping的次数 3是指ping 3次 ，-w 3  以秒为单位指定超时间隔，是指超时时间为3秒 
				p = Runtime.getRuntime().exec("ping -c 3 -w 3 " + str);
				int status = p.waitFor();

				InputStream input = p.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(input));
			    StringBuffer buffer = new StringBuffer();
			    String line = "";
			    while ((line = in.readLine()) != null){
			      buffer.append(line);
			    }
			    System.out.println("Return ============" + buffer.toString());

				if (status == 0) {
					resault = true;
				} else {
					resault = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			

		return resault;
	}
	//检查学霸开app了没
	private boolean isServerConnected()
	{
		checkThread t  = new checkThread();
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t.getBool();
	}
	//检查学霸开app了没的线程
	class checkThread  extends Thread
	{
		 boolean innerTemp = true;
		 public boolean getBool()
		 {
			 return innerTemp;
		 }
		@Override
		public void run() {
			super.run();
			try {
				Socket soc =  new Socket("192.168.43.1",12345); 
				if( !soc.isConnected())
				{
					innerTemp = false;
				}
				else
				{
					soc.close();
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				innerTemp = false;
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				innerTemp = false;
				e.printStackTrace();
			}
		}
	}
	public void displayImage(View view)
	{
		text.setText("");
		instruction.setVisibility(View.VISIBLE);
		instruction.setImageResource(R.drawable.instruction);
	}
	public void changeText(View view)
	{
		if( ! textChanged)
		{
			text.setText(R.string.attention);
			changeText.setText("使用说明");
			instruction.setVisibility(View.GONE);
			textChanged = true;
		}
		else
		{
			text.setText(R.string.instruction);
			changeText.setText("注意事项");
			instruction.setVisibility(View.GONE);
			textChanged = false;
		}
	}
}
