/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.designer;

import java.awt.Color;

import org.netbeans.modules.visualweb.css2.PageBox;


// For CVS archaeology: Most of the code in this file used to be in SelectionManager.java

/**
 * This class manages color selections for the designer surface
 * @todo This file needs some cleanup after being split out from SelectionManager.
 *
 * @author Tor Norbye
 */
public class ColorManager {
    private WebForm webform;

    /** Last pagebox we've analyzed and chosen colors for */
    private PageBox pageBox;
    public Color insertColor;
    public Color selectionColor;
    public Color selectionColorReverse;
    public Color selectionBoundsColor;
    public Color primaryColor;
    public Color dropTargetColor;
    public Color hierarchyForegroundColor;
    public Color hierarchyBackgroundColor;
    public Color draggerColor;
    public Color draggerColorBorder;
    public Color resizerColor;
    public Color resizerColorBorder;
    public Color marqueeColor;
    public Color marqueeColorBorder;
    public Color gridColor;

    /** Creates a new instance of ColorManager */
    public ColorManager(WebForm webform) {
        this.webform = webform;
    }

    public void resetPageBox() {
        pageBox = null;
    }

    public void sync() {
        DesignerPane pane = webform.getPane();

        if ((pane != null) && (pane.getPageBox() != pageBox)) {
            pageBox = pane.getPageBox();
            initializeColors();
        }
    }

    private void initializeColors() {
        PageBox pb = webform.getPane().getPageBox();

        if ((pb != null) && pb.isDarkBackground()) {
            setDarkBackgroundColors();
        } else {
            setLightBackgroundColors();
        }
    }

    /** @todo Make colors user-configurable. Better yet, look them
     * up from the UA stylesheet, default.css!
     */
    private void setLightBackgroundColors() {
        insertColor = Color.LIGHT_GRAY;
        gridColor = Color.GRAY;
        selectionColor = Color.BLACK;
        selectionColorReverse = Color.WHITE;
        selectionBoundsColor = Color.DARK_GRAY;

        //primaryColor = Color.lightGray;
        primaryColor = Color.YELLOW;
        dropTargetColor = Color.BLUE;
        draggerColor = new Color(0, 0, 0, 30);
        draggerColorBorder = new Color(0, 0, 0, 100);
        marqueeColor = draggerColor;
        marqueeColorBorder = draggerColorBorder;
        resizerColor = draggerColor;
        resizerColorBorder = draggerColorBorder;

        if (SelectionManager.PAINT_SELECTION_HIERARCHY) {
            hierarchyForegroundColor = Color.BLACK;
            hierarchyBackgroundColor = new Color(200, 200, 200, 128);
        }
    }

    /** @todo Make colors user-configurable. Better yet, look them
     * up from the UA stylesheet, default.css!
     */
    private void setDarkBackgroundColors() {
        insertColor = Color.LIGHT_GRAY;
        gridColor = Color.GRAY;
        selectionColor = Color.WHITE;
        selectionColorReverse = Color.BLACK;
        selectionBoundsColor = Color.DARK_GRAY;
        primaryColor = Color.YELLOW;
        dropTargetColor = Color.BLUE;
        draggerColor = new Color(200, 200, 200, 30);
        draggerColorBorder = new Color(200, 200, 200, 100);
        marqueeColor = draggerColor;
        marqueeColorBorder = draggerColorBorder;
        resizerColor = draggerColor;
        resizerColorBorder = draggerColorBorder;

        if (SelectionManager.PAINT_SELECTION_HIERARCHY) {
            hierarchyForegroundColor = Color.WHITE;
            hierarchyBackgroundColor = new Color(200, 200, 200, 128);
        }
    }

    /** Check whether the given color is "dark".
     * @todo Move to ColorManager
     */
    public static boolean isDark(Color color) {
        // Decide whether this color is "light" or "dark".
        // Can't just do a simple "(r+g+b)/3 > 128" check since for
        // example saturated yellow is six times brighter than saturated blue.
        // So compute the luminance. I found various different luminance
        // formulas but they were similar enough that any one would be
        // close enough for my purposes:
        float luminance255 = // no point dividing by 255 to normalize to 1.0
            (0.27f * color.getRed()) + (0.67f * color.getGreen()) + (0.06f * color.getBlue());

        // Could split at 50% (128) but since this method is used to
        // decide when to switch to reverse video, go a little bit
        // darker since visually it looks better with dark selection
        // handles for example and it's light enough a bit further down
        // the scale - let's go down to 25% before we hit reverse video
        return luminance255 < 64.0f;
    }
}
