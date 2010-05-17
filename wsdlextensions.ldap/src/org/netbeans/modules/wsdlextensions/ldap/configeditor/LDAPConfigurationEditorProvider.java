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

package org.netbeans.modules.wsdlextensions.ldap.configeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.Operation;

/**
 *
 * @author sadiraju
 */
public class LDAPConfigurationEditorProvider extends ExtensibilityElementConfigurationEditorProvider {
    
    //private int mTemplateMode = LDAPConstants.READ_WRITE;
    private String mLinkDirection = null;
    private QName qname;
    private WSDLComponent wsdlComponent;
    private ExtensibilityElementConfigurationEditorComponent  editorComponent;
    private Map<Object, ExtensibilityElementConfigurationEditorComponent> operationToEditorMap = new HashMap<Object, ExtensibilityElementConfigurationEditorComponent>();

    
    @Override
    public String getNamespace() {
        return "http://schemas.sun.com/jbi/wsdl-extensions/ldap/";
    }

    @Override
    public ExtensibilityElementConfigurationEditorComponent getComponent(QName qname, WSDLComponent component) {
    	editorComponent =  new LDAPConfigurationEditorComponent(qname, component);
    	return editorComponent;
    }
    
    @Override
    public void initOperationBasedEditingSupport(WSDLComponent component, String linkDirection) {
        mLinkDirection = linkDirection;
        wsdlComponent = component;
    }
    
     @Override
    public ExtensibilityElementConfigurationEditorComponent getOperationBasedEditorComponent(Operation operation) {
        /*if (operationToEditorMap.containsKey(operation)) {
            return operationToEditorMap.get(operation);
        }
        ExtensibilityElementConfigurationEditorComponent component = getComponent(qname, wsdlComponent);
        operationToEditorMap.put(operation, component);
        
        // update the visual per the selected operation
        if (component instanceof LDAPConfigurationEditorComponent) {
            ((LDAPConfigurationEditorComponent) component).setOperation(operation);
            
        } */    
         ExtensibilityElementConfigurationEditorComponent component = getComponent(qname, wsdlComponent);
        return component;
    }
     
     public boolean commitOperationBasedEditor(ArrayList<Operation> operationList) {
    	 boolean status = true;
    	 //ExtensibilityElementConfigurationEditorComponent component = getComponent(qname, wsdlComponent);
    	 
         if (editorComponent != null) {
             if (!editorComponent.commit() && status) {
                 status = false;
             }
             
         }
         return status;
     }
    
     /**
      * Called when dialog is cancelled/closed, rollback all the panels related to each operation in the operation list.
      * Can be used to cleanup.
      * @param operationList
      */
     public void rollbackOperationBasedEditor(ArrayList<Operation> operationList) {
        
     }

}
