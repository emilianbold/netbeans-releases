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
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.apache.batik.css.engine.value.IdentifierProvider;

/**
 * This class provides a manager for the "line-height" CSS property
 *
 * @author Tor Norbye
 */
public class LineHeightManager extends NonautoableLengthManager implements IdentifierProvider {

    /**
     * The identifier values.
     */
    protected final static StringMap values = new StringMap();
    static {
        values.put(CssConstants.CSS_NORMAL_VALUE,
                   CssValueConstants.NORMAL_VALUE);
    }

    public boolean isInheritedProperty() {
        return true;
    }

    public String getPropertyName() {
        return CssConstants.CSS_LINE_HEIGHT_PROPERTY;
    }

    public Value getDefaultValue() {
        return CssValueConstants.NORMAL_VALUE;
    }

    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
        case LexicalUnit.SAC_INHERIT:
            return CssValueConstants.INHERIT_VALUE;

        case LexicalUnit.SAC_IDENT:
            /* See faster impl below as long as we have only a single ident
	    Object v = values.get(lu.getStringValue().toLowerCase().intern());
	    if (v == null) {
                throw createInvalidIdentifierDOMException(lu.getStringValue());
            }
            return (Value)v;
            */
            String s = lu.getStringValue();
            if (CssConstants.CSS_NORMAL_VALUE.equalsIgnoreCase(s)) {
                return CssValueConstants.NORMAL_VALUE;
            }
            throw createInvalidIdentifierDOMException(s, engine);
        }
        return super.createValue(lu, engine);
    }

    public Value createStringValue(short type, String value, CSSEngine engine)
        throws DOMException {
        if (type != CSSPrimitiveValue.CSS_IDENT) {
            throw createInvalidIdentifierDOMException(value, engine);
        }
        Object v = values.get(value.toLowerCase().intern());
        if (v == null) {
            throw createInvalidIdentifierDOMException(value, engine);
        }
        return (Value)v;
    }

    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        switch (value.getPrimitiveType()) {
            case CSSPrimitiveValue.CSS_PERCENTAGE: {
                sm.putFontSizeRelative(idx, true);
                float v = value.getFloatValue();
                int fsidx = engine.getFontSizeIndex();
                float fs;
                fs = engine.getComputedStyle(elt, pseudo, fsidx).getFloatValue();
                return new FloatValue(CSSPrimitiveValue.CSS_NUMBER, v * fs / 100);
            }
// XXX What about INTEGER AND REAL?
            case CSSPrimitiveValue.CSS_NUMBER: {
                // for line-height the number refers to multiples of the font size
                sm.putFontSizeRelative(idx, true);
                float v = value.getFloatValue();
                int fsidx = engine.getFontSizeIndex();
                float fs;
                fs = engine.getComputedStyle(elt, pseudo, fsidx).getFloatValue();
                return new FloatValue(CSSPrimitiveValue.CSS_NUMBER, v * fs);
            }
        }
        return super.computeValue(elt, pseudo, engine, idx, sm, value);
    }

    protected int getOrientation() {
        return BOTH_ORIENTATION; // Not used
    }

    public StringMap getIdentifierMap() {
        return values;
    }
}
