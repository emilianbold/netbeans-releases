/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xslt.project.wizard.element;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xml.reference.ReferenceFile;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.ui.editors.ServiceParamChooser;
import org.netbeans.modules.xslt.tmap.util.Util;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vitaly Bychkov
 * @author Vladimir Yaroslavskiy
 * @version 2007.01.30
 */
class PanelWebService extends WizardSettingsPanel {

    public static final String CHOOSER_LABEL2 = "LBL_Chooser2";
    public static final String SERVICE_LABEL2 = "LBL_Xslt_Service_Operation2";

    PanelWebService(Project project, Panel stepPanel) {
        super(project);
        myServiceLabelString = SERVICE_LABEL2;
        myChooserLabelString = CHOOSER_LABEL2;
        myWsdlPanel = stepPanel;
        myServiceModel = new XsltServiceConfigurationModel(project, false);
    }

    @Override
    protected String getError() {
        if (getWsdlOperation() == null) {
            return i18n("ERR_CallWeb_ServiceOperation_Is_Required"); // NOI18N
        }
        return null;
    }

    @Override
    protected Object getResult() {
        return getWsdlOperation();
    }

    @Override
    protected void createPanel(JPanel mainPanel, GridBagConstraints cc) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;

        // label
        c.gridy++;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(TINY_SIZE, 0, TINY_SIZE, 0);

        myServiceLabel = createLabel(i18n(myServiceLabelString)); // NOI18N
        a11y(myServiceLabel, "ACSN_LBL_Xslt_Service_Operation", "ACSD_LBL_Xslt_Service_Operation");
        panel.add(myServiceLabel, c);

        // textField
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(TINY_SIZE, LARGE_SIZE, TINY_SIZE, 0);
        JTextField wsdlOperationTextField = getWsdlOperationTextField();
        wsdlOperationTextField.setEditable(false);
        myServiceLabel.setLabelFor(wsdlOperationTextField);
        panel.add(wsdlOperationTextField, c);

        // [choose]
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(TINY_SIZE, LARGE_SIZE, TINY_SIZE, 0);
        myChooser = createChooseWsdlOperationButton(myWsdlOperationTextField,
                myChooserLabelString);
        panel.add(myChooser, c);

        setWsdlOperation(myServiceModel.getPartnerWsdlOperation());

        DocumentListener docListener = new DocumentListenerImpl(this, myWsdlPanel);
        wsdlOperationTextField.getDocument().addDocumentListener(docListener);

        mainPanel.add(panel, cc);
        update();
    }

    @Override
    protected void setEnabled(boolean enabled) {
        myWsdlOperationTextField.setEnabled(enabled);
        myChooser.setEnabled(enabled);
        myServiceLabel.setEnabled(enabled);
    }

    //-----------------------------------
    protected final JButton createChooseWsdlOperationButton(JTextField fileTextField) {
        return createBrowseButton(fileTextField, CHOOSER_LABEL2);
    }

    protected final JButton createChooseWsdlOperationButton(final JTextField fileTextField, String labelKey) {

        JButton chooseButton = createButton(
                new ButtonAction(
                i18n(labelKey), // NOI18N
                i18n("TLT_ChooseWsdlOperation")) { // NOI18N


                    public void actionPerformed(ActionEvent event) {

                        TMapModel tMapModel = Util.getTMapModel(Util.getTMapFo(getProject()));
                        if (tMapModel == null) {
                            LOGGER.log(Level.WARNING, "ERR_TMapModelIsNull"); // NOI18N
                            return;
                        }
                        ServiceParamChooser chooser = new ServiceParamChooser(
                                tMapModel, Operation.class);
                        chooser.setSelectedValue(getWsdlOperation());
                        if (!ServiceParamChooser.showDlg(chooser)) {
                            return; // The cancel is pressed
                        }
                        WSDLComponent selOperation = chooser.getSelectedValue();

                        if (!(selOperation instanceof Operation)) {
                            LOGGER.log(Level.WARNING, i18n("MSG_WrongElementWereSelected", selOperation.getClass().toString(), Operation.class.toString()));
                            return;
                        }

                        // generate user text info
                        setWsdlOperation(fileTextField, (Operation) selOperation);
                    }
                });
        return chooseButton;
    }

    protected JTextField getWsdlOperationTextField() {
        if (myWsdlOperationTextField == null) {
            myWsdlOperationTextField = new JTextField();
        }
        return myWsdlOperationTextField;
    }

    protected Operation getWsdlOperation() {
        return myWsdlOperation;
    }

    protected Panel getStepPanel() {
        return myWsdlPanel;
    }
    
    protected void setWsdlOperation(Operation wsdlOperation) {
        setWsdlOperation(getWsdlOperationTextField(), wsdlOperation);
    }

    protected void setWsdlOperation(JTextField fileTextField, Operation wsdlOperation) {
        assert fileTextField != null;
        if (wsdlOperation == null) {
            return;
        }
        WSDLModel wsdlModel = wsdlOperation.getModel();
        ReferenceFile prjWSDL = null;
        FileObject wsdlFo = wsdlModel == null ? null : SoaUtil.getFileObjectByModel(wsdlModel);
        if (wsdlFo != null) {
            prjWSDL = new ReferenceFile(wsdlFo, getProject());
        }

        myWsdlOperation = wsdlOperation;
        StringBuffer wsdlOpTfValue = new StringBuffer();
        if (prjWSDL != null) {
            wsdlOpTfValue.append(prjWSDL.getName());
        }

        PortType pt = (PortType) myWsdlOperation.getParent();
        assert pt != null;
        String ptName = pt.getName();
        if (ptName != null) {
            wsdlOpTfValue.append(SERVICE_PARAM_SEPARATOR).append(ptName);
        }

        String opName = myWsdlOperation.getName();
        if (opName != null) {
            wsdlOpTfValue.append(SERVICE_PARAM_SEPARATOR).append(opName);
            fileTextField.setText(opName);
        }

        fileTextField.setToolTipText(wsdlOpTfValue.toString());
    }
    private static final Logger LOGGER = Logger.getLogger(PanelWebService.class.getName());
    private static final String SERVICE_PARAM_SEPARATOR = "/"; // NOI18N

    private String myServiceLabelString;
    private String myChooserLabelString;
    private JButton myChooser;
    private JLabel myServiceLabel;
    private JTextField myWsdlOperationTextField;
    private Operation myWsdlOperation;
    private Panel myWsdlPanel;
    private XsltServiceConfigurationModel myServiceModel;
}
