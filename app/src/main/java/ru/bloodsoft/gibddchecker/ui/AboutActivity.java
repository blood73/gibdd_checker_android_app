package ru.bloodsoft.gibddchecker.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.bloodsoft.gibddchecker.App;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.util.RunCounts;

public class AboutActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = false;

    private LinearLayout mTitleContainer;
    private TextView mTitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private Activity activity;

    @BindView(R.id.copy_code)
    Button copyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        activity = this;
        bindActivity();

        mAppBarLayout.addOnOffsetChangedListener(this);
        mToolbar.inflateMenu(R.menu.menu_main);
        startAlphaAnimation(mTitle, 0, View.INVISIBLE);

        TextView appVersionText = (TextView) activity.findViewById(R.id.about_version);
        String appVersion = activity.getResources().getString(R.string.version) + getCurrentVersion(activity);
        RunCounts settings = new RunCounts();
        Boolean isAdFree = settings.isAdFree();

        if (isAdFree) {
            appVersion += " PRO";
        }

        appVersionText.setText(appVersion);

        final Button share_button = (Button) findViewById(R.id.button_share);

        share_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // share
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, activity.getResources().getString(R.string.app_name));
                    String sAux = "\n" + activity.getResources().getString(R.string.share_text) + "\n\n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id=" + activity.getPackageName() + "\n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, activity.getResources().getString(R.string.share_text_choose)));
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final Button rate_button = (Button) findViewById(R.id.button_rate);

        rate_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // rate
                Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
                }
            }
        });

        ButterKnife.bind(this);

        copyCode.setText(AboutActivity.this.getResources().getString(R.string.code_support, settings.getSSAD()));
    }

    @OnClick(R.id.send_message)
    public void onOpenBrowserClicked(View view) {
        Toast.makeText(AboutActivity.this, AboutActivity.this.getResources().getString(R.string.rate_app_feedback_sent), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_SEND);
        RunCounts settings = new RunCounts();

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"antiperekup.app@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Антиперекуп. Отзыв");
        intent.putExtra(Intent.EXTRA_TEXT, "\n\n\n\n\n\n\n" +
                "\n===========================\n" +
                "Не удаляйте следующую информацию\n" +
                settings.getSSAD()
        );

        intent.setType("message/rfc822");

        startActivity(Intent.createChooser(intent, "Выберите ваш email-клиент для отправки письма:"));
    }

    @OnClick(R.id.button_policy)
    public void onOpenPolicy(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(AboutActivity.this).create();
        alertDialog.setTitle(AboutActivity.this.getResources().getString(R.string.button_policy));
        alertDialog.setMessage(AboutActivity.this.getResources().getString(R.string.policy_text));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @OnClick(R.id.copy_code)
    protected void onCopyCodeClicked() {
        RunCounts settings = new RunCounts();
        ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(settings.getSSAD(), settings.getSSAD());
        try {
            clipboard.setPrimaryClip(clip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(App.getContext(), "Скопировано", Toast.LENGTH_LONG).show();
    }

    private void bindActivity() {
        mToolbar        = (Toolbar) findViewById(R.id.main_toolbar);
        mTitle          = (TextView) findViewById(R.id.main_textview_title);
        mTitleContainer = (LinearLayout) findViewById(R.id.main_linearlayout_title);
        mAppBarLayout   = (AppBarLayout) findViewById(R.id.main_appbar);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_about;
    }

    @Override
    public boolean providesActivityToolbar() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_vk:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://vk.com/antiperekup_app"));
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean openVk(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_vk:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://vk.com/antiperekup_app"));
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    public static String getCurrentVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}