/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * TabPainter.java
 *
 * Created on December 3, 2003, 4:22 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * An extention to Border which can provide a non-rectangular interior region
 * that will contain the tab's content, and actually paint that interior. The
 * goal of this class is to make it extremely easy to plug in different painting
 * logic without having to write an entire UI delegate.
 *
 * @author Tim Boudreau
 */
public interface TabPainter extends Border {

    /**
     * Get the polygon representing the tag.  Clicks outside this polygon inside
     * the tab's rectangle will be ignored. This polygon makes up the bounds of
     * the tab. <p><code>AbstractTabsUI</code> contains generic support for
     * drawing drag and drop target indications.  If want to use it rather than
     * write your own, you need to specify the polygon returned by this method
     * with the following point order:  The last two points in the point array
     * of the polygon <strong>must be the bottom left corner, followed by the
     * bottom right corner</strong>.  In other words, start at the upper left
     * corner when constructing the polygon, and end at the bottom right corner,
     * using no more than one point for the bottom left and right corners:
     * <pre>
     * start here -->    /---------
     *                            |
     * finish here -->   ----------
     * </pre>
     */
    Polygon getInteriorPolygon(Component renderer);

    /**
     * Paint the interior (as defined by getInteriorPolygon()) as appropriate
     * for the tab.  Implementations will presumably use different colors to
     * manage selection, activated state, etc.
     */
    void paintInterior(Graphics g, Component renderer);

    /**
     * Get the close button rectangle for this tab.  May return null if
     * supportsCloseButton() returns null.
     *
     * @param jc     The current renderer
     * @param rect   A rectangle that should be configured with the close button
     *               bounds
     * @param bounds The bounds relative to which the close button rectangle
     *               should be determined
     */
    void getCloseButtonRectangle(JComponent jc, Rectangle rect,
                                 Rectangle bounds);

    boolean supportsCloseButton(JComponent renderer);
}
