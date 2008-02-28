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

import java.awt.Image;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.nodes.images.FolderIcon;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.children.MessageExchangeContainerChildren;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class MessageExchangeContainerNode
        extends ContainerBpelNode<BaseScope, MessageExchangeContainer>
        implements ReloadableChildren {
    
    public MessageExchangeContainerNode(final BaseScope baseScope
            , Children children
            , Lookup lookup) {
        super(baseScope,children, lookup);
    }
    
    public MessageExchangeContainerNode(final BaseScope baseScope
            , Lookup lookup) {
        super(baseScope, lookup);
        //
        Children.MUTEX.postWriteRequest(new Runnable() {
            public void run() {
                setChildren(new MessageExchangeContainerChildren(baseScope, getLookup()));
            }
        });
    }
    
    public NodeType getNodeType() {
        return NodeType.MESSAGE_EXCHANGE_CONTAINER;
    }
    
    public MessageExchangeContainer getContainerReference() {
        BaseScope ref = getReference();
        return ref == null ? null : ref.getMessageExchangeContainer();
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_MESSAGE_EXCHANGE,
            ActionType.SEPARATOR,
            ActionType.GO_TO_MSG_EX_CONTAINER_SOURCE
        };
    }
    
    public void reload() {
        Children.MUTEX.postWriteRequest(new Runnable() {
            public void run() {
                BaseScope ref = getReference();
                if (ref != null) {
                    setChildren(new MessageExchangeContainerChildren(ref, getLookup()));
                }
            }
        });
    }
    
    public Image getIcon(int type) {
        return FolderIcon.getClosedIcon();
    }

    public Image getOpenedIcon(int type) {
        return FolderIcon.getOpenedIcon();
    }

    public String getDisplayName() {
        return getNodeType().getDisplayName();
    }

    protected String getImplHtmlDisplayName() {
        return getDisplayName();
    }
}
