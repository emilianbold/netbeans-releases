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
 * WebServiceProvider.java
 *
 * Created on August 26, 2005, 10:54 AM
 *
 */
package org.netbeans.modules.mobility.end2end.providers;

import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.codegenerator.ConnectionGenerator;
import org.netbeans.modules.mobility.end2end.multiview.DDView;
import org.netbeans.spi.mobility.end2end.E2EServiceProvider;
import org.netbeans.spi.mobility.end2end.ServiceGeneratorResult;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.openide.util.Lookup;

/**
 *
 * @author suchys
 */
public class WebServiceProvider implements E2EServiceProvider{
        
    public String getServiceType() {
        return Configuration.WSDLCLASS_TYPE;
    }
    
    public DesignMultiViewDesc[] getMultiViewDesc(final Lookup lookup) {
        final E2EDataObject doj = lookup.lookup(E2EDataObject.class);
        return new DesignMultiViewDesc[] {
            new DDView( doj, DDView.MULTIVIEW_CLIENT ),
            new DDView( doj, DDView.MULTIVIEW_SERVER )
        };
    }
    
    public ServiceGeneratorResult generateStubs(final Lookup lookup) {
        return ConnectionGenerator.generate(lookup.lookup(E2EDataObject.class));
    }
}
