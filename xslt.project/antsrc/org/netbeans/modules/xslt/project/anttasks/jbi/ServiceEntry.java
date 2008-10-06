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
    //Member variable representing partner link name
    private String mPartnerLinkName = null;
    //Member variable representing port name
    private String mPortName = null;
    //Member variable representing partnerlink Namespace
    private String mPartnerLinkNS = null;
    //Member variable representing portname namespace
    private String mPortNameNS = null;
    //Member variable representing  role name
    private String mRoleName = null;
    //Member variable representing Partnerlink Namespace Prefix
    private String mPartnerLinkNSPrefix = null;
    //Member variable representing Portname Namespace Prefix
    private String mPortNameNSPrefix = null;    
    
    private QName mPartnerLinkNameQname = null;
    private QName mPortNameQname = null;
    
    private String mDisplayName;
    private String mProcessName;
    private String mFilePath;
    
    /**
     * Constructor
     * @param partnerLinkName Partner link name
     * @param portName    Port name
     * @param partnerLinkNS Namespace URI of the Partner Link
     * @param portNameNS Namespace URI of the portname
     * @param rolename  role name
     */
    public ServiceEntry(String partnerLinkName, String portName, String partnerLinkNS, String portNameNS, String roleName, String partnerLinkNSPrefix, String portNameNSPrefix, QName partnerLinkNameQname, QName portNameQname, String displayName, String processName, String filePath) {
        mPartnerLinkName = partnerLinkName;
        mPortName = portName;
        mPartnerLinkNS = partnerLinkNS;
        mPortNameNS = portNameNS;
        mRoleName =roleName;
        mPartnerLinkNSPrefix = partnerLinkNSPrefix;
        mPortNameNSPrefix = portNameNSPrefix;
        
        mPartnerLinkNameQname = partnerLinkNameQname;
        mPortNameQname = portNameQname;
        
        mDisplayName = displayName;
        mProcessName = processName;
        mFilePath = filePath;
    }
    

    public QName getPartnerLinkNameQname() {
        return mPartnerLinkNameQname;
    }
    
    /**
     * Get Name of the Partner Link
     * @return Name of the Partner Link
     */
    public String getPartnerLinkName() {
        return mPartnerLinkName;
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
     * Get Namespace URI of the Partner Link
     * @return Namespace URI of the Partner Link
     */
    public String getPartnerLinkNamespace() {
        return mPartnerLinkNS;
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
     * Get Namespace Prefix of the Partner Link
     * @return Namespace URI of the Partner Link
     */
    public String getPartnerLinkNamespacePrefix() {
        return mPartnerLinkNSPrefix;
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
        if (this.mPartnerLinkName.equals(serviceEntry.getPartnerLinkName()) && 
            this.mPortName.equals(serviceEntry.getPortName()) && 
            this.mPortNameNSPrefix.equals(serviceEntry.getPortNameNamespacePrefix())&& 
            this.mRoleName.equals(serviceEntry.getRoleName())){
                return true;
        }
        return false;
    }
    
    public int hashCode() {
       return (this.mPartnerLinkName+this.mPortName+this.mRoleName).hashCode();
    }
        
}
