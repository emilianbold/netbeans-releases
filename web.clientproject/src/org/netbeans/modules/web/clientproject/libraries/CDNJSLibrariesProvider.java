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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
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
 * Returns libraries from http://cdnjs.com based on the snapshot of their sources.
 * Snapshot can be updated by running "ant -f web.clientproject/build.xml get-cdnjs-jar"
 * and is stored in resources/cdnjs.zip file.
 */
//@ServiceProvider(service = org.netbeans.spi.project.libraries.LibraryProvider.class)
public class CDNJSLibrariesProvider implements LibraryProvider<LibraryImplementation> {

    @Override
    public LibraryImplementation[] getLibraries() {
        List<LibraryImplementation> libs = new ArrayList<LibraryImplementation>();
        Map<String, List<String>> versions = new HashMap<String, List<String>>();
        ZipInputStream str = new ZipInputStream(new BufferedInputStream(
                ClientSideProject.class.getResourceAsStream("resources/cdnjs.zip"))); // NOI18N
        ZipEntry entry;
        try {
            while ((entry = str.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entry.isDirectory()) {
                    int i = entryName.indexOf("/ajax/libs/"); // NOI18N
                    if (i > 0) {
                        String lib = entryName.substring(i+"/ajax/libs/".length()); // NOI18N
                        if (lib.length() == 0) {
                            continue;
                        }
                        lib = lib.substring(0, lib.length()-1);
                        i = lib.indexOf("/"); // NOI18N
                        if (i > 0) {
                            String name = lib.substring(0, i);
                            String version = lib.substring(i+1);
                            if (version.indexOf('/') != -1) { // NOI18N
                                continue;
                            }
                            List<String> v = versions.get(name);
                            if (v == null) {
                                v = new ArrayList<String>();
                                versions.put(name, v);
                            }
                            v.add(version);
                        }
                    }
                } else if (entry.getName().endsWith(".json")) { // NOI18N
                    try {
                        addLibrary(libs, str, versions);
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if (str != null) {
                    str.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return libs.toArray(new LibraryImplementation[libs.size()]);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    private void addLibrary(List<LibraryImplementation> libs, ZipInputStream str, 
            Map<String, List<String>> versions) throws ParseException, IOException {
        Reader r = new InputStreamReader(str);
        JSONObject desc = (JSONObject)JSONValue.parseWithException(r);
        String name = (String)desc.get("name"); // NOI18N
        String version = (String)desc.get("version"); // NOI18N
        String file = (String)desc.get("filename"); // NOI18N
        String homepage = (String)desc.get("homepage"); // NOI18N
        String description = (String)desc.get("description"); // NOI18N
        if (version != null) {
            libs.add(createLibrary(name, version, file, homepage, description));
        }
        List<String> vers = versions.get(name);
        if (vers == null) {
            return;
        }
        for (String v : vers) {
            if (v.equals(version) || v == null) {
                continue;
            }
            libs.add(createLibrary(name, v, file, homepage, description));
        }
    }
    
    private LibraryImplementation createLibrary(String name, String version, String file, 
            String homepage, String description) {
        LibraryImplementation3 l1 = (LibraryImplementation3) LibrariesSupport.createLibraryImplementation(
                WebClientLibraryManager.TYPE, JavaScriptLibraryTypeProvider.VOLUMES);
        NamedLibraryImplementation named = (NamedLibraryImplementation) l1;
        l1.setName("cdnjs-"+name+"-"+version); // NOI18N
        named.setDisplayName("[CDNJS] "+name+" "+version); // NOI18N
        Map<String, String> p = new HashMap<String, String>();
        p.put(WebClientLibraryManager.PROPERTY_VERSION, version);
        p.put(WebClientLibraryManager.PROPERTY_REAL_NAME, name);
        p.put(JavaScriptLibraryTypeProvider.PROPERTY_REAL_DISPLAY_NAME, name);
        p.put(JavaScriptLibraryTypeProvider.PROPERTY_CDN, "CDNJS"); // NOI18N
        p.put(JavaScriptLibraryTypeProvider.PROPERTY_SITE, homepage);
        l1.setProperties(p);
        l1.setDescription(description);
        try {
            String path = "http://cdnjs.cloudflare.com/ajax/libs/"+name+"/"+version+"/"+file; // NOI18N
            l1.setContent(WebClientLibraryManager.VOL_MINIFIED, 
                    Collections.singletonList(new URL(path)));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return l1;
    }
}
