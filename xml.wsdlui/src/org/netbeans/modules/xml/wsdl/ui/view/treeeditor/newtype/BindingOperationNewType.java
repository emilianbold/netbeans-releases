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

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;

import org.netbeans.modules.xml.wsdl.bindingsupport.template.ExtensibilityElementTemplateFactory;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.BindingOperationView;
import org.netbeans.modules.xml.wsdl.ui.wizard.BindingOperationGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

public class BindingOperationNewType extends NewType {
	private WSDLComponent mDef = null;
	
	public BindingOperationNewType(WSDLComponent def) {
		mDef = def;
	}
	
	
	@Override
	public String getName() {
		return NbBundle.getMessage(BindingOperationNewType.class, "LBL_NewType_BindingOperation");
	}
	
	
	@Override
	public void create() throws IOException {
		Binding binding = (Binding) mDef;
		if(!isValidBinding(binding)) {
            return;
        }
		
        PortType portType = binding.getType().get();
        Operation[] selectedOperations = null;
        //If there is only one implementable operation then add it, without showing dialog
        Collection<Operation> operations = Utility.getImplementableOperations(portType, binding);
        if (operations != null && operations.size() == 1) {
        	selectedOperations = new Operation[]{operations.iterator().next()};
        } else {
            BindingOperationView operationView = new BindingOperationView(operations);
            
            final DialogDescriptor dd = 
                new DialogDescriptor(operationView, 
                        NbBundle.getMessage(BindingOperationNewType.class, "AddBindingOperationView_SELECT_OPERATION") , 
                        true, 
                        DialogDescriptor.OK_CANCEL_OPTION, 
                        DialogDescriptor.CANCEL_OPTION, 
                        null);
            operationView.addPropertyChangeListener(new PropertyChangeListener() {
            
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("ENABLE_OK") && evt.getNewValue() instanceof Boolean) {
                        dd.setValid(((Boolean) evt.getNewValue()).booleanValue());
                    }            
                }
            });
            Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
            dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
            dlg.pack();
            dlg.setVisible(true);
            
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            	selectedOperations = operationView.getSelectedOperations();
            }
        }
        if (selectedOperations != null && selectedOperations.length > 0) {
            WSDLModel document = binding.getModel();
            Collection<ExtensibilityElement> exElements = binding.getExtensibilityElements();

            String namespace = null;
            if (!exElements.isEmpty()) {
                namespace = exElements.iterator().next().getQName().getNamespaceURI();
            }
            
            document.startTransaction();
            BindingOperationGenerator generator = new BindingOperationGenerator(binding, namespace, selectedOperations);
            generator.execute();
            document.endTransaction();
            ActionHelper.selectNode(generator.getBindingOperation());
        }
    }
	
	
	private boolean isValidBinding(Binding binding) {
		boolean valid = false;
		String message = null;
		if(binding != null && binding.getType() != null) {
			PortType portType = binding.getType().get();
			if(portType != null && portType.getOperations() != null && portType.getOperations().size() > 0) {
				
				valid = true;
			} else {
				valid = false;
				message = NbBundle.getMessage(BindingOperationNewType.class, "AddBindingOperationView_LONG_DESCRIPTION_MISSING_OPERATION_IN_BINDING_PORTTYPE");
			}
		} else {
			valid = false;
			message = NbBundle.getMessage(BindingOperationNewType.class, "AddBindingOperationView_LONG_DESCRIPTION1_MISSING_PORTTYPE_IN_BINDING");
		}
		
		if(!valid) {
			
			NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
			DialogDisplayer.getDefault().notify(nd);
		}
		
		return valid;
	}
}
