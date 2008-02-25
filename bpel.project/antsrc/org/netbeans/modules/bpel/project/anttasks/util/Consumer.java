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
package org.netbeans.modules.bpel.project.anttasks.util;

public class Consumer {
    //Member variable representing partner link name
    private String mPartnerLinkName = null;
    //Member variable representing port name
    private String mPortName = null;
    //Member variable representing partnerlink Namespace
    private String mPartnerLinkNS = null;
    //Member variable representing portname namespace
    private String mPortNameNS = null;
    //Member variable representing  role name
    private String mPartnerRoleName = null;   
    //Member variable representing Partnerlink Namespace Prefix
    private String mPartnerLinkNSPrefix = null;
    //Member variable representing Portname Namespace Prefix
    private String mPortNameNSPrefix = null;       
     
    private String mProcessName = null;
    private String mFilePath = null;
    
    /**
     * Constructor
     * @param partnerLinkName Partner link name
     * @param portName    Port name
     * @param partnerLinkNS Namespace URI of the Partner Link
     * @param portNameNS Namespace URI of the portname
     * @param partnerRoleName Partner role name
     */
    public Consumer(
            String partnerLinkName, 
            String portName, 
            String partnerLinkNS, 
            String portNameNS, 
            String partnerRoleName, 
            String partnerLinkNSPrefix, 
            String portNameNSPrefix,
            String processName,
            String filePath) {
        mPartnerLinkName = partnerLinkName;
        mPortName = portName;
        mPartnerLinkNS = partnerLinkNS;
        mPortNameNS = portNameNS;
        mPartnerRoleName =partnerRoleName;
        mPartnerLinkNSPrefix = partnerLinkNSPrefix;
        mPortNameNSPrefix = portNameNSPrefix;        
        mProcessName = processName;
        mFilePath = filePath;
    }    
    
    /**
     * Get Name of the Partner Link
     * @return Name of the Partner Link
     */
    public String getPartnerLinkName() {
        return mPartnerLinkName;
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
     * Get Namespace Prefix of the Partner Link
     * @return Namespace URI of the Partner Link
     */
    public String getPartnerLinkNamespacePrefix() {
        return mPartnerLinkNSPrefix;
    }   
    
    /**
     * Return Namespace URI of portName
     * @return Namespace URI of the portname
     */
    public String getPortNameNamespace() {
        return mPortNameNS;
    }    
    
    /**
     * Return Namespace Prefix of portName
     * @return Namespace URI of the portname
     */
    public String getPortNameNamespacePrefix() {
        return mPortNameNSPrefix;
    }     
    
    /**
     * Return Partner Role name
     * @return Partner Role name
     */   
    public String getPartnerRoleName(){
        return mPartnerRoleName;
    }
    
    public String getProcessName() {
        return mProcessName;
    }
    
    public String getFilePath() {
        return mFilePath;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Provider)) {
            return false;
        }
        Consumer consumer = (Consumer)obj;
        if (this.mPartnerLinkName.equals(consumer.getPartnerLinkName()) && 
            this.mPortName.equals(consumer.getPortName()) && 
            this.mPartnerLinkNS.equals(consumer.getPartnerLinkNamespace())&& 
            this.mPortNameNSPrefix.equals(consumer.getPortNameNamespacePrefix())&& 
            this.mPartnerRoleName.equals(consumer.getPartnerRoleName())){
                return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return (this.mPartnerLinkName+this.mPortName+this.mPartnerRoleName).hashCode();
    }
}
