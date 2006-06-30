/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
