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

package org.netbeans.modules.db.explorer;

import java.util.Map;
import java.util.Collection;
import java.util.Comparator;
import java.util.Vector;
import java.util.Arrays;
import java.util.TreeSet;
import java.sql.*;
import javax.swing.SwingUtilities;
import org.openide.options.SystemOption;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.DatabaseModule;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class DatabaseNodeChildren extends Children.Array
{
    protected Collection initCollection()
    {
        DatabaseNodeInfo nodeinfo = ((DatabaseNode)getNode()).getInfo();
        java.util.Map nodeord = (java.util.Map)nodeinfo.get(DatabaseNodeInfo.CHILDREN_ORDERING);
        boolean sort = nodeinfo.getName().equals("Drivers") ? false : true; //NOI18N
        TreeSet children = new TreeSet(new NodeComparator(nodeord, sort));

        try {
            Vector chlist = nodeinfo.getChildren();
            
            for (int i=0;i<chlist.size();i++) {
                Node snode = null;
                Object sinfo = chlist.elementAt(i);

                if (sinfo instanceof DatabaseNodeInfo) {
                    DatabaseNodeInfo dni = (DatabaseNodeInfo) sinfo;
                    if (dni.getName().equals("Connection")) //NOI18N
                        //dni.setName(dni.getName() + " " + dni.getDatabase());
                        dni.setName(dni.getDatabase());

                    // aware! in this method is clone of instance dni created    
                    snode = createNode(dni);
                    
                }
                else
                    if (sinfo instanceof Node)
                        snode = (Node)sinfo;
                if (snode != null)
                    children.add(snode);
            }
            if(getNode() instanceof RootNode) { 
                // open connection (after initCollection done)
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            // add connection (if needed) and make the connection to SAMPLE database connected
                            createPointbaseConnection();
                            } catch(Exception ex) {
                            }
                        }
                    });
            }
    } catch (Exception e) {
            e.printStackTrace();
            children.clear();
        }

        return children;
    }

    class NodeComparator implements Comparator
    {
        private java.util.Map map = null;
        private boolean sort;

        public NodeComparator(java.util.Map map, boolean sort)
        {
            this.map = map;
            this.sort = sort;
        }

        public int compare(Object o1, Object o2)
        {
            if (! sort)
                return 1;
            
            if (!(o1 instanceof DatabaseNode))
                return -1;
            if (!(o2 instanceof DatabaseNode))
                return 1;
            
            int o1val, o2val, diff;
            Integer o1i = (Integer)map.get(o1.getClass().getName());
            if (o1i != null) o1val = o1i.intValue();
            else o1val = Integer.MAX_VALUE;
            Integer o2i = (Integer)map.get(o2.getClass().getName());
            if (o2i != null) o2val = o2i.intValue();
            else o2val = Integer.MAX_VALUE;

            diff = o1val-o2val;
            if (diff == 0) return ((DatabaseNode)o1).getInfo().getName().compareTo(((DatabaseNode)o2).getInfo().getName());
            return diff;
        }
    }

    public DatabaseNode createNode(DatabaseNodeInfo info)
    {
        String ncode = (String)info.get(DatabaseNodeInfo.CODE);
        String nclass = (String)info.get(DatabaseNodeInfo.CLASS);
        DatabaseNode node = null;

        try {
            node = (DatabaseNode)Class.forName(nclass).newInstance();
            node.setInfo(info); /* makes a copy of info, use node.getInfo() to access it */
            node.getInfo().setNode(node); /* this is a weak, be cool, baby ;) */
        } catch (Exception e) {
            e.printStackTrace();
        }

        return node;
    }

    public DatabaseNode createSubnode(DatabaseNodeInfo info, boolean addToChildrenFlag)
    throws DatabaseException
    {
        DatabaseNode subnode = createNode(info);
        if (subnode != null && addToChildrenFlag) {
            DatabaseNodeInfo ninfo = ((DatabaseNode)getNode()).getInfo();
            ninfo.getChildren().add(info);
            if (isInitialized()) add(new Node[] {subnode});
        }

        return subnode;
    }

    /** Creating the database connection to Pointbase SAMPLE database acording of setting PointbaseModule, if module is installed.
     */
    private void createPointbaseConnection() {

        try {
            
            // only test for PointBase module
            Class settings = Class.forName
                             ("com.sun.forte4j.pointbase.PointBaseSettings", // NOI18N
                             false, this.getClass().getClassLoader());

            // load the method for creating connection
            Class restore = Class.forName
                            ("com.sun.forte4j.pointbase.util.CreatorConnection", // NOI18N
                             false, this.getClass().getClassLoader());

            Method addOrConnectMethod = restore.getMethod ("addOrConnectPointbase", null); // NOI18N

            // call it
            addOrConnectMethod.invoke (restore.newInstance(), null);

        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        } catch (Exception e) {
        }
    }
}
