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
package org.netbeans.modules.wsdlextensions.file.template;

import org.netbeans.modules.wsdlextensions.file.configeditor.InboundMessageStepWizardPanel;
import org.netbeans.modules.wsdlextensions.file.configeditor.OutboundMessageInStepWizardPanel;
import org.netbeans.modules.wsdlextensions.file.configeditor.OutboundMessageStepWizardPanel;
import org.netbeans.modules.wsdlextensions.file.configeditor.SolicitedReadStepWizardPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardExtensionIterator;
import org.netbeans.modules.soa.wsdl.bindingsupport.ui.util.BindingComponentUtils;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.NbBundle;

/**
 *
 * @author skini
 */
public class FileWSDLWizardExtensionIterator extends WSDLWizardExtensionIterator {

    private String templateName;
    private String[] steps;
    private int currentStepIndex = -1;
    private WSDLWizardDescriptorPanel[] panels;

    public FileWSDLWizardExtensionIterator(WSDLWizardContext context) {
        super(context);
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
        currentStepIndex = -1;
        WSDLWizardContext context = getWSDLWizardContext();
        
        if (templateName.equals("InboundOneWay")) {     //NOI18N
            panels = new WSDLWizardDescriptorPanel[]{
                        new InboundMessageStepWizardPanel(context)
                    };
        } else if (templateName.equals("OutboundOneWay")) {     //NOI18N
            panels = new WSDLWizardDescriptorPanel[]{
                        new OutboundMessageInStepWizardPanel(context)
                    };
        } else if (templateName.equals("InboundRequestResponse")) {     //NOI18N
            panels = new WSDLWizardDescriptorPanel[]{
                        new InboundMessageStepWizardPanel(context),
                        new OutboundMessageStepWizardPanel(context)
                    };
        } else if (templateName.equals("SyncRead")) {     //NOI18N
            panels = new WSDLWizardDescriptorPanel[]{
                        new SolicitedReadStepWizardPanel(context)
                    };
        }
        
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
            if (panel instanceof InboundMessageStepWizardPanel) {                                
                status = ((InboundMessageStepWizardPanel)panel).commit();                 
            } else if (panel instanceof OutboundMessageInStepWizardPanel) {
                status = ((OutboundMessageInStepWizardPanel)panel).commit();
            } else if (panel instanceof OutboundMessageStepWizardPanel) {
                status = ((OutboundMessageStepWizardPanel)panel).commit();
            } else if (panel instanceof SolicitedReadStepWizardPanel) {
                status = ((SolicitedReadStepWizardPanel)panel).commit();
            }
            
            if (!status) {
                return status;
            }
        }
        return status;
    }
}
