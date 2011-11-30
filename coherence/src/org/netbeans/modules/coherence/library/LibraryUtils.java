/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.library;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.netbeans.modules.coherence.server.util.Version;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class LibraryUtils {

    private static final Logger LOGGER = Logger.getLogger(LibraryUtils.class.getName());

    public static final String LIBRARY_BASE_NAME = "Coherence"; //NOI18N

    /**
     * Suggests Coherence library name.
     *
     * @param version Coherence server version
     * @return Coherence library name
     */
    public static String getCoherenceLibraryDisplayName(Version version) {
        if (version == null) {
            return LIBRARY_BASE_NAME;
        }

        StringBuilder nameSB = new StringBuilder(LIBRARY_BASE_NAME);
        nameSB.append(" ").append(version.getReleaseVersion()); //NOI18N
        return nameSB.toString().trim();
    }

    /**
     * Creates new Coherence library if not exists library of the same name.
     *
     * @param libraryDisplayName display name of the Coherence library (library name is parsed from it)
     * @param serverRoot directory root of Coherence server
     * @return {@code true} if new library was created in the IDE, {@code false} otherwise
     */
    public static boolean registerCoherenceLibrary(String libraryDisplayName, File serverRoot) {
        String libraryName = parseLibraryName(libraryDisplayName);
        if (LibraryManager.getDefault().getLibrary(libraryName) != null) {
            return false;
        }

        URI coherenceServerURI = CoherenceProperties.getCoherenceJar(serverRoot).toURI();
        Map<String, List<URI>> content = new HashMap<String, List<URI>>();
        content.put("classpath", Collections.<URI>singletonList(coherenceServerURI)); //NOI18N
        try {
            LibraryManager.getDefault().createURILibrary(
                    "j2se", //NOI18N
                    libraryName,
                    libraryDisplayName,
                    NbBundle.getMessage(LibraryUtils.class, "DESC_CoherenceLibraryDescription"), //NOI18N
                    content);
            LOGGER.log(Level.FINE, "Created Coherence library: name={0}, displayName={1}, cp={2}.",
                    new Object[]{libraryName, libraryDisplayName, coherenceServerURI.toString()});
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Gets library name for library display name
     * @return library name
     */
    protected static String parseLibraryName(String displayName) {
        return displayName.replace(" ", "-").toLowerCase(); //NOI18N
    }
}
