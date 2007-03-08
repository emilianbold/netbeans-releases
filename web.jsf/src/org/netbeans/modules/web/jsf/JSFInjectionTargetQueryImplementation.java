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

package org.netbeans.modules.web.jsf;

import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.common.queries.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Pisl
 */

public class JSFInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    public JSFInjectionTargetQueryImplementation() {
    }
    
    /** 
     * For method return true if:
     *      1) The web module follows 2.5 servlet specification or higher
     *      2) The jc is defined as manage bean in a jsp configuration file.
     */
    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        Parameters.notNull("controller", controller);
        Parameters.notNull("typeElement", typeElement);
        
        // Find the web module, where the class is
        WebModule webModule  = WebModule.getWebModule(controller.getFileObject());
        // Is the web modile 2.5 servlet spec or higher?
        if (webModule != null && !webModule.getJ2eePlatformVersion().equals(WebModule.J2EE_13_LEVEL)
                && !webModule.getJ2eePlatformVersion().equals(WebModule.J2EE_14_LEVEL)){
            // Get deployment desctriptor from the web module
            FileObject ddFileObject = webModule.getDeploymentDescriptor();
            if (ddFileObject != null){
                // Get all jsf configurations files
                FileObject[] jsfConfigs = ConfigurationUtils.getFacesConfigFiles(webModule);
                for (FileObject jsfConfigFO : ConfigurationUtils.getFacesConfigFiles(webModule)) {
                    // Get manage beans from the configuration file
                    List<ManagedBean> beans = ConfigurationUtils.getConfigModel(jsfConfigFO, true).getRootComponent().getManagedBeans();
                    for (ManagedBean managedBean : beans) {
                        if (typeElement.getQualifiedName().contentEquals(managedBean.getManagedBeanClass())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        return false;
    }
    
}
