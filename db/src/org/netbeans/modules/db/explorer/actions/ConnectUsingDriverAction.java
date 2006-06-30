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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Vector;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.db.explorer.dlg.ConnectionDialogMediator;

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
import org.netbeans.modules.db.explorer.dlg.NewConnectionPanel;
import org.netbeans.modules.db.explorer.dlg.SchemaPanel;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.modules.db.explorer.driver.JDBCDriverSupport;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.DriverNodeInfo;
import org.netbeans.modules.db.explorer.infos.RootNodeInfo;
import org.netbeans.modules.db.explorer.nodes.RootNode;

public class ConnectUsingDriverAction extends DatabaseAction {
    static final long serialVersionUID =8245005834483564671L;

    protected boolean enable(Node[] activatedNodes) {
        return (activatedNodes != null && activatedNodes.length == 1);
    }

    public void performAction(Node[] activatedNodes) {
        Node node = activatedNodes[0];
        DriverNodeInfo info = (DriverNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        JDBCDriver driver = info.getJDBCDriver();
        
        String driverName, driverClass;
        if (driver != null) {
            driverName = driver.getName();
            driverClass = driver.getClassName();
        } else {
            // no JDBCDriver, have to resort to the info
            driverName = info.getName();
            // info.getURL() suprisingly returns the driver class
            driverClass = info.getURL();
        }
        new NewConnectionDialogDisplayer().showDialog(driverName, driverClass);
    }
    
    public static final class NewConnectionDialogDisplayer extends ConnectionDialogMediator {
        
        ConnectionDialog dlg;
        ConnectionNodeInfo cni;
        boolean advancedPanel = false;
        boolean okPressed = false;

        public void showDialog(String driverName, String driverClass) {
            showDialog(driverName, driverClass, null);
        }
        
        public void showDialog(String driverName, String driverClass, String databaseUrl) {
            
            Vector drvs = new Vector();
            JDBCDriver[] drivers = null;
            if ((null != databaseUrl) && (null != driverClass))
                drivers = JDBCDriverManager.getDefault().getDrivers(driverClass);
            else
                drivers = JDBCDriverManager.getDefault().getDrivers();
            for (int i = 0; i < drivers.length; i++)
                if (JDBCDriverSupport.isAvailable(drivers[i])) {
                    drvs.add(drivers[i]);
                    if (driverName == null || driverClass == null) {
                        driverName = drivers[i].getName();
                        driverClass = drivers[i].getClassName();
                    }
                }
            
            final DatabaseConnection cinfo = new DatabaseConnection();
            cinfo.setDriverName(driverName);
            cinfo.setDriver(driverClass);

            if (null != databaseUrl) {
                cinfo.setDatabase(databaseUrl);
            }
                
            final NewConnectionPanel basePanel = new NewConnectionPanel(this, drvs, cinfo);
            final SchemaPanel schemaPanel = new SchemaPanel(this, cinfo);

            PropertyChangeListener argumentListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    if (event.getPropertyName().equals("argumentChanged")) { //NOI18N
                        schemaPanel.setSchemas(new Vector(), ""); //NOI18N
                        schemaPanel.resetProgress();
                        try {
                            Connection conn = cinfo.getConnection();
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
                    if (event.getPropertyName().equals("connecting")) { // NOI18N
                        fireConnectionStarted();
                    }
                    if (event.getPropertyName().equals("failed")) { // NOI18N
                        fireConnectionFailed();
                    }
                    if (event.getPropertyName().equals("connected")) { //NOI18N
                        if (retrieveSchemas(schemaPanel, cinfo, cinfo.getUser()))
                            cinfo.setSchema(schemaPanel.getSchema());
                        else {
                            //switch to schema panel
                            fireConnectionFinished();
                            dlg.setSelectedComponent(schemaPanel);
                            return;
                        }
                        
                        fireConnectionFinished();

                        //connected by "Get Schemas" button in the schema panel => don't create connection node
                        //and don't close the connect dialog
                        if (advancedPanel && !okPressed)
                            return;

                        try {
                            ((RootNodeInfo)RootNode.getInstance().getInfo()).addConnection(cinfo);
                        } catch (DatabaseException exc) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                            String message = MessageFormat.format(bundle().getString("ERR_UnableToAddConnection"), new String[] {exc.getMessage()}); //NOI18N
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                            try {
                                cinfo.getConnection().close();
                            } catch (SQLException e) {
                                //unable to close db connection
                            }
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

            final ExceptionListener excListener = new ExceptionListener() {
                public void exceptionOccurred(Exception exc) {
                    if (exc instanceof DDLException) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc.getCause());
                    } else {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                    }
                    
                    String message = null;
                    if (exc instanceof ClassNotFoundException) {
                        message = MessageFormat.format(bundle().getString("EXC_ClassNotFound"), new String[] {exc.getMessage()}); //NOI18N
                    } else {
                        StringBuffer buffer = new StringBuffer();
                        buffer.append(MessageFormat.format(bundle().getString("ERR_UnableToAddConnection"), new String[] {exc.getMessage()})); //NOI18N
                        if (exc instanceof DDLException && exc.getCause() instanceof SQLException) {
                            SQLException sqlEx = ((SQLException)exc.getCause()).getNextException();
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

            cinfo.addPropertyChangeListener(connectionListener);
            cinfo.addExceptionListener(excListener);

            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        okPressed = true;
                        basePanel.setConnectionInfo();
                        try {
                            if (cinfo.getConnection() == null || cinfo.getConnection().isClosed())
                                cinfo.connect();
                            else {
                                cinfo.setSchema(schemaPanel.getSchema());
                                ((RootNodeInfo)RootNode.getInstance().getInfo()).addConnection(cinfo);
                                if(dlg != null)
                                    dlg.close();
                            }
                        } catch (SQLException exc) {
                            //isClosed() method failed, try to connect
                            cinfo.connect();
                        } catch (DatabaseException exc) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                            String message = MessageFormat.format(bundle().getString("ERR_UnableToAddConnection"), new String[] {exc.getMessage()}); //NOI18N
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                            try {
                                cinfo.getConnection().close();
                                cinfo.setConnection(null);
                            } catch (SQLException e) {
                                //unable to close db connection
                                cinfo.setConnection(null);
                            } finally {
                            }
                        }
                        return;
                    }
                }
            };

            ChangeListener changeTabListener = new ChangeListener() {
                public void stateChanged (ChangeEvent e) {
                    if (((JTabbedPane) e.getSource()).getSelectedComponent().equals(schemaPanel)) {
                        advancedPanel = true;
                        basePanel.setConnectionInfo();
                    } else
                        advancedPanel = false;
                }
            };

            dlg = new ConnectionDialog(this, basePanel, schemaPanel, basePanel.getTitle(), actionListener, changeTabListener);
            dlg.setVisible(true);
        }

//    private void removeListeners() {
//        cinfo.removePropertyChangeListener(connectionListener);
//        cinfo.removeExceptionListener(excListener);
//    }

        protected boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema) {
            fireConnectionStep(bundle().getString("ConnectionProgress_Schemas")); // NOI18N
            Vector schemas = new Vector();
            try {
                ResultSet rs = dbcon.getConnection().getMetaData().getSchemas();
                if (rs != null)
                    while (rs.next()) {
                        schemas.add(rs.getString(1).trim());
                    }
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

            return schemaPanel.setSchemas(schemas, defaultSchema);
        }
    }
}
