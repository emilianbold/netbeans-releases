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
package org.netbeans.modules.visualweb.faces.dt_1_1.component.html;

import javax.faces.component.UIComponent;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupDesignInfo;
import com.sun.rave.designtime.markup.MarkupPosition;
import com.sun.rave.designtime.markup.MarkupRenderContext;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;

public class HtmlOutputLabelDesignInfo implements MarkupDesignInfo {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(HtmlOutputLabelDesignInfo.class);
    public Class getBeanClass() { return HtmlOutputLabel.class; }

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        try {
            bean.getProperty("for").setValue(bean.getInstanceName());  //NOI18N
            DesignBean output = bean.getDesignContext().createBean(HtmlOutputText.class.getName(), bean, null);
            output.setInstanceName(bean.getInstanceName() + "Text", true);  //NOI18N
            output.getProperty("value").setValue(bundle.getMessage("htmlOutputLabel"));  //NOI18N
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return Result.SUCCESS;
    }

    public Result beanPastedSetup(DesignBean bean) {
        return Result.SUCCESS;
    }

    public Result beanDeletedCleanup(DesignBean bean) {
        return Result.SUCCESS;
    }

    public DisplayAction[] getContextItems(DesignBean bean) {
        return new DisplayAction[0];//new DisplayAction[] { new RowDataBindingCustomizerAction(), new ObjectBindingCustomizerAction() };
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        return UIComponent.class.isAssignableFrom(sourceClass);
    }

    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        if (sourceBean.getInstance() instanceof UIComponent &&
            sourceBean != targetBean) {
            DesignProperty prop = targetBean.getProperty("for"); // NOI18N
            if (prop != null) {
                prop.setValue(sourceBean.getInstanceName());
            }
        }
        return Result.SUCCESS;
    }

    public void beanContextActivated(DesignBean bean) {}
    public void beanContextDeactivated(DesignBean bean) {}
    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {}
    public void beanChanged(DesignBean bean) {}
    public void propertyChanged(DesignProperty prop, Object oldValue) {}
    public void eventChanged(DesignEvent event) {}

    public void customizeRender(MarkupDesignBean bean, MarkupRenderContext renderContext) {
        DocumentFragment documentFragment = renderContext.getDocumentFragment();
        MarkupPosition begin = renderContext.getBeginPosition();
        MarkupPosition end = renderContext.getEndPosition();

        if (begin == end) {
            return;
        }
        assert begin.getUnderParent() == end.getUnderParent();
        Node child = begin.getBeforeSibling();
        Node stop = end.getBeforeSibling();
        for (child = begin.getBeforeSibling(); child != stop; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)child;
                if ("label".equals(element.getTagName())) {  //NOI18N
                    // Must have a for attribute
                    /*
                     * this will never trigger since a for was added in annotate create
                     * without the for in annotate create, the jsf code asserts before
                     * we get to customizeRender
                     */
                    if (!element.hasAttribute("for")) {  //NOI18N
                        element.setAttribute("for", "");  //NOI18N
                    }
                    // If no PCDATA, set the instancename as the PCDATA
                    boolean hasPcData = false;
                    NodeList children = element.getChildNodes();
                    int childCount = children.getLength();
                    for (int j = 0; j < childCount; j++) {
                        if (children.item(j).getNodeType() == Node.TEXT_NODE) {
                            hasPcData = true;
                            break;
                        }
                    }
                    if (!hasPcData) {
                        element.appendChild(element.getOwnerDocument().createTextNode(bean.getInstanceName()));
                    }
                }
            }
        }
    }
}
