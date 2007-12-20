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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.nodes.Node;

public class GenericWSDLComponentChildren<T extends WSDLComponent> extends RefreshableChildren {
    
    T component;
    
    public GenericWSDLComponentChildren(T element) {
        super();
        component = element;
    }

    T getWSDLComponent() {
        return component;
    }
    
    @Override
    protected Node[] createNodes(Object key) {
        if (key instanceof WSDLComponent) {
            Node node = NodesFactory.getInstance().create(WSDLComponent.class.cast(key));
            if(node != null) {
                return new Node[] {node};
            }
        }
        return null;
    }
    
    @Override
    public Collection<? extends WSDLComponent> getKeys() {
        if(component != null) {
            ArrayList<WSDLComponent> keys = new ArrayList<WSDLComponent>();

            List<WSDLComponent> children = component.getChildren();
            if(children != null) {
                keys.addAll(children);
            }
            return keys;
        }

        return Collections.emptyList();
    }
}
