package ru.bloodsoft.gibddchecker.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.paolorotolo.appintro.ISlidePolicy;
import butterknife.BindView;
import butterknife.OnClick;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.util.RunCounts;

public class IntroPolicyFragment extends Fragment implements ISlidePolicy {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private int layoutResId;
    private View layoutView;

    public static IntroPolicyFragment newInstance(int layoutResId) {
        IntroPolicyFragment sampleSlide = new IntroPolicyFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        layoutView = inflater.inflate(layoutResId, container, false);

        TextView policyTextView = (TextView) layoutView.findViewById(R.id.policy_text);
        policyTextView.setMovementMethod(new ScrollingMovementMethod());

        View policyMessage = (View) layoutView.findViewById(R.id.policy_message);
        policyMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageView slideImageView = (ImageView) layoutView.findViewById(R.id.slide_image);
                View policyTextTextView = (View) layoutView.findViewById(R.id.policy_text);

                slideImageView.setVisibility(View.GONE);
                policyTextTextView.setVisibility(View.VISIBLE);
            }
        });

        return layoutView;
    }

    @Override
    public boolean isPolicyRespected() {
        RunCounts runCounts = new RunCounts();
        runCounts.setPolicyAccepted();
        return true;
    }

    @Override
    @SuppressWarnings("Deprecated")
    public void onUserIllegallyRequestedNextPage() {

    }
}