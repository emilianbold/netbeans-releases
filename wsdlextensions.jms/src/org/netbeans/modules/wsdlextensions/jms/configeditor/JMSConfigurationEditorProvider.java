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

package org.netbeans.modules.wsdlextensions.jms.configeditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.wsdlextensions.jms.JMSConstants;
import org.netbeans.modules.wsdlextensions.jms.JMSOperation;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.soa.wsdl.bindingsupport.ui.util.BindingComponentUtils;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author skini
 */
public class JMSConfigurationEditorProvider extends ExtensibilityElementConfigurationEditorProvider {

    private int mTemplateMode = JMSConstants.INBOUND_ONE_WAY;
    private String mLinkDirection = null;
    private QName qname;
    private WSDLComponent wsdlComponent;
    
    private Map<Object, ExtensibilityElementConfigurationEditorComponent> operationToEditorMap = new HashMap<Object, ExtensibilityElementConfigurationEditorComponent>();

    @Override
    public String getNamespace() {
        return "http://schemas.sun.com/jbi/wsdl-extensions/jms/";
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
                    mTemplateMode = JMSConstants.INBOUND_REQ_RESP;                   
                } else if (BindingComponentUtils.getInputBindingOperationCount(component) > 0) {
                    mTemplateMode = JMSConstants.INBOUND_ONE_WAY;
                }                
            } else if (mLinkDirection.equals(ExtensibilityElementConfigurationEditorComponent.BP_TO_BC_DIRECTION)) {
                // from direction of link, it is an outbound.  we need now look 
                // model to see if one way, solicited read, or request/response
                if ((BindingComponentUtils.getInputBindingOperationCount(component) > 0) &&
                        (BindingComponentUtils.getOutputBindingOperationCount(component) > 0)) {
                    // if verb attribute exists, then check if Solicited Read;
                    // otherwise, not supported      
                    if (isSolicited(wsdlComponent)) {
                        mTemplateMode = JMSConstants.SOLICITED_REC;
                    } else {
                        mTemplateMode = -1;
                    }
                } else if (BindingComponentUtils.getInputBindingOperationCount(component) > 0) {
                    mTemplateMode = JMSConstants.OUTBOUND_ONE_WAY;
                }
            }
        }
        if (mTemplateMode == JMSConstants.INBOUND_ONE_WAY) {
            return new InboundOneWayMainConfigurationEditorComponent(qname, component);
        } else if (mTemplateMode == JMSConstants.OUTBOUND_ONE_WAY) {
            return new OutboundOneWayMainConfigurationEditorComponent(qname, component);
        } else if (mTemplateMode == JMSConstants.INBOUND_REQ_RESP) {
            return new InboundRequestResponseMainConfigurationEditorComponent(qname, component);
        } else if (mTemplateMode == JMSConstants.SOLICITED_REC) {
            return new SolicitedMainConfigurationEditorComponent(qname, component);
        } else if (mTemplateMode == JMSConstants.OUTBOUND_REQ_RESP) {
            return new OutboundRequestResponseMainConfigurationEditorComponent(qname, component);
        } else {
            // unsupported template mode
            return null;
        }        

//        if (mDirectionMode == JMSConstants.INBOUND_ONE_WAY) {
//            return new InboundOneWayMainConfigurationEditorComponent(qname, component);
//        } else if (mDirectionMode == JMSConstants.OUTBOUND_ONE_WAY) {
//            return new OutboundOneWayMainConfigurationEditorComponent(qname, component);
//        } else if (mDirectionMode == JMSConstants.INBOUND_REQ_RESP) {
//            return new InboundRequestResponseMainConfigurationEditorComponent(qname, component);
//        } else if (mDirectionMode == JMSConstants.OUTBOUND_REQ_RESP) {
//            return new OutboundRequestResponseMainConfigurationEditorComponent(qname, component);
//        } else if (mDirectionMode == JMSConstants.SOLICITED_REC) {
//            return new SolicitedMainConfigurationEditorComponent(qname, component);
//        } 
//        
//        // default to original design
//        return new JMSConfigurationEditorComponent(qname, component);        
    }    
    
  @Override
    public ExtensibilityElementConfigurationEditorComponent getOperationBasedEditorComponent(Operation operation) {
        if (operationToEditorMap.containsKey(operation)) {
            return operationToEditorMap.get(operation);
        }
        ExtensibilityElementConfigurationEditorComponent component = getComponent(qname, wsdlComponent);
        operationToEditorMap.put(operation, component);
        
        // update the visual per the selected operation
        if (component instanceof InboundOneWayMainConfigurationEditorComponent) {
            ((InboundOneWayMainConfigurationEditorComponent) component).setOperation(operation);
            ((InboundOneWayMainConfigurationEditorComponent) component).enablePayloadProcessing(false);
        } else if (component instanceof OutboundOneWayMainConfigurationEditorComponent) {
            ((OutboundOneWayMainConfigurationEditorComponent) component).setOperation(operation);
            ((OutboundOneWayMainConfigurationEditorComponent) component).enablePayloadProcessing(false);
        } else if (component instanceof InboundRequestResponseMainConfigurationEditorComponent) {
            ((InboundRequestResponseMainConfigurationEditorComponent) component).setOperation(operation);
            ((InboundRequestResponseMainConfigurationEditorComponent) component).enablePayloadProcessing(false);          
        } else if (component instanceof OutboundRequestResponseMainConfigurationEditorComponent) {
            ((OutboundRequestResponseMainConfigurationEditorComponent) component).setOperation(operation);
            ((OutboundRequestResponseMainConfigurationEditorComponent) component).enablePayloadProcessing(false);
        } else if (component instanceof SolicitedMainConfigurationEditorComponent) {
            ((SolicitedMainConfigurationEditorComponent) component).setOperation(operation);
            ((SolicitedMainConfigurationEditorComponent) component).enablePayloadProcessing(false);
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
        mTemplateMode = JMSConstants.INBOUND_ONE_WAY;
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
                        Collection<JMSOperation> fileOps =
                                bindingOp.getExtensibilityElements(JMSOperation.class);
                        if ((fileOps != null) && (!fileOps.isEmpty())) {
                            for (JMSOperation fileOp : fileOps) {
                                String verb = fileOp.getVerb();
                                if (JMSConstants.VERB_READ.equals(verb)) {
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
}
