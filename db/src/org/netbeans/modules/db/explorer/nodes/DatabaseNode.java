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

package org.netbeans.modules.db.explorer.nodes;

import java.io.IOException;
import java.util.*;
import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import java.awt.datatransfer.Transferable;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.actions.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;

public class DatabaseNode extends AbstractNode implements Node.Cookie {
    static final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
    
    /** Cookie */
    private DatabaseNodeInfo info;

    /** Context menu flags */
    private boolean writable = false;
    private boolean cutflag = false, copyflag = false, delflag = false;

    /** Properties */
    public static final String ROOT = "root"; //NOI18N
    public static final String DRIVER_LIST = "driverlist"; //NOI18N
    public static final String DRIVER = "driver"; //NOI18N
    public static final String CONNECTION = "connection"; //NOI18N
    public static final String CATALOG = "catalog"; //NOI18N
    public static final String TABLELIST = "tablelist"; //NOI18N
    public static final String TABLE = "table"; //NOI18N
    public static final String VIEW = "view"; //NOI18N
    public static final String VIEWLIST = "viewlist"; //NOI18N
    public static final String VIEWCOLUMN = "viewcolumn"; //NOI18N
    public static final String INDEX = "index"; //NOI18N
    public static final String COLUMN = "column"; //NOI18N
    public static final String INDEXCOLUMN = "indexcolumn"; //NOI18N
    public static final String PRIMARY_KEY = "pcolumn"; //NOI18N
    public static final String INDEXED_COLUMN = "icolumn"; //NOI18N
    public static final String FOREIGN_KEY = "fcolumn"; //NOI18N
    public static final String EXPORTED_KEY = "ekey"; //NOI18N
    public static final String PROCEDURE = "procedure"; //NOI18N
    public static final String PROCEDURELIST = "procedurelist"; //NOI18N
    public static final String PROCEDURE_COLUMN = "procedurecolumn"; //NOI18N

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

        Map opts = (Map)info.get("options"); //NOI18N
        if (opts != null) {
            String str = (String)opts.get("cut"); //NOI18N
            if (str != null) cutflag = str.toUpperCase().equals("YES"); //NOI18N
            str = (String)opts.get("copy"); //NOI18N
            if (str != null) copyflag = str.toUpperCase().equals("YES"); //NOI18N
            str = (String)opts.get("delete"); //NOI18N
            if (str != null) delflag = str.toUpperCase().equals("YES"); //NOI18N
        }

        try {
            Vector prop = (Vector)info.get(DatabaseNodeInfo.PROPERTIES);
            Enumeration prop_i = prop.elements();
            while (prop_i.hasMoreElements()) {
                Map propmap = (Map)prop_i.nextElement();
                if (((String)propmap.get(DatabaseNodeInfo.CODE)).equals(DatabaseNodeInfo.NAME)) {
                    writable = ((String)propmap.get(DatabaseNodeInfo.WRITABLE)).toUpperCase().equals("YES"); //NOI18N
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
        while (prop_i.hasMoreElements()) {
            boolean canWrite, expert = false;
            Map propmap = (Map)prop_i.nextElement();
            String key = (String)propmap.get(DatabaseNodeInfo.CODE);
            String expkey = (String)propmap.get("expert"); //NOI18N
            if (expkey != null) expert = expkey.toUpperCase().equals("YES"); //NOI18N

            try {

                PropertySupport psitem = null;
                String pname = null, pclass = null, pdesc = null;
                if (propmap == null) {
                    propmap = createProperty(key);
                    if (propmap != null) info.put(key, propmap);
                }

                if (key.equals("name")) { //NOI18N
                    if (!info.isReadOnly()) psitem = new PropertySupport.Name(this);
                } else {
                    Class pc = null;
                    pname = (String)propmap.get(DatabaseNodeInfo.NAME);
                    if (info.canAdd(propmap, pname)) {
                        pclass = (String)propmap.get(DatabaseNodeInfo.CLASS);
                        canWrite = info.canWrite(propmap, pname, writable);
                        if (pclass.equals("java.lang.Boolean")) pc = Boolean.TYPE; //NOI18N
                        else if (pclass.equals("java.lang.Integer")) pc = Integer.TYPE; //NOI18N
                        else pc = Class.forName(pclass);

                        try {
                            pname = bundle.getString(pname);
                        } catch (MissingResourceException e) {
                            pdesc = bundle.getString("DatabaseNodeUntitled"); //NOI18N
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
