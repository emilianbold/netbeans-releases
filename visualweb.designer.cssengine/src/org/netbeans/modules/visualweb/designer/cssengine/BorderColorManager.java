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
import org.apache.batik.css.engine.value.Value;

/**
 * This class provides a manager for the 'border-x-color' property values,
 * where x is left, right, top or bottom.
 *
 * @author Tor Norbye
 */
public class BorderColorManager extends BackgroundColorManager {

    private String property;

    public BorderColorManager(String property) {
        this.property = property;
    }

    public boolean isInheritedProperty() {
	return false;
    }

    public String getPropertyName() {
        return property;
    }

    public Value getDefaultValue() {
        // The default here should be the value of the "color" property
        // For now however this case will be discovered on the client side
        // XXX That's not right, since "transparent" and "not set, so use
        // color" are two different answers! Figure out how to use the
        // ValueManager infrastructure to change the lookup to return
        // the color value at this point.
        return CssValueConstants.TRANSPARENT_VALUE;
    }

    public Value computeValue(CSSStylableElement elt,
                              String pseudo,
                              CSSEngine engine,
                              int idx,
                              StyleMap sm,
                              Value value) {
        if (value == CssValueConstants.TRANSPARENT_VALUE) {
            return CssValueConstants.TRANSPARENT_RGB_VALUE;
        }
        return super.computeValue(elt, pseudo, engine, idx, sm, value);
    }
}
