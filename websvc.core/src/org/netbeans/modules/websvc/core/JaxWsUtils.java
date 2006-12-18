/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.core;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModel;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.BindingsModelFactory;
import org.netbeans.modules.websvc.core.jaxws.bindings.model.GlobalBindings;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsUtils {
    
    private static int projectType;
    
    protected static final int JSE_PROJECT_TYPE = 0;
    protected static final int WEB_PROJECT_TYPE = 1;
    protected static final int EJB_PROJECT_TYPE = 2;
    
    private static boolean jwsdpSupported = false;
    private static boolean jsr109Supported = false;
    private static boolean wsitSupported = false;
    
    /** Creates a new instance of JaxWsUtils */
    public JaxWsUtils() {
    }
    
    /** This method is called from Refresh Service action
     */
    public static void generateJaxWsImplementationClass(Project project, FileObject targetFolder, String targetName, WsdlModel wsdlModel, org.netbeans.modules.websvc.api.jaxws.project.config.Service service) throws Exception {
        WsdlService wsdlService = wsdlModel.getServiceByName(service.getServiceName());
        WsdlPort wsdlPort = null;
        if (wsdlService != null)
            wsdlPort = wsdlService.getPortByName(service.getPortName());
        if (wsdlService!=null && wsdlPort!=null) {
            String serviceID = service.getName();
            if(wsdlPort.isProvider()){
                generateProviderImplClass(project, targetFolder, targetName, wsdlService, wsdlPort, serviceID);
            }else{
                generateJaxWsImplClass(project, targetFolder, targetName, null, wsdlService, wsdlPort, false, serviceID);
            }
        }
    }
    
    /** This method is called from Create Web Service from WSDL wizard
     */
    public static void generateJaxWsImplementationClass(Project project, FileObject targetFolder, String targetName, URL wsdlURL, WsdlService service, WsdlPort port) throws Exception {
            generateJaxWsImplClass(project, targetFolder, targetName, wsdlURL, service, port, true, null);
    }
    
    /** This method is called from Create Web Service from WSDL wizard
     */
    public static void generateJaxWsArtifacts(Project project, FileObject targetFolder, String targetName, URL wsdlURL, String service, String port) throws Exception {
        initProjectInfo(project);
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        String artifactsPckg =  "service."+targetName.toLowerCase(); //NOI18N
        ClassPath classPath = ClassPath.getClassPath(targetFolder, ClassPath.SOURCE);
        String serviceImplPath = classPath.getResourceName(targetFolder, '.', false);
        jaxWsSupport.addService(targetName, serviceImplPath+"."+targetName, wsdlURL.toExternalForm(), service, port, artifactsPckg, jsr109Supported && Util.isJavaEE5orHigher(project));
    }
    
    
    public static void generateProviderImplClass(Project project, FileObject targetFolder,
            String targetName, WsdlService service, WsdlPort port, String serviceID) throws Exception{
// Retouche        
//        initProjectInfo(project);
//        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
//        String wsdlLocation = jaxWsSupport.getWsdlLocation(serviceID);
//        JavaClass javaClass = null;
//        JavaModel.getJavaRepository().beginTrans(true);
//        
//        try{
//            javaClass = JMIGenerationUtil.createClass(targetFolder, targetName);
//            //Initially, Provider<Source> will be implemented. The user can then change the Provider type if he/she wishes
//            JMIUtils.addInterface(javaClass, "javax.xml.ws.Provider<Source>"); //NOI18N 
//            
//            if (projectType == EJB_PROJECT_TYPE) {//EJB project
//                Annotation statelessAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.ejb.Stateless", Collections.EMPTY_LIST); //NOI18N
//                javaClass.getAnnotations().add(statelessAnnotation);
//            }
//            
//            //Initially, set mode to PAYLOAD. The user can then change if he/she wishes
//            AttributeValue serviceModeValue = JMIGenerationUtil.createAttributeValue(javaClass, "value", "javax.xml.ws.Service.Mode", "PAYLOAD"); //NOI18N
//            ArrayList attrList = new ArrayList();
//            attrList.add(serviceModeValue);
//            Annotation serviceModeAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.xml.ws.ServiceMode", attrList); //NOI18N
//            javaClass.getAnnotations().add(serviceModeAnnotation);
//            
//            AttributeValue wsdlLocationValue = JMIGenerationUtil.createAttributeValue(javaClass, "wsdlLocation", wsdlLocation );
//            AttributeValue serviceNameAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "serviceName", service.getName()); //NOI18N
//            AttributeValue targetNamespaceAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "targetNamespace", port.getNamespaceURI()); //NOI18N
//            AttributeValue portNameAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "portName", port.getName()); //NOI18N
//            attrList = new ArrayList();
//            attrList.add(wsdlLocationValue);
//            attrList.add(serviceNameAttibuteValue);
//            attrList.add(targetNamespaceAttibuteValue);
//            attrList.add(portNameAttibuteValue);
//            Annotation serviceProviderAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.xml.ws.WebServiceProvider", attrList); //NOI18N
//            javaClass.getAnnotations().add(serviceProviderAnnotation);
//            String returnType = "javax.xml.transform.Source";  //NOI18N
//            String operationName = "invoke";   //NOI18N
//            Method op = JMIGenerationUtil.createMethod(javaClass, operationName, Modifier.PUBLIC, returnType);
//            Parameter param = JMIGenerationUtil.createParameter(javaClass, "source", returnType);  //NOI18N
//            op.getParameters().add(param);
//            op.setBodyText("//TODO implement this method\nreturn null;");  //NOI18N
//            javaClass.getFeatures().add(op);
//        }finally{
//            JavaModel.getJavaRepository().endTrans();
//        }
//        
//        FileObject fo = JavaModel.getFileObject(javaClass.getResource());
//        //open in the editor
//        DataObject dobj = DataObject.find(fo);
//        openFileInEditor(dobj);
    }
    
    private static void generateJaxWsImplClass(Project project, FileObject targetFolder, String targetName, URL wsdlURL, final WsdlService service, final WsdlPort port, boolean addService, String serviceID) throws Exception {
        initProjectInfo(project);
        
        // Use Progress API to display generator messages.
        //ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(JaxWsUtils.class, "TXT_WebServiceGeneration")); //NOI18N
        //handle.start(100);
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());

        FileObject implClassFo = GenerationUtils.createClass(targetFolder, targetName, null);
        
        ClassPath classPath = ClassPath.getClassPath(implClassFo, ClassPath.SOURCE);
        String serviceImplPath = classPath.getResourceName(implClassFo, '.', false);
        String portJavaName = port.getJavaName();
        String artifactsPckg = portJavaName.substring(0, portJavaName.lastIndexOf("."));
        if (addService) {
            serviceID = jaxWsSupport.addService(targetName, serviceImplPath, wsdlURL.toString(), service.getName(), port.getName(), artifactsPckg, jsr109Supported && Util.isJavaEE5orHigher(project));
        }
        
        final String wsdlLocation = jaxWsSupport.getWsdlLocation(serviceID);
        JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        CancellableTask task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                if (genUtils!=null) {     
                    TreeMaker make = workingCopy.getTreeMaker();
                    ClassTree javaClass = genUtils.getClassTree();
                    
                    // add implementation clause
                    ClassTree modifiedClass = genUtils.addImplementsClause(javaClass, port.getJavaName());
                    
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
                        attrs
                    );
                    modifiedClass = genUtils.addAnnotation(modifiedClass, WSAnnotation);
                        
                    // add @Stateless annotation
                    if (projectType == EJB_PROJECT_TYPE) {//EJB project
                        TypeElement StatelessAn = workingCopy.getElements().getTypeElement("javax.ejb.Stateless"); //NOI18N                   
                        AnnotationTree StatelessAnnotation = make.Annotation(
                            make.QualIdent(StatelessAn), 
                            Collections.<ExpressionTree>emptyList()
                        );
                        modifiedClass = genUtils.addAnnotation(modifiedClass, StatelessAnnotation);
                    }
                    
                    List<WsdlOperation> operations = port.getOperations();
                    for(WsdlOperation operation: operations) {
                        
                        // return type
                       String returnType = operation.getReturnTypeName();
                        
                        // create parameters
                        List<WsdlParameter> parameters = operation.getParameters();
                        List<VariableTree> params = new ArrayList<VariableTree>();
                        for (WsdlParameter parameter:parameters) {
                            // create parameter:
                            // final ObjectOutput arg0
                            params.add(make.Variable(
                                    make.Modifiers(
                                        Collections.<Modifier>emptySet(),
                                        Collections.<AnnotationTree>emptyList()
                                    ),
                                    parameter.getName(), // name
                                    make.Identifier(parameter.getTypeName()), // parameter type
                                    null // initializer - does not make sense in parameters.
                            ));
                        }
                        
                        // create exceptions
                        List<String> exceptions = operation.getExceptions();
                        List<ExpressionTree> exc = new ArrayList<ExpressionTree>();
                        for (String exception:exceptions) {
                            TypeElement excEl = workingCopy.getElements().getTypeElement(exception);
                            exc.add(make.QualIdent(excEl));
                        }

                        // create method
                        ModifiersTree methodModifiers = make.Modifiers(
                            Collections.<Modifier>singleton(Modifier.PUBLIC),
                            Collections.<AnnotationTree>emptyList()
                        );
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
                        
                        modifiedClass =  make.addClassMember(modifiedClass, method);
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
        
// Retouche        
//        JavaClass javaClass = null;
//        boolean rollback = true;
//        
//        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
//        
//        JavaModel.getJavaRepository().beginTrans(true);
//        try {
//            javaClass = JMIGenerationUtil.createClass(targetFolder, targetName);
//            JMIUtils.addInterface(javaClass, port.getJavaName());
//            
//            if (projectType == EJB_PROJECT_TYPE) {//EJB project
//                Annotation statelessAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.ejb.Stateless", Collections.EMPTY_LIST); //NOI18N
//                javaClass.getAnnotations().add(statelessAnnotation);
//            }
//            
//            List operations = port.getOperations();
//            if (operations != null) {
//                for (int i = 0; i < operations.size(); i++) {
//                    WsdlOperation operation = (WsdlOperation) operations.get(i);
//                    List parameters = operation.getParameters();
//                    
//                    String returnType = operation.getReturnTypeName();
//                    List exceptions = operation.getExceptions();
//                    Method op = JMIGenerationUtil.createMethod(javaClass, operation.getJavaName(), Modifier.PUBLIC, returnType);
//                    JMIUtils.addExceptions(op, exceptions);
//                    if (parameters != null) {
//                        for (int j = 0; j < parameters.size(); j++) {
//                            WsdlParameter parameter = (WsdlParameter) parameters.get(j);
//                            Parameter param = JMIGenerationUtil.createParameter(javaClass, parameter.getName(), parameter.getTypeName());
//                            op.getParameters().add(param);
//                        }
//                    }
//                    
//                    Type type = JavaModel.getDefaultExtent().getType().resolve(returnType);
//                    op.setBodyText(createBody(type));
//                    
//                    javaClass.getFeatures().add(op);
//                    
//                }
//            }
//            
//            rollback = false;
//        } finally {
//            JavaModel.getJavaRepository().endTrans(rollback);
//        }
//        
//        FileObject fo = javaClass == null ? null : JavaModel.getFileObject(javaClass.getResource());
//        ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//        String serviceImplPath = classPath.getResourceName(fo, '.', false);
//        String portJavaName = port.getJavaName();
//        String artifactsPckg = portJavaName.substring(0, portJavaName.lastIndexOf("."));
//        
//        if (addService) {
//            serviceID = jaxWsSupport.addService(targetName, serviceImplPath, wsdlURL.toString(), service.getName(), port.getName(), artifactsPckg, jsr109Supported && Util.isJavaEE5orHigher(project));
//        }
//        String wsdlLocation = jaxWsSupport.getWsdlLocation(serviceID);
//        
//        //add wsdlLocation attribute
//        rollback = true;
//        JavaModel.getJavaRepository().beginTrans(true);
//        try {
//            AttributeValue serviceNameAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "serviceName", service.getName()); //NOI18N
//            AttributeValue portNameAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "portName", port.getName()); //NOI18N
//            AttributeValue endPointInterfaceAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "endpointInterface", port.getJavaName()); //NOI18N
//            AttributeValue targetNamespaceAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "targetNamespace", port.getNamespaceURI()); //NOI18N
//            AttributeValue wsdlLocationAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "wsdlLocation", wsdlLocation); //NOI18N
//            
//            List attributes = new LinkedList();
//            attributes.add(serviceNameAttibuteValue);
//            attributes.add(portNameAttibuteValue);
//            attributes.add(endPointInterfaceAttibuteValue);
//            attributes.add(targetNamespaceAttibuteValue);
//            attributes.add(wsdlLocationAttibuteValue);
//            Annotation wsAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.jws.WebService", attributes); //NOI18N
//            javaClass.getAnnotations().add(wsAnnotation);
//            
//            // add @javax.xml.ws.BindingType annotation for SOAP12 binding
//            if (port.getSOAPVersion().equals(WsdlPort.SOAP_VERSION_12)) {
//                AttributeValue bindingAttibuteValue = JMIGenerationUtil.createAttributeValue(javaClass, "value", WsdlPort.SOAP_VERSION_12); //NOI18N
//                attributes.clear();
//                attributes.add(bindingAttibuteValue);
//                Annotation bindingAnnotation = JMIGenerationUtil.createAnnotation(javaClass, "javax.xml.ws.BindingType", attributes); //NOI18N
//                javaClass.getAnnotations().add(bindingAnnotation);
//            }
//            
//            rollback = false;
//        } finally {
//            JavaModel.getJavaRepository().endTrans(rollback);
//        }
//        
//        //open in the editor
//        DataObject dobj = DataObject.find(fo);
//        openFileInEditor(dobj);
//        
//        //handle.finish();
    }
    
    private static void openFileInEditor(DataObject dobj){
        final EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                ec.open();
            }
        }, 1000);
    }

