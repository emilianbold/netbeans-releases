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
package org.openide.nodes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Synchronous children implementation that takes a ChildFactory.
 *
 * @author Tim Boudreau
 */
final class SynchChildren<T> extends Children.Keys<T> implements ChildFactory.Observer {
    private final ChildFactory<T> factory;
    
    /** Creates a new instance of SynchChildren
     * @param factory An instance of ChildFactory which will provide keys,
     *                values, nodes
     */
    SynchChildren(ChildFactory<T> factory) {
        this.factory = factory;
        factory.setObserver(this);
    }
    
    volatile boolean active = false;
    protected @Override void addNotify() {
        active = true;
        refresh(true);
    }
    
    protected @Override void removeNotify() {
        active = false;
        setKeys(Collections.<T>emptyList());
    }
    
    protected Node[] createNodes(T key) {
        return factory.createNodesForKey(key);
    }
    
    public void refresh(boolean immediate) {
        if (active) {
            List <T> toPopulate = new LinkedList<T>();
            while (!factory.createKeys(toPopulate)) {}
            setKeys(toPopulate);
        }
    }
}
