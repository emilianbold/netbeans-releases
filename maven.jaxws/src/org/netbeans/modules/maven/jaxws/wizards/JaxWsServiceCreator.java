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
package org.netbeans.modules.maven.jaxws.wizards;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.maven.jaxws.MavenJAXWSSupportIml;
import org.netbeans.modules.maven.jaxws.MavenWebService;
import org.netbeans.modules.maven.jaxws.WSUtils;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.support.ServiceCreator;
import java.io.IOException;
import org.netbeans.api.java.project.JavaProjectConstants;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;

import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.jaxws.MavenModelUtils;
import org.netbeans.modules.websvc.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Radko, Milan Kuchtiak
 */
public class JaxWsServiceCreator implements ServiceCreator {

    private Project project;
    private WizardDescriptor wiz;
    private boolean addJaxWsLib;
    private int serviceType;

    /**
     * Creates a new instance of WebServiceClientCreator
     */
    public JaxWsServiceCreator(Project project, WizardDescriptor wiz, boolean addJaxWsLib) {
        this.project = project;
        this.wiz = wiz;
        this.addJaxWsLib = addJaxWsLib;
    }

    public void createService() throws IOException {
        serviceType = ((Integer) wiz.getProperty(WizardProperties.WEB_SERVICE_TYPE)).intValue();

        // Use Progress API to display generator messages.
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JaxWsServiceCreator.class, "TXT_WebServiceGeneration")); //NOI18N
        handle.start(100);

        Runnable r = new Runnable() {

            public void run() {
                try {
                    generateWebService(handle);
                } catch (IOException e) {
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
                    handle.start(100);
                    generateWsFromWsdl15(handle);
                } catch (IOException e) {
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
    private void generateWebService(ProgressHandle handle) throws IOException {

        FileObject pkg = Templates.getTargetFolder(wiz);

        if (serviceType == WizardProperties.FROM_SCRATCH) {
            handle.progress(NbBundle.getMessage(JaxWsServiceCreator.class, "MSG_GEN_WS"), 50); //NOI18N
            //add the JAXWS 2.0 library, if not already added
            if (addJaxWsLib) {
                MavenModelUtils.addJaxws21Library(project);
            }
            generateJaxWSImplFromTemplate(pkg);
            handle.finish();
        }
    }

    private void generateWsFromWsdl15(final ProgressHandle handle) throws IOException {
        handle.progress(NbBundle.getMessage(JaxWsServiceCreator.class, "MSG_GEN_WS"), 50); //NOI18N

        JAXWSLightSupport jaxWsSupport = JAXWSLightSupport.getJAXWSLightSupport(project.getProjectDirectory());
        String wsdlUrl = (String)wiz.getProperty(WizardProperties.WSDL_URL);
        String filePath = (String)wiz.getProperty(WizardProperties.WSDL_FILE_PATH);

        //Boolean useDispatch = (Boolean) wiz.getProperty(ClientWizardProperties.USEDISPATCH);
        //if (wsdlUrl==null) wsdlUrl = "file:"+(filePath.startsWith("/")?filePath:"/"+filePath); //NOI18N

        if(wsdlUrl == null) {
            wsdlUrl = FileUtil.toFileObject(FileUtil.normalizeFile(new File(filePath))).getURL().toExternalForm();
        }
        FileObject localWsdlFolder = jaxWsSupport.getLocalWsdlFolder(true);

        boolean hasSrcFolder = false;
        File srcFile = new File (FileUtil.toFile(project.getProjectDirectory()),"src"); //NOI18N
        if (srcFile.exists()) {
            hasSrcFolder = true;
        } else {
            hasSrcFolder = srcFile.mkdirs();
        }

        if (localWsdlFolder != null) {
            FileObject wsdlFo = null;
            try {
                wsdlFo = WSUtils.retrieveResource(
                        localWsdlFolder,
                        (hasSrcFolder ? new URI(MavenJAXWSSupportIml.CATALOG_PATH) : new URI("jax-ws-catalog.xml")), //NOI18N
                        new URI(wsdlUrl));
            } catch (URISyntaxException ex) {
                //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(JaxWsServiceCreator.class, "ERR_IncorrectURI", wsdlUrl); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            } catch (UnknownHostException ex) {
                //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(JaxWsServiceCreator.class, "ERR_UnknownHost", ex.getMessage()); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            } catch (IOException ex) {
                //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(JaxWsServiceCreator.class, "ERR_WsdlRetrieverFailure", wsdlUrl); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }

            Preferences prefs = ProjectUtils.getPreferences(project, MavenWebService.class,true);
            if (prefs != null) {
                // remember original WSDL URL for service
                prefs.put(MavenWebService.SERVICE_PREFIX+wsdlFo.getName(), wsdlUrl);
            }

            if (wsdlFo != null) {
                MavenModelUtils.addJaxws21Library(project);
                final String relativePath = FileUtil.getRelativePath(localWsdlFolder, wsdlFo);
                final String serviceName = wsdlFo.getName();
                ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                    public void performOperation(POMModel model) {
                        org.netbeans.modules.maven.model.pom.Plugin plugin =
                                WSUtils.isEJB(project) ?
                                    MavenModelUtils.addJaxWSPlugin(model, "2.0") : //NOI18N
                                    MavenModelUtils.addJaxWSPlugin(model);
                        MavenModelUtils.addWsimportExecution(plugin, serviceName, relativePath);
                        if (WSUtils.isWeb(project)) { // expecting web project
                            MavenModelUtils.addWarPlugin(model);
                        } else { // J2SE Project
                            MavenModelUtils.addWsdlResources(model);
                        }
                    }
                };
                Utilities.performPOMModelOperations(project.getProjectDirectory().getFileObject("pom.xml"),
                        Collections.singletonList(operation));

                // create empty web service implementation class
                FileObject pkg = Templates.getTargetFolder(wiz);
                final FileObject targetFile = generateJaxWSImplFromTemplate(pkg);

                // execute wsimport goal
                RunConfig cfg = RunUtils.createRunConfig(FileUtil.toFile(project.getProjectDirectory()), project, "wsimport",
                        Collections.singletonList("jaxws:wsimport"));
                ExecutorTask task = RunUtils.executeMaven(cfg);

                try {
                    task.waitFinished(60000);
                } catch (InterruptedException ex) {

                }

                final WsdlService wsdlService = (WsdlService) wiz.getProperty(WizardProperties.WSDL_SERVICE);
                final WsdlPort wsdlPort = (WsdlPort) wiz.getProperty(WizardProperties.WSDL_PORT);

                try {
                    String wsdlLocationPrefix = WSUtils.isWeb(project) ? "WEB-INF/wsdl/" : "META-INF/wsdl/"; //NOI18N
                    generateJaxWsImplClass(targetFile, wsdlService, wsdlPort, wsdlLocationPrefix+relativePath); //NOI18N
                    DataObject targetDo = DataObject.find(targetFile);
                    if (targetDo != null) {
                        SaveCookie save = targetDo.getCookie(SaveCookie.class);
                        if (save != null) {
                            save.save();
                        }
                    }

                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);                   
                }

            }
        }

        handle.finish();
    }
    
    private FileObject generateJaxWSImplFromTemplate(FileObject pkg) throws IOException {
        DataFolder df = DataFolder.findFolder(pkg);
        FileObject template = Templates.getTemplate(wiz);

        if (WSUtils.isEJB(project)) { //EJB Web Service
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("EjbWebService", "java"); //NOI18N
        }
        
        DataObject dTemplate = DataObject.find(template);

        DataObject dobj = dTemplate.createFromTemplate(df, Templates.getTargetName(wiz));
        FileObject createdFile = dobj.getPrimaryFile();
           
        openFileInEditor(dobj);

        return createdFile;
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
    
    public static void openFileInEditor(DataObject dobj) {

        final OpenCookie openCookie = dobj.getCookie(OpenCookie.class);
        if (openCookie != null) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    openCookie.open();
                }
            }, 1000);
        }
    }

    private void generateJaxWsImplClass(FileObject targetFile, final WsdlService service, final WsdlPort port, final String wsdlLocation) throws IOException {

        JavaSource targetSource = JavaSource.forFileObject(targetFile);
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (javaClass != null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);

                    //add @WebService annotation
                    TypeElement WSAn = workingCopy.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
                    List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                    attrs.add(
                            make.Assignment(make.Identifier("serviceName"), make.Literal(service.getName()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("portName"), make.Literal(port.getName()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("endpointInterface"), make.Literal(port.getJavaName()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("targetNamespace"), make.Literal(port.getNamespaceURI()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("wsdlLocation"), make.Literal(wsdlLocation))); //NOI18N

                    AnnotationTree WSAnnotation = make.Annotation(
                            make.QualIdent(WSAn),
                            attrs);
                    ClassTree  modifiedClass = genUtils.addAnnotation(javaClass, WSAnnotation);

                    if (WsdlPort.SOAP_VERSION_12.equals(port.getSOAPVersion())) {
                        TypeElement BindingAn = workingCopy.getElements().getTypeElement("javax.xml.ws.BindingType"); //NOI18N

                        List<ExpressionTree> bindingAttrs = new ArrayList<ExpressionTree>();
                        bindingAttrs.add(make.Assignment(make.Identifier("value"), //NOI18N
                                make.Identifier("javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING"))); //NOI18N
                        AnnotationTree bindingAnnotation = make.Annotation(
                                make.QualIdent(BindingAn),
                                bindingAttrs);
                        modifiedClass = genUtils.addAnnotation(modifiedClass, bindingAnnotation);
                    }

                    // add @Stateless annotation
                    if (WSUtils.isEJB(project)) {
                        TypeElement statelessAn = workingCopy.getElements().getTypeElement("javax.ejb.Stateless"); //NOI18N
                        if (statelessAn != null) {
                            AnnotationTree StatelessAnnotation = make.Annotation(
                                    make.QualIdent(statelessAn),
                                    Collections.<ExpressionTree>emptyList());
                            modifiedClass = genUtils.addAnnotation(modifiedClass, StatelessAnnotation);
                        }
                    }

                    List<WsdlOperation> operations = port.getOperations();
                    for (WsdlOperation operation : operations) {

                        // return type
                        String returnType = operation.getReturnTypeName();

                        // create parameters
                        List<WsdlParameter> parameters = operation.getParameters();
                        List<VariableTree> params = new ArrayList<VariableTree>();
                        for (WsdlParameter parameter : parameters) {
                            // create parameter:
                            // final ObjectOutput arg0
                            params.add(make.Variable(
                                    make.Modifiers(
                                    Collections.<Modifier>emptySet(),
                                    Collections.<AnnotationTree>emptyList()),
                                    parameter.getName(), // name
                                    make.Identifier(parameter.getTypeName()), // parameter type
                                    null // initializer - does not make sense in parameters.
                                    ));
                        }

                        // create exceptions
                        Iterator<String> exceptions = operation.getExceptions();
                        List<ExpressionTree> exc = new ArrayList<ExpressionTree>();
                        while (exceptions.hasNext()) {
                            String exception = exceptions.next();
                            TypeElement excEl = workingCopy.getElements().getTypeElement(exception);
                            if (excEl != null) {
                                exc.add(make.QualIdent(excEl));
                            } else {
                                exc.add(make.Identifier(exception));
                            }
                        }

                        // create method
                        ModifiersTree methodModifiers = make.Modifiers(
                                Collections.<Modifier>singleton(Modifier.PUBLIC),
                                Collections.<AnnotationTree>emptyList());
                        MethodTree method = make.Method(
                                methodModifiers, // public
                                operation.getJavaName(), // operation name
                                make.Identifier(returnType), // return type
                                Collections.<TypeParameterTree>emptyList(), // type parameters - none
                                params,
                                exc, // throws
                                "{ //TODO implement this method\nthrow new UnsupportedOperationException(\"Not implemented yet.\") }", // body text
                                null // default value - not applicable here, used by annotations
                                );

                        modifiedClass = make.addClassMember(modifiedClass, method);
                    }
                    workingCopy.rewrite(javaClass, modifiedClass);

//                    if (port.getSOAPVersion().equals(SOAP12_VERSION)) {  //if SOAP 1.2 binding, add BindingType annotation
//                        TypeElement bindingElement = workingCopy.getElements().getTypeElement(BINDING_TYPE_ANNOTATION);
//                        if (bindingElement != null) {
//                            ModifiersTree modifiersTree = modifiedClass.getModifiers();
//                            AssignmentTree soapVersion = make.Assignment(make.Identifier("value"), make.Literal(SOAP12_NAMESPACE)); //NOI18N
//                            AnnotationTree soapVersionAnnotation = make.Annotation(
//                                    make.QualIdent(bindingElement),
//                                    Collections.<ExpressionTree>singletonList(soapVersion));
//
//                            ModifiersTree newModifiersTree = make.addModifiersAnnotation(modifiersTree, soapVersionAnnotation);
//                            workingCopy.rewrite(modifiersTree, newModifiersTree);
//                        }
//                    }
                }
            }

            public void cancel() {
            }
        };
        targetSource.runModificationTask(task).commit();

    }
}
