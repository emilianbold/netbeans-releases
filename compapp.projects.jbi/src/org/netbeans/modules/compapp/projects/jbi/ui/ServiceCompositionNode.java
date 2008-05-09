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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.projects.jbi.ui;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.compapp.projects.jbi.CasaHelper;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.OpenEditorAction;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * This class represents a casa node in the composite application project view.
 * It provides a direct invocation of casa view by double clicking this node in the
 * compapp project view. It also has "edit" and "clean' actions to explicitly 
 * edit or clean the comapp configuration.
 * 
 * @author chikkala
 */
public class ServiceCompositionNode extends FilterNode {

    public ServiceCompositionNode(Node casaNode) {
        super(casaNode, FilterNode.Children.LEAF);
        disableDelegation(DELEGATE_GET_DISPLAY_NAME |
                DELEGATE_SET_DISPLAY_NAME | DELEGATE_GET_SHORT_DESCRIPTION |
                DELEGATE_GET_ACTIONS | DELEGATE_GET_CONTEXT_ACTIONS | DELEGATE_DESTROY);
        
        setDisplayName(NbBundle.getMessage(ServiceCompositionNode.class, "LBL_ServiceCompositionNode"));
        setShortDescription(NbBundle.getMessage(ServiceCompositionNode.class, "DESC_ServiceCompositionNode"));       
    }

    @Override
    public Action[] getActions(boolean context) {
        ResourceBundle bundle = NbBundle.getBundle(ServiceCompositionNode.class);

        return new Action[]{
                    null,
                    ProjectSensitiveActions.projectSensitiveAction(
                    new OpenEditorAction(),
                    bundle.getString("LBL_ServiceCompositionNode.edit.action.name"), // NOI18N
                    null),
                    ProjectSensitiveActions.projectCommandAction(
                    JbiProjectConstants.COMMAND_JBICLEANCONFIG,
                    bundle.getString("LBL_ServiceCompositionNode.clean.action.name"), // NOI18N
                    null)
                };
    }

    @Override
    public Action getPreferredAction() {
        ResourceBundle bundle = NbBundle.getBundle(ServiceCompositionNode.class);
        return ProjectSensitiveActions.projectSensitiveAction(
                    new OpenEditorAction(),
                    bundle.getString("LBL_ServiceCompositionNode.edit.action.name"), // NOI18N
                    null);
    }

    @Override
    public void destroy() throws IOException {
        super.destroy();
    }
    
    

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }
    /**
     * creates a node that is a shadow of the casa node used for displaying the
     * node in compapp project view.
     * @param p
     * @return
     */
    public static ServiceCompositionNode createServiceCompositionNode(JbiProject p) {
        try {
            FileObject fobj = CasaHelper.getCasaFileObject(p, true); // create on demand
            DataObject dobj = DataObject.find(fobj);
            return new ServiceCompositionNode(dobj.getNodeDelegate());
        } catch (Exception ex) {
            // failed to open casa...
            Logger.getLogger(ServiceCompositionNode.class.getName()).log(Level.FINE,ex.getMessage(), ex);                    
        }
        return null;
    }
}
