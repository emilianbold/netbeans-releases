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
 *  the utility class serving to hide some details to make 
 *  API less dependent on details of DOM Node (or whatever it will, if any, 
 *  be changed to later) 
 *  representation of element information
 */
public class ElementDescriptor {

    private Node node = null;

    public ElementDescriptor(Node node) {
	this.node = node;
    }

    public Node getNode() {
	return node;
    }

    public int getStartPos() {
	return getPosition("StartPosition");
    }

    public int getEndPos() {
	return getPosition("EndPosition");
    }
    
    private int getPosition(String attrName) {
	if (node == null) {
	    return -1;
	}
	int position = -1;
	String query = "TokenDescriptors/TDescriptor[@type = \""+attrName+"\"]";
	Node tdnode = XMLManip.selectSingleNode(node, query);
	if (tdnode !=  null) {
	    try {
		position = XMLManip.getAttributeIntValue(tdnode, "position");
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	}
	return position;
    }


    public boolean isMarked(){
	return true;
    }
 
    public String getIDMarker() {
	return null;
    }
}
