package com.example.xkfeng.mycat.DrawableView;

import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.xkfeng.mycat.R;
import com.example.xkfeng.mycat.Util.DensityUtil;

import org.w3c.dom.Attr;

import butterknife.internal.ListenerClass;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.xkfeng.mycat.R.layout.message_item;

public class ListSlideView extends HorizontalScrollView {

    //内容
    private RelativeLayout relativeLayout;

    //置顶
    private TextView topView;

    //标记读取与否
    private TextView markReadView;

    //是否有标记读取与否的标志 （群助手提示消息没有，个人聊天和群聊有）
    private Boolean isMarkReadFlag = true;

    //删除
    private TextView deleteView;

    //滚动距离
    private int mScrollWidth;

    //按钮菜单是否代开
    private Boolean isOpen = false;

    //是否第一次测量（只在onMeasure中调用一次）
    private Boolean once = false;
    /**
     * 消息来源用户头像，群头像
     */
    private CircleImageView pciv_messageHeaderImage;
    /**
     * 用户ID，群标题
     */
    private TextView tv_meessageTitle;
    /**
     * 消息内容
     */
    private TextView tv_messageContent;
    /**
     * 最近消息的时间
     */
    private TextView tv_meessageTime;


    //自定义接口，将事件传递出去
    private SlideViewClickListener slideViewClickListener;
    private RedPointerViewListner redPointerViewListner ;


    private static final String TAG = "ListSlideView";
    private Context mContext;

    private RedPointViewHelper stickyViewHelper;

    public ListSlideView(Context context) {
        this(context, null);

    }

    public ListSlideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListSlideView(Context context, AttributeSet attrs, int str) {
        super(context, attrs, str);
        mContext = context;
        this.setOverScrollMode(OVER_SCROLL_NEVER);


    }


    /**
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!once) {
            once = true;
            int width = DensityUtil.getScreenWidth(mContext);

            /**
             * 设置View宽度为屏幕宽度
             */
            relativeLayout = (RelativeLayout) findViewById(R.id.rl_contentLayout);

            /**
             * 提高用户体验
             * 点击触感
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                relativeLayout.setBackground(mContext.getDrawable(R.drawable.state_pressed_drawable));
            }
            /**
             * 设置红顶啊拖拽
             */
            View redPointMessage = relativeLayout.findViewById(R.id.redpoint_view_message);
            stickyViewHelper = new RedPointViewHelper(mContext, redPointMessage, R.layout.item_drag_view);
            stickyViewHelper.setRedPointViewReleaseOutRangeListener(new RedPointViewHelper.RedPointViewReleaseOutRangeListener() {
                @Override
                public void onReleaseOutRange() {

                    if (redPointerViewListner != null){
                        redPointerViewListner.onRedPointerClickRealeaseOutRange();
                    }
                }

                @Override
                public void onRedViewClickDown() {

                    if (redPointerViewListner != null){
                        redPointerViewListner.onRedPointerClickDown();
                    }
                }

                @Override
                public void onRedViewCLickUp() {

                    if (redPointerViewListner != null){
                        redPointerViewListner.onRedPointerClickUp();
                    }
                }
            });

            /**
             * 消息对象内容的获取
             */
            pciv_messageHeaderImage = relativeLayout.findViewById(R.id.pciv_messageHeaderImage);
            tv_meessageTitle = relativeLayout.findViewById(R.id.tv_meessageTitle);
            tv_messageContent = relativeLayout.findViewById(R.id.tv_messageContent);
            tv_meessageTime = relativeLayout.findViewById(R.id.tv_meessageTime);


            ViewGroup.LayoutParams lp = relativeLayout.getLayoutParams();
            lp.width = width;
            relativeLayout.setLayoutParams(lp);


            relativeLayout.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (slideViewClickListener != null && !isOpen) {
                        slideViewClickListener.contentViewLongClick(v);
                        return true;
                    }
                    return false;

                }
            });
            relativeLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (slideViewClickListener != null ) {
                        if (isOpen){
                            closeSideSlide();
                            return ;
                        }
                        slideViewClickListener.contentViewClick(v);
                    }
                }
            });

            /**
             * 点击事件用接口回调方式传出
             */
            topView = (TextView) findViewById(R.id.tv_topSlideView);
            topView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (slideViewClickListener != null) {
                        slideViewClickListener.topViewClick(v);
                    }
                }
            });

            markReadView = (TextView) findViewById(R.id.tv_flagSlideView);
            markReadView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (slideViewClickListener != null) {
                        slideViewClickListener.flagViewClick(v);
                    }
                }
            });

            deleteView = (TextView) findViewById(R.id.tv_deleteSlideView);
            deleteView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (slideViewClickListener != null)
                        slideViewClickListener.deleteViewClick(v);
                }
            });

        }

    }


    /**
     * 让Item在每次布局改变的时候（change==true）回到初始位置，
     * 并且获取滚动条可移动的距离
     *
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (changed) {
            //回到初始位置
            this.scrollTo(0, 0);
            //获取滚动条可以滚动的距离
            //需要根据markReadView是否可见进行判断
            if (markReadView.getVisibility() != View.GONE) {

                mScrollWidth = topView.getWidth() + markReadView.getWidth() + deleteView.getWidth();
            } else {

                mScrollWidth = topView.getWidth() + deleteView.getWidth();

            }
        }

    }

    //滑动监听，
    // 按滑动的距离大小控制菜单开关
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN://按下

                break ;
            case MotionEvent.ACTION_MOVE://移动

                break;
            case MotionEvent.ACTION_UP://松开
            case MotionEvent.ACTION_CANCEL:

                changeScrollX();
                return true;
            default:
                break;
        }
        super.onTouchEvent(ev);
        return true;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        //改变View在X轴方向上的位置
        topView.setTranslationX(1);
    }

    public void changeScrollX() {
//        Log.d(TAG, "changeScrollX: ");
        if (getScrollX() >= (mScrollWidth / 2)) {
            this.smoothScrollTo(mScrollWidth, 0);
            isOpen = true;
        } else {
            this.smoothScrollTo(0, 0);
            isOpen = false;


        }

        stickyViewHelper.isResponseClickEvent(!isOpen);

    }

    public void closeSideSlide(){
        this.smoothScrollTo(0,0);
    }

    /**
     * 外部调用设置接口
     *
     * @param slideViewClickListener
     */
    public void setSlideViewClickListener(SlideViewClickListener slideViewClickListener) {
        this.slideViewClickListener = slideViewClickListener;

    }

    public void setRedPointerViewListner(RedPointerViewListner redPointerViewListner){
        this.redPointerViewListner = redPointerViewListner ;
    }


    public interface RedPointerViewListner{
        public void onRedPointerClickDown() ;

        public void onRedPointerClickRealeaseOutRange() ;

        public void onRedPointerClickUp() ;
    }

    /**
     * 自定义接口
     */
    public interface SlideViewClickListener {
        //点击置顶View
        public void topViewClick(View view);

        //点击标志View
        public void flagViewClick(View view);

        //点击删除View
        public void deleteViewClick(View view);

        public void contentViewLongClick(View view);

        public void contentViewClick(View view);
    }

    /**
     * slide view 是否处于打开状态
     * isOpen = true 打开
     * isOpen = false 关闭
     */
    public interface SlideViewIsOpenListener{

        public void isOpen(boolean isOpen) ;
    }

}
