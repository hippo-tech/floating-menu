package tech.hippo.floatingmenu;

/**
 * This interface is used to pass layout attributes to the adapter in order to create the TextViews
 *
 * Created by hippo on 23/7/18.
 */
interface OptionTextProperties {
    /**
     * Returns the text color property.
     * @return an int representing the {@link android.graphics.Color}
     */
    int getTextColor();

    /**
     * Returns the text size property.
     * @return an int with the text size property
     */
    int getTextSize();

    /**
     * Returns the TextView background color property.
     * @return an int representing the {@link android.graphics.Color}
     */
    int getBgColor();
}
