/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.selectors.SelectorUtils;

/**
 * Packs override resources into branding JARs with the correct paths.
 * @author Jesse Glick
 */
public final class Branding extends Task {

    private File cluster;
    private File overrides;
    private String token;
    
    public Branding() {}
    
    public void setCluster(File cluster) {
        this.cluster = cluster;
    }
    
    public void setOverrides(File overrides) {
        this.overrides = overrides;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public void execute() throws BuildException {
        if (cluster == null || !cluster.isDirectory()) {
            throw new BuildException("Must specify a valid cluster directory", getLocation());
        }
        if (overrides == null || !overrides.isDirectory()) {
            throw new BuildException("Must specify a valid overrides directory", getLocation());
        }
        if (token == null || !token.matches("[a-z][a-z0-9]*(_[a-z][a-z0-9]*)*")) { // cf. NbBundle.setBranding
            throw new BuildException("Must specify a valid branding token: " + token, getLocation());
        }
        try {
            lookForBrandingJars(overrides, cluster, overrides.getAbsolutePath() + File.separatorChar);
        } catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
    }
    
    private boolean excluded(File f, String prefix) { // #68929
        String pathAbs = f.getAbsolutePath();
        if (!pathAbs.startsWith(prefix)) {
            throw new BuildException("Examined file " + f + " should have a path starting with " + prefix, getLocation());
        }
        // Cannot just call matchPath on the pathAbs because a relative pattern will *never* match an absolute path!
        String path = pathAbs.substring(prefix.length());
        for (String exclude : DirectoryScanner.getDefaultExcludes()) {
            if (SelectorUtils.matchPath(exclude, path)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean lookForBrandingJars(File srcDir, File destDir, String srcPrefix) throws IOException {
        if (srcDir.getName().endsWith(".jar")) {
            packBrandingJar(srcDir, destDir);
            return true;
        } else {
            String[] kids = srcDir.list();
            if (kids == null) {
                throw new IOException("Could not list children of " + srcDir);
            }
            boolean used = false;
            for (int i = 0; i < kids.length; i++) {
                File kid = new File(srcDir, kids[i]);
                if (excluded(kid, srcPrefix)) {
                    continue;
                }
                if (!kid.isDirectory()) {
                    log("Warning: stray file " + kid + " encountered; ignoring", Project.MSG_WARN);
                    continue;
                }
                used |= lookForBrandingJars(kid, new File(destDir, kids[i]), srcPrefix);
            }
            if (!used) {
                log("Warning: stray directory " + srcDir + " with no brandables encountered; ignoring", Project.MSG_WARN);
            }
            return used;
        }
    }
    
    private void packBrandingJar(File srcDir, File destJarBase) throws IOException {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(srcDir);
        scanner.addDefaultExcludes(); // #68929
        scanner.scan();
        String[] files = scanner.getIncludedFiles();
        Zip zip = (Zip) getProject().createTask("zip");
        String name = destJarBase.getName();
        String nameBase = name.substring(0, name.length() - ".jar".length());
        File destFolder = new File(destJarBase.getParentFile(), "locale");
        if (!destFolder.isDirectory() && !destFolder.mkdirs()) {
            throw new IOException("Could not create directory " + destFolder);
        }
        File destJar = new File(destFolder, nameBase + "_" + token + ".jar");
        zip.setDestFile(destJar);
        zip.setCompress(true);
        for (int i = 0; i < files.length; i++) {
            ZipFileSet entry = new ZipFileSet();
            entry.setFile(new File(srcDir, files[i]));
            String basePath = files[i].replace(File.separatorChar, '/');
            int slash = basePath.lastIndexOf('/');
            int dot = basePath.lastIndexOf('.');
            String infix = "_" + token;
            String brandedPath;
            if (dot == -1 || dot < slash) {
                brandedPath = basePath + infix;
            } else {
                brandedPath = basePath.substring(0, dot) + infix + basePath.substring(dot);
            }
            entry.setFullpath(brandedPath);
            zip.addZipfileset(entry);
        }
        zip.setLocation(getLocation());
        zip.init();
        zip.execute();
    }
    
}
