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
import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import java.awt.datatransfer.Transferable;
import com.netbeans.enterprise.modules.db.explorer.*;
import com.netbeans.enterprise.modules.db.explorer.actions.*;
import com.netbeans.enterprise.modules.db.DatabaseException;
import com.netbeans.enterprise.modules.db.explorer.infos.DatabaseNodeInfo;

public class DatabaseNode extends AbstractNode implements Node.Cookie
{
	/** Cookie */
	private DatabaseNodeInfo info;
	
	/** Context menu flags */
	private boolean writable = false;
	private boolean cutflag = false, copyflag = false, delflag = false;
	
	/** Properties */
	public static final String ROOT = "root";
	public static final String DRIVER_LIST = "driverlist";
	public static final String DRIVER = "driver";
	public static final String CONNECTION = "connection";
	public static final String CATALOG = "catalog";
	public static final String TABLELIST = "tablelist";
	public static final String TABLE = "table";
	public static final String VIEW = "view";
	public static final String VIEWLIST = "viewlist";
	public static final String VIEWCOLUMN = "viewcolumn";
	public static final String INDEX = "index";
	public static final String COLUMN = "column";
	public static final String INDEXCOLUMN = "indexcolumn";
	public static final String PRIMARY_KEY = "pcolumn";
	public static final String INDEXED_COLUMN = "icolumn";
	public static final String FOREIGN_KEY = "fcolumn";
	public static final String EXPORTED_KEY = "ekey";
	public static final String PROCEDURE = "procedure";
	public static final String PROCEDURELIST = "procedurelist";
	public static final String PROCEDURE_COLUMN = "procedurecolumn";
	
	/** Constructor */
	public DatabaseNode()
	{
		super(new DatabaseNodeChildren());
	}

	/** Constructor */
	public DatabaseNode(Children child)
	{
		super(child);
	}

	/** Returns cookie */
	public DatabaseNodeInfo getInfo()
	{
		return info;
	}

	/** Sets cookie */
	public void setInfo(DatabaseNodeInfo nodeinfo)
	{
		info = (DatabaseNodeInfo)nodeinfo.clone();
		super.setName(info.getName());
		setIconBase(info.getIconBase());
		
		String fmt = info.getDisplayname();
		if (fmt != null) {
			String dname = MapFormat.format(fmt, info);
//			if (dname != null) setDisplayName(dname);
		}
		
		// Read options
		// Cut, copy and delete flags
		
		Map opts = (Map)info.get("options");
		if (opts != null) {
			String str = (String)opts.get("cut");
			if (str != null) cutflag = str.toUpperCase().equals("YES");
			str = (String)opts.get("copy");
			if (str != null) copyflag = str.toUpperCase().equals("YES");
			str = (String)opts.get("delete");
			if (str != null) delflag = str.toUpperCase().equals("YES");
		}

		try {
    		Vector prop = (Vector)info.get(DatabaseNodeInfo.PROPERTIES);
			Enumeration prop_i = prop.elements();
			while (prop_i.hasMoreElements()) {
				Map propmap = (Map)prop_i.nextElement();
    			if (((String)propmap.get(DatabaseNodeInfo.CODE)).equals(DatabaseNodeInfo.NAME)) {
    				writable = ((String)propmap.get(DatabaseNodeInfo.WRITABLE)).toUpperCase().equals("YES");
    			}
    		}
		} catch (Exception e) {}
	}

	/** Sets name */
	public void setName(String newname)
	{
		super.setName(newname);
		info.setName(newname);
	}

	public boolean canRename()
	{
		return writable;
	}

    /** 
    * Can be cut only if copyable flag is set.
    */
    public boolean canCut () 
    {
		return cutflag;
    }

     /** 
    * Can be copied only if copyable flag is set.
    */
    public boolean canCopy () 
    {
		return copyflag;
    }
    
    /** 
    * Can be destroyed only if copyable flag is set.
    */
	public boolean canDestroy()
	{
		return delflag;
	}

	public void destroy() throws IOException
	{
		info.delete();
		super.destroy();
	}

	public Node.Cookie getCookie(Class cls) 
	{
		if (cls.isInstance(info)) return info;
    	return super.getCookie(cls);
  	}

	protected SystemAction[] createActions() 
	{
		SystemAction[] retacts, sysacts = NodeOp.getDefaultActions();
		Vector actions = info.getActions();
		if (actions.size() > 0) {
			SystemAction[] myacts = (SystemAction[])actions.toArray(new SystemAction[actions.size()]);
			retacts = new SystemAction[sysacts.length+myacts.length];
			System.arraycopy((Object)myacts,0,(Object)retacts,0,myacts.length);
			System.arraycopy((Object)sysacts,0,(Object)retacts,myacts.length,sysacts.length);
		} else retacts = sysacts;
		
		return retacts;
  	}

