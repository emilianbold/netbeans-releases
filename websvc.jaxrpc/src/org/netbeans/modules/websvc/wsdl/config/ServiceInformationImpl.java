/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
/*
 * ServiceInformationImpl.java
 *
 * Created on April 26, 2006, 10:11 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.wsdl.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.jaxrpc.PortInformation;
import org.netbeans.modules.websvc.jaxrpc.ServiceInformation;
import org.netbeans.modules.websvc.jaxrpc.nodes.WsCompileConfigCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author rico
 * Refactored methods from the WsdlDataObject
 */
public class ServiceInformationImpl implements ServiceInformation {
    
    private WeakReference portInformationHandlerRef = null;
    private DataObject wsdlDataObj;
    
    // If isClientWsdl is true, the WSDL file is in the WSDL folder of a web service
    // client enabled module and thus will have operations and UI exposed that affect
    // the service as it exists within the project.  E.g. deleting such a file will
    // actually remove the service from the project, not just delete the file on disk.
    private boolean clientResolved, isClientWsdl;
    
    /** Typical data object constructor.
     */
    public ServiceInformationImpl(DataObject dobj) {
        wsdlDataObj = dobj;
    }
    
    public boolean isClientWsdl() {
        if (!clientResolved) {
            initClientWsdl();
            clientResolved=true;
        }
        return isClientWsdl;
    }
    
    private void initClientWsdl() {
        isClientWsdl = false;
        FileObject wsdlFO = wsdlDataObj.getPrimaryFile();
        
        // Check to make sure it has a non-null parent (can't be in WSDL folder if it does).
        FileObject parentFO = wsdlFO.getParent();
        if(parentFO != null) {
            // Does this module support web service clients?
            WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(wsdlFO);
            if(clientSupport != null) {
                // Is this file object in the WSDL folder of the client?
                FileObject wsdlFolder = clientSupport.getWsdlFolder();
                if(wsdlFolder != null && wsdlFolder.equals(parentFO)) {
                    // If we get here, the following conditions should be true:
                    //   The WSDL file is in a code module that supports webservice clients.
                    //   The WSDL file is in the proper WSDL folder of that module.
                    isClientWsdl = true;
                    
                    // With the addition of "Create service from wsdl" feature,
                    // wsdl files in this folder could also be services (if the project
                    // in question supports service at least) so we need additional
                    // heuristics for this case.
//                    WebServicesSupport serviceSupport = WebServicesSupport.getWebServicesSupport(wsdlFO);
//                    if(serviceSupport != null) {
//                        List serviceList = serviceSupport.getServices();
//                    }
                    
                    // for now, just check and see if there is a mapping file in the web-inf/meta-inf folder (parent)
                    FileObject ddFolder = wsdlFolder.getParent();
                    if(ddFolder != null) {
                        FileObject mappingFile = ddFolder.getFileObject(wsdlDataObj.getName() + "-mapping", "xml"); // NOI18N
                        if(mappingFile != null) {
                            isClientWsdl = false;
                        }
                    }
                }
            }
        }
    }
    
