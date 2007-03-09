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

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
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
    // FIXME This is very suspicious, and should be revisited.
    public static final int AUTO = Integer.MAX_VALUE - 1;

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
    public static FontMetrics getDesignerFontMetricsForElement(Element element, String text) {
        Font font = getDesignerFontForElement(element, text);
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
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
    public static Font getDesignerFontForElement(Element element, String text) {
        String[] fontFamilyNames = CssProvider.getValueService().getFontFamilyNamesForElement(element);

        int style = CssProvider.getValueService().getFontStyleForElement(element, Font.PLAIN);
        int size = Math.round(CssProvider.getValueService().getFontSizeForElement(element, DesignerSettings.getInstance().getDefaultFontSize()));

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
     * XXX Provides the auto value as <code>AUTO</code>, revise that, it looks very dangerous. */
    public static int getCssLength(Element element, int property) {
//        Value val = getValue(element, property);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, property);
        
        // XXX #6460007 Possible NPE.
        if (cssValue == null) {
            // XXX What value to return?
            return 0;
        }
        
//        if (val == CssValueConstants.AUTO_VALUE) {
        if (CssProvider.getValueService().isAutoValue(cssValue)) {
            return AUTO;
        }
        
//        return (int)val.getFloatValue();
        return (int)cssValue.getFloatValue();
    }

}
