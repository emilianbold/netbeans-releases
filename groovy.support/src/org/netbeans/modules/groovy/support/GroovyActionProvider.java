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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ActionProvider;
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
public class GroovyActionProvider implements ActionProvider {

    // from J2SEProjectProperties
    public static final String BUILD_SCRIPT ="buildfile";      //NOI18N

    private static final String[] supportedActions = {
        COMMAND_RUN_SINGLE
    };

    private final Project project;
    /**Set of commands which are affected by background scanning*/
    final Set<String> bkgScanSensitiveActions;
    
    public GroovyActionProvider(Project project) {
        this.project = project;
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
        if (command.equals(COMMAND_RUN_SINGLE)) {
            FileObject file = findSources(context)[0];
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            String clazz = FileUtil.getRelativePath(getRoot(sourceGroups, file), file);
            p.setProperty("javac.includes", clazz); // NOI18N
            // Convert foo/FooTest.groovy -> foo.FooTest
            if (clazz.endsWith(".groovy")) { // NOI18N
                clazz = clazz.substring(0, clazz.length() - 7);
            }
            clazz = clazz.replace('/','.');
            p.setProperty("run.class", clazz); // NOI18N
            return new String[] { "run-single" }; // NOI18N
        }
        return new String[0];
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

    private FileObject findBuildXml() {
        return getBuildXml(project);
    }

    public static String getBuildXmlName (final Project project) {
        assert project != null;
        // XXX Use J2SEPropertyEvaluator from j2seproject friend API
        // have to look at other possible property files
        File propFile = FileUtil.toFile(project.getProjectDirectory().getFileObject("nbproject/project.properties")); // NOI18N
        Map<String, String> map = PropertyUtils.propertiesFilePropertyProvider(propFile).getProperties();
        String buildScriptPath = map.get(BUILD_SCRIPT);
        if (buildScriptPath == null) {
            buildScriptPath = GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return buildScriptPath;
    }
    
    public static FileObject getBuildXml (final Project project) {
        return project.getProjectDirectory().getFileObject (getBuildXmlName(project));
    }

}
