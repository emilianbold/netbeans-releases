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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.If;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 4 May 2006
 *
 */
public class ThenChildren extends BpelNodeChildren<If> {

    public ThenChildren(If entity, Lookup contextLookup) {
        super(entity, contextLookup);
    }

    public Collection getNodeKeys() {
        If ref = getReference();
        if (ref == null) {
            return Collections.EMPTY_LIST;
        }
        
        List<BpelEntity> childs = new ArrayList<BpelEntity>();
        
        // set activity node
        Activity activity = (Activity) ref.getActivity();
        if (activity != null) {
            childs.add(activity);
        }
        
        return childs;
    }
}
