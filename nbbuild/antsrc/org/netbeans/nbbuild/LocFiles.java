/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.nbbuild;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.types.PatternSet;

public final class LocFiles extends Task {
    private String patternset;
    public void setPatternSet(String s) {
        patternset = s;
    }
    
    private String cluster;
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
    
    private String locales;
    public void setLocales(String locales) {
        this.locales = locales;
    }
    
    private File srcDir;

    public void setSrc(File f) {
        srcDir = f;
    }
    File distDir = null;

    public void setDestDir(File d) {
        distDir = d;
    }
    String cnbDashes;

    public void setCodeNameBase(String d) {
        assert d.indexOf('-') == -1;
        cnbDashes = d.replace('.', '-');
    }
    File nbmsLocation = null;

    public void setNBMs(File f) {
        nbmsLocation = f;
    }
    
    @Override
    public void execute() throws BuildException {
        if (locales == null || locales.isEmpty()) {
            locales = Locale.getDefault().toString();
        }
        if (!srcDir.exists()) {
            log("No l10n files present. Do hg clone http://hg.netbeans.org/main/l10n!", Project.MSG_VERBOSE);
            return;
        }
        
        List<String> includes = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(locales, ",");
        while (tok.hasMoreElements()) {
            String l = tok.nextToken();
            do { 
                processLocale(l, includes);
                l = trailingUnderscore(l);
            } while (l != null);
        }
        
        if (patternset != null) {
            PatternSet ps = new PatternSet();
            ps.setProject(getProject());
            if (includes.isEmpty()) {
                ps.createExclude().setName("**/*");
            } else {
                for (String s : includes) {
                    ps.createInclude().setName(s);
                }
            }
            getProject().addReference(patternset, ps);
        }
        
    }
    
    private static String trailingUnderscore(String s) {
        int under = s.lastIndexOf('_');
        if (under == -1) {
            return null;
        }
        return s.substring(0, under);
    }
    
    private static String findModuleDir(String cnbDashes) {
        String pref = "org-netbeans-modules-";
        if (cnbDashes.startsWith(pref)) {
            return cnbDashes.substring(pref.length());
        }
        return cnbDashes;
    }
    
    private void processLocale(String locale, Collection<? super String> toAdd) {
        File baseSrcDir = fileFrom(srcDir, locale);
        if (!baseSrcDir.exists()) {
            log("No files for locale: " + locale);
            return;
        }
        log("Found L10N dir " + baseSrcDir, Project.MSG_VERBOSE);
        
        final String moduleDir = findModuleDir(cnbDashes);
        File locBaseDir = fileFrom(baseSrcDir, cluster, moduleDir);
        if (!locBaseDir.exists()) {
            log("Can't find directory " + locBaseDir, Project.MSG_WARN);
            return;
        }
        File[] ch = locBaseDir.listFiles();
        if (ch == null) {
            throw new BuildException("Surprising content of " + locBaseDir);
        }
        for (File f : ch) {
            processLocaleJar(f, locale, toAdd, moduleDir, locBaseDir, baseSrcDir);
        }
    }
    
