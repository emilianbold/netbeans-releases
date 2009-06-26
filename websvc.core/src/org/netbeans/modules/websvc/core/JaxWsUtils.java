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
package org.netbeans.modules.websvc.core;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModelFactory;
import org.netbeans.modules.websvc.api.jaxws.bindings.GlobalBindings;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWs;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWsStackProvider;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.websvc.core.dev.wizard.WizardProperties;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase.Use;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.Utilities;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsUtils {

    public static final String HANDLER_TEMPLATE = "Templates/WebServices/MessageHandler.java"; //NOI18N
    private static final String OLD_SOAP12_NAMESPACE = "http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/"; //NOI18N
    private static final String SOAP12_NAMESPACE = "http://www.w3.org/2003/05/soap/bindings/HTTP/";  //NOI18N
    private static final String BINDING_TYPE_ANNOTATION = "javax.xml.ws.BindingType"; //NOI18N
    private static int projectType;
    private static boolean jsr109Supported = false;

    /** Creates a new instance of JaxWsUtils */
    public JaxWsUtils() {
    }

    /** This method is called from Refresh Service action
     */
    public static void generateJaxWsImplementationClass(Project project, FileObject targetFolder, String targetName, WsdlModel wsdlModel, org.netbeans.modules.websvc.api.jaxws.project.config.Service service) throws Exception {
        WsdlService wsdlService = wsdlModel.getServiceByName(service.getServiceName());
        WsdlPort wsdlPort = null;
        if (wsdlService != null) {
            wsdlPort = wsdlService.getPortByName(service.getPortName());
        }
        if (wsdlService != null && wsdlPort != null) {
            String serviceID = service.getName();
            initProjectInfo(project);
            boolean isStatelessSB = (projectType == ProjectInfo.EJB_PROJECT_TYPE);
            if (wsdlPort.isProvider()/*from customization*/ || service.isUseProvider() /*from ws creation wizard*/) {
                generateProviderImplClass(project, targetFolder, null, targetName, wsdlService, wsdlPort, serviceID, isStatelessSB);
            } else {
                generateJaxWsImplClass(project, targetFolder, targetName, null, wsdlService, wsdlPort, false, serviceID, isStatelessSB);
            }
        }
    }

    /** This method is called from Create Web Service from WSDL wizard
     */
    public static void generateJaxWsImplementationClass(Project project, FileObject targetFolder, String targetName, URL wsdlURL, WsdlService service, WsdlPort port, boolean useProvider, boolean isStatelessSB) throws Exception {
        if (useProvider) {
            generateJaxWsProvider(project, targetFolder, targetName, wsdlURL, service, port, isStatelessSB);
        } else {
            initProjectInfo(project);
            generateJaxWsImplClass(project, targetFolder, targetName, wsdlURL, service, port, true, null, isStatelessSB);
        }
    }

    /** This method is called from Create Web Service from WSDL wizard
     */
    public static void generateJaxWsArtifacts(Project project, FileObject targetFolder, String targetName, URL wsdlURL, String service, String port) throws Exception {
        initProjectInfo(project);
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        String artifactsPckg = "service." + targetName.toLowerCase(); //NOI18N
        ClassPath classPath = ClassPath.getClassPath(targetFolder, ClassPath.SOURCE);
        String serviceImplPath = classPath.getResourceName(targetFolder, '.', false);
        jaxWsSupport.addService(targetName, serviceImplPath + "." + targetName, wsdlURL.toExternalForm(), service, port, artifactsPckg, jsr109Supported, false);
    }

    private static void generateProviderImplClass(Project project, FileObject targetFolder, FileObject implClass,
            String targetName, final WsdlService service, final WsdlPort port, String serviceID, final boolean isStatelessSB) throws Exception {
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        FileObject implClassFo = implClass;
        if (implClassFo == null) {
            implClassFo = GenerationUtils.createClass(targetFolder, targetName, null);
            implClassFo.setAttribute("jax-ws-service", Boolean.TRUE);
            implClassFo.setAttribute("jax-ws-service-provider", Boolean.TRUE);
            DataObject.find(implClassFo).setValid(false);
        }
        final String wsdlLocation = jaxWsSupport.getWsdlLocation(serviceID);
        JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (javaClass != null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);

                    // add implementation clause
                    ExpressionTree implClause = make.Identifier("javax.xml.ws.Provider<javax.xml.transform.Source>"); //NOI18N
                    ClassTree modifiedClass = make.addClassImplementsClause(javaClass, implClause);

                    // add @Stateless annotation
                    if (isStatelessSB) {//Stateless Session Bean
                        TypeElement StatelessAn = workingCopy.getElements().getTypeElement("javax.ejb.Stateless"); //NOI18N
                        AnnotationTree StatelessAnnotation = make.Annotation(
                                make.QualIdent(StatelessAn),
                                Collections.<ExpressionTree>emptyList());
                        modifiedClass = genUtils.addAnnotation(modifiedClass, StatelessAnnotation);
                    }
                    TypeElement serviceModeAn = workingCopy.getElements().getTypeElement("javax.xml.ws.ServiceMode"); //NOI18N
                    List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                    IdentifierTree idTree = make.Identifier("javax.xml.ws.Service.Mode.PAYLOAD");
                    attrs.add(
                            make.Assignment(make.Identifier("value"), idTree));  //NOI18N
                    AnnotationTree serviceModeAnnotation = make.Annotation(
                            make.QualIdent(serviceModeAn),
                            attrs);
                    modifiedClass = genUtils.addAnnotation(modifiedClass, serviceModeAnnotation);

                    TypeElement wsProviderAn = workingCopy.getElements().getTypeElement("javax.xml.ws.WebServiceProvider"); //NOI18N
                    attrs = new ArrayList<ExpressionTree>();
                    attrs.add(
                            make.Assignment(make.Identifier("serviceName"), make.Literal(service.getName()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("portName"), make.Literal(port.getName()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("targetNamespace"), make.Literal(port.getNamespaceURI()))); //NOI18N
                    attrs.add(
                            make.Assignment(make.Identifier("wsdlLocation"), make.Literal(wsdlLocation))); //NOI18N

                    AnnotationTree providerAnnotation = make.Annotation(
                            make.QualIdent(wsProviderAn),
                            attrs);
                    modifiedClass = genUtils.addAnnotation(modifiedClass, providerAnnotation);

                    String type = "javax.xml.transform.Source";
                    List<VariableTree> params = new ArrayList<VariableTree>();
                    params.add(make.Variable(
                            make.Modifiers(
                            Collections.<Modifier>emptySet(),
                            Collections.<AnnotationTree>emptyList()),
                            "source", // name
                            make.Identifier(type), // parameter type
                            null // initializer - does not make sense in parameters.
                            ));//);
                    // create method
                    ModifiersTree methodModifiers = make.Modifiers(
                            Collections.<Modifier>singleton(Modifier.PUBLIC),
                            Collections.<AnnotationTree>emptyList());
                    MethodTree method = make.Method(
                            methodModifiers, // public
                            "invoke", // operation name
                            make.Identifier(type), // return type
                            Collections.<TypeParameterTree>emptyList(), // type parameters - none
                            params,
                            Collections.<ExpressionTree>emptyList(), // throws
                            "{ //TODO implement this method\nthrow new UnsupportedOperationException(\"Not implemented yet.\") }", // body text
                            null // default value - not applicable here, used by annotations
                            );

                    modifiedClass = make.addClassMember(modifiedClass, method);
                    workingCopy.rewrite(javaClass, modifiedClass);
                }
            }

            public void cancel() {
            }
        };
        targetSource.runModificationTask(task).commit();
        //open in editor

        DataObject dobj = DataObject.find(implClassFo);
        List services = jaxWsSupport.getServices();
        if (serviceID != null) {
            for (Object serv : services) {
                if (serviceID.equals(((Service) serv).getName())) {

                    final EditCookie editCookie = dobj.getCookie(EditCookie.class);
                    if (editCookie != null) {
                        RequestProcessor.getDefault().post(new Runnable() {

                            public void run() {
                                editCookie.edit();
                            }
                        }, 1000);
                        break;
                    }
                }
            }
        }
    }

    private static void generateJaxWsProvider(Project project, FileObject targetFolder, String targetName, URL wsdlURL, WsdlService service, WsdlPort port, boolean isStatelessSB) throws Exception {
        initProjectInfo(project);
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        String portJavaName = port.getJavaName();
        String artifactsPckg = portJavaName.substring(0, portJavaName.lastIndexOf("."));
        FileObject implClassFo = GenerationUtils.createClass(targetFolder, targetName, null);
        implClassFo.setAttribute("jax-ws-service", Boolean.TRUE);
        implClassFo.setAttribute("jax-ws-service-provider", Boolean.TRUE);
        DataObject.find(implClassFo).setValid(false);
        ClassPath classPath = ClassPath.getClassPath(implClassFo, ClassPath.SOURCE);
        String serviceImplPath = classPath.getResourceName(implClassFo, '.', false);
        String serviceID = jaxWsSupport.addService(targetName, serviceImplPath, wsdlURL.toString(), service.getName(),
                port.getName(), artifactsPckg, jsr109Supported, true);

        generateProviderImplClass(project, targetFolder, implClassFo, targetName, service, port, serviceID, isStatelessSB);

    }

    private static void generateJaxWsImplClass(Project project, FileObject targetFolder, String targetName, URL wsdlURL, final WsdlService service, final WsdlPort port, boolean addService, String serviceID, final boolean isStatelessSB) throws Exception {

        // Use Progress API to display generator messages.
        //ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JaxWsUtils.class, "TXT_WebServiceGeneration")); //NOI18N
        //handle.start(100);
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());

        FileObject implClassFo = GenerationUtils.createClass(targetFolder, targetName, null);
        implClassFo.setAttribute("jax-ws-service", Boolean.TRUE);
        DataObject.find(implClassFo).setValid(false);

        ClassPath classPath = ClassPath.getClassPath(implClassFo, ClassPath.SOURCE);
        String serviceImplPath = classPath.getResourceName(implClassFo, '.', false);
        String portJavaName = port.getJavaName();
        String artifactsPckg = portJavaName.substring(0, portJavaName.lastIndexOf("."));
        if (addService) {
            serviceID = jaxWsSupport.addService(targetName, serviceImplPath, wsdlURL.toString(), service.getName(), port.getName(), artifactsPckg, jsr109Supported, false);
            if (serviceID == null) {
                Logger.getLogger(JaxWsUtils.class.getName()).log(Level.WARNING, "Failed to add service element to nbproject/jax-ws.xml. Either problem with downloading wsdl file or problem with writing into nbproject/jax-ws.xml.");
                return;
            }
        }

        final String wsdlLocation = jaxWsSupport.getWsdlLocation(serviceID);
        JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (javaClass != null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);

                    // add implementation clause
                    //ClassTree modifiedClass = genUtils.addImplementsClause(javaClass, port.getJavaName());

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
                        TypeElement bindingElement = workingCopy.getElements().getTypeElement(BINDING_TYPE_ANNOTATION);




                        if (bindingElement != null) {
                            List<ExpressionTree> bindingAttrs = new ArrayList<ExpressionTree>();
                            bindingAttrs.add(make.Assignment(make.Identifier("value"), //NOI18N
                                    make.Identifier(OLD_SOAP12_NAMESPACE))); //NOI18N
                            AnnotationTree bindingAnnotation = make.Annotation(
                                    make.QualIdent(bindingElement),
                                    bindingAttrs);
                            modifiedClass = genUtils.addAnnotation(modifiedClass, bindingAnnotation);
                        }
                    }

                    // add @Stateless annotation
                    if (isStatelessSB) {//EJB project
                        TypeElement StatelessAn = workingCopy.getElements().getTypeElement("javax.ejb.Stateless"); //NOI18N
                        AnnotationTree StatelessAnnotation = make.Annotation(
                                make.QualIdent(StatelessAn),
                                Collections.<ExpressionTree>emptyList());
                        modifiedClass = genUtils.addAnnotation(modifiedClass, StatelessAnnotation);
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
                }
            }

            public void cancel() {
            }
        };
        targetSource.runModificationTask(task).commit();

        //open in editor
        DataObject dobj = DataObject.find(implClassFo);
        openFileInEditor(dobj);
    }

    public static void openFileInEditor(DataObject dobj) {

        final OpenCookie openCookie = dobj.getCookie(OpenCookie.class);
        if (openCookie != null) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    openCookie.open();
                }
            }, 1000);
        } else {
            final EditorCookie ec = dobj.getCookie(EditorCookie.class);
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    ec.open();
                }
            }, 1000);
        }
    }

    public static String getPackageName(String fullyQualifiedName) {
        String packageName = "";
        int index = fullyQualifiedName.lastIndexOf(".");
        if (index != -1) {
            packageName = fullyQualifiedName.substring(0, index);
        }
        return packageName;
    }

    private static void initProjectInfo(Project project) {
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            String serverInstance = provider.getServerInstanceID();
            if (serverInstance != null) {
                try {
                    J2eePlatform j2eePlatform = Deployment.getDefault().getServerInstance(serverInstance).getJ2eePlatform();
                    WSStack<JaxWs> wsStack = JaxWsStackProvider.getJaxWsStack(j2eePlatform);
                    if (wsStack != null) {
                        jsr109Supported = wsStack.isFeatureSupported(JaxWs.Feature.JSR109);

                    }
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger(JaxWsUtils.class.getName()).log(Level.INFO, "Failed to find J2eePlatform", ex);
                }
            }
            J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
            if (J2eeModule.Type.EJB.equals(moduleType)) {
                projectType = ProjectInfo.EJB_PROJECT_TYPE;
            } else if (J2eeModule.Type.WAR.equals(moduleType)) {
                projectType = ProjectInfo.WEB_PROJECT_TYPE;
            } else if (J2eeModule.Type.CAR.equals(moduleType)) {
                projectType = ProjectInfo.CAR_PROJECT_TYPE;
            } else {
                projectType = ProjectInfo.JSE_PROJECT_TYPE;
            }
        } else {
            projectType = ProjectInfo.JSE_PROJECT_TYPE;
        }
    }

    public static boolean isProjectReferenceable(Project clientProject, Project targetProject) {
        if (clientProject == targetProject) {
            return true;
        } else {
            AntArtifactProvider antArtifactProvider = clientProject.getLookup().lookup(AntArtifactProvider.class);
            if (antArtifactProvider != null) {
                AntArtifact jarArtifact = getJarArtifact(antArtifactProvider);
                if (jarArtifact != null) {
                    return true;
                }
            }
            return false;
        }
    }

    /** Adding clientProject reference to targetProject
     * 
     */
    public static boolean addProjectReference(Project clientProject, FileObject targetFile) {
        try {
            assert clientProject != null && targetFile != null;
            Project targetProject = FileOwnerQuery.getOwner(targetFile);
            if (clientProject != targetProject) {
                AntArtifactProvider antArtifactProvider = clientProject.getLookup().lookup(AntArtifactProvider.class);
                if (antArtifactProvider != null) {
                    AntArtifact jarArtifact = getJarArtifact(antArtifactProvider);
                    if (jarArtifact != null) {
                        AntArtifact[] jarArtifacts = new AntArtifact[]{jarArtifact};
                        URI[] artifactsUri = jarArtifact.getArtifactLocations();
                        ProjectClassPathModifier.addAntArtifacts(jarArtifacts, artifactsUri, targetFile, ClassPath.COMPILE);
                        return true;
                    }
                }
            } else {
                return true;
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().log(ex.getLocalizedMessage());
        }
        return false;
    }

    private static AntArtifact getJarArtifact(AntArtifactProvider antArtifactProvider) {
        AntArtifact[] artifacts = antArtifactProvider.getBuildArtifacts();
        for (int i = 0; i < artifacts.length; i++) {
            if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(artifacts[i].getType())) {
                return artifacts[i];
            }
        }
        return null;
    }

    public static class WsImportServiceFailedMessage extends NotifyDescriptor.Message {

        public WsImportServiceFailedMessage(Throwable ex) {
            super(NbBundle.getMessage(JaxWsUtils.class, "TXT_CannotGenerateService", ex.getLocalizedMessage()),
                    NotifyDescriptor.ERROR_MESSAGE);
        }
    }

    public static class WsImportClientFailedMessage extends NotifyDescriptor.Message {

        public WsImportClientFailedMessage(Throwable ex) {
            super(NbBundle.getMessage(JaxWsUtils.class, "TXT_CannotGenerateClient", ex.getLocalizedMessage()),
                    NotifyDescriptor.ERROR_MESSAGE);
        }
    }

    /**
     * Utility for changing the wsdlLocation attribute in external JAXWS external files
     * @param bindingFile FileObject of the external binding file
     * @param relativePath String representing the relative path to the wsdl
     * @return true if modification succeeded, false otherwise.
     */
    public static boolean addRelativeWsdlLocation(FileObject bindingFile, String relativePath) {
        GlobalBindings gb = null;

        ModelSource ms = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(bindingFile, true);
        if (ms != null) {
            BindingsModel bindingsModel = BindingsModelFactory.getDefault().getModel(ms);
            if (bindingsModel != null) {
                gb = bindingsModel.getGlobalBindings();
                if (gb != null) {
                    bindingsModel.startTransaction();
                    gb.setWsdlLocation(relativePath);
                    bindingsModel.endTransaction();
                    return true;
                }
            }
        }
        return false;
    }

    /** Package name validation
     */
    public static boolean isJavaPackage(String pkg) {
        boolean result = false;
        StringTokenizer tukac = new StringTokenizer(pkg, ".", true);
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if (".".equals(token)) {
                if (result) {
                    result = false;
                } else {
                    return false;
                }
            } else {
                if (!Utilities.isJavaIdentifier(token)) {
                    return false;
                }
                result = true;
            }
        }

        return result;
    }

    /** Class/Identifier validation
     */
    public static boolean isJavaIdentifier(String id) {
        boolean result = true;

        if (id == null || id.length() == 0 || !Character.isJavaIdentifierStart(id.charAt(0))) {
            result = false;
        } else {
            for (int i = 1, idlength = id.length(); i < idlength; i++) {
                if (!Character.isJavaIdentifierPart(id.charAt(i))) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /** This method ensures the list of steps displayed in the left hand panel
     *  of the wizard is correct for any given displayed panel.
     *
     *  Taken from web/core
     */
    public static String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[(before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }

    public static boolean isEjbJavaEE5orHigher(Project project) {
        ProjectInfo projectInfo = new ProjectInfo(project);
        return isEjbJavaEE5orHigher(projectInfo);
    }

    public static boolean isEjbJavaEE5orHigher(ProjectInfo projectInfo) {
        int projType = projectInfo.getProjectType();
        if (projType == ProjectInfo.EJB_PROJECT_TYPE) {
            FileObject ddFolder = JAXWSSupport.getJAXWSSupport(projectInfo.getProject().getProjectDirectory()).getDeploymentDescriptorFolder();
            if (ddFolder == null || ddFolder.getFileObject("ejb-jar.xml") == null) { //NOI18N
                return true;
            }
        }
        return false;
    }

    public static boolean isCarProject(Project project) {
        ProjectInfo projectInfo = new ProjectInfo(project);
        return isCarProject(projectInfo);
    }

    public static boolean isCarProject(ProjectInfo projectInfo) {
        int projType = projectInfo.getProjectType();
        return projType == ProjectInfo.CAR_PROJECT_TYPE;
    }

    /** Setter for WebService annotation attribute, e.g. serviceName = "HelloService"
     *
     */
    public static void setWebServiceAttrValue(FileObject implClassFo, final String attrName, final String attrValue) {
        final JavaSource javaSource = JavaSource.forFileObject(implClassFo);
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree classTree = SourceUtils.getPublicTopLevelTree(workingCopy);
                if (classTree != null) {
                    TreeMaker make = workingCopy.getTreeMaker();
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);

                    ExpressionTree attrExpr =
                            (attrValue == null ? null : genUtils.createAnnotationArgument(attrName, attrValue));

                    ModifiersTree modif = classTree.getModifiers();
                    List<? extends AnnotationTree> annotations = modif.getAnnotations();
                    List<AnnotationTree> newAnnotations = new ArrayList<AnnotationTree>();

                    for (AnnotationTree an : annotations) {
                        IdentifierTree ident = (IdentifierTree) an.getAnnotationType();
                        TreePath anTreePath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), ident);
                        TypeElement anElement = (TypeElement) workingCopy.getTrees().getElement(anTreePath);
                        if (anElement != null && anElement.getQualifiedName().contentEquals("javax.jws.WebService")) { //NOI18N
                            List<? extends ExpressionTree> expressions = an.getArguments();
                            List<ExpressionTree> newExpressions = new ArrayList<ExpressionTree>();
                            boolean attrFound = false;
                            for (ExpressionTree expr : expressions) {
                                AssignmentTree as = (AssignmentTree) expr;
                                IdentifierTree id = (IdentifierTree) as.getVariable();
                                if (id.getName().contentEquals(attrName)) {
                                    attrFound = true;
                                    if (attrExpr != null) {
                                        newExpressions.add(attrExpr);
                                    }
                                } else {
                                    newExpressions.add(expr);
                                }
                            }
                            if (!attrFound) {
                                newExpressions.add(attrExpr);
                            }

                            TypeElement webServiceEl = workingCopy.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
                            AnnotationTree webServiceAn = make.Annotation(make.QualIdent(webServiceEl), newExpressions);
                            newAnnotations.add(webServiceAn);
                        } else {
                            newAnnotations.add(an);
                        }
                    }

                    ModifiersTree newModifier = make.Modifiers(modif, newAnnotations);
                    workingCopy.rewrite(modif, newModifier);
                }
            }

            public void cancel() {
            }
        };
        try {
            javaSource.runModificationTask(modificationTask).commit();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    private boolean resolveServiceUrl(Object moduleType, CompilationController controller, TypeElement targetElement, TypeElement wsElement, String[] serviceName, String[] name) throws IOException {
        boolean foundWsAnnotation = false;
        List<? extends AnnotationMirror> annotations = targetElement.getAnnotationMirrors();
        for (AnnotationMirror anMirror : annotations) {
            if (controller.getTypes().isSameType(wsElement.asType(), anMirror.getAnnotationType())) {
                foundWsAnnotation = true;
                Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : expressions.entrySet()) {
                    if (entry.getKey().getSimpleName().contentEquals("serviceName")) {
                        serviceName[0] = (String) expressions.get(entry.getKey()).getValue();
                        if (serviceName[0] != null) {
                            serviceName[0] = URLEncoder.encode(serviceName[0], "UTF-8"); //NOI18N
                        }
                    } else if (entry.getKey().getSimpleName().contentEquals("name")) {
                        name[0] = (String) expressions.get(entry.getKey()).getValue();
                        if (name[0] != null) {
                            name[0] = URLEncoder.encode(name[0], "UTF-8");
                        }
                    }
                    if (serviceName[0] != null && name[0] != null) {
                        break;
                    }
                }
                break;
            } // end if
        } // end for
        return foundWsAnnotation;
    }

    public static boolean isSoap12(FileObject implClassFo) {
        final JavaSource javaSource = JavaSource.forFileObject(implClassFo);
        final String[] version = new String[1];
        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {

            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
                boolean foundAnnotation = false;
                TypeElement bindingElement = controller.getElements().getTypeElement(BINDING_TYPE_ANNOTATION);
                for (AnnotationMirror anMirror : annotations) {
                    if (controller.getTypes().isSameType(bindingElement.asType(), anMirror.getAnnotationType())) {
                        Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : expressions.entrySet()) {
                            if (entry.getKey().getSimpleName().contentEquals("value")) {   //NOI18N
                                version[0] = (String)entry.getValue().getValue();
                                foundAnnotation = true;
                                break;
                            }
                        }

                    }
                    if (foundAnnotation) {
                        break;
                    }
                }
            }

            public void cancel() {
            }
        };
        try {
            javaSource.runUserActionTask(task, true);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return version[0] != null &&
                (SOAP12_NAMESPACE.equals(version[0]) || OLD_SOAP12_NAMESPACE.equals(version[0]));
    }

    public static void setSOAP12Binding(final FileObject implClassFo, final boolean isSOAP12) {
        final JavaSource javaSource = JavaSource.forFileObject(implClassFo);
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(workingCopy);
                ClassTree javaClass = workingCopy.getTrees().getTree(typeElement);

                TypeElement bindingElement = workingCopy.getElements().getTypeElement(BINDING_TYPE_ANNOTATION);
                if (bindingElement != null) {
                    AnnotationTree bindingAnnotation = null;
                    List<? extends AnnotationTree> annots = javaClass.getModifiers().getAnnotations();
                    for (AnnotationTree an : annots) {
                        Tree ident = an.getAnnotationType();
                        TreePath anTreePath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), ident);
                        TypeElement anElement = (TypeElement) workingCopy.getTrees().getElement(anTreePath);
                        if (anElement != null && anElement.getQualifiedName().contentEquals(BINDING_TYPE_ANNOTATION)) {
                            bindingAnnotation = an;
                            break;
                        }
                    }
                    if (isSOAP12 && bindingAnnotation == null) {

                        ModifiersTree modifiersTree = javaClass.getModifiers();

                        AssignmentTree soapVersion = make.Assignment(make.Identifier("value"), make.Literal(OLD_SOAP12_NAMESPACE)); //NOI18N
                        AnnotationTree soapVersionAnnotation = make.Annotation(
                                make.QualIdent(bindingElement),
                                Collections.<ExpressionTree>singletonList(soapVersion));

                        ModifiersTree newModifiersTree = make.addModifiersAnnotation(modifiersTree, soapVersionAnnotation);

                        workingCopy.rewrite(modifiersTree, newModifiersTree);
                    } else if (!isSOAP12 && bindingAnnotation != null) {
                        ModifiersTree modifiers = javaClass.getModifiers();
                        ModifiersTree newModifiers = make.removeModifiersAnnotation(modifiers, bindingAnnotation);
                        workingCopy.rewrite(modifiers, newModifiers);
                        CompilationUnitTree compileUnitTree = workingCopy.getCompilationUnit();
                        List<? extends ImportTree> imports = compileUnitTree.getImports();
                        for (ImportTree imp : imports) {
                            Tree impTree = imp.getQualifiedIdentifier();
                            TreePath impTreePath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), impTree);
                            TypeElement impElement = (TypeElement) workingCopy.getTrees().getElement(impTreePath);
                            if (impElement != null && impElement.getQualifiedName().contentEquals(BINDING_TYPE_ANNOTATION)) {
                                CompilationUnitTree newCompileUnitTree = make.removeCompUnitImport(compileUnitTree, imp);
                                workingCopy.rewrite(compileUnitTree, newCompileUnitTree);
                                break;
                            }
                        }

                    }
                }
            }

            public void cancel() {
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    try {
                        javaSource.runModificationTask(modificationTask).commit();
                        saveFile(implClassFo);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            });
        } else {
            try {
                javaSource.runModificationTask(modificationTask).commit();
                saveFile(implClassFo);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

    private static void saveFile(FileObject file) throws IOException {
        DataObject dataObject = DataObject.find(file);
        if (dataObject != null) {
            SaveCookie cookie = dataObject.getCookie(SaveCookie.class);
            if (cookie != null) {
                cookie.save();
            }
        }
    }

    /** Setter for WebMethod annotation attribute, e.g. operationName = "HelloOperation"
     *
     */
    public static void setWebMethodAttrValue(FileObject implClassFo, final ElementHandle method,
            final String attrName,
            final String attrValue) {
        final JavaSource javaSource = JavaSource.forFileObject(implClassFo);
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                Element methodEl = method.resolve(workingCopy);
                if (methodEl != null) {
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    if (genUtils != null) {
                        TreeMaker make = workingCopy.getTreeMaker();

                        ExpressionTree attrExpr =
                                (attrValue == null ? null : genUtils.createAnnotationArgument(attrName, attrValue));

                        MethodTree methodTree = (MethodTree) workingCopy.getTrees().getTree(methodEl);

                        ModifiersTree modif = methodTree.getModifiers();
                        List<? extends AnnotationTree> annotations = modif.getAnnotations();
                        List<AnnotationTree> newAnnotations = new ArrayList<AnnotationTree>();

                        boolean foundWebMethodAn = false;

                        for (AnnotationTree an : annotations) {
                            IdentifierTree ident = (IdentifierTree) an.getAnnotationType();
                            TreePath anTreePath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), ident);
                            TypeElement anElement = (TypeElement) workingCopy.getTrees().getElement(anTreePath);
                            if (anElement != null && anElement.getQualifiedName().contentEquals("javax.jws.WebMethod")) { //NOI18N
                                foundWebMethodAn = true;
                                List<? extends ExpressionTree> expressions = an.getArguments();
                                List<ExpressionTree> newExpressions = new ArrayList<ExpressionTree>();
                                boolean attrFound = false;
                                for (ExpressionTree expr : expressions) {
                                    AssignmentTree as = (AssignmentTree) expr;
                                    IdentifierTree id = (IdentifierTree) as.getVariable();
                                    if (id.getName().contentEquals(attrName)) {
                                        attrFound = true;
                                        if (attrExpr != null) {
                                            newExpressions.add(attrExpr);
                                        }

                                    } else {
                                        newExpressions.add(expr);
                                    }

                                }
                                if (!attrFound) {
                                    newExpressions.add(attrExpr);
                                }

                                TypeElement webMethodEl = workingCopy.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
                                AnnotationTree webMethodAn = make.Annotation(make.QualIdent(webMethodEl), newExpressions);
                                newAnnotations.add(webMethodAn);
                            } else {
                                newAnnotations.add(an);
                            }

                        }

                        if (!foundWebMethodAn && attrExpr != null) {
                            TypeElement webMethodEl = workingCopy.getElements().getTypeElement("javax.jws.WebMethod"); //NOI18N
                            AnnotationTree webMethodAn = make.Annotation(
                                    make.QualIdent(webMethodEl),
                                    Collections.<ExpressionTree>singletonList(attrExpr));
                            newAnnotations.add(webMethodAn);
                        }

                        ModifiersTree newModifier = make.Modifiers(modif, newAnnotations);
                        workingCopy.rewrite(modif, newModifier);
                    }

                }
            }

            public void cancel() {
            }
        };
        try {
            javaSource.runModificationTask(modificationTask).commit();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }

    }

    /** Setter for WebParam annotation attribute, e.g. name = "x"
     *
     */
    public static void setWebParamAttrValue(FileObject implClassFo, final ElementHandle methodHandle,
            final String paramName,
            final String attrName,
            final String attrValue) {
        final JavaSource javaSource = JavaSource.forFileObject(implClassFo);
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                Element methodEl = methodHandle.resolve(workingCopy);
                if (methodEl != null) {
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                    if (genUtils != null) {
                        TreeMaker make = workingCopy.getTreeMaker();

                        ExpressionTree attrExpr =
                                (attrValue == null ? null : genUtils.createAnnotationArgument(attrName, attrValue));

                        MethodTree methodTree = (MethodTree) workingCopy.getTrees().getTree(methodEl);
                        List<? extends VariableTree> parameters = methodTree.getParameters();

                        for (VariableTree paramTree : parameters) {
                            if (paramTree.getName().contentEquals(paramName)) {
                                ModifiersTree modif = paramTree.getModifiers();
                                List<? extends AnnotationTree> annotations = modif.getAnnotations();
                                List<AnnotationTree> newAnnotations = new ArrayList<AnnotationTree>();

                                boolean foundWebParamAn = false;

                                for (AnnotationTree an : annotations) {
                                    IdentifierTree ident = (IdentifierTree) an.getAnnotationType();
                                    TreePath anTreePath = workingCopy.getTrees().getPath(workingCopy.getCompilationUnit(), ident);
                                    TypeElement anElement = (TypeElement) workingCopy.getTrees().getElement(anTreePath);
                                    if (anElement != null && anElement.getQualifiedName().contentEquals("javax.jws.WebParam")) { //NOI18N
                                        foundWebParamAn = true;
                                        List<? extends ExpressionTree> expressions = an.getArguments();
                                        List<ExpressionTree> newExpressions = new ArrayList<ExpressionTree>();
                                        boolean attrFound = false;
                                        for (ExpressionTree expr : expressions) {
                                            AssignmentTree as = (AssignmentTree) expr;
                                            IdentifierTree id = (IdentifierTree) as.getVariable();
                                            if (id.getName().contentEquals(attrName)) {
                                                attrFound = true;
                                                if (attrExpr != null) {
                                                    newExpressions.add(attrExpr);
                                                }

                                            } else {
                                                newExpressions.add(expr);
                                            }

                                        }
                                        if (!attrFound) {
                                            newExpressions.add(attrExpr);
                                        }

                                        TypeElement webParamEl = workingCopy.getElements().getTypeElement("javax.jws.WebParam"); //NOI18N
                                        AnnotationTree webParamAn = make.Annotation(make.QualIdent(webParamEl), newExpressions);
                                        newAnnotations.add(webParamAn);
                                    } else {
                                        newAnnotations.add(an);
                                    }

                                }

                                if (!foundWebParamAn && attrExpr != null) {
                                    TypeElement webParamEl = workingCopy.getElements().getTypeElement("javax.jws.WebParam"); //NOI18N
                                    AnnotationTree webParamAn = make.Annotation(
                                            make.QualIdent(webParamEl),
                                            Collections.<ExpressionTree>singletonList(attrExpr));
                                    newAnnotations.add(webParamAn);
                                }

                                ModifiersTree newModifier = make.Modifiers(modif, newAnnotations);
                                workingCopy.rewrite(modif, newModifier);
                                break;

                            }


                        }
                    }
                }
            }

            public void cancel() {
            }
        };
        try {
            javaSource.runModificationTask(modificationTask).commit();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }

    }

    /**
     * @param model the WSDL model the handler is for.
     * @return true if the WSDL model operation parameters could be set in SOAP header
     */
    public static boolean needsSoapHandler(WsdlModel model) {
        //TODO
        return false;
    }

    /**
     * Retrieve map of SOAP header element QName and its java type name.
     * @param model the WSDL model the handler is for.
     * @return map of SOAP header element QName and its java type name.
     */
    public static Map<QName, String> getSoapHandlerParameterTypes(WsdlModel model) {
        return null;
    }

    public static Map<QName, String> getSoapHandlerParameterTypes(PortType portType) {
        Map<QName, String> paramMap = new HashMap<QName, String>();

        Definitions definitions = portType.getModel().getDefinitions();
        Collection<Binding> bindings = definitions.getBindings();
        Binding binding = null;
        for (Binding b : bindings) {
            NamedComponentReference<PortType> portTypeRef = b.getType();
            if (portTypeRef.get().equals(portType)) {
                binding = b;
                break;

            }

        }
        if (binding != null) {
            //Determine if it is a SOAP binding
            List<SOAPBinding> soapBindings = binding.getExtensibilityElements(SOAPBinding.class);
            if (soapBindings.size() >
                    0) { //we can assume that this is the only SOAP binding
                Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
                for (BindingOperation bOp : bindingOperations) {
                    BindingInput bindingInput = bOp.getBindingInput();
                    Collection<SOAPHeader> headers = bindingInput.getExtensibilityElements(SOAPHeader.class);
                    for (SOAPHeader header : headers) {
                        NamedComponentReference<Message> messageRef = header.getMessage();
                        Message message = messageRef.get();
                        String partName = header.getPart();
                        Collection<Message> messages = definitions.getMessages();
                        for (Message m : messages) {
                            if (m.equals(message)) {
                                Collection<Part> parts = m.getParts();
                                for (Part part : parts) {
                                    if (part.getName().equals(partName)) {
                                        NamedComponentReference<GlobalElement> elementRef = part.getElement();
                                        if (elementRef != null) {
                                            QName qname = elementRef.getQName();
                                            if (!paramMap.containsKey(qname)) {
                                                paramMap.put(qname, "");
                                            }
                                        } else {
                                            NamedComponentReference<GlobalType> typeRef = part.getType();
                                            if (typeRef != null) {
                                                QName qname = typeRef.getQName();
                                                if (!paramMap.containsKey(qname)) {
                                                    paramMap.put(qname, "");
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return paramMap;
    }

    /**
     * Retrieve SOAP handler for given WSDL model.
     * @param model the WSDL model the handler is for.
     * @return the hanlder or null if none exist.
     */
    public static FileObject getSoapHandler(
            WsdlModel model) {
        //TODO
        return null;
    }

    public static FileObject createSoapHandler(
            FileObject dest, PortType portType, Map<QName, Object> soapHeaderValues)
            throws IOException {
        String handlerName = portType.getName() + "_handler.java";
        DataObject dataObj = createDataObjectFromTemplate(HANDLER_TEMPLATE, dest, handlerName);

        //TODO Generate code for initializing the header values.
        return dataObj.getPrimaryFile();
    }

    /**
     * Create SOAP handler for given WSDL model.
     * @param destdir destination directory.
     * @param model the WSDL model the handler is for.
     * @param soapHeaderValues values for SOAP header elements.
     */
    public static FileObject createSoapHandler(
            FileObject dest, WsdlModel model, Map<QName, Object> soapHeaderValues) {
        return null;

    }

    public static DataObject createDataObjectFromTemplate(
            String template,
            FileObject targetFolder, String targetName) throws IOException {
        assert template != null;
        assert targetFolder != null;
        assert targetName != null && targetName.trim().length() > 0;

        FileObject templateFO = FileUtil.getConfigFile(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(targetFolder);

        return templateDO.createFromTemplate(dataFolder, targetName);
    }

    public static boolean isInSourceGroup(Project prj, String serviceClass) {

        SourceGroup[] sourceGroups = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup group : sourceGroups) {
            String resource = serviceClass.replace('.', '/') + ".java"; //NOI18N
            if (group.getRootFolder().getFileObject(resource) != null) {
                return true;
            }

        }
        return false;
    }

    /** Test if EJBs are supported in J2EE Container, e.g. in Tomcat they are not
     * 
     * @param project
     * @return
     */
    public static boolean isEjbSupported(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);

        if (j2eeModuleProvider != null) {
            String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
            if (serverInstanceId == null) {
                return false;
            }
            try {
                J2eePlatform platform = Deployment.getDefault().getServerInstance(serverInstanceId).getJ2eePlatform();
                if (platform.getSupportedTypes().contains(J2eeModule.Type.EJB)) {
                    return true;
                }
            } catch (InstanceRemovedException ex) {
                Logger.getLogger(JaxWsUtils.class.getName()).log(Level.INFO, "Failed to find J2eePlatform", ex);
            }
        }
        return false;
    }

    public static boolean isRPCEncoded(URI wsdlURI) {
        try {
            FileObject wsdlFO = FileUtil.toFileObject(new File(wsdlURI));
            
            WSDLModel wsdlModel = WSDLModelFactory.getDefault().
                    getModel(org.netbeans.modules.xml.retriever.catalog.Utilities.createModelSource(wsdlFO, true));
            Definitions definitions = wsdlModel.getDefinitions();
            Collection<Binding> bindings = definitions.getBindings();
            for (Binding binding : bindings) {
                List<SOAPBinding> soapBindings = binding.getExtensibilityElements(SOAPBinding.class);
                for (SOAPBinding soapBinding : soapBindings) {
                    if (soapBinding.getStyle() == Style.RPC) {
                        Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
                        for (BindingOperation bindingOperation : bindingOperations) {
                            BindingInput bindingInput = bindingOperation.getBindingInput();
                            if (bindingInput != null) {
                                List<SOAPBody> soapBodies = bindingInput.getExtensibilityElements(SOAPBody.class);
                                if (soapBodies != null && soapBodies.size() > 0) {
                                    SOAPBody soapBody = soapBodies.get(0);
                                    if (soapBody.getUse() == Use.ENCODED) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (CatalogModelException ex) {
            Logger.global.log(Level.INFO, "", ex);
        }

        return false;
    }
    
    public static Service findServiceForServiceName(FileObject createdFile, String serviceName) {
        JAXWSSupport support = JAXWSSupport.getJAXWSSupport(createdFile);
        List services = support.getServices();
        if (services.size()>1) {
            Project prj = FileOwnerQuery.getOwner(createdFile);
            for (int i=0;i<services.size()-1;i++) { // check only formerly created services
                Service service = (Service)services.get(i);
                if (service.getWsdlUrl() != null) {
                    // from WSDL
                    if (serviceName.equals(service.getServiceName())) {
                        return service;
                    }
                } else {
                    // from Java
                    if (serviceName.equals(getServiceName(prj, service))) {
                        return service;
                    }
                }
            }
        }
        return null;
    }
    
    private static String getServiceName(Project prj, Service service) {
        SourceGroup[] srcGroups = ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject implClassFo = null;
        String implClassResource = service.getImplementationClass().replace('.', '/') + ".java"; //NOI18N
        final String[] serviceName = new String[1];
        if (srcGroups != null) {
            for (SourceGroup srcGroup: srcGroups) {
                FileObject root = srcGroup.getRootFolder();
                implClassFo = root.getFileObject(implClassResource);
                if (implClassFo != null) break;
            }
        }
        if (implClassFo != null) {
            JavaSource javaSource = JavaSource.forFileObject(implClassFo);
            if (javaSource != null) {
                CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {

                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(Phase.ELEMENTS_RESOLVED);
                        TypeElement classElement = SourceUtils.getPublicTopLevelElement(controller);
                        TypeElement wsElement = controller.getElements().getTypeElement("javax.jws.WebService"); //NOI18N
                        if (classElement != null && wsElement != null) {
                            List<? extends AnnotationMirror> annotations = classElement.getAnnotationMirrors();

                            for (AnnotationMirror anMirror : annotations) {
                                if (controller.getTypes().isSameType(wsElement.asType(), anMirror.getAnnotationType())) {
                                    Map<? extends ExecutableElement, ? extends AnnotationValue> expressions = anMirror.getElementValues();
                                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : expressions.entrySet()) {
                                        if (entry.getKey().getSimpleName().contentEquals("serviceName")) { //NOI18N
                                            serviceName[0] = (String) expressions.get(entry.getKey()).getValue();
                                        }
                                        if (serviceName[0] != null) {
                                            break;
                                        }
                                    }
                                    break;
                                } // end if
                            } // end for
                        }
                    }

                    public void cancel() {
                    }
                };
                try {
                    javaSource.runUserActionTask(task, true);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            if (serviceName[0] == null) {
                serviceName[0] = implClassFo.getName()+"Service"; //NOI18N
            }
        }
        return serviceName[0];
    }
}
