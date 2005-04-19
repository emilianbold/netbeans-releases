/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.tax.traversal;

import java.io.*;
import java.util.ArrayList;

import org.netbeans.tax.traversal.TreeNodeFilter;

/**
 * @author Libor Kramolis
 */
public final class TreeNodeFilterHandle implements Serializable {
    private static final long serialVersionUID = -571598256778542088L;

    /** */
    private String[] nodeTypeNames;
    /** */
    private short acceptPolicy;

    /** */
    transient private TreeNodeFilter nodeFilter;


    //
    // init
    //

    /** */
    public TreeNodeFilterHandle (TreeNodeFilter nodeFilter) {
        this.nodeFilter = nodeFilter;
    }


    //
    // itself
    //

    /**
     */
    public TreeNodeFilter getNodeFilter () {
        if ( nodeFilter == null ) { // lazy init

            ArrayList knownTypes = new ArrayList();
            for (int i = 0; i < nodeTypeNames.length; i++) {
                try {
                    knownTypes.add (Class.forName ( nodeTypeNames[i] ));
                } catch (ClassNotFoundException ex) {
                    //let it be
                }
            }
            Class[] nodeTypes = (Class[])knownTypes.toArray (new Class[0]);

            nodeFilter = new TreeNodeFilter (nodeTypes, acceptPolicy);
        }

        return nodeFilter;
    }


    /**
     */
    private void initFields () {
        acceptPolicy = getNodeFilter().getAcceptPolicy();

        Class[] nodeTypes = getNodeFilter().getNodeTypes();
        nodeTypeNames = new String [nodeTypes.length];
        for (int i = 0; i < nodeTypes.length; i++) {
            nodeTypeNames[i] = nodeTypes[i].getName();
        }
    }


    /**
     */
    private void writeObject (ObjectOutputStream oos) throws IOException {
        initFields();

        oos.defaultWriteObject();
    }

}
