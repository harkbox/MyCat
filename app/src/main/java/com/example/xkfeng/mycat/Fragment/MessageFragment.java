package com.example.xkfeng.mycat.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xkfeng.mycat.Activity.IndexActivity;
import com.example.xkfeng.mycat.Activity.SearchActivity;
import com.example.xkfeng.mycat.DrawableView.IndexTitleLayout;
import com.example.xkfeng.mycat.DrawableView.ListSlideView;
import com.example.xkfeng.mycat.DrawableView.PopupMenuLayout;
import com.example.xkfeng.mycat.DrawableView.RedPointView;
import com.example.xkfeng.mycat.DrawableView.RedPointViewHelper;
import com.example.xkfeng.mycat.Model.JPushMessageInfo;
import com.example.xkfeng.mycat.Model.MessageInfo;
import com.example.xkfeng.mycat.R;
import com.example.xkfeng.mycat.RecyclerDefine.EmptyRecyclerView;
import com.example.xkfeng.mycat.RecyclerDefine.QucikAdapterWrapter;
import com.example.xkfeng.mycat.RecyclerDefine.QuickAdapter;
import com.example.xkfeng.mycat.Util.DensityUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;

public class MessageFragment extends Fragment {

    @BindView(R.id.indexTitleLayout)
    IndexTitleLayout indexTitleLayout;
    @BindView(R.id.et_searchEdit)
    TextView etSearchEdit;
    @BindView(R.id.tv_messageEmptyView)
    TextView tvMessageEmptyView;

    Unbinder unbinder;

    @BindView(R.id.rv_messageRecyclerView)
    EmptyRecyclerView rvMessageRecyclerView;

    private View view;
    private static final String TAG = "MessageFragment";

    private DisplayMetrics metrics;
    private Context mContext;
    private QucikAdapterWrapter<MessageInfo> qucikAdapterWrapter;
    private QuickAdapter<MessageInfo> quickAdapter;

    public static int STATUSBAR_PADDING_lEFT;
    public static int STATUSBAR_PADDING_TOP;
    public static int STATUSBAR_PADDING_RIGHT;
    public static int STATUSBAR_PADDING_BOTTOM;

    private PopupMenuLayout popupMenuLayout_CONTENT;
    private PopupMenuLayout popupMenuLayout_MENU;


    private List<Conversation> conversationList;
    private Conversation conversation;

    private List<JPushMessageInfo> jPushMessageInfoList;
    private JPushMessageInfo jPushMessageInfo;

    private QucikAdapterWrapter<JPushMessageInfo> jpushQuickAdapterWrapter;
    private QuickAdapter<JPushMessageInfo> jpushQuickAdapter;

    private Handler handler;
    private Runnable runnable ;


    private static final int INT_NULL = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.message_fragment_layout, container, false);
        unbinder = ButterKnife.bind(this, view);
        metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mContext = getContext();

        /**
         * 注册事件接收
         */
