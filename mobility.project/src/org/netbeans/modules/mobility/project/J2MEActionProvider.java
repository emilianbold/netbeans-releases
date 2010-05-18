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

package org.netbeans.modules.mobility.project;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.ui.CfgSelectionPanel;
import org.netbeans.spi.project.support.ant.*;
import org.netbeans.modules.mobility.project.ui.QuickRunPanel;
import org.netbeans.modules.mobility.project.ui.actions.AddConfigurationAction;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.*;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Action provider of the J2SE project. This is the place where to do
 * strange things to J2SE actions. E.g. compile-single.
 */
public class J2MEActionProvider implements ActionProvider {
    
    // Definition of commands
    public static final String COMMAND_COMPILE = "compile"; /*XXX define somewhere*/ // NOI18N
    public static final String COMMAND_OBFUSCATE = "obfuscate"; /*XXX define somewhere*/ // NOI18N
    public static final String COMMAND_PREVERIFY = "preverify"; /*XXX define somewhere*/ // NOI18N
    public static final String COMMAND_JAR = "jar"; /*XXX define somewhere*/ // NOI18N
    public static final String COMMAND_RUN_WITH = "runwith"; /*XXX define somewhere*/ // NOI18N
    public static final String COMMAND_JAVADOC = "javadoc"; /*XXX define somewhere*/ // NOI18N
    public static final String COMMAND_DEPLOY = "deploy"; /*XXX define somewhere*/ // NOI18N
    public static final String COMMAND_CLEAN_ALL = "clean-all"; /*XXX define somewhere*/ // NOI18N
    public static final String COMMAND_BUILD_ALL = "build-all"; /*XXX define somewhere*/ // NOI18N
    public static final String COMMAND_REBUILD_ALL = "rebuild-all"; /*XXX define somewhere*/ // NOI18N
    public static final String COMMAND_JAVADOC_ALL = "javadoc-all"; /*XXX define somewhere*/ // NOI18N
    public static final String COMMAND_DEPLOY_ALL = "deploy-all"; /*XXX define somewhere*/ // NOI18N
    
