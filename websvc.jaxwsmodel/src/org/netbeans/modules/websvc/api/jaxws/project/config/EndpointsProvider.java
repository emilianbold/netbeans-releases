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
 * EndpointsProvider.java
 *
 * Created on March 19, 2006, 8:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

import java.io.IOException;
import java.io.InputStream;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Roderico Cruz
 */
public class EndpointsProvider {
    
    private static EndpointsProvider  provider;
    
    /** Creates a new instance of HandlerChainsProvider */
    private EndpointsProvider() {
    }
    
    public static synchronized EndpointsProvider getDefault() {
        if (provider==null) {
            provider = new EndpointsProvider();
        }
        return provider;
    }
    
    public Endpoints getEndpoints(InputStream is) throws IOException {
        org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints impl = 
                org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints.createGraph(is);
        return (impl==null?null:new Endpoints(impl));
    }
    
    public Endpoints getEndpoints(FileObject fo) throws IOException {
        org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints impl = 
                org.netbeans.modules.websvc.jaxwsmodel.endpoints_config1_0.Endpoints.createGraph(fo.getInputStream());
        return (impl==null?null:new Endpoints(impl));
    }
}
