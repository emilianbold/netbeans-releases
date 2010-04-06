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

package org.netbeans.modules.xml.wsdl.bindingsupport.spi;

import java.util.ArrayList;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * Provides an interface for Binding Component plugins to provide a configuration panel,
 * which can help users to configure the binding.
 * 
 * @author skini
 */
public abstract class ExtensibilityElementConfigurationEditorProvider {

    
    /**
     * Return the namespace for which this plugin provides components.
     * 
     * @return String namespace corresponding to BC's schema file.
     */
    public abstract String getNamespace();
    
    
    /**
     * Provides component at current context using qname and/or wsdlcomponent.
     * Return an appropriate EditorComponent corresponding to the qname and/or wsdlcomponent.
     * @param qname QName of the element in the wsdl
     * @param component WSDLComponent in the wsdl.
     * @return
     */
    public abstract ExtensibilityElementConfigurationEditorComponent getComponent(QName qname, WSDLComponent component);

    
    /**
     * If configuration is supported on the extensibility element, return true, else false.
     * @param qname qname of the extensibility element
     * @return boolean
     */
    public boolean isConfigurationSupported(QName qname) {
        return true;
    }
    
    /**Override if you want the dialog to be not modal.
     * 
     * @return boolean
     */
    public boolean isModal() {
        return true;
    }

    
    /**
     * The next two methods are interlinked. It allows for binding configuration per operation.
     * This is the entry point from casa editor, the skeleton template is loaded from the template.xml at this point.
     * It is recommended that the port and the link direction is cached and reused when the getComponent(Operation operation) is called.
     * 
     * @param component
     * @param linkDirection
     * @return
     */
    public void initOperationBasedEditingSupport(WSDLComponent component, String linkDirection) {
        
    }

    /**
     * Return the component for the operation. This can be called multiple times, so it is recommended to cache it.
     * 
     * @param operation
     * @return
     */
    public ExtensibilityElementConfigurationEditorComponent getOperationBasedEditorComponent(Operation operation) {
        return null;
    }


    /**
     * Called when OK is pressed in the dialog, commit all the panels related to each operation in the operation list.
     * 
     * @param operationList
     * @return true if successfully committed
     */
    public boolean commitOperationBasedEditor(ArrayList<Operation> operationList) {
        return false;
    }
    
    /**
     * Called when dialog is cancelled/closed, rollback all the panels related to each operation in the operation list.
     * Can be used to cleanup.
     * @param operationList
     */
    public void rollbackOperationBasedEditor(ArrayList<Operation> operationList) {
        
    }
}
