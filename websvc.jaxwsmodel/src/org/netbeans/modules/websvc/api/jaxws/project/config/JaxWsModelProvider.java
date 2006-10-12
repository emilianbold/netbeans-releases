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

package org.netbeans.modules.websvc.api.jaxws.project.config;

import java.io.IOException;
import java.io.InputStream;
import org.openide.filesystems.FileObject;

/** Accessor for JaxWsModel
 *
 * @author mkuchtiak
 */
public class JaxWsModelProvider {
    
    private static JaxWsModelProvider provider;
    
    /** Creates a new instance of ModelProvider */
    private JaxWsModelProvider() {
    }
    
    public static synchronized JaxWsModelProvider getDefault() {
        if (provider==null) {
            provider = new JaxWsModelProvider();
        }
        return provider;
    }
    
    public JaxWsModel getJaxWsModel(InputStream is) throws IOException {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs impl = 
                org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs.createGraph(is);
        return (impl==null?null:new JaxWsModel(impl));
    }
    
    public JaxWsModel getJaxWsModel(FileObject fo) throws IOException {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs impl = 
                org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs.createGraph(fo.getInputStream());
        return (impl==null?null:new JaxWsModel(impl,fo));
    }
}
