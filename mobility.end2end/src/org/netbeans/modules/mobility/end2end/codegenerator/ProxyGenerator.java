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

/*
 * ProxyGenerator.java
 *
 * Created on August 12, 2005, 11:35 AM
 *
 */
package org.netbeans.modules.mobility.end2end.codegenerator;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
//import org.netbeans.api.mdr.MDRepository;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
//import org.netbeans.jmi.javamodel.ClassMember;
//import org.netbeans.jmi.javamodel.Feature;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.JavaModelPackage;
//import org.netbeans.jmi.javamodel.Method;
//import org.netbeans.jmi.javamodel.Parameter;
//import org.netbeans.jmi.javamodel.PrimitiveType;
//import org.netbeans.jmi.javamodel.PrimitiveTypeKindEnum;
//import org.netbeans.jmi.javamodel.Type;
//import org.netbeans.jmi.javamodel.UnresolvedClass;
//import org.netbeans.modules.javacore.api.JavaModel;
//import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
//import org.netbeans.modules.javacore.internalapi.JavaModelUtil;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.classdata.OperationData;
import org.netbeans.modules.mobility.end2end.classdata.PortData;
import org.netbeans.modules.mobility.end2end.classdata.WSDLService;
import org.netbeans.modules.mobility.end2end.client.config.ServerConfiguration;
import org.netbeans.modules.mobility.end2end.util.Util;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;


/**
 *
 * @author suchys
 */
public class ProxyGenerator {
    final private E2EDataObject dataObject;
    private DataObject createdProxy;
    
    /** Creates a new instance of ProxyGenerator */
    public ProxyGenerator(E2EDataObject dataObject) {
        this.dataObject = dataObject;
    }
    
