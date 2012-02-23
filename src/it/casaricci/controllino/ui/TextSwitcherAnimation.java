package it.casaricci.controllino.ui;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextSwitcher;

public class TextSwitcherAnimation extends Animation implements AnimationListener {

    private String[] mTextStarting;
    private int mStep;
    private TextSwitcher mView;

    public TextSwitcherAnimation(Context context, TextSwitcher view, long duration, int strings) {
        super();
        setRepeatCount(INFINITE);
        setDuration(duration);
        setAnimationListener(this);
        mView = view;
        mTextStarting = context.getResources().getStringArray(strings);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        mStep += 1;
        if (mStep >= mTextStarting.length)
            onAnimationStart(animation);
        else
            mView.setText(mTextStarting[mStep]);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        mStep = 0;
        mView.setText(mTextStarting[0]);
    }

}
