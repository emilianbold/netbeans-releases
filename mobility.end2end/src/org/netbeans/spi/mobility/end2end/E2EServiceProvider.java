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

/*
 * E2EServiceProvider.java
 *
 * Created on August 26, 2005, 10:45 AM
 *
 */
package org.netbeans.spi.mobility.end2end;

import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.openide.util.Lookup;

/**
 *
 * @author suchys
 */
public interface E2EServiceProvider {
    /**
     * @return service type
     * @see org.netbeans.modules.mobility.end2end.client.config.Configuration.CLASS_TYPE
     * @see org.netbeans.modules.mobility.end2end.client.config.Configuration.WSDLCLASS_TYPE
     * @see org.netbeans.modules.mobility.end2end.client.config.Configuration.JSR172_TYPE
     */
    public String getServiceType();
    
    /**
     * @param lookup singleton having E2EDataObject representing type to call generation on
     * @return visual representation in MultiView
     */
    public DesignMultiViewDesc[] getMultiViewDesc(Lookup lookup);
    
    
    /**
     * Calls the generation of stubs
     * @param lookup singleton having E2EDataObject representing type to call generation on
     * @return result of generation or null if failed
     */
    public ServiceGeneratorResult generateStubs(Lookup lookup);
    
}
