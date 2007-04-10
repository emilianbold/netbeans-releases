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

package org.netbeans.modules.identity.profile.ui.support;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author PeterLiu
 */
public class J2ee15ProjectHelper extends J2eeProjectHelper {
    
    private static final String SERVICE_SUFFIX = "Service";     //NOI18N
    
    private static final String SUN_WEB_TEMPLATE = "Templates/Identity/SunDD/sun-web.xml";     //NOI18N
    
    private static final String SUN_WEB_XML = "sun-web";        //NOI18N
    
    private static final String SUN_EJB_JAR_TEMPLATE = "Templates/Identity/SunDD/sun-ejb-jar.xml";     //NOI18N
    
    private static final String SUN_EJB_JAR_XML = "sun-ejb-jar";    //NOI18N
    
    private static final String SUN_APPLICATION_CLIENT_TEMPLATE = "Templates/Identity/SunDD/sun-application-client.xml";    //NOI18N
    
    private static final String SUN_APPLICATION_CLIENT_XML = "sun-application-client";  //NOI18N
    
    private static final String XML_EXT = "xml";        //NOI18N
    
    private String portComponentName;
    private String serviceDescriptionName;
    private List<String> serviceRefNames;
    private List<ServiceRef> serviceRefs;
    
    
    /** Creates a new instance of J2ee15ProjectHelper */
    protected J2ee15ProjectHelper(Node node, JaxWsModel model) {
        super(node, model);
    }
    
    
    public String getPortComponentName() {
        if (portComponentName == null) {
            JavaSource source = JavaSource.forFileObject(getJavaSource());
            
            if (source != null)
                portComponentName = getClassName(source);
        }
        
        //System.out.println("J2ee15ProjectHelper.portComponentName = " + portComponentName);
        return portComponentName;
    }
    
    public String getServiceDescriptionName() {
        if (serviceDescriptionName == null) {
            JavaSource source = JavaSource.forFileObject(getJavaSource());
            
            if (source != null) {
                serviceDescriptionName = getServiceName(source);
                
                if (serviceDescriptionName == null) {
                    serviceDescriptionName = getPortComponentName() + SERVICE_SUFFIX;
                }
            }
        }
        
        //System.out.println("J2ee15ProjectHelper.serviceDescriptionName = " + serviceDescriptionName);
        return serviceDescriptionName;
    }
    
    public List<String> getAllServiceRefNames() {
        if (serviceRefNames == null) {
            serviceRefNames = new ArrayList<String>();
            serviceRefs = new ArrayList<ServiceRef>();
            List<ServiceRef> refs = getServiceRefsFromSources();
            String wsdlUri = getClient().getWsdlUrl();
            
            for (ServiceRef ref : refs) {
                if (ref.getWsdlLocation().equals(wsdlUri)) {
                    serviceRefNames.add(ref.getName());
                    serviceRefs.add(ref);
                }
            }
        }
        
        return serviceRefNames;
    }
    
    public List<ServiceRef> getServiceRefs() {
        if (serviceRefs == null) {
            getAllServiceRefNames();
        }
        
        return serviceRefs;
    }
    
