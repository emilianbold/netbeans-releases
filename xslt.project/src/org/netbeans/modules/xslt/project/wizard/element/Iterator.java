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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.cookies.SaveCookie;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponentFactory;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.tmap.model.impl.VariableReferenceImpl;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.util.ModelUtil;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xslt.project.XsltproConstants;
import org.netbeans.modules.xslt.tmap.model.api.events.VetoException;
import org.netbeans.modules.xslt.tmap.model.spi.NameGenerator;
import org.netbeans.modules.xslt.tmap.util.ImportRegistrationHelper;
import org.openide.util.NbBundle;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.25
 */
public final class Iterator implements TemplateWizard.Iterator {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGGER = Logger.getLogger("xslt.project");

    public static Iterator createXsl() {
        return new Iterator();
    }

    public Set<DataObject> instantiate(TemplateWizard wizard) throws IOException {
        return Collections.singleton(createFile(wizard));
    }

    public void initialize(TemplateWizard wizard) {
        myPanel = new PanelStartup(Templates.getProject(wizard), null);
    }

    public void uninitialize(TemplateWizard wizard) {
        myPanel = null;
    }

    public String name() {
        return i18n(Iterator.class, "LBL_Title"); // NOI18N
    }

    public boolean hasNext() {
        return myPanel.getNext() != null;
    }

    public boolean hasPrevious() {
        return myPanel.getPrevious() != null;
    }

    public void nextPanel() {
        myPanel = myPanel.getNext();
    }

