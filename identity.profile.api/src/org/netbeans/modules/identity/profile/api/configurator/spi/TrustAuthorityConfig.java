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


package org.netbeans.modules.identity.profile.api.configurator.spi;

/**
 * This interface is used to provide an abstraction over the TrustAuthorityConfig
 * class from the AM client sdk. It allows us to provide both a dynamic 
 * (using the client sdk API) and a file-based (using xml) implementation. 
 *
 * Created on August 2, 2006, 3:32 AM
 * 
 * @author ptliu
 */
public interface TrustAuthorityConfig {
    
    public void setEndpoint(String endpoint);
    
    public String getEndpoint();
    
    public void saveConfig();
    
}
