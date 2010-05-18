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

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueFactory;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.svg.ColorManager;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * Represents the "border" shorthand property for setting
 * the same width, color, and style for all four borders of a box.
 *
 * @author Tor Norbye
 */
public class BorderShorthandManager
    extends AbstractValueFactory
    implements ShorthandManager {

    public String getPropertyName() {
        return CssConstants.CSS_BORDER_PROPERTY;
    }

    public void setValues(CSSEngine eng,
                          ShorthandManager.PropertyHandler ph,
                          LexicalUnit lu,
                          boolean imp)
        throws DOMException {

        for (; lu != null; lu = lu.getNextLexicalUnit()) {
            String first = null;
            String second = null;
            String third = null;
            String fourth = null;
            switch (lu.getLexicalUnitType()) {
                /* Inherit isn't allowed is it?
            case LexicalUnit.SAC_INHERIT:
                return ValueConstants.INHERIT_VALUE;
                */
            case LexicalUnit.SAC_RGBCOLOR:
                first = CssConstants.CSS_BORDER_TOP_COLOR_PROPERTY;
                second = CssConstants.CSS_BORDER_LEFT_COLOR_PROPERTY;
                third = CssConstants.CSS_BORDER_BOTTOM_COLOR_PROPERTY;
                fourth = CssConstants.CSS_BORDER_RIGHT_COLOR_PROPERTY;
                break;
            case LexicalUnit.SAC_EM:
            case LexicalUnit.SAC_EX:
            case LexicalUnit.SAC_PIXEL:
            case LexicalUnit.SAC_CENTIMETER:
            case LexicalUnit.SAC_MILLIMETER:
            case LexicalUnit.SAC_INCH:
            case LexicalUnit.SAC_POINT:
            case LexicalUnit.SAC_PICA:
            case LexicalUnit.SAC_INTEGER:
            //case LexicalUnit.SAC_PERCENTAGE: N/A
            case LexicalUnit.SAC_REAL:
                first = CssConstants.CSS_BORDER_TOP_WIDTH_PROPERTY;
                second = CssConstants.CSS_BORDER_LEFT_WIDTH_PROPERTY;
                third = CssConstants.CSS_BORDER_BOTTOM_WIDTH_PROPERTY;
                fourth = CssConstants.CSS_BORDER_RIGHT_WIDTH_PROPERTY;
                break;
            case LexicalUnit.SAC_IDENT:
            case LexicalUnit.SAC_STRING_VALUE:
                String s = lu.getStringValue().toLowerCase().intern();
                if (BorderWidthManager.values.get(s) != null) {
                    first = CssConstants.CSS_BORDER_TOP_WIDTH_PROPERTY;
                    second = CssConstants.CSS_BORDER_LEFT_WIDTH_PROPERTY;
                    third = CssConstants.CSS_BORDER_BOTTOM_WIDTH_PROPERTY;
                    fourth = CssConstants.CSS_BORDER_RIGHT_WIDTH_PROPERTY;
                } else if (BorderStyleManager.values.get(s) != null) {
                    first = CssConstants.CSS_BORDER_TOP_STYLE_PROPERTY;
                    second = CssConstants.CSS_BORDER_LEFT_STYLE_PROPERTY;
                    third = CssConstants.CSS_BORDER_BOTTOM_STYLE_PROPERTY;
                    fourth = CssConstants.CSS_BORDER_RIGHT_STYLE_PROPERTY;
                } else if (ColorManager.values.get(s) != null) {
                    first = CssConstants.CSS_BORDER_TOP_COLOR_PROPERTY;
                    second = CssConstants.CSS_BORDER_LEFT_COLOR_PROPERTY;
                    third = CssConstants.CSS_BORDER_BOTTOM_COLOR_PROPERTY;
                    fourth = CssConstants.CSS_BORDER_RIGHT_COLOR_PROPERTY;
                }
                break;
            }
            if (first != null) {
                ph.property(first, lu, imp);
                ph.property(second, lu, imp);
                ph.property(third, lu, imp);
                ph.property(fourth, lu, imp);
            }
        }
    }
}
