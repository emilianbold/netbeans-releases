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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class GraphUtility {

    /** Creates a new instance of GraphUtility - this is a utility class so its private */
    private GraphUtility() {
    }

    /**
     * Truncates the text in the given BasicText object and appends ellipses ("...") so
     * that the text can fit within its bounds.
     * 
     * @param g Graphics2D context in which text will be drawn
     * @param text BasicText object containing text to be adjusted
     */
    public static void adjustText(Graphics2D g, BasicText text) {
        FontMetrics fMetrics = g.getFontMetrics(text.getFont());

        final String fullString = text.getOriginalText();
        Rectangle2D rect = fMetrics.getStringBounds(fullString, g);

        final int maxWidth = text.getWidth();
        int fullTextWidth = (int) Math.ceil(rect.getWidth());
        if (maxWidth >= fullTextWidth) {
            text.setText(text.getOriginalText());
            return;
        }

        String dotString = "...";
        rect = fMetrics.getStringBounds(dotString, g);
        final int dotWidth = (int) Math.ceil(rect.getWidth());
        if (maxWidth <= dotWidth) {
            text.setText(dotString);
            return;
        }

        String newStr = null;
        for (int i = fullString.length(); i > 0; i--) {
            // Get substring of fullstring and compare length (w/ dots) against
            // maxWidth.
            rect = fMetrics.getStringBounds(fullString, 0, i, g);
            int newWidth = (int) Math.ceil(rect.getWidth());
            if ((newWidth + dotWidth) <= maxWidth) {
                newStr = fullString.substring(0, i);
                break;
            }
        }

        // Now set the truncated string
        if (newStr != null && newStr.length() != 0) {
            text.setText(newStr + dotString);
        } else {
            text.setText(dotString);
        }
    }
}

