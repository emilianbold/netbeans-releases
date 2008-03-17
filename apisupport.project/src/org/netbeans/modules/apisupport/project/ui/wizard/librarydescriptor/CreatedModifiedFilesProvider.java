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

package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.CreatedModifiedFilesFactory;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Radek Matous
 */
final class CreatedModifiedFilesProvider  {

    private CreatedModifiedFilesProvider() {}

    private static final String VOLUME_CLASS = "classpath";//NOI18N
    private static final String VOLUME_SRC = "src";//NOI18N
    private static final String VOLUME_JAVADOC = "javadoc";//NOI18N
    
    private static final String LIBRARY_LAYER_ENTRY = "org-netbeans-api-project-libraries/Libraries";//NOI18N
    
    static CreatedModifiedFiles createInstance(NewLibraryDescriptor.DataModel data)  {
        
        CreatedModifiedFiles retval = new CreatedModifiedFiles(data.getProject());
        addOperations(retval, data);
        
        return retval;
    }
    
    private static void addOperations(CreatedModifiedFiles fileSupport, NewLibraryDescriptor.DataModel data)  {
        FileObject template = CreatedModifiedFiles.getTemplate("libdescriptemplate.xml");//NOI18N
        Map<String, String> tokens = getTokens(fileSupport, data.getProject(), data);
        String layerEntry = getLibraryDescriptorEntryPath(data.getLibraryName());
        
        fileSupport.add(
                fileSupport.createLayerEntry(layerEntry, template, tokens, null, null));
        
        fileSupport.add(
                fileSupport.bundleKeyDefaultBundle(data.getLibraryName(), data.getLibraryDisplayName()));
    }
    
    
    private static String getPackagePlusBundle(Project project) {
        NbModuleProject nbproj = project.getLookup().lookup(NbModuleProject.class);
        assert nbproj != null : "this template works only with default netbeans modules.";
        ManifestManager mm = ManifestManager.getInstance(nbproj.getManifest(), false);
        
        String bundle = mm.getLocalizingBundle().replace('/', '.');
        if (bundle.endsWith(".properties")) { // NOI18N
            bundle = bundle.substring(0, bundle.length() - 11);
        }
        
        return bundle;
    }
    
    static String getLibraryDescriptorEntryPath(String libraryName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append(LIBRARY_LAYER_ENTRY).append("/").append(libraryName).append(".xml");//NOI18N
        
        return sb.toString();//NOI18N
    }
    
    
    private static String transformURL(final String cnb, final String pathPrefix, final String archiveName) {
        StringBuffer sb = new StringBuffer();
        
        sb.append("jar:nbinst://").append(cnb).append("/");//NOI18N
        sb.append(pathPrefix).append(archiveName).append("!/");//NOI18N
        
        return sb.toString();
    }
    
    private static Map<String, String> getTokens(CreatedModifiedFiles fileSupport, Project project, NewLibraryDescriptor.DataModel data) {
        Map<String, String> retval = new HashMap<String, String>();
        Library library = data.getLibrary();
        retval.put("NAME",data.getLibraryName());//NOI18N
        retval.put("BUNDLE",getPackagePlusBundle(project).replace('/','.'));//NOI18N
        
        Iterator<URL> it = library.getContent(VOLUME_CLASS).iterator();
        retval.put("CLASSPATH",getTokenSubstitution(it, fileSupport, data, "libs/"));//NOI18N
        
        it = library.getContent(VOLUME_SRC).iterator();
        retval.put("SRC",getTokenSubstitution(it, fileSupport, data, "sources/"));//NOI18N
        
        it = library.getContent(VOLUME_JAVADOC).iterator();
        retval.put("JAVADOC",getTokenSubstitution(it, fileSupport, data, "docs/"));//NOI18N
        
        return retval;
    }
    
