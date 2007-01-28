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

import com.sun.rave.designtime.*;
import com.sun.rave.designtime.impl.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;

public class BindingCustomizerAction extends BasicDisplayAction {

    public static final ComponentBundle bundle = ComponentBundle.getBundle(BindingCustomizerAction.class);

    public BindingCustomizerAction(DesignBean bean) {
        super(bundle.getMessage("propBindingEllipse")); //NOI18N
        this.bean = bean;
    }
    public BindingCustomizerAction(DesignProperty prop) {
        super(bundle.getMessage("propBindingEllipse")); //NOI18N
        this.prop = prop;
    }

    protected DesignBean bean = null;
    protected DesignProperty prop = null;

    public Result invoke() {
        BindingsCustomizer blc = prop != null ? new BindingsCustomizer(prop) : new BindingsCustomizer(bean);
        if (prop != null) {
            blc.panel.setShowSourcePanel(false);
        }
        return new CustomizerResult(prop != null ? prop.getDesignBean() : bean, blc);
    }

    public void setBean(DesignBean bean) {
        this.bean = bean;
    }

    public void setProperty(DesignProperty property) {
        this.prop = property;
    }

}
