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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.groovy.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * ONLY FOR J2SE PROJECT TYPE!
 * 
 * Instance of this class and LookupMerger<ActionProvider> is added to J2SEProject's lookup.
 * If somebody else will add another merger of ActionProvider, then good luck...
 * 
 * There's no API to hook into project's Run file action implementations,
 * so this is extract from J2SEActionProvider to support:
 * - Run File
 * - Debug File (not implemented yet)
 * Basically only modification is changing hardcoded .java to .groovy plus
 * some changes needed to solve lack of J2SE project internals
 * 
 * @author Martin Adamek
 */
@ProjectServiceProvider(service=ActionProvider.class, projectType="org-netbeans-modules-java-j2seproject")
public class GroovyActionProvider implements ActionProvider {

    // from J2SEProjectProperties
    public static final String BUILD_SCRIPT ="buildfile";      //NOI18N
    // from J2SEConfigurationProvider
    public static final String PROP_CONFIG = "config"; // NOI18N

    // Commands available from J2SE project
    private static final String[] supportedActions = {
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG_SINGLE
    };


    private static final String[] platformSensitiveActions = {
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG_SINGLE
    };

    /** Map from commands to ant targets */
    Map<String,String[]> commands;

    private final Project project;
    /**Set of commands which are affected by background scanning*/
    final Set<String> bkgScanSensitiveActions;
    
    public GroovyActionProvider(Project project) {
        this.project = project;
        commands = new HashMap<String,String[]>();
        // treated specially: COMMAND_{,RE}BUILD
        commands.put(COMMAND_RUN_SINGLE, new String[] {"run-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug-single"}); // NOI18N

        this.bkgScanSensitiveActions = new HashSet<String>(Arrays.asList(
            COMMAND_RUN_SINGLE,
            COMMAND_DEBUG_SINGLE
        ));
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }

    public void invokeAction(final String command, final Lookup context) throws IllegalArgumentException {
        final Runnable action = new Runnable () {
            public void run () {
                Properties p = new Properties();
                String[] targetNames;

                targetNames = getTargetNames(command, context, p);
                if (targetNames == null) {
                    return;
                }
                if (targetNames.length == 0) {
                    targetNames = null;
                }
                if (p.keySet().size() == 0) {
                    p = null;
                }
                try {
                    FileObject buildFo = findBuildXml();
                    if (buildFo == null || !buildFo.isValid()) {
                        //The build.xml was deleted after the isActionEnabled was called
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(GroovyActionProvider.class,
                                "LBL_No_Build_XML_Found"), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                    else {
                        ActionUtils.runTarget(buildFo, targetNames, p);
                    }
                }
                catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        };

        if (this.bkgScanSensitiveActions.contains(command)) {
            ScanDialog.runWhenScanFinished(action, NbBundle.getMessage (GroovyActionProvider.class,"ACTION_"+command));   //NOI18N
        }
        else {
            action.run();
        }
    }

    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (command.equals(COMMAND_RUN_SINGLE)) {
            FileObject fos[] = findSources(context);
            return fos != null && fos.length == 1;
        }
        return false;
    }

    private String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        Map<String,String[]> targetsFromConfig = loadTargetsFromConfig();
        String[] targetNames = new String[0];
        if (command.equals (COMMAND_RUN_SINGLE) || command.equals (COMMAND_DEBUG_SINGLE)) {
            FileObject[] files = findTestSources(context, false);
            if (files != null) {
                if (command.equals(COMMAND_RUN_SINGLE)) {
                    targetNames = setupTestSingle(p, files);
                } else {
                    targetNames = setupDebugTestSingle(p, files);
                }
            } else {
                FileObject file = findSources(context)[0];
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                String clazz = FileUtil.getRelativePath(getRoot(sourceGroups, file), file);
                p.setProperty("javac.includes", clazz); // NOI18N
                // Convert foo/FooTest.java -> foo.FooTest
                if (clazz.endsWith(".groovy")) { // NOI18N
                    clazz = clazz.substring(0, clazz.length() - 7);
                }
                clazz = clazz.replace('/','.');
//                final boolean hasMainClassFromTest = MainClassChooser.unitTestingSupport_hasMainMethodResult == null ? false :
//                    MainClassChooser.unitTestingSupport_hasMainMethodResult.booleanValue();
////                final Collection<ElementHandle<TypeElement>> mainClasses = J2SEProjectUtil.getMainMethods (file);
//                if (!hasMainClassFromTest){// && mainClasses.isEmpty()) {
//                    NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(GroovyActionProvider.class, "LBL_No_Main_Classs_Found", clazz), NotifyDescriptor.INFORMATION_MESSAGE);
//                    DialogDisplayer.getDefault().notify(nd);
//                    return null;
//                } else {
//                    if (!hasMainClassFromTest) {
////                        if (mainClasses.size() == 1) {
////                            //Just one main class
////                            clazz = mainClasses.iterator().next().getBinaryName();
////                        }
////                        else {
////                            //Several main classes, let the user choose
////                            clazz = showMainClassWarning(file, mainClasses);
////                            if (clazz == null) {
////                                return null;
////                            }
////                        }
//                    }
                    if (command.equals (COMMAND_RUN_SINGLE)) {
                        p.setProperty("run.class", clazz); // NOI18N
                        String[] targets = targetsFromConfig.get(command);
                        targetNames = (targets != null) ? targets : commands.get(COMMAND_RUN_SINGLE);
                    } else {
                        p.setProperty("debug.class", clazz); // NOI18N
                        String[] targets = targetsFromConfig.get(command);
                        targetNames = (targets != null) ? targets : commands.get(COMMAND_DEBUG_SINGLE);
                    }
//                }
            }
        }

        return targetNames;
    }
    
