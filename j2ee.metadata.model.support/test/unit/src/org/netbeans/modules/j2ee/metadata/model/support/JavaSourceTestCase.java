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

package org.netbeans.modules.j2ee.metadata.model.support;

import java.io.File;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public abstract class JavaSourceTestCase extends NbTestCase {

    protected static FileObject srcFO;

    protected static ClassPath srcCP;
    protected static ClassPath compileCP;
    protected static ClassPath bootCP;

    public JavaSourceTestCase(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        File userdir = new File(getWorkDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        File src = new File(getWorkDir(), "src");
        src.mkdirs();
        srcFO = FileUtil.toFileObject(src);
        srcCP = ClassPathSupport.createClassPath(new URL[] { src.toURI().toURL() });
        compileCP = additionalCompilePath();
        bootCP = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        MockServices.setServices(ClassPathProviderImpl.class);
    }

    protected void tearDown() {
        MockServices.setServices();
    }

    protected ClassPath additionalCompilePath() {
        return ClassPathSupport.createClassPath(new URL[0]);
    }

    public static class ClassPathProviderImpl implements ClassPathProvider {

        public ClassPath findClassPath(FileObject file, String type) {
            if (!srcFO.equals(file) && !FileUtil.isParentOf(srcFO, file)) {
                return null;
            }
            if (ClassPath.SOURCE.equals(type)) {
                return srcCP;
            } else if (ClassPath.COMPILE.equals(type)) {
                return compileCP;
            } else if (ClassPath.BOOT.equals(type)) {
                return bootCP;
            }
            return null;
        }
    }
}
