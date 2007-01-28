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

import org.w3c.dom.css.CSSPrimitiveValue;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.IdentifierProvider;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * This class provides a manager for the "background-position" CSS property
 * @todo I should make value constants for 50% and 100%
 *
 * @author Tor Norbye
 */
public class BackgroundPositionManager extends AbstractValueManager implements IdentifierProvider {

    protected final static StringMap values = new StringMap();
    static {

        values.put(CssConstants.CSS_TOP_VALUE,
                   CssValueConstants.TOP_VALUE);
        values.put(CssConstants.CSS_CENTER_VALUE,
                   CssValueConstants.CENTER_VALUE);
        values.put(CssConstants.CSS_BOTTOM_VALUE,
                   CssValueConstants.BOTTOM_VALUE);
        values.put(CssConstants.CSS_LEFT_VALUE,
                   CssValueConstants.LEFT_VALUE);
        values.put(CssConstants.CSS_RIGHT_VALUE,
                   CssValueConstants.RIGHT_VALUE);
    }

    public boolean isInheritedProperty() {
	return false;
    }

    public String getPropertyName() {
	return CssConstants.CSS_BACKGROUND_POSITION_PROPERTY;
    }

    public Value getDefaultValue() {
        return DEFAULT_VALUE;
    }

    private final static ListValue DEFAULT_VALUE = new ListValue();
    static {
        DEFAULT_VALUE.append(CssValueConstants.NUMBER_0);
        DEFAULT_VALUE.append(CssValueConstants.NUMBER_0);
    }

    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        ListValue result = new ListValue();
        // This is a little bit complicated because the user is not
        // required to put the horizontal and vertical position words
        // in a particular order, and "center" can mean both center
        // horizontally and center vertically. Thus, we have to use
        // the other word to disambiguate.
        isHorizontal = true;
        Value v1 = createNewValue(lu, engine);
        Value v2 = null;
        boolean swapped = !isHorizontal;
        LexicalUnit lu2 = lu.getNextLexicalUnit();
        if (lu2 == null) {
            v2 = new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 50.0f);
        } else {
            if (swapped) {
                isHorizontal = true;
            } else {
                isHorizontal = false;
            }
            v2 = createNewValue(lu2, engine);
            // if swapped then we expect isHorizontal = true now... otherwise
            // the user entered two vertical numbers... which is wrong
            if (swapped && !isHorizontal) {
                // Discard value
                v2 = CssValueConstants.NUMBER_0;
            }
            if (lu2.getNextLexicalUnit() != null) { // should only have two!
                throw createInvalidLexicalUnitDOMException
                        (lu.getLexicalUnitType(), engine);
            }
        }

        if (v1 == CssValueConstants.CENTER_VALUE) {
            v1 = new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 50.0f);
        }
        if (v2 == CssValueConstants.CENTER_VALUE) {
            v2 = new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 50.0f);
        }
        if (swapped) {
            result.append(v2);
            result.append(v1);
        } else {
            result.append(v1);
            result.append(v2);
        }
        return result;
    }

    /** 
     * Flag used to communicate an additional return value from createValue(LexicalUnit): whether
     * the return value implied a particular axis (e.g. top implies vertical, right implies
     * horizontal. Numbers, percentages and "center" doesn't tell us anything so createValue
     * will leave it unmodified.
     */
    private boolean isHorizontal;
    
    /** Create a value for the given lexical unit. Set the member "isHorizontal" if
     * we can infer something about whether the property applies to the horizontal
     * or vertical direction. 
     */
    private Value createNewValue(LexicalUnit lu, CSSEngine engine) {
	switch (lu.getLexicalUnitType()) {
	case LexicalUnit.SAC_INHERIT:
	    return CssValueConstants.INHERIT_VALUE;
            
	case LexicalUnit.SAC_EM:
	    return new FloatValue(CSSPrimitiveValue.CSS_EMS,
                                  lu.getFloatValue());

	case LexicalUnit.SAC_EX:
	    return new FloatValue(CSSPrimitiveValue.CSS_EXS,
                                  lu.getFloatValue());

	case LexicalUnit.SAC_PIXEL:
	    return new FloatValue(CSSPrimitiveValue.CSS_PX,
                                  lu.getFloatValue());

	case LexicalUnit.SAC_CENTIMETER:
	    return new FloatValue(CSSPrimitiveValue.CSS_CM,
                                  lu.getFloatValue());

	case LexicalUnit.SAC_MILLIMETER:
	    return new FloatValue(CSSPrimitiveValue.CSS_MM,
                                  lu.getFloatValue());

	case LexicalUnit.SAC_INCH:
	    return new FloatValue(CSSPrimitiveValue.CSS_IN,
                                  lu.getFloatValue());

	case LexicalUnit.SAC_POINT:
	    return new FloatValue(CSSPrimitiveValue.CSS_PT,
                                  lu.getFloatValue());

	case LexicalUnit.SAC_PICA:
	    return new FloatValue(CSSPrimitiveValue.CSS_PC,
                                  lu.getFloatValue());

        // XXX illegal for background-position
	case LexicalUnit.SAC_INTEGER:
	    return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getIntegerValue());

        // XXX illegal for background-position   
	case LexicalUnit.SAC_REAL:
	    return new FloatValue(CSSPrimitiveValue.CSS_NUMBER,
                                  lu.getFloatValue());
         
	case LexicalUnit.SAC_PERCENTAGE:
	    return new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE,
                                  lu.getFloatValue());
  
        case LexicalUnit.SAC_IDENT:
            String s = lu.getStringValue().toLowerCase().intern();
	    Object v = values.get(s);
            if (v == CssValueConstants.CENTER_VALUE) {
                // The caller needs to figure out if we're talking about the first
                // or the second parameter here
                return CssValueConstants.CENTER_VALUE;
            } else if (v == CssValueConstants.TOP_VALUE) {
                isHorizontal = false;
                return CssValueConstants.NUMBER_0;
            } else if (v == CssValueConstants.BOTTOM_VALUE) {
                isHorizontal = false;
                return new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 100.0f);
            } else if (v == CssValueConstants.LEFT_VALUE) {
                isHorizontal = true;
                return CssValueConstants.NUMBER_0;
            } else if (v == CssValueConstants.RIGHT_VALUE) {
                isHorizontal = true;
                return new FloatValue(CSSPrimitiveValue.CSS_PERCENTAGE, 100.0f);
            } else {
		throw createInvalidIdentifierDOMException(lu.getStringValue(), engine);
	    }
        }
        return null;
    }

    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        // XXX what about INHERIT? Can't have that!

        // We're supposed to leave percentages alone according to the CSS2.1 spec;
        // the computed value will be either a length or the percentage.
        return value;
    }

    public StringMap getIdentifierMap() {
        return values;
    }
}
