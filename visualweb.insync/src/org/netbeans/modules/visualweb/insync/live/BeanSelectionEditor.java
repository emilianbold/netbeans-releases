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
package org.netbeans.modules.visualweb.insync.live;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;

/**
 *
 */
public class BeanSelectionEditor extends PropertyEditorSupport {

    final DesignProperty property;
    DesignBean[] liveBeans;
    int selection;

    public BeanSelectionEditor(DesignProperty property) {
        this.property = property;
        assert Trace.trace("insync.live", "BSE.BSE: " + property);

        // lookup current DesignBean in list from current property value
        setValue(property.getValue());
    }

    // load up-to-date list of all sibling beans
    private void loadList() {
        assert Trace.trace("insync.live", "BSE.loadList");
        DesignBean bean = property.getDesignBean();
        DesignContext context = bean.getDesignContext();
        PropertyDescriptor pd = property.getPropertyDescriptor();
        Class type = pd.getPropertyType();
        liveBeans = context.getBeansOfType(type);
        selection = -1;
    }

    public String getAsText() {
        String text = selection >= 0
            ? liveBeans[selection].getInstanceName()
            : "";
        assert Trace.trace("insync.live", "BSE.getAsText: " + text);
        return text;
    }

    public String getJavaInitializationString() {
        String init = selection >= 0
            ? liveBeans[selection].getInstanceName()
            : "";
        assert Trace.trace("insync.live", "BSE.getJavaInitializationString: " + init);
        return init;
    }

    public String[] getTags() {
        String[] tags = new String[liveBeans.length];
        for (int i = 0; i < liveBeans.length; i++)
            tags[i] = liveBeans[i].getInstanceName();
        return tags;
    }

    public Object getValue() {
        Object value = selection >= 0
            ? liveBeans[selection].getInstance()
            : null;
        assert Trace.trace("insync.live", "BSE.getValue: " + value);
        return value;
    }

    public void setAsText(String text)  {
        assert Trace.trace("insync.live", "BSE.setAsText: " + text);
        loadList();
        for (int i = 0; i < liveBeans.length; i++) {
            if (liveBeans[i].getInstanceName().equals(text)) {
                selection = i;
                return;
            }
        }
        //!CQ could throw an invalid arg exception
    }

    public void setValue(Object value) {
        assert Trace.trace("insync.live", "BSE.setValue: " + value);
        loadList();
        for (int i = 0; i < liveBeans.length; i++) {
            if (liveBeans[i].getInstance() == value) {
                selection = i;
                return;
            }
        }
        //!CQ could throw an invalid arg exception
    }

}
