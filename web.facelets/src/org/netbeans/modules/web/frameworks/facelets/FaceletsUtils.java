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

package org.netbeans.modules.web.frameworks.facelets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebFrameworks;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Petr Pisl
 */
public class FaceletsUtils {
    //constants for web.xml
    protected static final String FACELETS_SKIPCOMMNETS = "facelets.SKIP_COMMENTS";
    protected static final String FACELETS_DEVELOPMENT = "facelets.DEVELOPMENT";
    protected static final String FACELETS_DEFAULT_SUFFIX = "javax.faces.DEFAULT_SUFFIX";
    // FQCN for default Facelets view handler.
    protected static final String FACELETS_VIEW_HANDLER = "com.sun.facelets.FaceletViewHandler";
    
    private static final String FACELETS_JAR = "jsf-facelets.jar";  //NOI18N
    private static final String EL_API_HEADER = "el-api";  //NOI18N
    private static final String EL_RI_HEADER = "el-ri";  //NOI18N
    private static final String EL_IMPL_HEADER = "el-impl";  //NOI18N
    protected static final String LIB_FACELETS_DISPLAY_NAME = "Facelets ";         //NOI18N
    private static final String LIB_FACELETS_NAME = "facelets";
    private static final String LIBS_FOLDER = "org-netbeans-api-project-libraries/Libraries"; //NOI18N
    
    protected static final String LIB_FACELETS_JSF_NAME = "Facelets_JSF_RI";
    private static final String LIB_FACELETS_JSF_RI = "facelets-jsf-ri.xml";
    private static final String JSF_API_JAR = "jsf-api.jar";
    private static final String JSF_IMPL_JAR = "jsf-impl.jar";
    private static final String DIGESTER_JAR = "commons-digester.jar";
    private static final String LOGGING_JAR = "commons-logging.jar";
    private static final String COLLECTIONS_JAR = "commons-collections.jar";
    private static final String BEANUTILS_JAR = "commons-beanutils.jar";
    
    protected static final String LIB_FACELETS_MYFACES_NAME = "Facelets_MYFACES";
    private static final String LIB_FACELETS_MYFACES = "facelets-myfaces.xml";
    private static final String START_MYFACES = "myfaces";
    
    
    public static Set setFaceletsFramework(FileObject folder) {
        List<WebFrameworkProvider> frameworks = WebFrameworks.getFrameworks();
        for (WebFrameworkProvider framework : frameworks) {
            if (framework instanceof FaceletsFrameworkProvider) {
                WebModule webModule = WebModule.getWebModule(folder);
                if (framework.isInWebModule(webModule)) {
                    return null;
                }

                WebModuleExtender extender = framework.createWebModuleExtender(webModule, ExtenderController.create());
                ((FaceletsFrameworkProvider) framework).setExtendData(FaceletsFrameworkProvider.ExtendType.NEWFILE);
                return extender.extend(webModule);
            }
        }
        return null;
    }
    
    public static boolean isFaceletsInstallFolder(File file){
        boolean result = false;
        String fileSeparator = System.getProperty("file.separator"); //NOI18N
        if (file.exists() && file.isDirectory()){
            File facelets = new File(file, FACELETS_JAR);
            if (facelets.exists())
                result = true;
        }
        return result;
    }
    
    public static boolean isFaceletsLibCreated(){
        return !(LibraryManager.getDefault().getLibrary(FaceletsUtils.LIB_FACELETS_NAME) == null);
    }
    
