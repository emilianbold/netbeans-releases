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

package org.netbeans.modules.maven.osgi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.osgi.util.ManifestElement;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.osgi.framework.BundleException;

/**
 *
 * @author mkleint
 */
@ServiceProvider(service = AccessibilityQueryImplementation.class)
public class OSGiJarAccessibilityQueryImpl implements AccessibilityQueryImplementation {
    private static final Logger LOG = Logger.getLogger(OSGiJarAccessibilityQueryImpl.class.getName());

    private final WeakHashMap<FileObject, List<ManifestElement>> publicCache = new WeakHashMap<FileObject, List<ManifestElement>>();
    private final List<ManifestElement> NOT_OSGIJAR = new ArrayList<ManifestElement>();
    
    @Override
    public Boolean isPubliclyAccessible(FileObject pkg) {
        FileObject jarFile = FileUtil.getArchiveFile(pkg);
        if (jarFile != null) {
            FileObject jarRoot = FileUtil.getArchiveRoot(jarFile);
            synchronized (publicCache) {
                List<ManifestElement> pub = publicCache.get(jarRoot);
                
                if (pub != null) {
                    if (pub == NOT_OSGIJAR) {
                        return null;
                    }
                    return check(pub, FileUtil.getRelativePath(jarRoot, pkg).replace("/", "."));
                }
            }
            FileObject manifest = jarRoot.getFileObject("META-INF/MANIFEST.MF");
            if (manifest != null) {
                try {
                    Manifest mf = new Manifest(manifest.getInputStream());
                    List<ManifestElement> pub = null;
                    String exportPack = mf.getMainAttributes().getValue(OSGiConstants.EXPORT_PACKAGE);
                    if (exportPack != null) {
                        try {
                            ManifestElement[] mans = ManifestElement.parseHeader(OSGiConstants.EXPORT_PACKAGE, exportPack);
                            pub = Arrays.asList(mans);
                        } catch (BundleException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    if (pub != null) {
                        synchronized (publicCache) {
                            publicCache.put(jarRoot, pub);
                        }
                        return check(pub, FileUtil.getRelativePath(jarRoot, pkg).replace("/", "."));
                    }
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "cannot read manifest", ex);
                }
            }
            synchronized (publicCache) {
                publicCache.put(jarRoot, NOT_OSGIJAR);
            }
        }
        return null;
        
    }

    private Boolean check(List<ManifestElement> pub, String packageName) {
        for (ManifestElement p : pub) {
            if (packageName.equals(p.getValue())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

}
