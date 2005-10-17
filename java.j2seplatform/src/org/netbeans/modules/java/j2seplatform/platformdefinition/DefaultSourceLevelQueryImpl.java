/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Returns the source level for the non projectized java/class files (those
 * file for which the classpath is provided by the {@link DefaultClassPathProvider}
 * @author Tomas Zezula
 */
public class DefaultSourceLevelQueryImpl implements SourceLevelQueryImplementation {
    
    private static final String JAVA_EXT = "java";  //NOI18N
    
    /** Creates a new instance of DefaultSourceLevelQueryImpl */
    public DefaultSourceLevelQueryImpl() {
    }

    public String getSourceLevel(final FileObject javaFile) {
        assert javaFile != null : "javaFile has to be non null";   //NOI18N
        String ext = javaFile.getExt();
        if (JAVA_EXT.equalsIgnoreCase (ext)) {
            JavaPlatform jp = JavaPlatformManager.getDefault().getDefaultPlatform();
            assert jp != null : "JavaPlatformManager.getDefaultPlatform returned null";     //NOI18N
            return jp.getSpecification().getVersion().toString();
        }
        return null;
    }
    
    
    
}
