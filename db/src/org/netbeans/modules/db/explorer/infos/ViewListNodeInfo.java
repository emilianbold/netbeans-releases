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

package org.netbeans.modules.db.explorer.infos;

import java.sql.*;
import java.util.*;
import org.netbeans.lib.ddl.impl.*;
import org.openide.nodes.Node;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

public class ViewListNodeInfo extends DatabaseNodeInfo
{
  static final long serialVersionUID =2854540580610981370L;
  
	public void initChildren(Vector children) throws DatabaseException {
 		try {
			DatabaseMetaData dmd = getSpecification().getMetaData();
			String catalog = (String) get(DatabaseNode.CATALOG);
			String[] types = new String[] {"VIEW"};
      
      DriverSpecification drvSpec = getDriverSpecification();
      drvSpec.getTables(catalog, dmd, null, types);

      if (drvSpec.rs != null) {
        while (drvSpec.rs.next()) {
          DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEW, drvSpec.rs);
          if (info != null) {
            info.put(DatabaseNode.VIEW, info.getName());
            children.add(info);
          } else
            throw new Exception("unable to create node information for table");
        }
        drvSpec.rs.close();
      }
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}

	/** Adds view into list
	* Adds view named name into children list. View should exist.
	* @param name Name of existing view
	*/
	public void addView(String name)
	throws DatabaseException
	{
 		try {
      DatabaseMetaData dmd = getSpecification().getMetaData();
			String catalog = (String) get(DatabaseNode.CATALOG);
			String[] types = new String[] {"VIEW"};
      
      DriverSpecification drvSpec = getDriverSpecification();
      drvSpec.getTables(catalog, dmd, name, types);
			
      if (drvSpec.rs != null) {
        drvSpec.rs.next();
  			DatabaseNodeInfo info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.VIEW, drvSpec.rs);
        drvSpec.rs.close();
        if (info != null)
          ((DatabaseNodeChildren)getNode().getChildren()).createSubnode(info,true);
        else
          throw new Exception("unable to create node information for view");
      }
 		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());	
		}
	}
  
	public void refreshChildren() throws DatabaseException {
		Vector charr = new Vector();
		DatabaseNodeChildren chil = (DatabaseNodeChildren)getNode().getChildren();

		put(DatabaseNodeInfo.CHILDREN, charr);
		chil.remove(chil.getNodes());		
		initChildren(charr);
		Enumeration en = charr.elements();
		while(en.hasMoreElements()) {
			DatabaseNode subnode = chil.createNode((DatabaseNodeInfo)en.nextElement());
			chil.add(new Node[] {subnode});
		}
	}
}

/*
 * <<Log>>
 *  15   Gandalf-post-FCS1.13.1.0    4/10/00  Radko Najman    
 *  14   Gandalf   1.13        1/25/00  Radko Najman    new driver adaptor 
 *       version
 *  13   Gandalf   1.12        12/22/99 Radko Najman    Case Identifiers removed
 *  12   Gandalf   1.11        12/15/99 Radko Najman    driver adaptor
 *  11   Gandalf   1.10        11/27/99 Patrik Knakal   
 *  10   Gandalf   1.9         11/15/99 Radko Najman    MS ACCESS
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/8/99  Radko Najman    getUser() method 
 *       replaced by dmd.getUserName()
 *  7    Gandalf   1.6         9/13/99  Slavek Psenicka 
 *  6    Gandalf   1.5         9/8/99   Slavek Psenicka adaptor changes
 *  5    Gandalf   1.4         8/19/99  Slavek Psenicka English
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
