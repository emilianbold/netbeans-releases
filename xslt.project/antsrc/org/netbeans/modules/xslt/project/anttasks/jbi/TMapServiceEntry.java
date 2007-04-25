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
import org.w3c.dom.Node;

public class TMapServiceEntry {
    //Member variable representing partner link name
    private QName mPartnerLinkNameQname = null;
    private String mRoleName = null;
    private String mOperation = null;
    private String mFile = null;
    private String mTransformJBI = null;
    
    private Node mNode;

        /**
     * Constructor
     * @param partnerLinkName Partner link name
     * @param portName    Port name
     * @param partnerLinkNS Namespace URI of the Partner Link
     * @param portNameNS Namespace URI of the portname
     * @param rolename  role name
     */
    public TMapServiceEntry(QName partnerLinkNameQname, 
            String roleName, 
            String operation, 
            String file, 
            String transformJBI, 
            Node node
            ) 
    {
        assert partnerLinkNameQname != null;
        
        mPartnerLinkNameQname = partnerLinkNameQname;
        mRoleName = roleName;
        mOperation = operation;
        mFile = file;
        mTransformJBI = transformJBI;
        
        mNode = node;
    }
    

    public QName getPartnerLinkNameQname() {
        return mPartnerLinkNameQname;
    }
    
    
    /**
     * Return Role name
     * @return String role name
     */   
    public String getRoleName(){
        return mRoleName;
    }
    
    /**
     * Return operation
     * @return String operation
     */   
    public String getOperation(){
        return mOperation;
    }

    
    /**
     * Return file
     * @return String file
     */   
    public String getFile(){
        return mFile;
    }

    /**
     * Return TransformJBI
     * @return String transformJBI
     */   
    public String getTransformJBI(){
        return mTransformJBI;
    }

    public Node  getNode() {
        return mNode;
    }
    
    public boolean equals(Object obj) {
        if (! (obj instanceof TMapServiceEntry)) {
            return false;
        }
        TMapServiceEntry serviceEntry = (TMapServiceEntry)obj;
        String pltLocalName = this.mPartnerLinkNameQname.getLocalPart();
        String servicePltLocalName = serviceEntry.getPartnerLinkNameQname().getLocalPart();
        
        
        if (pltLocalName != null && pltLocalName.equals(servicePltLocalName)  && 
            this.mRoleName.equals(serviceEntry.getRoleName()) &&
            this.mOperation.equals(serviceEntry.getOperation()))
        {
                return true;
        }
        return false;
    }
    
    public int hashCode() {
       return (this.mPartnerLinkNameQname.getLocalPart()+this.mRoleName+this.mOperation).hashCode();
    }
        
}
