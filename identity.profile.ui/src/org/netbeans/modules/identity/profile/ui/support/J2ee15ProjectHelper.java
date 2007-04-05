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
import java.util.List;
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
}

