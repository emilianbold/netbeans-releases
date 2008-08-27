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

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dlg.ConnectionDialogMediator;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor.Task;

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
            showDialog(driverName, driverClass, null, null, null);
        }
        
        public DatabaseConnection showDialog(JDBCDriver driver, String databaseUrl, String user, String password) {
            String driverName = (driver != null) ? driver.getName() : null;
            String driverClass = (driver != null) ? driver.getClassName() : null;
            return showDialog(driverName, driverClass, databaseUrl, user, password);
        }
        
        public DatabaseConnection showDialog(String driverName, String driverClass, String databaseUrl, String user, String password) {
            String finalDriverClass = null;
            
            JDBCDriver[] drivers;
            if ((null != databaseUrl) && (null != driverClass)) {
                drivers = JDBCDriverManager.getDefault().getDrivers(driverClass);
                finalDriverClass = driverClass;
            } else {
                drivers = JDBCDriverManager.getDefault().getDrivers();
            }
            
            // issue 74723: select the Derby network driver by default
            // otherwise just select the first driver
            String selectedDriverName = null;
            String selectedDriverClass = null;
            if (driverName == null || driverClass == null) {
                for (int i = 0; i < drivers.length; i++) {
                    if (JDBCDriverSupport.isAvailable(drivers[i])) {
                        if (selectedDriverName == null) {
                            selectedDriverName = drivers[i].getName();
                            selectedDriverClass = drivers[i].getClassName();
                        }
                        if ("org.apache.derby.jdbc.ClientDriver".equals(drivers[i].getClassName())) { // NOI18N
                            selectedDriverName = drivers[i].getName();
                            selectedDriverClass = drivers[i].getClassName();
                            break;
                        }
                    }
                }
            } else {
                selectedDriverName = driverName;
                selectedDriverClass = driverClass;
            }
            
            final DatabaseConnection cinfo = new DatabaseConnection();
            cinfo.setDriverName(selectedDriverName);
            cinfo.setDriver(selectedDriverClass);
            if (user != null) {
                cinfo.setUser(user);
            }
            if (password != null) {
                cinfo.setPassword(password);
            }

            if (null != databaseUrl) {
                cinfo.setDatabase(databaseUrl);
            }
            
            final NewConnectionPanel basePanel = new NewConnectionPanel(this, finalDriverClass, cinfo);
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
                            Logger.getLogger("global").log(Level.INFO, null, exc);
                            DbUtilities.reportError(bundle().getString("ERR_UnableToAddConnection"), exc.getMessage()); // NOI18N
                            try {
                                cinfo.getConnection().close();
                            } catch (SQLException e) {
                                //unable to close db connection
                            }
                            return;
                        }
                        if (dlg != null) {
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
                        Logger.getLogger("global").log(Level.INFO, null, exc.getCause());
                    } else {
                        Logger.getLogger("global").log(Level.INFO, null, exc);
                    }
                    
                    String message = null;
                    if (exc instanceof ClassNotFoundException) {
                        message = MessageFormat.format(bundle().getString("EXC_ClassNotFound"), new String[] {exc.getMessage()}); //NOI18N
                    } else {
                        StringBuffer buffer = new StringBuffer();
                        buffer.append(DbUtilities.formatError(bundle().getString("ERR_UnableToAddConnection"), exc.getMessage())); // NOI18N
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
                
                Task activeTask = null;
                
                public void actionPerformed(ActionEvent event) {
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        okPressed = true;
                        basePanel.setConnectionInfo();
                        try {
                            if (cinfo.getConnection() == null || cinfo.getConnection().isClosed())
                                activeTask = cinfo.connectAsync();
                            else {
                                cinfo.setSchema(schemaPanel.getSchema());
                                ((RootNodeInfo)RootNode.getInstance().getInfo()).addConnection(cinfo);
                                if(dlg != null)
                                    dlg.close();
                            }
                        } catch (SQLException exc) {
                            //isClosed() method failed, try to connect
                            activeTask = cinfo.connectAsync();
                        } catch (DatabaseException exc) {
                            Logger.getLogger("global").log(Level.INFO, null, exc);
                            DbUtilities.reportError(bundle().getString("ERR_UnableToAddConnection"), exc.getMessage()); // NOI18N
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
                    else if (event.getSource() == DialogDescriptor.CANCEL_OPTION) {
                        if (dlg != null)
                        {
                            if (activeTask != null)
                            {
                                activeTask.cancel();
                                activeTask = null;
                            }
                            
                            // in case a connection is underway...
                            basePanel.terminateProgress();
                        }
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

            dlg = new ConnectionDialog(this, basePanel, schemaPanel, basePanel.getTitle(), new HelpCtx("new_db_save_password"), actionListener, changeTabListener);  // NOI18N
            basePanel.setWindow(dlg.getWindow());
            dlg.setVisible(true);
            
            return ConnectionList.getDefault().getConnection(cinfo);
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
