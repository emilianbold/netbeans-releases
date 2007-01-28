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
 * This class provides a manager for border-top-style, border-left-style,
 * border-right-style and border-bottom-style.
 *
 * @author Tor Norbye
 */
public class BorderStyleManager extends IdentifierManager {
    private String property;

    /**
     * The identifier values.
     */
    protected final static StringMap values = new StringMap();
    static {
        values.put(CssConstants.CSS_NONE_VALUE,
                   CssValueConstants.NONE_VALUE);
        values.put(CssConstants.CSS_HIDDEN_VALUE,
                   CssValueConstants.HIDDEN_VALUE);
        values.put(CssConstants.CSS_DOTTED_VALUE,
                   CssValueConstants.DOTTED_VALUE);
        values.put(CssConstants.CSS_DASHED_VALUE,
                   CssValueConstants.DASHED_VALUE);
        values.put(CssConstants.CSS_SOLID_VALUE,
                   CssValueConstants.SOLID_VALUE);
        values.put(CssConstants.CSS_DOUBLE_VALUE,
                   CssValueConstants.DOUBLE_VALUE);
        values.put(CssConstants.CSS_GROOVE_VALUE,
                   CssValueConstants.GROOVE_VALUE);
        values.put(CssConstants.CSS_RIDGE_VALUE,
                   CssValueConstants.RIDGE_VALUE);
        values.put(CssConstants.CSS_INSET_VALUE,
                   CssValueConstants.INSET_VALUE);
        values.put(CssConstants.CSS_OUTSET_VALUE,
                   CssValueConstants.OUTSET_VALUE);
    }

    public BorderStyleManager(String property) {
        this.property = property;
    }

    public boolean isInheritedProperty() {
	return false;
    }

    public String getPropertyName() {
        return property;
    }

    public Value getDefaultValue() {
        return CssValueConstants.NONE_VALUE;
    }

    protected StringMap getIdentifiers() {
        return values;
    }
}
