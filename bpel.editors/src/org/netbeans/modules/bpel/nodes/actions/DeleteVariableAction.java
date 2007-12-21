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

import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class DeleteVariableAction extends DeleteAction {
    private static final long serialVersionUID = 1L;

    protected void performAction(BpelEntity[] bpelEntities) {
        BpelContainer container = bpelEntities[0].getParent();
        assert container instanceof VariableContainer;
        //
        super.performAction(bpelEntities);
        //
        VariableContainer varContainer = (VariableContainer)container;
        if (varContainer.sizeOfVariable() == 0) {
            //
            // Ask user to remove the variable container
            //

            // removed due to issue 81219
//            String questionText = NbBundle.getMessage(
//                    DeleteVariableAction.class, "MSG_DeleteVariableContainer"); // NOI18N
//            NotifyDescriptor descr = new NotifyDescriptor(
//                    questionText, "", 0,
//                    NotifyDescriptor.QUESTION_MESSAGE, null, null);
//            Object result = DialogDisplayer.getDefault().notify(descr);
//            if (NotifyDescriptor.YES_OPTION.equals(result) || 
//                    NotifyDescriptor.OK_OPTION.equals(result)) {
                varContainer.getParent().remove(varContainer);
//            }
        }
    }
    
}
