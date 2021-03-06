package com.example.xkfeng.mycat.Activity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xkfeng.mycat.DrawableView.BottomDialog;
import com.example.xkfeng.mycat.DrawableView.IndexTitleLayout;
import com.example.xkfeng.mycat.DrawableView.PopupMenuLayout;
import com.example.xkfeng.mycat.R;
import com.example.xkfeng.mycat.Util.ActivityController;
import com.example.xkfeng.mycat.Util.DensityUtil;
import com.example.xkfeng.mycat.Util.UserAutoLoginHelper;
import com.suke.widget.SwitchButton;
import com.tencent.connect.UserInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.data.JPushLocalNotification;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.IntegerCallback;
import cn.jpush.im.api.BasicCallback;

import static cn.jpush.im.android.api.JMessageClient.FLAG_NOTIFY_DISABLE;
import static cn.jpush.im.android.api.JMessageClient.FLAG_NOTIFY_SILENCE;
import static cn.jpush.im.android.api.JMessageClient.FLAG_NOTIFY_WITH_LED;
import static cn.jpush.im.android.api.JMessageClient.FLAG_NOTIFY_WITH_SOUND;
import static cn.jpush.im.android.api.JMessageClient.FLAG_NOTIFY_WITH_VIBRATE;

public class SettingActivity extends BaseActivity {


    @BindView(R.id.indexTitleLayout)
    IndexTitleLayout indexTitleLayout;
    @BindView(R.id.tv_vibrationText)
    TextView tvVibrationText;
    @BindView(R.id.sb_vibrationBtn)
    SwitchButton sbVibrationBtn;
    @BindView(R.id.tv_promptText)
    TextView tvPromptText;
    @BindView(R.id.sb_promptBtn)
    SwitchButton sbPromptBtn;
    @BindView(R.id.tv_noDisturbText)
    TextView tvNoDisturbText;
    @BindView(R.id.sb_noDisturbBtn)
    SwitchButton sbNoDisturbBtn;
    @BindView(R.id.tv_roamingText)
    TextView tvRoamingText;
    @BindView(R.id.sb_roamingBtn)
    SwitchButton sbRoamingBtn;
    @BindView(R.id.view_divideView)
    View viewDivideView;
    @BindView(R.id.tv_modifyPasswordText)
    TextView tvModifyPasswordText;
    @BindView(R.id.tv_exitCurrentAccount)
    TextView tvExitCurrentAccount;

