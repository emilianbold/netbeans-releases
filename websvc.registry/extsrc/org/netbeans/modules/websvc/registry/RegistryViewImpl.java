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

package org.netbeans.modules.websvc.registry;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Lookup;

import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.nodes.WebServicesNode;
import org.netbeans.modules.websvc.registry.nodes.WebServicesRootNode;
import org.netbeans.modules.websvc.registry.util.Util;
import org.netbeans.modules.websvc.registry.wsdl.WSDLInfo;

import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Peter Williams
 */
public class RegistryViewImpl implements WebServicesRegistryView, PropertyChangeListener {

    public RegistryViewImpl() {
        WebServiceListModel.getInstance().addPropertyChangeListener(this);
    }

    // !PW Does this have any use?
    public Node getRegistryRootNode() {
        Node rootNode = null;

        Lookup l = Lookups.forPath("UI/Runtime"); // NOI18N
        rootNode = (WebServicesRootNode) l.lookup(WebServicesRootNode.class);

        return rootNode;
    }

    public Node[] getWebServiceNodes(FileObject wsdlFile) {
        // locate all service nodes that came from this wsdl file.
        // I could turn WSDL file into blocks of WebServiceData, that I then search
        // for in the list model?

        Node [] result = null;
        ArrayList foundServices = new ArrayList();

        WebServiceListModel model = WebServiceListModel.getInstance();
        Set registeredServices = model.getWebServiceSet();
        List serviceNames = getServiceNames(wsdlFile);

        for (Iterator nameIter = serviceNames.iterator(); nameIter.hasNext(); ) {
            String searchName = (String) nameIter.next();
            searchName = removeSpacesFromServiceName(searchName);

            for (Iterator iter = registeredServices.iterator(); iter.hasNext(); ) {
                WebServiceData wsData = (WebServiceData) iter.next();
                if(searchName.equalsIgnoreCase(wsData.getName())) {
                    foundServices.add(new WebServicesNode(wsData));
                    break;
                }
            }
        }

        if(foundServices.size() > 0) {
            result = (Node []) foundServices.toArray(new Node [foundServices.size()]);
        }

        return result;
    }

    public static String removeSpacesFromServiceName(String serviceName) {
        if ((serviceName!=null) && (serviceName.indexOf(" ") > -1)) {  //NOI18N
            String result = ""; //NOI18N
            StringTokenizer serviceNameTokenizer = new StringTokenizer(serviceName, " ", false); //NOI18N
            while (serviceNameTokenizer.hasMoreTokens()) {
                StringBuffer token = new StringBuffer(serviceNameTokenizer.nextToken());
                if (token != null) {
                    token.setCharAt(0, Character.toUpperCase(token.charAt(0)));
                    result = result.concat(token.toString());
                }
            }
            return result;
        }
        return serviceName;
    }
    
    public boolean isServiceRegistered(String serviceName) {
        return WebServiceListModel.getInstance().webServiceExists(serviceName);
    }

    public boolean registerService(FileObject wsdlFile, boolean replaceService) {
        boolean result;

        try {
            result = registerService(wsdlFile.getURL(), "default", replaceService);
        } catch(FileStateInvalidException ex) {
            result = false;
        }

        return result;
    }

    public boolean registerService(URL wsdlUrl, boolean replaceService) {
        return registerService(wsdlUrl, "default", replaceService);
    }

