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

package org.netbeans.modules.visualweb.dataprovider.designtime;


import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openide.modules.ModuleInstall;


/**
 *
 * @author Peter Zavadsky
 */
public class Install extends ModuleInstall {

     /** Packages in this module that contain BeanInfo classes. */
    private static final String[] BEANINFO_PATHS = {
        "org.netbeans.modules.visualweb.dataprovider.designtime", // NOI18N
        "org.netbeans.modules.visualweb.dataprovider.designtime.impl" // NOI18N
    };


    public void restored() {
        // Add our beaninfo packages to introspector search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        List paths = Arrays.asList(sp);
        for( int i = 0; i < BEANINFO_PATHS.length; i++) {
            if(!paths.contains(BEANINFO_PATHS[i])) {
                paths = new ArrayList(paths);
                paths.add(BEANINFO_PATHS[i]);
            }
        }
        Introspector.setBeanInfoSearchPath((String[])paths.toArray(new String[paths.size()]));
    }

    public void uninstalled() {
        // Remove our beaninfo packages from the intropsector search path.
        String[] sp = Introspector.getBeanInfoSearchPath();
        List paths = Arrays.asList(sp);
        for (int i = 0; i < BEANINFO_PATHS.length; i++) {
            if(paths.contains(BEANINFO_PATHS[i])) {
                paths = new ArrayList(paths);
                paths.remove(BEANINFO_PATHS[i]);
            }
        }
        Introspector.setBeanInfoSearchPath((String[])paths.toArray(new String[paths.size()]));
    }

}
