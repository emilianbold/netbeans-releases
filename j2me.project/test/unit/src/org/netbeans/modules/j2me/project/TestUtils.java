/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2me.project;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Tomas Zezula
 */
public final class TestUtils {

    private TestUtils() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    @CheckForNull
    public static JavaPlatform findMEPlatform() {
        for (JavaPlatform jp : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            if ("j2me".equals(jp.getSpecification().getName())) {   //NOI18N
               return jp;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <R,E extends Throwable> R sneaky(Throwable t) throws E {
        throw (E) t;
    }

    @CheckForNull
    public static JavaPlatform findSEPlatfrom() {
        return JavaPlatform.getDefault();
    }

    @NonNull
    private static ClassPath defaultBootstrap() {
        final String path = System.getProperty("sun.boot.class.path");  //NOI18N
        return
            path == null ?
            ClassPath.EMPTY :
            ClassPathSupport.createClassPath(path);
    }

    private static FileObject defaultInstallFolder() {
        final String jrePath = System.getProperty("java.home"); //NOI18N
        final File jre = new File (jrePath);
        return FileUtil.toFileObject(jre.getParentFile());
    }

    public static final class MockJavaPlatformProvider implements JavaPlatformProvider {
        
        private final FileObject j2meHome;
        private final JavaPlatform[] platforms;

        public MockJavaPlatformProvider(File j2meEmulator) throws IOException {
            j2meHome = FileUtil.createFolder(j2meEmulator);
            Assert.assertNotNull(j2meHome);
            platforms = new JavaPlatform[] {
                new MockJavaPlatform(
                    "Default",  //NOI18N
                    new Specification(
                        "j2se", //NOI18N
                        new SpecificationVersion(System.getProperty("java.specification.version"))),    //NOI18N
                    defaultInstallFolder(),
                    defaultBootstrap()),
                new MockJavaPlatform(
                    "ME Platform",  //NOI18N
                    new Specification(
                        "j2me", //NOI18N
                        new SpecificationVersion("8.0")),   //NOI18N
                    j2meHome,

                    ClassPath.EMPTY)
            };
        }         

        @Override
        public JavaPlatform[] getInstalledPlatforms() {
            return Arrays.copyOf(platforms, platforms.length);
        }

        @Override
        public JavaPlatform getDefaultPlatform() {
            return platforms[0];
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

    private static final class MockJavaPlatform extends JavaPlatform {

        private final FileObject installFolder;
        private final String displayName;
        private final Specification spec;
        private final ClassPath bootstrap;

        MockJavaPlatform(
            final String displayName,
            final Specification spec,
            final FileObject installFolder,
            final ClassPath bootstrap) {
            this.displayName = displayName;
            this.spec = spec;
            this.installFolder = installFolder;
            this.bootstrap = bootstrap;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.singletonMap(
                "platform.ant.name",    //NOI18N
                PropertyUtils.getUsablePropertyName(displayName));
        }

        @Override
        public ClassPath getBootstrapLibraries() {
            return bootstrap;
        }

        @Override
        public ClassPath getStandardLibraries() {
            return ClassPath.EMPTY;
        }

        @Override
        public String getVendor() {
            return "";  //NOI18N
        }

        @Override
        public Specification getSpecification() {
            return spec;
        }

        @Override
        public Collection<FileObject> getInstallFolders() {
            return Collections.<FileObject>singleton(installFolder);
        }

        @Override
        public FileObject findTool(String toolName) {
            return null;
        }

        @Override
        public ClassPath getSourceFolders() {
            return ClassPath.EMPTY;
        }

        @Override
        public List<URL> getJavadocFolders() {
            return Collections.<URL>emptyList();
        }
    }

}
