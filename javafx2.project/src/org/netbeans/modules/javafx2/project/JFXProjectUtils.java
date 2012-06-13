/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.*;
import org.netbeans.api.project.*;
import org.netbeans.modules.extbrowser.ExtWebBrowser;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.cookies.CloseCookie;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 * Utility class for JavaFX 2.0 Project
 * 
 * @author Petr Somol
 */
public final class JFXProjectUtils {

    private static Set<SearchKind> kinds = new HashSet<SearchKind>(Arrays.asList(SearchKind.IMPLEMENTORS));
    private static Set<SearchScope> scopes = new HashSet<SearchScope>(Arrays.asList(SearchScope.SOURCE));
    
    private static final String JFX_BUILD_TEMPLATE = "Templates/JFX/jfx-impl.xml"; //NOI18N
    private static volatile String currentJfxImplCRCCache;

    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N
    
    /**
     * Returns list of JavaFX 2.0 JavaScript callback entries.
     * In future should read the list from the current platform
     * (directly from FX SDK or Ant taks).
     * Current list taken from
     * http://javaweb.us.oracle.com/~in81039/new-dt/js-api/Callbacks.html
     * 
     * @param IDE java platform name
     * @return callback entries
     */
    public static Map<String,List<String>/*|null*/> getJSCallbacks(String platformName) {
        final String[][] c = {
            {"onDeployError", "app", "mismatchEvent"}, // NOI18N
            {"onGetNoPluginMessage", "app"}, // NOI18N
            {"onGetSplash", "app"}, // NOI18N
            {"onInstallFinished", "placeholder", "component", "status", "relaunchNeeded"}, // NOI18N
            {"onInstallNeeded", "app", "platform", "cb", "isAutoinstall", "needRelaunch", "launchFunc"}, // NOI18N
            {"onInstallStarted", "placeholder", "component", "isAuto", "restartNeeded"}, // NOI18N
            {"onJavascriptReady", "id"}, // NOI18N
            {"onRuntimeError", "id", "code"} // NOI18N
        };
        Map<String,List<String>/*|null*/> m = new LinkedHashMap<String,List<String>/*|null*/>();
        for(int i = 0; i < c.length; i++) {
            String[] s = c[i];
            assert s.length > 0;
            List<String> l = null;
            if(s.length > 1) {
                l = new ArrayList<String>();
                for(int j = 1; j < s.length; j++) {
                    l.add(s[j]);
                }
            }
            m.put(s[0], l);
        }
        return m;
    }
    
    /**
     * Returns all classpaths relevant for given project. To be used in
     * main class searches.
     * 
     * @param project
     * @return map of classpaths of all project files
     */
    public static Map<FileObject,List<ClassPath>> getClassPathMap(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] srcGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        final Map<FileObject,List<ClassPath>> classpathMap = new HashMap<FileObject,List<ClassPath>>();

