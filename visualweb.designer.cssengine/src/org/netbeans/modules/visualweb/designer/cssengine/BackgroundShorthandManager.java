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
