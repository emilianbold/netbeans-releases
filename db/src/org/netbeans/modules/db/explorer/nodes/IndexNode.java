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

import org.openide.nodes.Children;
import java.io.IOException;
import java.util.*;
import java.sql.*;
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
import org.openide.util.NbBundle;

public class IndexNode extends DatabaseNode
{		
/*
	public void setName(String newname)
	{
		try {
			DatabaseNodeInfo info = getInfo();
			String table = (String)info.get(DatabaseNode.TABLE);
			Specification spec = (Specification)info.getSpecification();
			RenameColumn cmd = spec.createCommandRenameColumn(table);
			cmd.renameColumn(info.getName(), newname);
			cmd.execute();
			super.setName(newname);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/

	protected void createPasteTypes(Transferable t, List s) 
	{
		super.createPasteTypes(t, s);
		Node node = NodeTransfer.node(t, NodeTransfer.MOVE);
		if (node != null) {
			ColumnNodeInfo nfo = (ColumnNodeInfo)node.getCookie(ColumnNodeInfo.class);
			if (nfo != null) s.add(new IndexPasteType((ColumnNodeInfo)nfo, null));
		}
	}	

	class IndexPasteType extends PasteType
	{
		/** transferred info */
		private DatabaseNodeInfo info;
		
		/** the node to destroy or null */
		private Node node;
		
		/** Constructs new TablePasteType for the specific type of operation paste.
		*/
		public IndexPasteType(ColumnNodeInfo info, Node node)
		{
			this.info = info;
			this.node = node;
		}
	
		/* @return Human presentable name of this paste type. */
		public String getName() 
		{
			ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
			return bundle.getString("IndexPasteTypeName");
		}

		/** Performs the paste action.
		* @return Transferable which should be inserted into the clipboard after
		*         paste action. It can be null, which means that clipboard content
		*         should stay the same.
		*/
		public Transferable paste() throws IOException 
		{
			IndexNodeInfo destinfo = (IndexNodeInfo)getInfo();
			ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
			if (info != null) {

				Connection con;
				DatabaseMetaData dmd;
				Specification spec;
				String catalog;

				try {
					con = info.getConnection();
					dmd = info.getSpecification().getMetaData();
					spec = (Specification)info.getSpecification();
					catalog = (String)info.get(DatabaseNode.CATALOG);
          boolean jdbcOdbcBridge = (((java.sql.DriverManager.getDriver(dmd.getURL()) instanceof sun.jdbc.odbc.JdbcOdbcDriver) && (!dmd.getDatabaseProductName().trim().equals("DB2/NT"))) ? true : false);

          DriverSpecification drvSpec = info.getDriverSpecification();
          drvSpec.getIndexInfo(catalog, dmd, info.getTable(), true, false);
					if (drvSpec.rs != null) {
            String index = destinfo.getName();
            HashSet ixrm = new HashSet();

            while (drvSpec.rs.next()) {
              String ixname = drvSpec.rs.getString("INDEX_NAME");
              String colname = drvSpec.rs.getString("COLUMN_NAME");
              if (ixname.equals(index)) ixrm.add(colname);
            }
            drvSpec.rs.close();
            
            if (ixrm.contains(info.getName())) throw new IOException("index "+index+" already contains column "+info.getName());

            CreateIndex icmd = spec.createCommandCreateIndex(info.getTable());
            icmd.setIndexName(destinfo.getName());
            Iterator enu = ixrm.iterator();
            while (enu.hasNext()) {
              icmd.specifyColumn((String)enu.next());
            }

            icmd.specifyColumn(info.getName());
            spec.createCommandDropIndex(index).execute();
            icmd.execute();

            drvSpec.getIndexInfo(catalog, dmd, destinfo.getTable(), true, false);
            if (drvSpec.rs != null) {
              while (drvSpec.rs.next()) {
                if (jdbcOdbcBridge) drvSpec.rsTemp.next();
                String ixname = drvSpec.rs.getString("INDEX_NAME");
                String colname = drvSpec.rs.getString("COLUMN_NAME");
                if (ixname.equals(index) && colname.equals(info.getName())) {
                  IndexNodeInfo ixinfo;
                  if (jdbcOdbcBridge)
                    ixinfo = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(destinfo, DatabaseNode.INDEX, drvSpec.rsTemp);
                  else
                    ixinfo = (IndexNodeInfo)DatabaseNodeInfo.createNodeInfo(destinfo, DatabaseNode.INDEX, drvSpec.rs);
                  
                  if (ixinfo != null) {
                    ((DatabaseNodeChildren)destinfo.getNode().getChildren()).createSubnode(ixinfo,true);
                  } else throw new Exception("unable to create node information for index");
                }
              }
              drvSpec.rs.close();
              if (jdbcOdbcBridge) drvSpec.rsTemp.close();
            }
          }
				} catch (Exception e) { 
					throw new IOException(e.getMessage());
				}

			} else throw new IOException("cannot find index owner information");
			return null;
		}
	}	
}
/*
 * <<Log>>
 *  14   Gandalf   1.13        1/26/00  Radko Najman    JDBC-ODBC bridge HACK
 *  13   Gandalf   1.12        1/26/00  Radko Najman    new driver adaptor 
 *       version
 *  12   Gandalf   1.11        1/25/00  Radko Najman    new driver adaptor 
 *       version
 *  11   Gandalf   1.10        12/15/99 Radko Najman    driver adaptor
 *  10   Gandalf   1.9         11/15/99 Radko Najman    MS ACCESS
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/8/99  Radko Najman    getUser() method 
 *       replaced by dmd.getUserName()
 *  7    Gandalf   1.6         9/13/99  Slavek Psenicka 
 *  6    Gandalf   1.5         9/8/99   Slavek Psenicka adaptor changes
 *  5    Gandalf   1.4         8/19/99  Slavek Psenicka English
 *  4    Gandalf   1.3         6/30/99  Ian Formanek    NodeTransfer related 
 *       changes to make it compilable
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
