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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.openide.util.NbBundle;

/**
 *
 * @author aa160298
 */
public class AddCatchAllAction extends BpelNodeAction {

    private static final long serialVersionUID = 1L;

    public AddCatchAllAction() {
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(getClass(), 
                "CTL_DESC_AddCatchAllAction")); // NOI18N
    }    

    
    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_AddCatchAllAction"); // NOI18N
    }
    
    
    public ActionType getType() {
        return ActionType.ADD_CATCH_ALL;
    }
    
    
    protected void performAction(BpelEntity[] bpelEntities) {
        BpelEntity bpelEntity = bpelEntities[0];
        
        CatchAll newCatchAll = bpelEntity.getBpelModel().getBuilder()
                .createCatchAll();
        
        if (bpelEntity instanceof Invoke) {
            Invoke invoke = (Invoke) bpelEntity;
            invoke.setCatchAll(newCatchAll);
        } if (bpelEntity instanceof FaultHandlers) {
            ((FaultHandlers) bpelEntity).setCatchAll(newCatchAll);
        }
    }
    
    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities)) {
            return false;
        }
        
        BpelEntity bpelEntity = bpelEntities[0];
        
        if (bpelEntity instanceof Invoke) {
            Invoke invoke = (Invoke) bpelEntity;
            return (invoke.getCatchAll() == null);
        } else if (bpelEntity instanceof FaultHandlers) {
            return ((FaultHandlers) bpelEntity).getCatchAll() == null;
        }
        return false;
    }
}
