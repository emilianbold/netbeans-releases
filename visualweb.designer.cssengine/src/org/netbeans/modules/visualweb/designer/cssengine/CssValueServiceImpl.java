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


package org.netbeans.modules.visualweb.designer.cssengine;


import org.netbeans.modules.visualweb.api.designer.cssengine.CssListValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValueService;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.batik.css.engine.value.ComputedValue;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;


/**
 * Impl of the <code>CssValueService</code>.
 * XXX Impl code copied from various places before spread in the modules.
 *
 * @author Peter Zavadsky
 */
public class CssValueServiceImpl implements CssValueService {

    private static final String CSS_FONT_FAMILY_NAME_MONOSPACE = "monospace"; // NOI18N
    private static final String CSS_FONT_FAMILY_NAME_SANS_SERIF = "sans-serif"; // NOI18N

    private static final String JAVA_FONT_FAMILY_NAME_MONOSPACED = "monospaced"; // NOI18N
    private static final String JAVA_FONT_FAMILY_NAME_SANS_SERIF = "SansSerif"; // NOI18N


    private static final Object LOCK_FONT_MAPPING = new Object();
    private static Map<String, String> fontMapping;

    // XXX Moving to CssBoxUtilities.
//    private static FontKey fontSearch = new FontKey(null, 0, 0);
//    private static Hashtable fontTable = new Hashtable();


    private static final CssValueService instance = new CssValueServiceImpl();


    /** Creates a new instance of CssValueServiceImpl */
    private CssValueServiceImpl() {
    }


    public static CssValueService getDefault() {
        return instance;
    }


    private static Value getCssValue(Element e, int property) {
        CssValueImpl cssValueImpl = CssEngineServiceImpl.get().getComputedValueImplForElement(e, property);
        return cssValueImpl == null ? null : cssValueImpl.getValue();
    }

    public Color getColorForElement(Element element, int styleIndex) {
        Value v = getCssValue(element, styleIndex);

        if (v == CssValueConstants.TRANSPARENT_RGB_VALUE) {
            return null;
        }
        if (v instanceof ComputedValue
        && ((ComputedValue)v).getComputedValue() == CssValueConstants.TRANSPARENT_RGB_VALUE) {
            return null;
        }

        if (v != null) {
            //if (v.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            return convertColor(v /*, 1*/);

            /*
            } else {
                return PaintServer.convertRGBICCColor
                    (e, v.item(0), (ICCColor)v.item(1), 1, ctx);
            }
            */
        }

        return null;
    }
    
    public boolean isColorTransparentForElement(Element element, int styleIndex) {
        Value v = getCssValue(element, styleIndex);
        // XXX #94265 The initial is transparent, but that doesn't work (in browser it doesn't seem to be transparent),
        // or there might be browser specific stylesheets changing the default behavior.
        // Hack: For now we ignore when it is initial value.
//        if (v == CssValueConstants.TRANSPARENT_RGB_VALUE) {
//            return true;
//        }
        if (v instanceof ComputedValue) {
            return ((ComputedValue)v).getComputedValue() == CssValueConstants.TRANSPARENT_RGB_VALUE;
        }
        return false;
    }

    private static final String[] CSS_LENGTH_UNITS = { "%", "em", "ex", "px", "cm", "mm", "in", "pt", "pc" }; // NOI18N

    public String[] getCssLengthUnits() {
        return CSS_LENGTH_UNITS;
    }

    /**
     * Converts the given Value and opacity to a Color object.
     * @param c The CSS color to convert.
     * @param o The opacity value (0 <= o <= 1).
     */
    private static Color convertColor(Value c /*, float opacity*/) {
        int r = resolveColorComponent(c.getRed());
        int g = resolveColorComponent(c.getGreen());
        int b = resolveColorComponent(c.getBlue());

        return new Color(r, g, b, 255 /*Math.round(opacity * 255f)*/);
    }

    /**
     * Returns the value of one color component (0 <= result <= 255).
     * @param v the value that defines the color component
     */
    private static int resolveColorComponent(Value v) {
        float f;

        switch (v.getPrimitiveType()) {
        case CSSPrimitiveValue.CSS_PERCENTAGE:
            f = v.getFloatValue();
            f = (f > 100f) ? 100f : ((f < 0f) ? 0f : f);

            return Math.round((255f * f) / 100f);

        case CSSPrimitiveValue.CSS_NUMBER:
            f = v.getFloatValue();
            f = (f > 255f) ? 255f : ((f < 0f) ? 0f : f);

            return Math.round(f);

        default:
            assert false;

            return 0;
        }
    }

