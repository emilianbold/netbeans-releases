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

package org.netbeans.modules.bpel.nodes.children;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.BpelSafeReference;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 30 March 2006
 *
 */
public abstract class BpelNodeChildren<T extends BpelEntity> extends Children.Keys
    implements ReloadableChildren 
{
    protected Lookup lookup;
    private BpelSafeReference reference;
    
    public BpelNodeChildren(T bpelEntity, Lookup contextLookup) {
        this.lookup = contextLookup;
        setReference(bpelEntity);
    }

    public Lookup getLookup() {
        return lookup;
    }
    
    protected void setReference(T bpelEntity) {
        this.reference = new BpelSafeReference<T>(bpelEntity);
    }
    
    public T getReference() {
        return (T)reference.getBpelObject();
    }
    
    public abstract Collection getNodeKeys();
    
//    protected Node[] createNodes(Object object) {
//        Node childNode = Node.EMPTY;
//        if (object != null && object instanceof Node) {
//            childNode = (Node)object;
//        }
//        return new Node[] {childNode};
//    }

    // by default NavigatorNodeFactory is used 
    // for others factories this method should be overriden
    protected Node[] createNodes(Object object) {
        if (object != null && object instanceof BpelEntity) {
            NavigatorNodeFactory factory = NavigatorNodeFactory.getInstance();
            Node childNode = factory.createNode((BpelEntity)object,lookup);
            if (childNode != null) {
                return new Node[] {childNode};
            }
        } 
        
        return new Node[0];
    }
    
    protected void addNotify() {
        reload();
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }
    
    public void reload() {
        setKeys(getNodeKeys());
    }
}
