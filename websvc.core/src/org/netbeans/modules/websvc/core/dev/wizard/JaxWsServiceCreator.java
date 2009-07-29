/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.websvc.core.dev.wizard;

import org.netbeans.modules.websvc.api.support.ServiceCreator;
import org.netbeans.modules.websvc.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.core.ProjectInfo;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.VariableTree;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.project.config.ServiceAlreadyExistsExeption;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Radko, Milan Kuchtiak
 */
public class JaxWsServiceCreator implements ServiceCreator {

    private ProjectInfo projectInfo;
    private WizardDescriptor wiz;
    private boolean addJaxWsLib;
    private int serviceType;
    private int projectType;

    /**
     * Creates a new instance of WebServiceClientCreator
     */
    public JaxWsServiceCreator(ProjectInfo projectInfo, WizardDescriptor wiz, boolean addJaxWsLib) {
        this.projectInfo = projectInfo;
        this.wiz = wiz;
        this.addJaxWsLib = addJaxWsLib;
    }

    public void createService() throws IOException {
        serviceType = ((Integer) wiz.getProperty(WizardProperties.WEB_SERVICE_TYPE)).intValue();
        projectType = projectInfo.getProjectType();

        // Use Progress API to display generator messages.
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JaxWsServiceCreator.class, "TXT_WebServiceGeneration")); //NOI18N
        handle.start(100);

        Runnable r = new Runnable() {

            public void run() {
                try {
                    generateWebService(handle);
                } catch (Exception e) {
                    //finish progress bar
                    handle.finish();
                    String message = e.getLocalizedMessage();
                    if (message != null) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(r);
    }

    public void createServiceFromWsdl() throws IOException {

        //initProjectInfo(project);

        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JaxWsServiceCreator.class, "TXT_WebServiceGeneration")); //NOI18N

        Runnable r = new Runnable() {

            public void run() {
                try {
                    handle.start();
                    generateWsFromWsdl15(handle);
                } catch (Exception e) {
                    //finish progress bar
                    handle.finish();
                    String message = e.getLocalizedMessage();
                    if (message != null) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
                    }
                }
            }
        };
        RequestProcessor.getDefault().post(r);
    }

    //TODO it should be refactored to prevent duplicate code but it is more readable now during development
    private void generateWebService(ProgressHandle handle) throws Exception {

        FileObject pkg = Templates.getTargetFolder(wiz);
        String wsName = Templates.getTargetName(wiz);


        if (serviceType == WizardProperties.FROM_SCRATCH) {
            JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(projectInfo.getProject().getProjectDirectory());
            if (jaxWsSupport != null) {
                wsName = getUniqueJaxwsName(jaxWsSupport, wsName);
                handle.progress(NbBundle.getMessage(JaxWsServiceCreator.class, "MSG_GEN_WS"), 50); //NOI18N
                //add the JAXWS 2.0 library, if not already added
                if (addJaxWsLib) {
                    addJaxws21Library(projectInfo.getProject());
                }
                generateJaxWSImplFromTemplate(pkg, wsName, projectType);
                handle.finish();
            } else {
                 DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(JaxWsServiceCreator.class, "TXT_JaxWsNotSupported"),
                        NotifyDescriptor.ERROR_MESSAGE));
                 handle.finish();
            }
            return;
        }
        if (serviceType == WizardProperties.ENCAPSULATE_SESSION_BEAN) {
            if (/*(projectType == JSE_PROJECT_TYPE && Util.isSourceLevel16orHigher(project)) ||*/(Util.isJavaEE5orHigher(projectInfo.getProject()) && (projectType == ProjectInfo.WEB_PROJECT_TYPE || projectType == ProjectInfo.EJB_PROJECT_TYPE)) //NOI18N
                    ) {

                JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(projectInfo.getProject().getProjectDirectory());
                if (jaxWsSupport != null) {
                    wsName = getUniqueJaxwsName(jaxWsSupport, wsName);
                    handle.progress(NbBundle.getMessage(JaxWsServiceCreator.class, "MSG_GEN_SEI_AND_IMPL"), 50); //NOI18N
                    Node[] nodes = (Node[]) wiz.getProperty(WizardProperties.DELEGATE_TO_SESSION_BEAN);
                    generateWebServiceFromEJB(wsName, pkg, projectInfo, nodes);
                    handle.finish();
                } else {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(JaxWsServiceCreator.class, "TXT_JaxWsNotSupported"),
                        NotifyDescriptor.ERROR_MESSAGE));
                    handle.finish();                   
                }
            }
        }
    }

    private FileObject generateJaxWSImplFromTemplate(FileObject pkg, String wsName, int projectType) throws Exception {
        DataFolder df = DataFolder.findFolder(pkg);
        FileObject template = Templates.getTemplate(wiz);

        if ((Boolean)wiz.getProperty(WizardProperties.IS_STATELESS_BEAN)) { //EJB Web Service
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("EjbWebService", "java"); //NOI18N
        }
        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, wsName);
        FileObject createdFile = dobj.getPrimaryFile();
        createdFile.setAttribute("jax-ws-service", java.lang.Boolean.TRUE);
        dobj.setValid(false);
        dobj = DataObject.find(createdFile);
        final JaxWsModel jaxWsModel = projectInfo.getProject().getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel != null) {
            
            ClassPath classPath = getClassPathForFile( projectInfo.getProject(), createdFile);
                if (classPath != null) {
                String serviceImplPath = classPath.getResourceName(createdFile, '.', false);
                Service service = jaxWsModel.addService(wsName, serviceImplPath);
                ProjectManager.mutex().writeAccess(new Runnable() {

                    public void run() {
                        try {
                            jaxWsModel.write();
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                    }
                });
            }            
            JaxWsUtils.openFileInEditor(dobj);
            displayDuplicityWarning(createdFile);
        }

        return createdFile;
    }

    private String getUniqueJaxwsName(JAXWSSupport jaxWsSupport, String origName) {
        List webServices = jaxWsSupport.getServices();
        List<String> serviceNames = new ArrayList<String>(webServices.size());
        for (Object service : webServices) {
            serviceNames.add(((Service)service).getName());
        }
        return uniqueWSName(origName, serviceNames);
    }

    private String uniqueWSName(final String origName, List<String> names) {
        int uniquifier = 0;
        String truename = origName;
        while (names.contains(truename)) {
            truename = origName + String.valueOf(++uniquifier);
        }
        return truename;
    }

    private void addJaxws21Library(Project project) throws Exception {

        // check if the wsimport class is already present - this means we don't need to add the library
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(), ClassPath.COMPILE);
        FileObject wsimportFO = classPath.findResource("com/sun/tools/ws/ant/WsImport.class"); // NOI18N
        if (wsimportFO != null) {
            return;
        }

        ProjectClassPathExtender pce = (ProjectClassPathExtender) project.getLookup().lookup(ProjectClassPathExtender.class);
        Library jaxws21_ext = LibraryManager.getDefault().getLibrary("jaxws21"); //NOI18N
        if (pce != null && jaxws21_ext != null) {
            try {
                pce.addLibrary(jaxws21_ext);
            } catch (IOException e) {
                throw new Exception("Unable to add JAXWS 21 Library. " + e.getMessage());
            }
        } else {
            throw new Exception("Unable to add JAXWS 2.1 Library. " +
                    "ProjectClassPathExtender or library not found");
        }
    }

    private void generateWsFromWsdl15(final ProgressHandle handle) throws Exception {
        String wsdlFilePath = (String) wiz.getProperty(WizardProperties.WSDL_FILE_PATH);
        URL wsdlUrl = null;
        if (wsdlFilePath == null) {
            wsdlUrl = new URL((String) wiz.getProperty(WizardProperties.WSDL_URL));
        } else {
            File normalizedWsdlFilePath = FileUtil.normalizeFile(new File(wsdlFilePath));
            //convert to URI first to take care of spaces
            wsdlUrl = normalizedWsdlFilePath.toURI().toURL();
        }
        final URL wsdlURL = wsdlUrl;
        final WsdlService service = (WsdlService) wiz.getProperty(WizardProperties.WSDL_SERVICE);
        final Boolean useProvider = (Boolean) wiz.getProperty(WizardProperties.USE_PROVIDER);
        if (service == null) {
            FileObject targetFolder = Templates.getTargetFolder(wiz);
            String targetName = Templates.getTargetName(wiz);

            // create a fake implementation class to enable WS functionality (to enable WS node creation)
            if (targetFolder != null) {
                GenerationUtils.createClass(targetFolder, targetName, null);
            }

            WsdlServiceHandler handler = (WsdlServiceHandler) wiz.getProperty(WizardProperties.WSDL_SERVICE_HANDLER);
            JaxWsUtils.generateJaxWsArtifacts(projectInfo.getProject(), targetFolder, targetName, wsdlURL, handler.getServiceName(), handler.getPortName());
            WsdlModeler wsdlModeler = (WsdlModeler) wiz.getProperty(WizardProperties.WSDL_MODELER);
            if (wsdlModeler != null && wsdlModeler.getCreationException() != null) {
                handle.finish();
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(JaxWsServiceCreator.class, "TXT_CannotGenerateArtifacts",
                        wsdlModeler.getCreationException().getLocalizedMessage()),
                        NotifyDescriptor.ERROR_MESSAGE));
            } else {
                handle.finish();
            }
        } else {
            final WsdlPort port = (WsdlPort) wiz.getProperty(WizardProperties.WSDL_PORT);
            final boolean isStatelessSB = (Boolean)wiz.getProperty(WizardProperties.IS_STATELESS_BEAN);
            //String portJavaName = port.getJavaName();   
            WsdlModeler wsdlModeler = (WsdlModeler) wiz.getProperty(WizardProperties.WSDL_MODELER);
            // don't set the packageName for modeler (use the default one generated from target Namespace)
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {

                public void modelCreated(WsdlModel model) {
                    if (model == null) {
                        handle.finish();
                        return;
                    }
                    WsdlService service1 = model.getServiceByName(service.getName());
                    WsdlPort port1 = service1.getPortByName(port.getName());

                    port1.setSOAPVersion(port.getSOAPVersion());
                    FileObject targetFolder = Templates.getTargetFolder(wiz);
                    String targetName = Templates.getTargetName(wiz);
                    try {
                        JaxWsUtils.generateJaxWsImplementationClass(projectInfo.getProject(),
                                targetFolder,
                                targetName,
                                wsdlURL,
                                service1, port1, useProvider, isStatelessSB);
                        handle.finish();
                    } catch (Exception ex) {
                        handle.finish();
                        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,
                                ex);
                    }
                }
            });
        }
    }

    private void generateWebServiceFromEJB(String wsName, FileObject pkg, ProjectInfo projectInfo, Node[] nodes) throws IOException, ServiceAlreadyExistsExeption, PropertyVetoException {

        if (nodes != null && nodes.length == 1) {

            EjbReference ejbRef = nodes[0].getLookup().lookup(EjbReference.class);
            if (ejbRef != null) {

                DataFolder df = DataFolder.findFolder(pkg);
                FileObject template = Templates.getTemplate(wiz);

                if ((Boolean)wiz.getProperty(WizardProperties.IS_STATELESS_BEAN)) { //EJB Web Service
                    FileObject templateParent = template.getParent();
                    template = templateParent.getFileObject("EjbWebService", "java"); //NOI18N
                }
                DataObject dTemplate = DataObject.find(template);
                DataObject dobj = dTemplate.createFromTemplate(df, wsName);
                FileObject createdFile = dobj.getPrimaryFile();
                createdFile.setAttribute("jax-ws-service", java.lang.Boolean.TRUE);
                dobj.setValid(false);
                dobj = DataObject.find(createdFile);

                ClassPath classPath = getClassPathForFile(projectInfo.getProject(), createdFile);
                if (classPath != null) {
                    String serviceImplPath = classPath.getResourceName(createdFile, '.', false);
                    generateDelegateMethods(createdFile, ejbRef);

                    final JaxWsModel jaxWsModel = projectInfo.getProject().getLookup().lookup(JaxWsModel.class);
                    if (jaxWsModel != null) {
                        jaxWsModel.addService(wsName, serviceImplPath);
                        ProjectManager.mutex().writeAccess(new Runnable() {

                            public void run() {
                                try {
                                    jaxWsModel.write();
                                } catch (IOException ex) {
                                    ErrorManager.getDefault().notify(ex);
                                }
                            }
                        });
                    }
                }
                JaxWsUtils.openFileInEditor(dobj);
                displayDuplicityWarning(createdFile);
            }
        }
    }
    
    private void displayDuplicityWarning(final FileObject createdFile) {
        final String serviceName = createdFile.getName()+"Service"; //NOI18N
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Service serv = JaxWsUtils.findServiceForServiceName(createdFile, serviceName);
                if (serv != null) {
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            NbBundle.getMessage(JaxWsServiceCreator.class,"MSG_ServiceNameExists", serviceName, serv.getImplementationClass()), 
                            NotifyDescriptor.WARNING_MESSAGE));
                }
            }

        });        
    }

    private void generateDelegateMethods(final FileObject targetFo, final EjbReference ref) throws IOException {
        final boolean[] onClassPath = new boolean[1];
        final String[] interfaceClass = new String[1];

        JavaSource targetSource = JavaSource.forFileObject(targetFo);
        CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                TreeMaker make = workingCopy.getTreeMaker();

                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                if (typeElement != null) {
                    VariableTree ejbRefInjection = null;
                    interfaceClass[0] = ref.getLocal();
                    if (interfaceClass[0] == null) {
                        interfaceClass[0] = ref.getRemote();
                    }
                    if (interfaceClass[0] == null) {
                        interfaceClass[0] = ref.getEjbClass();
                    }

                    ejbRefInjection = generateEjbInjection(workingCopy, make, interfaceClass[0], onClassPath);

                    if (ejbRefInjection != null) {
                        String comment1 = "Add business logic below. (Right-click in editor and choose"; //NOI18N
                        String comment2 = "\"Insert Code > Add Web Service Operation\")"; //NOI18N
                        make.addComment(ejbRefInjection, Comment.create(Comment.Style.LINE, 0, 0, 4, comment1), false);
                        make.addComment(ejbRefInjection, Comment.create(Comment.Style.LINE, 0, 0, 4, comment2), false);

                        ClassTree javaClass = workingCopy.getTrees().getTree(typeElement);
                        ClassTree modifiedClass = make.insertClassMember(javaClass, 0, ejbRefInjection);

                        if (onClassPath[0]) {
                            TypeElement beanInterface = workingCopy.getElements().getTypeElement(interfaceClass[0]);
                            modifiedClass = generateMethods(workingCopy, make, typeElement, modifiedClass, beanInterface);
                        }

                        workingCopy.rewrite(javaClass, modifiedClass);
                    }
                }
            }

            public void cancel() {
            }
        };
        targetSource.runModificationTask(modificationTask).commit();

        if (!onClassPath[0]) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(NbBundle.getMessage(JaxWsServiceCreator.class, "MSG_EJB_NOT_ON_CLASSPATH", interfaceClass[0], targetFo.getName()),
                            NotifyDescriptor.WARNING_MESSAGE));
                }
            });
        }
    }

    private VariableTree generateEjbInjection(WorkingCopy workingCopy, TreeMaker make, String beanInterface, boolean[] onClassPath) {

        TypeElement ejbAnElement = workingCopy.getElements().getTypeElement("javax.ejb.EJB"); //NOI18N
        TypeElement interfaceElement = workingCopy.getElements().getTypeElement(beanInterface); //NOI18N

        AnnotationTree ejbAnnotation = make.Annotation(
                make.QualIdent(ejbAnElement),
                Collections.<ExpressionTree>emptyList());
        // create method modifier: public and no annotation
        ModifiersTree methodModifiers = make.Modifiers(
                Collections.<Modifier>singleton(Modifier.PRIVATE),
                Collections.<AnnotationTree>singletonList(ejbAnnotation));

        onClassPath[0] = interfaceElement != null;

        return make.Variable(
                methodModifiers,
                "ejbRef", //NOI18N
                onClassPath[0] ? make.Type(interfaceElement.asType()) : make.Identifier(beanInterface),
                null);
    }

    private ClassTree generateMethods(WorkingCopy workingCopy,
            TreeMaker make,
            TypeElement classElement,
            ClassTree modifiedClass,
            TypeElement beanInterface) throws IOException {

        GeneratorUtilities utils = GeneratorUtilities.get(workingCopy);

        List<? extends Element> interfaceElements = beanInterface.getEnclosedElements();
        TypeElement webMethodEl = workingCopy.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
        assert (webMethodEl != null);
        if (webMethodEl == null) {
            return modifiedClass;
        }

        Set<String> operationNames = new HashSet<String>();
        for (Element el : interfaceElements) {
            if (el.getKind() == ElementKind.METHOD && el.getModifiers().contains(Modifier.PUBLIC)) {
                ExecutableElement methodEl = (ExecutableElement) el;
                MethodTree method = utils.createAbstractMethodImplementation(classElement, methodEl);

                Name methodName = methodEl.getSimpleName();
                boolean isVoid = workingCopy.getTypes().getNoType(TypeKind.VOID) == methodEl.getReturnType();

                String operationName = findUniqueOperationName(operationNames, methodName.toString());
                operationNames.add(operationName);

                // generate @WebMethod annotation
                AssignmentTree opName = make.Assignment(make.Identifier("operationName"), make.Literal(operationName)); //NOI18N

                AnnotationTree webMethodAn = make.Annotation(
                        make.QualIdent(webMethodEl),
                        Collections.<ExpressionTree>singletonList(opName));
                ModifiersTree modifiersTree = make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PUBLIC),
                        Collections.<AnnotationTree>singletonList(webMethodAn));

                // generate @RequestWrapper and @RequestResponse annotations
                if (!methodName.contentEquals(operationName)) {
                    TypeElement requestWrapperEl = workingCopy.getElements().getTypeElement("javax.xml.ws.RequestWrapper"); //NOI18N
                    TypeElement responseWrapperEl = workingCopy.getElements().getTypeElement("javax.xml.ws.ResponseWrapper"); //NOI18N
                    AssignmentTree className = make.Assignment(make.Identifier("className"), make.Literal(operationName)); //NOI18N
                    AnnotationTree requestWrapperAn = make.Annotation(
                            make.QualIdent(requestWrapperEl),
                            Collections.<ExpressionTree>singletonList(className));
                    modifiersTree = make.addModifiersAnnotation(modifiersTree, requestWrapperAn);

                    if (!isVoid) { // only if not void                     
                        className = make.Assignment(make.Identifier("className"), make.Literal(operationName + "Response")); //NOI18N
                        AnnotationTree responseWrapperAn = make.Annotation(
                                make.QualIdent(responseWrapperEl),
                                Collections.<ExpressionTree>singletonList(className));
                        modifiersTree = make.addModifiersAnnotation(modifiersTree, responseWrapperAn);
                    }
                }

                // generate @Oneway annotation
                if (isVoid && method.getThrows().isEmpty()) {
                    TypeElement onewayEl = workingCopy.getElements().getTypeElement("javax.jws.Oneway"); //NOI18N
                    AnnotationTree onewayAn = make.Annotation(
                            make.QualIdent(onewayEl),
                            Collections.<ExpressionTree>emptyList());
                    modifiersTree = make.addModifiersAnnotation(modifiersTree, onewayAn);
                }
                // parameters
                List<? extends VariableTree> params = method.getParameters();
                List<VariableTree> newParams = new ArrayList<VariableTree>();
                if (params.size() > 0) {
                    TypeElement paramEl = workingCopy.getElements().getTypeElement("javax.jws.WebParam"); //NOI18N
                    for (VariableTree param: params) {
                        String paramName = param.getName().toString();
                        AssignmentTree nameAttr = make.Assignment(make.Identifier("name"), make.Literal(paramName)); //NOI18N
                        AnnotationTree paramAn = make.Annotation(
                                make.QualIdent(paramEl),
                                Collections.<ExpressionTree>singletonList(nameAttr));
                        ModifiersTree paramModifierTree = make.addModifiersAnnotation(param.getModifiers(), paramAn);
                        newParams.add(make.Variable(paramModifierTree, param.getName(), param.getType(), null));
                    }
                }
                
                // method body
                List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
                for (VariableElement ve : methodEl.getParameters()) {
                    arguments.add(make.Identifier(ve.getSimpleName()));
                }
                MethodInvocationTree inv = make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(make.Identifier("ejbRef"), methodName), //NOI18N
                        arguments);

                StatementTree statement = isVoid ? make.ExpressionStatement(inv) : make.Return(inv);

                BlockTree body = make.Block(Collections.singletonList(statement), false);

                MethodTree delegatingMethod = make.Method(
                        modifiersTree,
                        method.getName(),
                        method.getReturnType(),
                        method.getTypeParameters(),
                        newParams,
                        method.getThrows(),
                        body,
                        null);
                modifiedClass = make.addClassMember(modifiedClass, delegatingMethod);
            }
        }
        return modifiedClass;
    }

    private String findUniqueOperationName(Set<String> existingNames, String operationName) {
        if (!existingNames.contains(operationName)) {
            return operationName;
        } else {
            int i = 1;
            String newName = operationName + "_1"; //NOI18N
            while (existingNames.contains(newName)) {
                newName = operationName + "_" + String.valueOf(++i); //NOI18N
            }
            return newName;
        }
    }
    
    private ClassPath getClassPathForFile(Project project, FileObject file) {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup srcGroup: srcGroups) {
            FileObject srcRoot = srcGroup.getRootFolder();
            if (FileUtil.isParentOf(srcRoot, file)) {
                return ClassPath.getClassPath(srcRoot, ClassPath.SOURCE);
            }
        }
        return null;
    }
}
