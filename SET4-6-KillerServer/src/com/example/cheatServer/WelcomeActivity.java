package com.example.cheatServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class WelcomeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
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
		}
		Intent intent = new Intent(this,ChatActivityClient.class);
		startActivity(intent);
	}
	//先检查连接上了没
	public boolean ping(String str) {
		boolean resault = false;
			Process p;
			try {
				//ping -c 3 -w 100  中  ，-c 是指ping的次数 3是指ping 3次 ，-w 100  以秒为单位指定超时间隔，是指超时时间为100秒 
				p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + str);
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
	
	
}
