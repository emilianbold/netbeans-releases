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
package org.netbeans.modules.visualweb.faces.dt.binding;

import java.awt.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.impl.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;

public class BindingsCustomizer extends BasicCustomizer2 {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(BindingsCustomizer.class);

    public BindingsCustomizer() {
        setDisplayName(bundle.getMessage("propBindings")); //NOI18N
        setApplyCapable(false);
    }
    public BindingsCustomizer(DesignBean designBean) {
        this();
        this.designBean = designBean;
        setDisplayName(bundle.getMessage("propBindingsPattern", designBean.getInstanceName())); //NOI18N
    }
    public BindingsCustomizer(DesignProperty prop) {
        this(prop.getDesignBean());
        this.prop = prop;
        setDisplayName(bundle.getMessage("propBindingPattern", designBean.getInstanceName(), prop.getPropertyDescriptor().getName())); //NOI18N
    }

    protected DesignProperty prop = null;
    protected BindingPanel panel = null;
    public Component getCustomizerPanel(DesignBean bean) {
        panel = new BindingPanel();
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
