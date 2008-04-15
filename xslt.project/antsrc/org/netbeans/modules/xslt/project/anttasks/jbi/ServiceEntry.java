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
package org.netbeans.modules.xslt.project.anttasks.jbi;

import javax.xml.namespace.QName;

public class ServiceEntry {
    private String mTargetNs = null;
    
    private String mName = null;
    //Member variable representing port name
    private String mPortName = null;
    //Member variable representing partnerlink Namespace
    private String mPortNameNS = null;
    //Member variable representing  role name
    private String mRoleName = null;
    //Member variable representing Portname Namespace Prefix
    private String mPortNameNSPrefix = null;    
    
    private QName mPortNameQname = null;
    
    private String mDisplayName;
    private String mProcessName;
    private String mFilePath;
    
    /**
     * Constructor
     * @param portName    Port name
     * @param portNameNS Namespace URI of the portname
     * @param rolename  role name
     */
    public ServiceEntry(String targetNs, String name, String portName,  
            String portNameNS, String portNameNSPrefix, QName portNameQname, 
            String displayName, String processName, String filePath) 
    {
        if (targetNs == null) {
            throw new IllegalStateException("target namespace should not be null");
        }
        mTargetNs = targetNs;
        mName = name;
        
        mPortName = portName;
        mPortNameNS = portNameNS;
        mPortNameNSPrefix = portNameNSPrefix;
        
        mPortNameQname = portNameQname;
        
        mDisplayName = displayName;
        mProcessName = processName;
        mFilePath = filePath;
    }
    

    /**
     * Get TargetNamespace of the TransformMap
     * @return TargetNamespace of the TransformMap
     */
    public String getTargetNamespace() {
        return mTargetNs;
    }

    /**
     * Get Name of the Service/Invoke
     * @return Name of the Service/Invoke
     */
    public String getName() {
        return mName;
    }

    public QName getPortNameQname() {
        return mPortNameQname;
    }

    /**
     * Get Name of the Port
     * @return Name of the Port
     */
    public String getPortName() {
        return mPortName;
    }
        
    /**
     * Return Namespace URI of portName
     * @return Namespace URI of the portname
     */
    public String getPortNameNamespace() {
        return mPortNameNS;
    }    
    
    /**
     * Return Role name
     * @return Role name
     */   
    public String getRoleName(){
        return mRoleName;
    }
    /**
     * Return Namespace Prefix of portName
     * @return Namespace URI of the portname
     */
    public String getPortNameNamespacePrefix() {
        return mPortNameNSPrefix;
    }    
    
    public String getDisplayName() {
        return mDisplayName;
    }
    
    public String getProcessName() {
        return mProcessName;
    }
    
    public String getFilePath() {
        return mFilePath;
    }
    
    public boolean equals(Object obj) {
        if (! (obj instanceof ServiceEntry)) {
            return false;
        }
        ServiceEntry serviceEntry = (ServiceEntry)obj;
        if (this.mPortName.equals(serviceEntry.getPortName()) && 
            this.mPortNameNSPrefix.equals(serviceEntry.getPortNameNamespacePrefix())){
                return true;
        }
        return false;
    }
    
    public int hashCode() {
       return (this.mPortName).hashCode();
    }
        
}
