/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject;

import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

/** The util methods for projectui module.
 *
 * @author  Jiri Rechtacek
 */
public class J2SEProjectUtil {
    private J2SEProjectUtil () {}
    
    /** Returns the J2SEProject sources directory.
     *
     * @param p project
     * @return source directory or null if directory not set or if the project 
     * doesn't provide AntProjectHelper
     */    
    final public static FileObject getProjectSourceDirectory (Project p) {
        J2SEProject.AntProjectHelperProvider provider = (J2SEProject.AntProjectHelperProvider)p.getLookup ().lookup (J2SEProject.AntProjectHelperProvider.class);
        if (provider != null) {
            AntProjectHelper helper = provider.getAntProjectHelper ();
            assert helper != null : p;
            String srcDir = helper.getStandardPropertyEvaluator ().getProperty (J2SEProjectProperties.SRC_DIR);
            if (srcDir == null) {
                return null;
            }
            return helper.resolveFileObject (srcDir);
        } else {
            return null;
        }
    }
    
    /**
     * Returns the property value evaluated by J2SEProject's PropertyEvaluator.
     *
     * @param p project
     * @param properties project's j2seproperties
     * @param property name of property
     * @return evaluated value of given property or null if the property not set or
     * if the project doesn't provide AntProjectHelper
     */    
    final public static Object getEvaluatedProperty (Project p, J2SEProjectProperties properties, String property) {
        J2SEProject.AntProjectHelperProvider provider = (J2SEProject.AntProjectHelperProvider)p.getLookup ().lookup (J2SEProject.AntProjectHelperProvider.class);
        if (provider != null) {
            assert provider.getAntProjectHelper () != null : p;
            return provider.getAntProjectHelper ().getStandardPropertyEvaluator ().getProperty (property);
        } else {
            return properties.get (property);
        }
    }
}
