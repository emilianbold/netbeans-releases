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
package org.netbeans.modules.visualweb.web.ui.dt.component.vforms;

import java.awt.Component;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicCustomizer2;

public class VirtualFormsCustomizer extends BasicCustomizer2 {

    public VirtualFormsCustomizer() {
        super(VirtualFormsCustomizerPanel.class, java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("vfHeader"));   //NOI18N
        setHelpKey("projrave_ui_elements_dialogs_virtual_forms_db"); //NOI18N
    }

    protected VirtualFormsCustomizerPanel customizerPanel;
    public Component getCustomizerPanel(DesignBean designBean) {
        this.designBean = designBean;
        this.customizerPanel = new VirtualFormsCustomizerPanel(this);
        return customizerPanel;
    }

    public Result applyChanges() {
        return customizerPanel.applyChanges();
    }
}
