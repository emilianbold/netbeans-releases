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
package org.netbeans.modules.visualweb.faces.dt_1_2.component.html;

import com.sun.jsfcl.util.ComponentBundle;
import com.sun.rave.designtime.*;
import com.sun.rave.propertyeditors.binding.data.DataBindingHelper;
import java.util.ArrayList;
import javax.faces.component.html.HtmlOutputFormat;

//import org.openide.ErrorManager;

public class HtmlOutputFormatDesignInfo implements DesignInfo {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(HtmlOutputFormatDesignInfo.class);
    public Class getBeanClass() { return HtmlOutputFormat.class; }

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        try {
            bean.getProperty("value").setValue(bundle.getMessage("htmlOutputFormat")); //NOI18N
        }
        catch (Exception x) {
            // send the error to the log
            //ErrorManager.getDefault().notify(x);
            x.printStackTrace();
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
        // Use the new data binding dialogs that support Data providers
        ArrayList actions = new ArrayList();
        DesignProperty property = bean.getProperty("value");
        if(property != null){
            Class bindingPanelClass = DataBindingHelper.BIND_VALUE_TO_DATAPROVIDER;
            DisplayAction bindToDataAction = DataBindingHelper.getDataBindingAction(bean,
                    property.getPropertyDescriptor().getName(),
                    new Class[] {bindingPanelClass, DataBindingHelper.BIND_VALUE_TO_OBJECT});
            actions.add(bindToDataAction);
        }
        return (DisplayAction[])actions.toArray(new DisplayAction[actions.size()]);
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        return false;
    }

    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        return null;
    }

    public void beanContextActivated(DesignBean bean) {}
    public void beanContextDeactivated(DesignBean bean) {}
    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {}
    public void beanChanged(DesignBean bean) {}
    public void propertyChanged(DesignProperty prop, Object oldValue) {}
    public void eventChanged(DesignEvent event) {}

    /*
    public MarkupMouseRegion[] annotateRender(DesignBean bean, DocumentFragment documentFragment, MarkupPosition begin, MarkupPosition end) {
        NodeList nodeList = documentFragment.getChildNodes();
        int len = nodeList.getLength();
        for (int i = 0; i < len; i++) {
            Node child = nodeList.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)child;
                if ("output_message".equals(element.getTagName())) { //NOI18N
                    String value = element.getAttribute("value"); //NOI18N
                    if (value == null || value.length() == 0 || "null".equals(value)) {
                        String newValue = bean.getInstanceName();
                        String valueRef = bean.getProperty("valueRef").getValueAsText();
                        if (valueRef != null && value.length() > 0) {
                            valueRef = valueRef.substring(valueRef.lastIndexOf("."));
                            newValue = valueRef;
                        } else {
                            Object bindValue = bean.getDesignContext().getContextData(bean.getInstanceName() + ".databind");
                            if (bindValue != null) {
                                newValue = "" + bindValue;
                            }
                        }
                        element.setAttribute("value", newValue);
                    }
                }
            }
        }
        return null;
    }
    */
}
