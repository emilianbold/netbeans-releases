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
package org.netbeans.modules.wsdlextensions.jms.template;


import org.netbeans.modules.wsdlextensions.jms.configeditor.AdvancedStepWizardPanel;
import org.netbeans.modules.wsdlextensions.jms.configeditor.InboundMessageConsumerStepWizardPanel;
import org.netbeans.modules.wsdlextensions.jms.configeditor.InboundMessageStepWizardPanel;
import org.netbeans.modules.wsdlextensions.jms.configeditor.InboundResponseMessageConsumerStepWizardPanel;
import org.netbeans.modules.wsdlextensions.jms.configeditor.InboundResponseMessageStepWizardPanel;
import org.netbeans.modules.wsdlextensions.jms.configeditor.OutboundOneWayConnectionStepWizardPanel;
import org.netbeans.modules.wsdlextensions.jms.configeditor.OutboundOneWayPublisherStepWizardPanel;
import org.netbeans.modules.wsdlextensions.jms.configeditor.OutboundResponsePublisherStepWizardPanel;
import org.netbeans.modules.wsdlextensions.jms.configeditor.OutboundResponseReplyStepWizardPanel;
import org.netbeans.modules.wsdlextensions.jms.configeditor.SynchronousInboundMessageStepWizardPanel;
import org.netbeans.modules.wsdlextensions.jms.configeditor.SynchronousStepWizardPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardExtensionIterator;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.NbBundle;

/**
 *
 * @author jalmero
 */
public class JMSWSDLWizardExtensionIterator extends WSDLWizardExtensionIterator {

    private String templateName;
    private String[] steps;
    private int currentStepIndex = -1;
    private WSDLWizardDescriptorPanel[] panels;
    private static String INBOUND_ONE_WAY = "InboundOneWay"; // NOI18N
    private static String OUTBOUND_ONE_WAY = "OutboundOneWay"; // NOI18N
    private static String INBOUND_REQ_RESP = "InboundRequestResponse"; // NOI18N
    private static String OUTBOUND_REQ_RESP = "OutboundRequestResponse"; // NOI18N
    private static String SYNC_READ = "SyncRead";    // NOI18N
    
    public JMSWSDLWizardExtensionIterator(WSDLWizardContext context) {
        super(context);
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
        currentStepIndex = -1;
        WSDLWizardContext context = getWSDLWizardContext();
        
        //Use NbBundle.getMessage to I18N the step names.
        if (templateName.equals(INBOUND_ONE_WAY)) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new InboundMessageStepWizardPanel(context),
                        new InboundMessageConsumerStepWizardPanel(context),
                        new AdvancedStepWizardPanel(context)
                    };
        } else if (templateName.equals(INBOUND_REQ_RESP)) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new InboundMessageStepWizardPanel(context),
                        new InboundResponseMessageStepWizardPanel(context),
                        new InboundResponseMessageConsumerStepWizardPanel(context),
                        new AdvancedStepWizardPanel(context)
                    };
        } else if (templateName.equals(OUTBOUND_ONE_WAY)) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new OutboundOneWayConnectionStepWizardPanel(context),
                        new OutboundOneWayPublisherStepWizardPanel(context),
                        new AdvancedStepWizardPanel(context)
                    };
        } else if (templateName.equals(OUTBOUND_REQ_RESP)) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new OutboundOneWayConnectionStepWizardPanel(context),
                        new OutboundResponseReplyStepWizardPanel(context),
                        new OutboundResponsePublisherStepWizardPanel(context),
                        new AdvancedStepWizardPanel(context)
                    };
        } else if (templateName.equals(SYNC_READ)) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new SynchronousInboundMessageStepWizardPanel(context),
                        new SynchronousStepWizardPanel(context),
                        new AdvancedStepWizardPanel(context)
                    };
        }        
        
        if (panels == null) {
            return;
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
        if (panels == null) {
            return false;
        }
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
            } else if (panel instanceof InboundMessageConsumerStepWizardPanel) {
                status = ((InboundMessageConsumerStepWizardPanel)panel).commit();
            } else if (panel instanceof InboundResponseMessageStepWizardPanel) {
                status = ((InboundResponseMessageStepWizardPanel)panel).commit();
            } else if (panel instanceof InboundResponseMessageConsumerStepWizardPanel) {
                status = ((InboundResponseMessageConsumerStepWizardPanel)panel).commit();
            } else if (panel instanceof OutboundOneWayConnectionStepWizardPanel) {
                status = ((OutboundOneWayConnectionStepWizardPanel)panel).commit();
            } else if (panel instanceof OutboundResponseReplyStepWizardPanel) {
                status = ((OutboundResponseReplyStepWizardPanel)panel).commit();
            } else if (panel instanceof OutboundResponsePublisherStepWizardPanel) {
                status = ((OutboundResponsePublisherStepWizardPanel)panel).commit();
            } else if (panel instanceof OutboundOneWayPublisherStepWizardPanel) {
                status = ((OutboundOneWayPublisherStepWizardPanel)panel).commit();
            } else if (panel instanceof SynchronousStepWizardPanel) {
                status = ((SynchronousStepWizardPanel)panel).commit();
            } else if (panel instanceof AdvancedStepWizardPanel) {
                status = ((AdvancedStepWizardPanel)panel).commit();
            }
            
            if (!status) {
                return status;
            }
                      
        }
        return status;
    }
}
