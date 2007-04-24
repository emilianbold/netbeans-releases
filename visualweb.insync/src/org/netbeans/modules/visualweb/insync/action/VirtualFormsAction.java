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

package org.netbeans.modules.visualweb.insync.action;


import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.ext.componentgroup.ComponentGroupHolder;
import com.sun.rave.designtime.ext.componentgroup.util.ComponentGroupHelper;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.util.NbBundle;

/**
 * Action providing virtual forms customizer.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> performAction impl)
 */
public class VirtualFormsAction extends AbstractDisplayActionAction {

    /** Creates a new instance of DesignBeanAction. */
    public VirtualFormsAction() {
    }

    protected DisplayAction[] getDisplayActions(DesignBean[] designBeans) {
	if (designBeans == null || designBeans.length == 0) {
            return new DisplayAction[0];
        }
		
        DesignContext designContext = designBeans[0].getDesignContext();
        ComponentGroupHolder[] holders = ComponentGroupHelper.getComponentGroupHolders(designContext);
        if (holders == null || holders.length == 0) {
            return new DisplayAction[0];
        }
        List<DisplayAction> displayActionList = new ArrayList<DisplayAction>();
        for (int h = 0; h < holders.length; h++) {
            DisplayAction[] holderDisplayActions = holders[h].getDisplayActions(designContext, designBeans);
            for (int d = 0; d < holderDisplayActions.length; d++) {
                displayActionList.add(holderDisplayActions[d]);
            }
        }
        return displayActionList.toArray(new DisplayAction[displayActionList.size()]);
    }

    protected String getDefaultDisplayName() {
        return NbBundle.getMessage(VirtualFormsAction.class, "LBL_VirtualFormsActionName");
    }

}
