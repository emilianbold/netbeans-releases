/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.actions;

import java.io.*;
import java.util.*;
import java.sql.Connection;
import java.text.MessageFormat;

import javax.swing.JFileChooser;

import org.openide.*;
import org.netbeans.lib.ddl.impl.*;
import org.openide.util.NbBundle;
import org.openide.nodes.*;

import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.dlg.*;
import org.netbeans.modules.db.explorer.dataview.*;

public class RecreateTableAction extends DatabaseAction {
    static final long serialVersionUID =6992569917995229492L;
    
    public void performAction (Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
        else return;
        try {
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            TableListNodeInfo nfo = (TableListNodeInfo)info.getParent(nodename);
            Specification spec = (Specification)nfo.getSpecification();
            String tablename = (String)nfo.get(DatabaseNode.TABLE);
            AbstractCommand cmd;

            // Get filename

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setDialogTitle(bundle.getString("RecreateTableFileOpenDialogTitle")); //NOI18N
            chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                                      public boolean accept(File f) {
                                          return (f.isDirectory() || f.getName().endsWith(".grab")); //NOI18N
                                      }
                                      public String getDescription() {
                                          return bundle.getString("GrabTableFileTypeDescription"); //NOI18N
                                      }
                                  });

            java.awt.Component par = TopManager.getDefault().getWindowManager().getMainWindow();
            if (chooser.showOpenDialog(par) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file != null && file.isFile()) {
                    FileInputStream fstream = new FileInputStream(file);
                    ObjectInputStream istream = new ObjectInputStream(fstream);
                    cmd = (AbstractCommand)istream.readObject();
                    istream.close();
                    cmd.setSpecification(spec);
                } else return;
            } else return;

            String newtab = cmd.getObjectName();
            String msg = MessageFormat.format(bundle.getString("RecreateTableRenameNotes"), new String[] {cmd.getCommand()}); //NOI18N
            msg = cmd.getCommand();
            LabeledTextFieldDialog dlg = new LabeledTextFieldDialog(bundle.getString("RecreateTableRenameTable"), bundle.getString("RecreateTableNewName"), msg); //NOI18N
            dlg.setStringValue(newtab);
            boolean noResult = true;
            while(noResult) {
                if (dlg.run()) { // OK option
                    try {
                        if(!dlg.isEditable()) { // from file
                            newtab = dlg.getStringValue();
                            cmd.setObjectName(newtab);
                            cmd.execute();
                            noResult = false;
                            nfo.addTable(newtab);
                        } else { // from editable text area
                            DataViewWindow win = new DataViewWindow(info, dlg.getEditedCommand());
                            if(win.executeCommand())
                                noResult = false;
                        }
                    } catch(Exception exc) {
                        String message = MessageFormat.format(bundle.getString("ERR_UnableToRecreateTable"), new String[] {exc.getMessage()}); // NOI18N
                        TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    }
                } else { // CANCEL option
                    noResult = false;
                }
            }
        } catch(Exception exc) {
            String message = MessageFormat.format(bundle.getString("ERR_UnableToRecreateTable"), new String[] {exc.getMessage()}); // NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
