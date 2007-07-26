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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sun.manager.jbi.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;

/**
 * Various file filters for filechooser.
 * 
 * @author jqian
 */
public class FileFilters {
    
    private FileFilters() {
    }
    
    public static class ZipFileFilter extends FileFilter {
        private static FileFilter instance; 
        
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".zip"); // NOI18N
        }
        
        public String getDescription() {
            return NbBundle.getMessage(JarFileFilter.class, "LBL_ZIPType"); // NOI18N
        }
        
        public static FileFilter getInstance() {
            if (instance == null) {
                instance = new ZipFileFilter();
            }
            return instance;
        }
    }
    
    public static class JarFileFilter extends FileFilter {
        private static FileFilter instance; 
        
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar"); // NOI18N
        }
        
        public String getDescription() {
            return NbBundle.getMessage(JarFileFilter.class, "LBL_JARType"); // NOI18N
        }
        
        public static FileFilter getInstance() {
            if (instance == null) {
                instance = new JarFileFilter();
            }
            return instance;
        }
    }
    
    public static class ArchiveFileFilter extends FileFilter {
        private static FileFilter instance; 
        
        public boolean accept(File f) {
            return f.isDirectory() ||
                    f.getName().toLowerCase().endsWith(".zip") || // NOI18N
                    f.getName().toLowerCase().endsWith(".jar"); // NOI18N
        }
        
        public String getDescription() {
            return NbBundle.getMessage(JarFileFilter.class, "LBL_ArchiveType"); // NOI18N
        }
        
        public static FileFilter getInstance() {
            if (instance == null) {
                instance = new ArchiveFileFilter();
            }
            return instance;
        }
    }    
}
