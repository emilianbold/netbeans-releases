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

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DbUtilities;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.Specification;

import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.TableListNodeInfo;
import org.netbeans.modules.db.explorer.dlg.LabeledTextFieldDialog;
import org.netbeans.modules.db.explorer.dataview.DataViewWindow;
import org.openide.util.Mutex;

public class RecreateTableAction extends DatabaseAction {
    static final long serialVersionUID =6992569917995229492L;
    
    protected boolean enable(Node[] activatedNodes) {
        return (activatedNodes != null && activatedNodes.length == 1);
    }

    public void performAction (Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length == 1)
            node = activatedNodes[0];
        else
            return;
        
        final DatabaseNodeInfo info = (DatabaseNodeInfo) node.getCookie(DatabaseNodeInfo.class);
        final java.awt.Component par = WindowManager.getDefault().getMainWindow();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    TableListNodeInfo nfo = (TableListNodeInfo) info.getParent(nodename);
                    Specification spec = (Specification) nfo.getSpecification();
                    AbstractCommand cmd;

                    // Get filename
                    JFileChooser chooser = new JFileChooser();
                    FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
                    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                    chooser.setDialogTitle(bundle().getString("RecreateTableFileOpenDialogTitle")); //NOI18N
                    chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        public boolean accept(File f) {
                            return (f.isDirectory() || f.getName().endsWith(".grab")); //NOI18N
                        }
                        
                        public String getDescription() {
                            return bundle().getString("GrabTableFileTypeDescription"); //NOI18N
                        }
                    });

                    if (chooser.showOpenDialog(par) == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        if (file != null && file.isFile()) {
                            FileInputStream fstream = new FileInputStream(file);
                            ObjectInputStream istream = new ObjectInputStream(fstream);
                            cmd = (AbstractCommand)istream.readObject();
                            istream.close();
                            cmd.setSpecification(spec);
                        } else
                            return;
                    } else
                        return;

                    cmd.setObjectOwner((String) info.get(DatabaseNodeInfo.SCHEMA));
                    String newtab = cmd.getObjectName();
                    String msg = cmd.getCommand();
                    LabeledTextFieldDialog dlg = new LabeledTextFieldDialog(msg); //NOI18N
                    dlg.setStringValue(newtab);
                    boolean noResult = true;
                    while(noResult) {
                        if (dlg.run()) { // OK option
                            if(!dlg.isEditable()) {
                                noResult = runCommand(nfo, dlg, cmd);
                            } else { // from editable text area
                                noResult = runWindow(info, dlg);
                            }
                        } else { // CANCEL option
                            noResult = false;
                        }
                    }
                } catch (Exception exc) {
                    Logger.getLogger("global").log(Level.INFO, null, exc);
                    DbUtilities.reportError(bundle().getString("ERR_UnableToRecreateTable"), exc.getMessage()); //NOI18N
                }
            }
        }, 0);
    }
    
    private boolean runCommand(final TableListNodeInfo info, 
            final LabeledTextFieldDialog dlg,
            final AbstractCommand cmd) {
        boolean noResult = true;
        String newtab = dlg.getStringValue();
        cmd.setObjectName(newtab);
        try {
            cmd.execute();
            info.addTable(newtab);
            noResult = false;
        } catch (org.netbeans.lib.ddl.DDLException exc) {
            Logger.getLogger("global").log(Level.INFO, null, exc);
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(exc.getMessage(), 
                    NotifyDescriptor.ERROR_MESSAGE));
            noResult = true;
        } catch (org.netbeans.api.db.explorer.DatabaseException exc) {
            Logger.getLogger("global").log(Level.INFO, null, exc);
            noResult = true;
        } catch (Exception exc) {
            Logger.getLogger("global").log(Level.INFO, null, exc);
            DbUtilities.reportError(bundle().
                    getString("ERR_UnableToRecreateTable"), 
                    exc.getMessage()); //NOI18N
            noResult = false;
        }
        return noResult;
    }
    
    private boolean runWindow(DatabaseNodeInfo info, LabeledTextFieldDialog dlg) 
        throws Exception {
        WindowTask wintask = new WindowTask(info, dlg);
        
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
        private final DatabaseNodeInfo info;
        private final LabeledTextFieldDialog dlg;
        
        public WindowTask(DatabaseNodeInfo info, LabeledTextFieldDialog dlg) {
            super();
            this.info = info;
            this.dlg = dlg;
        }
        
        public void run() {
            try {
                win = new DataViewWindow(info, dlg.getEditedCommand());
            } catch ( Exception e ) {
                this.exc = e;                
            }
            
            completed = true;
        }
    }
}
