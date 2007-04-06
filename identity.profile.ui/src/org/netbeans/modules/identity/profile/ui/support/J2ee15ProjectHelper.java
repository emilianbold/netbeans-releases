/*
 * J2ee15ProjectHelper.java
 *
 * Created on April 3, 2007, 8:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.identity.profile.ui.support;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
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
import org.netbeans.modules.identity.profile.api.bridgeapi.SunDDBridge;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author PeterLiu
 */
public class J2ee15ProjectHelper extends J2eeProjectHelper {
    
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
                    serviceDescriptionName = getPortComponentName();
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
    
      
    private String getClassName(JavaSource source) {
        final String[] className = new String[1];
        
        try {
            source.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
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

