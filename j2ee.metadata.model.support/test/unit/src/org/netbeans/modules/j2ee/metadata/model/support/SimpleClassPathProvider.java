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

package org.netbeans.modules.j2ee.metadata.model.support;

import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Parameters;

/**
 *
 * @author Andrei Badea
 */
public class SimpleClassPathProvider implements ClassPathProvider {

    private final FileObject sourceRoot;
    private final ClassPath sourcePath;
    private final ClassPath compilePath;
    private final ClassPath bootPath;

    public SimpleClassPathProvider(FileObject sourceRoot) {
        this(sourceRoot, ClassPathSupport.createClassPath(new URL[0]), JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries());
    }

    public SimpleClassPathProvider(FileObject sourceRoot, ClassPath compilePath, ClassPath bootPath) {
        Parameters.notNull("sourceRoot", sourceRoot);
        Parameters.notNull("compilePath", compilePath);
        Parameters.notNull("bootPath", bootPath);
        this.sourceRoot = sourceRoot;
        this.sourcePath = ClassPathSupport.createClassPath(new URL[] { URLMapper.findURL(sourceRoot, URLMapper.INTERNAL) });
        this.compilePath = compilePath;
        this.bootPath = bootPath;
    }

    public ClassPath findClassPath(FileObject file, String type) {
        Parameters.notNull("file", file);
        if (sourceRoot.equals(file) || FileUtil.isParentOf(sourceRoot, file)) {
            if (ClassPath.SOURCE.equals(type)) {
                return sourcePath;
            } else if (ClassPath.COMPILE.equals(type)) {
                return compilePath;
            } else if (ClassPath.BOOT.equals(type)) {
                return bootPath;
            }
        }
        return null;
    }
}
