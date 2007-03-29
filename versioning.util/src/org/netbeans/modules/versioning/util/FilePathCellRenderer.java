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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.util;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * Treats values in cells as file paths and renders them so that the end of the path is always visible.
 *
 * @author Maros Sandor
 */
public class FilePathCellRenderer extends DefaultTableCellRenderer {

    private static final int VISIBLE_START_CHARS = 0;

    private String computeFitText(String text) {
        if (text.length() <= VISIBLE_START_CHARS + 3) return text;

        FontMetrics fm = getFontMetrics(getFont());
        int width = getSize().width;
            
        String prefix = text.substring(0, VISIBLE_START_CHARS) + "...";
        int prefixLength = fm.stringWidth(prefix);
        int desired = width - prefixLength - 2;
        if (desired <= 0) return text;
        
        for (int i = text.length() - 1; i >= 0; i--) {
            String suffix = text.substring(i);
            int swidth = fm.stringWidth(suffix);
            if (swidth >= desired) {
                return suffix.length() > 0 ? prefix + suffix.substring(1) : text;
            }
        }
        return text;
    }
    
    protected void paintComponent(Graphics g) {
        setText(computeFitText(getText()));
        super.paintComponent(g);
    }
}
