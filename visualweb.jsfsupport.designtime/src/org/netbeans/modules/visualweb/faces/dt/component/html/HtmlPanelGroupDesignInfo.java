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

import com.sun.rave.designtime.markup.*;
import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import javax.faces.component.html.HtmlPanelGroup;
import org.w3c.dom.*;

public class HtmlPanelGroupDesignInfo extends HtmlDesignInfoBase implements MarkupDesignInfo {
    public Class getBeanClass() { return HtmlPanelGroup.class; }

    public void customizeRender(MarkupDesignBean bean, MarkupRenderContext renderContext) {
        DocumentFragment documentFragment = renderContext.getDocumentFragment();
        MarkupPosition begin = renderContext.getBeginPosition();
        MarkupPosition end = renderContext.getEndPosition();

        if (bean.isContainer() && bean.getChildBeanCount() > 0) {
            return;
        }
        if (begin == end) { // table didn't render anything.... that's not right! Insert one? TODO
            return;
        }
        assert begin.getUnderParent() == end.getUnderParent();
        Node child = begin.getBeforeSibling();
        Node stop = end.getBeforeSibling();
        for (child = begin.getBeforeSibling(); child != stop; child = child.getNextSibling()) {
            if (child instanceof Element) {
                Element e = (Element)child;
                String style = e.getAttribute("style"); //NOI18N
                if (style == null) {
                    style = ""; //NOI18N
                }
                // This is wrong because "font-height" will be a match for
                // example
                if (e.getChildNodes().getLength() == 0 && style.indexOf("position:") == -1) { // NOI18N
                    // An empty group panel in flow formatting context -- it will render
                    // to absolutely nothing visible, so put its bean name in here as a help
                    e.appendChild(e.getOwnerDocument().createTextNode(bean.getInstanceName()));
                    e.setAttribute("class", "rave-uninitialized-text"); // NOI18N
                    break;
                } else if (style.indexOf("width") < 0 || style.indexOf("height") < 0) { //NOI18N
                    StringBuffer sb = new StringBuffer(style.length()+30);
                    sb.append(style);
                    if (!style.endsWith(";")) { //NOI18N
                        sb.append(';');
                    }
                    // Set display: block  - work around the fact that
                    // the group panel uses a <span> which of course
                    // is an inline tag and therefore ignores our width
                    // and height settings.
                    sb.append("display:block;"); // NOI18N
                    if (style.indexOf("width") < 0) { //NOI18N
                        sb.append(" width: 96px;"); //NOI18N
                    }
                    if (style.indexOf("height") < 0) { //NOI18N
                        sb.append(" height: 96px;"); //NOI18N
                    }
                    style = sb.toString();
                    e.setAttribute("style", style); //NOI18N
                }
                // Ensure that we show a border
                e.setAttribute("class", "rave-design-border"); // NOI18N
                break;
            }
        }
    }

}
