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
package org.netbeans.modules.visualweb.faces.dt_1_2.component.html;

import com.sun.rave.designtime.*;
import java.util.ArrayList;
import javax.faces.component.html.HtmlOutputFormat;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import org.netbeans.modules.visualweb.propertyeditors.binding.data.DataBindingHelper;

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
