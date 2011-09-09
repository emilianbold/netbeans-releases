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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Utility class for JavaFX 2.0 Project
 * 
 * @author Petr Somol
 */
public final class JFXProjectUtils {

    private static Set<SearchKind> kinds = new HashSet<SearchKind>(Arrays.asList(SearchKind.IMPLEMENTORS));
    private static Set<SearchScope> scopes = new HashSet<SearchScope>(Arrays.asList(SearchScope.SOURCE));
    
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

}