    private void processLocaleJar(
        File file, String locale, Collection<? super String> toAdd,
        String moduleDir, File locBaseDir, File baseSrcDir
    ) {
        DirectoryScanner ds = new DirectoryScanner();
        int segments = -1;
        int dirIndex = -1;
        final File root;
        final String prefixRoot;
        final int remove;
        if (file.getName().equals(moduleDir)) {
            ds.setIncludes(new String[] { moduleDir });
            root = fileFrom(distDir, cluster);
            prefixRoot = null;
            remove = 0;
        } else {
            if (file.getName().equals("netbeans")) {
                ds.setIncludes(new String[] { 
                    "netbeans/**/org", "netbeans/**/com" 
                });
                remove = 4;
                segments = 3;
                dirIndex = 1;
                root = fileFrom(distDir, cluster);
                prefixRoot = "";
            } else {
                assert file.getName().equals("ext");
                ds.setIncludes(new String[] { file.getName() + "/*" });
                segments = 2;
                dirIndex = 0;
                root = fileFrom(distDir, cluster, "modules");
                prefixRoot = "modules/";
                remove = 0;
            }
        }
        
        ds.setBasedir(locBaseDir);
        ds.scan();
        for (String dir : ds.getIncludedDirectories()) {
            if (remove > 0) {
                dir = dir.substring(0, dir.length() - remove);
            }
            
            Jar jar = (Jar) getProject().createTask("jar");
            Mkdir mkdir = (Mkdir) getProject().createTask("mkdir");
            Task locJH = getProject().createTask("locjhindexer");
            
            String jarFileName;
            String subPath = "";
            File jarDir;
            String prefixDir;
            if (dir.equals(moduleDir)) {
                jarDir = fileFrom(root, "modules", "locale");
                prefixDir = "modules/locale/";
                jar.setBasedir(fileFrom(locBaseDir, dir));
                jarFileName = cnbDashes + "_" + locale + ".jar";
            } else {
                // netbeans/*/* case or ext/* case
                jar.setBasedir(fileFrom(locBaseDir, dir));
                String[] arr = dir.split("/");
                assert arr.length >= segments : "Expected segments: " + dir;
                final int jarIndex = arr.length - 1;
                jarFileName = arr[jarIndex] + "_" + locale + ".jar";
                jarDir = fileFrom(
                    fileFrom(root, arr, dirIndex, jarIndex),
                    "locale"
                );
                prefixDir = nameFrom(
                    nameFrom(prefixRoot, arr, dirIndex, jarIndex),
                    "locale"
                );
            }
            String fullName = prefixDir + jarFileName;
            toAdd.add(fullName);
            if (jarDir == null) {
                // in patternSet only mode
                continue;
            }
            
            /*
            String subPath = dir.substring((cluster + File.separator + nbm + File.separator).length() - 1, dir.lastIndexOf(File.separator));
            if (!subPath.startsWith(File.separator + "netbeans")) {
                subPath = File.separator + "modules" + subPath;
                if (!name.startsWith("org-") && !(subPath.endsWith(File.separator + "ext") || subPath.endsWith(File.separator + "ext" + File.separator + "locale"))) {
                    name = "org-netbeans-modules-" + name;
                } else {
                    // Handle exception from ext/
                    if (name.startsWith("web-httpmonitor") || name.startsWith("deployment-deviceanywhere")) {
                        name = "org-netbeans-modules-" + name;
                    }
                }
            } else {
                subPath = subPath.substring((File.separator + "netbeans").length());
                //Handle exceptions form nblib
                if (name.startsWith("j2ee-ant") || name.startsWith("deployment-deviceanywhere") || name.startsWith("mobility-project") || name.startsWith("java-j2seproject-copylibstask")) {
                    name = "org-netbeans-modules-" + name;
                }
            }
            nbm = nbm.replaceAll("^vw-rh", "visualweb-ravehelp-rave_nbpack");
            nbm = nbm.replaceAll("^vw-", "visualweb-");
            if (!nbm.startsWith("org-") && !nbm.startsWith("com-")) {
                nbm = "org-netbeans-modules-" + nbm;
            }
            */
            
            if (subPath.matches(".*/docs$")) {
                ds.setBasedir(fileFrom(baseSrcDir, dir));
                ds.setIncludes(new String[]{"**/*.hs"});
                ds.setExcludes(new String[]{""});
                ds.scan();
                if (ds.getIncludedFilesCount() != 1) {
                    throw new BuildException("Can't find .hs file for " + cnbDashes + " module.");
                }
                File hsFile = fileFrom(baseSrcDir, dir, ds.getIncludedFiles()[0]);
                File baseJHDir = hsFile.getParentFile();

                try {
                    System.out.println("Basedir: " + baseJHDir.getAbsolutePath());
                    locJH.getClass().getMethod("setBasedir", File.class).invoke(locJH, baseJHDir);
                    locJH.getClass().getMethod("setLocales", String.class).invoke(locJH, locale);
                    locJH.getClass().getMethod("setDbdir", String.class).invoke(locJH, "JavaHelpSearch");
              //      ((Path)locJH.getClass().getMethod("createClasspath").invoke(locJH)).add(classpath);

                } catch (Exception ex) {
                    throw new BuildException("Can't run locJHInxeder", ex);
                }
                locJH.execute();
            }
            mkdir.setDir(jarDir);
            mkdir.execute();
            jar.setDestFile(fileFrom(jarDir, jarFileName));
            jar.execute();
        }
    }
    
    private static File fileFrom(File base, String... paths) {
        return fileFrom(base, paths, 0, paths.length);
    }
    private static File fileFrom(File base, String[] paths, int from, int upTo) {
        if (base == null) {
            return null;
        }
        File f = base;
        while (from < upTo) {
            f = new File(f, paths[from]);
            from++;
        }
        return f;
    }
    private static String nameFrom(String base, String... paths) {
        return nameFrom(base, paths, 0, paths.length);
    }
    private static String nameFrom(String base, String[] paths, int from, int upTo) {
        if (base == null) {
            return null;
        }
        String f = base;
        while (from < upTo) {
            f += paths[from];
            f += '/';
            from++;
        }
        return f;
    }
}
