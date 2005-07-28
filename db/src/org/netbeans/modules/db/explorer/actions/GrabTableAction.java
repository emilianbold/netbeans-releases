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
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.infos.ColumnNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.openide.windows.WindowManager;

public class GrabTableAction extends DatabaseAction {
    static final long serialVersionUID =-7685449970256732671L;
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1)
            return true;
        else
            return false;
    }

    public void performAction (Node[] activatedNodes)
    {
        Node node;
        if (activatedNodes != null && activatedNodes.length == 1)
            node = activatedNodes[0];
        else
            return;
        
        try {
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            DatabaseNodeInfo nfo = info.getParent(nodename);
            Specification spec = (Specification)nfo.getSpecification();
            String tablename = (String)nfo.get(DatabaseNode.TABLE);

            // Get command

            CreateTable cmd = (CreateTable)spec.createCommandCreateTable(tablename);
            Enumeration enu = nfo.getChildren().elements();
            while (enu.hasMoreElements()) {
                Object element = enu.nextElement();
                if (element instanceof ColumnNodeInfo) {
                    cmd.getColumns().add(((ColumnNodeInfo)element).getColumnSpecification());
                }
            }

            //			System.out.println(cmd.getCommand());

            // Get filename

            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setDialogTitle(bundle().getString("GrabTableFileSaveDialogTitle")); //NOI18N
            chooser.setSelectedFile(new File(tablename+".grab")); //NOI18N
            chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                                      public boolean accept(File f) {
                                          return (f.isDirectory() || f.getName().endsWith(".grab")); //NOI18N
                                      }
                                      public String getDescription() {
                                          return bundle().getString("GrabTableFileTypeDescription"); //NOI18N
                                      }
                                  });

            java.awt.Component par = WindowManager.getDefault().getMainWindow();
            boolean noResult = true;
            File file = null;
            while(noResult) {
                if (chooser.showSaveDialog(par) == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    if (file != null) {
                        if(file.exists()) {
                            Object yesOption = new JButton(bundle().getString("Yes"));
                            Object noOption = new JButton (bundle().getString("No"));
                            Object result = DialogDisplayer.getDefault ().notify (new NotifyDescriptor
                                            (MessageFormat.format(bundle().getString("MSG_ReplaceFileOrNot"), // NOI18N
                                                new String[] {file.getName()}), //question
                                             bundle().getString("GrabTableFileSaveDialogTitle"), // title
                                             NotifyDescriptor.YES_NO_OPTION, // optionType
                                             NotifyDescriptor.QUESTION_MESSAGE, // messageType

                                             new Object[] { yesOption, noOption }, // options
                                             yesOption // initialValue
                                            ));
                            if (result.equals(yesOption)) {
                                // the file can be replaced
                                noResult = false;
                            }
                        } else noResult = false;
                    }
                } else return;
            }
            FileOutputStream fstream = new FileOutputStream(file);
            ObjectOutputStream ostream = new ObjectOutputStream(fstream);
            cmd.setSpecification(null);
            ostream.writeObject(cmd);
            ostream.flush();
            ostream.close();

        } catch(Exception exc) {
            String message = MessageFormat.format(bundle().getString("ERR_UnableToGrabTable"), new String[] {exc.getMessage()}); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
