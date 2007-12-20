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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;

import java.io.IOException;

import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

public class InputNewType extends NewType {
    
    private Operation mOperation = null;
    
    public InputNewType(Operation operation) {
        mOperation = operation;
    }
    

    @Override
    public String getName() {
        return NbBundle.getMessage(InputNewType.class, "LBL_NewType_OperationInput");
    }
    
    @Override
    public void create() throws IOException {
        WSDLModel model = mOperation.getModel();
        model.startTransaction();
        String operationInputName = NameGenerator.getInstance().generateUniqueOperationInputName(mOperation);
        Input input = model.getFactory().createInput();
        input.setName(operationInputName);
        mOperation.setInput(input);
        model.endTransaction();
        ActionHelper.selectNode(input);
    }
}
