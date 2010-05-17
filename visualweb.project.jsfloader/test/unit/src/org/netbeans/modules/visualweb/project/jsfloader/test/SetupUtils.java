/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.visualweb.project.jsfloader.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.visualweb.project.jsfloader.JsfJavaDataLoader;
import org.netbeans.modules.visualweb.project.jsfloader.JsfJspDataLoader;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.test.MockLookup;

/**
 *
 * @author quynguyen
 */
public class SetupUtils {
    private static JsfJspDataLoader jspLoader = null;
    private static JsfJavaDataLoader javaLoader = null;
    
    public static Project setup(File workDir) throws IOException {
        File userDir = new File(workDir, "userdir");
        userDir.mkdir();
        System.getProperties().put("netbeans.user", userDir.getAbsolutePath());
        
        MockLookup.init();
        DataLoaderPool pool = new SetupUtils.DefaultPool();
        MockLookup.setInstances(pool);
        
        if (jspLoader == null) {
            jspLoader = new JsfJspDataLoader();
        }
        
        if (javaLoader == null) {
            javaLoader = new JsfJavaDataLoader();
        }
        
        MockLookup.setInstances(pool, jspLoader, javaLoader);
        
        String zipResource = "VWJavaEE5.zip";
        String zipPath = SetupUtils.class.getResource(zipResource).getPath();
        if (zipPath == null) {
            throw new IOException("Could not load zip resource: " + zipResource);
        }
        
        File archiveFile = new File(zipPath);

        FileObject destFileObj = FileUtil.toFileObject(workDir);
        unZipFile(archiveFile, destFileObj);
        
        if (!destFileObj.isValid()) {
            throw new IOException("FileObject for project directory not valid");
        }
        
        FileObject testApp = destFileObj.getFileObject("VWJavaEE5");
        System.out.println("Children of VWJavaEE5:" + Arrays.toString(testApp.getChildren()));

        Project project = ProjectManager.getDefault().findProject(testApp);
        
        if (project == null) {
            throw new IOException("Could not load project");
        }
        
        OpenProjects.getDefault().open(new Project[]{project}, false);
        return project;
    }

    public static JsfJavaDataLoader getJavaLoader() {
        return javaLoader;
    }

    public static JsfJspDataLoader getJspLoader() {
        return jspLoader;
    }
    
    private static void unZipFile(File archiveFile, FileObject destDir) throws IOException {
        FileInputStream fis = new FileInputStream(archiveFile);
        try {
            ZipInputStream str = new ZipInputStream(fis);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(destDir, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(destDir, entry.getName());
                    FileLock lock = fo.lock();
                    try {
                        OutputStream out = fo.getOutputStream(lock);
                        try {
                            FileUtil.copy(str, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            fis.close();
        }
    }
    
    /**
     * Taken from DataLoaderPool.DefaultPool to override NbLoaderPool
     * 
     * Special pool for unit testing etc.
     * Finds all relevant data loaders in default lookup.
     * 
     */
    public static final class DefaultPool extends DataLoaderPool implements LookupListener {
        
        private final Lookup.Result<DataLoader> result;
        
        public DefaultPool() {
            result = Lookup.getDefault().lookupResult(DataLoader.class);
            result.addLookupListener(this);
        }
        
        protected Enumeration<? extends DataLoader> loaders() {
            return Collections.enumeration(result.allInstances());
        }
        
        public void resultChanged(LookupEvent e) {
            fireChangeEvent(new ChangeEvent(this));
        }
        
    }
}
