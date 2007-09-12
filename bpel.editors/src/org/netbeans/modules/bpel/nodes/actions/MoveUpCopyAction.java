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

import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.AssignChild;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Copy;
import org.openide.actions.MoveUpAction;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class MoveUpCopyAction extends BpelNodeAction {
    
    private static final long serialVersionUID = 1L;

    protected String getBundleName() {
        return NbBundle.getMessage(MoveUpCopyAction.class, "CTL_MoveUpCopyAction");
    }

    public ActionType getType() {
        return ActionType.MOVE_COPY_UP;
    }

    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities) || !(bpelEntities[0] instanceof Copy)) {
            return false;
        }
        
        BpelContainer parent = ((Copy)bpelEntities[0]).getParent();
        if (parent == null || !(parent instanceof Assign)) {
            return false;
        }
        
        int curIndex = ((Assign)parent).indexOf(Copy.class,(Copy)bpelEntities[0]);
        if (curIndex > 0) {
            return true;
        }
        return false;
    }

    protected void performAction(BpelEntity[] bpelEntities) {
        if (!enable(bpelEntities)) {
            return;
        }

        BpelContainer parent = ((Copy)bpelEntities[0]).getParent();
        if (parent == null || !(parent instanceof Assign)) {
            return;
        }
        
        int curIndex = ((Assign)parent).indexOf(Copy.class,(Copy)bpelEntities[0]);
        
        Copy cuttedCopy = (Copy)bpelEntities[0].cut();
        ((Assign)parent).insertAssignChild(cuttedCopy,curIndex-1);
    }
}
