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
package org.netbeans.modules.java;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jan Lahoda
 */
public class TestJavaPlatformProviderImpl implements JavaPlatformProvider {
    
    /** Creates a new instance of TestJavaPlatformProviderImpl */
    public TestJavaPlatformProviderImpl() {
    }

    public JavaPlatform[] getInstalledPlatforms() {
        return new JavaPlatform[] {getDefaultPlatform()};
    }

    private static DefaultPlatform DEFAULT = new DefaultPlatform();

    public JavaPlatform getDefaultPlatform() {
        return DEFAULT;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    private static final class DefaultPlatform extends JavaPlatform {
        private static ClassPath EMPTY = ClassPathSupport.createClassPath(Collections.EMPTY_LIST);

        public String getDisplayName() {
            return "default";
        }

        public Map getProperties() {
            return Collections.emptyMap();
        }

        private static ClassPath  bootClassPath;
        
        private static synchronized ClassPath getBootClassPath() {
            if (bootClassPath == null) {
                try {
                    String cp = System.getProperty("sun.boot.class.path");
                    List<URL> urls = new ArrayList<URL>();
                    String[] paths = cp.split(Pattern.quote(System.getProperty("path.separator")));
                    
                    for (String path : paths) {
                        File f = new File(path);
                        
                        if (!f.canRead())
                            continue;
                        
                        FileObject fo = FileUtil.toFileObject(f);
                        
                        if (FileUtil.isArchiveFile(fo)) {
                            fo = FileUtil.getArchiveRoot(fo);
                        }
                        
                        if (fo != null) {
                            urls.add(fo.getURL());
                        }
                    }
                    
                    bootClassPath = ClassPathSupport.createClassPath((URL[])urls.toArray(new URL[0]));
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            
            return bootClassPath;
        }

        public ClassPath getBootstrapLibraries() {
            return getBootClassPath();
        }

        public ClassPath getStandardLibraries() {
            return EMPTY;
        }

        public String getVendor() {
            return "";
        }

        private Specification spec = new Specification("j2se", new SpecificationVersion("1.5"));

        public Specification getSpecification() {
            return spec;
        }

        public Collection getInstallFolders() {
            throw new UnsupportedOperationException();
        }

        public FileObject findTool(String toolName) {
            return null;//no tools supported.
        }

        public ClassPath getSourceFolders() {
            return EMPTY;
        }

        public List getJavadocFolders() {
            return Collections.emptyList();
        }

    }

}
