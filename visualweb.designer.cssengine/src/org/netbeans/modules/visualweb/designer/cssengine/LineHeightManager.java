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
