package com.example.xkfeng.mycat.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.suke.widget.SwitchButton;
import com.tencent.connect.UserInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.im.android.api.JMessageClient;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting_layout);
        ButterKnife.bind(this);

        initView();

    }

    private void initView() {

        setIndexTitleLayout();


    }

    @OnClick(R.id.tv_modifyPasswordText)
    public void setTvModifyPasswordText(View view) {

        final String item1 = "";
        final String item2 = "确定修改";
        final String item3 = "取消";
        final BottomDialog dialog = new BottomDialog(SettingActivity.this, item1, item2, item3);
        dialog.setItemClickListener(new BottomDialog.ItemClickListener() {
            @Override
            public void onItem1Click(View view) {
                dialog.dismiss();
            }

            @Override
            public void onItem2Click(View view) {

                //转换到修改密码界面
                Toast.makeText(SettingActivity.this, "修改密码", Toast.LENGTH_SHORT).show();
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
                startActivity(new Intent(SettingActivity.this , LoginActivity.class));

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


}
