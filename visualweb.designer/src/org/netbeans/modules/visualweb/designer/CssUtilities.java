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

package org.netbeans.modules.visualweb.designer;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Hashtable;
import java.util.Map;
import org.w3c.dom.Element;

/**
 * Utilites for css box visual formatting.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (the old font stuff)
 */
public final class CssUtilities {

    // XXX Copied form CssBox.
    // XXX Copy also in designer/jsf/../DomDocumentImpl.
    // FIXME This is very suspicious, and should be revisited.
//    public static final int AUTO = Integer.MAX_VALUE - 1;
    public static final int AUTO = CssValue.AUTO;

    /** Creates a new instance of CssBoxUtilities */
    private CssUtilities() {
    }


//    /** Gets <code>FontMetrics</code> for the designer <code>Font</code> used for specified
//     * <code>Element</code> and text in its value attribute.
//     * @param element <code>Element</code> which used font's metrics is gonna be provided
//     * @see #getDesignerFontForElement */
//    public static FontMetrics getDesignerFontMetricsForElement(Element element) {
//        return getDesignerFontMetricsForElement(element, element.getAttribute(HtmlAttribute.VALUE));
//    }

    /** Gets <code>FontMetrics</code> for the designer <code>Font</code> used for specified
     * <code>Element</code> and text.
     * @param element <code>Element</code> which used font's metrics is gonna be provided
     * @param text text based on which the font is selected (according the priority list
     * and the ability to show all its characters).
     * @see #getDesignerFontForElement */
    public static FontMetrics getDesignerFontMetricsForElement(Element element, String text, int defaultFontSize) {
        Font font = getDesignerFontForElement(element, text, defaultFontSize);
//        return Toolkit.getDefaultToolkit().getFontMetrics(font);
        return DesignerUtils.getFontMetrics(font);
    }

//    /** Gets designer <code>Font</code> used for <code>element</code> and text in its value attribute.
//     * @param element <code>Element</code> for which the <code>Font</code> is provided */
//    public static Font getDesignerFontForElement(Element element) {
//        return getDesignerFontForElement(element, element.getAttribute(HtmlAttribute.VALUE));
//    }

    /** Gets designer <code>Font</code> for specified <code>element</code> and text.
     * @param element <code>Element</code> for which the <code>Font</code> is provided
     * @param text text based on which the font is selected (according the priority list
     * and the ability to show all its characters. */
    public static Font getDesignerFontForElement(Element element, String text, int defaultFontSize) {
        String[] fontFamilyNames = CssProvider.getValueService().getFontFamilyNamesForElement(element);

        int style = CssProvider.getValueService().getFontStyleForElement(element, Font.PLAIN);
//        int size = Math.round(CssProvider.getValueService().getFontSizeForElement(element, DesignerSettings.getInstance().getDefaultFontSize()));
        int size = Math.round(CssProvider.getValueService().getFontSizeForElement(element, defaultFontSize));

        for (String fontFamilyName : fontFamilyNames) {
            Font font = getFont(fontFamilyName, style, size);
            // #6461942 Needs to check whether the font is able to show the text.
            if (text == null || text.length() == 0 || font.canDisplayUpTo(text) == -1) {
                return font;
            }
        }

        // XXX This shouldn't happen, anyway fall back to the last font family name specified.
        return getFont(fontFamilyNames[fontFamilyNames.length - 1], style, size);
    }

    // >>> XXX Moved from CssValueServiceImpl
    private static FontKey fontSearch = new FontKey(null, 0, 0);
    private static Map<FontKey, Font> fontTable = new Hashtable<FontKey, Font>();

    /**
     * Gets a new font.  This returns a Font from a cache
     * if a cached font exists.  If not, a Font is added to
     * the cache.  This is basically a low-level cache for
     * 1.1 font features.
     *
     * @param family the font family (such as "Monospaced")
     * @param style the style of the font (such as Font.PLAIN)
     * @param size the point size >= 1
     * @return the new font
     */
    private static Font getFont(String family, int style, int size) {
        fontSearch.setValue(family, style, size);

        Font f = fontTable.get(fontSearch);

        if (f == null) {
            // haven't seen this one yet.
            f = new Font(family, style, size);

            FontKey key = new FontKey(family, style, size);
            fontTable.put(key, f);
        }

        return f;
    }
    
    /** Key for a font table */
    private static class FontKey {
        private String family;
        private int style;
        private int size;

        /**
         * Constructs a font key.
         */
        public FontKey(String family, int style, int size) {
            setValue(family, style, size);
        }

        public void setValue(String family, int style, int size) {
            this.family = (family != null) ? family.intern() : null;
            this.style = style;
            this.size = size;
        }

        /**
         * Returns a hashcode for this font.
         * @return     a hashcode value for this font.
         */
        public int hashCode() {
            int fhash = (family != null) ? family.hashCode() : 0;

            return fhash ^ style ^ size;
        }

        /**
         * Compares this object to the specifed object.
         * The result is <code>true</code> if and only if the argument is not
         * <code>null</code> and is a <code>Font</code> object with the same
         * name, style, and point size as this font.
         * @param     obj   the object to compare this font with.
         * @return    <code>true</code> if the objects are equal;
         *            <code>false</code> otherwise.
         */
        public boolean equals(Object obj) {
            if (obj instanceof FontKey) {
                FontKey font = (FontKey)obj;

                return (size == font.size) && (style == font.style) && (family == font.family);
            }

            return false;
        }
    } // End of FontKey
    // <<< XXX Moved from CssValueServiceImpl

    
    /** XXX Copy also in insync/FacesDnDSupport.
     * XXX Copy also in designer/jsf/../DomDocumentImpl
     * XXX Provides the auto value as <code>AUTO</code>, revise that, it looks very dangerous.
     * TODO At least move into designer/cssengine.
     */
    public static int getCssLength(Element element, int property) {
////        Value val = getValue(element, property);
//        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, property);
//        
//        // XXX #6460007 Possible NPE.
//        if (cssValue == null) {
//            // XXX What value to return?
//            return 0;
//        }
//        
////        if (val == CssValueConstants.AUTO_VALUE) {
//        if (CssProvider.getValueService().isAutoValue(cssValue)) {
//            return AUTO;
//        }
//        
////        return (int)val.getFloatValue();
//        return (int)cssValue.getFloatValue();
        return CssProvider.getValueService().getCssLength(element, property);
    }

}
