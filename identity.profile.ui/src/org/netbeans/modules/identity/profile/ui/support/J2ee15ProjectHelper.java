/*
 * J2ee15ProjectHelper.java
 *
 * Created on April 3, 2007, 8:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.identity.profile.ui.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
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
    
    /** Creates a new instance of J2ee15ProjectHelper */
    protected J2ee15ProjectHelper(Node node, JaxWsModel model) {
        super(node, model);
    }
    
    
    public String getPortComponentName() {
        if (portComponentName == null) {
            JavaSource source = JavaSource.forFileObject(getJavaSource());
            
            if (source != null)
                portComponentName = JavaSourceHelper.getClassName(source);
        }
        
        System.out.println("J2ee15ProjectHelper.portComponentName = " + portComponentName);
        return portComponentName;
    }
    
    public String getServiceDescriptionName() {
        if (serviceDescriptionName == null) {
            JavaSource source = JavaSource.forFileObject(getJavaSource());
            
            if (source != null) {
                serviceDescriptionName = JavaSourceHelper.getServiceName(source);
                
                if (serviceDescriptionName == null) {
                    serviceDescriptionName = getPortComponentName();
                }
            }
        }
        
        System.out.println("J2ee15ProjectHelper.serviceDescriptionName = " + serviceDescriptionName);
        return serviceDescriptionName;
    }
    
    public List<String> getAllServiceRefNames() {
        if (serviceRefNames == null) {
            serviceRefNames = new ArrayList<String>();
            List<ServiceRef> refs = getServiceRefs();
            String wsdlUri = getClient().getWsdlUrl();
            
            System.out.println("wsdlUri = " + wsdlUri);
            
            for (ServiceRef ref : refs) {
                if (ref.getWsdlLocation().equals(wsdlUri)) {
                    System.out.println("adding serviceRefName = " + ref.getName());
                    serviceRefNames.add(ref.getName());
                }
            }
        }
        
        return serviceRefNames;
    }
    
    public boolean isSecurityEnabled() {
        SunDDHelper helper = new SunDDHelper(getSunDDFO(), getProjectType());
      
        List<String> refNames = getAllServiceRefNames();
        
        if (isServer()) {
            return helper.isServiceSecurityEnabled(getServiceDescriptionName(), 
                    getPortComponentName());
        } else {
            for (WsdlData wsdlData : getWsdlData()) {
                String namespace = wsdlData.getTargetNameSpace();
                String localPart = wsdlData.getPort();
                
                for (String refName : getAllServiceRefNames()) {
                    if (helper.isClientSecurityEnabled(refName,
                            namespace, localPart)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    protected void enableMessageLevelSecurity(String providerId) {
        SunDDHelper helper = new SunDDHelper(getSunDDFO(), getProjectType());
        List<String> refNames = getAllServiceRefNames();
        
        if (isServer()) {
            helper.setServiceMessageSecurityBinding(getServiceDescriptionName(),
                    getPortComponentName(), providerId);
        } else {
            for (WsdlData wsdlData : getWsdlData()) {
                String namespace = wsdlData.getTargetNameSpace();
                String localPart = wsdlData.getPort();
                
                for (String refName : getAllServiceRefNames()) {
                    helper.setServiceRefMessageSecurityBinding(refName,
                            namespace, localPart);
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
                
                for (String refName : getAllServiceRefNames()) {
                    helper.removeServiceRefMessageSecurityBinding(refName,
                            namespace, localPart);
                }
            }
        }
    }
    
    private List<ServiceRef> getServiceRefs() {
        FileObject[] sourceRoots = getProvider().getSourceRoots();
        List<ServiceRef> refs = new ArrayList<ServiceRef>();
        
        for (FileObject root : sourceRoots) {
            System.out.println("root = " + root);
            if (root.getName().endsWith("conf")) {
                continue;
            }
            
            Enumeration<? extends FileObject> dataFiles = root.getData(true);
            
            while (dataFiles.hasMoreElements()) {
                FileObject fobj = dataFiles.nextElement();
                
                if (fobj.getExt().equals("java")) {
                    System.out.println("source fobj = " + fobj);
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
                    
                    TypeElement classElement = JavaSourceHelper.getTopLevelClassElement(controller);
                    List<VariableElement> fields = ElementFilter.fieldsIn(classElement.getEnclosedElements());
                    
                    for (VariableElement field : fields) {
                        System.out.println("field = " + field);
                        List<? extends AnnotationMirror> annotations = field.getAnnotationMirrors();
                        
                        for (AnnotationMirror annotation : annotations) {
                            System.out.println("annotation = " + annotation);
                            if (annotation.toString().startsWith("@javax.xml.ws.WebServiceRef")) {    //NOI18N
                                
                                Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();
                                
                                for (ExecutableElement key : values.keySet()) {
                                    System.out.println("key = " + key.getSimpleName());
                                    System.out.println("value = " + values.get(key));
                                    
                                    if (key.getSimpleName().toString().equals("wsdlLocation")) { //NOI18N
                                        String wsdlLocation = values.get(key).toString().replace("\"", "");
                                        String refName = classElement.getQualifiedName().toString() + "/" +
                                                field.getSimpleName().toString();
                                        
                                        refs.add(new ServiceRef(refName, wsdlLocation));
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
    
    
    private static class ServiceRef {
        private String name;
        private String wsdlLocation;
        
        public ServiceRef(String name, String wsdlLocation) {
            this.name = name;
            this.wsdlLocation = wsdlLocation;
        }
        
        public String getName() {
            return name;
        }
        
        public String getWsdlLocation() {
            return wsdlLocation;
        }
        
        public String toString() {
            return "name:" + name + " wsdlLocation: " + wsdlLocation;
        }
    }
}

