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

package org.netbeans.modules.web.project;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.project.spi.WebProjectImplementationFactory;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.Lookup;

public final class WebProjectType implements AntBasedProjectType {

    public static final String TYPE = "org.netbeans.modules.web.project";
    private static final String PROJECT_CONFIGURATION_NAME = "data";
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/web-project/3";
    private static final String PRIVATE_CONFIGURATION_NAME = "data";
    private static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/web-project-private/1";
    
    public static final String[] PROJECT_CONFIGURATION_NAMESPACE_LIST =         
            {"http://www.netbeans.org/ns/web-project/1",
            "http://www.netbeans.org/ns/web-project/2",
            "http://www.netbeans.org/ns/web-project/3"};

    /** Do nothing, just a service. */
    public WebProjectType() {}
    
    public String getType() {
        return TYPE;
    }
    
    public Project createProject(AntProjectHelper helper) throws IOException {
        for(WebProjectImplementationFactory factory : getProjectFactories()) {
            if (factory.acceptProject(helper)) {
                //delegate project completely to another implementation
                return factory.createProject(helper);
            }
        }
        return new WebProject(helper);
    }

    public String getPrimaryConfigurationDataElementName(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAME : PRIVATE_CONFIGURATION_NAME;
    }
    
    public String getPrimaryConfigurationDataElementNamespace(boolean shared) {
        return shared ? PROJECT_CONFIGURATION_NAMESPACE : PRIVATE_CONFIGURATION_NAMESPACE;
    }

    private Collection<WebProjectImplementationFactory> getProjectFactories() {
        return Lookup.getDefault().lookup(new Lookup.Template(WebProjectImplementationFactory.class)).allInstances();
    }
}
