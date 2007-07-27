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

package org.netbeans.modules.j2ee.persistence.sourcetestsupport;

import java.io.Serializable;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * An implementation of ClassPathProvider for running tests. Includes <tt>toplink-essentials.jar</tt> that contains
 * <code>javax.persistence.*</code> stuff. 
 *
 * @author Erno Mononen
 */
public class ClassPathProviderImpl implements ClassPathProvider {
    
    private final ClassPath classPath;
    
    public ClassPathProviderImpl(FileObject[] sources){
        this.classPath = ClassPathSupport.createClassPath(sources);
    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        if(ClassPath.SOURCE.equals(type)){
            return this.classPath;
        }
        if (ClassPath.COMPILE.equals(type)){
            try {
                URL toplinkJarUrl = Class.forName("javax.persistence.EntityManager").getProtectionDomain().getCodeSource().getLocation();
                return ClassPathSupport.createClassPath(new URL[]{FileUtil.getArchiveRoot(toplinkJarUrl)});
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        if (ClassPath.BOOT.equals(type)){
            return JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        }
        return null;
    }
}