    //XXX: Replace this when API for managing libraries is available
    public static boolean createFaceletsUserLibrary(File folder, final String version) throws IOException {
        assert folder != null;
        final File faceletsJar = new File(folder,FACELETS_JAR);
        if (!faceletsJar.exists()) {
            return false;
        }
        final File faceletsLibFolder = new File(folder, "lib"); // NOI18N
        if (!faceletsLibFolder.exists() || !faceletsLibFolder.isDirectory())
            return false;

        File _elApiJar = null;
        File _elRiJar = null;
        
        for (File file : faceletsLibFolder.listFiles()) {
            String name = file.getName();
            if (!name.endsWith(".jar")) {
                continue;
            }
            
            if (name.startsWith(EL_API_HEADER)) {
                _elApiJar = file;
            } else if (name.startsWith(EL_RI_HEADER) || name.startsWith(EL_IMPL_HEADER)) {
                _elRiJar = file;
            }
        }
        
        if ((_elApiJar == null) || (_elRiJar == null))
            return false;
        
        final File elApiJar = _elApiJar;
        final File elRiJar = _elRiJar;
        
        //final Library lib = LibraryManager.getDefault().getLibrary(LIB_FACELETS_NAME);
        final FileSystem sysFs = Repository.getDefault().getDefaultFileSystem();
        final FileObject libsFolder = sysFs.findResource(LIBS_FOLDER);
        final String convertedVersion = convertLibraryVersion(version);
        assert libsFolder != null && libsFolder.isFolder();
        
        sysFs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                String fileName = LIB_FACELETS_NAME + "-" + convertedVersion; //NOI18N
                FileObject facelets = libsFolder.getFileObject(fileName + ".xml");
                if (facelets == null) {
                    facelets = libsFolder.createData(fileName + ".xml");
                }
                FileLock lock = facelets.lock();
                try {
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(facelets.getOutputStream(lock)));
                    try {
                        String[] resources = new String[]{
                            FileUtil.getArchiveRoot(faceletsJar.toURI().toURL()).toString(),
                            FileUtil.getArchiveRoot(elRiJar.toURI().toURL()).toString()
                        };
                        createLibraryFile(out, fileName, resources);
                    } finally {
                        out.close();
                    }
                } finally {
                    lock.releaseLock();
                }
                