    public float getFontSizeForElement(Element element, int defaultSize) {
        Value val = getCssValue(element, XhtmlCss.FONT_SIZE_INDEX);

        if (val == null) {
            return defaultSize; // XXX can this happen?
        }

        return val.getFloatValue();
    }
    
    public int getFontStyleForElement(Element element, int defaultStyle) {
        int style = defaultStyle;
        Value val = getCssValue(element, XhtmlCss.FONT_WEIGHT_INDEX);

        if (val != null && val.getFloatValue() > 400.0) {
            style |= Font.BOLD;
        }

        val = getCssValue(element, XhtmlCss.FONT_STYLE_INDEX);

        if ((val == ValueConstants.ITALIC_VALUE) || (val == ValueConstants.OBLIQUE_VALUE)) {
            // treat oblique as italic -- see for example
            // http://www.scribeserver.com/medieval/abtfonts.htm
            // Serif fonts typically have associated italic fonts and
            // sans-serif fonts typically have associated oblique fonts,
            // so oblique serves the purpose of an italic font.
            style |= Font.ITALIC;
        }
        return style;
    }
    
    public String[] getFontFamilyNamesForElement(Element element) {
        // Font family
        Value val = getCssValue(element, XhtmlCss.FONT_FAMILY_INDEX);
//        String family = getFontFamily(val);
        return getFontFamilyNames(val);
    }

//    public Font[] getFontsForElement(Element element, int defaultSize, int type) {
//        float size = getFontSizeForElement(element, defaultSize);
//
//        // Font family
//        Value val = getCssValue(element, XhtmlCss.FONT_FAMILY_INDEX);
////        String family = getFontFamily(val);
//        String[] fontFamilyNames = getFontFamilyNames(val);
//
//        int style = type;
//        val = getCssValue(element, XhtmlCss.FONT_WEIGHT_INDEX);
//
//        if (val != null && val.getFloatValue() > 400.0) {
//            style |= Font.BOLD;
//        }
//
//        val = getCssValue(element, XhtmlCss.FONT_STYLE_INDEX);
//
//        if ((val == ValueConstants.ITALIC_VALUE) || (val == ValueConstants.OBLIQUE_VALUE)) {
//            // treat oblique as italic -- see for example
//            // http://www.scribeserver.com/medieval/abtfonts.htm
//            // Serif fonts typically have associated italic fonts and
//            // sans-serif fonts typically have associated oblique fonts,
//            // so oblique serves the purpose of an italic font.
//            style |= Font.ITALIC;
//        }
//
//        List<Font> fonts = new ArrayList<Font>();
//        for (String fontFamilyName : fontFamilyNames) {
////        Font f = getFont(family, style, (int)size);
//            Font font = getFont(fontFamilyName, style, Math.round(size));
//            if (fonts.contains(font)) {
//                continue;
//            }
//            fonts.add(font);
//        }
//
//        return fonts.toArray(new Font[fonts.size()]);
//    }
    
//    public FontMetrics[] getFontsMetricsForElement(Element element) {
//        //Font font = doc.getFont(styleElement, CssEngineServiceImpl.getUserAgentInfo().getDefaultFontSize());
////        Font font = getFont(element, CssEngineServiceImpl.getUserAgentInfo().getDefaultFontSize());
//        Font[] fonts = getFontsForElement(element, CssEngineServiceImpl.getUserAgentInfo().getDefaultFontSize(), Font.PLAIN);
//
//        List<FontMetrics> fontsMetrics = new ArrayList<FontMetrics>();
//        for (Font font : fonts) {
//            FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
//            fontsMetrics.add(fontMetrics);
//        }
//
//        return fontsMetrics.toArray(new FontMetrics[fontsMetrics.size()]);
//    }

