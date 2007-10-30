/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.websvc.components.strikeiron;

import javax.swing.Action;
import org.netbeans.modules.websvc.components.strikeiron.actions.FindServiceAction;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author nam
 */
public class StrikeIronWebServiceManager implements WebServiceManagerExt {

    public static final String STRIKE_IRON_GROUP = NbBundle.getMessage(StrikeIronWebServiceManager.class, "STRIKE_IRON_GROUP");
    public Action[] getGroupActions(Node node) {
        if (node.getName().startsWith(STRIKE_IRON_GROUP)) {
            return new Action[] { SystemAction.get(FindServiceAction.class) };
        } else {
            return EMPTY_ACTIONS;
        }
    }

    private static String strikeIronGroupId;
    
    public static String getGroupId() {
        if (strikeIronGroupId == null) {
            for (WebServiceGroup group : WebServiceListModel.getInstance().getWebServiceGroupSet()) {
                if (STRIKE_IRON_GROUP.equalsIgnoreCase(group.getName())) {
                    strikeIronGroupId = group.getId();
                    break;
                }
            }
            if (strikeIronGroupId == null) {
                //TODO create new group
            }
        }
        return strikeIronGroupId;
    }
    
    public Action[] getMethodActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public Action[] getPortActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public Action[] getWebServiceActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public Action[] getWebServicesRootActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public boolean wsServiceAddedExt(WebServiceDescriptor wsMetadataDesc) {
        return true;
    }

    public boolean wsServiceRemovedExt(WebServiceDescriptor wsMetadataDesc) {
        return true;
    }
    
}
