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

package org.netbeans.modules.j2ee.ejbjar;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;

public class ProjectEjbJarProvider implements EjbJarProvider {
    
    public ProjectEjbJarProvider () {
    }
    
    public org.netbeans.modules.j2ee.api.ejbjar.EjbJar findEjbJar (org.openide.filesystems.FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null) {
            EjbJarProvider provider = (EjbJarProvider) project.getLookup ().lookup (EjbJarProvider.class);
            if (provider != null) {
                return provider.findEjbJar (file);
            }
        }
        return null;
    }
}
