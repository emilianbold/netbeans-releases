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
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import org.netbeans.lib.ddl.impl.*;
import org.openide.*;
import org.openide.util.NbBundle;
import org.openide.nodes.*;

import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;

public class GrabTableAction extends DatabaseAction {
    static final long serialVersionUID =-7685449970256732671L;
    public void performAction (Node[] activatedNodes)
    {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
        else return;
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
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setDialogTitle(bundle.getString("GrabTableFileSaveDialogTitle")); //NOI18N
            chooser.setSelectedFile(new File(tablename+".grab")); //NOI18N
            chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                                      public boolean accept(File f) {
                                          return (f.isDirectory() || f.getName().endsWith(".grab")); //NOI18N
                                      }
                                      public String getDescription() {
                                          return bundle.getString("GrabTableFileTypeDescription"); //NOI18N
                                      }
                                  });

            java.awt.Component par = TopManager.getDefault().getWindowManager().getMainWindow();
            boolean noResult = true;
            File file = null;
            while(noResult) {
                if (chooser.showSaveDialog(par) == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    if (file != null) {
                        if(file.exists()) {
                            Object yesOption = new JButton(bundle.getString("Yes"));
                            Object noOption = new JButton (bundle.getString("No"));
                            Object result = TopManager.getDefault ().notify (new NotifyDescriptor
                                            (MessageFormat.format(bundle.getString("MSG_ReplaceFileOrNot"), // NOI18N
                                                new String[] {file.getName()}), //question
                                             bundle.getString("GrabTableFileSaveDialogTitle"), // title
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
            String message = MessageFormat.format(bundle.getString("ERR_UnableToGrabTable"), new String[] {exc.getMessage()}); // NOI18N
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
}
