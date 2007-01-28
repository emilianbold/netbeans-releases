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
 * This class provides a manager for the "text-transform" CSS property
 *
 * @author Tor Norbye
 */
public class TextTransformManager extends IdentifierManager {

    protected final static StringMap values = new StringMap();
    static {
        values.put(CssConstants.CSS_CAPITALIZE_VALUE,
                   CssValueConstants.CAPITALIZE_VALUE);
        values.put(CssConstants.CSS_UPPERCASE_VALUE,
                   CssValueConstants.UPPERCASE_VALUE);
        values.put(CssConstants.CSS_LOWERCASE_VALUE,
                   CssValueConstants.LOWERCASE_VALUE);
        values.put(CssConstants.CSS_NONE_VALUE,
                   CssValueConstants.NONE_VALUE);
    }

    public boolean isInheritedProperty() {
        return true;
    }

    public String getPropertyName() {
        return CssConstants.CSS_TEXT_TRANSFORM_PROPERTY;
    }

    public Value getDefaultValue() {
        return CssValueConstants.NONE_VALUE;
    }

    protected StringMap getIdentifiers() {
        return values;
    }
}
