/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.action;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import org.netbeans.api.db.explorer.DatabaseException;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node; 

import org.netbeans.modules.db.explorer.dlg.AddDriverDialog;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.explorer.node.DriverNode;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class CustomizeDriverAction extends BaseAction {
    private Dialog dialog;
    
    @Override
    public String getName() {
        return NbBundle.getMessage (CustomizeDriverAction.class, "Customize"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizeDriverAction.class);
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes.length == 1;
    }
    
    public void performAction(Node[] activatedNodes) {
        Lookup lookup = activatedNodes[0].getLookup();
        final DriverNode node = lookup.lookup(DriverNode.class);
        
        if (node != null) {
            JDBCDriver driver = node.getDatabaseDriver().getJDBCDriver();
            final AddDriverDialog dlgPanel = new AddDriverDialog(driver);

            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        String displayName = dlgPanel.getDisplayName();
                        List<URL> drvLoc = dlgPanel.getDriverLocation();
                        String drvClass = dlgPanel.getDriverClass();

                        StringBuffer err = new StringBuffer();
                        if (drvLoc.size() < 1)
                            err.append(NbBundle.getMessage (CustomizeDriverAction.class, "AddDriverDialog_MissingFile")); //NOI18N
                        if (drvClass == null || drvClass.equals("")) {
                            if (err.length() > 0)
                                err.append(", "); //NOI18N

                            err.append(NbBundle.getMessage (CustomizeDriverAction.class, "AddDriverDialog_MissingClass")); //NOI18N
                        }
                        if (err.length() > 0) {
                            String message = NbBundle.getMessage (CustomizeDriverAction.class, "AddDriverDialog_ErrorMessage", err.toString()); //NOI18N
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE));

                            return;
                        }

                        closeDialog();

                        //create driver instance and save it in the XML format
                        if (displayName == null || displayName.equals(""))
                            displayName = drvClass;

                        try {
                            String oldName = node.getDatabaseDriver().getJDBCDriver().getName();
                            node.destroy();
                            JDBCDriverManager.getDefault().addDriver(JDBCDriver.create(oldName, displayName, drvClass, drvLoc.toArray(new URL[0])));
                        } catch (DatabaseException exc) {
                            Exceptions.printStackTrace(exc);
                        }
                    }
                }
            };

            DialogDescriptor descriptor = new DialogDescriptor(dlgPanel, NbBundle.getMessage (CustomizeDriverAction.class, "AddDriverDialogTitle"), true, actionListener); //NOI18N
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setVisible(true);
        }
    }
    
    private void closeDialog() {
        if (dialog != null)
            dialog.dispose();
    }
}
