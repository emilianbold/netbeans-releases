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

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.MessageFormat;

import javax.swing.JFileChooser;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
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
            public void run () {
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

                    String newtab = cmd.getObjectName();
                    String msg = MessageFormat.format(bundle().getString("RecreateTableRenameNotes"), new String[] {cmd.getCommand()}); //NOI18N
                    msg = cmd.getCommand();
                    LabeledTextFieldDialog dlg = new LabeledTextFieldDialog(bundle().getString("RecreateTableRenameTable"), bundle().getString("RecreateTableNewName"), msg); //NOI18N
                    dlg.setStringValue(newtab);
                    boolean noResult = true;
                    while(noResult) {
                        if (dlg.run()) { // OK option
                            try {
                                if(!dlg.isEditable()) { // from file
                                    newtab = dlg.getStringValue();
                                    cmd.setObjectName(newtab);
                                    cmd.setObjectOwner((String) info.get(DatabaseNodeInfo.SCHEMA));
                                    try {
                                        cmd.execute();
                                        nfo.addTable(newtab);
                                    } catch (org.netbeans.lib.ddl.DDLException exc) {
                                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(exc.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                                        continue;
                                    } catch (org.netbeans.api.db.explorer.DatabaseException exc) {
                                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                                        continue;
                                    }
                                    noResult = false;
                                } else { // from editable text area
                                    DataViewWindow win = new DataViewWindow(info, dlg.getEditedCommand());
                                    if(win.executeCommand())
                                        noResult = false;
                                }
                            } catch(Exception exc) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);                        
                                String message = MessageFormat.format(bundle().getString("ERR_UnableToRecreateTable"), new String[] {exc.getMessage()}); // NOI18N
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                            }
                        } else { // CANCEL option
                            noResult = false;
                        }
                    }
                } catch(Exception exc) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                    String message = MessageFormat.format(bundle().getString("ERR_UnableToRecreateTable"), new String[] {exc.getMessage()}); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                }
            }
        }, 0);
    }
}
