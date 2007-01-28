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
package org.netbeans.modules.visualweb.css2;

import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.openide.ErrorManager;
import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;



/**
 * Class which represents a type of box - absolutel positioned,
 * normal/static position, floats, etc.
 * It's really an enumerated type.
 * @author Tor Norbye
 */
public class BoxType {
    public static final BoxType NONE     = new BoxType("none"); // NOI18N
    public static final BoxType STATIC   = new BoxType("static"); // NOI18N
    public static final BoxType ABSOLUTE = new BoxType("absolute"); // NOI18N
    public static final BoxType FIXED    = new BoxType("fixed"); // NOI18N
    public static final BoxType FLOAT    = new BoxType("float"); // NOI18N
    public static final BoxType RELATIVE = new BoxType("relative"); // NOI18N
    public static final BoxType LINEBOX  = new BoxType("linebox"); // NOI18N

    // Other types of boxes that are not laid out according to
    // CSS2 rules like the above, but behave as separate box types
    // needing treatment.
    public static final BoxType SPACE     = new BoxType("space"); // NOI18N
    public static final BoxType LINEBREAK = new BoxType("linebreak"); // NOI18N
    public static final BoxType TEXT      = new BoxType("text"); // NOI18N
    private final String description;

    /** Use factory method instead */
    private BoxType(String description) {
        this.description = description;
    }

    /**
     * Return the box type to use for the given element.
     * See section CSS2 spec section 9.7.
     * Should not be called on elements whose "display" CSS property
     * returns "none".
     */
    public static BoxType getBoxType(Element element) {
        // XXX This hack should not be necessary - check latest Jsf
        // drop and see if I can remove it now
        //element = getSourceElement(element);
//        assert CssLookup.getValue(element, XhtmlCss.DISPLAY_INDEX) != CssValueConstants.NONE_VALUE;
        if (CssProvider.getValueService().isNoneValue(
                CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.DISPLAY_INDEX))) {
            // XXX Why is it illegal?
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Element has none display style, element=" + element));
        }

//        Value val = CssLookup.getValue(element, XhtmlCss.FLOAT_INDEX);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.FLOAT_INDEX);


//        if (val != CssValueConstants.NONE_VALUE) {
        if (!CssProvider.getValueService().isNoneValue(cssValue)) {
            return FLOAT;
        }

//        val = CssLookup.getValue(element, XhtmlCss.POSITION_INDEX);
        cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.POSITION_INDEX);

//        if (val == CssValueConstants.STATIC_VALUE) {
        if (CssProvider.getValueService().isStaticValue(cssValue)) {
            return STATIC;
//        } else if (val == CssValueConstants.ABSOLUTE_VALUE) {
        } else if (CssProvider.getValueService().isAbsoluteValue(cssValue)) {
            return ABSOLUTE;
//        } else if (val == CssValueConstants.RELATIVE_VALUE) {
        } else if (CssProvider.getValueService().isRelativeValue(cssValue)) {
            return RELATIVE;
//        } else if (val == CssValueConstants.FIXED_VALUE) {
        } else if (CssProvider.getValueService().isFixedValue(cssValue)) {
            return FIXED;
        }

        // TODO Consult "display" and return something appropriate:
        // inline | block | list-item | run-in | compact | marker |
        // table | inline-table | table-row-group | table-header-group |
        // table-footer-group | table-row | table-column-group |
        // table-column | table-cell | table-caption | none | inherit
        return STATIC;
    }

    /** Does this box type participate in normal flow? */
    public boolean isNormalFlow() {
        // XXX "RELATIVE" - are these positioned or not?
        return (this == STATIC) || (this == RELATIVE) || (this == LINEBOX);
    }

    /** Are boxes of this type absolutely positioned? */
    public boolean isAbsolutelyPositioned() {
        return (this == ABSOLUTE) || (this == FIXED);
    }

    /** Are boxes of this type positioned? Positioned means
     * a box that allows left, right, top and/or bottom to be set.
     * In other words, absolute boxes, fixed boxes, and relative
     * boxes. Note that relative boxes are both positioned, AND
     * participate in normal flow. */
    public boolean isPositioned() {
        // XXX "RELATIVE" - are these positioned or not?
        return (this == ABSOLUTE) || (this == FIXED) || (this == RELATIVE);
    }

    /** 
     * Return whether this box represents a box for formatted inline content,
     * like text, spaces or linebreaks.
     */
    public boolean isInlineTextBox() {
        return (this == TEXT) || (this == LINEBREAK) || (this == SPACE);
    }
    
    public String getDescription() {
	return description;
    }

    public String toString() {
        return super.toString() + "[description=" + description + "]"; // NOI18N
    }
}
