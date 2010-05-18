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

import java.util.HashMap;
import java.util.Map;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueFactory;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.css2.FontSizeManager;
import org.apache.batik.css.engine.value.css2.FontStyleManager;
import org.apache.batik.css.engine.value.css2.FontVariantManager;
import org.apache.batik.css.engine.value.css2.FontWeightManager;
import org.openide.util.NbBundle;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * Represents the "font" shorthand property for setting
 * font-size, font-style, font-variant, font-family, and line-height
 *
 * @todo Need to implement reset-semantics for all the properties
 *  that aren't specified
 * @todo Handle the system font names better than today
 *
 * @author Tor Norbye
 */
public class FontShorthandManager
    extends AbstractValueFactory
    implements ShorthandManager {

    public String getPropertyName() {
        return CssConstants.CSS_FONT_PROPERTY;
    }

//    private static HashMap systemFonts = new HashMap(15);
    private static Map<String, String> systemFonts = new HashMap<String, String>(15);
    static {
        systemFonts.put("caption", "caption"); // NOI18N
        systemFonts.put("icon", "icon"); // NOI18N
        systemFonts.put("menu", "menu"); // NOI18N
        systemFonts.put("message-box", "message-box"); // NOI18N
        systemFonts.put("small-caption", "small-caption"); // NOI18N
        systemFonts.put("status-bar", "status-bar"); // NOI18N
    }

    public void setValues(CSSEngine eng,
                          ShorthandManager.PropertyHandler ph,
                          LexicalUnit lu,
                          boolean imp)
        throws DOMException {


        // The value manager should reset any property which is not set
        LexicalUnit fontStyle = null, fontVariant = null, fontWeight = null;
        LexicalUnit fontSize = null, lineHeight = null, fontFamily = null;

        // The CSS spec says there's a specific property order -- but browsers
        // seem more lenient than that so we should be too. But we can use
        // the order to disambiguate.
        for (; lu != null; lu = lu.getNextLexicalUnit()) {
            switch (lu.getLexicalUnitType()) {
            /* How do I handle inherit? Ditto for BackgroundShorthandManager.
             * Is
            case LexicalUnit.SAC_INHERIT:
                return ValueConstants.INHERIT_VALUE;
            */
            case LexicalUnit.SAC_EM:
            case LexicalUnit.SAC_EX:
            case LexicalUnit.SAC_PIXEL:
            case LexicalUnit.SAC_CENTIMETER:
            case LexicalUnit.SAC_MILLIMETER:
            case LexicalUnit.SAC_INCH:
            case LexicalUnit.SAC_POINT:
            case LexicalUnit.SAC_PICA:
            case LexicalUnit.SAC_REAL:
            case LexicalUnit.SAC_PERCENTAGE:
                if (lu.getPreviousLexicalUnit() != null &&
                    lu.getPreviousLexicalUnit().getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_SLASH) {
                    // This is a line height!
                    lineHeight = lu;
                } else {
                    fontSize = lu;
                }
                break;
            case LexicalUnit.SAC_INTEGER:
                // If it's 100, 200, ..., 900, then it might be font weight
                // Else it might be font size or line height
                int i = lu.getIntegerValue();
                if (i >= 100 && i % 100 == 0) {
                    fontWeight = lu;
                } else {
                    fontSize = lu;
                }
                break;
            case LexicalUnit.SAC_IDENT:
            case LexicalUnit.SAC_STRING_VALUE:
                String s = lu.getStringValue().toLowerCase().intern();
                if (s == CssConstants.CSS_NORMAL_VALUE) {
                    // This is defined for multiple properties.
                    // Initially I started to write code here to disambiguate
                    // based on the position to figure out who to assign the 
                    // normal to. But then I realized it doesn't matter; we
                    // can ignore it. All 4 properties which support "normal"
                    // (font-style, font-variant, font-weight and line-height)
                    // have "normal" as their default/initial values! And in
                    // the code below, I will reset the values to these defaults
                    // anyway so there's no need to do any work here.
                } else if (FontStyleManager.values.get(s) != null) {
                    fontStyle = lu;
                } else if (FontSizeManager.values.get(s) != null) {
                    fontSize = lu;
                } else if (FontVariantManager.values.get(s) != null) {
                    fontVariant = lu;
                } else if (FontWeightManager.values.get(s) != null) {
                    fontWeight = lu;
                } else if (systemFonts.get(s) != null) {
                    // It's one of the system fonts... not sure how to handle this.
                    // Not commonly used luckily.
                    return; // XXX do something better here after Reef!
                    // (don't fall through since these shorthands won't include
                    // a family-name etc. which will generate a warning. The
                    // best we can do is ignore this property.
                } else {
                    // Must be a font family name!
                    fontFamily = lu;
                }
                break;
            }
        }

        if (fontSize == null || fontFamily == null) {
            // Invalid font setting -- do nothing - but warn the user
            String msg = NbBundle.getMessage(FontShorthandManager.class, "IllegalFontShorthand"); // NOI18N
            throw new DOMException(DOMException.SYNTAX_ERR, msg);
        }
        
        if (fontStyle != null) {
            ph.property(CssConstants.CSS_FONT_STYLE_PROPERTY, fontStyle, imp);
        }
//        else {
//            // Reset property to default value;
//            // I should add a method for this to the PropertyHandler interface, 
//            // but it will have to be after Reef since we're minimizing
//            // risk and the number of changes at this point
//            // XXX TODO
//            //ph.reset(XhtmlCss.FONT_STYLE_INDEX);
//        }
        
        if (fontVariant != null) {
            ph.property(CssConstants.CSS_FONT_VARIANT_PROPERTY, fontVariant, imp);
        }
//        else {
//            //ph.reset(XhtmlCss.FONT_VARIANT_INDEX);
//        }
        
        if (fontWeight != null) {
            ph.property(CssConstants.CSS_FONT_WEIGHT_PROPERTY, fontWeight, imp);
        }
        
//        else {
//            //ph.reset(XhtmlCss.FONT_WEIGHT_INDEX);
//        }
        
        if (fontSize != null) {
            ph.property(CssConstants.CSS_FONT_SIZE_PROPERTY, fontSize, imp);
        }
        
//        else {
//            //ph.reset(XhtmlCss.FONT_SIZE_INDEX);
//        }
        
        if (lineHeight != null) {
            ph.property(CssConstants.CSS_LINE_HEIGHT_PROPERTY, lineHeight, imp);
        }
//        else {
//            //ph.reset(XhtmlCss.LINE_HEIGHT_INDEX);
//        }
        
        if (fontFamily != null) {
            ph.property(CssConstants.CSS_FONT_FAMILY_PROPERTY, fontFamily, imp);
        }
//        else {
//            //ph.reset(XhtmlCss.FONT_FAMILY_INDEX);
//        }
        
    }
}
