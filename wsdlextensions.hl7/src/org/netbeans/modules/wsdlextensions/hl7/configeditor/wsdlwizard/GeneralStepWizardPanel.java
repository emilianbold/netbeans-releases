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

import java.awt.Component;
import java.util.List;
import javax.xml.namespace.QName;

import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.hl7.HL7Binding;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.GeneralEditorForm;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.GeneralEditorModel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
//import org.netbeans.api.project.Project;
import org.openide.util.NbBundle;
//import org.openide.loaders.TemplateWizard;

/**
 *
 * @author Vishnuvardhan P.R
 */
public class GeneralStepWizardPanel extends WSDLWizardDescriptorPanel {
    /**
     * Model to represent this wizard panel with
     */
    WSDLComponent mComponent = null;
    
    /**
     * Project associated with this wsdl model
     */
    Project mProject = null;
    
    /**
     * Template wizard
     */
    TemplateWizard mTemplateWizard = null;
    
    QName mQName = null;
    
    /**
     * Controller associated with this wizard panel
     */
    GeneralStepPersistenceController mController = null;
    
    /**
     * Port type associated with this step
     */
    PortType mPortType = null;

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GeneralEditorForm mPanel;
    private String templateConst;
    
    public GeneralStepWizardPanel(WSDLWizardContext context, String templateConst){
        super(context);
        this.templateConst = templateConst;
    }
    
    
    @Override
    public String getName() {
        return NbBundle.getMessage(GeneralStepWizardPanel.class,
                "GeneralStepWizardPanel.StepLabel");
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (mPanel == null) {
            //mPanel = new GeneralPanel(mQName, mComponent);
            GeneralEditorModel generalEditorModel = new GeneralEditorModel();
            mPanel = new GeneralEditorForm(generalEditorModel,this.templateConst);
        } 
        //mPanel.setProject(mProject);
        if (mController == null) {
             WSDLWizardContext myContext = getWSDLWizardContext();
            if ((myContext != null) && (myContext.getWSDLModel() != null)) {
                WSDLModel wsdlModel = myContext.getWSDLModel();
                Definitions defs = wsdlModel.getDefinitions();
                mComponent = getHL7Binding(defs);
               // mPortType = FileUtilities.getPortType((FileAddress) mComponent);
            }        
            mController = new GeneralStepPersistenceController(mComponent, mPanel);
        }
                
        return mPanel;
    }
   private HL7Binding getHL7Binding(Definitions defs) {
        HL7Binding hl7Binding = null;
        if (defs != null) {
            if(defs.getBindings().size() > 0){
                Binding binding = defs.getBindings().iterator().next();
                List<HL7Binding> hl7Bindings = binding.getExtensibilityElements(HL7Binding.class);
                if(hl7Bindings.size() > 0){
                    return hl7Bindings.iterator().next();
                }
            }

        }
        return hl7Binding;
    }    

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.     
    // when prev panel's 'Next' is called, our readSettings is called
    @Override
    public void readSettings(Object settings) {
        /*TemplateWizard templateWizard = (TemplateWizard) settings;        
        mTemplateWizard = templateWizard;
        mProject = Templates.getProject(mTemplateWizard);
        if (mPanel != null) {
            mPanel.setProject(mProject);
        }       
        WSDLWizardContext myContext = getWSDLWizardContext();
        if ((myContext != null) && (myContext.getWSDLModel() != null)) {
            WSDLModel wsdlModel = myContext.getWSDLModel();
            Definitions defs = wsdlModel.getDefinitions();
            mComponent = getHL7Binding(defs);
           // mPortType = FileUtilities.getPortType((FileAddress) mComponent);
        }        */

    }

    @Override
    public void storeSettings(Object settings) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    /**
     * Return the PortType associated with this panel with the assumption
     * of one port, one binding, one portType
     * @return PortType
     */
    public PortType getPortType() {
        return mPortType;
    }
    
        /**
     * Commit model
     * @return
     */
    public boolean commit() {
        boolean ok = true;
        if (mController != null) {
            ok = mController.commit();
            /*if ((ok) && (mTemplateWizard.
                        getProperty("GENERATE_PARTNER_LINKTYPE") != null)) {
                boolean genPortLinkType = ((Boolean) mTemplateWizard.
                        getProperty("GENERATE_PARTNER_LINKTYPE")).booleanValue();
                if (genPortLinkType) {
                    BindingComponentUtils.createPartnerLinkType(mPortType, 
                            mComponent.getModel(), false);
                }                                
            }*/
        }
        return ok;
    }


}
