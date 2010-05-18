/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
