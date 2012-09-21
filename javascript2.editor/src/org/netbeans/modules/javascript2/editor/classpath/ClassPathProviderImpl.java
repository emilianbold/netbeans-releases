/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.classpath;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Defines classpaths (boot CP) of JavaScript files.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@ServiceProvider(service=ClassPathProvider.class)
public class ClassPathProviderImpl implements ClassPathProvider {

    private static final Logger LOG = Logger.getLogger(ClassPathProviderImpl.class.getName());
    public static final String BOOT_CP = "classpath/javascript-boot"; //NOI18N
    public static final AtomicBoolean JS_CLASSPATH_REGISTERED = new AtomicBoolean(false);

    // GuardedBy(this)
    private static ClassPath cachedBootClassPath;

    private static FileObject jsStubsFileObject;

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(BOOT_CP)) {
            return getBootClassPath();
        }
        return null;
    }

    public static synchronized ClassPath getBootClassPath() {
        if (cachedBootClassPath == null) {
            cachedBootClassPath = ClassPathSupport.createClassPath(getJsStubs());
        }
        return cachedBootClassPath;
    }

    protected static synchronized FileObject getJsStubs() { // protect for tests
        if (jsStubsFileObject == null) {
            // Stubs generated for the "built-in" JavaScript libraries.
            File allstubs = InstalledFileLocator.getDefault().locate(
                    "jsstubs/allstubs.zip", "org.netbeans.modules.javascript2.editor", false); //NOI18N
            if (allstubs == null) {
                // Probably inside unit test.
                try {
                    File moduleJar = Utilities.toFile(ClassPathProviderImpl.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                    allstubs = new File(moduleJar.getParentFile().getParentFile(), "jsstubs/allstubs.zip"); //NOI18N
                } catch (URISyntaxException x) {
                    assert false : x;
                    return null;
                }
            }
            if (!allstubs.isFile() || !allstubs.exists()) {
                LOG.log(Level.WARNING, "JavaScript signature files were not found: {0}", allstubs.getAbsolutePath());
                return null;
            }
            jsStubsFileObject = FileUtil.getArchiveRoot(FileUtil.toFileObject(allstubs));
        }
        return jsStubsFileObject;
    }

    /**
     * Registers JavaScript classpath if not already done.
     *<p>
     * Class synchronized since more language instances can be created in an undefined way.
     *<p>
     * The registration is done lazily in EDT task so it is not ensured that
     * the JavaScript classpath is properly initialized after returning from this method.
     *<p>
     * The JavaScript classpath unregistration is done in module's install class.
     */
    public static synchronized void registerJsClassPathIfNeeded() {
        if(!JS_CLASSPATH_REGISTERED.get()) {
            JS_CLASSPATH_REGISTERED.set(true);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ClassPath cp = ClassPathProviderImpl.getBootClassPath();
                    if (cp != null) {
                        GlobalPathRegistry.getDefault().register(ClassPathProviderImpl.BOOT_CP, new ClassPath[]{cp});
                    }
                }
            });
        }
        
        
    }
}
