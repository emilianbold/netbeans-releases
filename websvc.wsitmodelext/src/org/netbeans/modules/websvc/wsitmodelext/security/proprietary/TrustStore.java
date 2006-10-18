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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;

/**
 *
 * @author Martin Grebac
 */
public interface TrustStore extends ExtensibilityElement{
    public static final String LOCATION = "Location";     //NOI18N
    public static final String SERVICEALIAS = "ServiceAlias";     //NOI18N
    public static final String STSALIAS = "STSAlias";     //NOI18N
    public static final String PEERALIAS = "PeerAlias";     //NOI18N
    public static final String TYPE = "Type";     //NOI18N
    public static final String PASSWORD = "StorePassword";     //NOI18N
   
    void setVisibility(String vis);
    String getVisibility();
    
    void setLocation(String location);
    String getLocation();

    void setSTSAlias(String alias);
    String getSTSAlias();

    void setPeerAlias(String alias);
    String getPeerAlias();

    void setType(String type);
    String getType();

    void setStorePassword(String storepassword);
    String getStorePassword();    
}
