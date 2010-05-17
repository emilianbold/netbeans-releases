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
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.apache.batik.css.engine.value.IdentifierProvider;

/**
 * This class provides a manager for border-top-width, border-left-width,
 * border-right-width and border-bottom-width.
 *
 * @author Tor Norbye
 */
public class BorderWidthManager extends NonautoableLengthManager implements IdentifierProvider {
    private String property;

    /**
     * The identifier values.
     */
    protected final static StringMap values = new StringMap();
    static {
        values.put(CssConstants.CSS_THIN_VALUE,
                   CssValueConstants.THIN_VALUE);
        values.put(CssConstants.CSS_MEDIUM_VALUE,
                   CssValueConstants.MEDIUM_VALUE);
        values.put(CssConstants.CSS_THICK_VALUE,
                   CssValueConstants.THICK_VALUE);
    }

    public BorderWidthManager(String property) {
        this.property = property;
    }

    public boolean isInheritedProperty() {
	return false;
    }

    public String getPropertyName() {
        return property;
    }

    public Value getDefaultValue() {
        return CssValueConstants.MEDIUM_VALUE;
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

    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {

        if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
            return CssValueConstants.NUMBER_0;
        }

        // Border thickness - these are the values Mozilla 1.5 empirically
        // seems to use (on Solaris, hopefully not platform specific)
        // In the working draft for the CSS3 Box Model, they mention UA's
        // could make it depend on the font size, e.g. use the below sizes
        // when the font size is less than 17pt, and bump them up for bigger
        // fonts.
        if (value == CssValueConstants.THIN_VALUE) {
            return CssValueConstants.NUMBER_1;
        } else if (value == CssValueConstants.MEDIUM_VALUE) {
            return CssValueConstants.NUMBER_3;
        } else if (value == CssValueConstants.THICK_VALUE) {
            return CssValueConstants.NUMBER_5;
        }

        /*
        // absolute identifiers
        CSSContext ctx = engine.getCSSContext();
        float fs = ctx.getMediumFontSize();
        String s = value.getStringValue();
        switch (s.charAt(0)) {
        case 'm':
            break;

        case 's':
            fs = (float)(fs / 1.2);
            break;

        case 'l':
            fs = (float)(fs * 1.2);
            break;

        default: // 'x'
            switch (s.charAt(1)) {
            case 'x':
                switch (s.charAt(3)) {
                case 's':
                    fs = (float)(((fs / 1.2) / 1.2) / 1.2);
                    break;

                default: // 'l'
                    fs = (float)(fs * 1.2 * 1.2 * 1.2);
                }
                break;

            default: // '-'
                switch (s.charAt(2)) {
                case 's':
                    fs = (float)((fs / 1.2) / 1.2);
                    break;

                default: // 'l'
                    fs = (float)(fs * 1.2 * 1.2);
                }
            }
        }
        return new FloatValue(CSSPrimitiveValue.CSS_NUMBER, fs);
        */

        return super.computeValue(elt, pseudo, engine, idx, sm, value);
    }

    protected int getOrientation() {
        return HORIZONTAL_ORIENTATION; // doesn't matter / not used, we don't allow %
    }

    public StringMap getIdentifierMap() {
        return values;
    }
}
