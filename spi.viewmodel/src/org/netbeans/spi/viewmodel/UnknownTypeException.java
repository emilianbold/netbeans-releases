/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.viewmodel;


/**
 * Used by various data models if data model is asked to resolve node 
 * of unknown type.
 *
 * @author   Jan Jancura
 */
public class UnknownTypeException extends Exception {
    
    /**
     * Creates a new instance of exception for given node.
     *
     * @param node a node of unknown type
     */
    public UnknownTypeException (Object node) {
        super (node.toString ());
    }
}

