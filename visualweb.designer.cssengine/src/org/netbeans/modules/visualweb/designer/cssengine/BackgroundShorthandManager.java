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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueFactory;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.svg.ColorManager;
import org.apache.batik.css.parser.CSSLexicalUnit;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;


/**
 * Represents the "background" shorthand property for setting
 * background-color, background-image, background-position,
 * background-repeat, and background-attachment.
 *
 * @todo I need to RESET all the values for the various properties...
 *
 * @author Tor Norbye
 */
public class BackgroundShorthandManager extends AbstractValueFactory implements ShorthandManager {
    public String getPropertyName() {
        return CssConstants.CSS_BACKGROUND_PROPERTY;
    }

    public void setValues(CSSEngine eng, ShorthandManager.PropertyHandler ph, LexicalUnit lu,
        boolean imp) throws DOMException {
        // I cannot handle background-position lexical units immediately because there
        // may be multiple, and these should be sent in as a group.
//        ArrayList positions = new ArrayList(2);
        List<CSSLexicalUnit> positions = new ArrayList<CSSLexicalUnit>(2);

        for (; lu != null; lu = lu.getNextLexicalUnit()) {
            switch (lu.getLexicalUnitType()) {
            /* Inherit isn't allowed is it?
            case LexicalUnit.SAC_INHERIT:
            return ValueConstants.INHERIT_VALUE;
            */
            case LexicalUnit.SAC_URI:
                ph.property(CssConstants.CSS_BACKGROUND_IMAGE_PROPERTY, lu, imp);

                break;

            case LexicalUnit.SAC_RGBCOLOR:
                ph.property(CssConstants.CSS_BACKGROUND_COLOR_PROPERTY, lu, imp);

                break;

            case LexicalUnit.SAC_INTEGER: {
                // I've gotta duplicate the unit (without having a list) such
                // that the BackgroundPositionManager doesn't look at the
                // next lexical unit to use it as a second number
                CSSLexicalUnit lu2 = CSSLexicalUnit.createInteger(lu.getIntegerValue(), null);
                positions.add(lu2);

                break;
            }

            case LexicalUnit.SAC_EM:
            case LexicalUnit.SAC_EX:
            case LexicalUnit.SAC_PIXEL:
            case LexicalUnit.SAC_CENTIMETER:
            case LexicalUnit.SAC_MILLIMETER:
            case LexicalUnit.SAC_INCH:
            case LexicalUnit.SAC_POINT:
            case LexicalUnit.SAC_PICA:
            case LexicalUnit.SAC_REAL:
            case LexicalUnit.SAC_PERCENTAGE: {
                // I've gotta duplicate the unit (without having a list) such
                // that the BackgroundPositionManager doesn't look at the
                // next lexical unit to use it as a second number
                CSSLexicalUnit lu2 =
                    CSSLexicalUnit.createFloat(lu.getLexicalUnitType(), lu.getFloatValue(), null);
                positions.add(lu2);

                break;
            }

            case LexicalUnit.SAC_IDENT:
            case LexicalUnit.SAC_STRING_VALUE:

                String s = lu.getStringValue().toLowerCase().intern();

                if (BackgroundPositionManager.values.get(s) != null) {
                    // I've gotta duplicate the unit (without having a list) such
                    // that the BackgroundPositionManager doesn't look at the
                    // next lexical unit to use it as a second number
                    CSSLexicalUnit lu2 =
                        CSSLexicalUnit.createString(lu.getLexicalUnitType(), lu.getStringValue(),
                            null);
                    positions.add(lu2);
                } else if (ColorManager.values.get(s) != null) {
                    ph.property(CssConstants.CSS_BACKGROUND_COLOR_PROPERTY, lu, imp);
                } else if (BackgroundRepeatManager.values.get(s) != null) {
                    ph.property(CssConstants.CSS_BACKGROUND_REPEAT_PROPERTY, lu, imp);

                    /*
                    } else if (BackgroundAttachmentManager.values.get(s) != null) {
                    ph.property(CssConstants.CSS_BACKGROUND_ATTACHMENT_PROPERTY, lu, imp);
                    */
                }

                break;
            }
        }

        // Process positions.
        if (positions.size() == 1) {
            ph.property(CssConstants.CSS_BACKGROUND_POSITION_PROPERTY, positions.get(0), imp);
        } else if (positions.size() > 1) {
            CSSLexicalUnit prev = null;
//            Iterator it = positions.iterator();
            Iterator<CSSLexicalUnit> it = positions.iterator();

            while (it.hasNext()) {
                CSSLexicalUnit lu2 = it.next();

                if (prev != null) {
                    prev.setNextLexicalUnit(lu2);
                }

                lu2.setPreviousLexicalUnit(prev);
                prev = lu2;
            }

            ph.property(CssConstants.CSS_BACKGROUND_POSITION_PROPERTY, positions.get(0), imp);
        }
    }
}
