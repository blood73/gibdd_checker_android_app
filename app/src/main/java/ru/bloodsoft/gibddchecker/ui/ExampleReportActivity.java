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
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.util.WebClient;

public class ExampleReportActivity extends BaseActivity {


    private String URL = "https://antiperekup.net/example.html";
    //"https://drive.google.com/viewer?url=https://antiperekup.net/example.pdf";
    private ProgressDialog mProgressDialog;

    @BindView(R.id.webView)
    WebView mWebView;

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
                if (!ExampleReportActivity.this.isFinishing()) {
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

        ButterKnife.bind(this);
        setupToolbar();
    }

    private void showProgressDialog() {
        // instantiate it within the onCreate method
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(ExampleReportActivity.this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(ExampleReportActivity.this.getResources().getString(R.string.loading));
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
        return R.id.nav_example_report;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

}