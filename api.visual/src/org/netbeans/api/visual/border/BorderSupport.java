package org.netbeans.api.visual.border;

import org.netbeans.modules.visual.border.ResizeBorder;

/**
 * This class contains support method for working with borders.
 *
 * @author David Kaspar
 */
public final class BorderSupport {

    private BorderSupport () {
    }

    /**
     * Returns whether a resize border is outer.
     * @param border the border created by
     * @return true if the border is created the createResizeBorder method as outer parameter set to true; false otherwise
     */
    public static boolean isOuterResizeBorder (Border border) {
        return border instanceof ResizeBorder  &&  ((ResizeBorder) border).isOuter ();
    }

}
