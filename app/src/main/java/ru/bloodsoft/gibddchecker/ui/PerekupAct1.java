package ru.bloodsoft.gibddchecker.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.google.firebase.analytics.FirebaseAnalytics;
import butterknife.ButterKnife;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.util.WebClient;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class PerekupAct1 extends BaseActivity {

    private static final String TAG = makeLogTag(PerekupAct1.class);
    private WebView mWebView;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String URL = "http://tradeinplus.ru/vikupapp/";
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to);

        mWebView = (WebView) findViewById(R.id.webView);

        CookieManager cookieManager = CookieManager.getInstance();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mWebView,true);
        } else {
            cookieManager.setAcceptCookie(true);
        }

        showProgressDialog();

        mWebView.setWebViewClient(new WebClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!PerekupAct1.this.isFinishing()) {
                    dismissProgressDialog();
                }
            }
        });

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        mWebView.setInitialScale(100);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.loadUrl(URL);

        logD(TAG, "PA1 was loaded");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(PerekupAct1.this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "PEREKUP_1");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "PEREKUP_1");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "PEREKUP_1");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        ButterKnife.bind(this);
        setupToolbar();
    }

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(PerekupAct1.this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(PerekupAct1.this.getResources().getString(R.string.loading));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ad_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_browser:
                if (!URL.isEmpty()) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(URL));
                    startActivity(i);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.perekup_1;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

}