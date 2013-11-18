/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.avatar_js.project.ui.customizer.MainFileChooser;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.BaseActionProvider;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.ActionProvider;
import static org.netbeans.spi.project.ActionProvider.COMMAND_BUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_CLEAN;
import static org.netbeans.spi.project.ActionProvider.COMMAND_COMPILE_SINGLE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_COPY;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG_SINGLE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG_STEP_INTO;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG_TEST_SINGLE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DELETE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_MOVE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_PROFILE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_PROFILE_SINGLE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_PROFILE_TEST_SINGLE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_REBUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RENAME;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RUN;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RUN_SINGLE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_TEST;
import static org.netbeans.spi.project.ActionProvider.COMMAND_TEST_SINGLE;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Martin
 */
public class AvatarJSActionProvider extends BaseActionProvider {
    
    private static final Logger LOG = Logger.getLogger(AvatarJSActionProvider.class.getName());
    
    // Commands available from Avatar.js project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_PROFILE,
        COMMAND_PROFILE_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_PROFILE_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };

    private static final String[] platformSensitiveActions = {
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_PROFILE,
        COMMAND_PROFILE_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_PROFILE_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DEBUG_STEP_INTO,
    };
    
    private static final String[] actionsDisabledForQuickRun = {
        COMMAND_COMPILE_SINGLE,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
    };
    
    /** Map from commands to ant targets */
    private final Map<String,String[]> commands;
    
    /**Set of commands which are affected by background scanning*/
    private final Set<String> bkgScanSensitiveActions;

    /**Set of commands which need java model up to date*/
    private final Set<String> needJavaModelActions;
    
    AvatarJSActionProvider(AvatarJSProject project, UpdateHelper updateHelper) {
        super(
            project,
            updateHelper,
            project.evaluator(),
            project.getSourceRoots(),
            project.getTestSourceRoots(),
            project.getAntProjectHelper(),
            new CallbackImpl(project));
        
        commands = new HashMap<>();
        // treated specially: COMMAND_{,RE}BUILD
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_RUN_SINGLE, new String[] {"run-single"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug-single"}); // NOI18N
        commands.put(COMMAND_PROFILE, new String[] {"profile"}); // NOI18N
        commands.put(COMMAND_PROFILE_SINGLE, new String[] {"profile-single"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
        commands.put(COMMAND_TEST, new String[] {"test"}); // NOI18N
        commands.put(COMMAND_TEST_SINGLE, new String[] {"test-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug-test"}); // NOI18N
        commands.put(COMMAND_PROFILE_TEST_SINGLE, new String[]{"profile-test"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
        commands.put(COMMAND_DEBUG_STEP_INTO, new String[] {"debug-stepinto"}); // NOI18N

        this.bkgScanSensitiveActions = new HashSet<>(Arrays.asList(
            COMMAND_RUN,
            COMMAND_RUN_SINGLE,
            COMMAND_DEBUG,
            COMMAND_DEBUG_SINGLE,
            COMMAND_DEBUG_STEP_INTO
        ));
        
        this.needJavaModelActions = new HashSet<>(Arrays.asList(
            JavaProjectConstants.COMMAND_DEBUG_FIX
        ));
    }
    
    @ProjectServiceProvider(
            service=ActionProvider.class,
            projectTypes={@LookupProvider.Registration.ProjectType(id=AvatarJSProject.ID, position=100)})
    public static AvatarJSActionProvider create(@NonNull final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        final AvatarJSProject project = lkp.lookup(AvatarJSProject.class);
        final AvatarJSActionProvider avatarJSActionProvider = new AvatarJSActionProvider(project, project.getUpdateHelper());
        avatarJSActionProvider.startFSListener();
        return avatarJSActionProvider;
    }

    @Override
    protected String[] getPlatformSensitiveActions() {
        return platformSensitiveActions;
    }

    @Override
    protected String[] getActionsDisabledForQuickRun() {
        return actionsDisabledForQuickRun;
    }

    @Override
    public Map<String, String[]> getCommands() {
        return commands;
    }

    @Override
    protected Set<String> getScanSensitiveActions() {
        return bkgScanSensitiveActions;
    }

    @Override
    protected Set<String> getJavaModelActions() {
        return needJavaModelActions;
    }

    @Override
    protected boolean isCompileOnSaveEnabled() {
        /*
        String compileOnSaveProperty = ((AvatarJSProject) getProject()).evaluator().getProperty(ProjectProperties.COMPILE_ON_SAVE);

        return (compileOnSaveProperty != null && Boolean.valueOf(compileOnSaveProperty));// && J2SEProjectUtil.isCompileOnSaveSupported(project);
        */
        return false;
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions;
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) {
        switch (command) {
            case COMMAND_RUN_SINGLE:
            case COMMAND_DEBUG_SINGLE:
            case COMMAND_PROFILE_SINGLE:
                FileObject fos[] = findJSSources(context, getJSSourceRoots());
                if (fos != null && fos.length == 1) {
                    return true;
                }
                return false;
            default:
                return super.isActionEnabled(command, context);
        }
    }
    
    @Override
    public String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        return getTargetNames(command, context, p, false);
    }
    
    @Override
    public String[] getTargetNames(String command, Lookup context, Properties p, boolean doJavaChecks) throws IllegalArgumentException {
        switch (command) {
            case COMMAND_RUN:
            case COMMAND_DEBUG:
            case COMMAND_DEBUG_STEP_INTO:
            case COMMAND_PROFILE:
                String config = getEvaluator().getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG);
                String path;
                if (config == null || config.length() == 0) {
                    path = AntProjectHelper.PROJECT_PROPERTIES_PATH;
                } else {
                    // Set main class for a particular config only.
                    path = "nbproject/configs/" + config + ".properties"; // NOI18N
                }
                EditableProperties ep = getUpdateHelper().getProperties(path);

                // check project's main class
                // Check whether main class is defined in this config. Note that we use the evaluator,
                // not ep.getProperty(MAIN_CLASS), since it is permissible for the default pseudoconfig
                // to define a main class - in this case an active config need not override it.
                String mainFile = getEvaluator().getProperty("main.file");
                while (!isValidMainFile(mainFile)) {
                    boolean isSet = showSelectMainFile(mainFile, ep);
                    if (!isSet) {
                        return null;
                    }
                    mainFile = ep.get("main.file");
                    try {
                        if (getUpdateHelper().requestUpdate()) {
                            getUpdateHelper().putProperties(path, ep);
                            ProjectManager.getDefault().saveProject(getProject());
                        }
                        else {
                            return null;
                        }
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Error while saving project: " + ioe);
                    }
                }
                if (command.equals(COMMAND_RUN) || command.equals(COMMAND_PROFILE)) {
                    p.setProperty("run.file", mainFile); // NOI18N
                } else {
                    p.setProperty("debug.file", mainFile); // NOI18N
                }
                break;
                
            case COMMAND_RUN_SINGLE:
            case COMMAND_DEBUG_SINGLE:
            case COMMAND_PROFILE_SINGLE:
                FileObject[] files = null;//findTestSources(context, false);
                FileObject[] rootz = null;//projectTestRoots.getRoots();
                boolean isTest = true;
                if (files == null) {
                    isTest = false;
                    rootz = getJSSourceRoots();
                    files = findJSSources(context, rootz);
                }
//                if (LOG.isLoggable(Level.FINE)) {
//                    LOG.log(Level.FINE, "Is test: {0} Files: {1} Roots: {2}",    //NOI18N
//                            new Object[]{
//                                isTest,
//                                asPaths(files),
//                                asPaths(rootz)
//                    });
//                }
                if (files == null) {
                    //The file was not found under the source roots
                    return null;
                }
                final FileObject file = files[0];
                assert file != null;
                if (!file.isValid()) {
                    LOG.log(Level.WARNING,
                            "FileObject to execute: {0} is not valid.",
                            FileUtil.getFileDisplayName(file));   //NOI18N
                    return null;
                }
                String fileName = FileUtil.getRelativePath(getRoot(rootz, file), file);
                if (fileName == null) {
                    return null;
                }
                //p.setProperty("javac.includes", clazz); // NOI18N
                if (command.equals(COMMAND_DEBUG_SINGLE)) {
                    p.setProperty("debug.file", fileName); // NOI18N
                } else {
                    p.setProperty("run.file", fileName); // NOI18N
                }
                return getCommands().get(command);
            
            case COMMAND_BUILD:
            case COMMAND_CLEAN:
            case COMMAND_REBUILD:
            case COMMAND_COMPILE_SINGLE:
            default:
                break;
        }
        return super.getTargetNames(command, context, p, false);
    }
    
    private boolean isValidMainFile(String mainFile) {
        if (mainFile == null || mainFile.isEmpty()) {
            return false;
        }
        FileObject prjRoot = getProject().getProjectDirectory();
        FileObject jsRoot = prjRoot.getFileObject(AvatarJSProject.CONFIG_JS_SOURCE_PATH);
        if (jsRoot == null) {
            return false;
        }
        FileObject mfo = jsRoot.getFileObject(mainFile);
        return mfo != null && mfo.isData();
    }
    
    @NbBundle.Messages({"LBL_SelectMainFile_OK=OK",
                        "AD_SelectMainFile_OK=Press to confirm the selected main file.",
                        "# {0} - project name",
                        "LBL_MainFileNotFound=Project {0} does not have a main file set.",
                        "# {0} - name of file",
                        "# {1} - project name",
                        "LBL_MainFileWrong={0} file wasn''t found in {1} project.",
                        "CTL_MainFileWarning_Title=Run Project"})
    boolean showSelectMainFile(String mainFile, EditableProperties ep) {
        boolean selected = false;
        FileObject prjRoot = getProject().getProjectDirectory();
        FileObject jsRoot = prjRoot.getFileObject(AvatarJSProject.CONFIG_JS_SOURCE_PATH);
        if (jsRoot == null) {
            return false;
        }
        String prjName = ProjectUtils.getInformation(getProject()).getDisplayName();
        String message;
        if (mainFile == null || mainFile.isEmpty()) {
            message = Bundle.LBL_MainFileNotFound(prjName);
        } else {
            message = Bundle.LBL_MainFileWrong(mainFile, prjName);
        }
        final MainFileChooser mfch = new MainFileChooser(message, jsRoot);
        final JButton okButton = new JButton(Bundle.LBL_SelectMainFile_OK());
        okButton.getAccessibleContext().setAccessibleDescription(Bundle.AD_SelectMainFile_OK());
        mfch.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof MouseEvent && MouseUtils.isDoubleClick(((MouseEvent) e.getSource()))) {
                    // click button and the finish dialog with selected class
                    okButton.doClick();
                } else {
                    okButton.setEnabled (mfch.getSelectedFile() != null);
                }
            }
        });
        Object[] options = new Object[] {
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };
        okButton.setEnabled(false);
        DialogDescriptor desc = new DialogDescriptor(
                mfch,
                Bundle.CTL_MainFileWarning_Title(),
                true,
                options,
                options[0],
                DialogDescriptor.BOTTOM_ALIGN, null, null);
        desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
        dlg.setVisible (true);
        if (desc.getValue() == options[0]) {
            selected = true;
            mainFile = mfch.getSelectedFile();
            ep.put("main.file", mainFile == null ? "" : mainFile);
        }
        dlg.dispose();
        
        return selected;
    }
    
    private FileObject[] getJSSourceRoots() {
        FileObject srcDir = getProject().getProjectDirectory().getFileObject(AvatarJSProject.CONFIG_JS_SOURCE_PATH);
        return new FileObject[] { srcDir };
    }
    
    private static @CheckForNull FileObject[] findJSSources(Lookup context, FileObject[] srcPath) {
        for (FileObject path : srcPath) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, path, AvatarJSProject.JS_FILE_EXT, true);
            if (files != null) {
                return files;
            }
        }
        return null;
    }

    private static FileObject getRoot (FileObject[] roots, FileObject file) {
        assert file != null : "File can't be null";   //NOI18N
        FileObject srcDir = null;
        for (int i=0; i< roots.length; i++) {
            assert roots[i] != null : "Source Path Root can't be null"; //NOI18N
            if (FileUtil.isParentOf(roots[i],file) || roots[i].equals(file)) {
                srcDir = roots[i];
                break;
            }
        }
        return srcDir;
    }

    
    private static final class CallbackImpl implements Callback3 {

        private final AvatarJSProject prj;

        CallbackImpl(@NonNull final AvatarJSProject project) {
            Parameters.notNull("project", project); //NOI18N
            this.prj = project;
        }

        @Override
        public Map<String, String> createAdditionalProperties(String command, Lookup context) {
            return Collections.emptyMap();
        }

        @Override
        public Set<String> createConcealedProperties(String command, Lookup context) {
            return Collections.emptySet();
        }

        @Override
        public void antTargetInvocationStarted(String command, Lookup context) {
        }

        @Override
        public void antTargetInvocationFinished(String command, Lookup context, int result) {
        }

        @Override
        public void antTargetInvocationFailed(String command, Lookup context) {
        }

        @Override
        public ClassPath getProjectSourcesClassPath(String type) {
            return prj.getClassPathProvider().getProjectSourcesClassPath(type);
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            return prj.getClassPathProvider().findClassPath(file, type);
        }
    }

}
