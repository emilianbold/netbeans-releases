/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
