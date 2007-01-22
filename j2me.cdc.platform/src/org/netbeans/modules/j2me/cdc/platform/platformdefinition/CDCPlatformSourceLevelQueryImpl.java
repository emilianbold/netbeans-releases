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

package org.netbeans.modules.j2me.cdc.platform.platformdefinition;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;

/**
 *
 * @author  tom
 */
public class CDCPlatformSourceLevelQueryImpl implements SourceLevelQueryImplementation {
    
    /** Creates a new instance of CDCPlatformSourceLevelQueryImpl */
    public CDCPlatformSourceLevelQueryImpl() {
    }
    
    public String getSourceLevel(org.openide.filesystems.FileObject javaFile) {
        try {
        } catch (Exception e) {}
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms ();
        for (JavaPlatform platform : platforms ) {
            if (CDCPlatform.PLATFORM_CDC.equalsIgnoreCase(platform.getSpecification().getName()) && platform.getSourceFolders().contains(javaFile)) {   //NOI18N
                return platform.getSpecification().getVersion().toString();
            }
        }        
        return null;
    }    
        
            
}
