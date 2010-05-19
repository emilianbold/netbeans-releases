/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview.components;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;

/**
 *
 * @author anjeleevich
 */
public class StyledLabel extends JLabel {
    
    private Style style;
    
    public StyledLabel(String text) {
        super(text);
        this.style = PLAIN_STYLE;
    }
    
    public StyledLabel(Style style) {
        this.style = style;
    }
    
    @Override
    public Font getFont() {
        Font defaultFont = super.getFont();
        
        return (style != null) 
                ? style.getFont(defaultFont)
                : defaultFont;
    }
    
    @Override
    public Color getForeground() {
        Color defaultColor = super.getForeground();

        return (style != null) 
                ? style.getColor(defaultColor)
                : defaultColor;
    }
    
    public void setStyle(Style style) {
        this.style = style;
        revalidate();
        repaint();
    }
    
    public static class Style {
        private int fontSizePercents;
        private int fontStyle;
        private Color color;
        
        public Style(int fontSizePercents, int fontStyle, Color color) {
            this.fontSizePercents = fontSizePercents;
            this.fontStyle = fontStyle;
            this.color = color;
        }
        
        public Font getFont(Font defualtFont) {
            if (defualtFont == null) {
                return null;
            }
            
            int defaultFontStyle = defualtFont.getStyle();

            float defaultFontSize = defualtFont.getSize2D();
            
            float fontSize = defaultFontSize * fontSizePercents / 100;
            
            return (defaultFontStyle != fontStyle || fontSize != defaultFontSize)
                    ? defualtFont.deriveFont(fontStyle, fontSize)
                    : defualtFont;
        }
        
        public Color getColor(Color defaultColor) {
            return (color == null || defaultColor == null) 
                    ? defaultColor
                    : color;
        }
    }
    
    public static final Style PLAIN_STYLE 
            = new Style(100, Font.PLAIN, null);

    public static final Style BOLD_STYLE 
            = new Style(100, Font.BOLD, null);
    
    public static final Style SMALL_STYLE 
            = new Style(90, Font.PLAIN, Color.GRAY);
}
