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


package com.netbeans.enterprise.modules.db.explorer.actions;

import java.util.ResourceBundle;
import org.openide.*;
import org.openide.util.NbBundle;
import java.util.*;
import java.io.*;
import java.sql.Connection;
import org.openide.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.nodes.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import javax.swing.JFileChooser;
import com.netbeans.ddl.impl.*;

public class GrabTableAction extends DatabaseAction
{
	public void performAction (Node[] activatedNodes)
	{
		Node node;
		if (activatedNodes != null && activatedNodes.length>0) node = activatedNodes[0];
		else return;
		try {

			final ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
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

			// Get filename

			JFileChooser chooser = new JFileChooser();
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setDialogTitle(bundle.getString("GrabTableFileSaveDialogTitle"));
			chooser.setSelectedFile(new File(tablename+".grab"));
			chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
				public boolean accept(File f) {
					return (f.isDirectory() || f.getName().endsWith(".grab"));
				}
				public String getDescription() {
					return bundle.getString("GrabTableFileTypeDescription");
				}
			});
			
			java.awt.Component par = TopManager.getDefault().getWindowManager().getMainWindow();
			if (chooser.showSaveDialog(par) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (file != null) {
					FileOutputStream fstream = new FileOutputStream(file);
					ObjectOutputStream ostream = new ObjectOutputStream(fstream);
					cmd.setSpecification(null);
					ostream.writeObject(cmd);
					ostream.flush();
					ostream.close();
				}
			}
			
		} catch(Exception e) {
			TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to grab table, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
		}
	}
}