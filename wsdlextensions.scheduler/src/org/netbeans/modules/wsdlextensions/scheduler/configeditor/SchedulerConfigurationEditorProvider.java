/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.wsdlextensions.scheduler.configeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.scheduler.model.SchedulerConstants;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.soa.wsdl.bindingsupport.ui.util.BindingComponentUtils;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author sunsoabi_edwong
 */
public class SchedulerConfigurationEditorProvider
        extends ExtensibilityElementConfigurationEditorProvider
        implements SchedulerConstants {

    private OperationMode templateMode = null;
    private String linkDirection = null;
    private QName qname = null;
    private WSDLComponent wsdlComponent = null;
    private Operation operation = null;
    
    private Map<Object, ExtensibilityElementConfigurationEditorComponent>
        operationToEditorMap = new HashMap<Object,
            ExtensibilityElementConfigurationEditorComponent>();

    @Override
    public String getNamespace() {
        return SCHEDULER_NAMESPACE;
    }

    @Override
    public ExtensibilityElementConfigurationEditorComponent getComponent(
            QName qname, WSDLComponent component) {
        if (ExtensibilityElementConfigurationEditorComponent
                .BC_TO_BP_DIRECTION.equals(linkDirection)) {
            if (BindingComponentUtils
                    .getInputBindingOperationCount(component) > 0) {
                templateMode = OperationMode.STATIC;
            }               
        } else if (linkDirection.equals(
                ExtensibilityElementConfigurationEditorComponent
                    .BP_TO_BC_DIRECTION)) {
            if ((BindingComponentUtils
                        .getInputBindingOperationCount(component) > 0)
                    && (BindingComponentUtils
                        .getOutputBindingOperationCount(component) > 0)) {
                templateMode = OperationMode.DYNAMIC;
            }
        }
        
        if (templateMode != null) {
            WSDLModel wsdlModel = component.getModel();
            switch (templateMode) {
            case STATIC:
                if (!(wsdlComponent instanceof Port)) {
                    return null;
                }
                Port port = (Port) wsdlComponent;
                return new SchedulerStaticConfigurationEditorComponent(
                        wsdlModel, port, operation);
            default:
                return null;
            }
        }
        return null;
    }

    @Override
    public ExtensibilityElementConfigurationEditorComponent
            getOperationBasedEditorComponent(Operation operation) {
        this.operation = operation;
        if (operationToEditorMap.containsKey(operation)) {
            return operationToEditorMap.get(operation);
        }
        ExtensibilityElementConfigurationEditorComponent component =
                getComponent(qname, wsdlComponent);
        operationToEditorMap.put(operation, component);
        
        return component;
    }

    @Override
    public void initOperationBasedEditingSupport(WSDLComponent component,
            String linkDirection) {
        wsdlComponent = component;
        this.linkDirection = linkDirection;
    }

    @Override
    public boolean commitOperationBasedEditor(
            ArrayList<Operation> operationList) {
        boolean status = true;
        if (operationList != null) {
            for (Operation op : operationList) {
                ExtensibilityElementConfigurationEditorComponent component =
                        operationToEditorMap.get(op);
                if (component != null) {
                    if (!component.commit() && status) {
                        status = false;
                    }
                }
            }
        }
        cleanup();
        return status;
    }

    @Override
    public void rollbackOperationBasedEditor(
            ArrayList<Operation> operationList) {
        boolean status = true;
        if (operationList != null) {
            for (Operation op : operationList) {
                ExtensibilityElementConfigurationEditorComponent component =
                        operationToEditorMap.get(op);
                if (component != null) {
                    if (!component.rollback() && status) {
                        status = false;
                    }
                }
            }
        }
        cleanup();
    }

    private void cleanup() {
        linkDirection = null;
        qname = null;
        wsdlComponent = null;
        templateMode = null;
        operationToEditorMap.clear();
    }
}
