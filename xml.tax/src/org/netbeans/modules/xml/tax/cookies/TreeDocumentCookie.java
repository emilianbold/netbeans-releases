/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.cookies;

import javax.swing.text.Element;

import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;

import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeNode;

/**
 * Everything what can be represented as the tree can return this cookie.
 * It particurary can be DTD and XML document (fragment).
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public interface TreeDocumentCookie extends Node.Cookie {

    /**
     * Return current state of model (it may be different from state when cookie was queried).
     * @return root of tree hiearchy or null if source can not be represented as tree
     * (can not be parsed etc.)
     */
    public TreeDocumentRoot getDocumentRoot ();
    
    
    /**
     */
    /* public */ static interface Editor extends TreeDocumentCookie, EditorCookie {
	
	/**
	 */
	Element treeToText (TreeNode treeNode);

	/**
	 */
	TreeNode textToTree (Element textElement);

    }

}
