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

import com.sun.rave.designtime.CustomizerResult;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicDisplayAction;

public class VirtualFormsCustomizerAction extends BasicDisplayAction {
    protected DesignBean formBean;
    public VirtualFormsCustomizerAction(DesignBean formBean) {
        super(java.util.ResourceBundle.getBundle("com/sun/rave/web/ui/dt/component/vforms/Bundle").getString("vfAction"));  //NOI18N
        this.formBean = formBean;
    }
    public Result invoke() {
        return new CustomizerResult(formBean, new VirtualFormsCustomizer());
    }
}
