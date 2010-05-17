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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.complib;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.jar.Manifest;

import org.netbeans.modules.visualweb.classloaderprovider.CommonClassloaderProvider;
import org.netbeans.modules.visualweb.complib.api.ComplibException;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;

/**
 * Represents an extension component library provided by a third party. There are two main use cases
 * for this class:
 * <nl>
 * <li>Creating from a package file</li>
 * <li>Creating from an existing expanded directory</li>
 * </nl>
 * 
 * <p>
 * The first case is used during initial component import in the UI which must be fast. At this time
 * only a limited amount of information from the complib manifest such as the component library
 * title can be accessed via a few methods. Calling other methods requires that the complib package
 * first be expanded. Once expanded, any method can be called.
 * </p>
 * 
 * <p>
 * The second case is used when a library is already installed and is persisted on disk in expanded
 * form.
 * </p>
 * 
 * @author Edwin Goei
 */
public class ExtensionComplib extends Complib {

    /**
     * ClassLoader used to load l10n resources from a *.complib file
     * 
     * @author Edwin Goei
     */
    private static class ResourceClassLoader extends URLClassLoader {

        public ResourceClassLoader(URL packageFileUrl) {
            super(new URL[0], ResourceClassLoader.class.getClassLoader());
            addURL(packageFileUrl);
        }
    }

    /**
     * ClassLoader used to load classes from an expanded component library
     * 
     * @author Edwin Goei
     */
    private static class LibraryClassLoader extends URLClassLoader {
        private static final ClassLoader parentClassLoader;
        static {
            /*
             * Use Java EE 5 common class loader as a superset. It contains classes from older EE
             * API versions, the Creator design-time API, and also classes that should not
             * technically be available but this is a convenient implementation. Code copied from
             * insync ModelSet().
             */
            CommonClassloaderProvider commonClassloaderProvider = null;

            Properties capabilities = new Properties();
            capabilities.put(CommonClassloaderProvider.J2EE_PLATFORM,
                    CommonClassloaderProvider.JAVA_EE_5);
            Result result = Lookup.getDefault().lookup(
                    new Lookup.Template(CommonClassloaderProvider.class));
            for (Iterator iterator = result.allInstances().iterator(); iterator.hasNext();) {
                CommonClassloaderProvider aCommonClassloaderProvider = (CommonClassloaderProvider) iterator
                        .next();
                if (aCommonClassloaderProvider.isCapableOf(capabilities)) {
                    commonClassloaderProvider = aCommonClassloaderProvider;
                    break;
                }
            }

            if (commonClassloaderProvider == null) {
                throw new RuntimeException("No Common Classloader Provider found."); // TODO I18N
            }

            parentClassLoader = commonClassloaderProvider.getClassLoader();
        }

        public LibraryClassLoader() {
            super(new URL[0], parentClassLoader);
        }

        public void appendToClassPath(List<File> path) {
            for (File file : path) {
                try {
                    super.addURL(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    // Should not normally happen, output warning
                    IdeUtil.logWarning(e);
                }
            }
        }
    }

    private static final String MANIFEST_FILE = "META-INF/MANIFEST.MF"; // NOI18N

    /** Component library package file which may always be null */
    private File packageFile;

    /**
     * Component library directory is absolute, may temporarily be null. Non-null value iff package
     * is expanded.
     */
    private File absoluteLibDir;

    /**
     * ClassLoader used to load resources referenced in package file metadata such as the manifest
     * and initial-palette config file. These resources can be accessed before or after a package is
     * expanded.
     */
    private ResourceClassLoader resourceClassLoader;

    /**
     * ClassLoader for loading classes in this complib. This can only be used after a package is
     * expanded.
     */
    private LibraryClassLoader libClassLoader;

    /**
     * @param absoluteLibDir
     *            lib directory root
     * @throws ComplibException
     * @throws IOException
     */
    ExtensionComplib(File absoluteLibDir) throws ComplibException, IOException {
        // The complib has already been expanded
        assert absoluteLibDir != null;
        File file = new File(absoluteLibDir, MANIFEST_FILE);
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        Manifest manifest = new Manifest(in);
        URL absLibDirUrl = absoluteLibDir.toURI().toURL();
        this.resourceClassLoader = new ResourceClassLoader(absLibDirUrl);
        ComplibManifest compLibManifest = ComplibManifest
                .getInstance(manifest, resourceClassLoader);
        initCompLibManifest(compLibManifest);
        in.close();

        this.absoluteLibDir = absoluteLibDir;

        // Convert String paths in ComplibManifest into File paths
        initPaths();
    }

    protected List<File> convertConfigPathToFileList(List<String> path) throws ComplibException {
        ArrayList<File> retVal = new ArrayList<File>(path.size());
        for (String pathElm : path) {
            File file = new File(absoluteLibDir, pathElm);
            if (!file.canRead()) {
                throw new ComplibException("Unable to read '" // NOI18N
                        + file + "'"); // NOI18N
            }
            retVal.add(file);
        }
        return retVal;
    }

    protected File[] convertConfigPathToFileArray(List<String> path) throws ComplibException {
        File[] retVal = new File[path.size()];
        for (int i = 0; i < path.size(); i++) {
            String pathElm = (String) path.get(i);
            File file = new File(absoluteLibDir, pathElm);
            if (!file.canRead()) {
                throw new ComplibException("Unable to read '" // NOI18N
                        + file + "'"); // NOI18N
            }
            retVal[i] = file;
        }
        return retVal;
    }

    /**
     * @return basename of this library's package file or null if none
     */
    String getPackageFileBaseName() {
        return (packageFile == null) ? null : packageFile.getName();
    }

    /**
     * Returns the base name of the root of this component library. The library must already be
     * expanded.
     * 
     * @return Returns the base name of this component library
     */
    public String getDirectoryBaseName() {
        return getDirectory().getName();
    }

    /**
     * Returns the absolute root of this component library. The library must already be expanded.
     * 
     * @return Returns the absolute root of this component library, never null
     */
    public File getDirectory() {
        return absoluteLibDir;
    }

    public ClassLoader getClassLoader() {
        if (libClassLoader == null) {
            libClassLoader = new LibraryClassLoader();
            libClassLoader.appendToClassPath(getRuntimePath());
            libClassLoader.appendToClassPath(getDesignTimePath());
            libClassLoader.appendToClassPath(getHelpPath());
        }
        return libClassLoader;
    }

    @Override
    BeanInfo getBeanInfo(String className) throws ClassNotFoundException, IntrospectionException {
        // 6393979 Simulate an appropriate context ClassLoader.
        // Temporarily set the context ClassLoader and restore it later.
        // TODO Possibly remove this code when class loading is fixed in Mako.
        ClassLoader origContextLoader = null;
        try {
            origContextLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(getClassLoader());

            // Do the real work here
            Class beanClass = Class.forName(className, true, getClassLoader());
            return Introspector.getBeanInfo(beanClass);
        } finally {
            Thread.currentThread().setContextClassLoader(origContextLoader);
        }
    }

    @Override
    public String toString() {
        return getIdentifier().toString();
    }
}
