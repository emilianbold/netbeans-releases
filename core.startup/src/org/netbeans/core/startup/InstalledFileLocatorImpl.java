/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 * Ability to locate NBM-installed files.
 * Looks in ${netbeans.user} then each component of ${netbeans.dirs}
 * and finally ${netbeans.home}.
 * @author Jesse Glick
 */
public final class InstalledFileLocatorImpl extends InstalledFileLocator {
    
    /** Default constructor for lookup. */
    public InstalledFileLocatorImpl() {}
    
    private static final File[] dirs;
    static {
        List/*<File>*/ _dirs = new ArrayList();
        addDir(_dirs, System.getProperty("netbeans.user"));
        String nbdirs = System.getProperty("netbeans.dirs"); // #27151
        if (nbdirs != null) {
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                addDir(_dirs, tok.nextToken());
            }
        }
        addDir(_dirs, System.getProperty("netbeans.home"));
        dirs = (File[])_dirs.toArray(new File[_dirs.size()]);
    }
    
    private static void addDir(List _dirs, String d) {
        if (d != null) {
            File f = new File(d).getAbsoluteFile();
            if (f.isDirectory()) {
                _dirs.add(f);
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
    private static Map/*<String,Map<File,Set<String>>>*/ fileCache = null;
    
    /**
     * Called from {@link NonGui#run} early in the startup sequence to indicate
     * that available files should be cached from now on. Should be matched by a call to
     * {@link #discardCache} since the cache will be invalid if the user
     * e.g. installs a new NBM without restarting.
     */
    public static synchronized void prepareCache() {
        assert fileCache == null;
        fileCache = new HashMap();
    }
    
    /**
     * Called from {@link NonGui#run} after startup is essentially complete.
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
            Map fileCachePerPrefix = (Map)fileCache.get(prefix);
            if (fileCachePerPrefix == null) {
                fileCachePerPrefix = new HashMap(dirs.length * 2);
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
                            fileCachePerPrefix.put(root, new HashSet(Arrays.asList(kids)));
                        } else {
                            ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning - could not read files in " + d);
                        }
                    }
                }
                fileCache.put(prefix, fileCachePerPrefix);
            }
            for (int i = 0; i < dirs.length; i++) {
                Set names = (Set)fileCachePerPrefix.get(dirs[i]);
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
        return FileUtil.normalizeFile(new File(dir, prefix.replace('/', File.separatorChar) + name));
    }
    
}
