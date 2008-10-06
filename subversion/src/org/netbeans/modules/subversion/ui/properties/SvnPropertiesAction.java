/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
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
package org.netbeans.modules.subversion.ui.properties;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * 
 * @author Peter Pis
 */
public final class SvnPropertiesAction extends ContextAction {

    protected boolean enable(Node[] nodes) {
        return getContext(nodes).getRootFiles().length == 1;
    }
    
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_VERSIONED 
             | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    public String getName() {
        return NbBundle.getMessage(SvnPropertiesAction.class, "CTL_PropertiesAction");      // NOI18N
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Properties";   // NOI18N
    }

    protected void performContextAction(Node[] nodes) {       
        final Context ctx = getContext(nodes);
        String ctxDisplayName = getContextDisplayName(nodes);
        File[] roots = ctx.getRootFiles();
        
        openProperties(roots, ctxDisplayName);
    }

    public static void openProperties(File[ ]roots, String ctxDisplayName) {
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }       

        SvnClient client;
        try {            
            SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(roots[0]);            
            client = Subversion.getInstance().getClient(repositoryUrl);            
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }

        final PropertiesPanel panel = new PropertiesPanel();
        final PropertiesTable propTable;
        propTable = new PropertiesTable(panel.labelForTable, PropertiesTable.PROPERTIES_COLUMNS, new String[] { PropertiesTableModel.COLUMN_NAME_VALUE});
        panel.setPropertiesTable(propTable);

        JComponent component = propTable.getComponent();
        panel.propsPanel.setLayout(new BorderLayout());
        panel.propsPanel.add(component, BorderLayout.CENTER);
        SvnProperties svnProperties = new SvnProperties(panel, propTable, roots[0]);   
        ResourceBundle loc = NbBundle.getBundle(SvnPropertiesAction.class);
        JButton cancel = new JButton(loc.getString("CTL_Properties_Action_Cancel"));    // NOI18N
        cancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SvnPropertiesAction.class, "CTL_Properties_Action_Cancel")); // NOI18N
        cancel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SvnPropertiesAction.class, "CTL_Properties_Action_Cancel"));    // NOI18N

        DialogDescriptor dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(SvnPropertiesAction.class, "CTL_PropertiesDialog_Title", ctxDisplayName)); // NOI18N
        dd.setModal(true);
        dd.setOptions(new Object[] {cancel}); 
        dd.setHelpCtx(new HelpCtx(SvnPropertiesAction.class));

        panel.putClientProperty("contentTitle", ctxDisplayName);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SvnPropertiesAction.class, "CTL_PropertiesAction")); // NOI18N
        dialog.pack();
        dialog.setVisible(true);        
    }
    
}