    private String[] getFontFamilyNames(Value list) {
        if ((list == null) || (list.getLength() == 0)) {
            return new String[] {JAVA_FONT_FAMILY_NAME_SANS_SERIF};
        }

        List<String> fontFamilyNames = new ArrayList<String>();
        Map validNames = getValidFontNameMapping();
        for (int i = 0, len = list.getLength(); i < len; i++) {
            Value it = list.item(i);
            String fontName = it.getStringValue();
            String family = (String)validNames.get(mapCssFontFamilyNameToJavaFontFamilyName(fontName));

            if (family == null) {
                family = (String)validNames.get(fontName.toLowerCase());
            }

            if (family != null) {
//                return family;
                fontFamilyNames.add(family);
            }
        }
        
        if (fontFamilyNames.isEmpty()) {
            // Fallback.
            fontFamilyNames.add(JAVA_FONT_FAMILY_NAME_SANS_SERIF);
        }

        return fontFamilyNames.toArray(new String[fontFamilyNames.size()]);
    }

    private Map<String, String> getValidFontNameMapping() {
        // This can get called from multiple threads, lock before setting
        synchronized (LOCK_FONT_MAPPING) {
            if (fontMapping == null) {
                String[] names = null;
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

                if (ge != null) {
                    names = ge.getAvailableFontFamilyNames();
                }

                if (names == null) {
                    names = Toolkit.getDefaultToolkit().getFontList();
                }

                if (names != null) {
                    fontMapping = new HashMap<String, String>(names.length * 2);

                    for (int counter = names.length - 1; counter >= 0; counter--) {
                        // Put both lowercase and case value in table.
                        fontMapping.put(names[counter].toLowerCase(), names[counter]);
                        fontMapping.put(names[counter], names[counter]);
                    }
                } else {
                    fontMapping = new HashMap<String, String>();
                }
            }
        }

        return fontMapping;
    }

    /** Maps from CSS font family name to Java Font family name. */
    private static String mapCssFontFamilyNameToJavaFontFamilyName(String cssFontFamilyName) {
        if (CSS_FONT_FAMILY_NAME_MONOSPACE.equals(cssFontFamilyName)) {
            return JAVA_FONT_FAMILY_NAME_MONOSPACED;
        } else if (CSS_FONT_FAMILY_NAME_SANS_SERIF.equals(cssFontFamilyName)) {
            return JAVA_FONT_FAMILY_NAME_SANS_SERIF;
        }
        // XXX TBD Perhaps more is necessaryhere.

        return cssFontFamilyName;
    }

//    /**
//     * Gets a new font.  This returns a Font from a cache
//     * if a cached font exists.  If not, a Font is added to
//     * the cache.  This is basically a low-level cache for
//     * 1.1 font features.
//     *
//     * @param family the font family (such as "Monospaced")
//     * @param style the style of the font (such as Font.PLAIN)
//     * @param size the point size >= 1
//     * @return the new font
//     */
//    private static Font getFont(String family, int style, int size) {
//        fontSearch.setValue(family, style, size);
//
//        Font f = (Font)fontTable.get(fontSearch);
//
//        if (f == null) {
//            // haven't seen this one yet.
//            f = new Font(family, style, size);
//
//            FontKey key = new FontKey(family, style, size);
//            fontTable.put(key, f);
//        }
//
//        return f;
//    }

