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
    private static final long serialVersionUID = -571598256778542088L;

    /** */
    private static final boolean DEBUG = false;

    /** */
    private String[] nodeTypeNames;
    /** */
    private short acceptPolicy;

    /** */
    transient private TreeNodeFilter nodeFilter;


    // Stream replacing
    private static final String NODE_TYPE_NAMES_FIELD = "nodeTypeNames"; // NOI18N
    private static final String ACCEPT_POLICY_FIELD   = "acceptPolicy"; // NOI18N
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField (NODE_TYPE_NAMES_FIELD, Vector.class),
        new ObjectStreamField (ACCEPT_POLICY_FIELD, short.class),
    };


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


//      /**
//       */
//      private void writeObject (ObjectOutputStream oos) throws IOException {
//          initFields();

//          oos.defaultWriteObject();
//      }


    /**
     */
    private void writeObject (ObjectOutputStream oos) throws IOException {
        initFields();

        ObjectOutputStream.PutField putField = oos.putFields();

        Vector names = new Vector();
        for (int i = 0; i < nodeTypes.length; i++) {
            names.add (nodeTypes[i].getName());
        }
        putField.put (NODE_TYPE_NAMES_FIELD, names);

        putField.put (ACCEPT_POLICY_FIELD, acceptPolicy);
        
        oos.writeFields();
    }


    /**
     */
    private void readObject (ObjectInputStream ois) throws IOException, ClassNotFoundException {
        if ( DEBUG ) {
            Util.debug ("\n* TreeNodeFilter::readObject: this = " + this, new RuntimeException()); // NOI18N
        }

        ObjectInputStream.GetField getField = ois.readFields();
        
        if ( DEBUG ) {
            Util.debug ("*               ::readObject: getField = " + getField); // NOI18N
        }
        
        try {
            Vector nodeTypeNames = (Vector)getField.get
                (NODE_TYPE_NAMES_FIELD,
                 new Vector (Arrays.asList (new String[] { "org.netbeans.modules.xml.tree.TreeNode" }))); // NOI18N

            if ( DEBUG ) {
                Util.debug ("*               ::readObject: [new] nodeTypeNames = " + nodeTypeNames); // NOI18N
            }
            
            Vector types = new Vector();
            Iterator it = nodeTypeNames.iterator();
            while (it.hasNext()) {
                try {
                    types.add (Class.forName((String)it.next()));
                } catch (ClassNotFoundException ex) {
                    //let it be
                }
            }
            nodeTypes = (Class[])types.toArray (new Class[0]);
        } catch (IllegalArgumentException ex) {
            if ( DEBUG ) {
                Util.debug ("*               ::readObject: [old] nodeTypes = " + nodeTypes); // NOI18N
            }

            // old version: private Class[] whatToShow;
            nodeTypes = (Class[])getField.get (WHAT_TO_SHOW_FIELD, new Class[] { TreeNode.class });
        }
        
        acceptPolicy = getField.get (ACCEPT_POLICY_FIELD, ACCEPT_TYPES);
    }

}