    public void previousPanel() {
        myPanel = myPanel.getPrevious();
    }

    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return myPanel;
    }

    public void addChangeListener(ChangeListener listener) {
    }

    public void removeChangeListener(ChangeListener listener) {
    }

    private DataObject createFile(TemplateWizard wizard) throws IOException {
        FileObject file = null;
        Project project = Templates.getProject(wizard);
        String choice = (String) wizard.getProperty(Panel.CHOICE);

        TransformationUseCasesFactory tUseCaseFactory = TransformationUseCasesFactory.getInstance();
        file = tUseCaseFactory.createUseCase(project, wizard);

        return file == null ? null : DataObject.find(file);
    }

    private static TransformationUseCase getUseCase(TemplateWizard wizard) {
        String choice = wizard == null ? null : (String) wizard.getProperty(Panel.CHOICE);
        return choice == null ? null : TRANSFORMATION_USE_CASE.get(choice);
    }

    private enum TransformationUseCase {

        REQUEST_REPLY,
        FILTER_ONE_WAY,
        FILTER_REQUEST_REPLY;
    }

    private static class TransformationUseCasesFactory {

        private static TransformationUseCasesFactory INSTANCE = new TransformationUseCasesFactory();

        private TransformationUseCasesFactory() {
        }

        public static TransformationUseCasesFactory getInstance() {
            return INSTANCE;
        }

        public FileObject createUseCase(Project project, TemplateWizard wizard) throws IOException {
            assert project != null && wizard != null;

            List<FileObject> createdFos = new ArrayList<FileObject>();
            TransformationUseCase useCase = getUseCase(wizard);
            assert useCase != null;

            FileObject fo = createXslFiles(useCase, project, wizard, createdFos);

            if (fo != null) {
                try {
                    configureTMapModel(useCase, project, wizard);
                } catch (IOException ex) {
                    rollbackCreatedXslFiles(createdFos);
                    fo = null;
                    throw ex;
                }
            }
            return fo;
        }

        private void rollbackCreatedXslFiles(List<FileObject> createdFos) throws IOException {
            assert createdFos != null;
            for (FileObject fo : createdFos) {
                if (fo != null && fo.isValid()) {
                    fo.delete();
                }
            }
        }

        private List<String> getFiles(List<TransformationItem> items) {
            List<String> files = new ArrayList<String>();
            if (items != null) {
                for (TransformationItem item : items) {

                    String xslFile = getFile(item);
                    if (xslFile != null) {
                        files.add(xslFile);
                    }
                }
            }
            return files;
        }

        private String getFile(TransformationItem item) {
            if (item == null) {
                return null;
            }

            String xslFile = item.getXslFilePath();
            if (xslFile == null) {
                return null;
            }

            if (!(xslFile.endsWith(XsltproConstants.DOT + XsltproConstants.XSLT_EXTENSION))) {
                xslFile = xslFile + XsltproConstants.DOT + XsltproConstants.XSLT_EXTENSION;
            }

            return xslFile;
        }

        private FileObject createXslFiles(TransformationUseCase useCase,
                Project project, TemplateWizard wizard,
                List<FileObject> createdFos) throws IOException {
            assert project != null && useCase != null && wizard != null;

//        String file1 = (String) wizard.getProperty(Panel.INPUT_FILE);
//        String file2 = TransformationUseCase.FILTER_REQUEST_REPLY.equals(useCase) 
//                ?  (String) wizard.getProperty(Panel.OUTPUT_FILE) : null;

            List<TransformationItem> inputTransformations = (List<TransformationItem>) wizard.getProperty(Panel.INPUT_TRANSFORMATIONS);
            List<TransformationItem> outputTransformations =
                    TransformationUseCase.FILTER_REQUEST_REPLY.equals(useCase)
                    ? (List<TransformationItem>) wizard.getProperty(Panel.OUTPUT_TRANSFORMATIONS) : null;

            List<String> files = new ArrayList<String>();
            files.addAll(getFiles(inputTransformations));
            files.addAll(getFiles(outputTransformations));

            FileObject file = null;

            for (String filePath : files) {
                file = createXslFile(
                        project, filePath, createdFos);
            }

            return file;
        }

        private FileObject createXslFile(
                Project project,
                String file, List<FileObject> createdFos) throws IOException {
            if (file == null || "".equals(file)) {
                return null;
            }
            file = file.trim();

            int extIndex = file.lastIndexOf(XsltproConstants.XSLT_EXTENSION) - 1;
            if (extIndex <= 0) {
                return null;
            }

            file = file.substring(0, extIndex);

            if ("".equals(file)) {
                return null;
            }

            FileObject dirFo = ReferenceUtil.getSrcFolder(project);

            if (!PanelUtil.isAbsolute(file)) {
                file = ModelUtil.rel2absolut(dirFo, file);
            } 

            if (!PanelUtil.isAbsolute(file)) {
                return null;
            }
            boolean isAllowSlash = false;
            boolean isAllowBackslash = false;
            if (File.separatorChar == '\\') {
                isAllowBackslash = true;
                file = file.replace('/', File.separatorChar);
            } else {
                isAllowSlash = true;
            }
            
            dirFo = PanelUtil.getRoot(file, project);
            if (dirFo == null) {
                return null;
            }
            file = file.substring(dirFo.getPath().length());
            
            StringTokenizer dirTokens = new StringTokenizer(file, File.separator);
            int numDirs = dirTokens.countTokens();
            String[] dirs = new String[numDirs];
            int i = 0;
            while (dirTokens.hasMoreTokens()) {
                dirs[i] = dirTokens.nextToken();
                i++;
            }
            
            boolean isCreatedDir = false;
            if (numDirs > 1) {
                file = dirs[numDirs - 1];
                for (int j = 0; j < numDirs - 1; j++) {
                    FileObject tmpDirFo =
                            dirFo.getFileObject(dirs[j]);
                    if (tmpDirFo == null) {
                        try {
                            dirFo = dirFo.createFolder(dirs[j]);
                        } catch (IOException ex) {
                            rollbackCreatedXslFiles(createdFos);
                            throw ex;
                        }

                        if (dirFo == null) {
                            rollbackCreatedXslFiles(createdFos);
                            break;
                        }
                        // add just parentFo 
                        if (!isCreatedDir) {
                            isCreatedDir = true;
                            createdFos.add(dirFo);
                        }
                    } else {
                        dirFo = tmpDirFo;
                    }
                }
            }
            FileObject xslFo = null;

            if (dirFo != null) {
                xslFo = dirFo.getFileObject(file, XsltproConstants.XSLT_EXTENSION);
                if (xslFo == null) {
                    xslFo = PanelUtil.copyFile(dirFo,
                            TEMPLATES_PATH, XSLT_SERVICE,
                            file, XsltproConstants.XSLT_EXTENSION);
                    if (!isCreatedDir) {
                        createdFos.add(xslFo);
                    }
                    SoaUtil.fixEncoding(DataObject.find(xslFo), dirFo);
                }
            }
            return xslFo;
        }

        private void configureTMapModel(TransformationUseCase useCase,
                Project project, TemplateWizard wizard) throws IOException {
            assert useCase != null && project != null && wizard != null;

            FileObject tMapFo = getTMapFo(project);
            TMapModel tMapModel = tMapFo == null ? null : org.netbeans.modules.xslt.tmap.util.Util.getTMapModel(tMapFo);

            if (tMapModel == null || !TMapModel.State.VALID.equals(tMapModel.getState())) {
                throw new IllegalStateException("" + tMapModel.getState());
            }

            switch (useCase) {
                case REQUEST_REPLY:
                    configureRequestReply(tMapModel, wizard);
                    break;
                case FILTER_ONE_WAY:
                    configureFilterOneWay(tMapModel, wizard);
                    break;
                case FILTER_REQUEST_REPLY:
                    configureFilterRequestReply(tMapModel, wizard);
                    break;
            }

            saveConfiguredModel(tMapFo);
        }

        private void saveConfiguredModel(FileObject tMapFo) throws IOException {
            if (tMapFo == null) {
                return;
            }

            DataObject dObj = DataObject.find(tMapFo);
            if (dObj != null && dObj.isModified()) {

                SaveCookie saveCookie = dObj.getLookup().
                        lookup(SaveCookie.class);
                assert saveCookie != null;
                saveCookie.save();
            }
        }

        private FileObject getTMapFo(Project project) {
            FileObject tMapFo = org.netbeans.modules.xslt.tmap.util.Util.getTMapFo(project);
            if (tMapFo == null) {
                tMapFo = org.netbeans.modules.xslt.tmap.util.Util.createDefaultTransformmap(project);
            }
            return tMapFo;
        }

        public void configureRequestReply(TMapModel tMapModel, TemplateWizard wizard) {
            assert tMapModel != null && wizard != null;
            try {
                tMapModel.startTransaction();
                TMapComponentFactory componentFactory = tMapModel.getFactory();

                org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp =
                        setOperation(tMapModel, wizard, componentFactory);

                Transform rrTransform = null;
                if (tMapOp != null) {
                    List<TransformationItem> tItems = (List<TransformationItem>) wizard.getProperty(Panel.INPUT_TRANSFORMATIONS);
                    if (tItems != null) {
                        for (TransformationItem tItem : tItems) {
                            if (tItem == null) {
                                continue;
                            }
                            String source = getTMapVarRef(tMapOp.getInputVariable(), tItem.getInputPartName());
                            String result = getTMapVarRef(tMapOp.getOutputVariable(), tItem.getOutputPartName());

                            rrTransform = createTransform(tItem.getName(), tMapOp, componentFactory,
                                    getFile(tItem), source, result);
                            if (rrTransform != null) {
                                tMapOp.addTransform(rrTransform);
                            }
                        }
                    }
                }

            } finally {
                tMapModel.endTransaction();
            }
        }

        public void configureFilterOneWay(TMapModel tMapModel,
                TemplateWizard wizard) {
            assert tMapModel != null && wizard != null;
            try {
                tMapModel.startTransaction();
                TMapComponentFactory componentFactory = tMapModel.getFactory();

                org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp =
                        setOperation(tMapModel, wizard, componentFactory);

                NameGenerator varNameGenerator = NameGenerator.getDefault(tMapOp, Variable.class);
                String inputInvokeVar = varNameGenerator.getName(tMapOp, NameGenerator.INPUT_INVOKE_VARIABLE_PREFIX);
                String outputInvokeVar = varNameGenerator.getName(tMapOp, NameGenerator.OUTPUT_INVOKE_VARIABLE_PREFIX);

                List<TransformationItem> tItems = (List<TransformationItem>) wizard.getProperty(Panel.INPUT_TRANSFORMATIONS);
                if (tMapOp != null) {
                    if (tItems != null) {
                        for (TransformationItem tItem : tItems) {
                            if (tItem == null) {
                                continue;
                            }

                            String result = VariableReferenceImpl.getVarRefString(inputInvokeVar, tItem.getOutputPartName());
                            if (result == null) {
                                LOGGGER.log(Level.WARNING, NbBundle.getMessage(
                                        Iterator.class, "MSG_Warning_VariableReferenceNull", "result"));// NOI18N
                            }
                            String source = getTMapVarRef(tMapOp.getInputVariable(), tItem.getInputPartName());
                            if (source == null) {
                                LOGGGER.log(Level.WARNING, NbBundle.getMessage(
                                        Iterator.class, "MSG_Warning_VariableReferenceNull", "result"));// NOI18N
                            }

                            Transform foTransform = createTransform(tItem.getName(), tMapOp, componentFactory,
                                    getFile(tItem), source, result);
                            if (foTransform != null) {
                                if (foTransform != null) {
                                    tMapOp.addTransform(foTransform);
                                    foTransform = getTransform(tMapOp, foTransform.getName());
                                }
                            }
                        }
                    }

                }

                Invoke invoke = null;
                if (tMapOp != null) {
                    invoke = createInvoke(tMapOp, inputInvokeVar, outputInvokeVar, wizard, componentFactory);
                }

                if (invoke != null) {
                    tMapOp.addInvoke(invoke);
                    invoke = getInvoke(tMapOp, invoke.getName());
                    if (invoke != null) {
                        configureInvoke(invoke, wizard);
                    }
                }

//            if (tItems != null) {
//                for (TransformationItem item : tItems) {
//                    String inputPartName = item.getInputPartName();
//                    String outputPartName = item.getOutputPartName();
//                    String tName = item.getName();
//
//                    String result = getTMapVarRef(invoke.getInputVariable(), outputPartName);
//                    if (result == null) {
//                        LOGGGER.log(Level.WARNING, NbBundle.getMessage(
//                                Iterator.class, "MSG_Warning_VariableReferenceNull", "result"));// NOI18N
//                    }
//                    
//                    Transform transform = getTransform(tMapOp, tName);
//                    transform.setResult(result);
//                }
//            }

            } finally {
                tMapModel.endTransaction();
            }
        }

        public void configureFilterRequestReply(TMapModel tMapModel,
                TemplateWizard wizard) {
            assert tMapModel != null && wizard != null;
            try {
                tMapModel.startTransaction();
                TMapComponentFactory componentFactory = tMapModel.getFactory();

                org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp =
                        setOperation(tMapModel, wizard, componentFactory);

                if (tMapOp == null) {
                    return;
                }
                Invoke invoke = null;
                NameGenerator varNameGenerator = NameGenerator.getDefault(tMapOp, Variable.class);
                String inputInvokeVar = varNameGenerator.getName(tMapOp, NameGenerator.INPUT_INVOKE_VARIABLE_PREFIX);
                String outputInvokeVar = varNameGenerator.getName(tMapOp, NameGenerator.OUTPUT_INVOKE_VARIABLE_PREFIX);


                List<TransformationItem> tItems = (List<TransformationItem>) wizard.getProperty(Panel.INPUT_TRANSFORMATIONS);
                if (tMapOp != null) {
                    if (tItems != null) {
                        for (TransformationItem tItem : tItems) {
                            if (tItem == null) {
                                continue;
                            }

                            String result = VariableReferenceImpl.getVarRefString(inputInvokeVar, tItem.getOutputPartName());
                            if (result == null) {
                                LOGGGER.log(Level.WARNING, NbBundle.getMessage(
                                        Iterator.class, "MSG_Warning_VariableReferenceNull", "result"));// NOI18N
                            }
                            String source = getTMapVarRef(tMapOp.getInputVariable(), tItem.getInputPartName());
                            if (source == null) {
                                LOGGGER.log(Level.WARNING, NbBundle.getMessage(
                                        Iterator.class, "MSG_Warning_VariableReferenceNull", "result"));// NOI18N
                            }

                            Transform inTransform = createTransform(tItem.getName(), tMapOp, componentFactory,
                                    getFile(tItem), source, result);
                            if (inTransform != null) {
                                if (inTransform != null) {
                                    tMapOp.addTransform(inTransform);
                                    inTransform = getTransform(tMapOp, inTransform.getName());
                                }
                            }
                        }
                    }

                }

                invoke = createInvoke(tMapOp, inputInvokeVar, outputInvokeVar,
                        wizard, componentFactory);

                if (invoke != null) {
                    tMapOp.addInvoke(invoke);
                    invoke = getInvoke(tMapOp, invoke.getName());
                    if (invoke != null) {
                        configureInvoke(invoke, wizard);
                    }
                }

                List<TransformationItem> tOutItems = (List<TransformationItem>) wizard.getProperty(Panel.OUTPUT_TRANSFORMATIONS);
                if (tMapOp != null) {
                    if (tOutItems != null) {
                        for (TransformationItem tOutItem : tOutItems) {
                            if (tOutItem == null) {
                                continue;
                            }
                            String source = getTMapVarRef(invoke.getOutputVariable(), tOutItem.getInputPartName());
                            if (source == null) {
                                LOGGGER.log(Level.WARNING, NbBundle.getMessage(
                                        Iterator.class, "MSG_Warning_VariableReferenceNull", "source"));// NOI18N
                            }
                            String result = getTMapVarRef(tMapOp.getOutputVariable(), tOutItem.getOutputPartName());
                            if (source == null) {
                                LOGGGER.log(Level.WARNING, NbBundle.getMessage(
                                        Iterator.class, "MSG_Warning_VariableReferenceNull", "source"));// NOI18N
                            }
                            Transform outTransform = createTransform(tOutItem.getName(), tMapOp, componentFactory,
                                    getFile(tOutItem), source, result);
                            if (outTransform != null) {
                                if (outTransform != null) {
                                    tMapOp.addTransform(outTransform);
                                    outTransform = getTransform(tMapOp, outTransform.getName());
                                }
                            }
                        }
                    }

                }

                if (tItems != null) {
                    for (TransformationItem item : tItems) {
                        String inputPartName = item.getInputPartName();
                        String outputPartName = item.getOutputPartName();
                        String tName = item.getName();

                        String result = getTMapVarRef(invoke.getInputVariable(), outputPartName);
                        if (result == null) {
                            LOGGGER.log(Level.WARNING, NbBundle.getMessage(
                                    Iterator.class, "MSG_Warning_VariableReferenceNull", "result"));// NOI18N
                        }

                        Transform transform = getTransform(tMapOp, tName);
                        transform.setResult(result);
                    }
                }
            } finally {
                tMapModel.endTransaction();
            }
        }

        private Service getTMapService(TMapModel model, PortType wizardInPortType) {
            if (model == null || wizardInPortType == null) {
                return null;
            }

            Service service = null;
            TransformMap root = model.getTransformMap();
            if (root == null) {
                return service;
            }

            List<Service> services = root.getServices();
            if (services == null || services.size() < 1) {
                return service;
            }

            for (Service serviceElem : services) {
                WSDLReference<PortType> portTypeRef = serviceElem.getPortType();

                if (portTypeRef != null && wizardInPortType.equals(portTypeRef.get())) {
                    service = serviceElem;
                    break;
                }
            }
            return service;
        }

        private org.netbeans.modules.xslt.tmap.model.api.Operation getTMapOperation(
                Service tMapService, Operation wizardInputOperation) {
            org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp = null;

            if (tMapService == null) {
                return tMapOp;
            }

            List<org.netbeans.modules.xslt.tmap.model.api.Operation> operations =
                    tMapService.getOperations();
            if (operations == null || operations.size() < 1) {
                return tMapOp;
            }

            for (org.netbeans.modules.xslt.tmap.model.api.Operation operationElem : operations) {
                Reference<Operation> opRef = operationElem.getOperation();

                if (opRef != null && wizardInputOperation.equals(opRef.get())) {
                    tMapOp = operationElem;
                    break;
                }
            }
            return tMapOp;
        }

        private org.netbeans.modules.xslt.tmap.model.api.Operation getTMapOperation(
                TMapModel model, PortType wizardInPortType,
                Operation wizardInputOperation) {
            return getTMapOperation(getTMapService(model, wizardInPortType),
                    wizardInputOperation);
        }

        private org.netbeans.modules.xslt.tmap.model.api.Operation setOperation(
                TMapModel tMapModel,
                TemplateWizard wizard,
                TMapComponentFactory componentFactory) {
            assert tMapModel != null && wizard != null && componentFactory != null;

            String serviceName = (String) wizard.getProperty(Panel.XSLT_SERVICE_NAME);

            Operation wizardInputOperation =
                    (Operation) wizard.getProperty(Panel.IMPL_OPERATION);

            if (wizardInputOperation == null) {
                return null;
            }

            assert wizardInputOperation.getParent() instanceof PortType;
            PortType portType = (PortType) wizardInputOperation.getParent();

            Service tMapService = getTMapService(tMapModel, portType);
            if (tMapService == null) {
                tMapService = createTMapService(componentFactory, tMapModel,
                        portType, serviceName);
            }
            if (tMapService == null) {
                return null;
            }
            org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp = null;
            tMapOp = getTMapOperation(tMapService, wizardInputOperation);

            if (tMapOp == null) {
                tMapOp = componentFactory.createOperation();
                tMapOp.setOperation(
                        tMapOp.createWSDLReference(wizardInputOperation, Operation.class));

                tMapService.addOperation(tMapOp);
                NameGenerator varNameGenerator = NameGenerator.getDefault(tMapOp, Variable.class);
                tMapOp.setInputVariableName(varNameGenerator.getName(tMapOp, NameGenerator.INPUT_OPERATION_VARIABLE_PREFIX));

                tMapOp.setOutputVariableName(
                        varNameGenerator.getName(tMapOp, NameGenerator.OUTPUT_OPERATION_VARIABLE_PREFIX));
            }
            return tMapOp;
        }

        private void registerImport(TMapModel tMapModel, WSDLModel wsdlModel) {
            if (tMapModel == null || wsdlModel == null) {
                return;
            }

            // check if wsdl model from another project then it have to be registered as referenced resource
            FileObject wsdlFo = SoaUtil.getFileObjectByModel(wsdlModel);
            FileObject tMapFo = SoaUtil.getFileObjectByModel(tMapModel);
            if (!ReferenceUtil.isSameProject(tMapFo, wsdlFo)) {
                ReferenceUtil.addFile(tMapFo, wsdlFo);
            }
            new ImportRegistrationHelper(tMapModel).addImport(wsdlModel);
        }

        private Service createTMapService(TMapComponentFactory componentFactory,
                TMapModel tMapModel, PortType wizardInPortType, String serviceName) {
            assert componentFactory != null && tMapModel != null && wizardInPortType != null;
            Service tMapService = null;

            TransformMap root = tMapModel.getTransformMap();

            if (root == null) {
                root = componentFactory.createTransformMap();
                tMapModel.addChildComponent(null, root, -1);
            }
            root = tMapModel.getTransformMap();
            if (root == null) {
                return null;
            }

            registerImport(root, wizardInPortType);

            tMapService = componentFactory.createService();
            serviceName = serviceName != null ? serviceName : NameGenerator.getUniqueName(root, Service.class);
            if (serviceName != null) {
                try {
                    tMapService.setName(serviceName);
                } catch (VetoException ex) {
                    LOGGGER.log(Level.WARNING, "couldn't set unique name to service: " + serviceName);
                }
            }

            root.addService(tMapService);
            tMapService = getService(root, serviceName);
            if (tMapService != null) {

                tMapService.setPortType(
                        tMapService.createWSDLReference(wizardInPortType, PortType.class));
            }

            return tMapService;
        }

        private Service getService(TransformMap tMap, String name) {
            if (tMap == null || name == null) {
                return null;
            }

            List<Service> services = tMap.getServices();
            if (services == null) {
                return null;
            }

            for (Service service : services) {
                if (service != null && name.equals(service.getName())) {
                    return service;
                }
            }
            return null;
        }

        private Transform getTransform(org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp,
                String name) {
            if (tMapOp == null || name == null) {
                return null;
            }

            List<Transform> transforms = tMapOp.getTransforms();
            if (transforms == null) {
                return null;
            }

            for (Transform transform : transforms) {
                if (transform != null && name.equals(transform.getName())) {
                    return transform;
                }
            }
            return null;
        }

        private Invoke getInvoke(org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp,
                String name) {
            if (tMapOp == null || name == null) {
                return null;
            }

            List<Invoke> invokes = tMapOp.getInvokes();
            if (invokes == null) {
                return null;
            }

            for (Invoke invoke : invokes) {
                if (invoke != null && name.equals(invoke.getName())) {
                    return invoke;
                }
            }
            return null;
        }

        private void configureInvoke(Invoke invoke, TemplateWizard wizard) {
            assert invoke != null && wizard != null;

            Operation wizardOutputOperation =
                    (Operation) wizard.getProperty(Panel.CALLED_OPERATION);

            if (wizardOutputOperation != null) {
                assert wizardOutputOperation.getParent() instanceof PortType;
                PortType portType =
                        (PortType) wizardOutputOperation.getParent();
                invoke.setPortType(
                        invoke.createWSDLReference(portType, PortType.class));
                invoke.setOperation(
                        invoke.createWSDLReference(wizardOutputOperation, Operation.class));

            }
        }

        private void registerImport(TMapModel tMapModel,
                ReferenceableWSDLComponent wsdlComponent) {
            if (tMapModel == null || wsdlComponent == null) {
                return;
            }

            WSDLModel wsdlModel = wsdlComponent.getModel();
            registerImport(tMapModel, wsdlModel);
        }

        private void registerImport(TransformMap root,
                ReferenceableWSDLComponent wsdlComponent) {
            if (root == null || wsdlComponent == null) {
                return;
            }

            TMapModel tMapModel = root.getModel();
            registerImport(tMapModel, wsdlComponent);
        }

//    private Transform createTransform(String name, org.netbeans.modules.xslt.tmap.model.api.Operation parent,
//            TMapComponentFactory componentFactory, 
//            String inputFileStr, Variable source, Variable result) 
//    {
//        if (source == null || result == null) {
//            return null;
//        }
//
//        Transform transform = componentFactory.createTransform();
//        if (inputFileStr != null && !"".equals(inputFileStr)) {
//            transform.setFile(inputFileStr);
//        }
//        String sourcePartName = getFirstPartName(source);
//        transform.setSource(getTMapVarRef(source, sourcePartName));
//
//        String resultPartName = getFirstPartName(result);
//        transform.setResult(getTMapVarRef(result, resultPartName));
//        
//        name = name == null ? NameGenerator.getUniqueName(parent, Transform.class) : name;
//        if (name != null) {
//            try {
//                transform.setName(name);
//            } catch (VetoException ex) {
//                LOGGGER.log(Level.WARNING, "couldn't set unique name to transform: "+name);
//            }
//        }
//        
//        return transform;
//    }
//
        private Transform createTransform(String name, org.netbeans.modules.xslt.tmap.model.api.Operation parent,
                TMapComponentFactory componentFactory,
                String inputFileStr, String source, String result) {

            Transform transform = componentFactory.createTransform();
            if (inputFileStr != null && !"".equals(inputFileStr)) {
                transform.setFile(inputFileStr);
            }
            transform.setSource(source);
            transform.setResult(result);

            name = name == null ? NameGenerator.getUniqueName(parent, Transform.class) : name;
            if (name != null) {
                try {
                    transform.setName(name);
                } catch (VetoException ex) {
                    LOGGGER.log(Level.WARNING, "couldn't set unique name to transform: " + name);
                }
            }

            return transform;
        }

//    private String getTMapVarRef(Variable var) {
//        String firstPartName = var == null ? null : getFirstPartName(var);
//        return getTMapVarRef(var, firstPartName);
//    }
        private String getTMapVarRef(Variable var, String partName) {
            if (partName == null || var == null) {
                return null;
            }
            String varName = var.getName();

            return varName == null
                    ? null : VariableReferenceImpl.getVarRefString(varName, partName);
        }

        private String getFirstPartName(Reference<Message> messageRef) {
            String partName = null;
            if (messageRef == null) {
                return partName;
            }

            Message message = messageRef.get();

            Collection<Part> parts = null;
            if (message != null) {
                parts = message.getParts();
            }

            Part part = null;
            if (parts != null && parts.size() > 0) {
                java.util.Iterator<Part> partIter = parts.iterator();
                part = partIter.next();
            }

            if (part != null) {
                partName = part.getName();
            }
            return partName;
        }

        private String getFirstPartName(Variable var) {
            if (var == null) {
                return null;
            }
            return getFirstPartName(var.getMessage());
        }

        private String getFirstPartName(OperationParameter opParam) {
            if (opParam == null) {
                return null;
            }
            return getFirstPartName(opParam.getMessage());
        }

        private Invoke createInvoke(
                org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp,
                TemplateWizard wizard, TMapComponentFactory componentFactory) {
            return createInvoke(tMapOp, null, null, wizard, componentFactory);
        }

        private Invoke createInvoke(
                org.netbeans.modules.xslt.tmap.model.api.Operation tMapOp,
                String inputInvokeVar,
                String outputInvokeVar,
                TemplateWizard wizard, TMapComponentFactory componentFactory) {
            assert tMapOp != null && wizard != null && componentFactory != null;
            Invoke invoke = null;

            Operation wizardOutputOperation =
                    (Operation) wizard.getProperty(Panel.CALLED_OPERATION);

            if (wizardOutputOperation != null) {
                assert wizardOutputOperation.getParent() instanceof PortType;
                PortType portType = (PortType) wizardOutputOperation.getParent();
                registerImport(tMapOp.getModel(), portType);

                invoke = componentFactory.createInvoke();
//          invoke.setPortType(
//                  invoke.createWSDLReference(wizardOutputPortType, PortType.class));
//          invoke.setOperation(
//                  invoke.createWSDLReference(wizardOutputOperation, Operation.class));

                NameGenerator varNameGenerator = NameGenerator.getDefault(tMapOp, Variable.class);
                if (inputInvokeVar == null || "".equals(inputInvokeVar)) {
                    inputInvokeVar = varNameGenerator.getName(tMapOp, NameGenerator.INPUT_INVOKE_VARIABLE_PREFIX);
                }
                if (outputInvokeVar == null || "".equals(outputInvokeVar)) {
                    outputInvokeVar = varNameGenerator.getName(tMapOp, NameGenerator.OUTPUT_INVOKE_VARIABLE_PREFIX);
                }
                invoke.setInputVariableName(inputInvokeVar);
                invoke.setOutputVariableName(outputInvokeVar);

                String name = NameGenerator.getUniqueName(tMapOp, Invoke.class);
                if (name != null) {
                    try {
                        invoke.setName(name);
                    } catch (VetoException ex) {
                        LOGGGER.log(Level.WARNING, "couldn't set unique name to invoke: " + name);
                    }
                }
            }
            return invoke;
        }
    }
    private static Map<String, TransformationUseCase> TRANSFORMATION_USE_CASE =
            new HashMap<String, TransformationUseCase>();
    

    static {
        TRANSFORMATION_USE_CASE.put(Panel.CHOICE_REQUEST_REPLY,
                TransformationUseCase.REQUEST_REPLY);
        TRANSFORMATION_USE_CASE.put(Panel.CHOICE_FILTER_ONE_WAY,
                TransformationUseCase.FILTER_ONE_WAY);
        TRANSFORMATION_USE_CASE.put(Panel.CHOICE_FILTER_REQUEST_REPLY,
                TransformationUseCase.FILTER_REQUEST_REPLY);
    }
    private static String TEMPLATES_PATH = "Templates/SOA_XSLT/"; // NOI18N

    private static String XSLT_SERVICE = "xslt.service"; // NOI18N

    private Panel myPanel;
}