    private FileObject getRoot (SourceGroup[] groups, FileObject file) {
        assert file != null : "File can't be null";   //NOI18N
        FileObject srcDir = null;
        for (int i=0; i< groups.length; i++) {
            FileObject root = groups[i].getRootFolder();
            assert root != null : "Source Path Root can't be null"; //NOI18N
            if (FileUtil.isParentOf(root, file) || root.equals(file)) {
                srcDir = root;
                break;
            }
        }
        return srcDir;
    }

    private FileObject getRoot (FileObject[] groups, FileObject file) {
        assert file != null : "File can't be null";   //NOI18N
        FileObject srcDir = null;
        for (FileObject root : groups) {
            assert root != null : "Source Path Root can't be null"; //NOI18N
            if (FileUtil.isParentOf(root, file) || root.equals(file)) {
                srcDir = root;
                break;
            }
        }
        return srcDir;
    }

    private FileObject[] findSources(Lookup context) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i=0; i< sourceGroups.length; i++) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, sourceGroups[i].getRootFolder(), ".groovy", true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }

    /** Find either selected tests or tests which belong to selected source files
     */
    private FileObject[] findTestSources(Lookup context, boolean checkInSrcDir) {
        //XXX: Ugly, should be rewritten
        FileObject[] testSrcPath = getTestSourceRoots(project);
        for (int i=0; i< testSrcPath.length; i++) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcPath[i], ".groovy", true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }

    private FileObject findBuildXml() {
        return getBuildXml(project);
    }

    public static String getBuildXmlName (final Project project) {
        assert project != null;
        String buildScriptPath = evaluateProperty(project, BUILD_SCRIPT);
        if (buildScriptPath == null) {
            buildScriptPath = GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return buildScriptPath;
    }
    
    public static FileObject getBuildXml (final Project project) {
        return project.getProjectDirectory().getFileObject (getBuildXmlName(project));
    }

    private static FileObject[] getTestSourceRoots(Project project) {
        List<String> names = getTestRootsNames(project);
        List<FileObject> result = new ArrayList<FileObject>();
        for (String name : names) {
            FileObject root = project.getProjectDirectory().getFileObject(name);
            if (root != null) {
                result.add(root);
            }
        }
        return result.toArray(new FileObject[result.size()]);
    }

    private static List<String> getTestRootsNames(Project project) {
        // damn crazy hack, how do I get test source roots?

        // XXX Use J2SEPropertyEvaluator from j2seproject friend API
        // have to look at other possible property files
        File propFile = FileUtil.toFile(project.getProjectDirectory().getFileObject("nbproject/project.properties")); // NOI18N
        Map<String, String> map = PropertyUtils.propertiesFilePropertyProvider(propFile).getProperties();

        List<String> result = new ArrayList<String>();

        for (String key : map.keySet()) {
            if (key.startsWith("test.") && key.endsWith(".dir")) { // NOI18N
                result.add(map.get(key));
            }
        }

        return result;
    }

    private static String evaluateProperty(Project project, String key) {
        // XXX Use J2SEPropertyEvaluator from j2seproject friend API
        // have to look at other possible property files
        File propFile = FileUtil.toFile(project.getProjectDirectory().getFileObject("nbproject/project.properties")); // NOI18N
        Map<String, String> map = PropertyUtils.propertiesFilePropertyProvider(propFile).getProperties();
        return map.get(key);
    }

    private String[] setupTestSingle(Properties p, FileObject[] files) {
        FileObject[] testSrcPath = getTestSourceRoots(project);
        FileObject root = getRoot(testSrcPath, files[0]);
        p.setProperty("test.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        p.setProperty("javac.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        return new String[] {"test-single"}; // NOI18N
    }

    private String[] setupDebugTestSingle(Properties p, FileObject[] files) {
        FileObject[] testSrcPath = getTestSourceRoots(project);
        FileObject root = getRoot(testSrcPath, files[0]);
        String path = FileUtil.getRelativePath(root, files[0]);
        // Convert foo/FooTest.java -> foo.FooTest
        p.setProperty("test.class", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
        p.setProperty("javac.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        return new String[] {"debug-test"}; // NOI18N
    }

    // loads targets for specific commands from shared config property file
    // returns map; key=command name; value=array of targets for given command
    private HashMap<String,String[]> loadTargetsFromConfig() {
        HashMap<String,String[]> targets = new HashMap<String,String[]>(6);
        String config = evaluateProperty(project, PROP_CONFIG);
        // load targets from shared config
        FileObject propFO = project.getProjectDirectory().getFileObject("nbproject/configs/" + config + ".properties");
        if (propFO == null) {
            return targets;
        }
        Properties props = new Properties();
        try {
            InputStream is = propFO.getInputStream();
            try {
                props.load(is);
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return targets;
        }
        Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            if (propName.startsWith("$target.")) {
                String tNameVal = props.getProperty(propName);
                String cmdNameKey = null;
                if (tNameVal != null && !tNameVal.equals("")) {
                    cmdNameKey = propName.substring("$target.".length());
                    StringTokenizer stok = new StringTokenizer(tNameVal.trim(), " ");
                    List<String> targetNames = new ArrayList<String>(3);
                    while (stok.hasMoreTokens()) {
                        targetNames.add(stok.nextToken());
                    }
                    targets.put(cmdNameKey, targetNames.toArray(new String[targetNames.size()]));
                }
            }
        }
        return targets;
    }

    private static final class MainClassChooser {
        private static Boolean unitTestingSupport_hasMainMethodResult;

    }

}
