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
