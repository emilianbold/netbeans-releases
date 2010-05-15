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

package org.netbeans.modules.bpel.mapper.multiview;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.soa.xpath.mapper.context.XPathDesignContext;
import org.openide.nodes.Node;

/**
 * Immutable holder of the current state of the BPEL mapper.
 * 
 * @author nk160297
 * @author Vitaly Bychkov
 */
public interface BpelDesignContext extends XPathDesignContext {
    
    Node getActivatedNode();
    BpelModel getBpelModel();

    /**
     * The BPEL entity which defines a design context of the mapper.
     * For example, in case selected entity is Assign, Copy, From or To,
     * it always Assign.
     * @return
     */
    BpelEntity getContextEntity();

    /**
     * The BPEL entity which correspond to current selection. For example in
     * BPEL source editor. For BPEL diagram it usually the same as context entity
     * because the diagram contains only activities. For example, diagram can
     * contain Assign, but can't contain Copy, From, To and so on. 
     * @return
     */
    BpelEntity getSelectedEntity();
  
    VisibilityScope getVisibilityScope();
    
    StringBuffer getValidationErrMsgBuffer();

}
