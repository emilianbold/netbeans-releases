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

package com.netbeans.enterprise.modules.db.explorer.nodes;

import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;
import com.netbeans.ddl.*;
import com.netbeans.ddl.impl.*;
import com.netbeans.enterprise.modules.db.*;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.infos.*;
import org.openide.util.datatransfer.PasteType;
import java.awt.datatransfer.Transferable;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Node;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.*;

// Node for Table/View/Procedure things.

public class TableNode extends DatabaseNode
{
	public void setName(String newname)
	{
		try {
			DatabaseNodeInfo info = getInfo();
			Specification spec = (Specification)info.getSpecification();
			AbstractCommand cmd = spec.createCommandRenameTable(info.getName(), newname);
			cmd.execute();
			super.setName(newname);
			info.put(DatabaseNode.TABLE, newname);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void createPasteTypes(Transferable t, List s) 
	{
		super.createPasteTypes(t, s);
		DatabaseNodeInfo nfo;
		Node n = NodeTransfer.node(t, NodeTransfer.MOVE);
		if (n != null && n.canDestroy ()) {
/*			
			nfo = (TableNodeInfo)n.getCookie(TableNodeInfo.class);
			if (nfo != null) {
				s.add(new TablePasteType((TableNodeInfo)nfo, n));
				return;
			}  
*/
			nfo = (ColumnNodeInfo)n.getCookie(ColumnNodeInfo.class);
			if (nfo != null) {
				s.add(new ColumnPasteType((ColumnNodeInfo)nfo, n));
				return;
			}
			
		} else {
/*			
			nfo = (DatabaseNodeInfo)NodeTransfer.copyCookie(t, TableNodeInfo.class);
			if (nfo != null) {
				s.add(new TablePasteType((TableNodeInfo)nfo, null));
				return;
			}
*/	
			nfo = (DatabaseNodeInfo)NodeTransfer.cookie(t, NodeTransfer.MOVE, ColumnNodeInfo.class);
			if (nfo != null) {
				s.add(new ColumnPasteType((ColumnNodeInfo)nfo, null));
				return;
			}
		}
	}	

	/** Paste type for transfering tables.
	*/
	private class TablePasteType extends PasteType 
	{
		/** transferred info */
		private DatabaseNodeInfo info;
		
		/** the node to destroy or null */
		private Node node;
		
		/** Constructs new TablePasteType for the specific type of operation paste.
		*/
		public TablePasteType(TableNodeInfo info, Node node)
		{
			this.info = info;
			this.node = node;
		}
	
		/* @return Human presentable name of this paste type. */
		public String getName() 
		{
			ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
			return bundle.getString("PasteTableName");
		}

		/** Performs the paste action.
		* @return Transferable which should be inserted into the clipboard after
		*         paste action. It can be null, which means that clipboard content
		*         should stay the same.
		*/
		public Transferable paste() throws IOException 
		{
			TableNodeInfo info = (TableNodeInfo)getInfo();
			ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
			TableListNodeInfo ownerinfo = (TableListNodeInfo)getInfo().getParent(DatabaseNode.TABLELIST);
			if (info != null) {
				TableNodeInfo exinfo = ownerinfo.getChildrenTableInfo(info);
				DatabaseNodeChildren chi = (DatabaseNodeChildren)getChildren();
				String name = info.getName();
				if (exinfo != null) {
					String namefmt = bundle.getString("PasteTableNameFormat");
					name = MessageFormat.format(namefmt, new String[] {name});
				} 

				try {
				
					// Create in database
					// PENDING
				
					ownerinfo.addTable(name);
					if (node != null) node.destroy ();
		
				} catch (Exception e) {
					throw new IOException(e.getMessage());
				}
				
			} else throw new IOException("cannot find table owner information");
			return null;
		}
	}

	/** Paste type for transfering columns.
	*/
	private class ColumnPasteType extends PasteType 
	{
		/** transferred info */
		private DatabaseNodeInfo info;
		
		/** the node to destroy or null */
		private Node node;
		
		/** Constructs new TablePasteType for the specific type of operation paste.
		*/
		public ColumnPasteType(ColumnNodeInfo info, Node node)
		{
			this.info = info;
			this.node = node;
		}
	
		/* @return Human presentable name of this paste type. */
		public String getName() 
		{
			ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
			return bundle.getString("PasteColumnName");
		}

		/** Performs the paste action.
		* @return Transferable which should be inserted into the clipboard after
		*         paste action. It can be null, which means that clipboard content
		*         should stay the same.
		*/
		public Transferable paste() throws IOException 
		{
			ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
			TableNodeInfo ownerinfo = (TableNodeInfo)getInfo();
			if (info != null) {
				try {
					String name = info.getName();
					ColumnNodeInfo coli = (ColumnNodeInfo)info;
					TableColumn col = coli.getColumnSpecification();
					Specification spec = (Specification)ownerinfo.getSpecification();
					AddColumn cmd = (AddColumn)spec.createCommandAddColumn(ownerinfo.getTable());
					cmd.getColumns().add(col);
					cmd.execute();
					ownerinfo.addColumn(name);
					if (node != null) node.destroy();
				} catch (final Exception ex) {
					ex.printStackTrace();
/*
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							TopManager.getDefault().notify(new NotifyDescriptor.Message("Unable to process command, "+e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
						}
					});
*/					
				}
			} else throw new IOException("cannot find Column owner information");
			return null;
		}
	}
}