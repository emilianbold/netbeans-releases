/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