	protected Map createProperty(String name)
	{
		return null;
	}
	
	protected PropertySupport createPropertySupport(String name, Class type, String displayName, String shortDescription, DatabaseNodeInfo rep, boolean writable)
	{
		return new DatabasePropertySupport(name, type, displayName, shortDescription, rep, writable);
	}

	protected PropertySupport createPropertySupport(String name, Class type, String displayName, String shortDescription, DatabaseNodeInfo rep, boolean writable, boolean expert)
	{
		PropertySupport ps =  new DatabasePropertySupport(name, type, displayName, shortDescription, rep, writable);
		ps.setExpert(expert);
		return ps;
	}
	
	/** Sheet for this node.
	*/
	protected Sheet createSheet() 
	{
    	Sheet sheet = Sheet.createDefault();
    	Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
    	Vector prop = (Vector)info.get(DatabaseNodeInfo.PROPERTIES);
		Enumeration prop_i = prop.elements();
		ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
		while (prop_i.hasMoreElements()) {
			boolean canWrite, expert = false;
			Map propmap = (Map)prop_i.nextElement();
			String key = (String)propmap.get(DatabaseNodeInfo.CODE);
			String expkey = (String)propmap.get("expert");
			if (expkey != null) expert = expkey.toUpperCase().equals("YES");
			
			try {

				PropertySupport psitem = null;
				String pname = null, pclass = null, pdesc = null;
				if (propmap == null) {
					propmap = createProperty(key);
					if (propmap != null) info.put(key, propmap);
				} 

				if (key.equals("name")) {
					if (!info.isReadOnly()) psitem = new PropertySupport.Name(this);
				} else {
					Class pc = null;
					pname = (String)propmap.get(DatabaseNodeInfo.NAME);
					if (info.canAdd(propmap, pname)) {
						pclass = (String)propmap.get(DatabaseNodeInfo.CLASS);
						canWrite = info.canWrite(propmap, pname, writable);
						if (pclass.equals("java.lang.Boolean")) pc = Boolean.TYPE;
						else if (pclass.equals("java.lang.Integer")) pc = Integer.TYPE;
						else pc = Class.forName(pclass);

						try {
							pname = bundle.getString(pname);
						} catch (MissingResourceException e) {
							pdesc = "<untitled>";
						}
						
						psitem = createPropertySupport(key, pc, pname, pdesc, info, canWrite, expert);	
					}
				}
			
				if (psitem != null) ps.put(psitem);
//				else throw new DatabaseException("no property for "+pname+" "+pclass);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
    	
    	return sheet;
  	}  	

	/** Deletes subnode.
	* Called by deleteNode.
	* @param node Node to delete.
	*/
	protected void deleteNode(DatabaseNode node)
	throws DatabaseException
	{
		try {
			DatabaseNodeInfo ninfo = node.getInfo();
			DatabaseNodeChildren children = (DatabaseNodeChildren)getChildren();
			info.getChildren().removeElement(ninfo);
			children.remove(new Node[] {node});
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}			
	}
	
	/** Deletes node and subnodes.
	* Called by delete actions
	*/
	public void deleteNode()
	throws DatabaseException
	{
		try {
			DatabaseNode parent = (DatabaseNode)getParentNode().getCookie(null);
			if (parent != null) parent.deleteNode(this);
		} catch (Exception e) {
			throw new DatabaseException(e.getMessage());
		}
	}
}
/*
 * <<Log>>
 *  15   Gandalf   1.14        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        10/1/99  Radko Najman    comments
 *  13   Gandalf   1.12        9/23/99  Slavek Psenicka Bug #3311
 *  12   Gandalf   1.11        9/17/99  Slavek Psenicka 
 *  11   Gandalf   1.10        9/15/99  Slavek Psenicka 
 *  10   Gandalf   1.9         9/13/99  Slavek Psenicka 
 *  9    Gandalf   1.8         9/13/99  Slavek Psenicka 
 *  8    Gandalf   1.7         9/8/99   Slavek Psenicka 
 *  7    Gandalf   1.6         9/2/99   Slavek Psenicka ReadOnly feature
 *  6    Gandalf   1.5         8/18/99  Slavek Psenicka debug logs removed
 *  5    Gandalf   1.4         7/21/99  Slavek Psenicka nodes
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
