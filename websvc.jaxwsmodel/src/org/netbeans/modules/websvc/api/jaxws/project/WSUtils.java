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

package org.netbeans.modules.websvc.api.jaxws.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.Retriever;
import org.netbeans.modules.xml.retriever.RetrieverImpl;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class WSUtils {

    private static final String ENDORSED_DIR_PROPERTY="jaxws.endorsed.dir"; //NOI18N
    /** downloads XML resources from source URI to target folder
     * (USAGE : this method can download a wsdl file and all wsdl/XML schemas,
     * that are recursively imported by this wsdl)
     * @param targetFolder A folder inside a NB project (ONLY) to which the retrieved resource will be copied to. All retrieved imported/included resources will be copied relative to this directory.
     * @param source URI of the XML resource that will be retrieved into the project
     * @return FileObject of the retrieved resource in the local file system
     */
    public static FileObject retrieveResource(FileObject targetFolder, URI source)
    throws java.net.UnknownHostException, java.net.URISyntaxException, IOException{
        try {
            Retriever retriever = new RetrieverImpl();
            FileObject result = retriever.retrieveResource(targetFolder, source);
            if (result==null) {
                Map map = retriever.getRetrievedResourceExceptionMap();
                if (map!=null) {
                    Set keys = map.keySet();
                    Iterator it = keys.iterator();
                    while (it.hasNext()) {
                        RetrieveEntry key = (RetrieveEntry)it.next();
                        Object exc = map.get(key);
                        if (exc instanceof IOException) {
                            throw (IOException)exc;
                        } else if (exc instanceof java.net.URISyntaxException) {
                            throw (java.net.URISyntaxException)exc;
                        } else if (exc instanceof Exception) {
                            IOException ex = new IOException(NbBundle.getMessage(WSUtils.class,"ERR_retrieveResource",key.getCurrentAddress()));
                            ex.initCause((Exception)exc);
                            throw (IOException)(ex);
                        }
                    }
                }
            }
            return result;
        } catch (RuntimeException ex) {
            throw (IOException)(new IOException(ex.getLocalizedMessage()).initCause(ex));
        }
    }
    
    public static String findProperServiceName(String name, JaxWsModel jaxWsModel) {
        if (jaxWsModel.findServiceByName(name)==null) return name;
        for (int i = 1;; i++) {
            String destName = name + "_" + i; // NOI18N
            if (jaxWsModel.findServiceByName(destName)==null)
                return destName;
        }
    }
    
    public static void retrieveJaxWsFromResource(FileObject projectDir) throws IOException {
        final String jaxWsContent =
                readResource(WSUtils.class.getResourceAsStream("/org/netbeans/modules/websvc/jaxwsmodel/resources/jax-ws.xml")); //NOI18N
        final FileObject nbprojFo = projectDir.getFileObject("nbproject"); //NOI18N
        assert nbprojFo != null : "Cannot find nbproject directory"; //NOI18N
        FileSystem fs = nbprojFo.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject jaxWsFo = FileUtil.createData(nbprojFo, "jax-ws.xml");//NOI18N
                FileLock lock = jaxWsFo.lock();
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(jaxWsFo.getOutputStream(lock)));
                    bw.write(jaxWsContent);
                    bw.close();
                } finally {
                    lock.releaseLock();
                }
            }
        });
    }
    
    public static void retrieveHandlerConfigFromResource(final FileObject targetDir, final String handlerConfigName) throws IOException {
        final String handlerContent =
                readResource(WSUtils.class.getResourceAsStream("/org/netbeans/modules/websvc/jaxwsmodel/resources/handler.xml")); //NOI18N
        FileSystem fs = targetDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject handlerFo = FileUtil.createData(targetDir, handlerConfigName);//NOI18N
                FileLock lock = handlerFo.lock();
                BufferedWriter bw = null;
                OutputStream os = null;
                try {
                    os = handlerFo.getOutputStream(lock);
                    bw = new BufferedWriter(new OutputStreamWriter(os));
                    bw.write(handlerContent);
                    bw.close();
                } finally {
                    if(bw != null)
                        bw.close();
                    if(os != null)
                        os.close();
                    if(lock != null)
                        lock.releaseLock();
                }
            }
        });
    }
    
    public static void generateSunJaxwsFile(final FileObject targetDir) throws IOException {
        final String sunJaxwsContent =
                readResource(WSUtils.class.getResourceAsStream("/org/netbeans/modules/websvc/jaxwsmodel/resources/sun-jaxws.xml")); //NOI18N
        FileSystem fs = targetDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject sunJaxwsFo = FileUtil.createData(targetDir, "sun-jaxws.xml");//NOI18N
                FileLock lock = sunJaxwsFo.lock();
                BufferedWriter bw = null;
                OutputStream os = null;
                OutputStreamWriter osw = null;
                try {
                    os = sunJaxwsFo.getOutputStream(lock);
                    osw = new OutputStreamWriter(os);
                    bw = new BufferedWriter(osw);
                    bw.write(sunJaxwsContent);
                } finally {
                    if(bw != null)
                        bw.close();
                    if(os != null)
                        os.close();
                    if(osw != null)
                        osw.close();
                    if(lock != null)
                        lock.releaseLock();
                }
            }
        });
    }
    
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }
    
    public static void removeImplClass(Project project, String implClass) {
        Sources sources = (Sources)project.getLookup().lookup(Sources.class);
        String resource = implClass.replace('.','/')+".java"; //NOI18N
        if (sources!=null) {
            SourceGroup[] srcGroup = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i=0;i<srcGroup.length;i++) {
                final FileObject srcRoot = srcGroup[i].getRootFolder();
                final FileObject implClassFo = srcRoot.getFileObject(resource);
                if (implClassFo!=null) {
                    try {
                        FileSystem fs = implClassFo.getFileSystem();
                        fs.runAtomicAction(new AtomicAction() {
                            public void run() {
                                FileObject parent = implClassFo.getParent();
                                deleteFile(implClassFo);
                                while (parent!=srcRoot && parent.getChildren().length==0) {
                                    FileObject fileToDelete=parent;
                                    parent = parent.getParent();
                                    deleteFile(fileToDelete);
                                }
                            }
                        });
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                    return;
                }
            }
        }
    }
    
    private static void deleteFile(FileObject f) {
        FileLock lock = null;
        try {
            lock = f.lock();
            if (f.isFolder()) {
                DataFolder folder = DataFolder.findFolder(f);
                // save all opened files
                if (folder!=null) {
                    DataObject[] children = folder.getChildren();
                    for (int i=0;i<children.length;i++) {
                        SaveCookie save = (SaveCookie)children[i].getCookie(SaveCookie.class);
                        if (save!=null) save.save();
                    }
                }
            }
            f.delete(lock);
        } catch(java.io.IOException e) {
            NotifyDescriptor ndd =
                    new NotifyDescriptor.Message(NbBundle.getMessage(WSUtils.class, "MSG_Unable_Delete_File", f.getNameExt()),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(ndd);
        } finally {
            if(lock != null) {
                lock.releaseLock();
            }
        }
    }
    
    /** Copy files from source folder to target folder recursively */
    public static void copyFiles(FileObject sourceFolder, FileObject targetFolder) throws IOException {
        FileObject[] children = sourceFolder.getChildren();
        for (int i=0;i<children.length;i++) {
            if (children[i].isFolder()) {
                FileObject folder = targetFolder.createFolder(children[i].getName());
                copyFiles(children[i],folder);
            } else {
                children[i].copy(targetFolder, children[i].getName(), children[i].getExt());
            }
        }
    }
    
    public static FileObject backupAndGenerateJaxWs(FileObject projectDir, FileObject oldJaxWs, RuntimeException reason) throws IOException {
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(WSUtils.class,"ERR_corruptedJaxWs",oldJaxWs.getPath(),reason.getMessage()),NotifyDescriptor.ERROR_MESSAGE));
        FileObject parent = oldJaxWs.getParent();
        FileObject oldBackup = parent.getFileObject("jax-ws.xml.old"); //NOI18N
        FileLock lock = null;
        if (oldBackup!=null) {
            // remove old backup
            try {
                lock = oldBackup.lock();
                oldBackup.delete(lock);
            } finally {
                if (lock!=null) lock.releaseLock();
            }
        }
        // renaming old jax-ws.xml;
        try {
            lock = oldJaxWs.lock();
            oldJaxWs.rename(lock, "jax-ws.xml","old"); //NOI18N
        } finally {
            if (lock!=null) lock.releaseLock();
        }
        retrieveJaxWsFromResource(projectDir);
        return projectDir.getFileObject(GeneratedFilesHelper.JAX_WS_XML_PATH);
    }
    
    /** Set jaxws.endorsed.dir property for wsimport, wsgen tasks
     *  to specify jvmarg value : -Djava.endorsed.dirs=${jaxws.endorsed.dir}"
     */
    public static void setJaxWsEndorsedDirProperty(EditableProperties ep) {
        String oldJaxWsEndorsedDirs = ep.getProperty(ENDORSED_DIR_PROPERTY);
        String javaVersion = System.getProperty("java.specification.version"); //NOI18N
        if ("1.6".equals(javaVersion)) { //NOI18N
            String jaxWsEndorsedDirs = getJaxWsApiDir();
            if (jaxWsEndorsedDirs!=null && !jaxWsEndorsedDirs.equals(oldJaxWsEndorsedDirs))
            ep.setProperty(ENDORSED_DIR_PROPERTY, jaxWsEndorsedDirs);
        } else {
            if (oldJaxWsEndorsedDirs!=null) {
                ep.remove(ENDORSED_DIR_PROPERTY);
            }
        }
    }
    
    private static String getJaxWsApiDir() {
        URL wsFeatureUrl = WSUtils.class.getClassLoader().getResource("javax/xml/ws/WebServiceFeature.class");
        if (wsFeatureUrl!=null) {
            String prefix = "jar:file:"; //NOI18N
            String postfix = "/jaxws-api.jar!/javax/xml/ws/WebServiceFeature.class"; //NOI18N
            String jaxWsUrl = wsFeatureUrl.toExternalForm();
            if (jaxWsUrl.startsWith(prefix) && jaxWsUrl.endsWith(postfix));
            int endPosition = jaxWsUrl.indexOf(postfix);
            return jaxWsUrl.substring(9,endPosition);
        }
        return null;
    }
}