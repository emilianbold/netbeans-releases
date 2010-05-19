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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.sap.wizard;

import com.sap.conn.jco.JCoException;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.wsdlextensions.sap.model.BAPIMethod;
import org.netbeans.modules.wsdlextensions.sap.model.BAPIObject;
import org.netbeans.modules.wsdlextensions.sap.model.IDocType;
import org.netbeans.modules.wsdlextensions.sap.model.RFC;
import org.netbeans.modules.wsdlextensions.sap.model.SapConnection;
import org.netbeans.modules.wsdlextensions.sap.util.BORClient;
import org.netbeans.modules.wsdlextensions.sap.util.WSDLGenerator;
import org.netbeans.modules.wsdlextensions.sap.wizard.panels.ObjectSelectionPanel;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.loaders.TemplateWizard;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class ObjectSelectionStepWizardPanel extends WSDLWizardDescriptorPanel {

    /**
     * Template wizard
     */
    private TemplateWizard mTemplateWizard;

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ObjectSelectionPanel mPanel;

    public ObjectSelectionStepWizardPanel(WSDLWizardContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ObjectSelectionStepWizardPanel.class,
                "ObjectSelectionPanel.StepLabel"); // NOI18N
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (mPanel == null) {
            mPanel = new ObjectSelectionPanel();
        }

        return mPanel;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return mPanel.getUserSelection() != null;
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
        mTemplateWizard = (TemplateWizard) settings;
        SapConnection connection = (SapConnection) mTemplateWizard.getProperty("connection");
        ((ObjectSelectionPanel) getComponent()).setConnection(connection);
    }

    @Override
    public void storeSettings(Object settings) {
    }

    /**
     * Commit model
     * @return
     */
    public boolean commit() {

        boolean ret = false;

        //WSDLModel wsdlModel = getWSDLWizardContext().getWSDLModel();

        SapConnection connection = (SapConnection) mTemplateWizard.getProperty("connection");
        try {
            //DataFolder targetFolder = mTemplateWizard.getTargetFolder();
            //FileObject targetFolderFO = targetFolder.getPrimaryFile();
            //String targetName = mTemplateWizard.getTargetName() + ".wsdl";

            BORClient borClient = new BORClient(connection);

            WSDLGenerator wsdlGenerator = new WSDLGenerator(
                    borClient.getRfcClient(),
                    borClient.getRfcRepository(),
                    borClient.getConnection());

            Object selection = mPanel.getUserSelection();

            File tempFile = File.createTempFile("tmpwsdlmodel", ".wsdl");

            if (selection instanceof BAPIMethod) {
                BAPIMethod selectedMethod = (BAPIMethod) selection;
                String functionName = selectedMethod.getFunction();
                System.out.println("selected BAPI: " + functionName);
                //String fileName = functionName + ".wsdl";
                //wsdlFO = targetFolderFO.createData(fileName);
                //wsdlFO = targetFolderFO.getFileObject(targetName);
                wsdlGenerator.genRfcWsdl(functionName, tempFile);
            } else if (selection instanceof RFC) {
                RFC selectedRFC = (RFC) selection;
                String functionName = selectedRFC.getFunctionName();
                System.out.println("selected RFC: " + functionName);
                //String fileName = functionName.substring(functionName.lastIndexOf("/") + 1) + ".wsdl";
                //wsdlFO = targetFolderFO.createData(fileName);
                //wsdlFO = targetFolderFO.getFileObject(targetName);
                wsdlGenerator.genRfcWsdl(functionName, tempFile);
            } else if (selection instanceof BAPIObject) {
                BAPIObject selectedObject = (BAPIObject) selection;
                String boName = selectedObject.getExtName();
                System.out.println("selected BO: " + boName);
                //String fileName = boName + ".wsdl";
                //wsdlFO = targetFolderFO.createData(fileName);
                //wsdlFO = targetFolderFO.getFileObject(targetName);
                wsdlGenerator.genBoWsdl(selectedObject, tempFile);
            } else if (selection instanceof IDocType) {
                IDocType selectedObject = (IDocType) selection;
                String iDocName = selectedObject.getName();
                System.out.println("selected IDoc: " + iDocName);
                //String fileName = iDocName.substring(iDocName.lastIndexOf("/") + 1) + ".wsdl";
                //wsdlFO = targetFolderFO.createData(fileName);
                //wsdlFO = targetFolderFO.getFileObject(targetName);
                wsdlGenerator.genIDocWsdl(iDocName, tempFile);
            }

            FileObject tempFO = FileUtil.toFileObject(tempFile.getCanonicalFile());
            ModelSource source = Utilities.createModelSource(tempFO, true);
            WSDLModel updatedWSDLModel = WSDLModelFactory.getDefault().getModel(source);

            mTemplateWizard.putProperty("TEMP_WSDLMODEL", // WSDLWizardConstants.TEMP_WSDLMODEL
                    updatedWSDLModel);

            ret = true;

        } catch (JCoException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (CatalogModelException ex) {
            Exceptions.printStackTrace(ex);
        }

        return ret;
    }
}