                // Facelets EL API
                fileName = LIB_FACELETS_NAME + "-" + convertedVersion + "-el-api"; //NOI18N
                facelets = libsFolder.getFileObject(fileName + ".xml");
                if (facelets == null) {
                    facelets = libsFolder.createData(fileName + ".xml");
                }
                lock = facelets.lock();
                try {
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(facelets.getOutputStream(lock)));
                    try {
                        String[] resources = new String[]{
                            FileUtil.getArchiveRoot(elApiJar.toURI().toURL()).toString()
                        };
                        createLibraryFile(out, fileName, resources);
                    } finally {
                        out.close();
                    }
                } finally {
                    lock.releaseLock();
                }

            }
        });
        return true;
    }
    
    public static Library getCoreLibrary(final String version){
        String convertedVersion = convertLibraryVersion(version);
        return LibraryManager.getDefault().getLibrary( LIB_FACELETS_NAME + "-" + convertedVersion);
    }
    
    /* Converts a string to the text, which is fit for the LibraryManager as a library name
     */
    public static String convertLibraryVersion(String version){
        String converted = version;
        converted = converted.replace('.', '-');
        converted = converted.replace(' ', '_');
        return converted;
    }
    
    //XXX: Replace this when API for managing libraries is available
    public static boolean createFaceletsJSFUserLibrary(File folder, final String version) throws IOException {
        assert folder != null;
        
        final File faceletsLibFolder = new File(folder, "lib"); // NOI18N
        if (!faceletsLibFolder.exists() || !faceletsLibFolder.isDirectory())
            return false;
        final File jsfApiJar = new File(faceletsLibFolder, JSF_API_JAR);
        final File jsfImplJar = new File(faceletsLibFolder, JSF_IMPL_JAR);
        final File digesterJar = new File(faceletsLibFolder, DIGESTER_JAR);
        final File loggingJar = new File(faceletsLibFolder, LOGGING_JAR);
        final File collectionsJar = new File(faceletsLibFolder, COLLECTIONS_JAR);
        final File beanutilsJar = new File(faceletsLibFolder, BEANUTILS_JAR);
        if (!jsfApiJar.exists() || !jsfImplJar.exists()
        || !digesterJar.exists() || !loggingJar.exists()
        || !collectionsJar.exists() || !beanutilsJar.exists())
            return false;
        //final Library lib = LibraryManager.getDefault().getLibrary(LIB_FACELETS_JSF_NAME);
        final FileSystem sysFs = Repository.getDefault().getDefaultFileSystem();
        final FileObject libsFolder = sysFs.findResource(LIBS_FOLDER);
        final String convertedVersion = convertLibraryVersion(version);
        assert libsFolder != null && libsFolder.isFolder();
        
        sysFs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                String fileName = LIB_FACELETS_NAME + "-" + convertedVersion + "-jsf-ri"; //NOI18N
                FileObject facelets = libsFolder.getFileObject(fileName + ".xml");
                if (facelets == null) {
                    facelets = libsFolder.createData(fileName + ".xml");
                }
                FileLock lock = facelets.lock();
                try {
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(facelets.getOutputStream(lock)));
                    
                    try {
                        String[] resources = new String[]{
                            FileUtil.getArchiveRoot(jsfApiJar.toURI().toURL()).toString(),
                            FileUtil.getArchiveRoot(jsfImplJar.toURI().toURL()).toString(),
                            FileUtil.getArchiveRoot(digesterJar.toURI().toURL()).toString(),
                            FileUtil.getArchiveRoot(loggingJar.toURI().toURL()).toString(),
                            FileUtil.getArchiveRoot(collectionsJar.toURI().toURL()).toString(),
                            FileUtil.getArchiveRoot(beanutilsJar.toURI().toURL()).toString()
                        };
                        createLibraryFile(out, fileName, resources);
                    } finally {
                        out.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        });
        return true;
    }
    
    //XXX: Replace this when API for managing libraries is available
    public static boolean createFaceletsMyFacesUserLibrary(File folder, final String version) throws IOException {
        final File faceletsLibFolder = new File(folder, "lib"); // NOI18N
        if (!faceletsLibFolder.exists() || !faceletsLibFolder.isDirectory())
            return false;
        final File digesterJar = new File(faceletsLibFolder, DIGESTER_JAR);
        final File loggingJar = new File(faceletsLibFolder, LOGGING_JAR);
        final File collectionsJar = new File(faceletsLibFolder, COLLECTIONS_JAR);
        final File beanutilsJar = new File(faceletsLibFolder, BEANUTILS_JAR);
        if (!digesterJar.exists() || !loggingJar.exists()
        || !collectionsJar.exists() || !beanutilsJar.exists())
            return false;
        
        //final Library lib = LibraryManager.getDefault().getLibrary(LIB_FACELETS_MYFACES_NAME);
        final FileSystem sysFs = Repository.getDefault().getDefaultFileSystem();
        final FileObject libsFolder = sysFs.findResource(LIBS_FOLDER);
        final String convertedVersion = convertLibraryVersion(version);
        assert libsFolder != null && libsFolder.isFolder();
        
        sysFs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                String fileName = LIB_FACELETS_NAME + "-" + convertedVersion + "-myfaces"; //NOI18N
                FileObject facelets = libsFolder.getFileObject(fileName + ".xml");
                if (facelets == null) {
                    facelets = libsFolder.createData(fileName + ".xml");
                }
                FileLock lock = facelets.lock();
                try {
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(facelets.getOutputStream(lock)));
                    
                    try {
                        File[] myfacesFiles = faceletsLibFolder.listFiles( new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                if (name.startsWith("myfaces") && name.endsWith(".jar"))
                                    return true;
                                return false;
                            }
                        });
                        String[] resources = new String[myfacesFiles.length + 4];
                        resources[0] = FileUtil.getArchiveRoot(digesterJar.toURI().toURL()).toString();
                        resources[1] = FileUtil.getArchiveRoot(loggingJar.toURI().toURL()).toString();
                        resources[2] = FileUtil.getArchiveRoot(collectionsJar.toURI().toURL()).toString();
                        resources[3] = FileUtil.getArchiveRoot(beanutilsJar.toURI().toURL()).toString();
                        for (int i = 4; i < myfacesFiles.length+4; i++) {
                            resources[i] = FileUtil.getArchiveRoot(myfacesFiles[i-4].toURI().toURL()).toString();
                        }
                        createLibraryFile(out, fileName, resources);
                    } finally {
                        out.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        });
        return true;
    }
    
    private static void createLibraryFile(PrintWriter out, String name, String[] resources ){
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");  //NOI18N
        out.println("<!DOCTYPE library PUBLIC \"-//NetBeans//DTD Library Declaration 1.0//EN\" \"http://www.netbeans.org/dtds/library-declaration-1_0.dtd\">");  //NOI18N
        out.println("<library version=\"1.0\">");           //NOI18N
        out.println("\t<name>"+name+"</name>");                  //NOI18N
        //out.println("<localizing-bundle>org.netbeans.modules.web.frameworks.facelets.Bundle</localizing-bundle>");
        out.println("\t<type>j2se</type>");                 //NOI18N
        out.println("\t<volume>");                          //NOI18N
        out.println("\t\t<type>classpath</type>");          //NOI18N
        for (int i = 0; i < resources.length; i++) {
            out.println("\t\t<resource>"+resources[i]+"</resource>");    //NOI18N
        }
        out.println("\t</volume>");                         //NOI18N
        out.println("\t<volume>");                          //NOI18N
        out.println("\t\t<type>src</type>");                //NOI18N
        out.println("\t</volume>");                         //NOI18N
        out.println("\t<volume>");                          //NOI18N
        out.println("\t\t<type>javadoc</type>");            //NOI18N
        out.println("\t</volume>");                         //NOI18N
        out.println("</library>");                          //NOI18N
    }
    
    public static String readResource(InputStream is, String encoding) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }
    
    public static void createFile(FileObject target, String content, String encoding) throws IOException {
        FileLock lock = target.lock();
        try {
            OutputStreamWriter bw = new OutputStreamWriter(target.getOutputStream(lock), encoding);
            bw.write(content);
            bw.flush();
            bw.close();
            
        } finally {
            lock.releaseLock();
        }
    }
    
    /** Returns relative path from one file to another file
     */
    public static String getRelativePath (FileObject fromFO, FileObject toFO){
        String path = "./";
        FileObject parent = fromFO.getParent();
        String tmpPath = null;
        while (parent != null && (tmpPath = FileUtil.getRelativePath(parent, toFO)) == null){
            parent = parent.getParent();
            path = path + "../";
        }
        
        return (tmpPath != null ? path + tmpPath : null);
    }
    
    public static FileObject getRelativeFO (FileObject fromFO, String path){
        String resultPath = null;
        
        path = path.trim();
        FileObject find = fromFO;
        if (!fromFO.isFolder())  // if the file is not folder, get the parent
                find = fromFO.getParent();
        
        if (path.charAt(0) == '/'){  // is the absolute path in the web module?
            WebModule wm = WebModule.getWebModule(fromFO);
            if (wm != null) 
                find = wm.getDocumentBase();  // find the folder, where the absolut path starts
            else
                find = null;        // we are not able to find out the webmodule root

            path = path.substring(1);   // if we have folder, where the webmodule starts, the path can me relative to this folder
        }
        // find relative path to the folder
        StringTokenizer st = new StringTokenizer(path, "/");
        String token;
        while (find != null && st.hasMoreTokens()) {
            token = st.nextToken();
            if ("..".equals(token))     // move to parent
                find = find.getParent();
            else if (!".".equals(token))        // if there is . - don't move
                find = find.getFileObject(token);
        }
        return find;
    }
    
    /** Find the value of the facelets.DEVELOPMENT context parameter in the deployment descriptor.
     */
    public static boolean debugFacelets(FileObject dd){
        boolean value = false;  // the default value of the facelets.DEVELOPMENT
        if (dd != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "facelets.DEVELOPMENT"); //NOI18N
                if (param != null)
                    value =   "true".equals(param.getParamValue().trim()); //NOI18N
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return value;
    }
    
    /** Find the value of the facelets.SKIP_COMMENTS context parameter in the deployment descriptor.
     */
    public static boolean skipCommnets(FileObject dd){
        boolean value = false;  // the default value of the facelets.SKIP_COMMENTS
        if (dd != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "facelets.SKIP_COMMENTS"); //NOI18N
                if (param != null)
                    value =   "true".equals(param.getParamValue().trim()); //NOI18N
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return value;
    }
}
