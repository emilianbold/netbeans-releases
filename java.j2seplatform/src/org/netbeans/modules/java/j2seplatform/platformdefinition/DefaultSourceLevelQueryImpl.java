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

    public DefaultSourceLevelQueryImpl() {}

    public String getSourceLevel(final FileObject javaFile) {
        assert javaFile != null : "javaFile has to be non null";   //NOI18N
        String ext = javaFile.getExt();
        if (JAVA_EXT.equalsIgnoreCase (ext)) {
            JavaPlatform jp = JavaPlatformManager.getDefault().getDefaultPlatform();
            assert jp != null : "JavaPlatformManager.getDefaultPlatform returned null";     //NOI18N
            String s = jp.getSpecification().getVersion().toString();
            if (s.equals("1.6") || s.equals("1.7")) {
                // #89131: these levels are not actually distinct from 1.5.
                return "1.5";
            } else {
                return s;
            }
        }
        return null;
    }

}
