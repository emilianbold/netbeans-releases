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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.jaxb.util;

import java.io.File;
import java.net.URI;

/**
 * org.netbeans.spi.project.support.ant.PropertyUtils is not available as pub 
 * API yet. Use the code locally.
 * 
 */
public class FileSysUtil {

    private static String slashify(String path) {
        if (path.endsWith(File.separator)) {
            return path;
        } else {
            return path + File.separatorChar;
        }
    }

    public static String relativizeFile(File basedir, File file) {
        if (basedir.isFile()) {
            throw new IllegalArgumentException("Cannot relative w.r.t. a data file " + basedir); // NOI18N
        }
        if (basedir.equals(file)) {
            return "."; // NOI18N
        }
        StringBuffer b = new StringBuffer();
        File base = basedir;
        String filepath = file.getAbsolutePath();
        while (!filepath.startsWith(slashify(base.getAbsolutePath()))) {
            base = base.getParentFile();
            if (base == null) {
                return null;
            }
            if (base.equals(file)) {
                b.append(".."); // NOI18N
                return b.toString();
            }
            b.append("../"); // NOI18N
        }
        URI u = base.toURI().relativize(file.toURI());
        assert !u.isAbsolute() : u + " from " + basedir + " and " + file + " with common root " + base;
        b.append(u.getPath());
        if (b.charAt(b.length() - 1) == '/') {
            // file is an existing directory and file.toURI ends in /
            // we do not want the trailing slash
            b.setLength(b.length() - 1);
        }
        return b.toString();
    }
 
    public static boolean isAbsolutePath(File file){
        file.isAbsolute();
        return file.isAbsolute();
    }
    
    public static String Absolute2RelativePathStr(File base, File absPath){
        String relPath = null;
        if (isAbsolutePath(absPath)){
            relPath = relativizeFile(base, absPath);
        } else {
            relPath = absPath.getPath();
        }
        
        return relPath;
    }
    
    public static File Relative2AbsolutePath(File base, String relPath){
        File relPathFile = new File(relPath);
        File absPath = null;
        if (!isAbsolutePath(relPathFile)){
            absPath = new File(base, relPath);
        } else {
            absPath = relPathFile;
        }
        
        return absPath;
    }    
}
