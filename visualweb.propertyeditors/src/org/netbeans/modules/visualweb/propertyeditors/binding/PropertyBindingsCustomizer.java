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
package org.netbeans.modules.visualweb.propertyeditors.binding;

import java.awt.Component;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicCustomizer2;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;

public class PropertyBindingsCustomizer extends BasicCustomizer2 {

    public static final Bundle bundle = Bundle.getBundle(PropertyBindingsCustomizer.class);

    public PropertyBindingsCustomizer() {
        setDisplayName(bundle.getMessage("propBindings")); //NOI18N
        setApplyCapable(false);
        setHelpKey("projrave_ui_elements_dialogs_property_binding_db");
    }
    public PropertyBindingsCustomizer(DesignBean designBean) {
        this();
        this.designBean = designBean;
        setDisplayName(bundle.getMessage("propBindingsPattern", designBean.getInstanceName())); //NOI18N
    }
    public PropertyBindingsCustomizer(DesignProperty prop) {
        this(prop.getDesignBean());
        this.prop = prop;
        setDisplayName(bundle.getMessage("propBindingPattern", designBean.getInstanceName(), prop.getPropertyDescriptor().getName())); //NOI18N
    }

    protected DesignProperty prop = null;
    protected PropertyBindingPanel panel = null;
    public Component getCustomizerPanel(DesignBean bean) {
        panel = new PropertyBindingPanel();
        if (prop != null) {
            panel.setSourceProperty(prop);
        }
        else {
            panel.setSourceBean(bean);
        }
        panel.setCustomizer(this);
        return panel;
    }

    public Result applyChanges() {
//        if (panel != null) {
//            panel.doApplyExpr();
//        }
        return Result.SUCCESS;
    }

    public boolean isModified() {
        if (panel != null) {
            return panel.isModified();
        }
        return super.isModified();
    }
}
