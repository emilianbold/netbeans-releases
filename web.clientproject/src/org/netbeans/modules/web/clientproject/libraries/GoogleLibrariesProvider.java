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
package org.netbeans.modules.web.clientproject.libraries;

import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.NamedLibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns libraries from http://code.google.com/apis/libraries/devguide.html#Libraries
 * Hand written snapshot of these files is stored in resources/googlecdn.txt
 */
//@ServiceProvider(service = org.netbeans.spi.project.libraries.LibraryProvider.class)
public class GoogleLibrariesProvider implements LibraryProvider<LibraryImplementation> {

    @Override
    public LibraryImplementation[] getLibraries() {
        List<LibraryImplementation> libs = new ArrayList<LibraryImplementation>();
        BufferedReader is = new BufferedReader(new InputStreamReader(
                ClientSideProject.class.getResourceAsStream("resources/googlecdn.txt"))); // NOI18N
        try {
            String line;
            String dispName = "", name = "", versions = "", path = "", pathu = "", site = ""; // NOI18N
            while ((line = is.readLine()) != null) {
                if (line.startsWith("name:")) { // NOI18N
                    name = line.substring("name: ".length()); // NOI18N
                } else if (line.startsWith("latest version:")) { // NOI18N
                    versions = line.substring("latest version: ".length()); // NOI18N
                } else if (line.startsWith("path:")) { // NOI18N
                    path = line.substring("path: ".length()); // NOI18N
                } else if (line.startsWith("path(u):")) { // NOI18N
                    pathu = line.substring("path(u): ".length()); // NOI18N
                } else if (line.startsWith("site:")) { // NOI18N
                    site = line.substring("site: ".length()); // NOI18N
                } else if (line.trim().length() == 0) {
                    addLibraries(libs, dispName, name, versions, path, pathu, site);
                    dispName = ""; // NOI18N
                    name = ""; // NOI18N
                    versions = ""; // NOI18N
                    path = ""; // NOI18N
                    pathu = ""; // NOI18N
                    site = ""; // NOI18N
                } else {
                    dispName = line;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return libs.toArray(new LibraryImplementation[libs.size()]);
    }

    private void addLibraries(List<LibraryImplementation> libs, String dispName, String name,
                              String versions, String path, String pathu, String site) {
        String latestVersion = null;
        for (String version : versions.split(", ")) { // NOI18N
            if (latestVersion == null) {
                latestVersion = version;
            }
            LibraryImplementation3 l1 = (LibraryImplementation3) LibrariesSupport.createLibraryImplementation(
                    WebClientLibraryManager.TYPE, JavaScriptLibraryTypeProvider.VOLUMES);
            NamedLibraryImplementation named = (NamedLibraryImplementation) l1;
            l1.setName("google-"+name+"-"+version); // NOI18N
            named.setDisplayName("[Google] "+dispName+" "+version); // NOI18N
            Map<String, String> p = new HashMap<String, String>();
            p.put(WebClientLibraryManager.PROPERTY_VERSION, version);
            p.put(WebClientLibraryManager.PROPERTY_REAL_NAME, name);
            p.put(JavaScriptLibraryTypeProvider.PROPERTY_REAL_DISPLAY_NAME, dispName);
            p.put(JavaScriptLibraryTypeProvider.PROPERTY_CDN, "Google"); // NOI18N
            p.put(JavaScriptLibraryTypeProvider.PROPERTY_SITE, site);
            l1.setProperties(p);
        try {
                l1.setContent(WebClientLibraryManager.VOL_MINIFIED, 
                        Collections.singletonList(new URL(path.replace(latestVersion, version))));
                if (pathu.length() != 0) {
                    l1.setContent(WebClientLibraryManager.VOL_REGULAR, 
                            Collections.singletonList(new URL(pathu.replace(latestVersion, version))));
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            libs.add(l1);
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
}
