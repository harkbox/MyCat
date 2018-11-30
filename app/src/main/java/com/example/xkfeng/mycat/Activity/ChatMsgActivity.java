package com.example.xkfeng.mycat.Activity;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.xkfeng.mycat.DrawableView.IndexTitleLayout;
import com.example.xkfeng.mycat.DrawableView.KeyBoradRelativeLayout;
import com.example.xkfeng.mycat.Fragment.AddBoradFragment;
import com.example.xkfeng.mycat.Fragment.NullBoradFragment;
import com.example.xkfeng.mycat.Fragment.VoiceBoradFragment;
import com.example.xkfeng.mycat.R;
import com.example.xkfeng.mycat.Util.DensityUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import io.github.rockerhieu.emojicon.EmojiconEditText;
import io.github.rockerhieu.emojicon.EmojiconGridFragment;
import io.github.rockerhieu.emojicon.EmojiconsFragment;
import io.github.rockerhieu.emojicon.emoji.Emojicon;


public class ChatMsgActivity extends BaseActivity implements
        EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener,
        KeyBoradRelativeLayout.KeyBoradStateListener {

    enum SendOrAdd{
        send , add
    };

    private static final String TAG = "ChatMsgActivity";
    @BindView(R.id.rl_rootLayoutView)
    KeyBoradRelativeLayout rlRootLayoutView;
    @BindView(R.id.indexTitleLayout)
    IndexTitleLayout indexTitleLayout;
    @BindView(R.id.editEmojicon)
    EmojiconEditText editEmojicon;
    @BindView(R.id.iv_sendImage)
    ImageView ivSendImage;
    @BindView(R.id.iv_chatVoiceImg)
    ImageView ivChatVoiceImg;
    @BindView(R.id.iv_chatEmojiImg)
    ImageView ivChatEmojiImg;
    @BindView(R.id.rv_messageRecyclerView)
    RecyclerView rvMessageRecyclerView;
    @BindView(R.id.fl_keyBroadLayout)
    FrameLayout flKeyBroadLayout;

    //附加Fragment
    private AddBoradFragment addBoradFragment ;
    //录音Fragment
    private VoiceBoradFragment voiceFragment;
    //空布局的fragment
    private NullBoradFragment nullBoradFragment;
    //控制系统软键盘的显示和隐藏
    private InputMethodManager inputMethodManager;
    //系统软键盘高度
    private static int KEY_BROAD_HEIGHT = 770;
    //默认最小的软键盘高度阈值
    private static int MIN_KEYBROAD_HITGHT = 100;
    //emoji表情键盘是否打开
    private boolean emojiKeyBroadIsOpen = false;
    //录音fragment是否打开
    private boolean voiceBroadIsOpen = false;
    //addBroadFragment是否打开
    private boolean addBroadIsOpen = false ;
    //系统软键盘是否打开
    private boolean systemSoftKeyBoradIsOpen = false;
    //会话
    private Conversation conversation;
    //会话列表
    private List<Message> messageList;
    // 如果有数据记录那么会修改为static
    // send：表示发送功能
    // add：表示附加功能
    // 默认为add
    private int sendOrAdd = SendOrAdd.add.ordinal();

    //    【A】stateUnspecified：软键盘的状态并没有指定，系统将选择一个合适的状态或依赖于主题的设置
//　　【B】stateUnchanged：当这个activity出现时，软键盘将一直保持在上一个activity里的状态，无论是隐藏还是显示
//　　【C】stateHidden：用户选择activity时，软键盘总是被隐藏
//　　【D】stateAlwaysHidden：当该Activity主窗口获取焦点时，软键盘也总是被隐藏的
//　　【E】stateVisible：软键盘通常是可见的
//　　【F】stateAlwaysVisible：用户选择activity时，软键盘总是显示的状态
//　　【G】adjustUnspecified：默认设置，通常由系统自行决定是隐藏还是显示
//　　【H】adjustResize：该Activity总是调整屏幕的大小以便留出软键盘的空间
//　　【I】adjustPan：当前窗口的内容将自动移动以便当前焦点从不被键盘覆盖和用户能总是看到输入内容的部分
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_message_layout);
        ButterKnife.bind(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        initTitleView();
        initInputView();
        initMessageView();
    }

    //初始化顶部标题
    private void initTitleView() {

        //沉浸式状态栏
        DensityUtil.fullScreen(this);

//        设置内边距
//        其中left right bottom都用现有的
//        top设置为现在的topPadding+状态栏的高度
//        表现为将indexTitleLayout显示的数据放到状态栏下面
        indexTitleLayout.setPadding(indexTitleLayout.getPaddingLeft(),
                indexTitleLayout.getPaddingTop() + DensityUtil.getStatusHeight(this),
                indexTitleLayout.getPaddingRight(),
                indexTitleLayout.getPaddingBottom());


//        设置点击事件监听
        indexTitleLayout.setTitleItemClickListener(new IndexTitleLayout.TitleItemClickListener() {
            @Override
            public void leftViewClick(View view) throws Exception {
                /**
                 * 退出当前Activity
                 */

                finish();
            }

            @Override
            public void middleViewClick(View view) {

            }

            @Override
            public void rightViewClick(View view) {

            }
        });
    }


    /**
     * 初始化输入键盘
     */
    private void initInputView() {

        //根布局相关设置
        setRlRootLayoutView();

        //设置EmojiEdit相关属性
        setEditEmojionView();

        addBoradFragment = new AddBoradFragment();
        voiceFragment = new VoiceBoradFragment();
        nullBoradFragment = new NullBoradFragment();
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


    }

    /**
     * 初始化消息列表
     */
    private void initMessageView() {
        conversation = JMessageClient.getSingleConversation(getIntent().getStringExtra("userName"));

        if (conversation != null) {

            messageList = conversation.getAllMessage();

            Log.d(TAG, "initView: " + messageList.get(0).getCreateTime());
        }
    }

    /**
     * 根布局相关设置
     */
    private void setRlRootLayoutView() {

        //监听系统键盘的打开或者关闭
        //并且做出一些调整
        rlRootLayoutView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (getSupportSoftInputHeight() > MIN_KEYBROAD_HITGHT) {
                    systemSoftKeyBoradIsOpen = true;
                } else {
                    /**
                     * 源代码
                     * if(!hasFragmentOpen())
                     * BUG：
                     *    每次进入该Activity，第一部点击emojiEdit，第二部点击返回，会发现系统软键盘撤销
                     * 但是我们frameLayout的Height仍然为系统软件盘的高度，且以空白布局呈现在eojiEdit下，而且界面有重绘，存在偏移现象
                     *
                     * 原因：
                     *    经过反复测试，每次进入该Activity时，系统软键盘并不会显示，那么就是直接调用
                     *    setNullInput()方法，在该方法中会设置FrameLayout的高度为1，但是在我们第一步点击emojiEdit的时候又会设置
                     *    FrameLayout的高度为系统软键盘高度。
                     *解决办法：
                     *    让setNullInput只在经过了键盘显示之后才能调用。
                     */
                    if (!hasFragmentOpen() && systemSoftKeyBoradIsOpen) {
                        setNullInput();
                    }
                    systemSoftKeyBoradIsOpen = false;

                }
            }
        });


        /**
         ****废弃****
         * 绑定监听事件
         * 监听系统软键盘是否打开
         * ****无法实时监听系统软键盘的打开与否。已废弃：
         * 原因：
         * 当设置了InputMode为WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
         * 且底部始终放置了一个跟系统软键盘一样高度的frameLayout（大部分时间高度一致）
         * 所以所居的onSizeChange不会完全随着软键盘的显示或者隐藏而发生改动。
         * 那么也就没法实时监听软键盘的打开/关闭状态
         *
         */
        rlRootLayoutView.setKeyBoradStateListener(this);
    }

    /**
     * 设置emojiEdit相关属性
     */
    private void setEditEmojionView() {

        /**
         * 当每次进入当前界面的时候，
         * 先点击emoji表情，再点击emojiEdit出现BUG，
         * 具体表现为：
         *        系统软件盘弹出，但emoji表情键盘不消失，布局在系统软件盘上面
         * 问题所在：
         *        这种情况下第一次点击emojiEdit无法不会执行onClick。
         * 解决思路和方法:
         *        虽然emojiEdit不会执行onClick方法，但是存在焦点的获取。
         *        监听emojiEdit的焦点获取情况，
         *        如果时第一次获取焦点，
         *        就直接按点击事件来处理
         */

        editEmojicon.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    onEditEmojionFocused(v);
                }
            }
        });

        /**
         * 随文本变动，
         * 界面布局，
         * 部分按钮的功能都要随之改变
         */
        editEmojicon.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //如果为空
                if (TextUtils.isEmpty(s)){
                    sendOrAdd = SendOrAdd.add.ordinal() ;
                    ivSendImage.setImageResource(R.drawable.ic_add_gray);
                }else {
                    sendOrAdd = SendOrAdd.send.ordinal() ;
                    ivSendImage.setImageResource(R.drawable.ic_send_blue);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @OnClick({R.id.iv_sendImage, R.id.iv_chatVoiceImg,
            R.id.iv_chatEmojiImg, R.id.editEmojicon})
    public void onIvClick(View view) {
        switch (view.getId()) {
            case R.id.editEmojicon:
                //关闭掉emoji表情键盘
                onEditEmojionFocused(view);
                break;
            case R.id.iv_sendImage:
                if(sendOrAdd == SendOrAdd.send.ordinal()){
                    /**
                     * 走消息发送的逻辑
                     */

                }else {
                    /**
                     * 走打开特殊消息布局的逻辑
                     */
                    if (addBroadIsOpen){
                        isOpenAddBorad(false);
                    }else {
                        isOpenAddBorad(true);
                    }
                }
                break;
            case R.id.iv_chatVoiceImg:

                if (voiceBroadIsOpen) {
                    isOpenVoiceBorad(false);
                } else {
                    // 如果当前存在有其它Fragment处于打开的状态
                    // 直接切换界面
                    isOpenVoiceBorad(true);
                }
                break;

            case R.id.iv_chatEmojiImg:

                if (emojiKeyBroadIsOpen == true) {
                    //关闭
                    isOpenEmojiBorad(view, false);
                } else if (emojiKeyBroadIsOpen == false) {
                    //打开
                    isOpenEmojiBorad(view, true);
                }
                break;

        }
    }


    /**
     * 1 设置底部fragmeLayout高度和系统软件盘高度一致
     * 2 复原所有已经打开的fragment
     * 3 打开系统软键盘
     *
     * @param view editEmoji
     */
    private void onEditEmojionFocused(View view) {
        flKeyBroadLayout.getLayoutParams().height = KEY_BROAD_HEIGHT;
        recoveryFragment();
        showSoftInput(view);
    }


    private void isOpenAddBorad(boolean isOpen){

        if (isOpen){
            //清除焦点
            editEmojicon.clearFocus();
            //关闭系统软键盘
            hideSoftInput(editEmojicon);

            //判断是否有正在正在显示fragment，
            //如果有将其样式回复
            if (!recoveryFragment()) { }
            //改变样式
            ivSendImage.setImageResource(R.drawable.ic_add_blue);
            //设置布局属性
            flKeyBroadLayout.getLayoutParams().height = KEY_BROAD_HEIGHT;
            flKeyBroadLayout.setVisibility(View.VISIBLE);
            //设置add键盘状态为打开
            addBroadIsOpen = true;
            //打开add键盘
            setAddInput() ;
        } else {
            //改变样式
            ivSendImage.setImageResource(R.drawable.ic_add_gray);
            //设置add键盘状态为未打开
            addBroadIsOpen = false;
            flKeyBroadLayout.getLayoutParams().height = 1;
            //关闭voice键盘
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_keyBroadLayout, nullBoradFragment)
                    .commit();
        }

    }

    /**
     * 打开还是关闭Voice-record界面
     *
     * @param isOpen true：打开  flase：关闭
     */
    private void isOpenVoiceBorad(boolean isOpen) {
        if (isOpen) {

            //清除焦点
            editEmojicon.clearFocus();
            //关闭系统软键盘
            hideSoftInput(editEmojicon);

            //判断是否有正在正在显示fragment，
            //如果有将其样式回复
            if (!recoveryFragment()) {
            }
            //改变样式
            ivChatVoiceImg.setImageResource(R.drawable.ic_voice_blue);
            //设置布局属性
            flKeyBroadLayout.getLayoutParams().height = KEY_BROAD_HEIGHT;
            flKeyBroadLayout.setVisibility(View.VISIBLE);
            //设置voice键盘状态为打开
            voiceBroadIsOpen = true;
            //打开Voice键盘
            setVoiceInput();
        } else {
            //改变样式
            ivChatVoiceImg.setImageResource(R.drawable.ic_voice_gray);
            //设置voice键盘状态为未打开
            voiceBroadIsOpen = false;
            flKeyBroadLayout.getLayoutParams().height = 1;
            //关闭voice键盘
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_keyBroadLayout, nullBoradFragment)
                    .commit();
        }
    }

    /**
     * 打开还是关闭Emoji表情键盘
     *
     * @param view   绑定的控件
     * @param isOpen 是否打开
     */
    private void isOpenEmojiBorad(View view, boolean isOpen) {

        if (isOpen) {
            //清除焦点
            editEmojicon.clearFocus();
            //关闭系统软键盘
            hideSoftInput(editEmojicon);
            //判断是否有正在正在显示fragment，
            //如果有将其样式回复
            if (!recoveryFragment()) {
            }
            //改变样式
            ivChatEmojiImg.setImageResource(R.drawable.ic_emoji_blue);
            //设置布局属性
            flKeyBroadLayout.getLayoutParams().height = KEY_BROAD_HEIGHT;
            flKeyBroadLayout.setVisibility(View.VISIBLE);
            //设置emoji键盘状态为打开
            emojiKeyBroadIsOpen = true;
            //显示emoji表情键盘
            setEmojiInput(false);
        } else {
            //改变样式
            ivChatEmojiImg.setImageResource(R.drawable.ic_emoji_gray);
            //关闭系统软键盘
//            hideSoftInput(ivChatEmojiImg);
            //设置emoji键盘状态为未打开
            emojiKeyBroadIsOpen = false;
            flKeyBroadLayout.getLayoutParams().height = 1;

            //关闭emoji键盘
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_keyBroadLayout, nullBoradFragment)
                    .commit();
        }

    }

    /**
     * @param view
     */
    private void showSoftInput(View view) {
        inputMethodManager.showSoftInput(view, 0);
    }

    /**
     * @param view
     */
    private void hideSoftInput(View view) {
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 1 判断当前是否有Fragment处于打开状态
     * 2 将已经打开的Fragment对应的Image的图片复原
     * 3 在逻辑上将已经打开的Fragment标记为关闭状态
     *
     * @return true：存在打开的Fragment  false：当前没有打开的Fragemt
     */
    private boolean recoveryFragment() {
        boolean flag = false;
        if (emojiKeyBroadIsOpen) {
            flag = true;
            emojiKeyBroadIsOpen = false;
            ivChatEmojiImg.setImageResource(R.drawable.ic_emoji_gray);
        } else if (voiceBroadIsOpen) {
            flag = true;
            voiceBroadIsOpen = false;
            ivChatVoiceImg.setImageResource(R.drawable.ic_voice_gray);
        }else if (addBroadIsOpen){
            flag = true;
            addBroadIsOpen = false;
            ivSendImage.setImageResource(R.drawable.ic_add_gray);

        }
        return flag;
    }

    /**
     * 判断当前是否有fragment打开
     *
     * @return true已有fragment打开， false没有fragment打开
     */
    private boolean hasFragmentOpen() {
        boolean flag = false;
        if (emojiKeyBroadIsOpen) {
            flag = true;
        } else if (voiceBroadIsOpen) {
            flag = true;
        }else if (addBroadIsOpen){
            flag = true ;
        }
        return flag;
    }


    /**
     * 设置布局表情键盘，特殊消息键盘的高度
     *
     * @param height 高度
     */
    private void setSupportSoftInputHeight(int height) {
        ViewGroup.LayoutParams params = flKeyBroadLayout.getLayoutParams();
        params.height = height;

    }

    /**
     * 获取Android手机手机软键盘的高度
     *
     * @return 手机软键盘的高度
     */
    private int getSupportSoftInputHeight() {

        Rect r = new Rect();
        //获取当前界面可视部分
        getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        //获取屏幕的高度
        int screenHeight = getWindow().getDecorView().getRootView().getHeight();
        //此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数
        int heightDifference = screenHeight - r.bottom;

        return heightDifference;
//        //获取屏幕可见高度
//        Rect rect = new Rect();
//        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
//        //获取屏幕高度
//        int screeenHeight = getWindow().getDecorView().getRootView().getHeight();
//        Log.d(TAG, "getSupportSoftInputHeight: screenHeight : " + screeenHeight + "  rect : " + rect);
//        int softInputHeight = screeenHeight - rect.bottom;
//        if (Build.VERSION.SDK_INT >= 18) {
//            softInputHeight = screeenHeight - getSoftBottomBarHeight();
//        }
//        return softInputHeight;
    }

    /**
     * 获取Android底部虚拟键的高度
     *
     * @return 底部虚拟键高度
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int getSoftBottomBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int useHeight = metrics.heightPixels;
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > useHeight) {
            return realHeight - useHeight;
        } else {
            return 0;
        }

    }


    /**
     * 设置显示Add界面
     */
    private void setAddInput(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_keyBroadLayout , addBoradFragment)
                .commit() ;
    }

    /**
     * 设置显示Voice界面
     */
    private void setVoiceInput() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_keyBroadLayout, voiceFragment)
                .commit();
    }

    /**
     * 设置显示emoji表情
     *
     * @param useSystemDefault true or false
     */
    private void setEmojiInput(boolean useSystemDefault) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_keyBroadLayout, EmojiconsFragment.newInstance(useSystemDefault))
                .commit();
    }

    /**
     * 设置隐藏额外键盘布局
     */
    private void setNullInput() {
        //关闭voice键盘
        ViewGroup.LayoutParams params = flKeyBroadLayout.getLayoutParams();
        params.height = 1;
        flKeyBroadLayout.setLayoutParams(params);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_keyBroadLayout, nullBoradFragment)
                .commit();

    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(editEmojicon, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(editEmojicon);
    }

    /**
     * 键盘点击监听处理
     *
     * @param keyCode 键盘按键
     * @param event   点击事件
     * @return true or false
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "onKeyDown: back");
            if (hasFragmentOpen() == true) {

                if (emojiKeyBroadIsOpen)
                {
                    isOpenEmojiBorad(editEmojicon, false);
                }else if (addBroadIsOpen){
                    isOpenAddBorad(false);
                }else if (voiceBroadIsOpen){
                    isOpenVoiceBorad(false);
                }
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 废弃
     * @param state
     */
    @Deprecated
    @Override
    public void stateChange(int state) {
        switch (state) {
            case KeyBoradRelativeLayout.KEY_BORAD_HIDE:
                break;

            case KeyBoradRelativeLayout.KEY_BORAD_SHOW:
                break;
        }
    }
}