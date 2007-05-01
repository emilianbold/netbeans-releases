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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mashup.db.ui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * List of children of a containing node. Remember to document what your permitted keys
 * are!
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class FlatfileChildren extends Children.Keys {

    /** Optional holder for the keys, to be used when changing them dynamically. */
    protected List myKeys;

    /** Constructs default instance of FlatfileChildren */
    public FlatfileChildren() {
        myKeys = null;
    }

    /** AddNotify */
    protected void addNotify() {
        super.addNotify();
        if (myKeys != null) {
            return;
        }
        myKeys = new LinkedList();
        // add whatever keys you need
        setKeys(myKeys);
    }

    /** Remove Notify */
    protected void removeNotify() {
        myKeys = null;
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }

    /**
     * Create Node
     * 
     * @param key key
     * @return array of nodes
     */
    protected Node[] createNodes(Object key) {
        return null;
    }

    /**
     * Optional accessor method for the keys, for use by the container node or maybe
     * subclasses.
     */
    /*
     * protected addKey(Object newKey) { // Make sure some keys already exist:
     * addNotify(); myKeys.add(newKey); // Ensure that the node(s) is displayed:
     * refreshKey(newKey); }
     */

    /**
     * Optional accessor method for keys, for use by the container node or maybe
     * subclasses.
     */
    /*
     * protected void setKeys(Collection keys) { myKeys = new LinkedList();
     * myKeys.addAll(keys); super.setKeys(keys); }
     */

    // Could also write e.g. removeKey to be used by the nodes in this children.
    // Or, could listen to changes in their status (NodeAdapter.nodeDestroyed)
    // and automatically remove them from the keys list here. Etc.
}