    public CssListValue getComputedCssListValue(CssValue cssValue) {
        if (cssValue == null) {
            return null;
        }
        
        Value value = ((CssValueImpl)cssValue).getValue();

        if (value instanceof ListValue) {
            return CssValueFactory.createCssListValue((ListValue)value);
        } else if (value instanceof ComputedValue) {
            ComputedValue computedValue = (ComputedValue)value;
            Value result = computedValue.getComputedValue();
            if (result instanceof ListValue) {
                return CssValueFactory.createCssListValue((ListValue)result);
            }
        }

        return null;
    }
    
    
    public boolean isAbsoluteValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.ABSOLUTE_VALUE);
    }
    public boolean isAutoValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.AUTO_VALUE);
    }
    public boolean isBaseLineValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.BASELINE_VALUE);
    }
    public boolean isBlockValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.BLOCK_VALUE);
    }
    public boolean isBothValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.BOTH_VALUE);
    }
    public boolean isBottomValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.BOTTOM_VALUE);
    }
    public boolean isCapitalizeValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.CAPITALIZE_VALUE);
    }
    public boolean isCenterValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.CENTER_VALUE);
    }
    public boolean isCircleValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.CIRCLE_VALUE);
    }
    public boolean isCollapseValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.COLLAPSE_VALUE);
    }
    public boolean isDashedValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.DASHED_VALUE);
    }
    public boolean isDecimalValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.DECIMAL_VALUE);
    }
    public boolean isDiscValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.DISC_VALUE);
    }
    public boolean isDottedValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.DOTTED_VALUE);
    }
    public boolean isDoubleValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.DOUBLE_VALUE);
    }
    public boolean isFixedValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.FIXED_VALUE);
    }
    public boolean isGridValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.GRID_VALUE);
    }
    public boolean isGrooveValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.GROOVE_VALUE);
    }
    public boolean isHiddenValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.HIDDEN_VALUE);
    }
    public boolean isLeftValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.LEFT_VALUE);
    }
    public boolean isListItemValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.LIST_ITEM_VALUE);
    }
    public boolean isLowerAlphaValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.LOWER_ALPHA_VALUE);
    }
    public boolean isLowerCaseValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.LOWERCASE_VALUE);
    }
    public boolean isLowerLatinValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.LOWER_LATIN_VALUE);
    }
    public boolean isLowerRomanValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.LOWER_ROMAN_VALUE);
    }
    public boolean isInlineValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.INLINE_VALUE);
    }
    public boolean isInlineBlockValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.INLINE_BLOCK_VALUE);
    }
    public boolean isInsetValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.INSET_VALUE);
    }
    public boolean isJustifyValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.JUSTIFY_VALUE);
    }
    public boolean isMiddleValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.MIDDLE_VALUE);
    }
    public boolean isNoneValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.NONE_VALUE);
    }
    public boolean isNormalValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.NORMAL_VALUE);
    }
    public boolean isNoRepeatValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.NO_REPEAT_VALUE);
    }
    public boolean isNoWrapValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.NOWRAP_VALUE);
    }
    public boolean isOutsetValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.OUTSET_VALUE);
    }
    public boolean isPreValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.PRE_VALUE);
    }
    public boolean isPreWrapValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.PRE_WRAP_VALUE);
    }
    public boolean isRaveCenterValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.RAVECENTER_VALUE);
    }
    public boolean isRelativeValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.RELATIVE_VALUE);
    }
    public boolean isRepeatValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.REPEAT_VALUE);
    }
    public boolean isRepeatXValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.REPEAT_X_VALUE);
    }
    public boolean isRepeatYValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.REPEAT_Y_VALUE);
    }
    public boolean isRidgeValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.RIDGE_VALUE);
    }
    public boolean isRightValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.RIGHT_VALUE);
    }
    public boolean isSmallCapsValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.SMALL_CAPS_VALUE);
    }
    public boolean isSolidValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.SOLID_VALUE);
    }
    public boolean isSquareValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.SQUARE_VALUE);
    }
    public boolean isStaticValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.STATIC_VALUE);
    }
    public boolean isSubValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.SUB_VALUE);
    }
    public boolean isSuperValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.SUPER_VALUE);
    }
    public boolean isTableValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TABLE_VALUE);
    }
    public boolean isTableCaptionValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TABLE_CAPTION_VALUE);
    }
    public boolean isTableCellValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TABLE_CELL_VALUE);
    }
    public boolean isTableColumnValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TABLE_COLUMN_VALUE);
    }
    public boolean isTableColumnGroupValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TABLE_COLUMN_GROUP_VALUE);
    }
    public boolean isTableFooterGroupValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TABLE_FOOTER_GROUP_VALUE);
    }
    public boolean isTableHeaderGroupValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TABLE_HEADER_GROUP_VALUE);
    }
    public boolean isTableRowGroupValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TABLE_ROW_GROUP_VALUE);
    }
    public boolean isTableRowValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TABLE_ROW_VALUE);
    }
    public boolean isTextBottomValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TEXT_BOTTOM_VALUE);
    }
    public boolean isTextTopValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TEXT_TOP_VALUE);
    }
    public boolean isTopValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.TOP_VALUE);
    }
    public boolean isUpperAlphaValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.UPPER_ALPHA_VALUE);
    }
    public boolean isUpperCaseValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.UPPERCASE_VALUE);
    }
    public boolean isUpperLatinValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.UPPER_LATIN_VALUE);
    }
    public boolean isUpperRomanValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.UPPER_ROMAN_VALUE);
    }
    public boolean isVisibleValue(CssValue cssValue) {
        return isCssValueConstant(cssValue, CssValueConstants.VISIBLE_VALUE);
    }

    private static boolean isCssValueConstant(CssValue cssValue, Value constant) {
        if (cssValue == null) {
            return false;
        }
        
        return ((CssValueImpl)cssValue).getValue() == constant;
    }
    
    public boolean isOfPrimitivePercentageType(CssValue cssValue) {
        if (!(cssValue instanceof CssValueImpl)) {
            return false;
        }
        return ((CssValueImpl)cssValue).getValue().getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE;
    }
    
    public boolean isOfPrimitiveEmsType(CssValue cssValue) {
        if (!(cssValue instanceof CssValueImpl)) {
            return false;
        }
        return ((CssValueImpl)cssValue).getValue().getPrimitiveType() == CSSPrimitiveValue.CSS_EMS;
    }

    public CssValue getBothCssValueConstant() {
        return CssValueFactory.createCssValue(CssValueConstants.BOTH_VALUE);
    }
    public CssValue getCollapseCssValueConstant() {
        return CssValueFactory.createCssValue(CssValueConstants.COLLAPSE_VALUE);
    }
    public CssValue getDecimalCssValueConstant() {
        return CssValueFactory.createCssValue(CssValueConstants.DECIMAL_VALUE);
    }
    public CssValue getDiscCssValueConstant() {
        return CssValueFactory.createCssValue(CssValueConstants.DISC_VALUE);
    }
    public CssValue getTableFooterGroupValueConstant() {
        return CssValueFactory.createCssValue(CssValueConstants.TABLE_FOOTER_GROUP_VALUE);
    }
    public CssValue getTableHeaderGroupValueConstant() {
        return CssValueFactory.createCssValue(CssValueConstants.TABLE_HEADER_GROUP_VALUE);
    }
    public CssValue getTableRowGroupValueConstant() {
        return CssValueFactory.createCssValue(CssValueConstants.TABLE_ROW_GROUP_VALUE);
    }
    public CssValue getTableRowValueConstant() {
        return CssValueFactory.createCssValue(CssValueConstants.TABLE_ROW_VALUE);
    }

    public boolean isPositionProperty(String property) {
        if (property == null) {
            return false;
        }
        return property.equals(CssConstants.CSS_CLEAR_PROPERTY)
            || property.equals(CssConstants.CSS_FLOAT_PROPERTY)
            || property.equals(CssConstants.CSS_HEIGHT_PROPERTY)
            || property.equals(CssConstants.CSS_LEFT_PROPERTY)
            || property.equals(CssConstants.CSS_RIGHT_PROPERTY)
            || property.equals(CssConstants.CSS_POSITION_PROPERTY)
            || property.equals(CssConstants.CSS_TOP_PROPERTY)
            || property.equals(CssConstants.CSS_Z_INDEX_PROPERTY)
            || property.equals(CssConstants.CSS_WIDTH_PROPERTY);
    }
            
    public boolean isTextProperty(String property) {
        return property.startsWith("font-") // NOI18N
            || property.startsWith("text-") // NOI18N
            || property.equals(CssConstants.CSS_VERTICAL_ALIGN_PROPERTY)
            || property.equals(CssConstants.CSS_WHITE_SPACE_PROPERTY)
            || property.equals(CssConstants.CSS_COLOR_PROPERTY)
            || property.equals(CssConstants.CSS_LETTER_SPACING_PROPERTY)
            || property.equals(CssConstants.CSS_WORD_SPACING_PROPERTY);
    }

    public boolean isAutoValue(String value) {
        if (value == null) {
            return false;
        }
        
        return value.equals(CssConstants.CSS_AUTO_VALUE);
    }
    
    public String getAbsoluteValue() {
        return CssConstants.CSS_ABSOLUTE_VALUE;
    }
    
    public String getGridValue() {
        return CssConstants.CSS_GRID_VALUE;
    }
    
    public String getHeightProperty() {
        return CssConstants.CSS_HEIGHT_PROPERTY;
    }
    
    public String getWidthProperty() {
        return CssConstants.CSS_WIDTH_PROPERTY;
    }

    public boolean hasNoUnits(String value) {
        return XhtmlCssEngine.hasNoUnits(value);
    }
    
