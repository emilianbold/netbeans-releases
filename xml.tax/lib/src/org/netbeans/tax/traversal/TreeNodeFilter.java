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
package org.netbeans.tax.traversal;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Iterator;

import org.netbeans.tax.*;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class TreeNodeFilter {
    /** */
    private static final boolean DEBUG = false;
    
    
    // Constants returned by acceptNode
    /** */
    public static final short FILTER_ACCEPT = 0;
    /** */
    public static final short FILTER_REJECT = 1;
    //      /** */
    //      public static final short FILTER_SKIP   = 2;
    
    
    // Constants of acceptPolicy property
    /** */
    public static final short ACCEPT_TYPES = 10;
    /** */
    public static final short REJECT_TYPES = 11;
    
    
    /** */
    private Class[] nodeTypes;
    /** */
    private short acceptPolicy;
    
    
    /** */
    public static final TreeNodeFilter SHOW_ALL_FILTER = new TreeNodeFilter ();
    /** */
    public static final TreeNodeFilter SHOW_DATA_ONLY_FILTER =
    new TreeNodeFilter (new Class[] { TreeComment.class, TreeProcessingInstruction.class }, REJECT_TYPES);
    
    
    //
    // init
    //
    
    /** */
    public TreeNodeFilter (Class[] nodeTypes, short acceptPolicy) throws IllegalArgumentException {
        for (int i = 0; i < nodeTypes.length; i++) {
            if ( isValidNodeType (nodeTypes[i]) == false ) {
                throw new IllegalArgumentException ();
            }
        }
        
        this.nodeTypes    = nodeTypes;
        this.acceptPolicy = acceptPolicy;
    }
    
    /** */
    public TreeNodeFilter (Class[] nodeTypes) {
        this (nodeTypes, ACCEPT_TYPES);
    }
    
    /** */
    public TreeNodeFilter () {
        this (new Class[] { TreeNode.class });
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public short acceptNode (TreeNode node) {
        short isInstanceReturn;
        short isNotInstanceReturn;
        
        if ( acceptPolicy == ACCEPT_TYPES ) {
            isInstanceReturn    = FILTER_ACCEPT;
            isNotInstanceReturn = FILTER_REJECT;
        } else {
            isInstanceReturn    = FILTER_REJECT;
            isNotInstanceReturn = FILTER_ACCEPT;
        }
        
        if ( DEBUG ) {
            Util.debug ("\n+----------------->"); // NOI18N
            Util.debug ("+ TreeNodeFilter::acceptNode: this = " + this); // NOI18N
            Util.debug ("+               ::acceptNode: node = " + node); // NOI18N
            Util.debug ("+               ::acceptNode: acceptPolicy = " + acceptPolicy); // NOI18N
        }
        
        for (int i = 0; i < nodeTypes.length; i++) {
            if ( nodeTypes[i].isInstance (node) ) {
                if ( DEBUG ) {
                    Util.debug ("+               ::acceptNode: RETURN " + isInstanceReturn); // NOI18N
                }
                
                return isInstanceReturn;
            }
        }
        
        if ( DEBUG ) {
            Util.debug ("+               ::acceptNode: RETURN " + isNotInstanceReturn); // NOI18N
        }
        
        return isNotInstanceReturn;
    }
    
    
    /**
     */
    public boolean equals (Object object) {
        if ( DEBUG ) {
            Util.debug ("\n");
            Util.debug ("-=#| TreeNodeFilter.equals");
        }
        
        if ( (object instanceof TreeNodeFilter) == false ) {
            return false;
        }
        
        TreeNodeFilter peer = (TreeNodeFilter)object;
        
        Set thisSet = new HashSet (Arrays.asList (this.nodeTypes));
        Set peerSet = new HashSet (Arrays.asList (peer.nodeTypes));
        
        if ( DEBUG ) {
            Util.debug ("-=#|    thisSet = " + thisSet);
            Util.debug ("-=#|    peerSet = " + peerSet);
            Util.debug ("-=#|    acceptPolicy? " + (this.acceptPolicy == peer.acceptPolicy));
            Util.debug ("-=#|    nodeTypes   ? " + (thisSet.equals (peerSet)));
        }
        
        if ( this.acceptPolicy != peer.acceptPolicy ) {
            return false;
        }
        
        return thisSet.equals (peerSet);
    }
    
    
    /**
     */
    public Class[] getNodeTypes () {
        return nodeTypes;
    }
    
    /**
     */
    public short getAcceptPolicy () {
        return acceptPolicy;
    }
    
    /**
     */
    public static boolean isValidNodeType (Class type) {
        if ( TreeNode.class.isAssignableFrom (type) ) {
            return true;
        }
        if ( TreeCharacterData.class.isAssignableFrom (type) ) {
            return true;
        }
        if ( TreeReference.class.isAssignableFrom (type) ) {
            return true;
        }
        if ( TreeEntityReference.class.isAssignableFrom (type) ) {
            return true;
        }
        if ( TreeNodeDecl.class.isAssignableFrom (type) ) {
            return true;
        }
        
        return false;
    }
    
    
    /**
     */
    public String toString () {
        StringBuffer sb = new StringBuffer ();
        
        sb.append (super.toString ()).append (" [ ");
        sb.append ("acceptPolicy= [").append (acceptPolicy).append ("] ");
        if ( acceptPolicy == ACCEPT_TYPES ) {
            sb.append ("ACCEPT");
        } else {
            sb.append ("REJECT");
        }
        sb.append (" | nodeTypes= [");
        for (int i = 0; i < nodeTypes.length; i++) {
            if ( i != 0 ) {
                sb.append (" | ");
            }
            sb.append (nodeTypes[i].getName ());
        }
        sb.append ("] ]");
        
        return sb.toString ();
    }
    
}
