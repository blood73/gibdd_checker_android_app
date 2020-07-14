package ru.bloodsoft.gibddchecker.ui;

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
import android.webkit.WebViewClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import butterknife.ButterKnife;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

public class SravniAct extends BaseActivity {

    private static final String TAG = makeLogTag(SravniAct.class);
    private WebView mWebView;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String URL = "https://www.sravni.ru/kasko?marker=1399|antiperekup.app|referral_link";

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

        //showProgressDialog();

        mWebView.setWebViewClient(new WebViewClient());

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.loadUrl(URL);

        logD(TAG, "SRAVNI was loaded");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(SravniAct.this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "SRAVNI");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "SRAVNI");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "SRAVNI");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        ButterKnife.bind(this);
        setupToolbar();
    }

    @Override
    protected void onDestroy() {
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
        return R.id.sravni;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

}
