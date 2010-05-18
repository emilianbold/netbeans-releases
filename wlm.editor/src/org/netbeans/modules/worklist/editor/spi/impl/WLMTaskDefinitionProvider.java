/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.spi.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMModelFactory;
import org.netbeans.modules.worklist.editor.spi.TaskDefinitionProvider;
import org.netbeans.modules.worklist.util.Utility;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mbhasin
 */
public class WLMTaskDefinitionProvider implements TaskDefinitionProvider {

    static String DEFAULT_TARGET_NAMESPACE = "urn:WS/wsdl"; //NOI18N;
    static String targetNamespace = "SomeNamespace"; //NOI18N;
    static String encoding = "UTF-8"; //NOI18N;
    static String defaultWSDLFileName = "NewWorflowWSDL"; //NOI18N;
    static String defaultWFDefinitionFileName = "NewWorflowTaskDefination"; //NOI18N;
    static String definitionName = "NewWorflowTaskDefination"; //NOI18N;
    static String fileTemplateUrl = "/org/netbeans/modules/worklist/editor/WorklistTemplate.wf"; //NOI18N;
    static String wfDefFileSuffix = ".wf"; //NOI18N;

    public WLMModel createWLMTaskDefinitionFile(String taskName, String newWLMProjectLocation) {

        WLMModel wlmModel = getWLMModelForTempleteWFDefinition();

        File srcFolder = new File(newWLMProjectLocation);
        FileObject fileObj = FileUtil.toFileObject(srcFolder);
        FileObject fo = null;

        try {
            fo = fileObj.createData(taskName + "TaskDefinition", "wf");
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }

        if (fo != null) {
            Document doc = wlmModel.getBaseDocument();
            Utility.writeOutputFile(doc, fo, encoding);
        }
        // construct the model again using the actual wf file created inside 
        // the worklist project
        wlmModel = getWLMModel(fo);
        
        return wlmModel;
    }

    public void configureWFTask(WLMModel wlmModel, TTask task) {
        wlmModel.startTransaction();
        try {
            wlmModel.setTask(task);
        } finally {
            wlmModel.endTransaction();
        }
    }

    private WLMModel getWLMModelForTempleteWFDefinition() {
        WLMModel wlmModel = null;
        try {
            // Create a temporary file for storing our settings.
            File templeteWFDefinitionFile = getWFDefFileFromTemplate();
            templeteWFDefinitionFile.deleteOnExit();
            FileObject fileObj = FileUtil.toFileObject(templeteWFDefinitionFile);

            wlmModel = getWLMModel(fileObj);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        return wlmModel;
    }

    public void createWSDLImport(WLMModel wlmModel, WSDLModel wsdlModel) {

        Collection<PortType> portTypes = wsdlModel.getDefinitions().getPortTypes();
        PortType portType = (PortType) portTypes.iterator().next();
        Operation op = null;
        if (portType != null) {
            Collection<Operation> ops = portType.getOperations();
            op = (Operation) ops.iterator().next();
        }

        String wsdlCatalogId = getCatalogID(wlmModel, wsdlModel);

        TTask task = wlmModel.getTask();

        wlmModel.startTransaction();
        try {
            task.setOperation(task.createOperationReference(op), wsdlCatalogId);
        } finally {
            wlmModel.endTransaction();
        }

    }

    private WLMModel getWLMModel(FileObject fileObject) {
        ModelSource wlmModelSource = Utilities.getModelSource(fileObject, true);
        WLMModel wlmModel = null;
        if (wlmModelSource != null) {
            wlmModel = WLMModelFactory.getDefault().getModel(wlmModelSource);
        }
        return wlmModel;
    }
    
    
    private String getCatalogID(WLMModel wlmModel, WSDLModel wsdlModel) {
        FileObject wfFileObj = SoaUtil.getFileObjectByModel(wlmModel);
        FileObject wsdlFileObj = SoaUtil.getFileObjectByModel(wsdlModel);
        String location = ReferenceUtil.getLocation(wfFileObj, wsdlFileObj);
        return location;
    }

    private static File getWFDefFileFromTemplate() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        String content = Utility.readFileContent(fileTemplateUrl);
        return Utility.getFile(content, encoding, definitionName, wfDefFileSuffix);
    }
}
