/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

