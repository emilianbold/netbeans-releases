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
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.modules.web.clientproject.api.network.NetworkException;
import org.netbeans.modules.web.clientproject.api.network.NetworkSupport;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.netbeans.spi.project.libraries.NamedLibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

/**
 * Returns libraries from http://cdnjs.com based on the snapshot of their sources.
 * Snapshot can be updated by running "ant -f web.clientproject.api/build.xml get-cdnjs-jar"
 * and is stored in resources/cdnjs.zip file.
 */
//@ServiceProvider(service = org.netbeans.spi.project.libraries.LibraryProvider.class)
public class CDNJSLibrariesProvider implements EnhancedLibraryProvider<LibraryImplementation> {

    private static final Logger LOGGER = Logger.getLogger(CDNJSLibrariesProvider.class.getName());

    private static final String JSLIBS_CACHE_PATH = "html5/jslibs"; // NOI18N
    private static final String CDNJS_ZIP_FILENAME = "cdnjs.zip"; // NOI18N
    private static final String CDNJS_ZIP_TMP_FILENAME = "cdnjs-tmp.zip"; // NOI18N
    private static final String CDNJS_ZIP_URL = "https://github.com/cdnjs/cdnjs/zipball/master"; // NOI18N

    private static final CDNJSLibrariesProvider INSTANCE = new CDNJSLibrariesProvider();

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    private CDNJSLibrariesProvider() {
    }

    public static CDNJSLibrariesProvider getDefault() {
        return INSTANCE;
    }

    @Override
    public LibraryImplementation[] getLibraries() {
        List<LibraryImplementation> l = readLibraries(getLibraryZip(), null);
        return l.toArray(new LibraryImplementation[l.size()]);
    }