    public String generate(){
        final ServerConfiguration sc = dataObject.getConfiguration().getServerConfigutation();
        final Sources s = ProjectUtils.getSources(dataObject.getServerProject());
//        final SourceGroup sourceGroup = Util.getPreselectedGroup(
//                s.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA ),
//                sc.getClassDescriptor().getLocation());
//        final FileObject srcDirectory = sourceGroup.getRootFolder();
//        try {
//            final ClassPath cp = ClassPath.getClassPath(srcDirectory,ClassPath.SOURCE);
//            final FileObject fo = cp.getRoots()[0]; //TODO fix me - find src or test folder @see sc.getProjectPath();
//            //there is only 1/1 service/class here
//            final WSDLService wsdlService = (WSDLService)dataObject.getConfiguration().getServices().get(0);
//            final PortData pd = (PortData)wsdlService.getData().get( 0 );
//            
//            final String targetFolderName = (sc.getClassDescriptor().getType().toLowerCase() + "support").replace('.','/'); // NOI18N
//            FileObject targetFolder = fo.getFileObject(targetFolderName);
//            if (targetFolder == null){
//                targetFolder = FileUtil.createFolder(fo, targetFolderName);
//            }
//            
//            String proxyClassName = pd.getClassName();
//            proxyClassName = proxyClassName.substring(proxyClassName.lastIndexOf('.') + 1); // NOI18N
//            proxyClassName = proxyClassName + /*sc.getClassDescriptor().getLeafClassName() + */"Proxy"; // NOI18N
//            final JavaClass jc = generateProxyClassStub(targetFolder, proxyClassName);
//            if (copyInterfaces(jc, pd)){
//                return jc.getName();
//            }
//            return null;
//        } catch (Exception ex){
//            
//        }
        return null;
    }
    
//    private JavaClass generateProxyClassStub(final FileObject targetFolder, final String name) {
//        
//        try {
//            final FileObject tempFO = Repository.getDefault().getDefaultFileSystem().findResource("Templates/Classes/Class.java"); // NOI18N
//            
//            final DataFolder folder = (DataFolder) DataObject.find(targetFolder);
//            final FileObject forDelete = folder.getPrimaryFile().getFileObject(name, "java"); // NOI18N
//            if (forDelete != null){
//                forDelete.delete();
//            }
//            final DataObject template = DataObject.find(tempFO);
//            createdProxy = template.createFromTemplate(folder, name);
//            final FileObject newIfcFO = createdProxy.getPrimaryFile();
//            return (JavaClass) JavaMetamodel.getManager().getResource(newIfcFO).getClassifiers().iterator().next();
//        } catch (DataObjectNotFoundException e) {
//            ErrorManager.getDefault().notify(e);
//        } catch (IOException e) {
//            ErrorManager.getDefault().notify(e);
//        }
//        return null;
//    }
    
//    private boolean copyInterfaces(final JavaClass target, final PortData portData){
//        final String className = portData.getType();
//        final Sources s = ProjectUtils.getSources(dataObject.getServerProject());
//        final SourceGroup[] sourceGroups = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
//        final NotifyDescriptor.Message message=new NotifyDescriptor.Message(NbBundle.getMessage(ProxyGenerator.class, "MSG_WebProjectNotBuilt"));
//        for (int id = 0; id < sourceGroups.length; id++){
//            
//            JavaModel.getJavaRepository().beginTrans(false);
//            try {
//                final JavaClass jc = Util.resolveWebServiceClass(dataObject.getServerProject().getProjectDirectory(), className );
//                if (jc == null || jc instanceof UnresolvedClass){
//                    DialogDisplayer.getDefault().notify(message);
//                    return false;
//                }
//                final Method[] methods = getMethods(jc);
//                if (methods.length != 0) {
//                    JavaModel.setClassPath(target.getResource());
//                    final List<ClassMember> contents = target.getContents();
//                    final List<OperationData> md = portData.getOperations();
//                    for (int i = 0; i < methods.length; i++ ){
//                        for ( final OperationData m : md ) {
//                            if (!m.getName().equals(methods[i].getName()))
//                                continue;
//                            final Method method = (Method)JavaModelUtil.duplicateInScope(target, methods[i]);
//                            method.setJavadoc(null);
//                            method.setModifiers(method.getModifiers()&~Modifier.ABSTRACT);
//                            contents.add(method);
//                            method.setBodyText(createInvocationBody(createdProxy, method));
//                            final JavaModelPackage modelPkg = (JavaModelPackage) method.refImmediatePackage();
//                            
//                            final WSDLService service = (WSDLService)dataObject.getConfiguration().getServices().get(0);
//                            String file = service.getFile();
//                            file = file.substring(0, file.lastIndexOf('.')); // NOI18N
//                            final ClientStubDescriptor stubType = getStub(createdProxy.getPrimaryFile(), file);
//                            if (stubType != null && !ClientStubDescriptor.JSR109_CLIENT_STUB.equals(stubType.getName())){
//                                final JavaClass ex = (JavaClass) modelPkg.getJavaClass().resolve("javax.xml.rpc.ServiceException"); //NOI18N
//                                method.getExceptionNames().add(JavaModelUtil.resolveImportsForClass(method, ex));
//                            }
//                            
//                            final JavaClass ex = (JavaClass) modelPkg.getJavaClass().resolve("java.lang.Exception"); //NOI18N
//                            method.getExceptionNames().add(JavaModelUtil.resolveImportsForClass(method, ex));
//                        }
//                    }
//                    insertMethodCall(createdProxy, target);
//                    return true;
//                }
//            } catch (Exception e){
//                ErrorManager.getDefault().notify(e);
//                return false;
//            } finally {
//                JavaModel.getJavaRepository().endTrans();
//            }
//        }
//        return true;
//    }
    
//#------------------------ WS InvokeOperationAction
    
    // {0} = service name (as type, e.g. "FooSeWebService")
    // {1} = service name (as variable, e.g. "fooService")
    // {2} = fully qualified service name (as type, e.g. com.service.FooService)
    private static final String SERVICE_DELEGATE_BODY =
            "{2} {1} = null;" + // NOI18N
            "\ntry '{'\n" + // NOI18N
            "\t\tjavax.naming.InitialContext ic = new javax.naming.InitialContext();\n" + // NOI18N
            "\t\t{1} = ({2}) ic.lookup(\"java:comp/env/service/{0}\");\n" + // NOI18N
            "\t'}' catch(javax.naming.NamingException ex) '{'\n" + // NOI18N
            "\t\tthrow new RuntimeException(ex);\n" + // NOI18N
            "\t'}'\n" + // NOI18N
            "return {1};\n";  // NOI18N
    
