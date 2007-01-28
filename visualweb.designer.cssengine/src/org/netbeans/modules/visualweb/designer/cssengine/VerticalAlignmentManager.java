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
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.apache.batik.css.engine.value.IdentifierProvider;

/**
 * This class provides a manager for the "vertical-align" CSS property
 *
 * @todo Fix percentage handling. Percentages are supposed to be
 *    relative to the line-height property's value for this element.
 *
 * @author Tor Norbye
 */
public class VerticalAlignmentManager extends NonautoableLengthManager implements IdentifierProvider {

/*
        // If percentage: from http://www.westciv.com/style_master/academy/css_tutorial/properties/text_layout.html
"Percentage values

Specifying vertical-align as a percentage value gives rise to a quite complicated situation. The baseline of the element is raised above the baseline of its parent element. By how much? By that percentage of the element's line-height.

For example, {vertical-align: 20%} with an element that has a line-height of 10pt, the baseline of the element will be raised 2 points above the baseline of its parent element.

You can lower the baseline of an element below the baseline of its parent by using negative percentage values."
 */

    protected final static StringMap values = new StringMap();
    static {
 	values.put(CssConstants.CSS_BASELINE_VALUE,
                   CssValueConstants.BASELINE_VALUE);
        values.put(CssConstants.CSS_SUB_VALUE,
                   CssValueConstants.SUB_VALUE);
        values.put(CssConstants.CSS_SUPER_VALUE,
                   CssValueConstants.SUPER_VALUE);
        values.put(CssConstants.CSS_TOP_VALUE,
                   CssValueConstants.TOP_VALUE);
        values.put(CssConstants.CSS_TEXT_TOP_VALUE,
                   CssValueConstants.TEXT_TOP_VALUE);
        values.put(CssConstants.CSS_MIDDLE_VALUE,
                   CssValueConstants.MIDDLE_VALUE);
        values.put(CssConstants.CSS_BOTTOM_VALUE,
                   CssValueConstants.BOTTOM_VALUE);
        values.put(CssConstants.CSS_TEXT_BOTTOM_VALUE,
                   CssValueConstants.TEXT_BOTTOM_VALUE);
    }

    public boolean isInheritedProperty() {
	return false;
    }

    public String getPropertyName() {
        return CssConstants.CSS_VERTICAL_ALIGN_PROPERTY;
    }

    public Value getDefaultValue() {
        return CssValueConstants.BASELINE_VALUE;
    }

    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
            case LexicalUnit.SAC_INHERIT:
                return CssValueConstants.INHERIT_VALUE;

            case LexicalUnit.SAC_IDENT:
                String s = lu.getStringValue().toLowerCase().intern();
                Object v = values.get(s);
                if (v == null) {
                    throw createInvalidIdentifierDOMException(s, engine);
                }
                return (Value)v;
        }
        return super.createValue(lu, engine);
    }

    public Value createStringValue(short type, String value, CSSEngine engine)
        throws DOMException {
        if (type != CSSPrimitiveValue.CSS_IDENT) {
            throw createInvalidStringTypeDOMException(type, engine);
        }
        Object v = values.get(value.toLowerCase().intern());
        if (v == null) {
            throw createInvalidIdentifierDOMException(value, engine);
        }
        return (Value)v;
    }

    protected int getOrientation() {
        return VERTICAL_ORIENTATION;
    }

    public StringMap getIdentifierMap() {
        return values;
    }
}
