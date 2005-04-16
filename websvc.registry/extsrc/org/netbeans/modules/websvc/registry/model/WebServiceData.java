/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.model;

///import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
///import org.netbeans.modules.websvc.registry.jaxrpc.Wsdl2Java;
////import org.netbeans.modules.websvc.registry.util.Util;

import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.Operation;
/////////import com.sun.xml.rpc.processor.model.java.JavaMethod;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;


/**
 * A webservice model. Holds the URL & methods information etc
 * When the WSDL is parsed for each port a WebServiceData is created and added to the
 * WebService Node Model.
 * @author octav, Winston Prakash, David Botterill
 */
public class WebServiceData {
    
    /** Unique Web service id*/
    private String websvcId;
    /** Web service name */
    private String name;
    /** Web Service port number */
    private int webServicePort;
    /** Web service URL */
    private String wsdlUrl;
    /** proxy name */
    private String proxy;
    
    private String groupId;
    
    private String displayName;
    
    private String proxyJarFileName;
    
    private String packageName;
    
    private String webServiceAddress;
    
    /**
     * Array list of com.sun.xml.rpc.processor.model.Port objects
     */
    private ArrayList ports = new ArrayList();
    
    public final static String PORT_PROPERTY_NAME="WSPORTNAME";
    /** Default constructor */
    public WebServiceData() {
        this(WebServiceListModel.getInstance().getUniqueWebServiceId());
    }
    
    public WebServiceData(String id) {
        setId(id);
    }
    
    public void setId(String id){
        websvcId = id;
    }
    
    public String getId(){
        return websvcId;
    }
    
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        if(displayName == null) displayName = name;
        return displayName;
    }
    
    public void setDisplayName(String dispName) {
        displayName = dispName;
    }
    
    
    public String getGroupId(){
        return groupId;
    }
    
    public void setGroupId(String id){
        groupId = id;
    }
    public void setPackageName(String inPackageName){
        packageName = inPackageName;
    }
    
    public void setWebServiceAddress(String inAddress){
        webServiceAddress = inAddress;
    }
    
    /**
     * InSync needs this, however this is a read only property
     * @param Web Service name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /** This method will return the methods for the service.
     * @return an array list of com.sun.xml.rpc.processor.model.Operation objects.
     */
    public ArrayList getMethods() {
        
        /**
         * Go through the ports and get the operations, for each
         * operation, return the JavaMethod.
         */
        ArrayList returnMethods = new ArrayList();
        Iterator portIterator = ports.iterator();
        Port currentPort = null;
        while(portIterator.hasNext()) {
            currentPort = (Port)portIterator.next();
            if(null == currentPort || null == currentPort.getOperations()) {
                continue;
            }
            Iterator operationIterator = currentPort.getOperations();
            Operation currentOperation = null;
            while(operationIterator.hasNext()) {
                currentOperation = (Operation)operationIterator.next();
                if(null == currentOperation) {
                    continue;
                }
                returnMethods.add(currentOperation);
            }
        }
        return returnMethods;
    }
    
    
    
    public int getWebServicePort() {
        return webServicePort;
    }
    
    public void setWebServicePort(int portNumber) {
        webServicePort = portNumber;
    }
    
    public String getURL() {
        return wsdlUrl;
    }
    
    public void setURL(String url) {
        wsdlUrl = url;
    }
    
    public String getProxy() {
        return proxy;
    }
    
    public void setProxy(String proxy) {
        this.proxy = proxy;
    }
    
    public void setProxyJarFileName(String jarName){
        
        proxyJarFileName = jarName;
    }
    
    public String getProxyJarFileName(){
        /**
         * Since this data could have been persisted and moved, we need to
         * make sure the file name of this jar file is still valid.  If it is not found,
         * try getting it from the current user directory.  There are really only two places
         * that are valid for the location to be in, the netbeans user directory or a temp directory
         * when the user is testing the web service before they've added it to server navigator.
         * - David Botterill 4/23/2004
         */
        if(null == proxyJarFileName) return null;
       
        File proxyFile = new File(proxyJarFileName);
        if(!proxyFile.exists()) {
            /**
             * First, strip off any bogus path information for both windows and unix style paths that might exist.
             */
            String fileOnlyName = proxyJarFileName;
            if(proxyJarFileName.indexOf("/") != -1) {
                fileOnlyName = proxyJarFileName.substring(proxyJarFileName.lastIndexOf("/")+1);
            }
            
            if(proxyJarFileName.indexOf("\\") != -1) {
                fileOnlyName = proxyJarFileName.substring(proxyJarFileName.lastIndexOf("\\")+1);
            }
            
            String newName = System.getProperty("netbeans.user") + File.separator + "websvc" + File.separator + fileOnlyName;
            proxyFile = new File(newName);
            if(!proxyFile.exists()) {
                return null;
            } else {
                proxyJarFileName = newName;
                /**
                 * make sure to set the new name.
                 */
                this.setProxyJarFileName(proxyJarFileName);
            }
        }
        
        return proxyJarFileName;
    }
    
    public String getWSDescription() {
        return "Web Service Information-\n" +
        "Name: " + name + "\n" +
        "Port number: " + webServicePort + "\n" +
        "URL: " + wsdlUrl + "\n" +
        "Address: " + webServiceAddress + "\n"
        ;
    }
    public String getPackageName(){
        if(null == packageName) {
            packageName = "webservice";///Wsdl2Java.DEFAULT_TARGET_PACKAGE;
        }
        return packageName;
    }
    public String getWebServiceAddress(){
        return webServiceAddress;
    }
    
    public Port [] getPorts() {
        return (Port []) ports.toArray(new Port [0]);
    }
    
    public void addPort(Port inPort) {
        ports.add(inPort);
    }
    
    public void setPorts(Port [] inPorts) {
        ports = new ArrayList(Arrays.asList(inPorts));
    }
    
    
    /**
     * Override equals to specialize checking equality of WebServiceData.  Web services
     * for Creator will be equal if the display name is equal regardless of case.
     */
    public boolean equals(Object inWSData) {
        /**
         * If the object passed isn't of the same type, they aren't equal.
         */
        if(!(inWSData instanceof WebServiceData)) {
            return false;
        }
        
        WebServiceData comparingWSdata = (WebServiceData)inWSData;
        String thisDisplayName = this.getDisplayName();
        String comparingDisplayName = comparingWSdata.getDisplayName();
        
        /**
         * the Display Name should never be null but we still need to check the condition.
         */
        if(null == comparingDisplayName && null != thisDisplayName) {
            return false;
        }
        if(null == thisDisplayName && null != comparingDisplayName) {
            return false;
        }
        
        /**
         * This should never happen but they logically are equal based on our
         * definition of object equality.
         */
        if(null == thisDisplayName && null == comparingDisplayName) {
            return true;
        }
        
        
        if(thisDisplayName.equalsIgnoreCase(comparingDisplayName)) {
            return true;
        } else {
            return false;
        }
        
    }
    
    /**
     * Not only debug, also used to present the W/S node description in the
     * Server Navigator
     * @return W/S Info stuff: name, port, URL
     */
    public String toString() {
        // return "Web Service Information - \n\tName: " + name + "\n\t Port number " + webServicePort + "\n\t URL " + wsdlUrl;
        return "<html><b>Web Service Information</b>" + "" +
        "<br><b>Name:</b> " + name +
        "<br><b>Port number:</b> " + webServicePort +
        "<br><b>URL:</b> " + wsdlUrl +
        "<br><b>Address:</b> " + webServiceAddress +
        "</html>"
        ;
    }
    
    
    
}