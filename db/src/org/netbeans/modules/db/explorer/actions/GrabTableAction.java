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
  static final long serialVersionUID =-7685449970256732671L;
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

//			System.out.println(cmd.getCommand());

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
/*
 * <<Log>>
 *  13   Gandalf   1.12        11/27/99 Patrik Knakal   
 *  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        10/12/99 Radko Najman    debug messages removed
 *  10   Gandalf   1.9         9/17/99  Slavek Psenicka 
 *  9    Gandalf   1.8         9/8/99   Slavek Psenicka adaptor changes
 *  8    Gandalf   1.7         8/18/99  Slavek Psenicka debug logs removed
 *  7    Gandalf   1.6         7/21/99  Slavek Psenicka log
 *  6    Gandalf   1.5         6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/21/99  Slavek Psenicka new version
 *  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka oprava activatedNode[0] 
 *       check
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
