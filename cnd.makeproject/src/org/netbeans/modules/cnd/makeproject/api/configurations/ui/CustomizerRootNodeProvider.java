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

package org.netbeans.modules.cnd.makeproject.api.configurations.ui;

import java.util.Vector;

public class CustomizerRootNodeProvider {
    private static CustomizerRootNodeProvider instance = null;
    private Vector customizerNodes = null;

    public static CustomizerRootNodeProvider getInstance() {
	if (instance == null)
	    instance = new CustomizerRootNodeProvider();
	return instance;
    }

    public Vector getCustomizerNodes() {
	if (customizerNodes == null) {
	    customizerNodes = new Vector();
	}
	return customizerNodes;
    }
    
    public Vector getCustomizerNodes(boolean advanced) {
        Vector ret = new Vector();
        CustomizerNode[] nodes = getCustomizerNodesAsArray();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].advanced == advanced)
                ret.add(nodes[i]);
        }
        return ret;
    }

    public CustomizerNode[] getCustomizerNodesAsArray() {
	Vector cn = getCustomizerNodes();
	return (CustomizerNode[]) cn.toArray(new CustomizerNode[cn.size()]);
    }
    
    public CustomizerNode getCustomizerNode(String id) {
        CustomizerNode[] nodes = getCustomizerNodesAsArray();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].name.equals(id))
                return nodes[i];
        }
        return null;
    }
    
    public void addCustomizerNode(CustomizerNode node) {
	synchronized(getCustomizerNodes()) {
	    getCustomizerNodes().add(node);
	}
    }

    public void removeCustomizerNode(CustomizerNode node) {
	synchronized(getCustomizerNodes()) {
	    getCustomizerNodes().remove(node);
	}
    }
}
