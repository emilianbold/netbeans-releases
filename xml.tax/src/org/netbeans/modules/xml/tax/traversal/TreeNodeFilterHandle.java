/*
 * TreeNodeFilter.java -- synopsis.
 *
 *
 * SUN PROPRIETARY/CONFIDENTIAL:  INTERNAL USE ONLY.
 *
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package org.netbeans.modules.xml.tax.traversal;

import java.io.*;
import java.util.ArrayList;

import org.netbeans.tax.traversal.TreeNodeFilter;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class TreeNodeFilterHandle implements Serializable {
    /** */
    private static final boolean DEBUG = false;

    /** */
    private String[] nodeTypeNames;
    /** */
    private short acceptPolicy;

    /** */
    transient private TreeNodeFilter nodeFilter;

private static final long serialVersionUID = -571598256778542088L;


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
        acceptPolicy = nodeFilter.getAcceptPolicy();

        Class[] nodeTypes = nodeFilter.getNodeTypes();
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
