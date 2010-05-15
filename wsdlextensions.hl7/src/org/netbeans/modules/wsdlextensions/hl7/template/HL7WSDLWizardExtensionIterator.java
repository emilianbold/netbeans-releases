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

package org.netbeans.modules.wsdlextensions.hl7.template;

import org.netbeans.modules.wsdlextensions.hl7.HL7Constants;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.CommunicationControlPanel;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.wsdlwizard.AbstractStepWizardPanel;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.wsdlwizard.CommunicationControlStepWizardPanel;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.wsdlwizard.GeneralStepWizardPanel;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.wsdlwizard.V2StepWizardPanel;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.wsdlwizard.V3StepWizardPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Vishnuvardhan P.R
 */
public class HL7WSDLWizardExtensionIterator extends org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardExtensionIterator{

    private String templateName;
    private String[] steps;
    private int currentStepIndex = -1;
    private WSDLWizardDescriptorPanel[] panels;
    
    public HL7WSDLWizardExtensionIterator(WSDLWizardContext context) {
        super(context);
    }
    
    @Override
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
        currentStepIndex = -1;
        WSDLWizardContext context = getWSDLWizardContext();
        
        if(templateName.equals(NbBundle.getMessage(
                HL7WSDLWizardExtensionIterator.class, 
                "TEMPLATE_name_HL7V2IN"))){
            String templateConst = HL7Constants.TEMPLATE_IN;
            panels = new WSDLWizardDescriptorPanel[]{
                        new AbstractStepWizardPanel(context,templateConst),
                        new GeneralStepWizardPanel(context,templateConst),
                        new V2StepWizardPanel(context,templateConst),
                        new CommunicationControlStepWizardPanel(context, templateConst)
            };
        }else if(templateName.equals(NbBundle.getMessage(
                HL7WSDLWizardExtensionIterator.class, 
                "TEMPLATE_name_HL7V2OUT"))){
            String templateConst = HL7Constants.TEMPLATE_OUT;
            panels = new WSDLWizardDescriptorPanel[]{
                        new AbstractStepWizardPanel(context,templateConst),
                        new GeneralStepWizardPanel(context,templateConst),
                        new V2StepWizardPanel(context,templateConst),
                        new CommunicationControlStepWizardPanel(context, templateConst)
            };
        }/*else if (templateName.equals(NbBundle.getMessage(
                HL7WSDLWizardExtensionIterator.class, 
                "TEMPLATE_name_HL7V3"))) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new AbstractStepWizardPanel(context,templateName),
                        new GeneralStepWizardPanel(context,templateName),
                        new V3StepWizardPanel(context)
                    };
        }*/
        steps = new String[panels.length];
        int i = 0;
        for (WSDLWizardDescriptorPanel panel : panels) {
            steps[i++] = panel.getName();
        }

        
    }

    @Override
    public WSDLWizardDescriptorPanel current() {
        return panels[currentStepIndex];
    }

    @Override
    public String[] getSteps() {
        assert templateName != null : "template is not set";
        return steps;
    }

    @Override
    public boolean hasNext() {
        return currentStepIndex < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return true;
    }

    @Override
    public void nextPanel() {
        currentStepIndex++;
    }

    @Override
    public void previousPanel() {
        currentStepIndex--;
    }
    
    @Override
    public boolean commit() {
                boolean status = true;
        WSDLWizardContext context = getWSDLWizardContext();
        WSDLModel wsdlModel = context.getWSDLModel();
        
        for (WSDLWizardDescriptorPanel panel : panels) {
            if (panel instanceof AbstractStepWizardPanel){
                status = ((AbstractStepWizardPanel)panel).commit();
            }else if (panel instanceof GeneralStepWizardPanel) {                                
                status = ((GeneralStepWizardPanel)panel).commit();                 
            } else if (panel instanceof V2StepWizardPanel) {
                status = ((V2StepWizardPanel)panel).commit();
            }else if (panel instanceof CommunicationControlStepWizardPanel) {
                status = ((CommunicationControlStepWizardPanel)panel).commit();
            }  else if (panel instanceof V3StepWizardPanel) {
                status = ((V3StepWizardPanel)panel).commit();
            } 
            
            if (!status) {
                return status;
            }
        }
        return status;

    }
    

}
