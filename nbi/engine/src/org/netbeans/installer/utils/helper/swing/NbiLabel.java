/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.utils.helper.swing;

import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JLabel;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiLabel extends JLabel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private boolean collapsePaths;
    
    private String text;
    
    public NbiLabel() {
        super();
        
        setText(null);
        collapsePaths = false;
    }
    
    public NbiLabel(final boolean collapsePaths) {
        this();
        
        this.collapsePaths = collapsePaths;
    }
    
    public void clearText() {
        setText(null);
    }
    
    @Override
    public void setText(final String text) {
        if ((text == null) || text.equals("")) {
            this.text = DEFAULT_TEXT;
            
            super.setText(DEFAULT_TEXT);
            super.setDisplayedMnemonic(DEFAULT_MNEMONIC);
            super.setToolTipText(DEFAULT_TOOLTIP_TEXT);
        } else {
            this.text = text;
            
            super.setText(StringUtils.stripMnemonic(this.text));
            super.setToolTipText(StringUtils.stripMnemonic(this.text));
            
            if (!SystemUtils.isMacOS()) {
                super.setDisplayedMnemonic(StringUtils.fetchMnemonic(this.text));
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics graphics) {
        if (collapsePaths && !text.equals(DEFAULT_TEXT)) {
            final String string = StringUtils.stripMnemonic(text);
            final String separator = SystemUtils.getFileSeparator();
            
            final int boundsWidth = getBounds().width;
            final int lastIndex = string.lastIndexOf(separator);
            
            int stringWidth = getStringBounds(graphics).width;
            int index = string.lastIndexOf(separator, lastIndex - 1);
            
            // we should continue while there is at least one separator
            // (lastIndex > -1), there is a previous separator (index > -1) and
            // the rendered string width exceeds the bounds
            // (stringWidth > boundsWidth)
            // note: if there are no separators in the string, it will not be
            // shortened at all and the default shortening procedure will take
            // place, also if collapsing a path does not help completely, additional
            // shortening will be performed by the default procedure
            while ((lastIndex != -1) &&
                    (index != -1) &&
                    (stringWidth > boundsWidth)) {
                final String shortenedString =
                        StringUtils.replace(string, "...", index + 1, lastIndex);
                
                super.setText(shortenedString);
                
                stringWidth = getStringBounds(graphics).width;
                index = string.lastIndexOf(separator, index - 1);
            }
        }
        
        super.paintComponent(graphics);
    }
    
    private Rectangle getStringBounds(Graphics graphics) {
        return getFontMetrics(
                getFont()).getStringBounds(super.getText(), graphics).getBounds();
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TEXT =
            " "; // NOI18N
    
    public static final String DEFAULT_TOOLTIP_TEXT =
            null;
    
    public static final char DEFAULT_MNEMONIC =
            '\u0000'; // NOMAGI
}
