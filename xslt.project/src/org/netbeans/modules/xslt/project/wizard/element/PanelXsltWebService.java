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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.tmap.model.spi.NameGenerator;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.WizardDescriptor;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vitaly Bychkov
 * @author Vladimir Yaroslavskiy
 * @version 2007.01.30
 */
final class PanelXsltWebService extends PanelWebService {

    public static final String CHOOSER_LABEL = "LBL_Chooser";
    public static final String SERVICE_LABEL = "LBL_Xslt_Service_Operation";

    PanelXsltWebService(Project project, Panel stepPanel) {
        super(project, stepPanel);
        myServiceLabelString = SERVICE_LABEL;
        myChooserLabelString = CHOOSER_LABEL;
        myServiceModel = new XsltServiceConfigurationModel(project);
    }

    @Override
    protected String getError() {
        if (!isFinishEditing.get()) {
            return null;
        }
        
        String serviceNameCheck = checkServiceName(getServiceName());
        if (serviceNameCheck != null) {
            return serviceNameCheck;
        }

        if (getWsdlOperation() == null) {
            return i18n("ERR_Web_ServiceOperation_Is_Required"); // NOI18N
        }
        return null;
    }

    private String checkServiceName(String serviceName) {
        if (serviceName == null) {
            return i18n("ERR_XsltServiceName_Is_Required"); // NOI18N
        }

        if (!(Util.isValidNCName(serviceName))) {
            return i18n("ERR_WrongXsltServiceName", serviceName); // NOI18N
        }

        TMapModel model = Util.getTMapModel(getProject());
        if (model != null && !isExistedService 
                && !(NameGenerator.isUniqueName(model.getTransformMap(), Service.class, serviceName)))
        {
            return i18n("ERR_XsltServiceName_IsNotUnique", serviceName); // NOI18N
        }

        org.netbeans.modules.xslt.tmap.model.api.Operation definedOp
                = getDefinedXsltService(model);
        if (isExistedService && definedOp != null) {
            Reference<Operation> definedOpRef = definedOp.getOperation();
            String definedOpName = definedOpRef != null ? definedOpRef.getRefString() : null;
            return i18n("ERR_XsltServiceAlredyDefined", serviceName, definedOpName);
        }

        return null;
    }

    private org.netbeans.modules.xslt.tmap.model.api.Operation getDefinedXsltService(
            TMapModel model)
    {
        org.netbeans.modules.xslt.tmap.model.api.Operation op
                = getOperation(model, getWsdlOperation());
        boolean isDefined = false;
        if (op !=null) {
            //probably documentation tMap component will be added
            // but we are interesting just in invokes and transforms
            List<Transform> tChildren = op.getTransforms();
            isDefined = tChildren != null && tChildren.size() > 0;

            if (!isDefined) {
                List<Invoke> iChildren = op.getInvokes();
                isDefined = iChildren != null && iChildren.size() > 0;
            }
        }

        return isDefined ? op : null;
    }

    private void updateServiceNameField() {
        TMapModel model = Util.getTMapModel(getProject());
        final Service existedService = getService(model, getWsdlOperation());
        if (existedService != null) {
            isExistedService = true;
            myServiceNameField.setEditable(false);
            myServiceNameField.setText(existedService.getName());
            // TODO add information message
        } else {
            isExistedService = false;
            myServiceNameField.setEditable(true);
            myServiceNameField.setText(myServiceModel.getServiceName());
        }
    }

    private org.netbeans.modules.xslt.tmap.model.api.Operation getOperation(
            TMapModel model, Operation wsdlOperation)
    {
        Service service = getService(model, wsdlOperation);
        List<org.netbeans.modules.xslt.tmap.model.api.Operation> ops = null;
        if (service != null && wsdlOperation != null) {
            ops = service.getOperations();
        }

        if (ops != null && ops.size() > 0) {
            for (org.netbeans.modules.xslt.tmap.model.api.Operation op : ops) {
                Reference<Operation> opRef = op.getOperation();
                if (opRef != null && wsdlOperation.equals(opRef.get())) {
                    return op;
                }
            }
        }
        return null;
    }

