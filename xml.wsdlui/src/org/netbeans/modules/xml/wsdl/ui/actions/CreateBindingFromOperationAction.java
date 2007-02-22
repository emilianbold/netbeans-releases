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

package org.netbeans.modules.xml.wsdl.ui.actions;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.cookies.CreateBindingFromOperationCookie;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class CreateBindingFromOperationAction extends CommonNodeAction {

    private static final long serialVersionUID = 8950680955260279104L;

    @Override
    protected int mode() {
        return MODE_ALL;
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {CreateBindingFromOperationCookie.class};
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null) {
            for (int i = 0; i < activatedNodes.length; i++) {
                Node node = activatedNodes[i];
                CreateBindingFromOperationCookie cookie = (CreateBindingFromOperationCookie) node.getCookie(CreateBindingFromOperationCookie.class);
                if (cookie != null) {
                    Operation operation = cookie.getOperation();
                    WSDLModel model = operation.getModel();
                    
                    model.startTransaction();
                    PortType portType = (PortType)operation.getParent();
                    Binding binding = model.getFactory().createBinding();
                    String bindingName = NameGenerator.getInstance().generateUniqueBindingName(model, portType.getName());
                    binding.setName(bindingName);
                    binding.setType(binding.createReferenceTo(portType, PortType.class));
                    BindingOperation bo = model.getFactory().createBindingOperation();
                    bo.setName(operation.getName());
                    bo.setOperation(bo.createReferenceTo(operation, Operation.class));
                    binding.addBindingOperation(bo);
                    Input input = operation.getInput();
                    if (input != null) {
                        BindingInput ip = model.getFactory().createBindingInput();
                        ip.setName(input.getName());
                        bo.setBindingInput(ip);
                    }
                    Output output = operation.getOutput();
                    if (output != null) {
                        BindingOutput op = model.getFactory().createBindingOutput();
                        op.setName(output.getName());
                        bo.setBindingOutput(op);
                    }
                    Collection faults = operation.getFaults();
                    if (faults != null && faults.size() > 0) {
                        Iterator faultIter = faults.iterator();
                        while (faultIter.hasNext()) {
                            Fault fault = (Fault) faultIter.next();
                            BindingFault bf = model.getFactory().createBindingFault();
                            bf.setName(fault.getName());
                            bo.addBindingFault(bf);
                        }
                    }
                    model.getDefinitions().addBinding(binding);
                    model.endTransaction();
                }
                
            }
        }

    }

    @Override
    public String getName() {
        return NbBundle.getMessage(CreateBindingFromOperationAction.class, "CreateBindingFromOperation_DISPLAY_NAME");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
