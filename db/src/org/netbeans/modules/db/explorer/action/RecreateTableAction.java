/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dataview.DataViewWindow;
import org.netbeans.modules.db.explorer.dlg.LabeledTextFieldDialog;
import org.netbeans.modules.db.explorer.node.SchemaNameProvider;
import org.netbeans.modules.db.explorer.node.TableListNode;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

/**
 *
 * @author Rob Englander
 */
public class RecreateTableAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(RecreateTableAction.class.getName());

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;

        if (activatedNodes.length == 1) {
            TableNode tn = activatedNodes[0].getLookup().lookup(TableNode.class);

            DatabaseConnection dbconn = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);

            if (dbconn != null && tn != null && (! tn.isSystem())) {
                enabled = DatabaseConnection.isVitalConnection(dbconn.getConnection(), dbconn);
            }
        }

        return enabled;
    }

    @Override
    public void performAction (Node[] activatedNodes) {

        final BaseNode node = activatedNodes[0].getLookup().lookup(BaseNode.class);
        final DatabaseConnection connection = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);
        final DatabaseConnector connector = connection.getConnector();

        //final DatabaseNodeInfo info = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        final java.awt.Component par = WindowManager.getDefault().getMainWindow();
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    //TableListNodeInfo nfo = (TableListNodeInfo) info.getParent(nodename);
                    Specification spec = connector.getDatabaseSpecification(); //Specification) nfo.getSpecification();
                    AbstractCommand cmd;

                    // Get filename
                    FileChooserBuilder chooser = new FileChooserBuilder(RecreateTableAction.class);
                    chooser.setTitle(NbBundle.getMessage (RecreateTableAction.class, "RecreateTableFileOpenDialogTitle")); //NOI18N
                    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return (f.isDirectory() || f.getName().endsWith(".grab")); //NOI18N
                        }

                        @Override
                        public String getDescription() {
                            return NbBundle.getMessage (RecreateTableAction.class, "GrabTableFileTypeDescription"); //NOI18N
                        }
                    });

                    File file = chooser.showOpenDialog();
                    if (file != null && file.isFile()) {
                        FileInputStream fstream = new FileInputStream(file);
                        ObjectInputStream istream = new ObjectInputStream(fstream);
                        cmd = (AbstractCommand)istream.readObject();
                        istream.close();
                        cmd.setSpecification(spec);
                    } else {
                        return;
                    }

                    SchemaNameProvider schemaProvider = node.getLookup().lookup(SchemaNameProvider.class);
                    String schemaName = schemaProvider.getSchemaName();
                    String catName = schemaProvider.getCatalogName();
                    if (schemaName == null) {
                        schemaName = catName;
                    }
                    
                    cmd.setObjectOwner(schemaName);

                    String newtab = cmd.getObjectName();
                    String msg = cmd.getCommand();
                    LabeledTextFieldDialog dlg = new LabeledTextFieldDialog(msg); //NOI18N
                    dlg.setStringValue(newtab);
                    boolean noResult = true;
                    while(noResult) {
                        if (dlg.run()) { // OK option
                            if(!dlg.isEditable()) {
                                noResult = runCommand(dlg, cmd);
                            } else { // from editable text area
                                noResult = runWindow(connection, dlg);
                            }
                        } else { // CANCEL option
                            noResult = false;
                        }
                    }
                } catch (Exception exc) {
                    LOGGER.log(Level.INFO, exc.getLocalizedMessage(), exc);
                    DbUtilities.reportError(NbBundle.getMessage (RecreateTableAction.class, "ERR_UnableToRecreateTable"), exc.getMessage()); //NOI18N
                }

                // if there's a TableListNode in the parent chain, that's the one
                // we want to refresh, otherwise, refreshing this node will have to do
                Node refreshNode = node;
                while ( !(refreshNode instanceof TableListNode) ) {
                    refreshNode = refreshNode.getParentNode();
                    if (refreshNode == null) {
                        break;
                    }
                }

                if (refreshNode == null) {
                    refreshNode = node;
                }

                SystemAction.get(RefreshAction.class).performAction(new Node[] { refreshNode });
            }
        }, 0);
    }

    private boolean runCommand(final LabeledTextFieldDialog dlg, final AbstractCommand cmd) {
        boolean noResult = true;
        String newtab = dlg.getStringValue();
        cmd.setObjectName(newtab);
        try {
            cmd.execute();
            //info.addTable(newtab);
            noResult = false;
        } catch (org.netbeans.lib.ddl.DDLException exc) {
            LOGGER.log(Level.INFO, null, exc);
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(exc.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE));
            noResult = true;
        } catch (Exception exc) {
            LOGGER.log(Level.INFO, exc.getLocalizedMessage(), exc);
            DbUtilities.reportError(
                    NbBundle.getMessage (RecreateTableAction.class, "ERR_UnableToRecreateTable"), // NOI18N
                    exc.getMessage());
            noResult = false;
        }
        return noResult;
    }

    private boolean runWindow(DatabaseConnection connection, LabeledTextFieldDialog dlg)
        throws Exception {
        WindowTask wintask = new WindowTask(connection, dlg);

        // We have to create the window on the AWT thread...
        Mutex.EVENT.postReadRequest(wintask);

        // I thought Thread.join() was supposed to wait for the thread to
        // complete, but this isn't working, perhaps because the run method
        // has not yet been called?
        while ( ! wintask.completed ) {
            Thread.sleep(10);
        }

        if ( wintask.exc != null ) {
            throw new DatabaseException(wintask.exc);
        }

        if ( wintask.win.executeCommand() ) {
            return false;
        } else {
            return true;
        }
    }

    private static class WindowTask implements Runnable {
        public DataViewWindow win;
        public Exception exc = null;
        public boolean completed = false;
        private final DatabaseConnection connection;
        private final LabeledTextFieldDialog dlg;

        public WindowTask(DatabaseConnection conn, LabeledTextFieldDialog dlg) {
            super();
            this.connection = conn;
            this.dlg = dlg;
        }

        @Override
        public void run() {
            try {
                win = new DataViewWindow(connection, dlg.getEditedCommand());
            } catch ( Exception e ) {
                this.exc = e;
            }

            completed = true;
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage (RecreateTableAction.class, "RecreateTable"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RecreateTableAction.class);
    }
}