    // {0} = port name (as variable, e.g. "fooPort")
    // {1} = true port name (e.g. "FooPort")
    // {2} = service delegate name (e.g. "getFooService")
    // {3} = fully qualified port name (as type, e.g. com.service.FooPortType)
    private static final String PORT_DELEGATE_BODY =
            "{3} {0} = null;" + // NOI18N
            "\ntry '{'\n" + // NOI18N
            "\t\t{0} = {2}().get{1}();\n" + // NOI18N
            "\t'}' catch(javax.xml.rpc.ServiceException ex) '{'\n" + // NOI18N
            "\t\tthrow new RuntimeException(ex);\n" + // NOI18N
            "\t'}'\n" + // NOI18N
            "return {0};\n";  // NOI18N
    
    // {0} = service operation name (e.g. "getFoo")
    // {1} = port delegate name (e.g. "getFooPort")
    private static final String OPERATION_INVOCATION_BODY =
            "\ntry '{'\n" + // NOI18N
            "\t{3} {1}().{0}({2});\n" + // NOI18N
            "'}' catch(java.rmi.RemoteException ex) '{'\n" + // NOI18N
            "\tthrow ex;\n" + // NOI18N
            "'}' catch(Exception ex) '{'\n" + // NOI18N
            "\tthrow ex;\n" + // NOI18N
            "'}"; // NOI18N
    
    // {0} = service name (as type, e.g. "FooService")
    // {1} = service name (as variable, e.g. "fooService")
    // {2} = fully qualified service name (as type, e.g. com.service.FooService)
    // {3} = fully qualified service stub name (as type, e.g. com.service.FooService_Impl)
    // {4} = port name (as variable, e.g. "fooPort")
    // {5} = true port name (e.g. "FooPort")
    // {6} = fully qualified port name (as type, e.g. com.service.FooPortType)
    // {7} = service operation name (e.g. "getFoo")
    private static final String OPERATION_INVOCATION_JAXRPC_BODY =
            "\ntry '{'\n" + // NOI18N
            "\t{2} {1} = new {3}();\n" + // NOI18N
            "\t{6} {4} = {1}.get{5}();\n" + // NOI18N
            "\t{9} {4}.{7}({8});\n" + // NOI18N
            "'}' catch(javax.xml.rpc.ServiceException ex) '{'\n" + // NOI18N
            "\tthrow ex;\n" + // NOI18N
            "'}' catch(java.rmi.RemoteException ex) '{'\n" + // NOI18N
            "\tthrow ex;\n" + // NOI18N
            "'}' catch(Exception ex) '{'\n" + // NOI18N
            "\tthrow ex;\n" + // NOI18N
            "'}'\n"; // NOI18N
    
    private static String varFromName(final String name) {
        if(name.length() > 0) {
            final StringBuffer buf = new StringBuffer(name);
            
            // If the first character is uppercase, make it lowercase for the variable name,
            // otherwise, prefix an underscore.
            if(Character.isUpperCase(buf.charAt(0))) {
                buf.setCharAt(0, Character.toLowerCase(buf.charAt(0)));
            } else {
                buf.insert(0, '_'); // NOI18N
            }
            
            return removeDots(buf).toString();
        }
        return "unknown"; // NOI18N
    }
    
    private static String classFromName(final String name) {
        if (name.length() > 0) {
            final StringBuffer result = new StringBuffer(name);
            
            if (result.length() > 0 && !Character.isUpperCase(result.charAt(0))) {
                result.setCharAt(0, Character.toUpperCase(result.charAt(0)));
            }
            
            return removeDots(result).toString();
        } 
        //or return name here?
        return "unknown"; // NOI18N
    }
    