        for (SourceGroup srcGroup : srcGroups) {
            FileObject srcRoot = srcGroup.getRootFolder();
            ClassPath bootCP = ClassPath.getClassPath(srcRoot, ClassPath.BOOT);
            ClassPath executeCP = ClassPath.getClassPath(srcRoot, ClassPath.EXECUTE);
            ClassPath sourceCP = ClassPath.getClassPath(srcRoot, ClassPath.SOURCE);
            List<ClassPath> cpList = new ArrayList<ClassPath>();
            if (bootCP != null) {
                cpList.add(bootCP);
            }
            if (executeCP != null) {
                cpList.add(executeCP);
            }
            if (sourceCP != null) {
                cpList.add(sourceCP);
            }
            if (cpList.size() == 3) {
                classpathMap.put(srcRoot, cpList);
            }
        }
        return classpathMap;
    }
    
    /**
     * Returns set of names of classes of the classType type.
     * 
     * @param classpathMap map of classpaths of all project files
     * @param classType return only classes of this type
     * @return set of class names
     */
    public static Set<String> getAppClassNames(Map<FileObject,List<ClassPath>> classpathMap, final String classType) {
        final Set<String> appClassNames = new HashSet<String>();
        for (FileObject fo : classpathMap.keySet()) {
            List<ClassPath> paths = classpathMap.get(fo);
            ClasspathInfo cpInfo = ClasspathInfo.create(paths.get(0), paths.get(1), paths.get(2));
            final ClassIndex classIndex = cpInfo.getClassIndex();
            final JavaSource js = JavaSource.create(cpInfo);
            try { 
                js.runUserActionTask(new CancellableTask<CompilationController>() {
                    @Override
                    public void run(CompilationController controller) throws Exception {
                        Elements elems = controller.getElements();
                        TypeElement fxAppElement = elems.getTypeElement(classType);
                        ElementHandle<TypeElement> appHandle = ElementHandle.create(fxAppElement);
                        Set<ElementHandle<TypeElement>> appHandles = classIndex.getElements(appHandle, kinds, scopes);
                        for (ElementHandle<TypeElement> elemHandle : appHandles) {
                            appClassNames.add(elemHandle.getQualifiedName());
                        }
                    }
                    @Override
                    public void cancel() {

                    }
                }, true);
            } catch (Exception e) {

            }
        }
        return appClassNames;
    }


    /** Finds available FX Preloader classes in given JAR files. 
     * Looks for classes specified in the JAR manifest only.
     * 
     * @param jarFile FileObject representing an existing JAR file
     * @param classType return only classes of this type
     * @return set of class names
     */
    public static Set<String> getAppClassNamesInJar(@NonNull FileObject jarFile, final String classType) {
        final File jarF = FileUtil.toFile(jarFile);
        if (jarF == null) {
            return null;
        }
        final Set<String> appClassNames = new HashSet<String>();
        JarFile jf;
        try {
            jf = new JarFile(jarF);
        } catch (IOException x) {
            return null;
        }
        Enumeration<JarEntry> entries = jf.entries();
        if (entries == null) {
            return null;
        }        
        while(entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if(!entry.getName().endsWith(".class")) { // NOI18N
                continue;
            }
            if(entry.getName().contains("$")) { // NOI18N
                continue;
            }
            String classname = entry.getName().substring(0, entry.getName().length() - 6) // cut off ".class"
                    .replace('\\', '/').replace('/', '.');
            if (classname.startsWith(".")) { // NOI18N
                classname = classname.substring(1);
            }
            appClassNames.add(classname);
        }

        return appClassNames;
    }

    /**
     * Checks if the JFX support is enabled for given project
     * @param prj the project to check
     * @return true if project supports JFX
     */
    public static boolean isFXProject(@NonNull final Project prj) {
        final J2SEPropertyEvaluator ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (ep == null) {
            return false;
        }
        return JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED));
    }

     /**
     * Checks if the project is a Swing project with JFX support enabled
     * @param prj the project to check
     * @return true if project supports FX in Swing
     */
    public static boolean isFXinSwingProject(@NonNull final Project prj) {
        final J2SEPropertyEvaluator ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (ep == null) {
            return false;
        }
        return JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED))
                && JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_SWING));
    }

     /**
     * Checks if the project is a JavaFX preloader project. Note that in pre-7.2 NB
     * preloader type had been used for fx-in-swing as workaround, hence the logic below
     * @param prj the project to check
     * @return true if project is JavaFX preloader
     */
    public static boolean isFXPreloaderProject(@NonNull final Project prj) {
        final J2SEPropertyEvaluator ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (ep == null) {
            return false;
        }
        return JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED))
                && JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_PRELOADER))
                && !JFXProjectProperties.isTrue(ep.evaluator().getProperty(JFXProjectProperties.JAVAFX_SWING));
    }

    /**
     * Checks what Run model is selected in current configuration of JFX Run Project Property panel
     * @param prj the project to check
     * @return string value of JFXProjectProperties.RunAsType type or null meaning JFXProjectProperties.RunAsType.STANDALONE
     */
    public static String getFXProjectRunAs(@NonNull final Project prj) {
        final J2SEPropertyEvaluator ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (ep == null) {
            return null;
        }
        return ep.evaluator().getProperty(JFXProjectProperties.RUN_AS);
    }

    /**
     * Finds the relative path to targetFO from sourceFO. 
     * Unlike FileUtil.getRelativePath() does not require targetFO to be within sourceFO sub-tree
     * Returns null if there is no shared parent directory except root.
     * 
     * @param sourceFO file/dir to which the relative path will be related
     * @param targetFO file whose location will be determined with respect to sourceFO
     * @return string relative path leading from sourceFO to targetFO
     */
    public static String getRelativePath(@NonNull final FileObject sourceFO, @NonNull final FileObject targetFO) {
        String path = ""; //NOI18N
        FileObject src = sourceFO;
        FileObject tgt = targetFO;
        String targetName = null;
        if(!src.isFolder()) {
            src = src.getParent();
        }
        if(!tgt.isFolder()) {
            targetName = tgt.getNameExt();
            tgt = tgt.getParent();
        }
        LinkedList<String> srcSplit = new LinkedList<String>();
        LinkedList<String> tgtSplit = new LinkedList<String>();
        while(!src.isRoot()) {
            srcSplit.addFirst(src.getName());
            src = src.getParent();
        }
        while(!tgt.isRoot()) {
            tgtSplit.addFirst(tgt.getName());
            tgt = tgt.getParent();
        }
        boolean share = false;
        while(!srcSplit.isEmpty() && !tgtSplit.isEmpty()) {
            if(srcSplit.getFirst().equals(tgtSplit.getFirst())) {
                srcSplit.removeFirst();
                tgtSplit.removeFirst();
                share = true;
            } else {
                break;
            }
        }
        if(!share) {
            return null;
        }
        for(int left = 0; left < srcSplit.size(); left++) {
            if(left == 0) {
                path += ".."; //NOI18N
            } else {
                path += "/.."; //NOI18N
            }
        }
        while(!tgtSplit.isEmpty()) {
            if(path.isEmpty()) {
                path += tgtSplit.getFirst();
            } else {
                path += "/" + tgtSplit.getFirst(); //NOI18N
            }
            tgtSplit.removeFirst();
        }
        if(targetName != null) {
            if(!path.isEmpty()) {
                path += "/" + targetName; //NOI18N
            } else {
                path += targetName;
            }
        }
        return path;
    }

    /**
     * Finds the file/dir represented by relPath with respect to sourceDir. 
     * Returns null if the file does not exist.
     * 
     * @param sourceDir file/dir to which the relative path is related
     * @param relPath relative path related to sourceDir
     * @return FileObject or null
     */
    public static FileObject getFileObject(@NonNull final FileObject sourceDir, @NonNull final String relPath) {
        String split[] = relPath.split("[\\\\/]+"); //NOI18N
        FileObject src = sourceDir;
        String path = ""; //NOI18N
        boolean back = true;
        if(split[0].equals("..")) {
            for(int i = 0; i < split.length; i++) {
                if(back && split[i].equals("..")) { //NOI18N
                    src = src.getParent();
                    if(src == null) {
                        return null;
                    }
                } else {
                    if(back) {
                        back = false;
                        path = src.getPath();
                    }
                    path += "/" + split[i]; //NOI18N
                }
            }
        } else {
            path = relPath;
        }
        File f = new File(path);
        if(f.exists()) {
            return FileUtil.toFileObject(f);
        }
        return null;
    }

    /**
     * Checks whether file nbproject/jfx-impl.xml equals current template. 
     * If not, the file is backed up and regenerated to the current state
     * and textual commentary is generated to UPDATED.TXT.
     * 
     * @param prj the project to check
     * @return FileObject pointing at generated UPDATED.TXT or null
     */
    static FileObject updateJfxImpl(final @NonNull Project proj) throws IOException {
        FileObject returnFO = null;
        boolean isJfxCurrent = true;
        FileObject projDir = proj.getProjectDirectory();
        FileObject jfxBuildFile = projDir.getFileObject("nbproject/jfx-impl.xml"); // NOI18N
        if (jfxBuildFile != null) {
            final InputStream in = jfxBuildFile.getInputStream();
            if(in != null) {
                try {
                    isJfxCurrent = isJfxImplCurrentVer(computeCrc32( in ));
                } finally {
                    in.close();
                }
            }
        }
        if (!isJfxCurrent) {
            // try to close the file just in case the file is already opened in editor
            DataObject dobj = DataObject.find(jfxBuildFile);
            CloseCookie closeCookie = dobj.getLookup().lookup(CloseCookie.class);
            if (closeCookie != null) {
                closeCookie.close();
            }
            final FileObject nbproject = projDir.getFileObject("nbproject"); //NOI18N
            final String backupName = FileUtil.findFreeFileName(nbproject, "jfx-impl_backup", "xml"); //NOI18N
            FileUtil.moveFile(jfxBuildFile, nbproject, backupName);
            LOGGER.log(Level.INFO, "Old build script file jfx-impl.xml has been renamed to: {0}.xml", backupName); // NOI18N
            jfxBuildFile = null;

            try {
                final File readme = new File (FileUtil.toFile(nbproject), NbBundle.getMessage(JFXProjectUtils.class, "TXT_UPDATED_README_FILE_NAME")); //NOI18N
                if (!readme.exists()) {
                    readme.createNewFile();
                }
                final FileObject readmeFO = FileUtil.toFileObject(readme);
                returnFO = readmeFO;
                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                        @Override
                        public Void run() throws Exception {
                            OutputStream os = null;
                            FileLock lock = null;
                            try {
                                lock = readmeFO.lock();
                                os = readmeFO.getOutputStream(lock);
                                final PrintWriter out = new PrintWriter ( os );
                                try {
                                    final String headerTemplate = NbBundle.getMessage(JFXProjectUtils.class, "TXT_UPDATED_README_FILE_CONTENT_HEADER"); //NOI18N
                                    final String header = MessageFormat.format(headerTemplate, new Object[] {ProjectUtils.getInformation(proj).getDisplayName()});
                                    char[] underline = new char[header.length()];
                                    Arrays.fill(underline, '='); // NOI18N
                                    final String content = NbBundle.getMessage(JFXProjectUtils.class, "TXT_UPDATED_README_FILE_CONTENT"); //NOI18N
                                    out.println(underline);
                                    out.println(header);
                                    out.println(underline);
                                    out.println (MessageFormat.format(content, new Object[] {backupName + ".xml"})); //NOI18N
                                } finally {
                                    if(out != null) {
                                        out.close ();
                                    }
                                }
                            } finally {
                                if (lock != null) {
                                    lock.releaseLock();
                                }
                                if (os != null) {
                                    os.close();
                                }
                            }
                            return null;
                        }
                    });
                } catch (MutexException mux) {
                    throw (IOException) mux.getException();
                }
                
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "Cannot create file readme file. ", ioe); // NOI18N
            }        
        }
        if (jfxBuildFile == null) {
            FileObject templateFO = FileUtil.getConfigFile(JFX_BUILD_TEMPLATE);
            if (templateFO != null) {
                FileUtil.copyFile(templateFO, projDir.getFileObject("nbproject"), "jfx-impl"); // NOI18N
                LOGGER.log(Level.INFO, "Build script jfx-impl.xml has been updated to the latest version supported by this NetBeans installation."); // NOI18N
            }
        }
        return returnFO;
    }

    /**
     * Computes CRC code of data from InputStream
     * 
     * @param is InputStream to read data from
     * @return CRC code
     */
    static String computeCrc32(InputStream is) throws IOException {
        Checksum crc = new CRC32();
        int last = -1;
        int curr;
        while ((curr = is.read()) != -1) {
            if (curr != '\n' && last == '\r') {
                crc.update('\n');
            }
            if (curr != '\r') {
                crc.update(curr);
            }
            last = curr;
        }
        if (last == '\r') {
            crc.update('\n');
        }
        int val = (int)crc.getValue();
        String hex = Integer.toHexString(val);
        while (hex.length() < 8) {
            hex = "0" + hex; // NOI18N
        }
        return hex;
    }

    /**
     * Checks whether crc is the CRC code of current jfx-impl.xml template.
     * 
     * @param crc code to be compared against
     * @return true if crc is the CRC code of current jfx-impl.xml template.
     */
    static boolean isJfxImplCurrentVer(String crc) throws IOException {
        String _currentJfxImplCRC = currentJfxImplCRCCache;
        if (_currentJfxImplCRC == null) {
            final FileObject template = FileUtil.getConfigFile(JFX_BUILD_TEMPLATE);
            final InputStream in = template.getInputStream();
            if(in != null) {
                try {
                    currentJfxImplCRCCache = _currentJfxImplCRC = computeCrc32(in);
                } finally {
                    in.close();
                }
            }
        }
        return _currentJfxImplCRC.equals(crc);
    }

    /**
     * Checks the existence of default configuration for given RUN_AS type.
     * If it does not exist, it is created.
     * 
     * @param proj the project to check
     * @param runAs run type whose default configuration is to be updated
     * @param setBrowserProps if true, adds properties representing an existing browser
     * @return true is any update took place
     */
    public static boolean updateDefaultRunAsConfigFile(final @NonNull FileObject projDir, JFXProjectProperties.RunAsType runAs, boolean setBrowserProps) throws IOException {
        boolean updated = false;
        String configName = runAs.getDefaultConfig();
        String configFile = makeSafe(configName);
        String sharedPath = JFXProjectProperties.getSharedConfigFilePath(configFile);
        FileObject sharedCfgFO = projDir.getFileObject(sharedPath);
        final EditableProperties sharedCfgProps = sharedCfgFO != null ?
                JFXProjectProperties.readFromFile(sharedCfgFO) : new EditableProperties(true);
        assert sharedCfgProps != null;
        if(sharedCfgProps.isEmpty()) {
            sharedCfgProps.setProperty("$label", configName); // NOI18N
            sharedCfgProps.setComment("$label", new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_run_as_defaults")}, false); // NOI18N
            JFXProjectProperties.saveToFile(projDir, sharedPath, sharedCfgProps);
            updated = true;
        }
        String privatePath = JFXProjectProperties.getPrivateConfigFilePath(configFile);
        FileObject privateCfgFO = projDir.getFileObject(privatePath);
        final EditableProperties privateCfgProps = privateCfgFO != null ?
                JFXProjectProperties.readFromFile(projDir, privatePath) : new EditableProperties(true);
        assert privateCfgProps != null;
        if(privateCfgProps.isEmpty() || setBrowserProps) {
            privateCfgProps.setProperty("$label", configName); // NOI18N
            privateCfgProps.setComment("$label", new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_run_as_defaults")}, false); // NOI18N
            privateCfgProps.setProperty(JFXProjectProperties.RUN_AS, runAs.getString());
            privateCfgProps.setComment(JFXProjectProperties.RUN_AS, new String[]{"# " + NbBundle.getMessage(JFXProjectGenerator.class, "COMMENT_run_as_defaults")}, false); // NOI18N
            if(setBrowserProps) {
                Map<String,String> browserInfo = getDefaultBrowserInfo();
                if(browserInfo != null && !browserInfo.isEmpty()) {
                    for(Map.Entry<String,String> entry : browserInfo.entrySet()) {
                        privateCfgProps.setProperty(JFXProjectProperties.RUN_IN_BROWSER, entry.getKey());
                        privateCfgProps.setProperty(JFXProjectProperties.RUN_IN_BROWSER_PATH, entry.getValue());
                        break;
                    }
                }
            }
            JFXProjectProperties.saveToFile(projDir, privatePath, privateCfgProps);       
            updated = true;
        }
        return updated;
    }

    /**
     * @return name and path to default browser
     */
    public static Map<String,String> getDefaultBrowserInfo() {
        Lookup.Result<ExtWebBrowser> allBrowsers = Lookup.getDefault().lookupResult(ExtWebBrowser.class);
        Map<String,String> browserPaths = new HashMap<String, String>();
        for(Lookup.Item<ExtWebBrowser> browser : allBrowsers.allItems()) {
            String name = browser.getDisplayName();
            if(name != null && name.toLowerCase().contains("default")) { // NOI18N
                NbProcessDescriptor proc = browser.getInstance().getBrowserExecutable();
                String path = proc.getProcessName();
                if(JFXProjectProperties.isNonEmpty(path)) {
                    browserPaths.put(name, path);
                }
                break;
            }
        }
        return browserPaths;
    }
    
    /**
     * Returns a copy of existing list of maps, usually used to store application parameters
     * 
     * @param list2Copy
     * @return copy of list2Copy
     */
    public static List<Map<String,String>> copyList(List<Map<String,String>> list2Copy) {
        List<Map<String,String>> list2Return = new ArrayList<Map<String,String>>();
        if(list2Copy != null ) {
            for (Map<String,String> map : list2Copy) {
                list2Return.add(copyMap(map));
            }
        }
        return list2Return;
    }

    /**
     * Returns a copy of existing string map, usually used to store application parameter
     * (keys 'name' and 'value')
     * 
     * @param map2Copy
     * @return copy of map2Copy
     */
    public static Map<String,String> copyMap(Map<String,String> map2Copy) {
        Map<String,String> newMap = new HashMap<String,String>();
        if(map2Copy != null) {
            for(String key : map2Copy.keySet()) {
                String value = map2Copy.get(key);
                newMap.put(key, value);
            }
        }
        return newMap;
    }

    /**
     * Modifies name so that it can be used as file name,
     * i.e., replaces problematic characters.
     * 
     * @param name
     * @return modified name usable as file name
     */
    public static String makeSafe(@NonNull String name) {
        return name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N;
    }
}
