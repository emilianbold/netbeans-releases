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

import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.RGBColorValue;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.SVGValueConstants;
import org.w3c.dom.css.CSSPrimitiveValue;


public interface CssValueConstants extends SVGValueConstants {

    // Numbers missing from ValueConstants and SVGValueConstants
    Value NUMBER_3 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 3);
    Value NUMBER_5 = new FloatValue(CSSPrimitiveValue.CSS_NUMBER, 5);

    /*
      Make sure you don't add the following in here:
      I'm relying on these getting inherited from the parent class
      and having a local copy would confuse code which refers
      to CssValueConstants.INHERIT or NONE
      INHERIT_VALUE
      NONE_VALUE
    */

    // "transparent", as used for fill-colors in border-colors,
    // background-colors, etc.
    Value TRANSPARENT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_TRANSPARENT_VALUE);

    Value TRANSPARENT_RGB_VALUE =
        new RGBColorValue(NUMBER_255, NUMBER_255, NUMBER_255);


    // Border Styles
    Value HIDDEN_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_HIDDEN_VALUE);

    Value DOTTED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_DOTTED_VALUE);

    Value DASHED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_DASHED_VALUE);

    Value SOLID_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_SOLID_VALUE);

    Value DOUBLE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_DOUBLE_VALUE);

    Value GROOVE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_GROOVE_VALUE);

    Value RIDGE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_RIDGE_VALUE);

    Value INSET_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_INSET_VALUE);

    Value OUTSET_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_OUTSET_VALUE);

    // Border thicknesses
    Value THIN_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_THIN_VALUE);

    Value MEDIUM_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_MEDIUM_VALUE);

    Value THICK_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_THICK_VALUE);

    // Position types
    Value STATIC_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_STATIC_VALUE);
    Value RELATIVE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_RELATIVE_VALUE);
    Value ABSOLUTE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_ABSOLUTE_VALUE);
    Value FIXED_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_FIXED_VALUE);

    // Float types
    Value LEFT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_LEFT_VALUE);
    Value RIGHT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_RIGHT_VALUE);

    // Background Repeat values
    Value REPEAT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_REPEAT_VALUE);

    Value REPEAT_X_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_REPEAT_X_VALUE);

    Value REPEAT_Y_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_REPEAT_Y_VALUE);

    Value NO_REPEAT_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_NO_REPEAT_VALUE);

    // Text Alignment Values
    Value CENTER_VALUE = // also used for background-positioning etc.
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_CENTER_VALUE);

    // BEGIN RAVE MODIFICATIONS
    Value RAVECENTER_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_RAVECENTER_VALUE);
    // END RAVE MODIFICATIONS
    
    Value JUSTIFY_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_JUSTIFY_VALUE);

    // List Style Type choices
    Value LOWER_ROMAN_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_LOWER_ROMAN_VALUE);

    Value DISC_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_DISC_VALUE);

    Value CIRCLE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_CIRCLE_VALUE);

    Value DECIMAL_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_DECIMAL_VALUE);

    Value DECIMAL_LEADING_ZERO_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_DECIMAL_LEADING_ZERO_VALUE);

    Value UPPER_ROMAN_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_UPPER_ROMAN_VALUE);

    Value LOWER_LATIN_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_LOWER_LATIN_VALUE);

    Value LOWER_ALPHA_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_LOWER_ALPHA_VALUE);

    Value UPPER_LATIN_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_UPPER_LATIN_VALUE);

    Value UPPER_ALPHA_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_UPPER_ALPHA_VALUE);

    // -rave-layout choices
    Value GRID_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_GRID_VALUE);
    Value FLOW_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_FLOW_VALUE);

    // Border Collapse choices
    Value COLLAPSE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_COLLAPSE_VALUE);
    Value SEPARATE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_SEPARATE_VALUE);

    // Whitespace choices
    Value PRE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_PRE_VALUE);
    Value NOWRAP_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_NOWRAP_VALUE);
    Value PRE_WRAP_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_PRE_WRAP_VALUE);
    Value PRE_LINE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_PRE_LINE_VALUE);

    // Clear values
    Value BOTH_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_BOTH_VALUE);

    // Text Transform values
    Value CAPITALIZE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_CAPITALIZE_VALUE);
    Value UPPERCASE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_UPPERCASE_VALUE);
    Value LOWERCASE_VALUE =
        new StringValue(CSSPrimitiveValue.CSS_IDENT,
                        CssConstants.CSS_LOWERCASE_VALUE);
}
