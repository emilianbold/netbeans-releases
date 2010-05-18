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

package org.netbeans.modules.wsdlextensions.hl7.configeditor;

import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Controller that manages the BindingsConfigurationEditorView and the data
 * model.
 *
 * @author Vishnuvardhan P.R
 */
public class HL7BindingConfigurationsEditor 
        implements ExtensibilityElementConfigurationEditorComponent{
    
    private final Form form;
    private final HL7BindingsConfigurationEditorForm.Model viewModel;
    private final WsdlConfigModelAdapter wsdlModelAdapter;    
    
    private static final Logger logger = Logger.getLogger(
            HL7BindingConfigurationsEditor.class.getName());

    
    public HL7BindingConfigurationsEditor(WSDLModel wsdlModel){
        if (wsdlModel == null) {
            throw new NullPointerException("wsdlModel");
        }
        viewModel = new HL7BindingsConfigurationEditorModel();
        wsdlModelAdapter = new WsdlConfigModelAdapter(wsdlModel);
        try {
            syncToFrom(viewModel, wsdlModelAdapter);
        } catch (ModelModificationException e) {
            throw new RuntimeException(e);
        }
        form = new HL7BindingsConfigurationEditorForm(viewModel);
        
    }

    /**
     * Return a editor panel. In general, its better to cache this in
     * implementation, till commit/rollback is called. So that user provided
     * values will be saved in the panel.
     *
     * @return editor view
     */
    public JPanel getEditorPanel() {
        return (JPanel) form;
    }

    /**
     * Return title for the dialog.
     *
     * @return String title
     */
    public String getTitle() {
        return NbBundle.getMessage(HL7BindingConfigurationsEditor.class,
                "HL7BindingConfigurationsEditor.CONFIGURE_TITLE"); //NOI18N

    }

    /**
     * Return the helpctx to be shown in dialog/wizards.
     *
     * @return HelpCtx.
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    /**
     * Generally is not needed to be used. Implement if you have special cases.
     * listener on OK/Cancel buttons.
     *
     * @return ActionListener
     */
    public ActionListener getActionListener() {
        return null;
    }
    
    /**
     * Commit all values from the panel and commit it to the wsdl model. Return
     * true, if successfully committed, otherwise false.
     *
     * @return boolean
     */
    public boolean commit() {
        boolean success = true;
        form.commit();
        try {
            syncToFrom(wsdlModelAdapter, form.getModel());
        } catch (ModelModificationException e) {
            logger.log(Level.SEVERE, "Configuration failed to save to WSDL document", e);
            success = false;
        }
        return success;
    }
    
    /**
     * Cleanup panel, discard values.
     *
     * @return boolean
     */
    public boolean rollback() {
        // Note: Different from reparse semantics.
        // Here, we tell the form to reload its presentation from its own model
        // (The Form contract requires data held by visual widgets to be
        // effectively distinct from what is in the Form's model.)
        // The underlying WSDL model may have changed, yes, but that's not
        // my problem here.  That's reparse business.
        form.refresh();
        return true;
    }

    /**
     * Do validation, and return true if valid, otherwise false.
     *
     * @return boolean
     */
    public boolean isValid() {
        return requiredInformationExists();
    }
    
    public void reparse(WSDLComponent component) {
        // Note: Different from rollback semantics.
        // In rollback(), we discard changes to the form made THRU the form.
        // Here, we want to reload ALL changes regardless of source;
        // essentially, repopulate the form's data model.
        try {
            wsdlModelAdapter.focus(component);
            syncToFrom(form.getModel(), wsdlModelAdapter);
            form.refresh();
        } catch (ModelModificationException e) {
            logger.log(Level.SEVERE, "Failed to reload configuration from WSDL document", e);
        }
    }

    private boolean requiredInformationExists() {
        boolean success = true ;
        /*success = !Utils.safeString(viewModel.getQueueManager()).equals("");
        success &= !Utils.safeString(viewModel.getQueue()).equals("");
        success &= !Utils.safeString(viewModel.getPolling()).equals(""); */
        return success;
    }
    
    private void syncToFrom(Form.FormModel destModel, Form.FormModel srcModel)
        throws ModelModificationException {
        if (!(destModel instanceof HL7BindingsConfigurationEditorForm.Model)) {
            return;
        }
        if (!(srcModel instanceof HL7BindingsConfigurationEditorForm.Model)) {
            return;
        }
        HL7BindingsConfigurationEditorForm.Model dest =
                (HL7BindingsConfigurationEditorForm.Model) destModel;
        HL7BindingsConfigurationEditorForm.Model src =
                (HL7BindingsConfigurationEditorForm.Model) srcModel;     
        
        dest.setAcknowledgementMode(src.getAcknowledgementMode());
        dest.setEncodingStyle(src.getEncodingStyle());
        dest.setEndBlockCharacter(src.getEndBlockCharacter());
        dest.setEndDataCharacter(src.getEndDataCharacter());
        dest.setHLLPChecksumEnabled(src.isHLLPChecksumEnabled());
        dest.setLLPType(src.getLLPType());
        dest.setLocation(src.getLocation());
        dest.setProcessingId(src.getProcessingId());
        dest.setSFTEnabled(src.isSFTEnabled());
        dest.setSequenceNoEnabled(src.isSequenceNoEnabled());
        dest.setSoftwareBinaryId(src.getSoftwareBinaryId());
        dest.setSoftwareCertifiedVersionOrReleaseNo(src.getSoftwareCertifiedVersionOrReleaseNo());
        dest.setSoftwareProductInformation(src.getSoftwareProductInformation());
        dest.setSoftwareProductName(src.getSoftwareProductName());
        dest.setSoftwareVendorOrganization(src.getSoftwareVendorOrganization());
        dest.setStartBlockCharacter(src.getStartBlockCharacter());
        dest.setTransportProtocol(src.getTransportProtocol());
        dest.setUse(src.getUse());
        dest.setValidateMSH(src.isValidateMSH());
        dest.setVersionId(src.getVersionId());
        
    }


}
