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

package org.netbeans.modules.db.explorer.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

import org.netbeans.modules.db.explorer.dlg.AddDriverDialog;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.explorer.infos.DriverListNodeInfo;
import org.netbeans.modules.db.explorer.infos.DriverNodeInfo;

public class CustomizeDriverAction extends DatabaseAction {
    static final long serialVersionUID =-109193000951395612L;
    private static final Logger LOGGER = Logger.getLogger(CustomizeDriverAction.class.getName());
    
    private Dialog dialog;

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length != 1)
            return false;
        
		DriverNodeInfo info = (DriverNodeInfo) activatedNodes[0].getCookie(DriverNodeInfo.class);
        if (info != null && info.getURL().equals("sun.jdbc.odbc.JdbcOdbcDriver")) //NOI18N
            return false;
        
        return true;
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        final Node[] n = activatedNodes;
        
		final DriverNodeInfo info = (DriverNodeInfo) n[0].getCookie(DriverNodeInfo.class);
        if (info == null)
            return; //should not happen
        JDBCDriver drv = info.getJDBCDriver();
        if (drv == null) {
            return;
        }
        final AddDriverDialog dlgPanel = new AddDriverDialog(drv);
        
        
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (event.getSource() == DialogDescriptor.OK_OPTION) {
                    String displayName = dlgPanel.getDisplayName();
                    List drvLoc = dlgPanel.getDriverLocation();
                    String drvClass = dlgPanel.getDriverClass();
                    
                    StringBuffer err = new StringBuffer();
                    if (drvLoc.size() < 1)
                        err.append(bundle().getString("AddDriverDialog_MissingFile")); //NOI18N
                    if (drvClass == null || drvClass.equals("")) {
                        if (err.length() > 0)
                            err.append(", "); //NOI18N

                        err.append(bundle().getString("AddDriverDialog_MissingClass")); //NOI18N
                    }
                    if (err.length() > 0) {
                        String message = MessageFormat.format(bundle().getString("AddDriverDialog_ErrorMessage"), err.toString()); //NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE));
                        
                        return;
                    }
                    
                    closeDialog();
                    
                    //create driver instance and save it in the XML format
                    if (displayName == null || displayName.equals(""))
                        displayName = drvClass;
                    
                    try {
                        String oldName = info.getJDBCDriver().getName();
                        info.delete();
                        JDBCDriverManager.getDefault().addDriver(JDBCDriver.create(oldName, displayName, drvClass, (URL[]) drvLoc.toArray(new URL[drvLoc.size()])));
                    } catch (IOException exc) {
                        LOGGER.log(Level.INFO, exc.getMessage(), exc);
                    } catch (DatabaseException exc) {
                        LOGGER.log(Level.INFO, exc.getMessage(), exc);
                    }
                    
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                DriverListNodeInfo info = (DriverListNodeInfo) n[0].getCookie(DriverListNodeInfo.class);
                                if (info != null)
                                    info.refreshChildren();
                            } catch (Exception exc) {
                                LOGGER.log(Level.INFO, exc.getMessage(), exc);
                                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Exception(exc));
                            }
                        }
                    });

                }
            }
        };

        DialogDescriptor descriptor = new DialogDescriptor(dlgPanel, bundle().getString("AddDriverDialogTitle"), true, actionListener); //NOI18N
        Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
        descriptor.setClosingOptions(closingOptions);
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
    }
    
    private void closeDialog() {
        if (dialog != null)
            dialog.dispose();
    }
}