    // replace dots in a class/var name
    private static StringBuffer removeDots(final StringBuffer name) {
        int dotIndex;
        while ((dotIndex = name.indexOf(".")) > -1) { //NOI18N
            name.deleteCharAt(dotIndex); //delete the dot
            name.setCharAt(dotIndex, Character.toUpperCase(name.charAt(dotIndex))); // make the letter after dot uppercase
        }
        return name;
    }
    
    private ClientStubDescriptor getStub(final FileObject fo, final String serviceName) {
        ClientStubDescriptor result = null;
        final WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(fo);
        if(clientSupport != null) {
            final List<WsCompileClientEditorSupport.ServiceSettings> clients = clientSupport.getServiceClients();
            for ( final WsCompileClientEditorSupport.ServiceSettings settings : clients) {
                if(settings.getServiceName().equals(serviceName)) {
                    result = settings.getClientStubDescriptor();
                    break;
                }
            }
        } 
        return result;
    }
        
//    public void insertMethodCall(final DataObject dataObj, final JavaClass jc){
//        //TODO BLBE
//        final WSDLService service = (WSDLService)dataObject.getConfiguration().getServices().get(0);
//        final PortData portData = (PortData)service.getData().get( 0 );
//        
//        final String fqServiceClassName = service.getType();
//        final String fqPortTypeName = portData.getType();
//        final String serviceName = service.getName();
//        final String serviceClassName = classFromName(serviceName); //here is the class name //??
//        final String serviceVarName = varFromName(serviceName);
//        final String servicePortJaxRpcName = classFromName(portData.getName());
//        final String servicePortVarName = varFromName(portData.getName());
//        final String serviceDelegateName = "get" + serviceClassName; //NOI18N
//        final String portDelegateName = "get" + servicePortJaxRpcName; //NOI18N
//        
//        String file = service.getFile();
//        file = file.substring(0, file.lastIndexOf('.')); // NOI18N
//        final ClientStubDescriptor stubType = getStub(dataObj.getPrimaryFile(), file);
//        
//        final MDRepository repository = JavaModel.getJavaRepository();
//        
//        // including code to java class
//        boolean rollbackFlag = true; // rollback the transaction by default
//        repository.beginTrans(true); // create transaction for adding delegate methods
//        
//        try {
//            if (stubType != null) 
//                if (ClientStubDescriptor.JSR109_CLIENT_STUB.equals(stubType.getName())) {        // add service and port delegate methods
//                try {
//                    if (jc.isValid()) {
//                        final JavaModelPackage modelPkg = JavaMetamodel.getManager().getJavaExtent(jc);
//                        
//                        final Type serviceType = modelPkg.getType().resolve(fqServiceClassName);
//                        final Type portType = modelPkg.getType().resolve(fqPortTypeName);
//                        
//                        // Add service delegate
//                        final Method serviceDelegate = modelPkg.getMethod().createMethod();
//                        if (serviceDelegate != null) {
//                            serviceDelegate.setName(serviceDelegateName);
//                            serviceDelegate.setType(serviceType);
//                            serviceDelegate.setModifiers(Modifier.PRIVATE);
//                            serviceDelegate.getExceptionNames().add(
//                                    modelPkg.getMultipartId().createMultipartId("java.lang.RuntimeException", null, null)); //NOI18N
//                            
//                            final Object [] args = new Object [] { serviceName, serviceVarName, fqServiceClassName };
//                            final String delegateBody = MessageFormat.format(SERVICE_DELEGATE_BODY, args);
//                            serviceDelegate.setBodyText(delegateBody);
//                            jc.getContents().add(serviceDelegate);
//                        }
//                        
//                        // Add port delegate
//                        final Method portDelegate = modelPkg.getMethod().createMethod();
//                        if (portDelegate != null) {
//                            portDelegate.setName(portDelegateName);
//                            portDelegate.setType(portType);
//                            portDelegate.setModifiers(Modifier.PRIVATE);
//                            portDelegate.getExceptionNames().add(
//                                    modelPkg.getMultipartId().createMultipartId("java.lang.RuntimeException", null, null)); //NOI18N
//                            
//                            final Object [] args = new Object [] { servicePortVarName, servicePortJaxRpcName, serviceDelegateName, fqPortTypeName };
//                            final String delegateBody = MessageFormat.format(PORT_DELEGATE_BODY, args);
//                            portDelegate.setBodyText(delegateBody);
//                            jc.getContents().add(portDelegate);
//                        }
//                        rollbackFlag = false;   // no errors! - do not rollback
//                    }
//                } catch (NullPointerException npe) {
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, npe);
//                }
//            } else {
//                rollbackFlag = false;
//            }
//        } finally {
//            repository.endTrans(rollbackFlag);
//        }
//    }
    
