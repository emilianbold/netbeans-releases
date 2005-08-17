/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.actions;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Vector;
import javax.swing.JComponent;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.ExceptionListener;
import org.netbeans.modules.db.explorer.DatabaseConnection;

//commented out for 3.6 release, need to solve for next Studio release
//import org.netbeans.modules.db.explorer.PointbasePlus;

import org.netbeans.modules.db.explorer.dlg.ConnectionDialog;
import org.netbeans.modules.db.explorer.dlg.ConnectPanel;
import org.netbeans.modules.db.explorer.dlg.ConnectProgressDialog;
import org.netbeans.modules.db.explorer.dlg.SchemaPanel;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class ConnectAction extends DatabaseAction {
    static final long serialVersionUID =-6822218300035053411L;

    ConnectionDialog dlg;
    boolean advancedPanel = false;
    boolean okPressed = false;

    protected boolean enable(Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length == 1)
            node = activatedNodes[0];
        else
            return false;

        DatabaseNodeInfo info = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        if (info != null) {
            DatabaseNodeInfo nfo = info.getParent(DatabaseNode.CONNECTION);
            if (nfo != null)
                return (nfo.getConnection() == null);
        }

        return false;
    }

    protected int mode() {
        return MODE_ALL;
    }

    public void performAction(Node[] activatedNodes) {
        Node node = activatedNodes[0];
        DatabaseNodeInfo info = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        ConnectionNodeInfo nfo = (ConnectionNodeInfo) info.getParent(DatabaseNode.CONNECTION);
        
//commented out for 3.6 release, need to solve for next Studio release
//        if (PointbasePlus.DATABASE_URL.equals(nfo.getDatabase()) && PointbasePlus.DRIVER.equals(nfo.getDriver()) && PointbasePlus.USER_NAME.equals(nfo.getUser()))
//            try {
//                // add the connection (if needed) and connect to Pointbase SAMPLE database
//                PointbasePlus.addOrConnectAccordingToOption();
//                user = nfo.getUser();
//                pwd = nfo.getPassword();
//                rpwd = (Boolean) nfo.get(DatabaseNodeInfo.REMEMBER_PWD);
//                remember = ((rpwd != null) ? rpwd.booleanValue() : false);
//            } catch(Exception ex) {
//                //ignore the exception and open connect dialog
////                org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
//            }        
        
        new ConnectionDialogDisplayer().showDialog(nfo, false);
    }
    
    public static final class ConnectionDialogDisplayer {
        
        ConnectionDialog dlg;
        boolean advancedPanel = false;
        boolean okPressed = false;
        
        public void showDialog(final ConnectionNodeInfo nfo, boolean showDialog) {
            String user = nfo.getUser();
            String pwd = nfo.getPassword();
            Boolean rpwd = (Boolean) nfo.get(DatabaseNodeInfo.REMEMBER_PWD);
            boolean remember = ((rpwd != null) ? rpwd.booleanValue() : false);
            
            final DatabaseConnection dbcon = (DatabaseConnection) nfo.getDatabaseConnection();

            final ExceptionListener excListener = new ExceptionListener() {
                public void exceptionOccurred(Exception exc) {
                    if (exc instanceof ClassNotFoundException) {
                        String message = MessageFormat.format(bundle().getString("EXC_ClassNotFound"), new String[] {exc.getMessage()}); //NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    } else {
                        String message = MessageFormat.format(bundle().getString("ERR_UnableToConnect"), new String[] {exc.getMessage()}); //NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    }
                }
            };

            dbcon.addExceptionListener(excListener);

            if (user == null || pwd == null || !remember) {
                final ConnectPanel basePanel = new ConnectPanel(dbcon);
                final SchemaPanel schemaPanel = new SchemaPanel(dbcon);

                PropertyChangeListener argumentListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getPropertyName().equals("argumentChanged")) { //NOI18N
                            schemaPanel.setSchemas(new Vector(), ""); //NOI18N
                            schemaPanel.resetProgress();
                            try {
                                Connection conn = dbcon.getConnection();
                                if (conn != null && !conn.isClosed())
                                    conn.close();
                            } catch (SQLException exc) {
                                //unable to close the connection
                            }
                        }
                    }
                };
                basePanel.addPropertyChangeListener(argumentListener);

                final PropertyChangeListener connectionListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        if (event.getPropertyName().equals("connected")) { //NOI18N
                            if (setSchema(schemaPanel, dbcon, nfo.getSchema()))
                                dbcon.setSchema(schemaPanel.getSchema());
                            else {
                                //switch to schema panel
                                dlg.setSelectedComponent(schemaPanel);
                                return;
                            }

                            //connected by "Get Schemas" button in the schema panel => don't initialize the connection node,
                            //it will be done in actionListener
                            if (advancedPanel && !okPressed)
                                return;

                            try {
                                nfo.finishConnect(null, dbcon, dbcon.getConnection());
                            } catch (DatabaseException exc) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                                String message = MessageFormat.format(bundle().getString("ERR_UnableToInitializeConnection"), new String[] {exc.getMessage()}); //NOI18N
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                                return;
                            }

                            if(dlg != null) {
                                dlg.close();
    //                        removeListeners(cinfo);
                            }
                        } else
                            okPressed = false;
                    }
                };

                dbcon.addPropertyChangeListener(connectionListener);

                ActionListener actionListener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (event.getSource() == DialogDescriptor.OK_OPTION) {
                            okPressed = true;
                            nfo.setUser(basePanel.getUser());
                            nfo.setPassword(basePanel.getPassword());
                            dbcon.setUser(basePanel.getUser());
                            dbcon.setPassword(basePanel.getPassword());
                            if (basePanel.rememberPassword())
                                nfo.put(DatabaseNodeInfo.REMEMBER_PWD, Boolean.TRUE);

                            try {
                                if (dbcon.getConnection() == null || dbcon.getConnection().isClosed())
                                    dbcon.connect();
                                else {
                                    dbcon.setSchema(schemaPanel.getSchema());
                                    nfo.setSchema(schemaPanel.getSchema());

                                    try {
                                        nfo.finishConnect(null, dbcon, dbcon.getConnection());
                                    } catch (DatabaseException exc) {
                                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                                        String message = MessageFormat.format(bundle().getString("ERR_UnableToInitializeConnection"), new String[] {exc.getMessage()}); //NOI18N
                                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                                        return;
                                    }

                                    if(dlg != null)
                                        dlg.close();
                                }
                            } catch (SQLException exc) {
                                //isClosed() method failed, try to connect
                                dbcon.connect();
                            }
                            return;
                        }
                    }
                };

                ChangeListener changeTabListener = new ChangeListener() {
                    public void stateChanged (ChangeEvent e) {
                        if (((JTabbedPane) e.getSource()).getSelectedComponent().equals(schemaPanel)) {
                            advancedPanel = true;
                            nfo.setUser(basePanel.getUser());
                            nfo.setPassword(basePanel.getPassword());
                            dbcon.setPassword(basePanel.getPassword());
                        } else
                            advancedPanel = false;
                    }
                };

                dlg = new ConnectionDialog(basePanel, schemaPanel, basePanel.getTitle(), actionListener, changeTabListener);
                dlg.setVisible(true);
            } else // without dialog with connection data (username, password), just with progress dlg based on the showDialog parameter
                try {
                    DialogDescriptor descriptor = null;
                    ProgressHandle progress = null;
                    
                    if (showDialog) {
                        progress = ProgressHandleFactory.createHandle("handle");
                        JComponent progressComponent = ProgressHandleFactory.createProgressComponent(progress);
                        progressComponent.setPreferredSize(new Dimension(250, 20));
                        ConnectProgressDialog panel = new ConnectProgressDialog();
                        panel.add(progressComponent);
                        descriptor = new DialogDescriptor(panel, bundle().getString("ConnectDialogTitle"), true, new Object[] { DialogDescriptor.CANCEL_OPTION }, 
                                DialogDescriptor.CANCEL_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
                    } 
                    final Dialog dialog = showDialog ? DialogDisplayer.getDefault().createDialog(descriptor) : null;
                    
                    final PropertyChangeListener connectionListener = new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent event) {
                            if (event.getPropertyName().equals("connected")) { //NOI18N
                                try {
                                    nfo.finishConnect(null, dbcon, dbcon.getConnection());
                                    if (dialog != null) {
                                        dialog.setVisible(false);
                                    }
                                } catch (DatabaseException exc) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                                    String message = MessageFormat.format(bundle().getString("ERR_UnableToInitializeConnection"), new String[] {exc.getMessage()}); //NOI18N
                                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                                    return;
                                }
                            } else if (event.getPropertyName().equals("failed")) { // NOI18N
                                if (dialog != null) {
                                    dialog.setVisible(false);
                                }
                            }
                        }
                    };
                    
                    dbcon.addPropertyChangeListener(connectionListener);
                    dbcon.connect();
                    
                    if (showDialog) {
                        progress.start();
                        progress.switchToIndeterminate();
                        dialog.setVisible(true);
                        progress.finish();
                        dialog.dispose();
                    }
                } catch (Exception exc) {
                    String message = MessageFormat.format(bundle().getString("ERR_UnableToConnect"), new String[] {exc.getMessage()}); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                }
        }

        protected boolean setSchema(SchemaPanel schemaPanel, DatabaseConnection dbcon, String schema) {
            Vector schemas = new Vector();
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
                    String message = MessageFormat.format(bundle().getString("ERR_UnableObtainSchemas"), new String[] {exc.getMessage()}); // NOI18N
//                    message = MessageFormat.format(bundle().getString("EXC_PointbaseServerRejected"), new String[] {message, dbcon.getDatabase()}); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
//                    schema will be set to null
//                    return true;
//                }
            }

            return schemaPanel.setSchemas(schemas, schema);
        }
    }
}
