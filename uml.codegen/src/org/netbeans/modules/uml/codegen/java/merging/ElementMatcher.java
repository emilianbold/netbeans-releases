/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.uml.codegen.java.merging;


import java.util.Iterator;
import java.util.List;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;


/** 
 */
public class ElementMatcher {

    // indicates that match to be performed using name 
    // for attribute or type, and signature for method
    public static final int BASE_MATCH = 0;

    // the match to be performed using marker ID
    // thus allowing to handle renames or signature changes 
    // if ID marker is present
    public static final int ID_MARKER_MATCH = 1;

	
    /**
     *   will return matching node if found, if several found 
     *   only first will be returned, an error will be logged
     */
    public Node findTypeMatch(Node elem, Node scopingType, int matchType) {
	return null;		
    }

    public Node findOperationMatch(Node elem, Node scopingType, int matchType) {
	String query = ".//UML:Operation";
	List opnodes = XMLManip.selectNodeList(scopingType, query);
	if (opnodes != null) {
	    Iterator iter = opnodes.iterator();
	    while(iter.hasNext()) {
		// match against elem or it's marker ID
	    }
	} 
	return null;	
    }

    public Node findAttributeMatch(Node elem, Node scopingType, int matchType) {
	return null;		
    }

}
