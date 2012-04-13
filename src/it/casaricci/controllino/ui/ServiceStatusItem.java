package it.casaricci.controllino.ui;

import it.casaricci.controllino.ConnectorService;
import it.casaricci.controllino.R;
import it.casaricci.controllino.controller.BaseController;
import it.casaricci.controllino.controller.BaseController.Status;
import it.casaricci.controllino.controller.BaseController.StatusChangedListener;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;


public class ServiceStatusItem extends RelativeLayout implements ViewSwitcher.ViewFactory {

    private BaseController mController;
    private ImageView mStatusIcon;
    private TextView mServiceName;
    private ImageView mServiceIcon;
    private TextSwitcher mServiceStatus;

    private StatusChangedListener mStatusListener = new StatusChangedListener() {
        @Override
        public void onStatusChanged(final BaseController ctrl) {
            post(new Runnable() {
                @Override
                public void run() {
                    // TODO not every status is i18n
                    Status st = ctrl.getStatus();
                    Animation anim = null;
                    int iconId = R.drawable.idle_light;
                    int textId = -1;
                    boolean enable = false;
                    switch (st) {
                        case STATUS_STARTING:
                            iconId = R.drawable.green_yellow;
                            anim = createTextSwitcherAnimation(R.array.starting);
                            break;
                        case STATUS_STOPPING:
                            iconId = R.drawable.green_yellow;
                            anim = createTextSwitcherAnimation(R.array.stopping);
                            break;
                        case STATUS_RUNNING:
                            iconId = R.drawable.green_light;
                            textId = R.string.status_running;
                            enable = true;
                            break;
                        case STATUS_STOPPED:
                            textId = R.string.status_stopped;
                            enable = true;
                            break;
                        case STATUS_RESTARTING:
                            iconId = R.drawable.green_yellow;
                            anim = createTextSwitcherAnimation(R.array.restarting);
                            break;
                        case STATUS_RELOADING:
                            iconId = R.drawable.green_yellow;
                            anim = createTextSwitcherAnimation(R.array.reloading);
                            break;
                        case STATUS_CHECKING:
                            iconId = R.drawable.green_yellow;
                            anim = createTextSwitcherAnimation(R.array.checking);
                            enable = true;
                            break;
                        case STATUS_ERROR:
                        case STATUS_UNKNOWN:
                            iconId = R.drawable.red_light;
                            enable = true;
                            break;
                    }

                    if (enable)
                        setEnabled(true);

                    mStatusIcon.setImageResource(iconId);
                    // if we have an animated icon, start it
                    Drawable icon = mStatusIcon.getDrawable();
                    if (icon instanceof AnimationDrawable)
                        ((AnimationDrawable) icon).start();

                    if (anim != null) {
                        mServiceStatus.startAnimation(anim);
                    }
                    else {
                        mServiceStatus.clearAnimation();
                        if (textId > 0)
                            mServiceStatus.setText(getResources().getString(textId));
                        else
                            // TODO i18n (for unknown status?)
                            mServiceStatus.setText("Status: " + st);
                    }
                }
            });
        }
    };

    public ServiceStatusItem(Context context) {
        super(context);
    }

    public ServiceStatusItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mStatusIcon = (ImageView) findViewById(R.id.light);
        mServiceName = (TextView) findViewById(R.id.name);
        mServiceIcon = (ImageView) findViewById(R.id.icon);
        mServiceStatus = (TextSwitcher) findViewById(R.id.status);
        mServiceStatus.setFactory(this);

        if (isInEditMode()) {
            mServiceName.setText("Test service");
            mServiceStatus.setText("Service is not running.");
            //mServiceStatus.setVisibility(GONE);
        }
    }

    public void bind(int position, BaseController controller, ConnectorService connector) {
        mController = controller;

        mServiceName.setText(mController.getServiceName());
        mServiceIcon.setImageResource(mController.getServiceIcon());

        // begin update
        mController.setStatusChangedListener(mStatusListener);
        if (mController.isDirty())
            mController.update();
    }

    public BaseController getController() {
        return mController;
    }

    private Animation createTextSwitcherAnimation(int strings) {
        return new TextSwitcherAnimation(getContext(), mServiceStatus, 500, strings);
    }

	@Override
	public View makeView() {
		TextView t = new TextView(getContext());
		t.setSingleLine(true);
		t.setEllipsize(TruncateAt.MARQUEE);
		return t;
	}
}