//    /**
//     * key for a font table
//     */
//    private static class FontKey {
//        private String family;
//        private int style;
//        private int size;
//
//        /**
//         * Constructs a font key.
//         */
//        public FontKey(String family, int style, int size) {
//            setValue(family, style, size);
//        }
//
//        public void setValue(String family, int style, int size) {
//            this.family = (family != null) ? family.intern() : null;
//            this.style = style;
//            this.size = size;
//        }
//
//        /**
//         * Returns a hashcode for this font.
//         * @return     a hashcode value for this font.
//         */
//        public int hashCode() {
//            int fhash = (family != null) ? family.hashCode() : 0;
//
//            return fhash ^ style ^ size;
//        }
//
//        /**
//         * Compares this object to the specifed object.
//         * The result is <code>true</code> if and only if the argument is not
//         * <code>null</code> and is a <code>Font</code> object with the same
//         * name, style, and point size as this font.
//         * @param     obj   the object to compare this font with.
//         * @return    <code>true</code> if the objects are equal;
//         *            <code>false</code> otherwise.
//         */
//        public boolean equals(Object obj) {
//            if (obj instanceof FontKey) {
//                FontKey font = (FontKey)obj;
//
//                return (size == font.size) && (style == font.style) && (family == font.family);
//            }
//
//            return false;
//        }
//    } // End of FontKey

    // XXX Moved from designer/../ContainerBox.
    public boolean isInlineTag(CssValue cssDisplay, Element element, HtmlTag tag) {
//        if (display == CssValueConstants.NONE_VALUE) {
        if (isNoneValue(cssDisplay)) {
            return false;
        }

//        if ((display == CssValueConstants.BLOCK_VALUE) ||
//                (display == CssValueConstants.LIST_ITEM_VALUE) ||
//                (display == CssValueConstants.TABLE_VALUE) ||
//                (
//            /* These are not always block
//            display == CssValueConstants.COMPACT_VALUE ||
//            display == CssValueConstants.RUN_IN_VALUE ||
//             */
//            display == CssValueConstants.INLINE_BLOCK_VALUE)) {
        if (isBlockValue(cssDisplay)
        || isListItemValue(cssDisplay)
        || isTableValue(cssDisplay)
        || isInlineBlockValue(cssDisplay)){
            return false;
//        } else if (display == CssValueConstants.INLINE_VALUE) {
        } else if (isInlineValue(cssDisplay)) {
            return true;

            // TODO: Handle rest of constants appropriately.
            // The "inline" boolean flag is too simplistic; we should
            // store the formatting type here and do the right type
            // of layout

            /*
              CssValueConstants.COMPACT_VALUE,
              CssValueConstants.INLINE_TABLE_VALUE,
              CssValueConstants.MARKER_VALUE,
              CssValueConstants.RUN_IN_VALUE,
              CssValueConstants.TABLE_VALUE,
              CssValueConstants.TABLE_CAPTION_VALUE,
              CssValueConstants.TABLE_CELL_VALUE,
              CssValueConstants.TABLE_COLUMN_VALUE,
              CssValueConstants.TABLE_COLUMN_GROUP_VALUE,
              CssValueConstants.TABLE_FOOTER_GROUP_VALUE,
              CssValueConstants.TABLE_HEADER_GROUP_VALUE,
              CssValueConstants.TABLE_ROW_VALUE,
              CssValueConstants.TABLE_ROW_GROUP_VALUE,
             */
        } else {
            // Else - use tag default
            if (tag == null) {
                tag = HtmlTag.getTag(element.getTagName());
            }

            if (tag != null) {
                return tag.isInlineTag();
            }
        }

        return true;
    }

    /** XXX */
    public int getCssLength(Element element, int property) {
//        Value val = getValue(element, property);
        CssValue cssValue = CssEngineServiceImpl.get().getComputedValueForElement(element, property);
        
        // XXX #6460007 Possible NPE.
        if (cssValue == null) {
            // XXX What value to return?
            return 0;
        }
        
//        if (val == CssValueConstants.AUTO_VALUE) {
        if (isAutoValue(cssValue)) {
            return CssValue.AUTO;
        }
        
//        return (int)val.getFloatValue();
        return (int)cssValue.getFloatValue();
    }
    
}
