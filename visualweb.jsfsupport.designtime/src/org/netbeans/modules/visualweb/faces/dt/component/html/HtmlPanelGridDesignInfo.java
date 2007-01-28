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
package org.netbeans.modules.visualweb.faces.dt.component.html;

import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.rave.designtime.*;
import com.sun.rave.designtime.markup.*;
import javax.faces.component.html.HtmlPanelGrid;

public class HtmlPanelGridDesignInfo extends HtmlDesignInfoBase implements MarkupDesignInfo {

    public Class getBeanClass() {
        return HtmlPanelGrid.class;
    }


    public Result beanCreatedSetup(DesignBean bean) {
        if (bean.getBeanParent().getInstance() instanceof HtmlPanelGrid) {
            bean.getProperty("style").setValue("width: 100%; height: 100%;");
        }
        return Result.SUCCESS;
    }

    public void customizeRender(final MarkupDesignBean bean, MarkupRenderContext renderContext) {
        DocumentFragment documentFragment = renderContext.getDocumentFragment();
        MarkupPosition begin = renderContext.getBeginPosition();
        MarkupPosition end = renderContext.getEndPosition();

        if (begin == end) {
            // Grid panel rendered nothing, so bail
            return;
        }

        assert begin.getUnderParent() == end.getUnderParent();
        Node child = begin.getBeforeSibling();
        Node stop = end.getBeforeSibling();

        if (bean.isContainer() && bean.getChildBeanCount() > 0) {
            // Grid panel has one or more children. Draw a rave design border around the table as
            // well around the individual cells
//            for (child = begin.getBeforeSibling(); child != stop; child = child.getNextSibling()) {
//                if (child instanceof Element && ((Element) child).getLocalName().equals("table")) {
//                    Element e = (Element)child;
//                    NodeList cellNodeList = e.getElementsByTagName("td");
//                    for (int i = 0; i < cellNodeList.getLength(); i++ ) {
//                        Element c = (Element) cellNodeList.item(i);
//                        c.setAttribute("class", "rave-design-border"); // NOI18N
//                    }
//                }
//            }
        } else {
            // Grid panel has no children. Set default height, width, and border
            for (child = begin.getBeforeSibling(); child != stop; child = child.getNextSibling()) {
                if (child instanceof Element) {
                    Element e = (Element)child;
                    String style = e.getAttribute("style"); //NOI18N
                    if (style == null) {
                        style = ""; //NOI18N
                    }
                    // This is wrong because "font-height" will be a match for
                    // example
                    if (style.indexOf("width") < 0 || style.indexOf("height") < 0) { //NOI18N
                        StringBuffer sb = new StringBuffer(style.length()+30);
                        sb.append(style);
                        if (!style.endsWith(";")) { //NOI18N
                            sb.append(';');
                        }
                        if (e.getAttribute("width").length() == 0 &&
                                style.indexOf("width") < 0) { //NOI18N
                            sb.append(" width: 96px;"); //NOI18N
                        }
                        if (e.getAttribute("height").length() == 0 &&
                                style.indexOf("height") < 0) { //NOI18N
                            sb.append(" height: 96px;"); //NOI18N
                        }
                        style = sb.toString();
                        e.setAttribute("style", style); //NOI18N
                    }
                    // Draw a rave design border around the panel
                    e.setAttribute("class", "rave-design-border"); // NOI18N
                    break;
                }
            }
        }
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        // XXX TODO get rid of using strings to describe the class.
        if (childClass.getName().equals("org.netbeans.modules.visualweb.xhtml.Jsp_Directive_Include"))
            return false;
        return super.acceptChild(parentBean, childBean, childClass);
    }
    
}
