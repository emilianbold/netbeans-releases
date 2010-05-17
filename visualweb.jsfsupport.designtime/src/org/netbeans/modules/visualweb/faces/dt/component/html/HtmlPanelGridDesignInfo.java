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
