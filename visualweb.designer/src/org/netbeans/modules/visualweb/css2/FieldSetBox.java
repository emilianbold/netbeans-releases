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

import java.awt.Graphics;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.visualweb.designer.DesignerUtils;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;


/**
 * FieldSetBox represents a &lt;fieldset&gt; tag, and
 * a potential child &lt;legend&gt; tag
 *
 * TODO -- this class is NOT DONE!!
 *
 * @author Tor Norbye
 */
public class FieldSetBox extends ContainerBox {

    /** Use the "getObjectBox" factory method instead */
    private FieldSetBox(WebForm webform, Element element, BoxType boxType, boolean inline,
        boolean replaced) {
        super(webform, element, boxType, inline, replaced);
    }

    /** Create a new framebox, or provide one from a cache */
    public static CssBox getFieldSetBox(WebForm webform, Element element, BoxType boxType,
        HtmlTag tag, boolean inline) {
        return new FieldSetBox(webform, element, boxType, inline, false);
    }

    // Fieldsets get an automatic border
    protected void initializeBorder() {
        int defWidth = 1;
        int defStyle = CssBorder.STYLE_SOLID;
        border = CssBorder.getBorder(getElement(), defWidth, defStyle, CssBorder.FRAME_UNSET);

        if (border != null) {
            leftBorderWidth = border.getLeftBorderWidth();
            topBorderWidth = border.getTopBorderWidth();
            bottomBorderWidth = border.getBottomBorderWidth();
            rightBorderWidth = border.getRightBorderWidth();
        }

        // Create the field set label
        considerDesignBorder();
    }

    public void paint(Graphics g, int px, int py) {
        super.paint(g, px, py);

        if (hidden) {
            return;
        }

        // Paint frame
        //paintFacesWatermark(g, px+getX()+leftMargin, py+getY()+effectiveTopMargin);
    }

    /** When building the box hierarchy, instead of adding content
     * for children, add the string attribute content
     */
    protected void createChildren(CreateContext context) {
        Element element = getElement();
        if (element == null) {
            return;
        }

        NodeList list = element.getChildNodes();
        int len = list.getLength();
        setProbableChildCount(len);

        // Look for <legend> and special handle it
        for (int i = 0; i < len; i++) {
            org.w3c.dom.Node child = (org.w3c.dom.Node)list.item(i);

            if ((child.getNodeType() == Node.TEXT_NODE) && COLLAPSE &&
                    DesignerUtils.onlyWhitespace(child.getNodeValue())) {
                continue;
            }

            addNode(context, child, null, null, null);
        }

        /*


        NodeList list = body.getChildNodes();
        int len = list.getLength();
        setProbableChildCount(len);

        for (int i = 0; i < len; i++) {
            org.w3c.dom.Node child = (org.w3c.dom.Node) list.item(i);
            if (child.getNodeType() == Node.TEXT_NODE
                && COLLAPSE
                && Utilities.onlyWhitespace(child.getNodeValue())) {
                continue;
            }
            addNode(cc, child, null, null, null);
        }

        fixedBoxes = cc.getFixedBoxes();
        */
    }
}
