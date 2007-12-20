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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

public class BindingOperationFaultNewType extends NewType {
    private BindingOperation mBindingOperation = null;
    
    public BindingOperationFaultNewType(WSDLComponent message) {
        mBindingOperation = (BindingOperation) message;
    }
    

    @Override
    public String getName() {
        return NbBundle.getMessage(BindingOperationFaultNewType.class, "LBL_NewType_BindingOperationFault");
    }


    @Override
    public void create() throws IOException {
    	BindingOperation bo = mBindingOperation;
        WSDLModel model = mBindingOperation.getModel();
        List<Fault> faultsNameSet = new ArrayList<Fault>();
        if (bo.getOperation() != null) {
            Operation operation = bo.getOperation().get();
            if (operation != null) {
                Collection<Fault> faults = operation.getFaults();
                for (Fault fault : faults) {
                    faultsNameSet.add(fault);
                }
            }
            Collection<BindingFault> bindingFaults = mBindingOperation.getBindingFaults();
            if (bindingFaults != null && !bindingFaults.isEmpty()) {
                for (BindingFault bFault : bindingFaults) {
                    if (bFault.getFault() != null) {
                        Fault fault = bFault.getFault().get();
                        if (!faultsNameSet.remove(fault)) {
                            break;
                        }
                    }
                }
            }
            
            if (!faultsNameSet.isEmpty()) {
                model.startTransaction();
                Fault fault = faultsNameSet.get(0);
                BindingFault bindingOperationFault = model.getFactory().createBindingFault();
                bindingOperationFault.setName(fault.getName());
                bo.addBindingFault(bindingOperationFault);
                model.endTransaction(); 
                ActionHelper.selectNode(bindingOperationFault);
            }
        }
    }

}
