package com.andong.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andong.refreshlistview_library.R;

/**
 * @author andong
 * 下拉刷新, 上拉加载 ListView
 */
public class RefreshListView extends ListView implements OnScrollListener {

	private final String TAG = "RefreshListView";
	private int firstVisibleItem;	// 滚动时显示在顶部的position
	private int downY = -1; // 按下y轴的偏移量
	private int refreshViewHeight;	// 下拉刷新布局的高度
	private LinearLayout headerView;	// 头布局
	private View mRefreshView;	// 头布局
	private DisplayMode currentState = DisplayMode.PULL_DOWN_REFRESH;	// 头布局当前的状态, 默认为下拉状态
	private RotateAnimation upAnimation;		// 向上转动的动画
	private RotateAnimation downAnimation;		// 向下转动的动画
	private ImageView ivArrow;	// 箭头
	private ProgressBar pbProgress;	// 进度条
	private TextView tvState;	// 刷新状态
	private TextView tvLastUpdateTime;	// 最后刷新时间
	private OnRefreshListener mOnRefreshListener;	// 下拉刷新监听事件
	private boolean isScroll2Bottom = false;	// 是否滚动到底部
	private View footerView; // 加载更多布局
	private int footerViewHeight; // 加载更多布局高度
	private boolean isLoadMoring = false;	// 是否正在加载更多中
	private boolean isEnablePullToRefresh = false;	// 是否启用下拉刷新功能
	private boolean isEnableLoadingMore = false;	// 是否启用加载更多功能
	private View topView;
	private int mLocationInWindowY = -1;

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initHeaderView();
		initFooterView();
		setOnScrollListener(this);
	}

	public void addCustomHeaderView(View v) {
		this.topView = v;
		headerView.addView(v);
	}
	
	/**
	 * 初始化头布局
	 */
	private void initHeaderView() {
		headerView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.listview_header, null);
		mRefreshView = headerView.findViewById(R.id.listview_header);
		
		ivArrow = (ImageView) mRefreshView.findViewById(R.id.iv_listview_header_arrow);
		pbProgress = (ProgressBar) mRefreshView.findViewById(R.id.pb_progress);
		tvState = (TextView) mRefreshView.findViewById(R.id.tv_refresh_state);
		tvLastUpdateTime = (TextView) mRefreshView.findViewById(R.id.tv_last_update_time);
		
		ivArrow.setMinimumWidth(50);
		tvLastUpdateTime.setText("最后刷新时间: " + getLastUpdateTime());
		
		mRefreshView.measure(0, 0);
		refreshViewHeight = mRefreshView.getMeasuredHeight();
		
		mRefreshView.setPadding(0, -refreshViewHeight, 0, 0);
		
		addHeaderView(headerView);
		initAnimation();
	}
	
	/**
	 * 初始化动画
	 */
	private void initAnimation() {
		upAnimation = new RotateAnimation(
				0f, -180f, 
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF, 0.5f);
		upAnimation.setDuration(500);
		upAnimation.setFillAfter(true);
		
		downAnimation = new RotateAnimation(
				-180f, -360f, 
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF, 0.5f);
		downAnimation.setDuration(500);
		downAnimation.setFillAfter(true);
	}
	
	/**
	 * 初始化脚布局
	 */
	private void initFooterView() {
		footerView = LayoutInflater.from(getContext()).inflate(R.layout.listview_footer, null);
		footerView.measure(0, 0);
		footerViewHeight = footerView.getMeasuredHeight();
		
		// 隐藏脚布局
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		addFooterView(footerView);
	}

	/**
	 * 获得最新的时间
	 * @return
	 */
	private String getLastUpdateTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		/**
		 * SCROLL_STATE_IDLE 当滚动停滞时返回
		 * SCROLL_STATE_TOUCH_SCROLL 按住屏幕使ListView滚动时返回
		 * SCROLL_STATE_FLING 快速的拖动屏幕滚动返回
		 */
		if(isEnableLoadingMore
				&&(scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_FLING)
				&& isScroll2Bottom
				&& !isLoadMoring) {
			footerView.setPadding(0, 0, 0, 0);
			setSelection(getCount());	// 滚动到ListView的最后一条
			isLoadMoring = true;
			// 回调用户的监听事件
			if(mOnRefreshListener != null) {
				mOnRefreshListener.onLoadMoreData();
			}
		}
	}

	/**
	 * 当ListView滚动时回调
	 * firstVisibleItem 当前屏幕滚动时最顶部显示的item的position
	 * visibleItemCount 当前屏幕滚动时显示多少个条目
	 * totalItemCount ListView的总条数
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.firstVisibleItem = firstVisibleItem;
		
		if((firstVisibleItem + visibleItemCount) == totalItemCount
				&& totalItemCount > visibleItemCount) {
			isScroll2Bottom = true;
		} else {
			isScroll2Bottom = false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if(downY == -1) {
				downY = (int) ev.getY();
			}
			
			if(topView != null) {
				int[] location = new int[2];
				if(mLocationInWindowY == -1) {
					getLocationInWindow(location);
					mLocationInWindowY = location[1];
				}
				
				topView.getLocationInWindow(location);
				if(location[1] < mLocationInWindowY) {
					break;
				}
			}

			int moveY = (int) ev.getY();	// 移动中的y轴偏移量

			int paddingTop = -refreshViewHeight + (moveY - downY) / 2;
			
			if(currentState == DisplayMode.REFRESHING) {	// 如果正在刷新中, 不做下拉的动作
				break;
			}
			
			if(isEnablePullToRefresh
					&& firstVisibleItem == 0
					&& paddingTop > -refreshViewHeight) {
				
				if(currentState == DisplayMode.PULL_DOWN_REFRESH
						&& paddingTop > 0) {		// 完全显示,并且当前的状态是下拉刷新状态
					currentState = DisplayMode.RELEASE_REFRESH;
					refreshHeaderViewState();
				} else if(currentState == DisplayMode.RELEASE_REFRESH
						&& paddingTop < 0) {	// 没有完全显示, 并且当前的状态是松开刷新状态
					currentState = DisplayMode.PULL_DOWN_REFRESH;
					refreshHeaderViewState();
				}
				mRefreshView.setPadding(0, paddingTop, 0, 0);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			downY = -1;
			
			if(currentState == DisplayMode.PULL_DOWN_REFRESH) {	// 下拉刷新状态
				mRefreshView.setPadding(0, -refreshViewHeight, 0, 0);
			} else if(currentState == DisplayMode.RELEASE_REFRESH) {	// 松开刷新
				mRefreshView.setPadding(0, 0, 0, 0);
				currentState = DisplayMode.REFRESHING;
				refreshHeaderViewState();
				
				if(mOnRefreshListener != null) {
					// 通知用户刷新数据
					mOnRefreshListener.onRefresh();
				}
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 当刷新完成时回调
	 */
	public void onRefreshFinish() {
		if(isLoadMoring) {
			isLoadMoring = false;
			footerView.setPadding(0, -footerViewHeight, 0, 0);
		} else {
			mRefreshView.setPadding(0, -refreshViewHeight, 0, 0);
			currentState = DisplayMode.PULL_DOWN_REFRESH;
			ivArrow.setVisibility(View.VISIBLE);
			pbProgress.setVisibility(View.GONE);
			tvLastUpdateTime.setText("最后刷新时间" + getLastUpdateTime());
		}
	}
	
	/**
	 * 刷新头布局的状态
	 */
	private void refreshHeaderViewState() {
		if(currentState == DisplayMode.PULL_DOWN_REFRESH) {	// 下拉刷新状态
			ivArrow.startAnimation(downAnimation);
			tvState.setText("下拉刷新");
		} else if(currentState == DisplayMode.RELEASE_REFRESH) {	// 松开刷新
			ivArrow.startAnimation(upAnimation);
			tvState.setText("松开刷新");
		} else if(currentState == DisplayMode.REFRESHING) {		// 正在刷新
			ivArrow.clearAnimation();
			ivArrow.setVisibility(View.GONE);
			pbProgress.setVisibility(View.VISIBLE);
			tvState.setText("正在刷新");
		}
	}
	
	/**
	 * @author andong
	 * 头布局的状态
	 */
	public enum DisplayMode {
		PULL_DOWN_REFRESH,	// 下拉刷新
		RELEASE_REFRESH,	// 松开刷新
		REFRESHING			// 正在刷新中
	}
	
	/**
	 * 设置刷新事件
	 * @param listener
	 */
	public void setOnRefreshListener(OnRefreshListener listener) {
		this.mOnRefreshListener = listener;
	}
	
	/**
	 * 设置下拉刷新的开关
	 * @param isEnablePullToRefresh true:启用
	 */
	public void setPullToRefreshEnable(boolean isEnablePullToRefresh) {
		this.isEnablePullToRefresh = isEnablePullToRefresh;
	}
	
	/**
	 * 设置加载更多的开关
	 * @param isEnableLoadingMore true:启用
	 */
	public void setLoadingMoreEnable(boolean isEnableLoadingMore) {
		this.isEnableLoadingMore = isEnableLoadingMore;
	}
}
