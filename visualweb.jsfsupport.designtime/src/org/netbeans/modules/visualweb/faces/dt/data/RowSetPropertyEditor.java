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
package org.netbeans.modules.visualweb.faces.dt.data;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import javax.sql.RowSet;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;

public class RowSetPropertyEditor extends PropertyEditorSupport implements PropertyEditor2 {

    private DesignProperty liveProperty;

    public String[] getTags() {
        DesignBean[] beans = getRowSetBeans();
        String[] tags = new String[beans.length];
        for (int i = 0; i < beans.length; i++) {
            tags[i] = beans[i].getInstanceName();
        }
        return tags;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        RowSet rowSet = null;
        DesignBean[] beans = getRowSetBeans();

        for (int i = 0; i < beans.length; i++) {
            if (beans[i].getInstanceName().equals(text)) {
                rowSet = (RowSet)beans[i].getInstance();
                break;
            }
        }
        setValue(rowSet);
    }

    public String getAsText() {
        DesignBean designBean = getDesignBean();
        return (designBean == null) ? null : designBean.getInstanceName();
    }

    public String getJavaInitializationString() {
        DesignBean designBean = getDesignBean();
        return (designBean == null) ? "null" : designBean.getInstanceName(); //NOI18N
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
        return (liveProperty == null) ? null :
            liveProperty.getDesignBean().getDesignContext().getBeanForInstance(getValue());
    }

    private DesignBean[] getRowSetBeans() {
        return (liveProperty == null) ? new DesignBean[0] :
            liveProperty.getDesignBean().getDesignContext().getBeansOfType(RowSet.class);
    }
}
