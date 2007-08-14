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

package org.netbeans.modules.mobility.project;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.ui.CfgSelectionPanel;
import org.netbeans.spi.project.support.ant.*;
import org.netbeans.modules.mobility.project.ui.QuickRunPanel;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.*;
import org.openide.util.HelpCtx;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
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
                    QuickRunPanel qrp = new QuickRunPanel(evaluateProperty(ep, DefaultPropertiesDescriptor.PLATFORM_ACTIVE, activeConfiguration), evaluateProperty(ep, DefaultPropertiesDescriptor.PLATFORM_DEVICE, activeConfiguration));
                    DialogDescriptor dd = new DialogDescriptor(qrp, NbBundle.getMessage(J2MEActionProvider.class, "Title_QuickRun"), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(J2MEActionProvider.class), null); //NOI18N
                    if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd)) && qrp.getPlatformName() != null && qrp.getDeviceName() != null) {
                        p.put(DefaultPropertiesDescriptor.PLATFORM_ACTIVE, qrp.getPlatformName());
                        p.put(DefaultPropertiesDescriptor.PLATFORM_DEVICE, qrp.getDeviceName());
                        p.put(DefaultPropertiesDescriptor.RUN_USE_SECURITY_DOMAIN, Boolean.FALSE.toString());
                    } else return;
                }
                try {
                    ActionUtils.runTarget(findBuildXml(), targetNames, p);
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
        return ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                String allCfg = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(DefaultPropertiesDescriptor.ALL_CONFIGURATIONS);
                if (allCfg == null) return false;
                //Just default configuration
                if (allCfg.trim().length() == 0) return true;
                //Ok we have more configs, so which one do we want to build
                EditableProperties priv = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                String selectedCfg = priv.getProperty(DefaultPropertiesDescriptor.SELECTED_CONFIGURATIONS);
                CfgSelectionPanel panel = new CfgSelectionPanel(allCfg, selectedCfg);
                if (DialogDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(new DialogDescriptor(panel, NbBundle.getMessage(CfgSelectionPanel.class, "Title_CfgSelection_" + command), true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(CfgSelectionPanel.class), null)))) { //NOI18N
                    String newSel = panel.getSelectedConfigurations();
                    if (selectedCfg != null && selectedCfg.equals(newSel)) return true;
                    priv.put(DefaultPropertiesDescriptor.SELECTED_CONFIGURATIONS, newSel);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, priv);
                    try {
                        ProjectManager.getDefault().saveProject(project);
                        return true;
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                    }                    
                }
                return false;
            }
        }).booleanValue();
    }
    
    protected String evaluateProperty(final EditableProperties ep, final String propertyName, final String configuration) {
        if (configuration == null)
            return ep.getProperty(propertyName);
        final String value = ep.getProperty(J2MEProjectProperties.CONFIG_PREFIX + configuration + "." + propertyName); // NOI18N
        return value != null ? value : evaluateProperty(ep, propertyName, null);
    }
    
    protected String getJadURL() {
        final FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        final FileObject fo = fs.findResource("HTTPServer_DUMMY"); // NOI18N
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
            @SuppressWarnings("unused") final Lookup context ) {
        return true;
    }
}
