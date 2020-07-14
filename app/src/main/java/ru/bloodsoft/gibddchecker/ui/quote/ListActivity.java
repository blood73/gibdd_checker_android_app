package ru.bloodsoft.gibddchecker.ui.quote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.morsebyte.shailesh.twostagerating.FeedbackReceivedListener;
import com.morsebyte.shailesh.twostagerating.TwoStageRate;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.models.GibddContent;
import ru.bloodsoft.gibddchecker.ui.SettingsActivity;
import ru.bloodsoft.gibddchecker.ui.TOActivity;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;
import ru.bloodsoft.gibddchecker.util.RunCounts;
import ru.bloodsoft.gibddchecker.util.SettingsStorage;
import static ru.bloodsoft.gibddchecker.util.LogUtil.logD;
import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

/**
 * Lists all available quotes. This Activity supports a single pane (= smartphones) and a two pane mode (= large screens with >= 600dp width).
 *
 */
public class ListActivity extends BaseActivity implements ArticleListFragment.Callback {
    /**
     * Whether or not the activity is running on a device with a large screen
     */
    private boolean twoPaneMode;
    Integer rateAppNumberOfDaysBeforeShowDialog = new SettingsStorage().getRateAppNumberDays();
    private static final String TAG = makeLogTag(ListActivity.class);
    static ListActivity listActivity;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, "ca-app-pub-3078563819949367~1340629934");

        setupToolbar();
        showRateDialog();
        listActivity = this;

        if (isTwoPaneLayoutUsed()) {
            twoPaneMode = true;
            enableActiveItemState();
        }

        if (savedInstanceState == null && twoPaneMode) {
            setupDetailFragment();
        }

        RunCounts settings = new RunCounts();
        Boolean isAdFree = settings.isAdFree();

        adView = (AdView) findViewById(R.id.adView);
        View fragmentView = (View) findViewById(R.id.article_list);

        if (!isAdFree) {
            adView.setVisibility(View.VISIBLE);

            AdRequest request = new AdRequest.Builder()
                    .addTestDevice("E31198FB67C1181AF2CFCEDF4476A9D1")
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();

            adView.loadAd(request);
        } else {
            adView.setVisibility(View.GONE);
            if (fragmentView != null) {

                try {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) fragmentView.getLayoutParams();

                    if (layoutParams != null) {
                        layoutParams.bottomMargin = 0;
                        fragmentView.setLayoutParams(layoutParams);
                    }
                } catch (Exception e) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) fragmentView.getLayoutParams();

                    if (layoutParams != null) {
                        layoutParams.bottomMargin = 0;
                        fragmentView.setLayoutParams(layoutParams);
                    }
                }
            }
        }
    }

    public static ListActivity getInstance() {
        return listActivity;
    }

    /**
     * Called when an item has been selected
     *
     * @param id the selected quote ID
     */
    @Override
    public void onItemSelected(String id) {
        if (twoPaneMode) {
            // Show the quote detail information by replacing the DetailFragment via transaction.
            ArticleDetailFragment fragment = ArticleDetailFragment.newInstance(id);
            getFragmentManager().beginTransaction().replace(R.id.article_detail_container, fragment).commit();
        } else {
            if (id.equals("5")) {
                Intent toIntent = new Intent(this, TOActivity.class);
                startActivity(toIntent);
            } else {
                // Start the detail activity in single pane mode.
                Intent detailIntent = new Intent(this, ArticleDetailActivity.class);
                detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, id);
                startActivity(detailIntent);
            }
        }
    }

    private void setupToolbar() {
        final ActionBar ab = getActionBarToolbar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setupDetailFragment() {
        ArticleDetailFragment fragment =  ArticleDetailFragment.newInstance(GibddContent.ITEMS.get(0).id);
        getFragmentManager().beginTransaction().replace(R.id.article_detail_container, fragment).commit();
    }

    /**
     * Enables the functionality that selected items are automatically highlighted.
     */
    private void enableActiveItemState() {
        ArticleListFragment fragmentById = (ArticleListFragment) getFragmentManager().findFragmentById(R.id.article_list);
        fragmentById.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    /**
     * Is the container present? If so, we are using the two-pane layout.
     *
     * @return true if the two pane layout is used.
     */
    private boolean isTwoPaneLayoutUsed() {
        return findViewById(R.id.article_detail_container) != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_gibdd;
    }

    @Override
    public boolean providesActivityToolbar() {
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private void showRateDialog() {
        String RATE_INITIAL_TITLE = this.getResources().getString(R.string.rate_app_title);
        String RATE_LATER_TEXT = this.getResources().getString(R.string.rate_app_later);
        String RATE_NEVER_TEXT = this.getResources().getString(R.string.rate_app_never);
        String RATE_CONFIRMATION_TITLE = this.getResources().getString(R.string.rate_app_confirmation_title);
        String RATE_CONFIRMATION_DESCRIPTION = this.getResources().getString(R.string.rate_app_confirmation_description);
        String RATE_POSITIVE_BUTTON_TEXT = this.getResources().getString(R.string.rate_app_dialog_positive_button);
        String RATE_NEGATIVE_BUTTON_TEXT = this.getResources().getString(R.string.rate_app_dialog_negative_button);
        String RATE_FEEDBACK_TITLE = this.getResources().getString(R.string.rate_app_feedback_title);
        String RATE_FEEDBACK_DIALOG_DESCRIPTION = this.getResources().getString(R.string.rate_app_feedback_description);
        String RATE_FEEDBACK_POSITIVE_BUTTON_TEXT = this.getResources().getString(R.string.rate_app_feedback_send);
        final String RATE_FEEDBACK_SENT = this.getResources().getString(R.string.rate_app_feedback_sent);

        TwoStageRate twoStageRate = TwoStageRate.with(this);

        twoStageRate.setInstallDays(rateAppNumberOfDaysBeforeShowDialog).
                setLaunchTimes(Integer.MAX_VALUE).
                setEventsTimes(Integer.MAX_VALUE);

        //If user dismisses it, it simply resets again. (when user dismissed by clicking anywhere else on screen)
        twoStageRate.resetOnDismiss(true);  //it is true by default

        //If user gives rating the first time but declines to give playstore rating/ feedback we can reset the
        //TwoStageRate. These are false by default.
        twoStageRate.resetOnFeedBackDeclined(true).resetOnRatingDeclined(true);

        //You may choose to show/hide your app icon in rating prompt (default true)
        twoStageRate.setShowAppIcon(true);

        //Setting texts for initial prompt
        twoStageRate.setRatePromptTitle(RATE_INITIAL_TITLE).
                setRatePromptLaterText(RATE_LATER_TEXT).setRatePromptNeverText(RATE_NEVER_TEXT).setRatePromptDismissible(true);

        //Setting texts for confirmation dialog
        twoStageRate.setConfirmRateDialogTitle(RATE_CONFIRMATION_TITLE).
                setConfirmRateDialogDescription(RATE_CONFIRMATION_DESCRIPTION).
                setConfirmRateDialogPositiveText(RATE_POSITIVE_BUTTON_TEXT).
                setConfirmRateDialogNegativeText(RATE_NEGATIVE_BUTTON_TEXT).
                setConfirmRateDialogDismissible(true);

        //Setting texts for feedback title
        twoStageRate.setFeedbackDialogTitle(RATE_FEEDBACK_TITLE).
                setFeedbackDialogDescription(RATE_FEEDBACK_DIALOG_DESCRIPTION).
                setFeedbackDialogPositiveText(RATE_FEEDBACK_POSITIVE_BUTTON_TEXT).
                setFeedbackDialogNegativeText(RATE_NEGATIVE_BUTTON_TEXT).
                setFeedbackDialogDismissible(true);

        twoStageRate.setFeedbackReceivedListener(new FeedbackReceivedListener() {
            @Override
            public void onFeedbackReceived(String feedback) {
                logD(TAG, "feedback: " + feedback);
                Toast.makeText(ListActivity.this, RATE_FEEDBACK_SENT, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_SEND);

                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"antiperekup.app@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Антиперекуп. Отзыв");
                intent.putExtra(Intent.EXTRA_TEXT, feedback);

                intent.setType("message/rfc822");

                startActivity(Intent.createChooser(intent, "Выберите ваш email-клиент для отправки письма:"));
            }
        });

        //Finally call to show feedback dialog if any of condition is met.
        twoStageRate.showIfMeetsConditions();
    }
}
