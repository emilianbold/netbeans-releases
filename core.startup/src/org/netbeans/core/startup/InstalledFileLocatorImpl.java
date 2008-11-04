/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.startup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.Util;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 * Ability to locate NBM-installed files.
 * Looks in ${netbeans.user} then each component of ${netbeans.dirs}
 * and finally ${netbeans.home}.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.modules.InstalledFileLocator.class)
public final class InstalledFileLocatorImpl extends InstalledFileLocator {
    
    /** Default constructor for lookup. */
    public InstalledFileLocatorImpl() {}
    
    private static final File[] dirs;
    static {
        List<File> _dirs = new ArrayList<File>();
        addDir(_dirs, System.getProperty("netbeans.user"));
        String nbdirs = System.getProperty("netbeans.dirs"); // #27151
        if (nbdirs != null) {
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                addDir(_dirs, tok.nextToken());
            }
        }
        addDir(_dirs, System.getProperty("netbeans.home"));
        dirs = _dirs.toArray(new File[_dirs.size()]);
    }
    
    private static void addDir(List<File> _dirs, String d) {
        if (d != null) {
            File f = new File(d).getAbsoluteFile();
            if (f.isDirectory()) {
                _dirs.add(FileUtil.normalizeFile(f));
            }
        }
    }
    
    /**
     * Cache of installed files (if present).
     * Keys are directory prefixes, e.g. "" or "x/" or "x/y/" ('/' is sep).
     * The values are nested maps; keys are entries in {@link #dirs}
     * (not all entries need have keys, only those for which the dir exists),
     * and values are unqualified file names which exist in that dir.
     */
    private static Map<String,Map<File,Set<String>>> fileCache = null;
    
    /**
     * Called from <code>NonGui.run</code> early in the startup sequence to indicate
     * that available files should be cached from now on. Should be matched by a call to
     * {@link #discardCache} since the cache will be invalid if the user
     * e.g. installs a new NBM without restarting.
     */
    public static synchronized void prepareCache() {
        assert fileCache == null;
        fileCache = new HashMap<String,Map<File,Set<String>>>();
    }
    
    /**
     * Called after startup is essentially complete.
     * After this point, the list of files in the installation are not
     * cached, since they might change due to dynamic NBM installation.
     * Anyway the heaviest uses of {@link InstalledFileLocator} are
     * during startup so that is when the cache has the most effect.
     */
    public static synchronized void discardCache() {
        assert fileCache != null;
        fileCache = null;
    }
    
    /**
     * Currently just searches user dir and install dir(s).
     * @see "#28729 for a suggested better impl in AU"
     */
    public File locate(String relativePath, String codeNameBase, boolean localized) {
        if (relativePath.length() == 0) {
            throw new IllegalArgumentException("Cannot look up \"\" in InstalledFileLocator.locate"); // NOI18N
        }
        if (relativePath.charAt(0) == '/') {
            throw new IllegalArgumentException("Paths passed to InstalledFileLocator.locate should not start with '/': " + relativePath); // NOI18N
        }
        int slashIdx = relativePath.lastIndexOf('/');
        if (slashIdx == relativePath.length() - 1) {
            throw new IllegalArgumentException("Paths passed to InstalledFileLocator.locate should not end in '/': " + relativePath); // NOI18N
        }
        
        String prefix, name;
        if (slashIdx != -1) {
            prefix = relativePath.substring(0, slashIdx + 1);
            name = relativePath.substring(slashIdx + 1);
            assert name.length() > 0;
        } else {
            prefix = "";
            name = relativePath;
        }
        synchronized (InstalledFileLocatorImpl.class) {
            if (localized) {
                int i = name.lastIndexOf('.');
                String baseName, ext;
                if (i == -1) {
                    baseName = name;
                    ext = "";
                } else {
                    baseName = name.substring(0, i);
                    ext = name.substring(i);
                }
                String[] suffixes = org.netbeans.Util.getLocalizingSuffixesFast();
                for (int j = 0; j < suffixes.length; j++) {
                    String locName = baseName + suffixes[j] + ext;
                    File f = locateExactPath(prefix, locName);
                    if (f != null) {
                        return f;
                    }
                }
                return null;
            } else {
                return locateExactPath(prefix, name);
            }
        }
    }
    
    /** Search all top dirs for a file. */
    private static File locateExactPath(String prefix, String name) {
        assert Thread.holdsLock(InstalledFileLocatorImpl.class);
        if (fileCache != null) {
            Map<File,Set<String>> fileCachePerPrefix = fileCache.get(prefix);
            if (fileCachePerPrefix == null) {
                fileCachePerPrefix = new HashMap<File,Set<String>>(dirs.length * 2);
                for (int i = 0; i < dirs.length; i++) {
                    File root = dirs[i];
                    File d;
                    if (prefix.length() > 0) {
                        assert prefix.charAt(prefix.length() - 1) == '/';
                        d = new File(root, prefix.substring(0, prefix.length() - 1).replace('/', File.separatorChar));
                    } else {
                        d = root;
                    }
                    if (d.isDirectory()) {
                        String[] kids = d.list();
                        if (kids != null) {
                            fileCachePerPrefix.put(root, new HashSet<String>(Arrays.asList(kids)));
                        } else {
                            Util.err.warning("could not read files in " + d);
                        }
                    }
                }
                fileCache.put(prefix, fileCachePerPrefix);
            }
            for (int i = 0; i < dirs.length; i++) {
                Set<String> names = fileCachePerPrefix.get(dirs[i]);
                if (names != null && names.contains(name)) {
                    return makeFile(dirs[i], prefix, name);
                }
            }
        } else {
            for (int i = 0; i < dirs.length; i++) {
                File f = makeFile(dirs[i], prefix, name);
                if (f.exists()) {
                    return f;
                }
            }
        }
        return null;
    }
    
    private static File makeFile(File dir, String prefix, String name) {
        return new File(dir, prefix.replace('/', File.separatorChar) + name);
    }
    
}
