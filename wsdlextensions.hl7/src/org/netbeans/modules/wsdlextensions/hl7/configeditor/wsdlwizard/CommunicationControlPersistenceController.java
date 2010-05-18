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

package org.netbeans.modules.wsdlextensions.hl7.configeditor.wsdlwizard;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.CommunicationControlForm;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.CommunicationControlWsdlAdapter;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.HL7Error;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.ModelModificationException;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author Vishnuvardhan P.R
 */
public class CommunicationControlPersistenceController {

    
    private CommunicationControlForm commControlPanel = null;
    private WSDLComponent mWSDLComponent = null;
    
    private static final Logger logger = Logger.getLogger(
            GeneralStepPersistenceController.class.getName());
    private String templateConstant;

    
    public CommunicationControlPersistenceController(WSDLComponent wsdlComponent,
            CommunicationControlForm visualComponent, String templateConstant) {
        commControlPanel = visualComponent;
        mWSDLComponent = wsdlComponent;
        this.templateConstant = templateConstant;
    }
    
        /**
     * Commit all changes
     * @return
     */
    public boolean commit() {
        boolean success = true;
        /*HL7Error hl7Error = commControlPanel.validateMe();
        if (ExtensibilityElementConfigurationEditorComponent.
                PROPERTY_ERROR_EVT.equals(hl7Error.getErrorMode())) {
            return false;
        } */          
        commControlPanel.commit();
        CommunicationControlForm.Model model = (CommunicationControlForm.Model)commControlPanel.getModel();
        CommunicationControlWsdlAdapter modelAdapter =
                new CommunicationControlWsdlAdapter(mWSDLComponent.getModel(),this.templateConstant);
        modelAdapter.focus(mWSDLComponent);
        try {
            CommunicationControlForm.syncCommControlPanel_ToFrom(modelAdapter, model);
        } catch (ModelModificationException ex) {
            logger.log(Level.SEVERE, "Configuration failed to save to WSDL document", ex);
            success = false;
        }

        return success;
    }



}
