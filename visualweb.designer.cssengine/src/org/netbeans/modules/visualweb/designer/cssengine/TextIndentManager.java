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
import org.apache.batik.css.engine.value.InheritValue;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * This class provides a manager for the "text-indent" CSS property,
 * as spec'ed in the CSS2.1 spec section 16.1.
 *
 * @author Tor Norbye
 */
public class TextIndentManager extends LengthManager {

    public boolean isInheritedProperty() {
	return true;
    }

    public String getPropertyName() {
        return CssConstants.CSS_TEXT_INDENT_PROPERTY;
    }

    public Value getDefaultValue() {
        return CssValueConstants.NUMBER_0;
    }

    protected int getOrientation() {
        return VERTICAL_ORIENTATION;
    }

    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
	if (lu.getLexicalUnitType() == LexicalUnit.SAC_INHERIT) {
	    return InheritValue.INSTANCE;
        }
        return super.createValue(lu, engine);
    }
}
