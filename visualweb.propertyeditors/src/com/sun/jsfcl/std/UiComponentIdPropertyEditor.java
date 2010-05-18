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
