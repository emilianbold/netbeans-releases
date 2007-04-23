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

package org.netbeans.modules.websvc.wsitconf.design;


import java.util.HashMap;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author rico
 */
public class SecurityConfigurationProvider implements WSConfigurationProvider {
    
    private HashMap<String, WSConfiguration> configProviders = new HashMap();
    
    /** Creates a new instance of SecurityConfigurationProvider */
    public SecurityConfigurationProvider() {
    }

    public synchronized WSConfiguration getWSConfiguration(Service service, FileObject implementationFile) {
        String key = "";
        if (service != null) key += service.getLocalWsdlFile();
        if (implementationFile != null) key += implementationFile.getPath();
        if (!configProviders.containsKey(key)) {
            configProviders.put(key, new SecurityConfiguration(service, implementationFile));
        }
        return configProviders.get(key);
    }
    
}