    public static List<LibraryImplementation> readLibraries(InputStream is, List<String> minifiedOrphanFiles) {
        Map<String, LibraryFiles> libs = new HashMap<>();
        ZipInputStream str = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry entry;
        try {
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String original = entry.getName();
                String entryName = original;
                int i = entryName.indexOf("/ajax/libs/"); // NOI18N
                if (i == -1) {
                    continue;
                }
                entryName = entryName.substring(i+"/ajax/libs/".length()); // NOI18N
                i = entryName.indexOf("/"); // NOI18N
                assert i != -1 : original;
                String libraryFolder = entryName.substring(0, i);
                entryName = entryName.substring(i+1);
                LibraryFiles lf = libs.get(libraryFolder);
                if (lf == null) {
                    lf = new LibraryFiles();
                    libs.put(libraryFolder, lf);
                }
                if ("package.json".equals(entryName)) {
                    assert lf.packageInfo == null : original;
                    lf.packageInfo = readPackage(str);
                } else {
                    i = entryName.indexOf("/"); // NOI18N
                    if (i == -1) {
                        // ignore: there is ajax/libs/documentup/latest.js which looks misplaced
                        continue;
                    }
                    String version = entryName.substring(0, i);
                    String file = entryName.substring(i+1);
                    List<String> files = lf.versions.get(version);
                    if (files == null) {
                        files = new ArrayList<>();
                        lf.versions.put(version, files);
                    }
                    files.add(file);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                str.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        List<LibraryImplementation> result = new ArrayList<>();
        for (LibraryFiles lf : libs.values()) {
            assert lf.packageInfo != null : lf;
            String name = (String)lf.packageInfo.get("name"); // NOI18N
            //String version = (String)lf.packageInfo.get("version"); // NOI18N
            //String file = (String)lf.packageInfo.get("filename"); // NOI18N
            String homepage = (String)lf.packageInfo.get("homepage"); // NOI18N
            String description = (String)lf.packageInfo.get("description"); // NOI18N

            for (Map.Entry<String, List<String>> e : lf.versions.entrySet()) {
                List<String> regularFiles = new ArrayList<>();
                List<String> minifiedFiles = new ArrayList<>();
                detectMinifiedAndRegularLibraries(e.getValue(), regularFiles, minifiedFiles, 
                        minifiedOrphanFiles, name+"/"+e.getKey()+"/");
                assert !minifiedFiles.isEmpty() : "version: "+e.getKey()+" - "+lf;
                result.add(createLibrary(name, e.getKey(), minifiedFiles, regularFiles, homepage, description));
            }
        }

        return result;
    }

    private static JSONObject readPackage(ZipInputStream str) {
        Reader r = new InputStreamReader(str, Charset.forName("UTF-8")); // NOI18N
        try {
            return (JSONObject)JSONValue.parseWithException(r);
        } catch (IOException | ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static final Pattern JS_FILE_PATTERN = Pattern.compile("(.+)([\\-\\.]min)(\\..+)"); // NOI18N

    private static void detectMinifiedAndRegularLibraries(List<String> files,
            List<String> regularFiles, List<String> minifiedFiles,
            List<String> minifiedOrphanFiles, String prefix) {
        List<String> unmatchedFiles = new ArrayList<>();
        // first find all minified files and their corresponding unminified files:
        for (int i = 0; i < files.size(); i++) {
            String minifiedFile = files.get(i);
            Matcher m = JS_FILE_PATTERN.matcher(minifiedFile);
            if (m.matches()) {
                String regularFile = m.group(1) + m.group(3);
                boolean matched = false;
                for (int j = 0; j < files.size(); j++) {
                    if (regularFile.equals(files.get(j))) {
                        // we found mininified and nonminified files:
                        regularFiles.add(regularFile);
                        minifiedFiles.add(minifiedFile);
                        // mark them used:
                        files.set(i, "");
                        files.set(j, "");
                        matched = true;
                        break;
                    }
                }
                if (!matched && !minifiedFile.endsWith(".map")) { // NOI18N
                    unmatchedFiles.add(prefix+minifiedFile);
                }
            }
        }

        // just for diagnostics of consistency; used only from unit test:
        if (minifiedOrphanFiles != null && !regularFiles.isEmpty() && !unmatchedFiles.isEmpty()) {
            minifiedOrphanFiles.addAll(unmatchedFiles);
        }

        // add remaining files to the list(s):
        boolean hasRegularVersion = !regularFiles.isEmpty();
        for (String f : files) {
            if (f.isEmpty()) {
                continue;
            }
            minifiedFiles.add(f);
            if (hasRegularVersion && !f.endsWith(".map")) { // NOI18N
                regularFiles.add(f);
            }
        }
    }

    private static class LibraryFiles {
        Map<String, List<String>> versions = new HashMap<>();
        JSONObject packageInfo = null;

        @Override
        public String toString() {
            return "LibraryFiles{" + ", packageInfo=" + packageInfo + "versions=" + versions + '}'; // NOI18N
        }
        
    }

    @Override
    public void updateLibraries(@NullAllowed ProgressHandle progressHandle) throws NetworkException, IOException, InterruptedException {
        File tmpZip = getCachedZip(true);
        // download to tmp
        if (progressHandle != null) {
            NetworkSupport.downloadWithProgress(CDNJS_ZIP_URL, tmpZip, progressHandle);
        } else {
            NetworkSupport.download(CDNJS_ZIP_URL, tmpZip);
        }
        assert tmpZip.isFile();
        // rename
        File cachedZip = getCachedZip(false);
        if (cachedZip.isFile()) {
            cachedZip.delete();
        }
        tmpZip.renameTo(cachedZip);
        // fire property change
        propertyChangeSupport.firePropertyChange(PROP_LIBRARIES, null, null);
    }

    @CheckForNull
    @Override
    public FileTime getLibrariesLastUpdatedTime() {
        File cachedZip = getCachedZip(false);
        if (!cachedZip.isFile()) {
            return null;
        }
        try {
            return Files.getLastModifiedTime(cachedZip.toPath());
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot get last modified time of " + cachedZip, ex);
        }
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private static LibraryImplementation createLibrary(String name, String version, List<String> minifiedFiles,
            List<String> regularFiles, String homepage, String description) {
        LibraryImplementation3 l1 = (LibraryImplementation3) LibrariesSupport.createLibraryImplementation(
                WebClientLibraryManager.TYPE, JavaScriptLibraryTypeProvider.VOLUMES);
        NamedLibraryImplementation named = (NamedLibraryImplementation) l1;
        l1.setName("cdnjs-"+name+"-"+version); // NOI18N
        named.setDisplayName("[CDNJS] "+name+" "+version); // NOI18N
        Map<String, String> p = new HashMap<>();
        p.put(WebClientLibraryManager.PROPERTY_VERSION, version);
        p.put(WebClientLibraryManager.PROPERTY_REAL_NAME, name);
        p.put(WebClientLibraryManager.PROPERTY_REAL_DISPLAY_NAME, name);
        p.put(WebClientLibraryManager.PROPERTY_CDN, "CDNJS"); // NOI18N
        p.put(WebClientLibraryManager.PROPERTY_SITE, homepage);
        p.put(WebClientLibraryManager.PROPERTY_FILES_ROOT, getLibraryRootURL(name, version));
        l1.setProperties(p);
        l1.setDescription(description);
        if (!minifiedFiles.isEmpty()) {
            l1.setContent(WebClientLibraryManager.VOL_MINIFIED, getFiles(minifiedFiles, name, version));
        }
        if (!regularFiles.isEmpty()) {
            l1.setContent(WebClientLibraryManager.VOL_REGULAR, getFiles(regularFiles, name, version));
        }
        return l1;
    }

    private static List<URL> getFiles(List<String> files, String name, String version) {
        List<URL> libFiles = new ArrayList<>();
        for (String f : files) {
            try {
                libFiles.add(new URL(getLibraryRootURL(name, version)+f));
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return libFiles;
    }

    private static String getLibraryRootURL(String name, String version) {
        if (name == null || version == null) {
            return null;
        }
        return "http://cdnjs.cloudflare.com/ajax/libs/"+name+"/"+version+"/"; // NOI18N
    }

    private InputStream getLibraryZip() {
        File cachedZip = getCachedZip(false);
        if (cachedZip.isFile()) {
            try {
                return new FileInputStream(cachedZip);
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.WARNING, "Existing file not found: " + cachedZip, ex);
            }
        }
        // fallback
        return getDefaultSnapshostFile();
    }

    public static InputStream getDefaultSnapshostFile() {
        File cdnJS = InstalledFileLocator.getDefault().locate(
                    "modules/ext/cdnjs.zip","org.netbeans.modules.web.clientproject.api", false); // NOI18N
        assert cdnJS != null && cdnJS.exists() : "default cdnjs.zip bundled with the IDE cannot be found"; // NOI18N
        try {
            return new FileInputStream(cdnJS);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private File getCachedZip(boolean tmp) {
        File jsLibsCacheDir = Places.getCacheSubdirectory(JSLIBS_CACHE_PATH);
        if (jsLibsCacheDir.isDirectory()) {
            return new File(jsLibsCacheDir, tmp ? CDNJS_ZIP_TMP_FILENAME : CDNJS_ZIP_FILENAME);
        }
        return null;
    }

}
