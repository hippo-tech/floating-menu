package tech.hippo.floatingmenu;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * This class contains the floating button and expands the menú rotating the button when this is
 * clicked.
 * <p>
 * Created by hippo on 18/7/18.
 */

public class FloatingMenu extends ConstraintLayout implements View.OnClickListener, OptionTextProperties {

    public static final int DEFAULT_OPTION_BG_COLOR = Color.TRANSPARENT;
    public static final int DEFAULT_OPTION_TEXT_SIZE = 0;
    public static final int DEFAULT_OPTION_TEXT_COLOR = Color.BLACK;
    public static final int DEFAULT_BUTTON_BACKGROUND_RES_ID = R.drawable.button_fab_standard_enabled;

    private static final long TRANSITION_DURATION = 25L;
    private static final String LOG_TAG = FloatingMenu.class.getSimpleName();
    private ImageView floating;
    private ConstraintLayout mainLayout;
    private Context context;
    private FloatingMenuAdapter adapter;
    private float currentDegree;

    private boolean visible;
    //    private TextView textView1, textView2, textView3, textView4;
    private List<TextView> views;

    private int mOptionBgColor;
    private int mOptionTextSize;
    private int mOptionTextColor;
    private int mButtonBackground;

    private OnFloatingMenuClickListener clickListener;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public FloatingMenu(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     * <p>
     * <p>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     * @see #FloatingMenu(Context, AttributeSet, int)
     */
    public FloatingMenu(Context context, @Nullable AttributeSet attrs) {

        this(context, attrs, 0);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute. This constructor of View allows subclasses to use their
     * own base style when they are inflating. For example, a Button class's
     * constructor would call this version of the super class constructor and
     * supply <code>R.attr.buttonStyle</code> for <var>defStyleAttr</var>; this
     * allows the theme's button style to modify all of the base view attributes
     * (in particular its background) as well as the Button class's attributes.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @see #FloatingMenu(Context, AttributeSet)
     */
    public FloatingMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        if (attrs != null) {
            init(attrs, defStyleAttr);
        }
    }

    /**
     * Convert a dimension in DP to Pixels
     *
     * @param resources Resources object
     * @param dp        Dimension in DP to be converted
     * @return Dimension in DP
     */
    public static float convertDpToPx(Resources resources, float dp) {
        if (resources == null) return dp;

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());

    }

