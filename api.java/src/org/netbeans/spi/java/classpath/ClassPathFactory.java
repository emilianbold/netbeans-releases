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

package org.netbeans.spi.java.classpath;

import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.classpath.ClassPathAccessor;
import org.openide.filesystems.FileUtil;

/**
 * Most general way to create {@link ClassPath} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link ClassPathImplementation} and use this factory.
 * See also {@link org.netbeans.spi.java.classpath.support.ClassPathSupport}
 * for easier ways to create classpaths.
 * @since org.netbeans.api.java/1 1.4
 */
public final class ClassPathFactory {

    private ClassPathFactory() {
    }

    /**
     * Create API classpath instance for the given SPI classpath.
     * @param spiClasspath instance of SPI classpath
     * @return instance of API classpath
     */
    public static ClassPath createClassPath(ClassPathImplementation spiClasspath) {
//        assert checkEntries (spiClasspath) : "ClassPathImplementation contains invalid root: " + spiClasspath.toString();    //Commented, not to decrease the performance even in the dev build.
        return ClassPathAccessor.DEFAULT.createClassPath(spiClasspath);
    }
    
    
    private static boolean checkEntries (ClassPathImplementation spiClasspath) {
        for (PathResourceImplementation impl : spiClasspath.getResources()) {
            URL[] roots = impl.getRoots();
            for (URL root : roots) {
                if (FileUtil.isArchiveFile(root)) {
                    return false;
                }
                if (root.toExternalForm().endsWith("/")) {  // NOI18N
                    return false;
                }
            }
        }
        return true;
    }

}
