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
package org.netbeans.modules.visualweb.faces.dt.converter;

import java.awt.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.impl.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;

/**
 * @author joe and gowri
 */

public class NumberConverterCustomizer extends BasicCustomizer2 {
    private static final ComponentBundle bundle = ComponentBundle.getBundle(NumberConverterCustomizer.class);

    public NumberConverterCustomizer() {
        super(NumberConverterCustomizerPanel.class, bundle.getMessage("numberFormat"), null, "projrave_ui_elements_dialogs_number_converter_db");   //NOI18N
        setApplyCapable(true);
    }

    protected NumberConverterCustomizerPanel nclcp = null;

    public Component getCustomizerPanel(DesignBean designBean) {
        nclcp = new NumberConverterCustomizerPanel(designBean);
        return nclcp;
    }

    public boolean isModified() {
        if (nclcp != null) {
            return nclcp.isModified();
        }
       // return super.isModified();
        return false;
    }

    public Result applyChanges() {
        nclcp.customizerApply();
        return super.applyChanges();
    }
}