    public String getServicePackageName() {
        // locate config object and use cookie on that to get package name
        String packageName = "unknown"; // NOI18N default to unknown package name
        
        FileObject configFO = null;
        FileObject wsdlFO = wsdlDataObj.getPrimaryFile();
        FileObject parentFO = wsdlFO.getParent();
        if(parentFO != null && parentFO.isFolder()) {
            configFO = parentFO.getFileObject(wsdlFO.getName() + WsCompileConfigDataObject.WSCOMPILE_CONFIG_FILENAME_SUFFIX, "xml");
        }
        
        if(configFO != null) {
            WsCompileConfigCookie configCookie = null;
            try {
                DataObject dobj = DataObject.find(configFO);
                if (dobj instanceof WsCompileConfigCookie)
                    configCookie = (WsCompileConfigCookie) dobj;
            } catch(DataObjectNotFoundException ex) {
                // Shouldn't happen, but it it does, we're screwed.
                // !PW FIXME log this.
            }
            
            if(configCookie != null) {
                packageName = configCookie.getServicePackageName();
            }
        }
        
        return packageName;
        
        // !PW FIXME rewrite this to use the DD provider API implemented for config files.
//        // If it's cached, use that.
//        synchronized (this) {
//            if(packageHandlerRef != null) {
//                PackageHandler handler = (PackageHandler) packageHandlerRef.get();
//                if(handler != null) {
//                    return handler.getPackageName();
//                }
//            }
//        }
//
//        String result = null;
//        FileObject configFO = null;
//
//        // Locate the -config file for this service client.
//        Set extraFiles = secondaryEntries();
//        if(extraFiles.size() >= 1) {
//            // !PW This section is a holdover from EA1 where config files are secondary entries in the loader.
//            //     It is probably obsolete.  Remove just before or after EA2.
//            FileEntry fe = (FileEntry) extraFiles.iterator().next();
//            configFO = fe.getFile();
//        } else {
//            FileObject wsdlFO = getPrimaryFile();
//            FileObject parentFO = wsdlFO.getParent();
//            if(parentFO != null && parentFO.isFolder()) {
//                configFO = parentFO.getFileObject(wsdlFO.getName() + WsCompileConfigDataObject.WSCOMPILE_CONFIG_FILENAME_SUFFIX, WSCOMPILE_CONFIG_EXTENSION);
//            }
//        }
//
//        if(configFO != null) {
//            // Invoke SAX parser on the WSDL config file to extract the package name
//            PackageHandler handler = new PackageHandler();
//
//            try {
//                javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
//                javax.xml.parsers.SAXParser saxParser = factory.newSAXParser();
//                saxParser.parse(configFO.getInputStream(), handler);
//
//                synchronized (this) {
//                    packageHandlerRef = new WeakReference(handler);
//                }
//
//                result = handler.getPackageName();
//            } catch(javax.xml.parsers.ParserConfigurationException ex) {
//                // !PW FIXME
//            } catch(org.xml.sax.SAXException ex) {
//                // !PW FIXME
//            } catch(FileNotFoundException ex) {
//                // !PW Should never happen.
//            } catch(IOException ex) {
//                // !PW FIXME
//            }
//        }
//
//        return result;
    }
    
    public PortInformation getPortInformation() {
        return parseWsdl();
    }
    
    public List getServicePorts(String serviceName) {
        List portList;
        PortInformationHandler handler = parseWsdl();
        
        if(handler != null) {
            PortInformation.ServiceInfo serviceInfo = handler.getServiceInfo(serviceName);
            portList = serviceInfo.getPorts();
        } else {
            portList = Collections.EMPTY_LIST;
        }
        
        return portList;
    }
    
    public String [] getServiceNames() {
        String [] result;
        PortInformationHandler handler = parseWsdl();
        
        if(handler != null) {
            result = handler.getServiceNames();
        } else {
            result = new String [0];
        }
        
        return result;
    }
    
    public String getTargetNamespace() {
        String result;
        PortInformationHandler handler = parseWsdl();
        
        if(handler != null) {
            result = handler.getTargetNamespace();
        } else {
            result = null; // !PW Should this be the default?
        }
        
        return result;
    }
    
    private PortInformationHandler parseWsdl() {
        PortInformationHandler handler = null;
        
        // If it's cached, use that.
        synchronized (this) {
            if(portInformationHandlerRef != null) {
                handler = (PortInformationHandler) portInformationHandlerRef.get();
                if(handler != null) {
                    return handler;
                }
            }
        }
        
        List result = Collections.EMPTY_LIST;
        FileObject primaryFile = wsdlDataObj.getPrimaryFile();
        // Invoke SAX parser on the WSDL to extract list of port bindings
        handler = new PortInformationHandler();
        
        try {
            org.xml.sax.XMLReader xmlReader = org.openide.xml.XMLUtil.createXMLReader(false,true);
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new org.xml.sax.InputSource(primaryFile.getInputStream()));
            // Extract service names and porttypes
            synchronized (this) {
                portInformationHandlerRef = new WeakReference(handler);
            }
        } catch(org.xml.sax.SAXException ex) {
            // !PW FIXME
            handler = null;
        } catch(FileNotFoundException ex) {
            // !PW Should never happen.
            handler = null;
        } catch(IOException ex) {
            // !PW FIXME
            handler = null;
        }
        
        return handler;
    }
}
