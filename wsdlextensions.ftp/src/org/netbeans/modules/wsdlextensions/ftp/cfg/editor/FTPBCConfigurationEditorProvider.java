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
package org.netbeans.modules.wsdlextensions.ftp.cfg.editor;

import java.util.ArrayList;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author Sun Microsystems
 */
public class FTPBCConfigurationEditorProvider extends ExtensibilityElementConfigurationEditorProvider {
    private String mLinkDirection = null;
    private QName qname;
    private WSDLComponent wsdlComponent;
    private ExtensibilityElementConfigurationEditorComponent mEditorComp;

    @Override
    public String getNamespace() {
        return "http://schemas.sun.com/jbi/wsdl-extensions/ftp/";
    }

    @Override
    public ExtensibilityElementConfigurationEditorComponent getComponent(QName qname, WSDLComponent component) {
        // based on the direction of the link, we need to determine if it is
        // OneWay (ftp:message or ftp:transfer) or RequestResponse (ftp:message or ftp:transfer) 
        // right visual component
        if (mLinkDirection != null) {
            if (mEditorComp == null) {
                mEditorComp = new CasaFTPBindingEditorComponent(qname, component, mLinkDirection);
            }
        }
        return mEditorComp;
    }

    @Override
    public ExtensibilityElementConfigurationEditorComponent getOperationBasedEditorComponent(Operation operation) {
        ExtensibilityElementConfigurationEditorComponent component = getComponent(qname, wsdlComponent);

        // update the visual per the selected operation
        if (component instanceof CasaFTPBindingEditorComponent) {
            ((CasaFTPBindingEditorComponent) component).setOperation(operation);
            ((CasaFTPBindingEditorComponent) component).enablePayloadProcessing(false);
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
        if (operationList != null && mEditorComp != null) {
            status = mEditorComp.commit();
        }
        cleanup();
        return status;
    }

    @Override
    public void rollbackOperationBasedEditor(ArrayList<Operation> operationList) {
        if (operationList != null && mEditorComp != null) {
            mEditorComp.rollback();
        }
        cleanup();
    }

    private void cleanup() {
        mLinkDirection = null;
        qname = null;
        wsdlComponent = null;
        mEditorComp = null;
    }
}
