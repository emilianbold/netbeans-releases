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

package org.netbeans.modules.xslt.tmap.nodes;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public abstract class TMapComponentNodeChildren<T extends TMapComponent> extends Children.Keys
        implements ReloadableChildren 
{

    private Lookup myLookup;
    private T myComponent;
    
    public TMapComponentNodeChildren(T component, Lookup lookup) {
        myLookup = lookup;
        myComponent = component;
    }

    public Lookup getLookup() {
        return myLookup; 
    }
    
    public T getReference() {
        return myComponent;
    }
    
    public abstract Collection getNodeKeys();

    public abstract boolean isSupportedKey(Object key);
    
    protected Node[] createNodes(Object key) {
        if (isSupportedKey(key) && key instanceof TMapComponent) {
            NavigatorNodeFactory factory = NavigatorNodeFactory.getInstance();
            Node childNode = factory.createNode((TMapComponent)key, getLookup());
            if (childNode != null) {
                return new Node[] {childNode};
            }
        } 
        
        return new Node[0];
    }

    @Override
    protected void addNotify() {
        reload();
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }
    
    public void reload() {
        setKeys(getNodeKeys());
    }
}
