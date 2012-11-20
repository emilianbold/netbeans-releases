/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author vita
 */
@ServiceProvider(service=ClassPathProvider.class)
public final class JsClassPathProvider implements ClassPathProvider {

    public static final String BOOT_CP = "JavascriptBootClassPath"; //NOI18N

    private static URL jsStubs;
    private static ClassPath bootClassPath;

    public JsClassPathProvider() {

    }
    
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(BOOT_CP) ) {
            return getBootClassPath();
        } else {
            return null;
        }
    }

    public static synchronized ClassPath getBootClassPath() {
        if (bootClassPath == null) {
            URL jsstubs = getJsStubs();
            if (jsstubs != null) {
                bootClassPath = ClassPathSupport.createClassPath(jsstubs);
            }
        }
        return bootClassPath;
    }

    // TODO - add classpath recognizer for these ? No, don't need go to declaration inside these files...
    private static URL getJsStubs() {
        if (jsStubs == null) {
            // Core classes: Stubs generated for the "builtin" Ruby libraries.
            File allstubs = InstalledFileLocator.getDefault().locate("jsstubs/allstubs.zip", "org.netbeans.modules.javascript.editing", false);
            if (allstubs == null) {
                // Probably inside unit test.
                try {
                    File moduleJar = new File(JsClassPathProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                    allstubs = new File(moduleJar.getParentFile().getParentFile(), "jsstubs/allstubs.zip");
                } catch (URISyntaxException x) {
                    assert false : x;
                    return null;
                }
            }
            assert allstubs.isFile() : allstubs;
            try {
                jsStubs = FileUtil.getArchiveRoot(Utilities.toURI(FileUtil.normalizeFile(allstubs)).toURL());
            } catch (MalformedURLException ex) {
                assert false : FileUtil.normalizeFile(allstubs);
                return null;
            }
        }
        return jsStubs;
    }
}