    public boolean providerExists() {
        return false;
    }
    
    
    public boolean isSecurityEnabled() {
        SunDDHelper helper = new SunDDHelper(getSunDDFO(), getProjectType());
        
        if (isServer()) {
            return helper.isServiceSecurityEnabled(getServiceDescriptionName(),
                    getPortComponentName());
        } else {
            for (WsdlData wsdlData : getWsdlData()) {
                String namespace = wsdlData.getTargetNameSpace();
                String localPart = wsdlData.getPort();
                
                for (ServiceRef ref : getServiceRefs()) {
                    if (helper.isClientSecurityEnabled(ref.getName(),
                            namespace, localPart, ref.getClassName())) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    protected void enableMessageLevelSecurity(String providerId) {
        SunDDHelper helper = new SunDDHelper(getSunDDFO(), getProjectType());
        
        if (isServer()) {
            helper.setServiceMessageSecurityBinding(getServiceDescriptionName(),
                    getPortComponentName(), providerId);
        } else {
            for (WsdlData wsdlData : getWsdlData()) {
                String namespace = wsdlData.getTargetNameSpace();
                String localPart = wsdlData.getPort();
                
                for (ServiceRef ref : getServiceRefs()) {
                    helper.setServiceRefMessageSecurityBinding(ref.getName(),
                            namespace, localPart, ref.getClassName());
                }
            }
        }
    }
    
    protected void disableMessageLevelSecurity() {
        SunDDHelper helper = new SunDDHelper(getSunDDFO(), getProjectType());
        
        if (isServer()) {
            helper.removeServiceMessageSecurityBinding(getServiceDescriptionName(),
                    getPortComponentName());
        } else {
            for (WsdlData wsdlData : getWsdlData()) {
                String namespace = wsdlData.getTargetNameSpace();
                String localPart = wsdlData.getPort();
                
                for (ServiceRef ref : getServiceRefs()) {
                    helper.removeServiceRefMessageSecurityBinding(ref.getName(),
                            namespace, localPart, ref.getClassName());
                }
            }
        }
    }
    
    public FileObject getSunDDFO() {
        FileObject conf = getConfRoot();
        String sunDDName = getSunDDName();
        FileObject fobj = conf.getFileObject(sunDDName, XML_EXT);
        
        if (fobj == null) {
            String template = getSunDDTemplate();
            
            try {
                fobj = createSunDDFromTemplate(template, conf, sunDDName);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        return fobj;
    }
    
    private String getSunDDName() {
        switch (getProjectType()) {
        case EJB:
            return SUN_EJB_JAR_XML;
        case WEB:
            return SUN_WEB_XML;
        case CLIENT:
            return SUN_APPLICATION_CLIENT_XML;
        }
        
        return null;
    }
    
    private String getSunDDTemplate() {
        switch (getProjectType()) {
        case EJB:
            return SUN_EJB_JAR_TEMPLATE;
        case WEB:
            return SUN_WEB_TEMPLATE;
        case CLIENT:
            return SUN_APPLICATION_CLIENT_TEMPLATE;
        }
        
        return null;
    }
    
    private FileObject getConfRoot() {
        FileObject[] sourceRoots = getProvider().getSourceRoots();
        
        for (FileObject root : sourceRoots) {
            String name = root.getName();
            
            if (getProjectType() == ProjectType.WEB) {
                if (name.equals("web")) {       //NOI18N
                    return root.getFileObject("WEB-INF");       //NOI18N
                }
            } else {
                if (name.equals("conf")) {      //NOI18N
                    return root;
                }
            }
        }
        return null;
    }
    
    private FileObject createSunDDFromTemplate(String template,
            FileObject folder, String sunDDName) throws IOException {
        assert template != null;
        assert folder != null;
        
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();
        FileObject templateFO = defaultFS.findResource(template);
        DataObject templateDO = DataObject.find(templateFO);
        DataFolder dataFolder = DataFolder.findFolder(folder);
        
        DataObject dataObj = templateDO.createFromTemplate(dataFolder, sunDDName);
        
        return dataObj.getPrimaryFile();
    }
    
    private String getClassName(JavaSource source) {
        final String[] className = new String[1];
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    ClassTree tree = getTopLevelClassTree(controller);
                    className[0] = tree.getSimpleName().toString();
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return className[0];
    }
    
    private String getServiceName(JavaSource source) {
        final String[] serviceName = new String[1];
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement classElement = getTopLevelClassElement(controller);
                    
                    if (classElement == null) {
                        //System.out.println("Cannot resolve class!");
                    } else {
                        List<? extends AnnotationMirror> annotations =
                                controller.getElements().getAllAnnotationMirrors(classElement);
                        
                        for (AnnotationMirror annotation : annotations) {
                            if (annotation.toString().startsWith("@javax.jws.WebService")) {    //NOI18N
                                
                                Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
                                
                                for (ExecutableElement key : values.keySet()) {
                                    if (key.getSimpleName().toString().equals("serviceName")) { //NOI18N
                                        String name = values.get(key).toString();
                                        serviceName[0] =  name.replace("\"", "");               //NOI18N
                                        
                                        return;
                                    }
                                    
                                }
                                break;
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return serviceName[0];
    }
    
    private List<ServiceRef> getServiceRefsFromSources() {
        FileObject[] sourceRoots = getProvider().getSourceRoots();
        List<ServiceRef> refs = new ArrayList<ServiceRef>();
        
        for (FileObject root : sourceRoots) {
            String name = root.getName();
            
            if (name.equals("conf") || name.equals("web") || name.equals("test")) {      //NOI18N
                continue;
            }
            
            Enumeration<? extends FileObject> dataFiles = root.getData(true);
            
            while (dataFiles.hasMoreElements()) {
                FileObject fobj = dataFiles.nextElement();
                
                if (fobj.getExt().equals("java")) {     //NOI18N
                    //System.out.println("source fobj = " + fobj);
                    JavaSource source = JavaSource.forFileObject(fobj);
                    
                    refs.addAll(getServiceRefsFromSource(source));
                }
            }
        }
        
        return refs;
    }
    
    private List<ServiceRef> getServiceRefsFromSource(JavaSource source) {
        final List<ServiceRef> refs = new ArrayList<ServiceRef>();
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    
                    TypeElement classElement = getTopLevelClassElement(controller);
                    List<VariableElement> fields = ElementFilter.fieldsIn(classElement.getEnclosedElements());
                    
                    for (VariableElement field : fields) {
                        List<? extends AnnotationMirror> annotations = field.getAnnotationMirrors();
                        
                        for (AnnotationMirror annotation : annotations) {
                            if (annotation.toString().startsWith("@javax.xml.ws.WebServiceRef")) {    //NOI18N
                                Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
                                
                                for (ExecutableElement key : values.keySet()) {
                                    if (key.getSimpleName().toString().equals("wsdlLocation")) {        //NOI18N
                                        String wsdlLocation = values.get(key).toString().replace("\"", "");     //NOI18N
                                        String refName = classElement.getQualifiedName().toString() + "/" +     //NOI18N
                                                field.getSimpleName().toString();
                                        String className = classElement.getSimpleName().toString();
                                        
                                        refs.add(new ServiceRef(refName, wsdlLocation, className));
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            
        }
        
        return refs;
    }
    
    private ClassTree getTopLevelClassTree(CompilationController controller) {
        String className = controller.getFileObject().getName();
        
        List<? extends Tree> decls = controller.getCompilationUnit().getTypeDecls();
        
        for (Tree decl : decls) {
            if (decl.getKind() != Tree.Kind.CLASS) {
                continue;
            }
            
            ClassTree classTree = (ClassTree) decl;
            
            if (classTree.getSimpleName().contentEquals(className) &&
                    classTree.getModifiers().getFlags().contains(Modifier.PUBLIC))
                return classTree;
        }
        
        return null;
    }
    
    private TypeElement getTopLevelClassElement(CompilationController controller) {
        ClassTree classTree = getTopLevelClassTree(controller);
        Trees trees = controller.getTrees();
        TreePath path = trees.getPath(controller.getCompilationUnit(), classTree);
        
        return (TypeElement) trees.getElement(path);
    }
    
    private static class ServiceRef {
        private String name;
        private String wsdlLocation;
        private String className;
        
        public ServiceRef(String name, String wsdlLocation, String className) {
            this.name = name;
            this.wsdlLocation = wsdlLocation;
            this.className = className;
        }
        
        public String getName() {
            return name;
        }
        
        public String getWsdlLocation() {
            return wsdlLocation;
        }
        
        public String getClassName() {
            return className;
        }
    }
}

