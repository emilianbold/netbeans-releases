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
package com.sun.jsfcl.std;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import com.sun.jsfcl.util.ComponentBundle;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;

/**
 * @deprecated
 */

public class UiComponentIdPropertyEditor extends PropertyEditorSupport implements
    PropertyEditor2 {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(
        UiComponentIdPropertyEditor.class);

    private static final String EMPTY = bundle.getMessage("parenEmpty"); //NOI18N
    private DesignProperty liveProperty;

    public String[] getTags() {
        ArrayList list = new ArrayList();
        DesignBean[] lbeans = getUIComponentBeans();
        list.add(EMPTY);
        for (int i = 0; i < lbeans.length; i++) {
            DesignBean lbean = lbeans[i];
            if (!(lbean.getInstance() instanceof UIViewRoot) &&
                (liveProperty == null ||
                liveProperty.getDesignBean().getInstance() != lbean.getInstance())) {
                list.add(lbean.getInstanceName());
            }
        }
        return (String[])list.toArray(new String[0]);
    }

    public void setAsText(String text) throws IllegalArgumentException {
        String uiComponentId = ""; //NOI18N
        DesignBean[] lbeans = getUIComponentBeans();
        if (!text.equals(EMPTY)) {
            for (int i = 0; i < lbeans.length; i++) {
                if (lbeans[i].getInstanceName().equals(text)) {
                    uiComponentId = ((UIComponent)lbeans[i].getInstance()).getId();
                    break;
                }
            }
        }
        setValue(uiComponentId);
    }

    public String getAsText() {
        String uiComponentId = (String)getValue();
        if (uiComponentId == null) {
            return ""; //NOI18N
        }
        DesignBean bean = getDesignBean();
        return (bean == null) ? "" : ((UIComponent)bean.getInstance()).getId(); //NOI18N
    }

    public String getJavaInitializationString() {
        if (getAsText().equals("null")) { //NOI18N
            return "\"\""; //NOI18N
        }
        return (getValue() == null || getValue().equals("")) ? "\"\"" : //NOI18N
            ("\"" + ((UIComponent)getDesignBean().getInstance()).getId() + "\""); //NOI18N
    }

    public boolean supportsCustomEditor() {
        return false;
    }

    public Component getCustomEditor() {
        return null;
    }

    public void setDesignProperty(DesignProperty liveProperty) {
        this.liveProperty = liveProperty;
    }

    private DesignBean getDesignBean() {
        if (liveProperty == null) {
            return null;
        }
        DesignBean[] lbeans = liveProperty.getDesignBean().getDesignContext().getBeansOfType(UIComponent.class);
        for (int i = 0; i < lbeans.length; i++) {
            if (getValue().equals(((UIComponent)lbeans[i].getInstance()).getId())) {
                return lbeans[i];
            }
        }
        return null;
    }

    private DesignBean[] getUIComponentBeans() {
        return (liveProperty == null) ? new DesignBean[0] :
            liveProperty.getDesignBean().getDesignContext().getBeansOfType(UIComponent.class);
    }
}
