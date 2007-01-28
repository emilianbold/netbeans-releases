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

// Why was this in SVG? What is svg specific about the implementation?
import org.apache.batik.css.engine.value.svg.ColorManager;

/**
 * This class provides a manager for the 'background-color' property values.
 *
 * @author Tor Norbye
 */
public class BackgroundColorManager extends ColorManager {

    //
    // Add some identifier values.
    //
    static {
        values.put(CssConstants.CSS_TRANSPARENT_VALUE,
                   CssValueConstants.TRANSPARENT_VALUE);
    }

    //
    // Add and replace some computed colors.
    //
    static {
        computedValues.put(CssConstants.CSS_TRANSPARENT_VALUE,
                           CssValueConstants.TRANSPARENT_RGB_VALUE);
    }

    public boolean isInheritedProperty() {
	return false;
    }

    public String getPropertyName() {
	return CssConstants.CSS_BACKGROUND_COLOR_PROPERTY;
    }

    public Value getDefaultValue() {
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
