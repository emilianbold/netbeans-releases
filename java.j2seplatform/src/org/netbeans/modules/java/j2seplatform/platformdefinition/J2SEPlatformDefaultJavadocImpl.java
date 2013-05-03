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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.spi.J2SEPlatformDefaultJavadoc;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default Javadoc for J2SE.
 * @author Tomas Zezula
 */
@ServiceProvider(service =J2SEPlatformDefaultJavadoc.class, position = 100, path = "org-netbeans-api-java/platform/j2seplatform/defaultJavadocProviders")
public final class J2SEPlatformDefaultJavadocImpl implements J2SEPlatformDefaultJavadoc {

    private static final Logger LOG = Logger.getLogger(J2SEPlatformDefaultJavadocImpl.class.getName());
    private static final Map<String,String> OFFICIAL_JAVADOC = new HashMap<String,String>();
    static {
        OFFICIAL_JAVADOC.put("1.0", null); // NOI18N
        OFFICIAL_JAVADOC.put("1.1", null); // NOI18N
        OFFICIAL_JAVADOC.put("1.2", null); // NOI18N
        OFFICIAL_JAVADOC.put("1.3", "http://download.oracle.com/javase/1.3/docs/api/"); // NOI18N
        OFFICIAL_JAVADOC.put("1.4", "http://download.oracle.com/javase/1.4.2/docs/api/"); // NOI18N
        OFFICIAL_JAVADOC.put("1.5", "http://download.oracle.com/javase/1.5.0/docs/api/"); // NOI18N
        OFFICIAL_JAVADOC.put("1.6", "http://download.oracle.com/javase/6/docs/api/"); // NOI18N
        OFFICIAL_JAVADOC.put("1.7", "http://download.oracle.com/javase/7/docs/api/"); // NOI18N
    }

    @Override
    public Collection<URI> getDefaultJavadoc(@NonNull final JavaPlatform platform) {
        final List<URI> result = new ArrayList<>();
        for (FileObject folder : platform.getInstallFolders()) {
            // XXX should this rather be docs/api?
            FileObject docs = folder.getFileObject("docs"); // NOI18N
            if (docs != null && docs.isFolder() && docs.canRead()) {
                result.add(docs.toURI());
            }
            if (Utilities.isMac()) {
                try {
                    final FileObject docsJar = folder.getFileObject("docs.jar"); //NOI18N
                    //XXX: Verify zip integrity? But it's slow
                    //Better not to do it, only test FileUtil.isArchoveFile
                    if (docsJar != null && docsJar.canRead() && FileUtil.isArchiveFile(docsJar)) {
                        final URL rootURL = FileUtil.getArchiveRoot(docsJar.toURL());
                        result.add(new URL(rootURL.toExternalForm() + "docs/api/").toURI());        //NOI18N
                        result.add(new URL(rootURL.toExternalForm() + "docs/jdk/api/").toURI());    //NOI18N
                        result.add(new URL(rootURL.toExternalForm() + "docs/jre/api/").toURI());    //NOI18N
                    }
                    final FileObject appleDocsJar = folder.getFileObject("appledocs.jar");    //NOI18N
                    //XXX: Verify zip integrity? But will slowdown the DefaultPlatform constructor.
                    //Better not to do it, only test FileUtil.isArchoveFile
                    if (appleDocsJar != null && appleDocsJar.canRead() && FileUtil.isArchiveFile(appleDocsJar)) {
                        result.add(new URL (FileUtil.getArchiveRoot(appleDocsJar.toURL()).toExternalForm() + "appledoc/api/").toURI());    //NOI18N
                    }
                } catch (MalformedURLException | URISyntaxException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
        if (!result.isEmpty()) {
            return Collections.unmodifiableList(result);
        }
        String version = platform.getSpecification().getVersion().toString();
        if (!OFFICIAL_JAVADOC.containsKey(version)) {
            LOG.log(Level.WARNING, "unrecognized Java spec version: {0}", version);
        }
        String location = OFFICIAL_JAVADOC.get(version);
        if (location != null) {
            try {
                return Collections.singletonList(new URI(location));
            } catch (URISyntaxException x) {
                LOG.log(Level.INFO, null, x);
            }
        }
        return Collections.emptyList();
    }

}
