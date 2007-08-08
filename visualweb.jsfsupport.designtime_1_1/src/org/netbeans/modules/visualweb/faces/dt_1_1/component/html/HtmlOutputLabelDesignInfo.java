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
