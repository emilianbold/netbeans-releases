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

package org.netbeans.modules.java.j2seproject.api;

import java.util.Map;
import javax.swing.JComponent;

import org.netbeans.api.project.Project;

/**
 * Provider of component that will be added to Run customizer panel that will
 * be used for additional customization of set of properties affected by given
 * run configuration. Implementation of the interface should be registered to
 * default lookup (e.g. META-INF/services).
 * 
 * @author Milan Kubec
 * @since 1.10
 */
public interface J2SERunConfigProvider {
    
    /**
     * Provides component that is added to Run Customizer panel of j2seproject
     * 
     * @param proj project to create the customizer component for
     * @param listener listener to be notified when properties should be updated
     */
    JComponent createComponent(Project proj, ConfigChangeListener listener);
    
    /**
     * Method is called when the config is changed (or created), 
     * component is updated according to properties of the config
     * 
     * @param props all properties (shared + private) of the new config;
     *        properites are not evaluated
     */
    void configUpdated(Map<String,String> props);
    
    /**
     * Callback listener for setting properties that are changed by interaction 
     * with the component
     */
    interface ConfigChangeListener {
        /**
         * Method is called when properties should be updated, null prop value 
         * means property will be removed from the property file, only shared 
         * properties are updated; properties are not evaluated
         * 
         * @param updates map holding updated properties
         */
        void propertiesChanged(Map<String,String> updates);
    }
    
}
