/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.*;
import org.openide.util.Mutex;
import org.netbeans.api.project.ProjectInformation;

import org.netbeans.modules.j2ee.api.common.J2eeProjectConstants;


/** Action provider of the Web project. This is the place where to do
 * strange things to Web actions. E.g. compile-single.
 */
class EjbJarActionProvider implements ActionProvider {
    
    // Definition of commands
    
    private static final String COMMAND_COMPILE = "compile"; //NOI18N
    private static final String COMMAND_VERIFY = "verify"; //NOI18N
        
    // Commands available from Web project
    private static final String[] supportedActions = {
        COMMAND_BUILD, 
        COMMAND_CLEAN, 
        COMMAND_REBUILD, 
        COMMAND_COMPILE_SINGLE, 
        COMMAND_RUN, 
        COMMAND_DEBUG, 
        J2eeProjectConstants.COMMAND_REDEPLOY,
        JavaProjectConstants.COMMAND_JAVADOC, 
        COMMAND_TEST, 
        COMMAND_TEST_SINGLE, 
        COMMAND_DEBUG_TEST_SINGLE, 
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_COMPILE,
        COMMAND_VERIFY,
    };
    
    // Project
    EjbJarProject project;
    
    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
        
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;
    
    public EjbJarActionProvider(EjbJarProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        commands = new HashMap();
        commands.put(COMMAND_BUILD, new String[] {"dist"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[] {"clean", "dist"}); // NOI18N
        commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commands.put(J2eeProjectConstants.COMMAND_REDEPLOY, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
        commands.put(COMMAND_TEST, new String[] {"test"}); // NOI18N
        commands.put(COMMAND_TEST_SINGLE, new String[] {"test-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug-test"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
        commands.put(COMMAND_COMPILE, new String[] {"compile"}); // NOI18N
        commands.put(COMMAND_VERIFY, new String[] {"verify"}); // NOI18N

        this.antProjectHelper = antProjectHelper;
        this.project = project;
        this.refHelper = refHelper;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(project.getBuildXmlName ());
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    public void invokeAction( String command, Lookup context ) throws IllegalArgumentException {
        Properties p;
        String[] targetNames = (String[])commands.get(command);
        //EXECUTION PART
        if (command.equals (COMMAND_RUN) || command.equals (J2eeProjectConstants.COMMAND_REDEPLOY)) {
            if (!isSelectedServer ()) {
                return;
            }
            if (isDebugged()) {
                NotifyDescriptor nd;
                ProjectInformation pi = (ProjectInformation)project.getLookup().lookup(ProjectInformation.class);
                String text = pi.getDisplayName();
                nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(EjbJarActionProvider.class, "MSG_SessionRunning", text),
                            NotifyDescriptor.OK_CANCEL_OPTION);
                Object o = DialogDisplayer.getDefault().notify(nd);
                if (o.equals(NotifyDescriptor.OK_OPTION)) {            
                    DebuggerManager.getDebuggerManager().getCurrentSession().kill();
                } else {
                    return;
                }
            }
            p = new Properties();
            if (command.equals (J2eeProjectConstants.COMMAND_REDEPLOY)) {
                p.setProperty("forceRedeploy", "true"); //NOI18N
            } else {
                p.setProperty("forceRedeploy", "false"); //NOI18N
            }
        //DEBUGGING PART
        } else if (command.equals (COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_SINGLE)) {
            if (!isSelectedServer ()) {
                return;
            }
            if (isDebugged()) {
                NotifyDescriptor nd;
                nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(EjbJarActionProvider.class, "MSG_FinishSession"),
                            NotifyDescriptor.OK_CANCEL_OPTION);
                Object o = DialogDisplayer.getDefault().notify(nd);
                if (o.equals(NotifyDescriptor.OK_OPTION)) {            
                    DebuggerManager.getDebuggerManager().getCurrentSession().kill();
                } else {
                    return;
                }
            }
            p = new Properties();
        //COMPILATION PART
        } else if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] sourceRoots = project.getSourceRoots().getRoots();
            FileObject[] files = findSourcesAndPackages( context, sourceRoots);
            
            p = new Properties();
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, getRoot(sourceRoots,files[0]))); // NOI18N
            } else {
            }
        // TEST PART
        } else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            p = new Properties();
            targetNames = setupTestSingle(p, files);
        } 
        else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            p = new Properties();
            targetNames = setupDebugTestSingle(p, files);
        } else {
            p = null;
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }

        try {
            ActionUtils.runTarget(findBuildXml(), targetNames, p);
        } 
        catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private String[] setupTestSingle(Properties p, FileObject[] files) {
        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        FileObject root = getRoot(testSrcPath, files[0]);
        p.setProperty("test.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        p.setProperty("javac.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        return new String[] {"test-single"}; // NOI18N
    }
    
    private String[] setupDebugTestSingle(Properties p, FileObject[] files) {
        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        FileObject root = getRoot(testSrcPath, files[0]);
        String path = FileUtil.getRelativePath(root, files[0]);
        // Convert foo/FooTest.java -> foo.FooTest
        p.setProperty("test.class", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
        return new String[] {"debug-test"}; // NOI18N
    }
    
    public boolean isActionEnabled( String command, Lookup context ) {
        
        if ( findBuildXml() == null ) {
            return false;
        }
        if ( command.equals( COMMAND_VERIFY ) ) {
            return project.getEjbModule().hasVerifierSupport();
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return true; // findJavaSources( context ) != null || findJsps (context) != null;
        }
        else if ( command.equals( COMMAND_TEST_SINGLE ) ) {
            return findTestSourcesForSources(context) != null;
        }
        else if ( command.equals( COMMAND_DEBUG_TEST_SINGLE ) ) {
            FileObject[] files = findTestSourcesForSources(context);
            return files != null && files.length == 1;
        } else {
            // other actions are global
            return true;
        }

        
    }
    
    // Private methods -----------------------------------------------------
    
    private static final String SUBST = "Test.java"; // NOI18N
    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    
    /** Find selected java sources 
     */
    private FileObject[] findJavaSources(Lookup context) {
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        for (int i=0; i< srcPath.length; i++) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcPath[i], ".java", true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }
    
    private FileObject[] findSourcesAndPackages (Lookup context, FileObject srcDir) {
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, null, true); // NOI18N
            //Check if files are either packages of java files
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isFolder() && !"java".equals(files[i].getExt())) {
                        return null;
                    }
                }
            }
            return files;
        } else {
            return null;
        }
    }
    
    private FileObject[] findSourcesAndPackages (Lookup context, FileObject[] srcRoots) {
        for (int i=0; i<srcRoots.length; i++) {
            FileObject[] result = findSourcesAndPackages(context, srcRoots[i]);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    
    /** Find tests corresponding to selected sources.
     */
    private FileObject[] findTestSourcesForSources(Lookup context) {
        FileObject[] sourceFiles = findSources(context);
        if (sourceFiles == null) {
            return null;
        }
        FileObject[] testSrcPath = project.getTestSourceRoots().getRoots();
        if (testSrcPath.length == 0) {
            return null;
        }
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        FileObject srcDir = getRoot(srcPath, sourceFiles[0]);
        for (int i=0; i<testSrcPath.length; i++) {
            FileObject[] files2 = ActionUtils.regexpMapFiles(sourceFiles, srcDir, SRCDIRJAVA, testSrcPath[i], SUBST, true);
            if (files2 != null) {
                return files2;
            }
        }
        return null;
    }      
    
    private FileObject getRoot (FileObject[] roots, FileObject file) {
        FileObject srcDir = null;
        for (int i=0; i< roots.length; i++) {
            if (FileUtil.isParentOf(roots[i],file)) {
                srcDir = roots[i];
                break;
            }
        }
        return srcDir;
    }

    private boolean isDebugged() {
        
        J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        ServerDebugInfo sdi = jmp.getServerDebugInfo ();
//        server.getServerInstance().getStartServer().getDebugInfo(null);
        Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
        
        for (int i=0; i < sessions.length; i++) {
            Session s = sessions[i];
            if (s != null) {
                Object o = s.lookupFirst(null, AttachingDICookie.class);
                if (o != null) {
                    AttachingDICookie attCookie = (AttachingDICookie)o;
                    if (sdi.getTransport().equals(ServerDebugInfo.TRANSPORT_SHMEM)) {
                        if (attCookie.getSharedMemoryName().equalsIgnoreCase(sdi.getShmemName())) {
                            return true;
                        }
                    } else {
                        if (attCookie.getHostName().equalsIgnoreCase(sdi.getHost())) {
                            if (attCookie.getPortNumber() == sdi.getPort()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean isSelectedServer () {
        String instance = antProjectHelper.getStandardPropertyEvaluator ().getProperty (EjbJarProjectProperties.J2EE_SERVER_INSTANCE);
        if (instance != null) {
            String id = Deployment.getDefault().getServerID(instance);
            if (id != null) {
                return true;
            }
        }
        
        // if there is some server instance of the type which was used
        // previously do not ask and use it
        String serverType = antProjectHelper.getStandardPropertyEvaluator ().getProperty (EjbJarProjectProperties.J2EE_SERVER_TYPE);
        if (serverType != null) {
            String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
            if (servInstIDs.length > 0) {
                setServerInstance(servInstIDs[0]);
                return true;
            }
        }

        // no selected server => warning
        String msg = NbBundle.getMessage(EjbJarActionProvider.class, "MSG_No_Server_Selected"); //  NOI18N
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
        return false;
    }
    
    private void setServerInstance(final String serverInstanceId) {
        EjbJarProjectProperties.setServerInstance(project, antProjectHelper, serverInstanceId);
    }
    
    private FileObject[] findSources(Lookup context) {
        FileObject[] srcPath = project.getSourceRoots().getRoots();
        for (int i=0; i< srcPath.length; i++) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcPath[i], ".java", true); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }
}
