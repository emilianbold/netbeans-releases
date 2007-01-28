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

import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.designer.WebForm;


/**
 * StringBox represents a box of text whose text is specified by
 * an attribute rather than the text-node children of the box' element
 * (which is the default).

 * @author Tor Norbye
 */
public class StringBox extends ContainerBox {
    private String string;

    /**
     * Create a StringBox for the given string
     *
     * @param webform The <code>WebForm</code>
     * @param element The element this string box is associated with
     * @param boxType Type of box.
     * @param string The string to manage
     * @param width The width to use for the box
     * @param height The height to use for the box
     */
    public StringBox(WebForm webform, Element element, BoxType boxType, String string,
        CssBorder border, int width, int height) {
        super(webform, element, boxType, true, true);
        this.string = string;
        this.width = width;
        this.height = height;
        this.border = border;
    }

    protected void initializeBorder() {
        if (border == null) {
            super.initializeBorder();
        }

        if (border == null) {
            border = CssBorder.getDesignerBorder();
        }

        leftBorderWidth = border.getLeftBorderWidth();
        topBorderWidth = border.getTopBorderWidth();
        bottomBorderWidth = border.getBottomBorderWidth();
        rightBorderWidth = border.getRightBorderWidth();
    }

    public int getIntrinsicWidth() {
        if (width == AUTO) {
            if (getBoxCount() > 0) {
                assert getBox(0) instanceof LineBoxGroup;

                return getBox(0).getPrefWidth();
            } else {
                width = 30;
            }
        }

        return width;
    }

    public int getIntrinsicHeight() {
        if (height == AUTO) {
            if (getBoxCount() > 0) {
                assert getBox(0) instanceof LineBoxGroup;

                return ((LineBoxGroup)getBox(0)).getMetrics().getHeight();
            } else {
                height = 14;
            }
        }

        return height;
    }

//    public String toString() {
//        return "StringBox[" + paramString() + "]";
//    }

    protected String paramString() {
        return super.paramString() + ", " + string;
    }

    /** When building the box hierarchy, instead of adding content
     * for children, add the string attribute content
     */
    protected void createChildren(CreateContext context) {
        LineBoxGroup old = context.lineBox;
        context.lineBox = null;
        addText(context, null, getElement(), string);
        finishLineBox(context);
        context.lineBox = old;
    }

    public int getPrefWidth() {
        return getIntrinsicWidth();
    }

    public int getPrefMinWidth() {
        return getIntrinsicWidth();
    }
}
