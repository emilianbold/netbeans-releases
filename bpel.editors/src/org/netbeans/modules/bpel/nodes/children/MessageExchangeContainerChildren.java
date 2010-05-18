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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.MessageExchange;
import org.netbeans.modules.bpel.model.api.MessageExchangeContainer;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class MessageExchangeContainerChildren extends BpelNodeChildren<BaseScope> {
    
    public MessageExchangeContainerChildren(BaseScope baseScope
            , Lookup contextLookup) 
    {
        super(baseScope, contextLookup);
    }

    public Collection getNodeKeys() {
        BaseScope ref = getReference();
        if (ref == null) {
            return Collections.EMPTY_LIST;
        }

        List<BpelEntity> childs = new ArrayList<BpelEntity>();
        
        // set import(schema, wsdl) nodes
        MessageExchangeContainer container = ref.getMessageExchangeContainer();
        MessageExchange[] mExchanges = container == null ? null
                : container.getMessageExchanges();
        if (mExchanges != null && mExchanges.length > 0) {
            childs.addAll(Arrays.asList(mExchanges));
        }
        
        return childs;
    }
}