    private Service getService(TMapModel model, Operation wsdlOperation) {
        if (model == null || wsdlOperation == null) {
            return null;
        }

        WSDLComponent wsComp = wsdlOperation.getParent();
        PortType wsdlPt = null;
        if (wsComp instanceof PortType) {
            wsdlPt = (PortType) wsComp;
        }
        if (wsdlPt == null) {
            return null;
        }
        TransformMap tMap = model.getTransformMap();

        List<Service> services = null;
        if (tMap != null) {
            services = tMap.getServices();
        }

        if (services != null) {
            for (Service service : services) {
                WSDLReference<PortType> wsdlPtRef = service.getPortType();
                if (wsdlPt.equals(wsdlPtRef.get())) {
                    return service;
                }
            }
        }

        return null;
    }

    @Override
    protected void createPanel(JPanel mainPanel, GridBagConstraints cc) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;

        // service name label
        c.gridy++;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(TINY_SIZE, 0, TINY_SIZE, 0);
        myServiceNameLabel = createLabel(i18n(myServiceNameLabelString)); // NOI18N
        a11y(myServiceNameLabel, "ACSN_LBL_Xslt_Service_Name", "ACSD_LBL_Xslt_Service_Name");
        panel.add(myServiceNameLabel, c);

        myServiceNameField = new JTextField();
        c.weightx = 1.0;
        c.gridwidth = 2;
        c.insets = new Insets(TINY_SIZE, LARGE_SIZE, TINY_SIZE, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        myServiceNameField.setText(myServiceModel.getServiceName());
        myServiceNameLabel.setLabelFor(myServiceNameField);
        panel.add(myServiceNameField, c);

        // label
        c.gridy++;
        c.weightx = 0.0;
        c.gridwidth = 1;
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
        myChooser = createChooseWsdlOperationButton(wsdlOperationTextField,
                myChooserLabelString);
        panel.add(myChooser, c);

        setWsdlOperation(myServiceModel.getWsdlOperation());

        DocumentListener docListener = new DocumentListenerImpl(this, getStepPanel());
        myServiceNameField.getDocument().addDocumentListener(docListener);
        wsdlOperationTextField.getDocument().addDocumentListener(docListener);

        mainPanel.add(panel, cc);
        update();
    }

    @Override
    public void storeSettings(WizardDescriptor object) {
        super.storeSettings(object);
        object.putProperty(XSLT_SERVICE_NAME, getServiceName());
    }

    @Override
    protected void setEnabled(boolean enabled) {
        getWsdlOperationTextField().setEnabled(enabled);
        myChooser.setEnabled(enabled);
        myServiceLabel.setEnabled(enabled);
    }

    @Override
    protected void setWsdlOperation(JTextField fileTextField, Operation wsdlOperation) {
        isFinishEditing.set(false);
        super.setWsdlOperation(fileTextField, wsdlOperation);
        updateServiceNameField();
        isFinishEditing.set(true);
    }

    //-----------------------------------
    private String getServiceName() {
        return myServiceNameField == null ? null : myServiceNameField.getText();
    }

    private static final Logger LOGGER = Logger.getLogger(PanelXsltWebService.class.getName());
    private boolean isExistedService = false;
    private AtomicBoolean isFinishEditing = new AtomicBoolean(true);
    private String myServiceLabelString;
    private String myChooserLabelString;
    private JButton myChooser;
    private JLabel myServiceLabel;
    private JTextField myServiceNameField;
    private JLabel myServiceNameLabel;
    private String myServiceNameLabelString = "LBL_Xslt_Service_Name";
    private XsltServiceConfigurationModel myServiceModel;
}