//        JMessageClient.registerEventReceiver(this);

        return view;


    }

    @Override
    public void onStart() {
        super.onStart();
        /**
         * 定时拉取数据
         */

        handlerForTimer();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

         /*
            设置搜索栏相关属性
         */
        setEtSearchEdit();

         /*
           设置顶部标题栏相关属性
         */
        setIndexTitleLayout();

        /**
         * 初始化消息列表
         */
        initData();

        /**
         * 初始化RecyclerView的属性
         */
        initRecyclerView();

        /**
         * 设置消息列表
         */
//        setMessageList();


    }

    /**
     * 定时每两秒拉取一次数据
     */
    private void handlerForTimer() {
        if (handler == null){
            handler = new Handler() ;
        }
        if (runnable == null){
            runnable = new Runnable() {
                @Override
                public void run() {
                    initData();
                    handler.postDelayed(this , 2000) ;
                }
            };
        }
        handler.postDelayed( runnable ,2000);
    }


    /**
     * 使用情况：
     * 1，登陆的时候初始化
     * 将从JPush从获取的消息列表转到为JPushMessageInfo列表对象
     * 2，定时任务
     * 定时从Jpush上拉取数据，同步更新
     */
    private void initData() {

        conversationList = JMessageClient.getConversationList();

        if (jPushMessageInfoList == null) {
            jPushMessageInfoList = new ArrayList<>();
        } else {
            jPushMessageInfoList.clear();
        }

        for (Conversation conversation : conversationList) {
            jPushMessageInfo = new JPushMessageInfo();
            if (conversation.getLatestMessage().getContent().getContentType() == ContentType.prompt) {
                jPushMessageInfo.setContent(((PromptContent) conversation.getLatestMessage().getContent()).getPromptText());
            } else {
                jPushMessageInfo.setContent(((TextContent) conversation.getLatestMessage().getContent()).getText());
            }
            jPushMessageInfo.setMsgID(conversation.getId()); //消息ID
            jPushMessageInfo.setUserName(((UserInfo) conversation.getTargetInfo()).getUserName());//用户名
            jPushMessageInfo.setTitle(conversation.getTitle()); //标题
            jPushMessageInfo.setUnReadCount(conversation.getUnReadMsgCnt()+"");//当前会话未读消息数

            //规范化时间
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            jPushMessageInfo.setTime("Time " + sdf.format(conversation.getLatestMessage().getCreateTime()));
            //设置会话
            jPushMessageInfo.setConversation(conversation);
            //获取会话发送方的头像，没有则设置为默认头像
            if (conversation.getAvatarFile() == null) {
//                Log.d(TAG, "initData: null");
                jPushMessageInfo.setImg("");
            } else {
                jPushMessageInfo.setImg(conversation.getAvatarFile().toURI() + "");
            }
            //将数据添加到列表中
            jPushMessageInfoList.add(jPushMessageInfo);

            /**
             * 更新
             */
            if (jpushQuickAdapterWrapter != null)
                jpushQuickAdapterWrapter.notifyDataSetChanged();
        }

    }


    private void initRecyclerView() {

        List<String> list = new ArrayList<>();
        list.add("设置为置顶消息");
        list.add("删除");
        popupMenuLayout_CONTENT = new PopupMenuLayout(mContext, list, PopupMenuLayout.CONTENT_POPUP);

        jpushQuickAdapter = new QuickAdapter<JPushMessageInfo>(jPushMessageInfoList) {
            @Override
            public int getLayoutId(int viewType) {
                return R.layout.message_list_item;
            }

            @Override
            public void convert(VH vh, JPushMessageInfo data, int position) {

                ((TextView) vh.getView(R.id.tv_meessageTitle)).setText(data.getTitle());
                ((TextView) vh.getView(R.id.tv_messageContent)).setText(data.getContent());
                ((TextView) vh.getView(R.id.tv_meessageTime)).setText(data.getTime());

                RedPointViewHelper stickyViewHelper = new RedPointViewHelper(getContext() ,
                        ((View)vh.getView(R.id.redpoint_view_message)) ,R.layout.item_drag_view ) ;
                stickyViewHelper.setRedPointViewText(data.getUnReadCount());

                if (TextUtils.isEmpty(data.getImg()))
                {
                    ((ImageView) vh.getView(R.id.pciv_messageHeaderImage)).setImageResource(R.mipmap.log);
                }else {
                    ((ImageView) vh.getView(R.id.pciv_messageHeaderImage)).setImageURI(Uri.parse(data.getImg()));
                }

                ((ListSlideView) vh.getView(R.id.listlide)).setSlideViewClickListener(new ListSlideView.SlideViewClickListener() {
                    @Override
                    public void topViewClick(View view) {

                        Toast.makeText(mContext, "topViewClick", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void flagViewClick(View view) {
                        Toast.makeText(mContext, "flagViewClick", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void deleteViewClick(View view) {
                        Toast.makeText(mContext, "deleteViewClick", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void contentViewLongClick(View view) {

                        /**
                         * 弹框前，需要得到PopupWindow的大小(也就是PopupWindow中contentView的大小)。
                         * 由于contentView还未绘制，这时候的width、height都是0。
                         * 因此需要通过measure测量出contentView的大小，才能进行计算。
                         */
                        popupMenuLayout_CONTENT.getContentView().measure(DensityUtil.makeDropDownMeasureSpec(popupMenuLayout_CONTENT.getWidth()),
                                DensityUtil.makeDropDownMeasureSpec(popupMenuLayout_CONTENT.getHeight()));
                        ;
                        popupMenuLayout_CONTENT.showAsDropDown(view,
                                DensityUtil.getScreenWidth(getContext()) / 2 - popupMenuLayout_CONTENT.getContentView().getMeasuredWidth() / 2
                                , -view.getHeight() - popupMenuLayout_CONTENT.getContentView().getMeasuredHeight());

                    }

                    @Override
                    public void contentViewClick(View view) {

                        Toast.makeText(getContext(), "Message Click", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        jpushQuickAdapterWrapter = new QucikAdapterWrapter<JPushMessageInfo>(jpushQuickAdapter);


        View addView = LayoutInflater.from(getContext()).inflate(R.layout.ad_item_layout, null);
        jpushQuickAdapterWrapter.setAdView(addView);

        rvMessageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvMessageRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        rvMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        rvMessageRecyclerView.setmEmptyView(tvMessageEmptyView);
        rvMessageRecyclerView.setAdapter(jpushQuickAdapterWrapter);


    }

    /**
     * 消息列表内容的初始化
     */
    private void setMessageList() {

        List<String> list = new ArrayList<>();
        list.add("设置为置顶消息");
        list.add("删除");
        popupMenuLayout_CONTENT = new PopupMenuLayout(mContext, list, PopupMenuLayout.CONTENT_POPUP);

        List<MessageInfo> messageInfoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (i % 3 == 0) {
                MessageInfo messageInfo = new MessageInfo("", "Hello world!", "value ;" + i, "19:58", "12");
                messageInfoList.add(messageInfo);
            } else {
                MessageInfo messageInfo = new MessageInfo("", "Hello world!", "value ;" + i, "19:58", "false");
                messageInfoList.add(messageInfo);
            }
        }

        quickAdapter = new QuickAdapter<MessageInfo>(messageInfoList) {
            @Override
            public int getLayoutId(int viewType) {
                return R.layout.message_list_item;
            }

            @Override
            public void convert(VH vh, MessageInfo data, final int position) {

//           Toast.makeText(mContext, "data : " + position, Toast.LENGTH_SHORT).show();

                ((TextView) vh.getView(R.id.tv_meessageTitle)).setText(data.getTitle());
                ((TextView) vh.getView(R.id.tv_messageContent)).setText(data.getContent());
                ((TextView) vh.getView(R.id.tv_meessageTime)).setText(data.getTime());

                ((ListSlideView) vh.getView(R.id.listlide)).setStickyViewHelper(data.getMessageNotRead());

                ((ListSlideView) vh.getView(R.id.listlide)).setSlideViewClickListener(new ListSlideView.SlideViewClickListener() {
                    @Override
                    public void topViewClick(View view) {

                        Toast.makeText(mContext, "topViewClick", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void flagViewClick(View view) {
                        Toast.makeText(mContext, "flagViewClick", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void deleteViewClick(View view) {
                        Toast.makeText(mContext, "deleteViewClick", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void contentViewLongClick(View view) {

                        /**
                         * 弹框前，需要得到PopupWindow的大小(也就是PopupWindow中contentView的大小)。
                         * 由于contentView还未绘制，这时候的width、height都是0。
                         * 因此需要通过measure测量出contentView的大小，才能进行计算。
                         */
                        popupMenuLayout_CONTENT.getContentView().measure(DensityUtil.makeDropDownMeasureSpec(popupMenuLayout_CONTENT.getWidth()),
                                DensityUtil.makeDropDownMeasureSpec(popupMenuLayout_CONTENT.getHeight()));
                        ;
                        popupMenuLayout_CONTENT.showAsDropDown(view,
                                DensityUtil.getScreenWidth(getContext()) / 2 - popupMenuLayout_CONTENT.getContentView().getMeasuredWidth() / 2
                                , -view.getHeight() - popupMenuLayout_CONTENT.getContentView().getMeasuredHeight());

                    }

                    @Override
                    public void contentViewClick(View view) {

                        Toast.makeText(getContext(), "Message Click", Toast.LENGTH_SHORT).show();
                    }
                });

                List<String> list = new ArrayList<>();
                list.add("设置为置顶消息");
                list.add("删除");
                popupMenuLayout_CONTENT = new PopupMenuLayout(mContext, list, PopupMenuLayout.CONTENT_POPUP);

            }

        };

        qucikAdapterWrapter = new QucikAdapterWrapter<MessageInfo>(quickAdapter);
        View addView = LayoutInflater.from(getContext()).inflate(R.layout.ad_item_layout, null);
        qucikAdapterWrapter.setAdView(addView);

        rvMessageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvMessageRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        rvMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        rvMessageRecyclerView.setmEmptyView(tvMessageEmptyView);
        rvMessageRecyclerView.setAdapter(qucikAdapterWrapter);

    }

    /*
  设置搜索栏属性
  Drawable
 */
    private void setEtSearchEdit() {
        Drawable left = getResources().getDrawable(R.drawable.searcher);
        left.setBounds(metrics.widthPixels / 2 - DensityUtil.dip2px(mContext, 10 + 14 * 2), 0,
                50 + metrics.widthPixels / 2 - DensityUtil.dip2px(mContext, 10 + 14 * 2), 30);
        Log.d(TAG, "setEtSearchEdit: " + metrics.widthPixels);
        etSearchEdit.setCompoundDrawablePadding(-left.getIntrinsicWidth() / 2 + 5);
        etSearchEdit.setCompoundDrawables(left, null, null, null);
        etSearchEdit.setAlpha((float) 0.6);
        //点击转到搜索页面
        etSearchEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SearchActivity.class));
            }
        });

    }


    /**
     * 设置顶部标题栏相关属性
     */
    private void setIndexTitleLayout() {


//        设置内边距
//        其中left right bottom都用现有的
//        top设置为现在的topPadding+状态栏的高度
//        表现为将indexTitleLayout显示的数据放到状态栏下面
        indexTitleLayout.setPadding(indexTitleLayout.getPaddingLeft(),
                indexTitleLayout.getPaddingTop() + DensityUtil.getStatusHeight(mContext),
                indexTitleLayout.getPaddingRight(),
                indexTitleLayout.getPaddingBottom());

//        Log.d("UserInfoActivity", "setIndexTitleLayout: " + indexTitleLayout.getPaddingTop() + DensityUtil.getStatusHeight(mContext));
        STATUSBAR_PADDING_lEFT = indexTitleLayout.getPaddingLeft();
        STATUSBAR_PADDING_TOP = indexTitleLayout.getPaddingTop();
        STATUSBAR_PADDING_RIGHT = indexTitleLayout.getPaddingRight();
        STATUSBAR_PADDING_BOTTOM = indexTitleLayout.getPaddingBottom();


//        设置点击事件监听
        indexTitleLayout.setTitleItemClickListener(new IndexTitleLayout.TitleItemClickListener() {
            @Override
            public void leftViewClick(View view) throws Exception {
                /**
                 * 获取Activity中的抽屉对象并且打开抽屉
                 */
                ((IndexActivity) getActivity()).getDrawerLayout().openDrawer(Gravity.LEFT);
            }

            @Override
            public void middleViewClick(View view) {

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void rightViewClick(View view) {
                Toast.makeText(mContext, "RightClick", Toast.LENGTH_SHORT).show();
//                //创建弹出式菜单对象（最低版本11）
//                PopupMenu popup = new PopupMenu(getContext(), view);//第二个参数是绑定的那个view
//
//
//                popup.getMenu().add("创建群聊").setIcon(R.drawable.create_group_chat);
//
//                //获取菜单填充器
//                MenuInflater inflater = popup.getMenuInflater();
//                //填充菜单
//                inflater.inflate(R.menu.add_friend, popup.getMenu());
//
//                //绑定菜单项的点击事件
//                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        return false;
//                    }
//                });
//
//                //使用反射，强制显示菜单图标
//                try {
//                    Field field = popup.getClass().getDeclaredField("mPopup");
//                    field.setAccessible(true);
//                    MenuPopupHelper mHelper = (MenuPopupHelper) field.get(popup);
//                    mHelper.setForceShowIcon(true);
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (NoSuchFieldException e) {
//                    e.printStackTrace();
//                }
//
//                //显示(这一行代码不要忘记了)
//                popup.show();

                List<String> list = new ArrayList<>();
                list.add("创建群聊");
                list.add("加好友/群");
                list.add("扫一扫");
                popupMenuLayout_MENU = new PopupMenuLayout(getContext(), list, PopupMenuLayout.MENU_POPUP);
//                popupMenuLayout_MENU.setContentView(indexTitleLayout);
//                Log.d(TAG, "rightViewClick: " + indexTitleLayout.getChildCount());
                popupMenuLayout_MENU.showAsDropDown(indexTitleLayout, DensityUtil.getScreenWidth(getContext())
                                - popupMenuLayout_MENU.getWidth() - DensityUtil.dip2px(getContext(), 5)
                        , DensityUtil.dip2px(getContext(), 5));


            }
        });
    }


    /**
     * 设置滑动View的相关属性
     */
    private void setSlideView() {

        List<String> list = new ArrayList<>();
        list.add("设置为置顶消息");
        list.add("删除");
        popupMenuLayout_CONTENT = new PopupMenuLayout(mContext, list, PopupMenuLayout.CONTENT_POPUP);

//        listSlideView = (ListSlideView) view.findViewById(R.id.listlide);
//        listSlideView.setSlideViewClickListener(new ListSlideView.SlideViewClickListener() {
//            @Override
//            public void topViewClick(View view) {
//
//                Toast.makeText(mContext, "topViewClick", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void flagViewClick(View view) {
//                Toast.makeText(mContext, "flagViewClick", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void deleteViewClick(View view) {
//                Toast.makeText(mContext, "deleteViewClick", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void contentViewLongClick(View view) {
//
//                /**
//                 * 弹框前，需要得到PopupWindow的大小(也就是PopupWindow中contentView的大小)。
//                 * 由于contentView还未绘制，这时候的width、height都是0。
//                 * 因此需要通过measure测量出contentView的大小，才能进行计算。
//                 */
//                popupMenuLayout_CONTENT.getContentView().measure(DensityUtil.makeDropDownMeasureSpec(popupMenuLayout_CONTENT.getWidth()),
//                        DensityUtil.makeDropDownMeasureSpec(popupMenuLayout_CONTENT.getHeight()));
//                ;
//                popupMenuLayout_CONTENT.showAsDropDown(view,
//                        DensityUtil.getScreenWidth(getContext()) / 2 - popupMenuLayout_CONTENT.getContentView().getMeasuredWidth() / 2
//                        , -view.getHeight() - popupMenuLayout_CONTENT.getContentView().getMeasuredHeight());
//
//            }
//
//            @Override
//            public void contentViewClick(View view) {
//
//                Toast.makeText(getContext(), "Message Click", Toast.LENGTH_SHORT).show();
//            }
//        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


}