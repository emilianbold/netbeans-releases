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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;

/**
 *
 * @author  tom
 */
public class J2SEPlatformSourceLevelQueryImpl implements SourceLevelQueryImplementation {
    
    /** Creates a new instance of J2SEPlatformSourceLevelQueryImpl */
    public J2SEPlatformSourceLevelQueryImpl() {
    }
    
    public String getSourceLevel(org.openide.filesystems.FileObject javaFile) {
        try {
        } catch (Exception e) {}
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms ();
        for (int i=0; i< platforms.length; i++) {
            if (platforms[i].getSourceFolders().contains(javaFile)) {
                return platforms[i].getSpecification().getVersion().toString();
            }
        }        
        return null;
    }    
        
            
}
