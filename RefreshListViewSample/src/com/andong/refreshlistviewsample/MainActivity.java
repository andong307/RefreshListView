package com.andong.refreshlistviewsample;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andong.widget.OnRefreshListener;
import com.andong.widget.RefreshListView;

public class MainActivity extends Activity {
	
	private List<String> listData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final RefreshListView mRefreshListView = (RefreshListView) findViewById(R.id.refreshlistview);
		
		listData = new ArrayList<String>();
		for (int i = 0; i < 30; i++) {
			listData.add("����ListView������" + i);
		}
		
		Button btn = new Button(this);
		btn.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		btn.setText("����ͷ����");
		btn.setTextSize(25);
		btn.setGravity(Gravity.CENTER);
		mRefreshListView.addCustomHeaderView(btn);
		
		final MyAdapter mAdapter = new MyAdapter();
		mRefreshListView.setAdapter(mAdapter);
		mRefreshListView.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onLoadMoreData() {
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						listData.add("���ظ������������1");
						listData.add("���ظ������������2");
						listData.add("���ظ������������3");
						listData.add("���ظ������������4");
						listData.add("���ظ������������5");
						SystemClock.sleep(2000);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						super.onPostExecute(result);
						mRefreshListView.onRefreshFinish();
						mAdapter.notifyDataSetChanged();
						
					}
				}.execute(new Void[]{});
			}
			
			@Override
			public void onRefresh() {
				new AsyncTask<String, String, String>() {

					@Override
					protected String doInBackground(String... params) {
						listData.add(0, "��������ˢ�³���������");
						SystemClock.sleep(2000);
						return null;
					}

					@Override
					protected void onPostExecute(String result) {
						super.onPostExecute(result);
						mRefreshListView.onRefreshFinish();
						mAdapter.notifyDataSetChanged();
					}
				}.execute(new String[]{});
			}
		});
	}
	
	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return listData.size();
		}

		@Override
		public Object getItem(int position) {
			return listData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = new TextView(MainActivity.this);
			tv.setText(listData.get(position));
			tv.setTextSize(18);
			return tv;
		}
		
	}
}
