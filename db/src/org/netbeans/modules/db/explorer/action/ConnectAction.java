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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.modules.db.ExceptionListener;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dlg.ConnectPanel;
import org.netbeans.modules.db.explorer.dlg.ConnectProgressDialog;
import org.openide.nodes.Node;

//import org.netbeans.modules.db.explorer.PointbasePlus;

import org.netbeans.modules.db.explorer.dlg.ConnectionDialog;
import org.netbeans.modules.db.explorer.dlg.ConnectionDialogMediator;
import org.netbeans.modules.db.explorer.dlg.SchemaPanel;
import org.netbeans.modules.db.explorer.node.ConnectionNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class ConnectAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(ConnectAction.class.getName());

    ConnectionDialog dlg;
    boolean advancedPanel = false;
    boolean okPressed = false;

    @Override
    public String getName() {
        return NbBundle.getMessage (ConnectAction.class, "Connect"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ConnectAction.class);
    }

    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;
        if (activatedNodes.length == 1) {
            Lookup lookup = activatedNodes[0].getLookup();
            ConnectionNode node = lookup.lookup(ConnectionNode.class);
            if (node != null) {
                DatabaseConnection dbconn = lookup.lookup(DatabaseConnection.class);
                enabled = dbconn.getConnector().isDisconnected();
            }
        }

        return enabled;
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        
        ConnectionNode node = activatedNodes[0].getLookup().lookup(ConnectionNode.class);
                
        // Don't show the dialog if all information is already available, 
        // just make the connection
        new ConnectionDialogDisplayer().showDialog(node, false);
    }

   
    public static final class ConnectionDialogDisplayer extends ConnectionDialogMediator {
        
        ConnectionDialog dlg;
        boolean advancedPanel = false;
        boolean okPressed = false;
        
        // This flag is used to detect whether there was a failure to connect
        // when using the progress bar.  The flag is set in the property
        // change listener when the status changes to "failed".          
        boolean failed = false;

        /** Shows notification if DatabaseConnection fails. */
        final ExceptionListener excListener = new ExceptionListener() {

            public void exceptionOccurred(Exception exc) {
                if (exc instanceof DDLException) {
                    LOGGER.log(Level.INFO, null, exc.getCause());
                } else {
                    LOGGER.log(Level.INFO, null, exc);
                }

                String message = null;
                if (exc instanceof ClassNotFoundException) {
                    message = MessageFormat.format(NbBundle.getMessage(ConnectAction.class, "EXC_ClassNotFound"), exc.getMessage()); //NOI18N
                } else {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(DbUtilities.formatError(NbBundle.getMessage(ConnectAction.class, "ERR_UnableToConnect"), exc.getMessage())); //NOI18N
                    if (exc instanceof DDLException && exc.getCause() instanceof SQLException) {
                        SQLException sqlEx = ((SQLException) exc.getCause()).getNextException();
                        while (sqlEx != null) {
                            buffer.append("\n\n" + sqlEx.getMessage()); // NOI18N
                            sqlEx = sqlEx.getNextException();
                        }
                    }
                    message = buffer.toString();
                }
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            }
        };

        public void showDialog(final ConnectionNode model, boolean showDialog) {
            DatabaseConnection dbcon = model.getLookup().lookup(DatabaseConnection.class);
            showDialog(dbcon, showDialog);
        }

        public void showDialog(final DatabaseConnection dbcon, boolean showDialog) {
            final DatabaseConnector connector = dbcon.getConnector();

            String user = dbcon.getUser();
            String pwd = dbcon.getPassword();
           
            boolean remember = dbcon.rememberPassword();

            dbcon.addExceptionListener(excListener);

            // If showDialog is true, show the dialog even if we have all 
            // the connection info
            //
            // Note that we don't have to show the dialog if the password is 
            // null and remember is true; null is often a valid password
            // (and is the default password for MySQL and PostgreSQL).
            if (user == null || !remember || showDialog) {
                final ConnectPanel basePanel = new ConnectPanel(this, dbcon);
                final SchemaPanel schemaPanel = new SchemaPanel(this, dbcon);

                PropertyChangeListener argumentListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getPropertyName().equals("argumentChanged")) { //NOI18N
                            schemaPanel.setSchemas(new Vector(), ""); //NOI18N
                            schemaPanel.resetProgress();
                            try {
                                Connection conn = dbcon.getConnection();
                                if (DatabaseConnection.isVitalConnection(conn, null)) {
                                    conn.close();
                                }
                            } catch (SQLException exc) {
                                Exceptions.printStackTrace(exc);
                            }
                        }
                    }
                };
                basePanel.addPropertyChangeListener(argumentListener);

                final PropertyChangeListener connectionListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getPropertyName().equals("connecting")) { // NOI18N
                            fireConnectionStarted();
                        }
                        if (event.getPropertyName().equals("failed")) { // NOI18N
                            fireConnectionFailed();
                        }
                        if (event.getPropertyName().equals("connected")) { //NOI18N
                            //connected by "Get Schemas" button in the schema panel => don't initialize the connection node,
                            //it will be done in actionListener
                            if (advancedPanel && !okPressed) {
                                // #67241: should not retrieve the schema list after connecting
                                // an existing connection, takes a long time on databases with a lot of schemas
                                // thus only retrieve the schema list when connected by the "Get schemas" button
                                if (retrieveSchemas(schemaPanel, dbcon, dbcon.getSchema())) {
                                    dbcon.setSchema(dbcon.getSchema());
                                }
                                dlg.setSelectedComponent(schemaPanel);
                                fireConnectionFinished();
                                return;
                            } else {
                                fireConnectionFinished();
                                dbcon.setSchema(dbcon.getSchema());
                            }
                            
                            try {
                                connector.finishConnect(null, dbcon, dbcon.getConnection());
                            } catch (DatabaseException exc) {
                                LOGGER.log(Level.INFO, null, exc);
                                DbUtilities.reportError(NbBundle.getMessage (ConnectAction.class, "ERR_UnableToInitializeConnection"), exc.getMessage()); // NOI18N
                                return;
                            }
                            
                            DatabaseConnection realDbcon = ConnectionList.getDefault().getConnection(dbcon);
                            if (realDbcon != null) {
                                realDbcon.setPassword(dbcon.getPassword());
                                realDbcon.setRememberPassword(dbcon.rememberPassword());
                            }
                            
                            dbcon.setRememberPassword(basePanel.rememberPassword());

                            if (dlg != null) {
                                dlg.close();
    //                        removeListeners(cinfo);
                            }

                            dbcon.fireConnectionComplete();
                        } else
                            okPressed = false;
                    }
                };

                dbcon.addPropertyChangeListener(connectionListener);

                ActionListener actionListener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (event.getSource() == DialogDescriptor.OK_OPTION) {
                            okPressed = true;
                            dbcon.setUser(basePanel.getUser());
                            dbcon.setPassword(basePanel.getPassword());
                            dbcon.setUser(basePanel.getUser());
                            dbcon.setPassword(basePanel.getPassword());
                            dbcon.setRememberPassword(basePanel.rememberPassword());

                            if (! DatabaseConnection.isVitalConnection(dbcon.getConnection(), null)) {
                                dbcon.connectAsync();
                            } else {
                                dbcon.setSchema(schemaPanel.getSchema());
                                dbcon.setSchema(schemaPanel.getSchema());

                                try {
                                    connector.finishConnect(null, dbcon, dbcon.getConnection());
                                } catch (DatabaseException exc) {
                                    Logger.getLogger("global").log(Level.INFO, null, exc);
                                    DbUtilities.reportError(NbBundle.getMessage (ConnectAction.class, "ERR_UnableToInitializeConnection"), exc.getMessage()); // NOI18N
                                    return;
                                }

                                DatabaseConnection realDbcon = ConnectionList.getDefault().getConnection(dbcon);
                                if (realDbcon != null) {
                                    realDbcon.setPassword(dbcon.getPassword());
                                    realDbcon.setRememberPassword(
                                            basePanel.rememberPassword());
                                }
                                dbcon.setRememberPassword(basePanel.rememberPassword());

                                if (dlg != null)
                                    dlg.close();

                                dbcon.fireConnectionComplete();
                            }
                            return;
                        }
                    }
                };

                ChangeListener changeTabListener = new ChangeListener() {
                    public void stateChanged (ChangeEvent e) {
                        if (((JTabbedPane) e.getSource()).getSelectedComponent().equals(schemaPanel)) {
                            advancedPanel = true;
                            dbcon.setUser(basePanel.getUser());
                            dbcon.setPassword(basePanel.getPassword());
                        } else
                            advancedPanel = false;
                    }
                };

                dlg = new ConnectionDialog(this, basePanel, schemaPanel, basePanel.getTitle(), new HelpCtx("db_save_password"), actionListener, changeTabListener);  // NOI18N
                dlg.setVisible(true);
            } else { // without dialog with connection data (username, password), just with progress dlg
                try {
                    DialogDescriptor descriptor = null;
                    ProgressHandle progress = null;
                    
                    progress = ProgressHandleFactory.createHandle("handle");
                    JComponent progressComponent = ProgressHandleFactory.createProgressComponent(progress);
                    progressComponent.setPreferredSize(new Dimension(350, 20));
                    ConnectProgressDialog panel = new ConnectProgressDialog(progressComponent);
                    panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (ConnectAction.class, "ACS_ConnectingDialogTextA11yDesc"));
                    descriptor = new DialogDescriptor(panel, NbBundle.getMessage (ConnectAction.class, "ConnectingDialogTitle"), true, new Object[] { DialogDescriptor.CANCEL_OPTION },
                            DialogDescriptor.CANCEL_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
                    final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                    
                    final PropertyChangeListener connectionListener = new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent event) {
                            if (event.getPropertyName().equals("connected")) { //NOI18N
                                try {
                                    connector.finishConnect(null, dbcon, dbcon.getConnection());
                                    if (dialog != null) {
                                        dialog.setVisible(false);
                                    }
                                }
                                catch (DatabaseException exc) {
                                    LOGGER.log(Level.INFO, null, exc);
                                    DbUtilities.reportError(NbBundle.getMessage (ConnectAction.class, "ERR_UnableToInitializeConnection"), exc.getMessage()); // NOI18N
                                    return;
                                }
                            } else if (event.getPropertyName().equals("failed")) { // NOI18N
                                if (dialog != null) {
                                    dialog.setVisible(false);
                                }
                                
                                // We want to bring up the Connect dialog if the
                                // attempt to connect using the progress bar fails.
                                // But we can't do it here because we can't control
                                // what processing the DatabaseConnection does 
                                // after posting this failure notification.  So
                                // we set a flag and wait for the connect process
                                // to fully complete, and *then* raise the Connect
                                // dialog.
                                failed = true;
                            }
                        }
                    };
                    
                    failed = false;
                    
                    dbcon.addPropertyChangeListener(connectionListener);
                    dbcon.connectAsync();
                    
                    progress.start();
                    progress.switchToIndeterminate();
                    dialog.setVisible(true);
                    progress.finish();                    
                    dialog.dispose();
                    
                    if ( failed ) {
                        // If the connection fails with a progress bar only, then 
                        // display the full Connect dialog so the user can give it
                        // another shot after changing some values, like the username
                        // or password.
                        showDialog(dbcon, true);
                    } else {
                        dbcon.fireConnectionComplete();
                    }
                } catch (Exception exc) {
                    String message = MessageFormat.format(NbBundle.getMessage (ConnectAction.class, "ERR_UnableToConnect"), exc.getMessage()); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    
                    // If the connection fails with a progress bar only, then 
                    // display the full Connect dialog so the user can give it
                    // another shot after changing some values, like the username
                    // or password.
                    showDialog(dbcon, true);
                }
            }

            dbcon.removeExceptionListener(excListener);
        }

        protected boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema) {
            fireConnectionStep(NbBundle.getMessage (ConnectAction.class, "ConnectionProgress_Schemas")); // NOI18N
            Vector<String> schemas = new Vector<String> ();
            try {
                ResultSet rs = dbcon.getConnection().getMetaData().getSchemas();
                if (rs != null)
                    while (rs.next())
                        schemas.add(rs.getString(1).trim());
            } catch (SQLException exc) {
//commented out for 3.6 release, need to solve for next Studio release
            // hack for Pointbase Network Server
//            if (dbcon.getDriver().equals(PointbasePlus.DRIVER))
//                if (exc.getErrorCode() == PointbasePlus.ERR_SERVER_REJECTED) {
                    String message = NbBundle.getMessage (ConnectAction.class, "ERR_UnableObtainSchemas", exc.getMessage()); // NOI18N
//                    message = MessageFormat.format(bundle().getString("EXC_PointbaseServerRejected"), new String[] {message, dbcon.getDatabase()}); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
//                    schema will be set to null
//                    return true;
//                }
            }

            return schemaPanel.setSchemas(schemas, defaultSchema);
        }
    }

}
