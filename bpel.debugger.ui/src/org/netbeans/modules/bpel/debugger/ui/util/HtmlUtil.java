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

package org.netbeans.modules.bpel.debugger.ui.util;

import java.awt.Color;

/**
 *
 * @author Alexander Zgursky
 */
public final class HtmlUtil {
    
    /** Creates a new instance of HtmlUtil */
    private HtmlUtil() {
    }
    
    /**
     * Converts given text to html with <code>bold=true</code>.
     *
     * @param text text to be converted
     * 
     * @return html-formatted String or <code>null</code>,
     *         if given text is <code>null</code>
     *
     * @see #toHtml
     */
    public static String toBold(String text) {
        return toHtml(text, true, false, null);
    }
    
    /**
     * Converts given text to html using given attributes.
     * @param text text to be converted
     * @param bold bold flag
     * @param italic italic flag
     * @param color color to be used or <code>null</code> for default color
     * 
     * @return html formatted text or <code>null</code> if
     *         given text is <code>null</code>
     */
    public static String toHtml(
            String text, boolean bold, boolean italic, Color color)
    {
        if (text == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html>"); // NOI18N
        
        if (bold) {
            buffer.append("<b>"); // NOI18N
        }
        if (italic) {
            buffer.append("<i>"); // NOI18N
        }
        if (color != null) {
            buffer.append("<font color=\"#"); // NOI18N
            final int RGB_BITS = 0xffffff;
            buffer.append(String.format("%06x", color.getRGB() & RGB_BITS));
            buffer.append("\">"); // NOI18N
        }
        buffer.append(text.
                replaceAll("&", "&amp;").       // NOI18N
                replaceAll("<", "&lt;").        // NOI18N
                replaceAll(">", "&gt;")         // NOI18N
        );
        
        if (color != null) {
            buffer.append("</font>"); // NOI18N
        }
        if (italic) {
            buffer.append("</i>"); // NOI18N
        }
        if (bold) {
            buffer.append("</b>"); // NOI18N
        }
        buffer.append("</html>"); // NOI18N
        
        return buffer.toString();
    }
}