// Retouche
//    private static String createBody(Type type) {
//        String initVal;
//        
//        if (type instanceof PrimitiveType) {
//            PrimitiveTypeKind primitiveType = ((PrimitiveType) type).getKind();
//            if (PrimitiveTypeKindEnum.BOOLEAN.equals(primitiveType)) {
//                initVal = "false"; // NOI18N
//            } else if (PrimitiveTypeKindEnum.CHAR.equals(primitiveType)) {
//                initVal = "'\\0'"; // NOI18N
//            } else if (PrimitiveTypeKindEnum.VOID.equals(primitiveType)) {
//                return "throw new UnsupportedOperationException(\"Not yet implemented\");"; // NOI18N
//            } else {
//                initVal = "0"; // NOI18N
//            }
//        } else if (type instanceof ClassDefinition) {
//            initVal = "null"; // NOI18N
//        } else {
//            throw new IllegalArgumentException("Type "+type.getClass()); // NOI18N
//        }
//        return "return ".concat(initVal).concat(";"); // NOI18N
//    }
    
    private static void initProjectInfo(Project project) {
        JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss != null) {
            Map properties = wss.getAntProjectHelper().getStandardPropertyEvaluator().getProperties();
            String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
            if (serverInstance != null) {
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                if (j2eePlatform != null) {
                    jwsdpSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP); //NOI18N
                    jsr109Supported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
                    wsitSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT);
                }
            }
        }
        
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        EjbJar em = EjbJar.getEjbJar(project.getProjectDirectory());
        if (em != null)
            projectType = EJB_PROJECT_TYPE;
        else if (wm != null)
            projectType = WEB_PROJECT_TYPE;
        else
            projectType = JSE_PROJECT_TYPE;
    }
    
    public static boolean isProjectReferenceable(Project clientProject, Project targetProject) {
        if (clientProject==targetProject) {
            return true;
        } else {
            ProjectClassPathExtender pce = (ProjectClassPathExtender)targetProject.getLookup().lookup(ProjectClassPathExtender.class);
            AntArtifactProvider antArtifactProvider = (AntArtifactProvider)clientProject.getLookup().lookup(AntArtifactProvider.class);
            if (antArtifactProvider!=null) {
                AntArtifact jarArtifact = getJarArtifact(antArtifactProvider);
                if (jarArtifact!=null) return true;
            }
            return false;
        }
    }
    
    public static boolean addProjectReference(Project clientProject, Project targetProject) {
        try {
            assert clientProject!=null && targetProject!=null;
            if (clientProject!=targetProject) {
                ProjectClassPathExtender pce = (ProjectClassPathExtender)targetProject.getLookup().lookup(ProjectClassPathExtender.class);
                AntArtifactProvider antArtifactProvider = (AntArtifactProvider)clientProject.getLookup().lookup(AntArtifactProvider.class);
                if (antArtifactProvider!=null) {
                    AntArtifact jarArtifact = getJarArtifact(antArtifactProvider);
                    if (jarArtifact!=null) {
                        URI[] artifactsUri = jarArtifact.getArtifactLocations();
                        for (int i=0;i<artifactsUri.length;i++) {
                            pce.addAntArtifact(jarArtifact,artifactsUri[i]);
                        }
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
        for (int i=0;i<artifacts.length;i++) {
            if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(artifacts[i].getType())) return artifacts[i];
        }
        return null;
    }
    
    public static class WsImportServiceFailedMessage extends NotifyDescriptor.Message {
        public WsImportServiceFailedMessage(Throwable ex) {
            super(NbBundle.getMessage(JaxWsUtils.class,"TXT_CannotGenerateService",ex.getLocalizedMessage()),
                    NotifyDescriptor.ERROR_MESSAGE);
        }
    }
    
    public static class WsImportClientFailedMessage extends NotifyDescriptor.Message {
        public WsImportClientFailedMessage(Throwable ex) {
            super(NbBundle.getMessage(JaxWsUtils.class,"TXT_CannotGenerateClient",ex.getLocalizedMessage()),
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
            if(ms != null){
                BindingsModel bindingsModel =  BindingsModelFactory.getDefault().getModel(ms);
                if(bindingsModel != null){
                    gb = bindingsModel.getGlobalBindings();
                    if(gb != null){
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
        
        if(pkg != null && pkg.length() > 0) {
            int state = 0;
            for(int i = 0, pkglength = pkg.length(); i < pkglength && state < 2; i++) {
                switch(state) {
                    case 0:
                        if(Character.isJavaIdentifierStart(pkg.charAt(i))) {
                            state = 1;
                        } else {
                            state = 2;
                        }
                        break;
                    case 1:
                        if(pkg.charAt(i) == '.') {
                            state = 0;
                        } else if(!Character.isJavaIdentifierPart(pkg.charAt(i))) {
                            state = 2;
                        }
                        break;
                }
            }
            
            if(state == 1) {
                result = true;
            }
        }
        
        return result;
    }
    
    /** Class/Identifier validation
     */
    public static boolean isJavaIdentifier(String id) {
        boolean result = true;
        
        if(id == null || id.length() == 0 || !Character.isJavaIdentifierStart(id.charAt(0))) {
            result = false;
        } else {
            for(int i = 1, idlength = id.length(); i < idlength; i++) {
                if(!Character.isJavaIdentifierPart(id.charAt(i))) {
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
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
}
