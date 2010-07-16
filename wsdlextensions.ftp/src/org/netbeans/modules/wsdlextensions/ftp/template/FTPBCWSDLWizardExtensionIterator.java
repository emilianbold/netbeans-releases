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
package org.netbeans.modules.wsdlextensions.ftp.template;

import org.netbeans.modules.wsdlextensions.ftp.cfg.editor.FTPSettingsOneWayStepWizardPanel;
import org.netbeans.modules.wsdlextensions.ftp.cfg.editor.FTPSettingsRequestResponseMessageStepWizardPanel;
import org.netbeans.modules.wsdlextensions.ftp.cfg.editor.FTPSettingsRequestResponseTransferStepWizardPanel;
import org.netbeans.modules.wsdlextensions.ftp.cfg.editor.InboundMessagePollReqStepWizardPanel;
import org.netbeans.modules.wsdlextensions.ftp.cfg.editor.InboundMessagePollRespStepWizardPanel;
import org.netbeans.modules.wsdlextensions.ftp.cfg.editor.OutboundMessagePutReqStepWizardPanel;
import org.netbeans.modules.wsdlextensions.ftp.cfg.editor.InboundTransferPollReqStepWizardPanel;
import org.netbeans.modules.wsdlextensions.ftp.cfg.editor.InboundTransferPollRespStepWizardPanel;
import org.netbeans.modules.wsdlextensions.ftp.cfg.editor.OutboundMessagePutRespStepWizardPanel;
import org.netbeans.modules.wsdlextensions.ftp.cfg.editor.OutboundTransferPutReqStepWizardPanel;
import org.netbeans.modules.wsdlextensions.ftp.cfg.editor.OutboundTransferPutRespStepWizardPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardExtensionIterator;

/**
 *
 * @author Sun Microsystems
 */
public class FTPBCWSDLWizardExtensionIterator extends WSDLWizardExtensionIterator {

    private String templateName;
    private String[] steps;
    private int currentStepIndex = -1;
    private WSDLWizardDescriptorPanel[] panels;
    
    public FTPBCWSDLWizardExtensionIterator(WSDLWizardContext context) {
        super(context);
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
        currentStepIndex = -1;
        WSDLWizardContext context = getWSDLWizardContext();
        
        if (templateName.equals("InboundOneWayMessaging")) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new FTPSettingsOneWayStepWizardPanel(context),
                        // is one way, is last
                        new InboundMessagePollReqStepWizardPanel(context, true, true)
                    };
        } else if (templateName.equals("OutboundOneWayMessaging")) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new FTPSettingsOneWayStepWizardPanel(context),
                        // is one way, is last
                        new OutboundMessagePutReqStepWizardPanel(context, true, true)
                    };
        } else if (templateName.equals("InboundRequestResponseMessaging")) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new FTPSettingsRequestResponseMessageStepWizardPanel(context),
                        // not one way, not last
                        new InboundMessagePollReqStepWizardPanel(context, false, false),
                        // not one way, is last
                        new OutboundMessagePutRespStepWizardPanel(context, false, true)
                    };
        } else if (templateName.equals("OutboundRequestResponseMessaging")) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new FTPSettingsRequestResponseMessageStepWizardPanel(context),
                        // not one way, not last
                        new OutboundMessagePutReqStepWizardPanel(context, false, false),
                        // not one way, is last
                        new InboundMessagePollRespStepWizardPanel(context, false, true, false)
                    };
        } else if (templateName.equals("OutboundGetMessaging")) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new FTPSettingsOneWayStepWizardPanel(context),
                        // one way, last
                        new InboundMessagePollRespStepWizardPanel(context, true, true, true)
                    };
        } else if (templateName.equals("InboundOneWayTransferring")) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new FTPSettingsOneWayStepWizardPanel(context),
                        // is one way, is last
                        new InboundTransferPollReqStepWizardPanel(context, true, true)
                    };
        } else if (templateName.equals("InboundRequestResponseTransferring")) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new FTPSettingsRequestResponseTransferStepWizardPanel(context),
                        // not one way, not last
                        new InboundTransferPollReqStepWizardPanel(context, false, false),
                        // not one way, is last
                        new OutboundTransferPutRespStepWizardPanel(context, false, true)
                    };
        } else if (templateName.equals("OutboundOneWayTransferring")) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new FTPSettingsOneWayStepWizardPanel(context),
                        // is one way, is last
                        new OutboundTransferPutReqStepWizardPanel(context, true, true)
                    };
        } else if (templateName.equals("OutboundRequestResponseTransferring")) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new FTPSettingsRequestResponseTransferStepWizardPanel(context),
                        // is not one way, is not last
                        new OutboundTransferPutReqStepWizardPanel(context, false, false),
                        // is not one way, is last
                        new InboundTransferPollRespStepWizardPanel(context, false, true)
                    };
        } else if (templateName.equals("OutboundReceiveTransferring")) {
            panels = new WSDLWizardDescriptorPanel[]{
                        new FTPSettingsOneWayStepWizardPanel(context),
                        // is one way, is last, is solicit
                        new InboundTransferPollRespStepWizardPanel(context, true, true, true)
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
        // need to extract properties from the current panel
        currentStepIndex++;
    }

    @Override
    public void previousPanel() {
        // need to extract properties from the current panel
        currentStepIndex--;
    }

    @Override
    public boolean commit() {
        boolean status = true;
//        WSDLWizardContext context = getWSDLWizardContext();
//        WSDLModel wsdlModel = context.getWSDLModel();
        
        for (WSDLWizardDescriptorPanel panel : panels) {
            if (panel instanceof InboundMessagePollReqStepWizardPanel) {                                
                status = ((InboundMessagePollReqStepWizardPanel)panel).commit();                 
            } else if (panel instanceof InboundMessagePollRespStepWizardPanel) {
                status = ((InboundMessagePollRespStepWizardPanel)panel).commit();
            } else if (panel instanceof OutboundMessagePutReqStepWizardPanel) {
                status = ((OutboundMessagePutReqStepWizardPanel)panel).commit();
            } else if (panel instanceof OutboundMessagePutRespStepWizardPanel) {
                status = ((OutboundMessagePutRespStepWizardPanel)panel).commit();
            } else if (panel instanceof InboundTransferPollReqStepWizardPanel) {
                status = ((InboundTransferPollReqStepWizardPanel)panel).commit();
            } else if (panel instanceof InboundTransferPollRespStepWizardPanel) {
                status = ((InboundTransferPollRespStepWizardPanel)panel).commit();
            } else if (panel instanceof OutboundTransferPutReqStepWizardPanel) {
                status = ((OutboundTransferPutReqStepWizardPanel)panel).commit();
            } else if (panel instanceof OutboundTransferPutRespStepWizardPanel) {
                status = ((OutboundTransferPutRespStepWizardPanel)panel).commit();
            } else if (panel instanceof FTPSettingsOneWayStepWizardPanel ) {
                status = ((FTPSettingsOneWayStepWizardPanel)panel).commit();
            } else if (panel instanceof FTPSettingsRequestResponseMessageStepWizardPanel) {
                status = ((FTPSettingsRequestResponseMessageStepWizardPanel)panel).commit();
            } else if (panel instanceof FTPSettingsRequestResponseTransferStepWizardPanel) {
                status = ((FTPSettingsRequestResponseTransferStepWizardPanel)panel).commit();
            }
            
            if (!status) {
                return status;
            }
        }
        return status;
    }
    
    public WSDLWizardDescriptorPanel[] getDescriptorPanels() {
        return panels;
    }
}
