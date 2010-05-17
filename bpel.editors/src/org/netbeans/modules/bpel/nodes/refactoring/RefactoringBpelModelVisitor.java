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
package org.netbeans.modules.bpel.nodes.refactoring;

import java.util.List;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor;
import org.netbeans.modules.xml.xam.Component;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class RefactoringBpelModelVisitor extends BpelModelVisitorAdaptor {
    
    private List<Component> myPresentedUsageStructure;
    
    public RefactoringBpelModelVisitor(List<Component> presentedUsageStructure ) {
        myPresentedUsageStructure = presentedUsageStructure;
        if (myPresentedUsageStructure == null) {
            throw new IllegalStateException("the list myPresentedUsageStructure shouldn't be null"); // NOI18N
        }
    }

    @Override
    public void visit(PartnerLink usageEntity) {
        if (usageEntity == null) {
            return;
        }
        setUsageObject(usageEntity);
        
        BpelEntity parent = usageEntity.getParent();
        parent = parent == null ? null : parent.getParent();
        if (!(parent instanceof Process)) {
            setUsageContext(parent);
        }
    }

    @Override
    public void visit(Assign usageEntity) {
        if (usageEntity == null) {
            return;
        }
        setUsageObject(usageEntity);
        
        BpelEntity parent = usageEntity.getParent();
        parent = parent == null ? null : parent.getParent();
        setUsageContext(parent);
    }

    @Override
    public void visit(BooleanExpr usageEntity) {
        setThreeLevelGeneralStructure(usageEntity);
    }

    @Override
    public void visit(Branches usageEntity) {
        setThreeLevelGeneralStructure(usageEntity);
    }

    @Override
    public void visit(From usageEntity) {
        setThreeLevelGeneralStructure(usageEntity);
    }

    @Override
    public void visit(To usageEntity) {
        setThreeLevelGeneralStructure(usageEntity);
    }

    @Override
    public void visit(For usageEntity) {
        setTwoLevelGeneralStructure(usageEntity);
    }

    @Override
    public void visit(RepeatEvery usageEntity) {
        setThreeLevelGeneralStructure(usageEntity);
    }

    @Override
    public void visit(Variable usageEntity) {
        setTwoLevelGeneralStructure(usageEntity);
    }

    @Override
    public void visit(Import usageEntity) {
        setTwoLevelGeneralStructure(usageEntity);
    }

    @Override
    protected void visit(Activity usageEntity) {
        setTwoLevelGeneralStructure(usageEntity);
    }
    

    private void setThreeLevelGeneralStructure(BpelEntity usageEntity) {
        if (usageEntity == null) {
            return;
        }
        setUsageDetail(usageEntity);
        
        BpelEntity parent = usageEntity.getParent();
        setUsageObject(parent);
        setUsageContext(parent == null ? null : parent.getParent());
    }

    private void setTwoLevelGeneralStructure(BpelEntity usageEntity) {
        if (usageEntity == null) {
            return;
        }
        setUsageObject(usageEntity);
        setUsageContext(usageEntity.getParent());
    }
    
    private void setUsageContext(BpelEntity usageEntity) {
        if (usageEntity == null) {
            return;
        }
        setUsageContextCookie(usageEntity);
        myPresentedUsageStructure.add(0,usageEntity);
    }
    
    private void setUsageObject(BpelEntity usageEntity) {
        if (usageEntity == null) {
            return;
        }
        setUsageObjectCookie(usageEntity);
        myPresentedUsageStructure.add(0,usageEntity);
    }
    
    private void setUsageDetail(BpelEntity usageEntity) {
        if (usageEntity == null) {
            return;
        }
        setUsageDetailCookie(usageEntity);
        myPresentedUsageStructure.add(0,usageEntity);
    }
    
    private void setUsageContextCookie(BpelEntity usageEntity) {
        setUsageTypeCookie(UsageNodeType.USAGE_CONTEXT, usageEntity);
    }
    
    private void setUsageObjectCookie(BpelEntity usageEntity) {
        setUsageTypeCookie(UsageNodeType.USAGE_OBJECT, usageEntity);
    }
    
    private void setUsageDetailCookie(BpelEntity usageEntity) {
        setUsageTypeCookie(UsageNodeType.USAGE_DETAIL, usageEntity);
    }
    
    private void setUsageTypeCookie(UsageNodeType usageType, BpelEntity usageEntity) {
        usageEntity.removeCookie(UsageNodeType.class);
        usageEntity.setCookie(UsageNodeType.class, usageType);
    }
}