    // Commands available from J2ME project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE,
        COMMAND_COMPILE_SINGLE,
        COMMAND_OBFUSCATE,
        COMMAND_PREVERIFY,
        COMMAND_JAR,
        COMMAND_RUN,
        COMMAND_RUN_WITH,
        COMMAND_DEBUG,
        //commenting out the next line disables the step into action in the IDE
        COMMAND_JAVADOC,
        COMMAND_DEPLOY,
        COMMAND_BUILD_ALL,
        COMMAND_CLEAN_ALL,
        COMMAND_REBUILD_ALL,
        COMMAND_JAVADOC_ALL,
        COMMAND_DEPLOY_ALL,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };
    
    /**Set of commands which are affected by background scanning*/
    final Set<String> bkgScanSensitiveActions;
    
    // Ant project helper of the project
    final protected AntProjectHelper helper;
    final protected J2MEProject project;
    
    /** Map from commands to ant targets */
    Map<String,String[]> commands;
    
    public J2MEActionProvider(J2MEProject project, AntProjectHelper helper) {
        commands = new HashMap<String,String[]>();
        commands.put(COMMAND_BUILD, new String[] {"build"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[] {"rebuild"}); // NOI18N
        commands.put(COMMAND_COMPILE, new String[] {"compile"}); // NOI18N
        commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
        commands.put(COMMAND_OBFUSCATE, new String[] {"obfuscate"}); // NOI18N
        commands.put(COMMAND_PREVERIFY, new String[] {"preverify"}); // NOI18N
        commands.put(COMMAND_JAR, new String[] {"jar"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(COMMAND_RUN_WITH, new String[] {"run-no-build"}); // NOI18N
        commands.put(COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
        commands.put(COMMAND_DEPLOY, new String[] {"deploy"}); // NOI18N
        commands.put(COMMAND_BUILD_ALL, new String[] {"build-all"}); // NOI18N
        commands.put(COMMAND_CLEAN_ALL, new String[] {"clean-all"}); // NOI18N
        commands.put(COMMAND_REBUILD_ALL, new String[] {"rebuild-all"}); // NOI18N
        commands.put(COMMAND_JAVADOC_ALL, new String[] {"javadoc-all"}); // NOI18N
        commands.put(COMMAND_DEPLOY_ALL, new String[] {"deploy-all"}); // NOI18N
        this.bkgScanSensitiveActions = new HashSet<String>(Arrays.asList(new String[] {
            COMMAND_DEBUG,
        }));
        this.project = project;
        this.helper = helper;
    }
    
    protected FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    public String[] getSupportedActions() {
        return supportedActions.clone();
    }
    
    protected FileObject[] findSourcesAndPackages(final Lookup context, final FileObject srcDir) {
        if (srcDir != null) {
            final FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, null, true); // NOI18N
            //Check if files are either packages of java files
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isFolder() && !"java".equals(files[i].getExt())) { //NOI18N
                        return null;
                    }
                }
            }
            return files;
        }
        return null;
    }
    
    public String[] getTargetNames(final String command) {
        return commands.get(command);
    }
    
    public void invokeAction(final String command, final Lookup context ) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }
        
        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }
        
        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }
        
        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }
        if (COMMAND_BUILD_ALL.equals(command) || COMMAND_CLEAN_ALL.equals(command)
                || COMMAND_DEPLOY_ALL.equals(command) || COMMAND_JAVADOC_ALL.equals(command)
                || COMMAND_REBUILD_ALL.equals(command)) {
            if (!showCfgSelectionDialog(command)) return ;
        }
        
        final Runnable action = new Runnable() {
            public void run() {
                String[] targetNames = getTargetNames(command);
                if (targetNames == null) {
                    throw new IllegalArgumentException(command);
                }
                EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ProjectConfigurationsHelper confs = project.getConfigurationHelper();
                String activeConfiguration = confs.getActiveConfiguration() != confs.getDefaultConfiguration() ? confs.getActiveConfiguration().getDisplayName() : null;
                Properties p = new Properties();
                if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
                    String sDir = helper.getStandardPropertyEvaluator().getProperty("src.dir"); //NOI18N
                    if (sDir != null) {
                        FileObject srcDir = helper.resolveFileObject(sDir);
                        if (srcDir != null) {
                            FileObject[] files = findSourcesAndPackages(context, srcDir);
                            if (files != null) {
                                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, srcDir)); // NOI18N
                            }
                        }
                    }
                } else if (COMMAND_DEBUG.equals(command)) {
                    p.put(DefaultPropertiesDescriptor.OBFUSCATION_LEVEL, "0"); //NOI18N
                    p.put("app.codename", project.getName()); // NOI18N
                }
                if (COMMAND_RUN.equals(command) || COMMAND_RUN_WITH.equals(command) || COMMAND_DEBUG.equals(command))  {
                    String url = getJadURL();
                    if (url != null)
                        p.put("dist.jad.url", url); //NOI18N
                    else {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(J2MEActionProvider.class, "ERR_GetJadURL"), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
                        return;
                    }
                }
                if (COMMAND_RUN_WITH.equals(command)) {
                    QuickRunPanel qrp = new QuickRunPanel(J2MEPlatform.SPECIFICATION_NAME, evaluateProperty(ep, DefaultPropertiesDescriptor.PLATFORM_ACTIVE, activeConfiguration), evaluateProperty(ep, DefaultPropertiesDescriptor.PLATFORM_DEVICE, activeConfiguration));
                    DialogDescriptor dd = new DialogDescriptor(qrp, NbBundle.getMessage(J2MEActionProvider.class, "Title_QuickRun"), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(J2MEActionProvider.class), null); //NOI18N
                    if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd)) && qrp.getPlatformName() != null && qrp.getDeviceName() != null) {
                        p.put(DefaultPropertiesDescriptor.PLATFORM_ACTIVE, qrp.getPlatformName());
                        p.put(DefaultPropertiesDescriptor.PLATFORM_DEVICE, qrp.getDeviceName());
                        p.put(DefaultPropertiesDescriptor.RUN_USE_SECURITY_DOMAIN, Boolean.FALSE.toString());
                    } else return;
                }
                try {
                    FileObject buildScript = findBuildXml();
                    if (buildScript != null) {
                        ActionUtils.runTarget(buildScript, targetNames, p);
                    } else {
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(J2MEActionProvider.class,
                                "MSG_NO_BUILD_SCRIPT")); //NOI18N
                    }
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        };
        
        //        if (this.bkgScanSensitiveActions.contains(command)) {
        //            JavaMetamodel.getManager().invokeAfterScanFinished(action, NbBundle.getMessage(J2MEActionProvider.class,"ACTION_"+command)); //NOI18N
        //        } else {
        action.run();
        //        }
    }
    
    private boolean showCfgSelectionDialog(final String command) {
        String allCfg = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(DefaultPropertiesDescriptor.ALL_CONFIGURATIONS);
        if (allCfg == null) return false;
        //Just default configuration
        if (allCfg.trim().length() == 0) return true;
        //Ok we have more configs, so which one do we want to build
        final EditableProperties priv = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        String selectedCfg = priv.getProperty(DefaultPropertiesDescriptor.SELECTED_CONFIGURATIONS);
        CfgSelectionPanel panel = new CfgSelectionPanel(allCfg, selectedCfg);
        String title = NbBundle.getMessage(AddConfigurationAction.class, "Title_CfgSelection_" + command); //NOI18N
        panel.getAccessibleContext().setAccessibleName(title);//NOI18N
        panel.getAccessibleContext().setAccessibleDescription(title);//NOI18N
        boolean result = DialogDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(
                new DialogDescriptor(panel,
                title, true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(CfgSelectionPanel.class), null)));

        final String newSel = panel.getSelectedConfigurations();
        boolean configurationChanged = ((selectedCfg == null) != (newSel == null)) || (selectedCfg != null && !selectedCfg.equals(newSel));
        if (result && configurationChanged) { //NOI18N
            ProjectManager.mutex().writeAccess(new Runnable() {
                public void run() {
                    priv.put(DefaultPropertiesDescriptor.SELECTED_CONFIGURATIONS, newSel);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, priv);
                    try {
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            });
        }
        return result;
    }
    
    protected String evaluateProperty(final EditableProperties ep, final String propertyName, final String configuration) {
        if (configuration == null)
            return ep.getProperty(propertyName);
        final String value = ep.getProperty(J2MEProjectProperties.CONFIG_PREFIX + configuration + "." + propertyName); // NOI18N
        return value != null ? value : evaluateProperty(ep, propertyName, null);
    }
    
    protected String getJadURL() {
        final FileObject fo = FileUtil.getConfigFile("HTTPServer_DUMMY"); // NOI18N
        final URL base = URLMapper.findURL(fo, URLMapper.NETWORK);
        if (base == null)
            return null;
        
        final PropertyEvaluator eval = helper.getStandardPropertyEvaluator();
        try {
            final URL newURL = new URL(base.getProtocol(), "localhost", base.getPort(), //NOI18N
                    encodeURL("/servlet/org.netbeans.modules.mobility.project.jam.JAMServlet/" + helper.getProjectDirectory().getPath() + "/" + eval.evaluate("${dist.dir}/${dist.jad}"))); // NOI18N
            return newURL.toExternalForm();
        } catch (MalformedURLException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
    }
    
    @SuppressWarnings("deprecation")
    private String encodeURL(final String orig) {
        final StringTokenizer slashTok = new StringTokenizer(orig, "/", true); // NOI18N
        final StringBuffer path = new StringBuffer();
        while (slashTok.hasMoreTokens()) {
            final String tok = slashTok.nextToken();
            if (tok.startsWith("/")) { // NOI18N
                path.append(tok);
            } else {
                try {
                    path.append(URLEncoder.encode(tok, "UTF-8")); // NOI18N
                } catch (UnsupportedEncodingException e) {
                    path.append(URLEncoder.encode(tok));
                }
            }
        }
        return path.toString();
    }
    
    public boolean isActionEnabled( @SuppressWarnings("unused") final String command,
            final Lookup context ) {
        if (COMMAND_RUN_WITH.equals(command)) {
            //Temporary workaround for http://www.netbeans.org/issues/show_bug.cgi?id=151778 -
            //disable Run With on CDC projects
            Collection<? extends Project> projects = context.lookupAll(Project.class);
            for (Project p : projects) {
                J2MEProject meProj = p.getLookup().lookup(J2MEProject.class); //wrapping
                if (meProj == null) { //multi-selection of heterogenous types
                    System.err.println("No J2MEProject in " + p + " lookup contents " + p.getLookup().lookupAll(Object.class));
                    return false;
                }
                EditableProperties props = meProj.helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                if (props != null) {
                    String trig = props.getProperty("platform.trigger"); //NOI18N
                    if ("CDC".equals(trig)) { //NOI18N
                        System.err.println("Found CDC trigger: " + trig);
                        return false;
                    }
                } else {
                    System.err.println("No props, bail");
                    return false;
                }
            }
        }
        return true;
    }
}
