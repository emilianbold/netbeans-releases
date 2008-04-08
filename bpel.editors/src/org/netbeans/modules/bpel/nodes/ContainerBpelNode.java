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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * This class represents the base class for nodes which are has one specific
 * feature, which distint them from ordinal nodes.
 * They keeps the reference not to a container object, but to it's parent object.
 * <p>
 * For example, the VariableContainerNode is a container.
 * It keeps reference to BaseScope but not to the VariableContainer.
 * This behaviour is necessary to show such nodes at a tree view
 * even if they are not present at source model.
 * So a user will not ever asked to add container first and then add
 * variable. User will add variable straight away.
 * <p>
 * This method is intended to provide correct node searching.
 * See the findNode method.
 * <P>
 * This class is a generic class ans has additional generic type paramenter.
 * It specifies the type of container reference. 
 *
 * @author nk160297
 */
public abstract class ContainerBpelNode<RT, CT> extends BpelNode<RT> {
    
    public ContainerBpelNode(RT referent, Lookup lookup) {
        super(referent, lookup);
    }
    
    public ContainerBpelNode(RT referent, Children children, Lookup lookup) {
        super(referent, children, lookup);
    }
    
    @Override
    protected boolean isEventRequreUpdate(ChangeEvent event) {
        if (isRequireSpecialUpdate(event, getReference())) {
            return true;
        }
        
        if (event == null) {
            return false;
        }
        
        CT containerRef = getContainerReference();
        if (containerRef != null && event.getParent() == containerRef) {
            return true;
        }

        RT ref = getReference();
        BpelEntity eventParent = event.getParent();
        
        return ref != null && eventParent != null 
                && eventParent.getParent() == ref;
    }
    
    /**
     * The reference to the container object.
     */
    public abstract CT getContainerReference();
}
