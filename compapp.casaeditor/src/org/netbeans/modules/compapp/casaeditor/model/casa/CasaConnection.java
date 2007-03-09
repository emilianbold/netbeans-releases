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
package org.netbeans.modules.compapp.casaeditor.model.casa;

import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author jqian
 */
public interface CasaConnection extends CasaComponent {
    public static final String STATE_PROPERTY = "state";            // NOI18N
    public static final String PROVIDER_PROPERTY = "provider";      // NOI18N
    public static final String CONSUMER_PROPERTY = "consumer";      // NOI18N
        
    void setConsumer(NamedComponentReference<CasaEndpoint> endpoint);
    NamedComponentReference<CasaEndpoint> getConsumer();
    
    void setProvider(NamedComponentReference<CasaEndpoint> endpoint);
    NamedComponentReference<CasaEndpoint> getProvider();
        
    String getState();
    void setState(String state);
}