    private boolean registerService(URL wsdlUrl, String groupId, boolean replaceService) {
        boolean result = false;

        // Retrieve WSDL and convert into a Set of WebServiceData objects
        WSDLInfo wsdLInfo = new WSDLInfo();
        wsdLInfo.setWsdlUrl(wsdlUrl);
        wsdLInfo.setPackageName("webservice");	// NOI18N
        wsdLInfo.setRemoveGeneratedFiles(true);

        if(wsdLInfo.create()) {
            Set webServices = wsdLInfo.getWebServices();
            if(webServices != null && webServices.size() > 0) { 
                Iterator iter =  webServices.iterator();
                boolean duplicateFound = false;
                WebServiceListModel wsListModel = WebServiceListModel.getInstance();

                // Assume that all of the web services adds failed.  For each 
                // success, remove the web service from the failed set.
                HashSet addFailedWebServices = new HashSet();
                addFailedWebServices.addAll(webServices);

                while(iter.hasNext()){
                    WebServiceData wsData = (WebServiceData) iter.next();
                    String targetGroupId = groupId;
                    WebServiceData existingWS = wsListModel.findService(wsData);
                    if (existingWS!=null) {
                        String existingWSGroupId = existingWS.getGroupId();
                        if (existingWSGroupId!=null) targetGroupId = existingWS.getGroupId();
                        if(replaceService) {
                            // remove old service first
                            wsListModel.removeWebService(existingWS);
                        } else {
                            // !PW Failed: Web service of that name already exists in model
                            continue;
                        }
                    }

                    // Now create the client code for the web service.
                    String jarFileName = System.getProperty("netbeans.user") +"/websvc/" + "webservice" + new Date().getTime() + ".jar";

                    if(!Util.createWSJar(wsData, null, jarFileName)) {
                        // !PW FIXME failed compilation
                        continue;
                    }

                    // Set the jar filename 
                    // !PW why isn't this done by Util.createWSJar?!
                    wsData.setProxyJarFileName(jarFileName);

                    // Add it to the list of web services for Server Navigator
                    wsListModel.addWebService(wsData);

                    // Put service in 'default'.
                    WebServiceGroup wsGroup = wsListModel.getWebServiceGroup(targetGroupId);
                    // !PW Fix for 49717 - If this group does not exist yet, create it.
                    if(wsGroup == null) {
                        wsGroup = new WebServiceGroup(targetGroupId);
                        wsListModel.addWebServiceGroup(wsGroup);
                    }

                    wsGroup.add(wsData.getId());
                    wsData.setGroupId(wsGroup.getId());

                    addFailedWebServices.remove(wsData);
                }

                // Return true if we added all services in this WSDL file, otherwise false.
                // !PW Need better error control and messaging.
                result = (addFailedWebServices.size() == 0);
            }
        } else {
            // !PW Failed to create WsdlInfo (bad URL, bad parse, etc.)
        }

        return result;
    }

    private List getServiceNames(FileObject wsdlFile) {
        List result = Collections.EMPTY_LIST;

        try {
            org.xml.sax.XMLReader xmlReader = org.openide.xml.XMLUtil.createXMLReader(false,true);
            ServiceNameParser handler= new ServiceNameParser();
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new org.xml.sax.InputSource(wsdlFile.getInputStream()));
            result = handler.getServiceNameList();
        } catch(SAXException ex) {
            // Bogus WSDL, return empty list.
        } catch(IOException ex) {
            // Bogus WSDL, return empty list.
        }

        return result;
    }

    /** Property change support
     */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(WebServiceListModel.MODEL_SERVICE_ADDED.equals(evt.getPropertyName())) {
            WebServiceData wsData = (WebServiceData) evt.getNewValue();
            fireServiceAdded(new WebServicesNode(wsData));
        } else if(WebServiceListModel.MODEL_SERVICE_REMOVED.equals(evt.getPropertyName())) {
            WebServiceData wsData = (WebServiceData) evt.getOldValue();
            fireServiceRemoved(wsData.getName());
        }
    }

    private void fireServiceAdded(Node wsNode) {
        propertyChangeSupport.firePropertyChange(WEB_SERVICE_ADDED, null, wsNode);
    }

    private void fireServiceRemoved(String serviceName) {
        propertyChangeSupport.firePropertyChange(WEB_SERVICE_REMOVED, serviceName, null);
    }

    private static final class ServiceNameParser extends DefaultHandler {

        private static final String W3C_WSDL_SCHEMA = "http://schemas.xmlsoap.org/wsdl";
        private static final String W3C_WSDL_SCHEMA_SLASH = "http://schemas.xmlsoap.org/wsdl/";

        private ArrayList serviceNameList;

        ServiceNameParser() {
            serviceNameList = new ArrayList();
        }

        public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
            if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
                if("service".equals(localname)) {
                    serviceNameList.add(attributes.getValue("name"));
                }
            }
        }

        public List/*String*/ getServiceNameList() {
            return serviceNameList;
        }
    }
}
