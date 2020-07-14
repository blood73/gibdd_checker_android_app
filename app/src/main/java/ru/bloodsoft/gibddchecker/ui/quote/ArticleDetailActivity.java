package ru.bloodsoft.gibddchecker.ui.quote;

import android.os.Bundle;

import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.ui.base.BaseActivity;

/**
 * Simple wrapper for {@link ArticleDetailFragment}
 * This wrapper is only used in single pan mode (= on smartphones)
 */
public class ArticleDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Show the Up button in the action bar.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ArticleDetailFragment fragment =  ArticleDetailFragment.newInstance(getIntent().getStringExtra(ArticleDetailFragment.ARG_ITEM_ID));
        getFragmentManager().beginTransaction().replace(R.id.article_detail_container, fragment).commit();
    }

    @Override
    public boolean providesActivityToolbar() {
        return false;
    }
}