    /**
     * Inits the View getting attrs and setting the main view.
     *
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     */
    private void init(@NonNull AttributeSet attrs, int defStyleAttr) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatingMenu, defStyleAttr, 0);

        mOptionBgColor = a.getColor(R.styleable.FloatingMenu_fm_option_backgroundColor, DEFAULT_OPTION_BG_COLOR);
        mOptionTextSize = a.getDimensionPixelSize(R.styleable.FloatingMenu_fm_option_textSize, DEFAULT_OPTION_TEXT_SIZE);
        mOptionTextColor = a.getColor(R.styleable.FloatingMenu_fm_option_textColor, DEFAULT_OPTION_TEXT_COLOR);
        mButtonBackground = a.getResourceId(R.styleable.FloatingMenu_rm_button_background, DEFAULT_BUTTON_BACKGROUND_RES_ID);

        a.recycle();

        inflate(context, R.layout.floating_menu_view, this);

        floating = findViewById(R.id.floating);
        mainLayout = findViewById(R.id.mainLayout);

        floating.setBackgroundResource(mButtonBackground);
        floating.setOnClickListener(this);


    }

    /**
     * Sets the adapter with the TextView collection
     *
     * @param floatingMenuAdapter the {@link FloatingMenuAdapter} containing the adapter
     */
    public void setAdapter(@NonNull FloatingMenuAdapter floatingMenuAdapter) {
        this.adapter = floatingMenuAdapter;
        views = new ArrayList<>(adapter.getCount());
        for (int i = 0; i < adapter.getCount(); i++) {
            final TextView t = (TextView) adapter.getDecoratedView(i, mainLayout, this);
            views.add(t);
            mainLayout.addView(t);


        }
    }

    /**
     * Adds the click listener to respond to click ecents
     *
     * @param onFloatingMenuClickListener the {@link OnFloatingMenuClickListener} with the click listener
     */
    public void addOnFloatingMenuClickListener(@NonNull OnFloatingMenuClickListener onFloatingMenuClickListener) {
        this.clickListener = onFloatingMenuClickListener;

        for (TextView v : views) {
            v.setOnClickListener(this);
        }
    }


    /**
     * Animates the button and starts the floating menu expand or collapse events
     *
     * @param degree the degree to rotate to
     */
    private void animate(float degree) {

        // Create rotation animation

        RotateAnimation ra = new RotateAnimation(-currentDegree, -degree,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f
        );
        // How long the animation will take place
        ra.setDuration(TRANSITION_DURATION);

        // Set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        floating.startAnimation(ra);
        visible = !visible;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (visible) {
                expandOption(0);
            } else {
                collapse(views.size() - 1);
            }
        }
    }

    /**
     * Collapses the floating menu for a given position under the next one.
     *
     * @param position the position to collapse.
     */
    void collapse(final int position) {

        ConstraintSet cs = new ConstraintSet();
        cs.clone(mainLayout);
        Log.d(LOG_TAG, "position collapsed is " + position);
        if (position == 0) {

            ChangeBounds cb = new ChangeBounds();
            cb.setDuration(TRANSITION_DURATION);
            cb.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    views.get(position).setAlpha(0.2f);
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    views.get(position).setAlpha(0.0f);


                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
            TransitionManager.beginDelayedTransition(mainLayout, cb);


        } else if (position > 0 && position < views.size()) {


            collapseSecondLevel(cs, position);

            cs.applyTo(mainLayout);
            ChangeBounds cb = new ChangeBounds();
            cb.setDuration(TRANSITION_DURATION);
            cb.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    views.get(position).setAlpha(0.2f);
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    views.get(position).setAlpha(0.0f);
                    collapse(position - 1);
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });

            TransitionManager.beginDelayedTransition(mainLayout, cb);
        }
    }

    /**
     * Expands the menu option at the position and recursivelly expands the next menu options.
     *
     * @param position the position of the menu option
     */
    void expandOption(final int position) {
        if (position == 0) {
            getFirstLevelConstraints().applyTo(mainLayout);
            ChangeBounds cb = new ChangeBounds();
            cb.setDuration(TRANSITION_DURATION);
//        cb.setInterpolator(new OvershootInterpolator());
            cb.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    if (views != null) {
                        for (View v : views) {
                            v.setAlpha(0.0f);
                        }
                    }
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    views.get(position).setAlpha(1.0f);
                    expandOption(position + 1);
                    ChangeBounds cb1 = new ChangeBounds();
                    cb1.setDuration(TRANSITION_DURATION);
                    getSecondLevelConstraints().applyTo(mainLayout);
                    TransitionManager.beginDelayedTransition(mainLayout, cb1);

                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
            TransitionManager.beginDelayedTransition(mainLayout, cb);


        } else if (position > 0 && position < views.size()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    views.get(position).setAlpha(1.0f);
                    if (position + 1 < views.size()) expandOption(position + 1);
                }
            }, TRANSITION_DURATION);
        }
    }


    /**
     * Collapses all the options but the first one.
     *
     * @param constraintSet the {@link ConstraintSet} to modify for the given position
     * @param position      the item's position to relocate in the screen
     */
    private void collapseSecondLevel(@NonNull ConstraintSet constraintSet, int position) {
        if (position > 0 && position < views.size() - 1) {
            constraintSet.clear(views.get(position).getId(), ConstraintSet.BOTTOM);

            constraintSet.connect(views.get(position).getId(), ConstraintSet.TOP, views.get(position - 1).getId(), ConstraintSet.TOP);
            constraintSet.constrainWidth(views.get(position).getId(), (int) convertDpToPx(getResources(), 0));
            views.get(position).setAlpha(0.2f);
        }
    }

    /**
     * Gets the first item {@link ConstraintSet} with the new position
     *
     * @return the {@link ConstraintSet} with the new position
     */
    private ConstraintSet getFirstLevelConstraints() {

        ConstraintSet cs = new ConstraintSet();
        cs.clone(mainLayout);

        for (View v : views) {
            getLevelConstraints(v, floating, cs, ConstraintSet.START);
        }

        return cs;

    }

    /**
     * Sets the first level constraints to all the views
     *
     * @param view                the {@link View to set the constraints to
     * @param anchorView          the {@link View} used as anchor to connect the view
     * @param cs                  the {@link ConstraintSet} to modify
     * @param anchorEndConstraint the end side connection
     */
    private void getLevelConstraints(@NonNull View view, @NonNull View anchorView, @NonNull ConstraintSet cs, int anchorEndConstraint) {
        cs.clear(view.getId(), ConstraintSet.TOP);
        cs.clear(view.getId(), ConstraintSet.START);

        cs.connect(view.getId(), ConstraintSet.END, anchorView.getId(), anchorEndConstraint);
        cs.connect(view.getId(), ConstraintSet.BOTTOM, anchorView.getId(), ConstraintSet.TOP, (int) convertDpToPx(getResources(), 5));
        cs.constrainWidth(view.getId(), ConstraintSet.WRAP_CONTENT);
        cs.constrainHeight(view.getId(), ConstraintSet.WRAP_CONTENT);
    }

    /**
     * Sets constraints for all the option menus except the first one
     *
     * @return the {@link ConstraintSet} with the new constraints
     */
    private ConstraintSet getSecondLevelConstraints() {

        ConstraintSet cs = new ConstraintSet();
        cs.clone(mainLayout);
        for (int i = 1; i < views.size(); i++) {
            getLevelConstraints(views.get(i), views.get(i - 1), cs, ConstraintSet.END);
        }
//        getLevelConstraints(textView2, anchorView, cs, ConstraintSet.END);
//        getLevelConstraints(textView3, textView2, cs, ConstraintSet.END);

        return cs;
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.floating) {
            if (currentDegree == 0) {
                animate(45.0f);
                currentDegree += 45.0f;

            } else {
                animate(0.0f);
                currentDegree = 0.0f;
            }
        } else if (v instanceof TextView) {
            if (clickListener != null) {
                clickListener.onClick((TextView) v, views.indexOf(v));
            }
        }
    }

    /**
     * Returns the text color property.
     *
     * @return an int representing the {@link android.graphics.Color}
     */
    @Override
    public int getTextColor() {
        return mOptionTextColor;
    }

    /**
     * Returns the text size property.
     *
     * @return an int with the text size property
     */
    @Override
    public int getTextSize() {
        return mOptionTextSize;
    }

    /**
     * Returns the TextView background color property.
     *
     * @return an int representing the {@link android.graphics.Color}
     */
    @Override
    public int getBgColor() {
        return mOptionBgColor;
    }

    /**
     * Sets the {@link Typeface} for option menu
     *
     * @param optionTypeface the new {@link Typeface} to set
     */
    public void setOptionTypeface(@NonNull Typeface optionTypeface) {

        if (views != null && !views.isEmpty()) {
            for (TextView v : views) {
                v.setTypeface(optionTypeface);
                mainLayout.invalidate();
            }
        }
    }

    /**
     * This listener gets the click events from the options menu
     */
    public interface OnFloatingMenuClickListener {

        /**
         * When one of the options is clicked this listener is invoked to pass the view and its position in menu
         *
         * @param view     the clicked {@link TextView}
         * @param position the position of the view in the menu
         */
        void onClick(TextView view, int position);
    }
}
