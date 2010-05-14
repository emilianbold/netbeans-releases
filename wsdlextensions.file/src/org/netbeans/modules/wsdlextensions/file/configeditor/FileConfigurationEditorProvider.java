/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.wsdlextensions.file.configeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.file.model.FileConstants;
import org.netbeans.modules.wsdlextensions.file.model.FileOperation;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.soa.wsdl.bindingsupport.ui.util.BindingComponentUtils;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author skini
 */
public class FileConfigurationEditorProvider extends ExtensibilityElementConfigurationEditorProvider {

    private int mTemplateMode = FileConstants.READ_WRITE;
    private String mLinkDirection = null;
    private QName qname;
    private WSDLComponent wsdlComponent;
    
    private Map<Object, ExtensibilityElementConfigurationEditorComponent> operationToEditorMap = new HashMap<Object, ExtensibilityElementConfigurationEditorComponent>();

    @Override
    public String getNamespace() {
        return "http://schemas.sun.com/jbi/wsdl-extensions/file/";
    }

    @Override
    public ExtensibilityElementConfigurationEditorComponent getComponent(QName qname, WSDLComponent component) {
        // based on the direction of the link, we need to determine if it is
        // OneWay or RequestResponse or Solicited Read so we can return the 
        // right visual component
        if (mLinkDirection != null) {
            if (mLinkDirection.equals(
                    ExtensibilityElementConfigurationEditorComponent.BC_TO_BP_DIRECTION)) {
                // from direction of link, it is an inbound.  we need to look at
                // model to see if it is one way or request/response
                if ((BindingComponentUtils.getInputBindingOperationCount(component) > 0) &&
                        (BindingComponentUtils.getOutputBindingOperationCount(component) > 0)) {
                    mTemplateMode = FileConstants.READ_WRITE;                   
                } else if (BindingComponentUtils.getInputBindingOperationCount(component) > 0) {
                    mTemplateMode = FileConstants.READ;
                }                
            } else if (mLinkDirection.equals(ExtensibilityElementConfigurationEditorComponent.BP_TO_BC_DIRECTION)) {
                // from direction of link, it is an outbound.  we need now look 
                // model to see if one way, solicited read, or request/response
                if ((BindingComponentUtils.getInputBindingOperationCount(component) > 0) &&
                        (BindingComponentUtils.getOutputBindingOperationCount(component) > 0)) {
                    // if verb attribute exists, then check if Solicited Read;
                    // otherwise, not supported      
                    if (isSolicited(wsdlComponent)) {
                        mTemplateMode = FileConstants.SOLICITED_READ;
                    } else {
                        // prompt user if user wants to set this as a solicited read
                        mTemplateMode = -1;
                        if (promptForSolicitedRead()) {
                            mTemplateMode = FileConstants.SOLICITED_READ;
                        }                                         
                    }
                } else if (BindingComponentUtils.getInputBindingOperationCount(component) > 0) {
                    mTemplateMode = FileConstants.WRITE;
                }
            }
        }
        if (mTemplateMode == FileConstants.READ) {
            return new FileInboundConfigurationEditorComponent(qname, component);
        } else if (mTemplateMode == FileConstants.WRITE) {
            return new FileOutboundConfigurationEditorComponent(qname, component);
        } else if (mTemplateMode == FileConstants.READ_WRITE) {
            return new FileInboundOutboundConfigurationEditorComponent(qname, component);
        } else if (mTemplateMode == FileConstants.SOLICITED_READ) {
            return new FileSolicitedConfigurationEditorComponent(qname, component);
        } else {
            // unsupported template mode
            return null;
        }
    }

    @Override
    public ExtensibilityElementConfigurationEditorComponent getOperationBasedEditorComponent(Operation operation) {
        if (operationToEditorMap.containsKey(operation)) {
            return operationToEditorMap.get(operation);
        }
        ExtensibilityElementConfigurationEditorComponent component = getComponent(qname, wsdlComponent);
        if (component != null) {
            operationToEditorMap.put(operation, component);
        }
        
        // update the visual per the selected operation
        if (component instanceof FileInboundConfigurationEditorComponent) {
            ((FileInboundConfigurationEditorComponent) component).setOperation(operation);
            ((FileInboundConfigurationEditorComponent) component).enablePayloadProcessing(false);
        } else if (component instanceof FileOutboundConfigurationEditorComponent) {
            ((FileOutboundConfigurationEditorComponent) component).setOperation(operation);
            ((FileOutboundConfigurationEditorComponent) component).enablePayloadProcessing(false);
        } else if (component instanceof FileInboundOutboundConfigurationEditorComponent) {
            ((FileInboundOutboundConfigurationEditorComponent) component).setOperation(operation); 
            ((FileInboundOutboundConfigurationEditorComponent) component).enablePayloadProcessing(false);
        } else if (component instanceof FileSolicitedConfigurationEditorComponent) {
            ((FileSolicitedConfigurationEditorComponent) component).setOperation(operation);
            ((FileSolicitedConfigurationEditorComponent) component).enablePayloadProcessing(false);
        }        
        return component;
    }

    @Override
    public void initOperationBasedEditingSupport(WSDLComponent component, String linkDirection) {
        mLinkDirection = linkDirection;
        wsdlComponent = component;
    }

    @Override
    public boolean commitOperationBasedEditor(ArrayList<Operation> operationList) {
        boolean status = true;
        if (operationList != null) {
            for (Operation operation : operationList) {
                ExtensibilityElementConfigurationEditorComponent component = operationToEditorMap.get(operation);
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
    public void rollbackOperationBasedEditor(ArrayList<Operation> operationList) {
        boolean status = true;
        if (operationList != null) {
            for (Operation operation : operationList) {
                ExtensibilityElementConfigurationEditorComponent component = operationToEditorMap.get(operation);
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
        mLinkDirection = null;
        qname = null;
        wsdlComponent = null;
        mTemplateMode = FileConstants.READ_WRITE;
        operationToEditorMap.clear();
    }
    
    private boolean isSolicited(WSDLComponent wsdlComponent) {
        boolean solicited = false;
        if ((wsdlComponent != null) && (wsdlComponent instanceof Port)) {
            Binding parentBinding = ((Port) wsdlComponent).getBinding().get();
            if (parentBinding != null) {
                Collection<BindingOperation> bindingOps =
                        parentBinding.getBindingOperations();
                if ((bindingOps != null) && (!bindingOps.isEmpty())) {
                    for (BindingOperation bindingOp : bindingOps) {
                        Collection<FileOperation> fileOps =
                                bindingOp.getExtensibilityElements(FileOperation.class);
                        if ((fileOps != null) && (!fileOps.isEmpty())) {
                            for (FileOperation fileOp : fileOps) {
                                String verb = fileOp.getVerb();
                                if (FileConstants.VERB_READ.equals(verb)) {
                                    return true;
                                }
                            }
                        }
                    }
                }                
            }
        }
        return solicited;
    }    

    private boolean promptForSolicitedRead() {
        boolean isSolicited = false;
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(FileConfigurationEditorProvider.class,
                "FileConfigurationEditorProvider.IsSolicited"),
                NbBundle.getMessage(FileConfigurationEditorProvider.class,
                "FileConfigurationEditorProvider.IsSolicitedTitle"),
                NotifyDescriptor.YES_NO_OPTION);
        Object result = DialogDisplayer.getDefault().notify(descriptor);
        if (result.equals(NotifyDescriptor.YES_OPTION)) {
            isSolicited = true;                   
        }
        return isSolicited;
    }
}
