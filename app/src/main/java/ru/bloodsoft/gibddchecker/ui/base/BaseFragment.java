package ru.bloodsoft.gibddchecker.ui.base;

import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;

import static ru.bloodsoft.gibddchecker.util.LogUtil.makeLogTag;

/**
 * The base class for all fragment classes.
 *
 */
public class BaseFragment extends Fragment {

    /**
     * Inflates the layout and binds the view via ButterKnife.
     * @param inflater the inflater
     * @param container the layout container
     * @param layout the layout resource
     * @return the inflated view
     */
    public View inflateAndBind(LayoutInflater inflater, ViewGroup container, int layout) {
        View view = inflater.inflate(layout, container, false);
        ButterKnife.bind(this, view);

        return view;
    }
}