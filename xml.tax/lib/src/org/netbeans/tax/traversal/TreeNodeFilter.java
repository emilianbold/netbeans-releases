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
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("+ TreeNodeFilter::acceptNode: this = " + this); // NOI18N
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("+               ::acceptNode: node = " + node); // NOI18N
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("+               ::acceptNode: acceptPolicy = " + acceptPolicy); // NOI18N
        
        for (int i = 0; i < nodeTypes.length; i++) {
            if ( nodeTypes[i].isInstance (node) ) {
                
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("+               ::acceptNode: RETURN " + isInstanceReturn); // NOI18N
                
                return isInstanceReturn;
            }
        }
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("+               ::acceptNode: RETURN " + isNotInstanceReturn); // NOI18N
        
        return isNotInstanceReturn;
    }
    
    
    /**
     */
    public boolean equals (Object object) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("-=#| TreeNodeFilter.equals");
        
        if ( (object instanceof TreeNodeFilter) == false ) {
            return false;
        }
        
        TreeNodeFilter peer = (TreeNodeFilter)object;
        
        Set thisSet = new HashSet (Arrays.asList (this.nodeTypes));
        Set peerSet = new HashSet (Arrays.asList (peer.nodeTypes));
        
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("-=#|    thisSet = " + thisSet);
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("-=#|    peerSet = " + peerSet);
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("-=#|    acceptPolicy? " + (this.acceptPolicy == peer.acceptPolicy));
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("-=#|    nodeTypes   ? " + (thisSet.equals (peerSet)));
        
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
