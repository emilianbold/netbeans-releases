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
 * Represents the "border-width" shorthand property for setting
 * border-left-width, border-right-width, border-top-width and
 * border-bottom-width.
 *
 * @author Tor Norbye
 */
public class BorderWidthShorthandManager
    extends AbstractValueFactory
    implements ShorthandManager {

    public String getPropertyName() {
        return CssConstants.CSS_BORDER_WIDTH_PROPERTY;
    }

    /** Set the values. This is a bit complicated since the number
     * of "arguments" in the value determines how we distribute
     * the children.
     */
    public void setValues(CSSEngine eng,
                          ShorthandManager.PropertyHandler ph,
                          LexicalUnit lu,
                          boolean imp)
        throws DOMException {

        LexicalUnit first = lu;
        LexicalUnit second = first.getNextLexicalUnit();
        if (second == null) {
            // Only one value specified
            // 1 value: applies to all four sides
            ph.property(CssConstants.CSS_BORDER_LEFT_WIDTH_PROPERTY,
                        first, imp);
            ph.property(CssConstants.CSS_BORDER_RIGHT_WIDTH_PROPERTY,
                        first, imp);
            ph.property(CssConstants.CSS_BORDER_TOP_WIDTH_PROPERTY,
                        first, imp);
            ph.property(CssConstants.CSS_BORDER_BOTTOM_WIDTH_PROPERTY,
                        first, imp);
        } else {
            LexicalUnit third = second.getNextLexicalUnit();
            if (third == null) {
                // Only two values specified

                // 2 values: (1) top & bottom  (2) left & right
                ph.property(CssConstants.CSS_BORDER_TOP_WIDTH_PROPERTY,
                            first, imp);
                ph.property(CssConstants.CSS_BORDER_BOTTOM_WIDTH_PROPERTY,
                            first, imp);
                ph.property(CssConstants.CSS_BORDER_LEFT_WIDTH_PROPERTY,
                            second, imp);
                ph.property(CssConstants.CSS_BORDER_RIGHT_WIDTH_PROPERTY,
                            second, imp);
            } else {
                LexicalUnit fourth = third.getNextLexicalUnit();
                if (fourth == null) {
                    // Only three values specified

                    // 3 values: (1) top, (2) left & right, (3) bottom
                    ph.property(CssConstants.CSS_BORDER_TOP_WIDTH_PROPERTY,
                                first, imp);
                    ph.property(CssConstants.CSS_BORDER_LEFT_WIDTH_PROPERTY,
                                second, imp);
                    ph.property(CssConstants.CSS_BORDER_RIGHT_WIDTH_PROPERTY,
                                second, imp);
                    ph.property(CssConstants.CSS_BORDER_BOTTOM_WIDTH_PROPERTY,
                                third, imp);
                } else {
                    // 4 values: (1) top, (2) right, (3) bottom, (4) left
                    ph.property(CssConstants.CSS_BORDER_TOP_WIDTH_PROPERTY,
                                first, imp);
                    ph.property(CssConstants.CSS_BORDER_RIGHT_WIDTH_PROPERTY,
                                second, imp);
                    ph.property(CssConstants.CSS_BORDER_BOTTOM_WIDTH_PROPERTY,
                                third, imp);
                    ph.property(CssConstants.CSS_BORDER_LEFT_WIDTH_PROPERTY,
                                fourth, imp);
                }
            }
        }
    }
}
