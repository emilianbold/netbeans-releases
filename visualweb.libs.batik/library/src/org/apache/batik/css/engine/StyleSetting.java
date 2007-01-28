package org.apache.batik.css.engine;


/**
 * A value object for communicating a desired setting or clearing of a CSS property
 *
 * @author Tor Norbye
 */
public class StyleSetting {
    private int index;
    private String value;

    /**
     * Construct a StyleSetting with the given index and value
     */
    public StyleSetting(int index, String value) {
        this.index = index;
        this.value = value;
    }

    /**
     * Create a style setting only specifying a property; should only
     * be used for removals
     */
    public StyleSetting(int index) {
        this.index = index;
    }

    /** Return the CSS property index */
    public int getIndex() {
        return index;
    }

    /** Return the value, if any, for this CSS setting */
    public String getValue() {
        return value;
    }
}
