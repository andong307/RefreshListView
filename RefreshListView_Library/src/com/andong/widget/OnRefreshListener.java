package com.andong.widget;

/**
 * @author andong
 * RefreshListView刷新事件的监听
 */
public interface OnRefreshListener {

	/**
	 * 当下拉刷新时调用
	 */
	public void onRefresh();
	
	/**
	 * 当加载更多时回调
	 */
	public void onLoadMoreData();
}
