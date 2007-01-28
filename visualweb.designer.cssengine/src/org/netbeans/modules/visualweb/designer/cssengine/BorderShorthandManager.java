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

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueFactory;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.svg.ColorManager;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * Represents the "border" shorthand property for setting
 * the same width, color, and style for all four borders of a box.
 *
 * @author Tor Norbye
 */
public class BorderShorthandManager
    extends AbstractValueFactory
    implements ShorthandManager {

    public String getPropertyName() {
        return CssConstants.CSS_BORDER_PROPERTY;
    }

    public void setValues(CSSEngine eng,
                          ShorthandManager.PropertyHandler ph,
                          LexicalUnit lu,
                          boolean imp)
        throws DOMException {

        for (; lu != null; lu = lu.getNextLexicalUnit()) {
            String first = null;
            String second = null;
            String third = null;
            String fourth = null;
            switch (lu.getLexicalUnitType()) {
                /* Inherit isn't allowed is it?
            case LexicalUnit.SAC_INHERIT:
                return ValueConstants.INHERIT_VALUE;
                */
            case LexicalUnit.SAC_RGBCOLOR:
                first = CssConstants.CSS_BORDER_TOP_COLOR_PROPERTY;
                second = CssConstants.CSS_BORDER_LEFT_COLOR_PROPERTY;
                third = CssConstants.CSS_BORDER_BOTTOM_COLOR_PROPERTY;
                fourth = CssConstants.CSS_BORDER_RIGHT_COLOR_PROPERTY;
                break;
            case LexicalUnit.SAC_EM:
            case LexicalUnit.SAC_EX:
            case LexicalUnit.SAC_PIXEL:
            case LexicalUnit.SAC_CENTIMETER:
            case LexicalUnit.SAC_MILLIMETER:
            case LexicalUnit.SAC_INCH:
            case LexicalUnit.SAC_POINT:
            case LexicalUnit.SAC_PICA:
            case LexicalUnit.SAC_INTEGER:
            //case LexicalUnit.SAC_PERCENTAGE: N/A
            case LexicalUnit.SAC_REAL:
                first = CssConstants.CSS_BORDER_TOP_WIDTH_PROPERTY;
                second = CssConstants.CSS_BORDER_LEFT_WIDTH_PROPERTY;
                third = CssConstants.CSS_BORDER_BOTTOM_WIDTH_PROPERTY;
                fourth = CssConstants.CSS_BORDER_RIGHT_WIDTH_PROPERTY;
                break;
            case LexicalUnit.SAC_IDENT:
            case LexicalUnit.SAC_STRING_VALUE:
                String s = lu.getStringValue().toLowerCase().intern();
                if (BorderWidthManager.values.get(s) != null) {
                    first = CssConstants.CSS_BORDER_TOP_WIDTH_PROPERTY;
                    second = CssConstants.CSS_BORDER_LEFT_WIDTH_PROPERTY;
                    third = CssConstants.CSS_BORDER_BOTTOM_WIDTH_PROPERTY;
                    fourth = CssConstants.CSS_BORDER_RIGHT_WIDTH_PROPERTY;
                } else if (BorderStyleManager.values.get(s) != null) {
                    first = CssConstants.CSS_BORDER_TOP_STYLE_PROPERTY;
                    second = CssConstants.CSS_BORDER_LEFT_STYLE_PROPERTY;
                    third = CssConstants.CSS_BORDER_BOTTOM_STYLE_PROPERTY;
                    fourth = CssConstants.CSS_BORDER_RIGHT_STYLE_PROPERTY;
                } else if (ColorManager.values.get(s) != null) {
                    first = CssConstants.CSS_BORDER_TOP_COLOR_PROPERTY;
                    second = CssConstants.CSS_BORDER_LEFT_COLOR_PROPERTY;
                    third = CssConstants.CSS_BORDER_BOTTOM_COLOR_PROPERTY;
                    fourth = CssConstants.CSS_BORDER_RIGHT_COLOR_PROPERTY;
                }
                break;
            }
            if (first != null) {
                ph.property(first, lu, imp);
                ph.property(second, lu, imp);
                ph.property(third, lu, imp);
                ph.property(fourth, lu, imp);
            }
        }
    }
}
