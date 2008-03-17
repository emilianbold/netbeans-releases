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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.axis2.nodes;

import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.axis2.actions.AxisConfigurationAction;
import org.netbeans.modules.websvc.axis2.actions.DeployAction;
import org.netbeans.modules.websvc.axis2.actions.ShowServicesAction;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

public class Axis2Node extends AbstractNode {

    private static final String IS_WEB_SERVICE_ROOT = "is_web_service_root"; //NOI18N
    private static final String AXIS_GROUP_ICON = "org/netbeans/modules/websvc/axis2/resources/axis_group_node_16.png"; // NOI18N
        
    public Axis2Node(Project project) {
        super(new Axis2Children(project), createLookup(project));
        setDisplayName(NbBundle.getBundle(Axis2Node.class).getString("LBL_Axis2"));
        setName("Axis2"); //NOI18N
        setIconBaseWithExtension(AXIS_GROUP_ICON);
        setValue(IS_WEB_SERVICE_ROOT, Boolean.TRUE);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            CommonProjectActions.newFileAction(),
            null,
            SystemAction.get(AxisConfigurationAction.class),
            SystemAction.get(DeployAction.class),
            SystemAction.get(ShowServicesAction.class),
            null,
            SystemAction.get(FindAction.class),
            null,
            SystemAction.get(PasteAction.class),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    private static Lookup createLookup(Project project) {
        return (project != null) ? Lookups.fixed(new Object[]{ project }) : null;
    }
    
}