//    private String createInvocationBody(final DataObject dataObj, final Method cm) {
//        final StringBuffer params = new StringBuffer();
//        
//        final List<Parameter> parameters = cm.getParameters();
//        final Iterator<Parameter> it = parameters.iterator();
//        while(it.hasNext()){
//            params.append(it.next().getName());
//            if (it.hasNext()){
//                params.append(','); // NOI18N
//            } else {
//                break;
//            }
//        }
//        
//        boolean isVoid = false;
//        //test na void
//        if ( cm.getType() instanceof PrimitiveType &&
//                ((PrimitiveType)cm.getType()).getKind().equals(PrimitiveTypeKindEnum.VOID)){
//            isVoid = true;
//        }
//        final String returnValue = !isVoid ? "return":""; //NOI18N
//        
//        final WSDLService service = (WSDLService)dataObject.getConfiguration().getServices().get(0);
//        final PortData portData = (PortData)service.getData().get( 0 );
//        
//        final String fqServiceClassName = service.getType();
//        final String fqPortTypeName = portData.getType();
//        final String serviceName = service.getName();
//        final String serviceVarName = varFromName(serviceName);
//        final String servicePortJaxRpcName = classFromName(portData.getName());
//        final String servicePortVarName = varFromName(portData.getName());
//        final String serviceOperationName = cm.getName();//cds.getPortName();
//        
//        final String portDelegateName = "get" + servicePortJaxRpcName; //NOI18N
//        String invocationBody = "";
//        
//        String file = service.getFile();
//        file = file.substring(0, file.lastIndexOf('.')); // NOI18N
//        final ClientStubDescriptor stubType = getStub(dataObj.getPrimaryFile(), file);
//        
//        if (ClientStubDescriptor.JSR109_CLIENT_STUB.equals(stubType.getName())) {
//            // create the inserted text
//            final Object [] args = new Object [] { serviceOperationName, portDelegateName, params.toString(), returnValue };
//            invocationBody = MessageFormat.format(OPERATION_INVOCATION_BODY, args);
//            
//        } else if (ClientStubDescriptor.JAXRPC_CLIENT_STUB.equals(stubType.getName())) { // JAXRPC static stub
//            // create the inserted text
//            final Object [] args = new Object [] { //serviceOperationName, portDelegateName.getName() };
//                serviceName, serviceVarName, fqServiceClassName,
//                fqServiceClassName + "_Impl", // NOI18N // !PW Note this classname is JAXRPC implementation dependent.
//                servicePortVarName, servicePortJaxRpcName, fqPortTypeName,
//                serviceOperationName, params.toString(), returnValue
//            };
//            invocationBody = MessageFormat.format(OPERATION_INVOCATION_JAXRPC_BODY, args);
//        }
//        return invocationBody;
//    }
        
//    public static Method[] getMethods(final JavaClass jc) {
//        final List<Method> result = new LinkedList<Method>();
//        if (jc != null) {
//            final List<Feature> features = jc.getFeatures();
//            for ( final Object o : features ) {
//                if (o instanceof Method) {
//                    result.add((Method)o);
//                }
//            }
//        }
//        return result.toArray(new Method[result.size()]);
//    }
}

