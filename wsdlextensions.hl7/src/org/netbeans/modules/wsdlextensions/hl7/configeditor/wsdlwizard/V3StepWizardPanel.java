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

package org.netbeans.modules.wsdlextensions.hl7.configeditor.wsdlwizard;

import java.awt.Component;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.V3EditorForm;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.V3EditorModel;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.panels.V3Panel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Vishnuvardhan P.R
 */
public class V3StepWizardPanel extends WSDLWizardDescriptorPanel {
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
    //InboundPersistenceController mController = null;
    
    /**
     * Port type associated with this step
     */
    PortType mPortType = null;

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private V3Panel mPanel;    

    public V3StepWizardPanel(WSDLWizardContext context){
        super(context);
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(GeneralStepWizardPanel.class,
                "V3StepWizardPanel.StepLabel");
    }

    @Override
    public Component getComponent() {
        if (mPanel == null) {
            //mPanel = new GeneralPanel(mQName, mComponent);
            V3EditorModel v3EditorModel = new V3EditorModel();
            mPanel = new V3EditorForm(v3EditorModel);
        } 
        //mPanel.setProject(mProject);
        /*if (mController == null) {
            mController = new GeneralPanelPersistenceController(mComponent, mPanel);
        }*/
                
        return mPanel;


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

    @Override
    public void readSettings(Object settings) {
    //    throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void storeSettings(Object settings) {
      //  throw new UnsupportedOperationException("Not supported yet.");
    }
    
            /**
     * Commit model
     * @return
     */
    public boolean commit() {
        boolean ok = true;
        /*if (mController != null) {
            ok = mController.commit();
            if ((ok) && (mTemplateWizard.
                        getProperty("GENERATE_PARTNER_LINKTYPE") != null)) {
                boolean genPortLinkType = ((Boolean) mTemplateWizard.
                        getProperty("GENERATE_PARTNER_LINKTYPE")).booleanValue();
                if (genPortLinkType) {
                    BindingComponentUtils.createPartnerLinkType(mPortType, 
                            mComponent.getModel(), false);
                }                                
            }
        }*/
        return ok;
    }


}
