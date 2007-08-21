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


package org.netbeans.modules.visualweb.api.designer.cssengine;


import java.awt.Color;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.w3c.dom.Element;


/**
 * Provides CSS value service.
 *
 * @author Peter Zavadsky
 */
public interface CssValueService {

    public Color getColorForElement(Element element, int styleIndex);
    public boolean isColorTransparentForElement(Element element, int styleIndex);

    public float getFontSizeForElement(Element element, int defaultSize);
    public int getFontStyleForElement(Element element, int defaultStyle);
    /** @return not-empty array of font family names */
    public String[] getFontFamilyNamesForElement(Element element);

//    /** XXX Replace with values returned, and manage the fonts out of desinger/cssengine.
//     * @return always non-empy <code>Font</code> array */
//    public Font[] getFontsForElement(Element element, int defaultSize, int defaultType);
//    /** XXX Replace with values returned, and manage the fonts out of desinger/cssengine. */
//    public FontMetrics[] getFontsMetricsForElement(Element element);

    public CssListValue getComputedCssListValue(CssValue cssValue);

    public String[] getCssLengthUnits();

    public boolean isAbsoluteValue(CssValue cssValue);
    public boolean isAutoValue(CssValue cssValue);
    public boolean isBaseLineValue(CssValue cssValue);
    public boolean isBlockValue(CssValue cssValue);
    public boolean isBothValue(CssValue cssValue);
    public boolean isBottomValue(CssValue cssValue);
    public boolean isCapitalizeValue(CssValue cssValue);
    public boolean isCenterValue(CssValue cssValue);
    public boolean isCircleValue(CssValue cssValue);
    public boolean isCollapseValue(CssValue cssValue);
    public boolean isDashedValue(CssValue cssValue);
    public boolean isDecimalValue(CssValue cssValue);
    public boolean isDiscValue(CssValue cssValue);
    public boolean isDottedValue(CssValue cssValue);
    public boolean isDoubleValue(CssValue cssValue);
    public boolean isFixedValue(CssValue cssValue);
    /** XXX Rave only, TODO get rid of this. */
    public boolean isGridValue(CssValue cssValue);
    public boolean isGrooveValue(CssValue cssValue);
    public boolean isHiddenValue(CssValue cssValue);
    public boolean isInlineBlockValue(CssValue cssValue);
    public boolean isInlineValue(CssValue cssValue);
    public boolean isInsetValue(CssValue cssValue);
    public boolean isJustifyValue(CssValue cssValue);
    public boolean isLeftValue(CssValue cssValue);
    public boolean isListItemValue(CssValue cssValue);
    public boolean isLowerAlphaValue(CssValue cssValue);
    public boolean isLowerCaseValue(CssValue cssValue);
    public boolean isLowerLatinValue(CssValue cssValue);
    public boolean isLowerRomanValue(CssValue cssValue);
    public boolean isMiddleValue(CssValue cssValue);
    public boolean isNoneValue(CssValue cssValue);
    public boolean isNormalValue(CssValue cssValue);
    public boolean isNoRepeatValue(CssValue cssValue);
    public boolean isNoWrapValue(CssValue cssValue);
    public boolean isOutsetValue(CssValue cssValue);
    public boolean isPreValue(CssValue cssValue);
    public boolean isPreWrapValue(CssValue cssValue);
    public boolean isRaveCenterValue(CssValue cssValue);
    public boolean isRelativeValue(CssValue cssValue);
    public boolean isRepeatValue(CssValue cssValue);
    public boolean isRepeatXValue(CssValue cssValue);
    public boolean isRepeatYValue(CssValue cssValue);
    public boolean isRidgeValue(CssValue cssValue);
    public boolean isRightValue(CssValue cssValue);
    public boolean isSmallCapsValue(CssValue cssValue);
    public boolean isSolidValue(CssValue cssValue);
    public boolean isSquareValue(CssValue cssValue);
    public boolean isStaticValue(CssValue cssValue);
    public boolean isSubValue(CssValue cssValue);
    public boolean isSuperValue(CssValue cssValue);
    public boolean isTableValue(CssValue cssValue);
    public boolean isTableCaptionValue(CssValue cssValue);
    public boolean isTableCellValue(CssValue cssValue);
    public boolean isTableColumnValue(CssValue cssValue);
    public boolean isTableColumnGroupValue(CssValue cssValue);
    public boolean isTableFooterGroupValue(CssValue cssValue);
    public boolean isTableHeaderGroupValue(CssValue cssValue);
    public boolean isTableRowGroupValue(CssValue cssValue);
    public boolean isTableRowValue(CssValue cssValue);
    public boolean isTextBottomValue(CssValue cssValue);
    public boolean isTextTopValue(CssValue cssValue);
    public boolean isTopValue(CssValue cssValue);
    public boolean isUpperAlphaValue(CssValue cssValue);
    public boolean isUpperCaseValue(CssValue cssValue);
    public boolean isUpperLatinValue(CssValue cssValue);
    public boolean isUpperRomanValue(CssValue cssValue);
    public boolean isVisibleValue(CssValue cssValue);
    
    public boolean isOfPrimitivePercentageType(CssValue cssValue);
    public boolean isOfPrimitiveEmsType(CssValue cssValue);
    
    public CssValue getBothCssValueConstant();
    public CssValue getCollapseCssValueConstant();
    public CssValue getDecimalCssValueConstant();
    public CssValue getDiscCssValueConstant();
    public CssValue getTableFooterGroupValueConstant();
    public CssValue getTableHeaderGroupValueConstant();
    public CssValue getTableRowGroupValueConstant();
    public CssValue getTableRowValueConstant();

    public boolean isPositionProperty(String property);
    public boolean isTextProperty(String property);
    
    public boolean isAutoValue(String value);
    
    public String getAbsoluteValue();
    public String getGridValue();
    
    public String getHeightProperty();
    public String getWidthProperty();
    
    public boolean hasNoUnits(String value);

    // XXX Get rid of the HtmlTag parameter.
    public boolean isInlineTag(CssValue cssDisplay, Element element, HtmlTag tag);
    
    /** XXX Computes css length, provides the auto value as <code>CssValue.AUTO</code>, revise that, it looks very dangerous.
     * XXX Hack, this engine impl is incorrect, it shouldn't be up to the engine to compute the final value. */
    public int getCssLength(Element element, int property);

}
