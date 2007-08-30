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

package org.netbeans.modules.websvc.core.jaxws.projects;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportImpl;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** Lookup Provider for WS Support
 *
 * @author mkuchtiak
 */
public class J2SEWSSupportLookupProvider implements LookupProvider {
    
    /** Creates a new instance of JaxWSLookupProvider */
    public J2SEWSSupportLookupProvider() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        final Project project = baseContext.lookup(Project.class);
        JAXWSClientSupportImpl j2seJAXWSClientSupport = new J2SEProjectJAXWSClientSupport(project);
        JAXWSClientSupport jaxWsClientSupportApi = JAXWSClientSupportFactory.createJAXWSClientSupport(j2seJAXWSClientSupport);
        
        WebServicesClientSupportImpl jaxrpcClientSupport = new J2SEProjectJaxRpcClientSupport(project);
        WebServicesClientSupport jaxRpcClientSupportApi = WebServicesClientSupportFactory.createWebServicesClientSupport(jaxrpcClientSupport);
        
        return Lookups.fixed(new Object[] {jaxWsClientSupportApi,jaxRpcClientSupportApi,new J2SEProjectWSClientSupportProvider()});
    }
}