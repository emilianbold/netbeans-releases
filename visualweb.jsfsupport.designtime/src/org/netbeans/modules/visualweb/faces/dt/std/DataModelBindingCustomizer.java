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
package org.netbeans.modules.visualweb.faces.dt.std;

import java.awt.Component;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicCustomizer2;

public class DataModelBindingCustomizer extends BasicCustomizer2 {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(
        DataModelBindingCustomizer.class);

    public DataModelBindingCustomizer() {
        super(null, bundle.getMessage("bindToDb"), null,
            "projrave_ui_elements_webform_dataref_binding_db"); //NOI18N
        setApplyCapable(true);
    }

    protected DataModelBindingPanel dmbp = null;
    public Component getCustomizerPanel(DesignBean designBean) {
        dmbp = new DataModelBindingPanel(null, this, designBean.getProperty("value")); //NOI18N
        return dmbp;
    }

    public Result applyChanges() {
        dmbp.customizerApply();
        return super.applyChanges();
    }
}
