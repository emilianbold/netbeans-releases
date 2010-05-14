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

package org.netbeans.modules.wsdlextensions.jdbc.configeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author Naveen K
 */
public class DBConfigurationEditorProvider extends ExtensibilityElementConfigurationEditorProvider{
    
    DBConfigurationEditorComponent dbConfigEditorComp;
    private String mLinkDirection = null;
    private QName qname;
    private WSDLComponent wsdlComponent;
    private Map<Object, ExtensibilityElementConfigurationEditorComponent> operationToEditorMap = new HashMap<Object, ExtensibilityElementConfigurationEditorComponent>();
    
    @Override
    public String getNamespace() {
        return NbBundle.getMessage(DBConfigurationEditorProvider.class, 
                "DBConfigurationEditorProvider.NameSpace");
    }

    @Override
    public ExtensibilityElementConfigurationEditorComponent getComponent(QName qname, WSDLComponent component) {
    	this.qname = qname;
    	this.wsdlComponent = component;
        dbConfigEditorComp = new DBConfigurationEditorComponent(qname, component);
        return dbConfigEditorComp;
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
    @Override
    public void initOperationBasedEditingSupport(WSDLComponent component, String linkDirection) {
       this.wsdlComponent = component;
       this.mLinkDirection = linkDirection;
    }

    /**
     * Return the component for the operation. This can be called multiple times, so it is recommended to cache it.
     *
     * @param operation
     * @return
     */
    @Override
    public ExtensibilityElementConfigurationEditorComponent getOperationBasedEditorComponent(Operation operation) {
        if (operationToEditorMap.containsKey(operation)) {
            return operationToEditorMap.get(operation);
        }
        
        ExtensibilityElementConfigurationEditorComponent dbExConfigEditorComp = getComponent(qname, wsdlComponent);
        ((DBConfigurationEditorComponent)dbExConfigEditorComp).setOperation(operation);
        operationToEditorMap.put(operation, dbExConfigEditorComp);
        return dbExConfigEditorComp;
    }


    /**
     * Called when OK is pressed in the dialog, commit all the panels related to each operation in the operation list.
     *
     * @param operationList
     * @return true if successfully committed
     */
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
   
    /**
     * Called when dialog is cancelled/closed, rollback all the panels related to each operation in the operation list.
     * Can be used to cleanup.
     * @param operationList
     */
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
        operationToEditorMap.clear();
    }


}
