package ru.bloodsoft.gibddchecker.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.ISlidePolicy;
import ru.bloodsoft.gibddchecker.R;
import ru.bloodsoft.gibddchecker.ui.quote.ListActivity;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance("Добро пожаловать!", "Выбирайте среди множества источников данных", R.drawable.slide_1, Color.parseColor("#2196F3")));
        addSlide(IntroPolicyFragment.newInstance(R.layout.fragment_policy));
        addSlide(AppIntroFragment.newInstance("Проверьте авто в базе ГИБДД", "Все, что Вам нужно - это VIN код автомобиля. Узнайте количество владельцев, наличие ДТП, получите данные о запрете регистрационных действий", R.drawable.slide_2, Color.parseColor("#2196F3")));
        addSlide(AppIntroFragment.newInstance("Нет VIN кода?", "Не беда. Попробуйте узнать VIN код по гос.номеру автомобиля. А также найти фотографии авто, при наличии в базе", R.drawable.slide_8, Color.parseColor("#2196F3")));
        //addSlide(AppIntroFragment.newInstance("Проверь авто на ДТП", "Поиск выполняется на официальном сайте ГИБДД", R.drawable.slide_9, Color.parseColor("#2196F3")));

        showSkipButton(false);
        showStatusBar(false);
        setFlowAnimation();
        setProgressIndicator();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSkipPressed() {
        Intent intent = new Intent(IntroActivity.this, ListActivity.class);
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onDonePressed() {
        Intent intent = new Intent(IntroActivity.this, ListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);

        // Check if oldFragment implements ISlidePolicy
        if (oldFragment != null && oldFragment instanceof ISlidePolicy) {
            showSkipButton(true);
        }
    }
}