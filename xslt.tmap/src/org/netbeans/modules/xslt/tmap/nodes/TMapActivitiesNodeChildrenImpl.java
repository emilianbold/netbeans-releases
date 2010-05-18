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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapActivitiesNodeChildrenImpl extends TMapComponentNodeChildren<TMapComponent> {

    public TMapActivitiesNodeChildrenImpl(TMapComponent component, Lookup lookup) {
        super(component, lookup);
    }

    @Override
    protected Node[] createNodes(Object key) {
        if (key == NodeType.VARIABLES_CONTAINER) {
            NavigatorNodeFactory factory = NavigatorNodeFactory.getInstance();
            Node childNode = factory.createNode(
                    NodeType.VARIABLES_CONTAINER ,getReference(), getLookup());
            if (childNode != null) {
                return new Node[] {childNode};
            }
        }
        return super.createNodes(key);
    }

    public Collection getNodeKeys() {
        TMapComponent component = getReference();
        if (component == null) {
            return Collections.EMPTY_LIST;
        }
        
        List<Object> childs  = new ArrayList<Object>();
        if (component instanceof Operation) {
            childs.add(NodeType.VARIABLES_CONTAINER);
        }
        
        List<? extends TMapComponent> componentChild = component.getChildren();
        if (componentChild != null) {
            if (component instanceof Transform && !SoaUtil.isAllowBetaFeatures(component.getModel())) {
                componentChild = ((Transform)component).getParams();
            } 
            childs.addAll(componentChild);
        }
        // 
        return childs != null && childs.size() > 0 
                ? childs 
                : Collections.EMPTY_LIST;
    }

    @Override
    public boolean isSupportedKey(Object key) {
        return key instanceof TMapComponent;
    }
}