    private UserAutoLoginHelper userAutoLoginHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting_layout);
        ButterKnife.bind(this);

        userAutoLoginHelper = UserAutoLoginHelper.getUserAutoLoginHelper(getApplicationContext());


        initView();

    }

    private void initView() {

        setIndexTitleLayout();


        setSbPromptBtn();

        setSbNoDisturbBtn();

        setSbRoamingBtn();

        setSbVibrationBtn();
    }


    /**
     * 消息震动
     */
    private void setSbVibrationBtn() {

        sbVibrationBtn.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                userAutoLoginHelper.setVib(isChecked);
                setJpushStyle();
            }
        });

    }

    /**
     * 消息漫游
     */
    private void setSbRoamingBtn() {

        sbRoamingBtn.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                JMessageClient.init(getApplicationContext(), isChecked);
                userAutoLoginHelper.setRoaming(isChecked);

            }
        });
    }

    /**
     * 消息免打扰
     */
    private void setSbNoDisturbBtn() {

        sbNoDisturbBtn.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                userAutoLoginHelper.setPush(isChecked);
                setJpushStyle();
            }
        });


    }

    /**
     * 消息提示音
     */
    private void setSbPromptBtn() {

        sbPromptBtn.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                userAutoLoginHelper.setMusic(isChecked);
                setJpushStyle();
            }
        });
    }

    private void setJpushStyle() {
        if (userAutoLoginHelper.getPush()) {
            setNotification4();
        } else if (userAutoLoginHelper.getMusic() && userAutoLoginHelper.getVib()) {
            setNotification1();
        } else if (userAutoLoginHelper.getMusic()) {
            setNotification2();
        } else if (userAutoLoginHelper.getVib()) {
            setNotification3();
        } else{
            setNotification4();
        }
    }

    //自定义报警通知（震动铃声都要）
    public void setNotification1() {

        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(SettingActivity.this);
        builder.statusBarDrawable = R.mipmap.log;//消息栏显示的图标
        builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //设置为自动消失
        builder.notificationDefaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;// 设置为铃声与震动都要
        JPushInterface.setDefaultPushNotificationBuilder(builder);
    }

    //自定义报警通知（铃声）
    public void setNotification2() {
        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(SettingActivity.this);
        builder.statusBarDrawable = R.mipmap.log;//消息栏显示的图标</span>
        builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //设置为自动消失
        builder.notificationDefaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS;// 设置为铃声与震动都要
        JPushInterface.setDefaultPushNotificationBuilder(builder);
    }

    //自定义报警通知（震动）
    public void setNotification3() {
        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(SettingActivity.this);
        builder.statusBarDrawable = R.mipmap.log;
        //消息栏显示的图标
        builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //设置为自动消失
        builder.notificationDefaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;// 震动
        JPushInterface.setDefaultPushNotificationBuilder(builder);
    }

    //自定义报警通知（震动铃声都不要）
    public void setNotification4() {
        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(SettingActivity.this);
        builder.statusBarDrawable = R.mipmap.log;
        builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //设置为自动消失
        builder.notificationDefaults = Notification.DEFAULT_LIGHTS;// 设置为铃声与震动都不要
        JPushInterface.setDefaultPushNotificationBuilder(builder);
    }

    /**
     * 默认推送
     */
    public void setNotification5() {
        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(SettingActivity.this);
        JPushInterface.setDefaultPushNotificationBuilder(builder);
    }


    @OnClick(R.id.tv_modifyPasswordText)
    public void setTvModifyPasswordText(View view) {

        final String item1 = "";
        final String item2 = "确定修改";
        final String item3 = "取消";
        final BottomDialog dialog = new BottomDialog(SettingActivity.this, item1, item2, item3);
        dialog.setBackground(Color.WHITE);
        dialog.setItem1TextColor(1 , Color.BLACK);
        dialog.setItem1TextColor(2 , Color.BLACK);
        dialog.setItem1TextColor(3 , Color.BLACK);
        dialog.setItemClickListener(new BottomDialog.ItemClickListener() {
            @Override
            public void onItem1Click(View view) {
                dialog.dismiss();
            }

            @Override
            public void onItem2Click(View view) {

                //关闭弹出窗口
                dialog.dismiss();

                //转换到修改密码界面
                startActivity(new Intent(SettingActivity.this, ModifyPasswordActivity.class));
            }

            @Override
            public void onItem3Click(View view) {

                dialog.dismiss();
            }
        });
        dialog.show();

    }

    @OnClick(R.id.tv_exitCurrentAccount)
    public void setTvExitCurrentAccountClick(View view) {

        final String item1 = "";
        final String item2 = "确定退出";
        final String item3 = "取消";
        final BottomDialog dialog = new BottomDialog(SettingActivity.this, item1, item2, item3);
        dialog.setItemClickListener(new BottomDialog.ItemClickListener() {
            @Override
            public void onItem1Click(View view) {
                dialog.dismiss();
            }

            @Override
            public void onItem2Click(View view) {
                //极光账号登出
                JMessageClient.logout();

                //将当前所有Activity退栈
                ActivityController.finishAll();

                //转换到登陆界面
                startActivity(new Intent(SettingActivity.this, LoginActivity.class));

                //关闭弹出窗口
                dialog.dismiss();
            }

            @Override
            public void onItem3Click(View view) {

                dialog.dismiss();
            }
        });
        dialog.show();
    }


    /**
     * 设置顶部标题栏相关属性
     */
    private void setIndexTitleLayout() {

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


    @Override
    protected void onResume() {
        super.onResume();

        sbVibrationBtn.setChecked(userAutoLoginHelper.getVib());
        sbRoamingBtn.setChecked(userAutoLoginHelper.getRoaming());
        sbNoDisturbBtn.setChecked(userAutoLoginHelper.getPush());
        sbPromptBtn.setChecked(userAutoLoginHelper.getMusic());

        setJpushStyle();
    }
}
