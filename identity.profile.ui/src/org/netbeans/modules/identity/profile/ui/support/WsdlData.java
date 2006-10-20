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

package org.netbeans.modules.identity.profile.ui.support;

/**
 * Wrapper class for a wsdl file.
 *
 * Created on July 19, 2006, 5:07 PM
 *
 * @author ptliu
 */
public class WsdlData {
    
    private String targetNameSpace;
    private String port;
    private String address;
    
    /** Creates a new instance of WsdlData */
    public WsdlData() {
    }
    
    void setTargetNameSpace(String tns) {
        this.targetNameSpace = tns;
    }
    
    public String getTargetNameSpace() {
        return targetNameSpace;
    }
      
    void setPort(String port) {
        this.port = port;
    }
    
    public String getPort() {
        return port;
    }
    
    void setAddress(String address) {
        this.address = address;
    }
    
    public String getAddress() {
        return address;
    }
    
    public boolean isValid() {
        return (targetNameSpace != null) && (port != null) && 
                (address != null);
    }
}
