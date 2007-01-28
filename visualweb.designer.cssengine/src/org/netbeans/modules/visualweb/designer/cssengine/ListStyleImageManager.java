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
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * This class provides support for the "list-style-image" property.
 *
 * @author Tor Norbye
 */
public class ListStyleImageManager extends AbstractValueManager {

    public boolean isInheritedProperty() {
        return false;
    }

    public String getPropertyName() {
        return CssConstants.CSS_LIST_STYLE_IMAGE_PROPERTY;
    }

    public Value getDefaultValue() {
        return CssValueConstants.NONE_VALUE;
    }

    public Value createValue(LexicalUnit lu, CSSEngine engine)
        throws DOMException {
        switch (lu.getLexicalUnitType()) {
            case LexicalUnit.SAC_INHERIT:
                return CssValueConstants.INHERIT_VALUE;

            case LexicalUnit.SAC_URI:
                return new URIValue(lu.getStringValue(),
                        resolveURI(engine.getCSSBaseURI(), lu.getStringValue()));
            case LexicalUnit.SAC_IDENT:
                String s = lu.getStringValue().toLowerCase().intern();
                if (s == CssConstants.CSS_NONE_VALUE) {
                    return CssValueConstants.NONE_VALUE;
                }
                throw createInvalidIdentifierDOMException(lu.getStringValue(), engine);
        }
        throw createInvalidLexicalUnitDOMException(lu.getLexicalUnitType(), engine);
    }
}
