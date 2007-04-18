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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Endpoint.java
 *
 * Created on October 4, 2006, 2:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.javaee.codegen.model;

import javax.xml.namespace.QName;

/**
 *
 * @author gpatil
 */
public class Endpoint {
    public static enum EndPointType { Provider, Consumer}
    
    private EndPointType ept;
    private String endPointName;     // PortName
    private QName interfaceName;    // PortType
    private QName serviceName;      // ServiceName
    
    // Below attributes are not part of equality check.
    private Boolean useBridge = Boolean.TRUE;      // Default is true.
    private Boolean usingDefaultNames = Boolean.FALSE;   // Default is false.
    
    private String  wsdlLocation = ""; 
    
    private int hash = -1;
    
    /**
     * Creates a new instance of Endpoint
     */
    public Endpoint() {
    }
    
    public Endpoint(EndPointType type, String portName, QName portType, QName nServiceName) {
        this.ept = type;
        this.endPointName = portName;
        this.interfaceName = portType;
        this.serviceName = nServiceName;
    }
    
    public Endpoint(Endpoint orig) {
        this.ept = orig.ept;
        this.endPointName = orig.endPointName;
        this.interfaceName = orig.interfaceName;
        this.serviceName = orig.serviceName;
    }
    
    public void setEndPointType(EndPointType nEpt){
        this.ept = nEpt;
        this.hash = -1;
    }
    
    public EndPointType getEndPointType(){
        return this.ept;
    }
    
    public void setEndPointName(String nName){
        this.endPointName = nName;
        this.hash = -1;
    }
    
    public String getEndPointName(){
        return this.endPointName;
    }
    
    public void setInterfaceName(QName nName){
        this.interfaceName = nName;
        this.hash = -1;
    }
    
    public QName getInterfaceName(){
        return this.interfaceName;
    }
    
    public void setServiceName(QName nName){
        this.serviceName = nName;
        this.hash = -1;
    }
       
    public QName getServiceName(){
        return this.serviceName;
    }
    
    public void isUseBridge(Boolean ib){
        this.useBridge = ib;
    }
    
    public Boolean isUseBridge(){
        return this.useBridge ;
    }
    
    public void isUsingDefaultNames(Boolean ib){
        this.usingDefaultNames = ib;
    }
    
    public Boolean isUsingDefaultNames(){
        return this.usingDefaultNames ;
    }

    public String getWSDLLocation(){
        return this.wsdlLocation;
    }
    
    public void setWSDLLocation(String nWsdlLoc){
        this.wsdlLocation = nWsdlLoc;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("EndPoint(");
        sb.append(this.ept);
        sb.append("):service:");
        sb.append(this.serviceName);
        sb.append(":port:");
        sb.append(this.endPointName);
        sb.append(":portType:");
        sb.append(this.interfaceName);
        return sb.toString();
    }
    
    public String toEndpointConfigXML(String indent){
        if (indent == null){
            indent = "";
        }
        
        String newlineIndent = "\n" + indent;
        
        StringBuffer sb = new StringBuffer();
        sb.append(indent);
        sb.append("<endpoint ");
        sb.append(newlineIndent);        
        sb.append("  endpointType=\"");
        sb.append(this.ept);
        sb.append("\"");  
        sb.append(newlineIndent);
        sb.append("  portName=\""); 
        sb.append(this.endPointName);
        sb.append("\"");          
        sb.append(newlineIndent);
        sb.append("  portTypeLocalName=\"");
        sb.append(this.interfaceName.getLocalPart());
        sb.append("\"");          
        sb.append(newlineIndent);
        sb.append("  portTypeNamespace=\"");
        sb.append(this.interfaceName.getNamespaceURI());
        sb.append("\"");          
        sb.append(newlineIndent);        
        sb.append("  serviceLocalName=\"");
        sb.append(this.serviceName.getLocalPart());
        sb.append("\"");          
        sb.append(newlineIndent);        
        sb.append("  serviceNamespace=\"");
        sb.append(this.serviceName.getNamespaceURI());
        sb.append("\"/>\n");
        return sb.toString();
    }
    
    /**
     * "UseBridge" value will not effect equality comparison.
     *
     */
    public boolean equals(Object obj) {
        boolean ret = true;
        Endpoint oObj = null;
        
        if (this == obj){
            return true;
        }
        
        if ((obj == null) || (!(obj instanceof Endpoint))){
            return false;
        }
        
        oObj = (Endpoint) obj;
        
        if (this.ept != oObj.ept){
            return false;
        }
        
        if ((this.interfaceName == null) && (oObj.getInterfaceName() != null)){
            return false;
        }
        
        if ((this.interfaceName != null) && (!this.interfaceName.equals(oObj.getInterfaceName()))){
            return false;
        }
        
        if ((this.serviceName == null) && (oObj.getServiceName() != null)){
            return false;
        }
        
        if ((this.serviceName != null) && (!this.serviceName.equals(oObj.getServiceName()))){
            return false;
        }
        
        if ((this.endPointName == null) && (oObj.getEndPointName() != null)){
            return false;
        }
        
        if ((this.endPointName != null) && (!this.endPointName.equals(oObj.getEndPointName()))){
            return false;
        }
        
        return ret;
    }
    
    public int hashCode() {
        if (this.hash == -1){
            // Do not include "UseBridge", "usingDefaultNames"
            StringBuffer sb = new StringBuffer();
            sb.append(this.endPointName);
            sb.append(this.ept);
            sb.append(this.interfaceName);
            sb.append(this.serviceName);
            this.hash = sb.toString().hashCode();
        }
        return this.hash;
    }
}
