/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.wsdl.bindingsupport.configeditor.ui;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author skini
 */
public class ExtensibilityElementConfigurationUtils {

    public static boolean configureBinding(ExtensibilityElementConfigurationEditorProvider provider, ExtensibilityElement wsdlComp, QName qName) {
        Collection<Operation> operations = null;
        if (wsdlComp != null && wsdlComp instanceof Port) {
            Port port = (Port) wsdlComp;
            Binding binding = port.getBinding().get();
            PortType porttype = binding.getType().get();
            operations = porttype.getOperations();

        }

        ExtensibilityElementConfigurationEditorComponent component = provider.getComponent(wsdlComp.getQName(), wsdlComp);
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel(operations, false);
        boolean hasComponents = panel.setProvider(provider);
        if (!hasComponents) return true;

        DialogDescriptor descriptor = new DialogDescriptor(panel, component.getTitle(), provider.isModal(), null);
        panel.setDialogDescriptor(descriptor);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.pack();
        dlg.setVisible(true);
        boolean status = false;
        WSDLModel model = wsdlComp.getModel();
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            if (!model.isIntransaction()) {
                model.startTransaction();
            }
            status = component.commit();
            if (wsdlComp.getModel().isIntransaction()) {
                wsdlComp.getModel().endTransaction();
            }
        } else {
            component.rollback();
            if (wsdlComp.getModel().isIntransaction()) {
                wsdlComp.getModel().rollbackTransaction();
            }
        }
        return status;
    }

    public static boolean configureBinding(ExtensibilityElementConfigurationEditorProvider provider,
            WSDLComponent wsdlComp, String linkDirection) {

        Collection<Operation> operations = null;
        PortType porttype = null;
        if (wsdlComp != null) {
            if (wsdlComp instanceof Port) {
                Port port = (Port) wsdlComp;
                Binding binding = port.getBinding().get();
                porttype = binding.getType().get();
            } else if (wsdlComp instanceof Binding) {
                porttype = ((Binding) wsdlComp).getType().get();
            }
            if (porttype != null) {
                operations = porttype.getOperations();
            }

        }
//        boolean allowMultipleOperationConfiguration = !ExtensibilityElementConfigurationEditorComponent.BC_TO_BP_DIRECTION.equals(linkDirection);
        boolean allowMultipleOperationConfiguration = operations.size() > 1;
        provider.initOperationBasedEditingSupport(wsdlComp, linkDirection);
        ConfigurationEditorPanel panel = new ConfigurationEditorPanel(operations, allowMultipleOperationConfiguration);
        boolean hasComponents = panel.setProvider(provider);
        if (!hasComponents) return true;        

        DialogDescriptor descriptor = new DialogDescriptor(panel, panel.getTitle(), provider.isModal(), null);
        panel.setDialogDescriptor(descriptor);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.pack();
        dlg.setVisible(true);
        boolean status = false;
        WSDLModel model = wsdlComp.getModel();
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            if (!model.isIntransaction()) {
                model.startTransaction();
            }
            ArrayList<Operation> operationList = new ArrayList<Operation>();
            if (allowMultipleOperationConfiguration) {
                operationList.addAll(panel.getAllConfiguredOperations());
            } else {
                Operation operation = panel.getSelectedOperation();
                operationList.add(operation);
            }
            status = provider.commitOperationBasedEditor(operationList);
            if (wsdlComp.getModel().isIntransaction()) {
                wsdlComp.getModel().endTransaction();
            }
        } else {
            ArrayList<Operation> operationList = new ArrayList<Operation>();
            if (allowMultipleOperationConfiguration) {
                operationList.addAll(panel.getAllConfiguredOperations());
            } else {
                Operation operation = panel.getSelectedOperation();
                operationList.add(operation);
            }
            provider.rollbackOperationBasedEditor(operationList);
            if (wsdlComp.getModel().isIntransaction()) {
                wsdlComp.getModel().rollbackTransaction();
            }
        }
        return status;
    }
}
