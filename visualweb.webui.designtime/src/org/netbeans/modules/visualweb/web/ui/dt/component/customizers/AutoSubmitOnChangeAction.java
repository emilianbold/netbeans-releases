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
package org.netbeans.modules.visualweb.web.ui.dt.component.customizers;

import com.sun.rave.designtime.CheckedDisplayAction;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicDisplayAction;
import com.sun.rave.web.ui.component.RbCbSelector;
import com.sun.rave.web.ui.component.RadioButtonGroup;
import com.sun.rave.web.ui.component.CheckboxGroup;
import javax.faces.component.NamingContainer;
import java.util.regex.Pattern;
import org.netbeans.modules.visualweb.web.ui.dt.component.FormDesignInfo;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;

/**
 * A basic implementation of auto-submit for editable value-holder
 * components. Relies on presence of Javascript function
 * <code>common_timeoutSubmitForm()</code>.
 *
 * @author gjmurphy
 */

public class AutoSubmitOnChangeAction extends BasicDisplayAction implements
    CheckedDisplayAction {

    private static final Pattern submitPattern = Pattern.compile(
            "common_timeoutSubmitForm\\s*\\(\\s*this\\s*\\.\\s*form\\s*,\\s*'\\S+'\\s*\\)\\s*;?"); //NOI18N

    protected DesignBean bean;

    public AutoSubmitOnChangeAction(DesignBean bean) {
        super(DesignMessageUtil.getMessage(AutoSubmitOnChangeAction.class,
                "AutoSubmitOnChangeAction.label")); //NOI18N
        this.bean = bean;
    }

    public boolean isChecked() {
        return isAutoSubmit();
    }

    public Result invoke() {
        return toggleAutoSubmit();
    }

    public boolean isAutoSubmit() {
        DesignProperty property = getSubmitProperty();
        if (property == null)
            return false;
        String value = (String) property.getValue();
        if(value == null)
            return false;
        return submitPattern.matcher(value).find();
    }

    public Result toggleAutoSubmit() {
        DesignProperty property = getSubmitProperty();
        if (property == null)
            return Result.FAILURE;
        String value = (String) property.getValue();
        if (value == null || value.length() == 0) {
            // If no property value, set it
            property.setValue(getSubmitScript(null));
        } else {
            if (isAutoSubmit()) {
                // If property value contains the onSubmit script, remove it
                property.setValue(submitPattern.matcher(value).replaceFirst("")); //NOI18N
            } else {
                // Otherwise, append the onSubmit script
                property.setValue(getSubmitScript(value));
            }
        }
        return Result.SUCCESS;
    }

    /**
     * Returns the <code>onChange</code> property for all components except
     * checkbox and radio button types, for which <code>onClick</code> is
     * returned. Special casing for these components needed by Internet
     * Explorer.
     */
    DesignProperty getSubmitProperty() {
        Object beanInstance = bean.getInstance();
        Class beanType = beanInstance.getClass();
        if (RbCbSelector.class.isAssignableFrom(beanType) ||
                beanInstance instanceof RadioButtonGroup ||
                beanInstance instanceof CheckboxGroup)
            return bean.getProperty("onClick"); //NOI18N
        else
            return bean.getProperty("onChange"); //NOI18N
    }

    String getSubmitScript(String previousScript) {
        StringBuffer buffer = new StringBuffer();
        if (previousScript != null) {
            buffer.append(previousScript);
            if (!Pattern.compile(";\\s*$").matcher(previousScript).find()) {
                buffer.append(';');
            }
            if (!Pattern.compile("\\s+$").matcher(buffer.toString()).find()) {
                buffer.append(' ');
            }
        }
        String id = FormDesignInfo.getFullyQualifiedId(bean);
        if (id == null) {
            id = bean.getInstanceName();
        }
        else if (id.startsWith(String.valueOf(NamingContainer.SEPARATOR_CHAR)) && id.length() > 1) {
            //fully qualified id (starting with ":") could look intimidating to users. so just chop off leading ":"
            id = id.substring(1, id.length());
        }
        buffer.append("common_timeoutSubmitForm(this.form, '");
        buffer.append(id);
        buffer.append("');");
        return buffer.toString();
    }

}
