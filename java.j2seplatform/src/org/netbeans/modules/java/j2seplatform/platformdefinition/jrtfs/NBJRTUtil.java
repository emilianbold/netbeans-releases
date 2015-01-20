/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seplatform.platformdefinition.jrtfs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.BaseUtilities;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public final class NBJRTUtil {

    static final String PROTOCOL = "nbjrt"; //NOI18N
    private static final String NIO_PROVIDER = "jrt-fs.jar";    //NOI18N
    private static final Logger LOG = Logger.getLogger(NBJRTUtil.class.getName());

    private NBJRTUtil() {
        throw new IllegalStateException("No instance allowed");
    }

    @CheckForNull
    public static URI getImageURI(@NonNull final File file) {
        if (!file.isFile() || !file.getName().endsWith(".jimage")) {    //NOI18N
            return null;
        }
        File jdkHome = file;
        for (int i=0; i<3; i++) {
            jdkHome = jdkHome.getParentFile();
            if (jdkHome == null) {
                return null;
            }
        }
        final File jrtFsJar = getNIOProvider(jdkHome);
        try {
            return jrtFsJar == null ?
                null :
                createURI(jdkHome, "");  //NOI18N
        } catch (URISyntaxException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }

    @NonNull
    public static URI createURI(
        @NonNull final File jdkHome,
        @NonNull final String pathInImageFile) throws URISyntaxException {
         return new URI(String.format(
            "%s:%s!/%s",  //NOI18N
            PROTOCOL,
            BaseUtilities.toURI(jdkHome).toString(),
            pathInImageFile));
    }

    @CheckForNull
    public static Pair<File,String> parseURI(@NonNull final URI uri) {
        try {
            return parseURL(uri.toURL());
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    @CheckForNull
    static Pair<File,String> parseURL(@NonNull final URL url) {
        if (PROTOCOL.equals(url.getProtocol())) {
            final String path = url.getPath();
            if (!path.startsWith("file:")) {    //NOI18N
                throw new IllegalArgumentException(String.format(
                    "Invalid %s URI: %s",   //NOI18N
                    PROTOCOL,
                    url.toExternalForm()
                ));
            }
            final int beginIndex = 5; //"file:"
            final int bangSlash = path.indexOf("!/");
            if (bangSlash <= 0) {
                throw new IllegalArgumentException(String.format(
                    "Invalid %s URI: %s",   //NOI18N
                    PROTOCOL,
                    url.toExternalForm()
                ));
            }
            final File root = new File(path.substring(beginIndex, bangSlash));
            final String pathInImage = path.substring(bangSlash + 2);
            return Pair.<File,String>of(root, pathInImage);
        }
        return null;
    }

    @CheckForNull
    static File getNIOProvider(@NonNull final File jdkHome) {
        final File jrtFsJar = new File(jdkHome, NIO_PROVIDER);
        return jrtFsJar.exists() ?
            jrtFsJar :
            null;
    }
}
