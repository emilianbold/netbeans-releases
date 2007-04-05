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
        FileObject sunDD = getSunDDFO();
        String pcName = getPortComponentName();
        String descName = getServiceDescriptionName();
        List<String> refNames = getAllServiceRefNames();
        
        if (isServer()) {
            SunDDHelper helper = new SunDDHelper(sunDD, getProjectType());
            
            return helper.isSecurityEnabled(descName, pcName);
        } else {
            /*
            String s = refNames.get(0);
            List<WsdlData> wsdlInfo = getWsdlData();
             
            if (!wsdlInfo.isEmpty()) {
                WsdlData w = wsdlInfo.get(0);
                String namespace = w.getTargetNameSpace();
                String localPart = w.getPort();
             
                if (SunDDBridge.doesSvcRefMSBExist(sunDD, s, namespace, localPart) &&
                        SunDDBridge.isSvcRefMSBAMProvider(sunDD, s, namespace, localPart))
                    return true;
            }
             */
        }
        return false;
    }
    
    protected void enableMessageLevelSecurity(String providerId) {
        FileObject sunDD = getSunDDFO();
        String pcName = getPortComponentName();
        String descName = getServiceDescriptionName();
        List<String> refNames = getAllServiceRefNames();
        
        if (isServer()) {
            //            System.out.println("descname: portcompname: " + descName +
            //                    " : " + pcName);
            SunDDHelper helper = new SunDDHelper(sunDD, getProjectType());
            
            helper.setServiceMessageSecurityBinding(descName, pcName, providerId);
        } else {
            /*
            List<WsdlData> wsdlInfo = getWsdlData();
            int i = 0;
            assert(wsdlInfo.size() >= refNames.size());
            for (String s : refNames) {
                if (wsdlInfo.get(i) != null) {
                    String namespace = wsdlInfo.get(i).getTargetNameSpace();
                    String localPart = wsdlInfo.get(i).getPort();
//                    System.out.println("refName : namespace: localpart: " + s +
//                            " : " + namespace + " : " + localPart);
                    if (!SunDDBridge.setSvceRefMSB(sunDD, s, namespace, localPart)) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                new Exception("Failed during SunDD changes")); // NOI18N
                    }
                }
                i++;
            }
             */
        }
    }
    
    protected void disableMessageLevelSecurity() {
        //if (!isSecurityEnabled()) return;
        
        FileObject sunDD = getSunDDFO();
        String pcName = getPortComponentName();
        String descName = getServiceDescriptionName();
        List<String> refNames = getAllServiceRefNames();
        
        if (isServer()) {
            //            System.out.println("Into delete -- descname: portcompname: " + descName +
            //                    " : " + pcName);
            SunDDHelper helper = new SunDDHelper(sunDD, getProjectType());
            helper.removeServiceMessageSecurityBinding(descName, pcName);
        } else {
            /*
            List<WsdlData> wsdlInfo = getWsdlData();
            int i = 0;
            assert(wsdlInfo.size() >= refNames.size());
            for (String s : refNames) {
                if (wsdlInfo.get(i) != null) {
                    String namespace = wsdlInfo.get(i).getTargetNameSpace();
                    String localPart = wsdlInfo.get(i).getPort();
                    //                    System.out.println("Into delete -- refName : namespace: localpart: " + s +
                    //                            " : " + namespace + " : " + localPart);
                    if (!SunDDBridge.deleteSvcRefMSB(sunDD, s, namespace, localPart)) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                new Exception("Failed during SunDD changes")); // NOI18N
                    }
                }
                i++;
            }
             */
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

