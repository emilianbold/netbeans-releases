/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.platform;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;

/**
 * Basic impl in case no other providers can be found.
 * @author Jesse Glick
 */
public final class FallbackDefaultJavaPlatform extends JavaPlatform {
    
    private static FallbackDefaultJavaPlatform instance;

    private FallbackDefaultJavaPlatform() {
        setSystemProperties(NbCollections.checkedMapByFilter(System.getProperties(), String.class, String.class, false));
    }

    @Override
    public String getDisplayName() {
        return System.getProperty("java.vm.vendor") + " " + System.getProperty("java.vm.version"); // NOI18N
    }

    @Override
    public Map<String,String> getProperties() {
        return Collections.singletonMap("platform.ant.name", "default_platform");
    }

    private static ClassPath sysProp2CP(String propname) {
        String sbcp = System.getProperty(propname);
        if (sbcp == null) {
            return null;
        }
        List<URL> roots = new ArrayList<URL>();
        StringTokenizer tok = new StringTokenizer(sbcp, File.pathSeparator);
        while (tok.hasMoreTokens()) {
            File f = new File(tok.nextToken());
            if (!f.exists()) {
                continue;
            }
            URL u;
            try {
                File normf = FileUtil.normalizeFile(f);
                u = Utilities.toURI(normf).toURL();
            } catch (MalformedURLException x) {
                throw new AssertionError(x);
            } 
            if (FileUtil.isArchiveFile(u)) {
                u = FileUtil.getArchiveRoot(u);
            }
            roots.add(u);
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }

    private static ClassPath sampleClass2CP(Class prototype) {
        CodeSource cs = prototype.getProtectionDomain().getCodeSource();
        return ClassPathSupport.createClassPath(cs != null ? new URL[] {cs.getLocation()} : new URL[0]);
    }

    @Override
    public ClassPath getBootstrapLibraries() {
        // XXX ignore standard extensions etc.
        ClassPath cp = sysProp2CP("sun.boot.class.path"); // NOI18N
        return cp != null ? cp : sampleClass2CP(Object.class);
    }

    @Override
    public ClassPath getStandardLibraries() {
        ClassPath cp = sysProp2CP("java.class.path"); // NOI18N
        return cp != null ? cp : sampleClass2CP(/* likely in startup CP */ Dependency.class);
    }

    @Override
    public String getVendor() {
        return System.getProperty("java.vm.vendor");
    }

    @Override
    public Specification getSpecification() {
        return new Specification(/*J2SEPlatformImpl.PLATFORM_J2SE*/"j2se", Dependency.JAVA_SPEC); // NOI18N
    }

    @Override
    public Collection<FileObject> getInstallFolders() {
        return Collections.singleton(FileUtil.toFileObject(FileUtil.normalizeFile(new File(System.getProperty("java.home"))))); // NOI18N
    }

    @Override
    public FileObject findTool(String toolName) {
        return null; // XXX too complicated, probably unnecessary for this purpose
    }

    @Override
    public ClassPath getSourceFolders() {
        return ClassPathSupport.createClassPath(new URL[0]);
    }

    @Override
    public List<URL> getJavadocFolders() {
        return Collections.emptyList();
    }
    
    public static synchronized FallbackDefaultJavaPlatform getInstance() {
        if (instance == null) {
            instance = new FallbackDefaultJavaPlatform();
        }
        return instance;
    }

}
