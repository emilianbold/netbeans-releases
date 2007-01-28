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
import org.apache.batik.css.engine.value.AbstractValueFactory;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * Represents the "list-style" shorthand property for setting
 * list-style-image, list-style-type and list-style-position
 *
 * @author Tor Norbye
 */
public class ListStyleShorthandManager extends AbstractValueFactory
    implements ShorthandManager {

    public String getPropertyName() {
        return CssConstants.CSS_LIST_STYLE_PROPERTY;
    }

    public void setValues(CSSEngine eng,
                          ShorthandManager.PropertyHandler ph,
                          LexicalUnit lu,
                          boolean imp)
        throws DOMException {

        for (; lu != null; lu = lu.getNextLexicalUnit()) {
            switch (lu.getLexicalUnitType()) {
            /* Inherit isn't allowed on this shorthand is it?
            case LexicalUnit.SAC_INHERIT:
                return ValueConstants.INHERIT_VALUE;
            */
            case LexicalUnit.SAC_URI:
                ph.property(CssConstants.CSS_LIST_STYLE_IMAGE_PROPERTY, lu, imp);
                break;
            case LexicalUnit.SAC_IDENT:
            case LexicalUnit.SAC_STRING_VALUE:
                String s = lu.getStringValue().toLowerCase().intern();
                if (ListStyleTypeManager.values.get(s) != null) {
                    ph.property(CssConstants.CSS_LIST_STYLE_TYPE_PROPERTY, lu, imp);
                } else if (s == CssConstants.CSS_NONE_VALUE) {
                    ph.property(CssConstants.CSS_LIST_STYLE_IMAGE_PROPERTY, lu, imp);
                /* Not yet implemented
                } else if (ListStylePositionManager.values.get(s) != null) {
                    ph.property(CssConstants.CSS_LIST_STYLE_POSITION_PROPERTY, lu, imp);
                */
                }
                break;
            }
        }

    }
}
