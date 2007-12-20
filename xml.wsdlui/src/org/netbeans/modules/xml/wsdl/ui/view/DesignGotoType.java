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

package org.netbeans.modules.xml.wsdl.ui.view;

import org.netbeans.modules.xml.xam.ui.actions.GotoType;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * GotoType for showing a component in the design view.
 *
 * @author  Nathan Fiedler
 */
public class DesignGotoType extends GotoType {

    public String getName() {
        return NbBundle.getMessage(StructureGotoType.class,
                "LBL_DesignGotoType_Name");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    protected ViewComponentCookie.View getView() {
        return ViewComponentCookie.View.DESIGN;
    }
}
