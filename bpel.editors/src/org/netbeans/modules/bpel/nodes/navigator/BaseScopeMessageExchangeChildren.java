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

package org.netbeans.modules.bpel.nodes.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.nodes.children.BpelNodeChildren;
import org.netbeans.modules.bpel.nodes.children.ChildrenType;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 10 April 2006
 *
 */
public class BaseScopeMessageExchangeChildren extends BpelNodeChildren<BaseScope> {
    
    public BaseScopeMessageExchangeChildren(BaseScope entity, Lookup contextLookup) {
        super(entity, contextLookup);
    }
    
    public Collection getNodeKeys() {
        BaseScope ref = getReference();
        if (ref == null) {
            return Collections.EMPTY_LIST;
        }
        
        List<BpelEntity> childs = new ArrayList<BpelEntity>();
        
        //set MessageExchange nodes
        MessageExchangeContainer mExContainer = ref.getMessageExchangeContainer();
        if (mExContainer  != null) {
            MessageExchange[] mExs = mExContainer.getMessageExchanges();
            if (mExs != null && mExs.length > 0) {
                childs.addAll(Arrays.asList(mExs));
            }
        }
        
        // Set BaseScope Nodes
        List<BaseScope> scopes = Util.getClosestBaseScopes(ref.getChildren());
        if (scopes != null && scopes.size() > 0) {
            childs.addAll(scopes);
        }
        
        return childs;
    }
    
    protected Node[] createNodes(Object object) {
        if (object == null) {
            return new Node[0];
        }
        NavigatorNodeFactory factory
                = NavigatorNodeFactory.getInstance();
        Node childNode = null;
        
        // create message exchange container node
        if (object instanceof MessageExchange) {
            childNode = factory.createNode(
                    NodeType.MESSAGE_EXCHANGE
                    ,(MessageExchange)object
                    ,getLookup());
        } else if (object instanceof BaseScope) {// create message exchange container
            childNode = factory.createNode(
                    NodeType.SCOPE
                    ,(BaseScope)object
                    , ChildrenType.SCOPE_MESSAGE_EXCHANGES_CHILD
                    ,getLookup());
        }
        
        return childNode == null ? new Node[0] : new Node[] {childNode};
    }
}
