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

package org.netbeans.modules.j2ee.earproject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.earproject.ui.NoSelectedServerWarning;
import org.netbeans.modules.j2ee.earproject.ui.customizer.ArchiveProjectProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.api.project.ProjectInformation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.j2ee.common.J2eeProjectConstants;

import org.openide.filesystems.FileUtil;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;


/** Action provider of the Eae project. This is the place where to do
 * strange things to Web actions. E.g. compile-single.
 */
public class EarActionProvider implements ActionProvider {
    
    // Definition of commands
    private static final String COMMAND_COMPILE = "compile"; //NOI18N
    private static final String COMMAND_VERIFY = "verify"; //NOI18N

    // Commands available from J2ee projects
    private static final String[] supportedActions = {
        COMMAND_BUILD, 
        COMMAND_CLEAN, 
        COMMAND_REBUILD, 
        COMMAND_RUN, 
        COMMAND_DEBUG, 
        J2eeProjectConstants.COMMAND_REDEPLOY,
        COMMAND_VERIFY,
    };
    
    EarProject project;
    
    // Ant project helper of the project
    private UpdateHelper updateHelper;
    private ReferenceHelper refHelper;
    private AntBasedProjectType abpt;
        
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;
    
    public EarActionProvider(
        EarProject project,
        UpdateHelper updateHelper, 
        ReferenceHelper refHelper,
        AntBasedProjectType abpt) {
            
        this.abpt = abpt;
        
        commands = new HashMap();
            commands.put(COMMAND_BUILD, new String[] {"dist"}); // NOI18N
            commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
            commands.put(COMMAND_REBUILD, new String[] {"clean", "dist"}); // NOI18N
            commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
            commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
            commands.put(J2eeProjectConstants.COMMAND_REDEPLOY, new String[] {"run-deploy"}); // NOI18N
            commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
            commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug"}); // NOI18N
            commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
            commands.put(COMMAND_COMPILE, new String[] {"compile"}); // NOI18N
            commands.put(COMMAND_VERIFY, new String[] {"verify"}); // NOI18N
        
        this.updateHelper = updateHelper;
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
        if (command.equals (COMMAND_RUN) || command.equals (J2eeProjectConstants.COMMAND_REDEPLOY)) { //  || command.equals (COMMAND_DEBUG)) {
            if (!isSelectedServer ()) {
                return;
            }
            if (isDebugged()) {
                NotifyDescriptor nd;
                String text;
                ProjectInformation pi = (ProjectInformation)project.getLookup().lookup(ProjectInformation.class);
                text = pi.getDisplayName();
                nd = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(EarActionProvider.class, "MSG_SessionRunning", text),
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
                    NbBundle.getMessage(EarActionProvider.class, "MSG_FinishSession"),
                    NotifyDescriptor.OK_CANCEL_OPTION);
                Object o = DialogDisplayer.getDefault().notify(nd);
                if (o.equals(NotifyDescriptor.OK_OPTION)) {            
                    DebuggerManager.getDebuggerManager().getCurrentSession().kill();
                } else {
                    return;
                }
            }

            p = new Properties();
            
            SubprojectProvider spp = (SubprojectProvider) project.getLookup().lookup(SubprojectProvider.class);
            if (null != spp) {
                StringBuffer edbd = new StringBuffer();
                final java.util.Set s = spp.getSubprojects();
                java.util.Iterator iter = s.iterator();
                while (iter.hasNext()) {
                    Project proj = (Project) iter.next();
                    WebModuleProvider wmp = (WebModuleProvider) proj.getLookup().lookup(WebModuleProvider.class);
                    if (null != wmp) {
                        WebModule wm = wmp.findWebModule(proj.getProjectDirectory());
                        if (null != wm) {
                            FileObject fo = wm.getDocumentBase();
			    if (null != fo) {
                                edbd.append(FileUtil.toFile(fo).getAbsolutePath()+":"); //NOI18N
			    }
                        }
                    }
                }
                p.setProperty("ear.docbase.dirs", edbd.toString());
            }
        //COMPILATION PART
        } else if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            FileObject[] files = findJavaSources( context );
            p = new Properties();
            if (files != null) {
                p.setProperty("javac.includes", ActionUtils.antIncludesList(files, project.getSourceDirectory())); // NOI18N
            }
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
        
    public boolean isActionEnabled( String command, Lookup context ) {
        
        if ( findBuildXml() == null ) {
            return false;
        }
        if ( command.equals( COMMAND_VERIFY ) ) {
            J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
            return provider != null && provider.hasVerifierSupport();
        }
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            return true; // findJavaSources( context ) != null || findJsps (context) != null;
        }
        if ( command.equals( COMMAND_RUN_SINGLE ) ) {
            return false;
        }
        else {
            // other actions are global
            return true;
        }

        
    }
    
    // Private methods -----------------------------------------------------
    
    
    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    
    /** Find selected java sources 
     */
    private FileObject[] findJavaSources(Lookup context) {
        FileObject srcDir = project.getSourceDirectory ();
        FileObject[] files = null;
        if (srcDir != null) {
            files = ActionUtils.findSelectedFiles(context, srcDir, ".java", true);
        }
        return files;
    }
    
    private boolean isDebugged() {
        
        J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        if (null == jmp) {
            // XXX this is a bug that I don't know about fixing yet
            return false;
        }
        ServerDebugInfo sdi = jmp.getServerDebugInfo ();
        if (null == sdi) {
            return false;
        }
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
        // XXX determine what to do with the ejb jar project properties
        String instance = updateHelper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty (ArchiveProjectProperties.J2EE_SERVER_INSTANCE);
        if (instance != null) {
            String id = Deployment.getDefault().getServerID(instance);
            if (id != null) {
                return true;
            }
        }
        
        // if there is some server instance of the type which was used
        // previously do not ask and use it
        String serverType = updateHelper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty (ArchiveProjectProperties.J2EE_SERVER_TYPE);
        if (serverType != null) {
            String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
            if (servInstIDs.length > 0) {
                setServerInstance(servInstIDs[0]);
                return true;
            }
        }
        
        // no selected server => warning
        String msg = NbBundle.getMessage(EarActionProvider.class, "MSG_No_Server_Selected"); //  NOI18N
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
        return false;
    }
    
    private void setServerInstance(String serverInstanceId) {
        ArchiveProjectProperties wpp = new ArchiveProjectProperties (project, updateHelper, refHelper, abpt);
        wpp.put (ArchiveProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceId);
        wpp.store ();
    }
    
}
