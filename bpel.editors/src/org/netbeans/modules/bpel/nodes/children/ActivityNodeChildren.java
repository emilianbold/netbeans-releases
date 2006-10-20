/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.model.api.BaseFaultHandlers;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CompensationHandlerHolder;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationsHolder;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 02 May 2006
 */
public class ActivityNodeChildren extends BpelNodeChildren<BpelContainer> {

    public ActivityNodeChildren(BpelContainer bpelEntity, Lookup contextLookup) {
        super(bpelEntity, contextLookup);
    }
    
    public Collection getNodeKeys() {
        BpelContainer ref = getReference();
        if (ref == null) {
            return Collections.EMPTY_LIST;
        }
        List<BpelEntity> unmodifiableActivityChilds = ref.getChildren();
        if (unmodifiableActivityChilds == null || unmodifiableActivityChilds.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        
        List<BpelEntity> childs = new ArrayList<BpelEntity>();
        List<BpelEntity> activityChilds = new ArrayList<BpelEntity>();
        activityChilds.addAll(unmodifiableActivityChilds);
        // so as special sort order ()
        // is required do next:
        

        // add correlationSet nodes if required
        if (ref instanceof CorrelationsHolder) {
            CorrelationContainer corrCont = ((CorrelationsHolder)ref)
                                                .getCorrelationContainer();
            if (corrCont != null) {
                activityChilds.remove(corrCont);
                Correlation[] corrs = corrCont.getCorrelations();
                if (corrs != null && corrs.length > 0) {
                    childs.addAll(Arrays.asList(corrs));
                }
            }
        }
        
        // set patterned correlationSet nodes for Invoke 
        if (ref instanceof Invoke) {
            PatternedCorrelationContainer pCorrCont = ((Invoke)ref)
                                            .getPatternedCorrelationContainer();
            if (pCorrCont != null ) {
                activityChilds.remove(pCorrCont);
                PatternedCorrelation[] pCorrs = pCorrCont
                                                .getPatternedCorrelations();
                if (pCorrs != null && pCorrs.length > 0) {
                    childs.addAll(Arrays.asList(pCorrs));
                }
            }
        }

        // add catch/ catchAll if required
        // !(ref instanceof FaultHandlers) need to avoid BaseScope and so on elements
        if (ref instanceof BaseFaultHandlers && !(ref instanceof FaultHandlers)) {
            
            // set catch nodes
            Catch[] catches = ((BaseFaultHandlers)ref).getCatches();
            if (catches != null && catches.length > 0) {
                activityChilds.removeAll(Arrays.asList(catches));
                childs.addAll(Arrays.asList(catches));
            }
            
            // set catchAll node
            CatchAll catchAll = ((BaseFaultHandlers)ref).getCatchAll();
            if (catchAll != null) {
                activityChilds.remove(catchAll);
                childs.add(catchAll);
            }
        }
        
        // CompensationHandler node if required
        if (ref instanceof CompensationHandlerHolder) {
            // set CompensationHandler node
            CompensationHandler compensationHandler = ((CompensationHandlerHolder)ref)
                        .getCompensationHandler();
            if (compensationHandler != null) {
                activityChilds.remove(compensationHandler);
                childs.add(compensationHandler);
            }
        }
        
        // activity childs didn't modified
        if (childs.size() == 0) {
            return activityChilds;
        }
        
        if (activityChilds.size() > 0) {
            childs.addAll(activityChilds);
        }
        
        return childs;
    }

//    protected Node[] createNodes(Object object) {
//        if (object != null && object instanceof BpelEntity) {
//            NavigatorNodeFactory factory = NavigatorNodeFactory.getInstance();
//            Node childNode = factory.createNode((BpelEntity)object,lookup);
//            if (childNode != null) {
//                return new Node[] {childNode};
//            }
//        } 
//        
//        return new Node[0];
//    }
}
