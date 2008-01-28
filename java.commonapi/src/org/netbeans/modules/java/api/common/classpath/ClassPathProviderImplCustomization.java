/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.api.common.classpath;

import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 * Customization of {@link ClassPathProviderImpl} which has to used by project types.
 * @author Tomas Mysik
 * @since org.netbeans.modules.java.api.common/0 1.0
 */
public interface ClassPathProviderImplCustomization {

    /**
     * Get the directory representing files for the given {@link ClassPathProviderImpl.Path path}.
     * @param path the {@link ClassPathProviderImpl.Path path} of e.g. classes of the given project.
     * @return {@link FileObject} representing the directory.
     */
    FileObject getDirectory(ClassPathProviderImpl.Path path);

    /**
     * Get the classpath for compiling of the given file type. Typically this can be source or test source.
     * @param type the {@link ClassPathProviderImpl.FileType type} the classpath is searched for.
     * @param context context provided by {@link ClassPathProviderImpl}.
     * @return the classpath for compiling.
     */
    ClassPath getCompileTimeClasspath(ClassPathProviderImpl.FileType type, final ClassPathProviderImpl.Context context);

    /**
     * Get the classpath for executing of the given file type. Typically this can be source or test source.
     * @param type the {@link ClassPathProviderImpl.FileType type} the classpath is searched for.
     * @param context context provided by {@link ClassPathProviderImpl}.
     * @return the classpath for executing.
     */
    ClassPath getRunTimeClasspath(ClassPathProviderImpl.FileType type, final ClassPathProviderImpl.Context context);

    /**
     * Get the classpath for sources of the given file type. Typically this can be source or test source.
     * @param type the {@link ClassPathProviderImpl.FileType type} the classpath is searched for.
     * @param context context provided by {@link ClassPathProviderImpl}.
     * @return the classpath for sources.
     */
    ClassPath getSourcePath(ClassPathProviderImpl.FileType type, final ClassPathProviderImpl.Context context);

    // XXX will be moved to j2ee/utilities or similar
    /**
     * Get the classpath for the active Java EE platform.
     * @param context context provided by {@link ClassPathProviderImpl}.
     * @return the classpath for compiling.
     */
    ClassPath getJ2eePlatformClassPath(final ClassPathProviderImpl.Context context);

    /**
     * Get the name of the classpath property.
     * @param classpathProperty classpath property.
     * @return the property name.
     */
    String getPropertyName(ClassPathProviderImpl.ClasspathProperty classpathProperty);
}
