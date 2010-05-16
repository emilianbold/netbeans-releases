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
package org.netbeans.modules.bpel.nodes.actions;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.references.BpelReference;

/**
 *
 * @author nk160297
 */
public class DeleteCorrelationSetAction extends DeleteAction {
    private static final long serialVersionUID = 1L;

    @Override
    protected void performAction(BpelEntity[] bpelEntities) {
        BpelContainer container = bpelEntities[0].getParent();
        assert container instanceof CorrelationSetContainer;

        /* Fix for issue #162377 (http://www.netbeans.org/issues/show_bug.cgi?id=162377)
           Related correlations and their containers should be deleted first
        */
        deleteRelatedCorrelations(bpelEntities);
        //
        super.performAction(bpelEntities);
        //
        CorrelationSetContainer corrSetContainer = (CorrelationSetContainer)container;
        if (corrSetContainer.sizeOfCorrelationSet() == 0) {
            assert corrSetContainer.getParent() != null;
            corrSetContainer.getParent().remove(corrSetContainer);
        }
    }

    /**
     * Fix for issue #162377 (http://www.netbeans.org/issues/show_bug.cgi?id=162377)
     * Related correlations and their containers should be deleted first.
     */
    private void deleteRelatedCorrelations(BpelEntity[] bpelEntities) {
        if ((bpelEntities == null) || (bpelEntities.length < 1) ||
            (! (bpelEntities[0] instanceof CorrelationSet))) {
            return;
        }

        List<Correlation> relatedCorrelations = getRelatedCorrelationList(bpelEntities);
        for (Correlation correlation : relatedCorrelations) {
            super.performAction(new BpelEntity[] {correlation});
        }
    }

    private List<Correlation> getRelatedCorrelationList(BpelEntity[] correlationSets) {
        List<Correlation> correlationList = null,
                          relatedCorrelations = new ArrayList<Correlation>();

        for (BpelEntity correlationSet : correlationSets) {
            if (correlationList == null) {
                correlationList = new ArrayList<Correlation>();
                getAllCorrelations(correlationList,
                    correlationSet.getBpelModel().getProcess());
            }

            List<Correlation> tempCorrelationList = new ArrayList<Correlation>();
            for (Correlation correlation : correlationList) {
                BpelReference<CorrelationSet> relatedCorrelationSetRef =
                    correlation.getSet();
                if ((relatedCorrelationSetRef != null) &&
                    (((CorrelationSet) correlationSet).equals(relatedCorrelationSetRef.get()))) {
                    tempCorrelationList.add(correlation);
                }
            }
            relatedCorrelations.addAll(tempCorrelationList);
            correlationList.removeAll(tempCorrelationList);
        }
        return relatedCorrelations;
    }

    private void getAllCorrelations(List<Correlation> correlationList,
        BpelEntity bpelEntity) {
        if (bpelEntity == null) {
            return;
        }
        if (correlationList == null) {
            correlationList = new ArrayList<Correlation>();
        }
        
        List<BpelEntity> children = bpelEntity.getChildren();
        if (children == null) return;

        for (BpelEntity childBpelEntity : children) {
            if (childBpelEntity instanceof Correlation) {
                correlationList.add((Correlation) childBpelEntity);
            }
            getAllCorrelations(correlationList, childBpelEntity);
        }
    }
}