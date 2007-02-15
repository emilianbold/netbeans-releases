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
 *
 * $Id$
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
            super.setDisplayedMnemonic(DEFAULT_MNEMONIC_CHAR);
        } else {
            this.text = text;
            
            super.setText(StringUtils.stripMnemonic(this.text));
            super.setDisplayedMnemonic(StringUtils.fetchMnemonic(this.text));
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
            // the redered string width exceeds the bounds 
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
    public static final char DEFAULT_MNEMONIC_CHAR =
            '\u0000'; // NOI18N
}
