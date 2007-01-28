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

import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;

/**
 * This class provides a manager for the "list-style-type" CSS property
 *
 * @author Tor Norbye
 */
public class ListStyleTypeManager extends IdentifierManager {

    protected final static StringMap values = new StringMap();
    static {
        values.put(CssConstants.CSS_LOWER_ROMAN_VALUE,
                   CssValueConstants.LOWER_ROMAN_VALUE);
        values.put(CssConstants.CSS_DISC_VALUE,
                   CssValueConstants.DISC_VALUE);
        values.put(CssConstants.CSS_CIRCLE_VALUE,
                   CssValueConstants.CIRCLE_VALUE);
        values.put(CssConstants.CSS_SQUARE_VALUE,
                   CssValueConstants.SQUARE_VALUE);
        values.put(CssConstants.CSS_DECIMAL_VALUE,
                   CssValueConstants.DECIMAL_VALUE);
        values.put(CssConstants.CSS_DECIMAL_LEADING_ZERO_VALUE,
                   CssValueConstants.DECIMAL_LEADING_ZERO_VALUE);
        values.put(CssConstants.CSS_UPPER_ROMAN_VALUE,
                   CssValueConstants.UPPER_ROMAN_VALUE);
        values.put(CssConstants.CSS_LOWER_LATIN_VALUE,
                   CssValueConstants.LOWER_LATIN_VALUE);
        values.put(CssConstants.CSS_UPPER_LATIN_VALUE,
                   CssValueConstants.UPPER_LATIN_VALUE);
        values.put(CssConstants.CSS_NONE_VALUE,
                   CssValueConstants.NONE_VALUE);
        // "lower-alpha" and "upper-alpha" are not part of the CSS2.1
        // spec. But it seems to be used in older documents so we'll
        // support it.
        values.put(CssConstants.CSS_LOWER_ALPHA_VALUE,
                   CssValueConstants.LOWER_ALPHA_VALUE);
        values.put(CssConstants.CSS_UPPER_ALPHA_VALUE,
                   CssValueConstants.UPPER_ALPHA_VALUE);
    }

    public boolean isInheritedProperty() {
        return true;
    }

    public String getPropertyName() {
        return CssConstants.CSS_LIST_STYLE_TYPE_PROPERTY;
    }

    public Value getDefaultValue() {
        return CssValueConstants.DISC_VALUE;
    }

    protected StringMap getIdentifiers() {
        return values;
    }
}