    private static String getTokenSubstitution(Iterator<URL> it, CreatedModifiedFiles fileSupport,
            NewLibraryDescriptor.DataModel data, String pathPrefix) {
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            URL originalURL = it.next();
            String archiveName;
            archiveName = addArchiveToCopy(fileSupport, data, originalURL, "release/"+pathPrefix);//NOI18N
            if (archiveName != null) {
                String codeNameBase = data.getModuleInfo().getCodeNameBase();
                String urlToString = transformURL(codeNameBase, pathPrefix, archiveName);//NOI18N
                sb.append("<resource>");//NOI18N
                sb.append(urlToString);
                if (it.hasNext()) {
                    sb.append("</resource>\n");//NOI18N
                } else {
                    sb.append("</resource>");//NOI18N
                }
            }
        }
        return sb.toString();
    }
    
    /** returns archive name or temporarily null cause there is no zip support for file protocol  */
    private static String addArchiveToCopy(CreatedModifiedFiles fileSupport,NewLibraryDescriptor.DataModel data, URL originalURL, String pathPrefix) {
        String retval = null;
        
        URL archivURL = FileUtil.getArchiveFile(originalURL);
        if (archivURL != null && FileUtil.isArchiveFile(archivURL)) {
            FileObject archiv = URLMapper.findFileObject(archivURL);
            assert archiv != null;
            retval = archiv.getNameExt();
            StringBuffer sb = new StringBuffer();
            sb.append(pathPrefix).append(retval);
            fileSupport.add(fileSupport.createFile(sb.toString(), archiv));
        } else {
            if ("file".equals(originalURL.getProtocol())) {//NOI18N
                FileObject folderToZip;
                folderToZip = URLMapper.findFileObject(originalURL);
                if (folderToZip != null) {
                    retval = data.getLibraryName()+".zip";//NOI18N
                    pathPrefix += retval;
                    fileSupport.add(new ZipAndCopyOperation(data.getProject(),
                            folderToZip, pathPrefix));
                }
            }
        }
        return retval;
    }
    
    private static class ZipAndCopyOperation extends CreatedModifiedFilesFactory.OperationBase {
        private FileObject folderToZip;
        private String relativePath;
        ZipAndCopyOperation(Project prj, FileObject folderToZip, String relativePath) {
            super(prj);
            this.folderToZip = folderToZip;
            this.relativePath = relativePath;
            addCreatedOrModifiedPath(relativePath, false);
        }
        
        public void run() throws IOException {
            Collection<? extends FileObject> files = Collections.list(folderToZip.getChildren(true));
            if (files.isEmpty()) return;
            FileObject prjDir = getProject().getProjectDirectory();
            assert prjDir != null;
            
            FileObject zipedTarget  = prjDir.getFileObject(relativePath);
            if (zipedTarget == null) {
                zipedTarget = FileUtil.createData(prjDir, relativePath);
            }
            
            assert zipedTarget != null;
            FileLock fLock = null;
            OutputStream os = null;
            
            try {
                fLock = zipedTarget.lock();
                os = zipedTarget.getOutputStream(fLock);
                createZipFile(os, folderToZip, files);
            } finally {
                if (os != null) {
                    os.close();
                }
                
                if (fLock != null) {
                    fLock.releaseLock();
                }
            }
        }
        
        private static void createZipFile(OutputStream target, FileObject root, Collection<? extends FileObject> files) throws IOException {
            ZipOutputStream str = null;
            try {
                str = new ZipOutputStream(target);
                for (FileObject fo : files) {
                    String relativePath = FileUtil.getRelativePath(root, fo);
                    if (fo.isFolder()) {
                        if (fo.getChildren().length > 0) {
                            continue;
                        } else if (!relativePath.endsWith("/")) {
                            relativePath += "/";
                        }
                    }
                    ZipEntry entry = new ZipEntry(relativePath);
                    str.putNextEntry(entry);
                    if (fo.isData()) {
                        InputStream in = null;
                        try {
                            in = fo.getInputStream();
                            FileUtil.copy(in, str);
                        } finally {
                            if (in != null) {
                                in.close();
                            }
                        }
                    }
                    str.closeEntry();
                }
            } finally {
                if (str != null) {
                    str.close();
                }
            }
        }
    }
}
