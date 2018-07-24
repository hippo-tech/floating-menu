package tech.hippo.floatingmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Provides views for the {@link FloatingMenu}
 * <p>
 * Created by hippo on 18/7/18.
 */

public class FloatingMenuAdapter extends ArrayAdapter<String> {

    private final int mResource;
    private TextView[] views;
    private int[] drawables;


    /**
     * Constructor
     *
     * @param context The current context.
     * @param objects The objects to represent in the ListView.
     * @throws IllegalArgumentException if the objects list is empty
     */
    public FloatingMenuAdapter(@NonNull Context context, @NonNull @Size(min = 1) List<String> objects) {
        this(context, objects, null);
    }

    /**
     * Constructor
     *
     * @param context The current context.
     * @param objects The objects to represent in the ListView.
     * @throws IllegalArgumentException if the objects list is empty
     */
    public FloatingMenuAdapter(@NonNull Context context, @NonNull @Size(min = 1) List<String> objects, @Nullable TypedArray drawables) {
        super(context, R.layout.txt_floating_layout, objects);
        if (objects.isEmpty()) {
            throw new IllegalArgumentException("The objects list cannot be null or empty");
        }
        if (drawables != null && drawables.length() > 0) {
            this.drawables = new int[drawables.length()];
            for (int i = 0; i < drawables.length(); i++) {
                this.drawables[i] = drawables.getResourceId(i, 0);
            }
            drawables.recycle();
        }

        this.mResource = R.layout.txt_floating_layout;
        this.views = new TextView[objects.size()];
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     *
     * @param position             the position to get the view
     * @param parent               The parent that this view will eventually be attached to
     * @param optionTextProperties the {@link OptionTextProperties} with the properties for decorating it
     * @return A {@link View} corresponding to the data at the specified position.
     */
    @NonNull
    View getDecoratedView(int position, @NonNull ViewGroup parent, @NonNull OptionTextProperties optionTextProperties) {

        if (views != null && views[position] != null) {
            return views[position];
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());

        @SuppressLint("ViewHolder")
        TextView optionMenu = (TextView) inflater.inflate(mResource, parent, false);

        optionMenu.setId(View.generateViewId());
        optionMenu.setText(getItem(position));
        optionMenu.setAlpha(0.0f);
        optionMenu.setClickable(true);
        if (drawables != null && position < drawables.length) {
            optionMenu.setCompoundDrawablesWithIntrinsicBounds(drawables[position], 0, 0, 0);
            optionMenu.setCompoundDrawablePadding(0);
            optionMenu.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }

        optionMenu.setGravity(Gravity.CENTER_VERTICAL);
        optionMenu.setBackground(changeDrawableShape(optionMenu, optionTextProperties.getBgColor()));
        optionMenu.setTextColor(optionTextProperties.getTextColor());
        if (optionTextProperties.getTextSize() > 0) {
            optionMenu.setTextSize(TypedValue.COMPLEX_UNIT_PX, optionTextProperties.getTextSize());
        }

        views[position] = optionMenu;

        return optionMenu;
    }

    /**
     * Changes the Drawable Shape with a new bacground color
     *
     * @param view          the view to change the shape
     * @param optionBgColor the new color for the background
     * @return a new {@link Drawable} to use as new shape for the View
     */
    private Drawable changeDrawableShape(@NonNull View view, final int optionBgColor) {

        final Drawable bg = view.getBackground();

        if (bg != null && bg instanceof GradientDrawable) {
            GradientDrawable sp = (GradientDrawable) bg;
            sp.setColor(optionBgColor);
            return sp;
        }

        return bg;

    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return super.getCount();
    }
}

