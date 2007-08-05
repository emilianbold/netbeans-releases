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

package org.netbeans.modules.j2ee.earproject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action provider of the Enterprise Application project.
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
        EjbProjectConstants.COMMAND_REDEPLOY,
        COMMAND_VERIFY,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME
    };
    
    EarProject project;
    
    // Ant project helper of the project
    private final UpdateHelper updateHelper;
        
    /** Map from commands to ant targets */
    Map<String,String[]> commands;
    
    public EarActionProvider(EarProject project, UpdateHelper updateHelper) {
        commands = new HashMap<String, String[]>();
        commands.put(COMMAND_BUILD, new String[] {"dist"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[] {"clean", "dist"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(EjbProjectConstants.COMMAND_REDEPLOY, new String[] {"run-deploy"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(COMMAND_COMPILE, new String[] {"compile"}); // NOI18N
        commands.put(COMMAND_VERIFY, new String[] {"verify"}); // NOI18N
        
        this.updateHelper = updateHelper;
        this.project = project;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(project.getBuildXmlName ());
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    public void invokeAction( final String command, final Lookup context ) throws IllegalArgumentException {
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
        
        Runnable action = new Runnable () {
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
                    ActionUtils.runTarget(findBuildXml(), targetNames, p);
                }
                catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        };
        
        action.run();
    }

    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        String[] targetNames = commands.get(command);
        
        //EXECUTION PART
        if (command.equals (COMMAND_RUN) || command.equals (EjbProjectConstants.COMMAND_REDEPLOY)) { //  || command.equals (COMMAND_DEBUG)) {
            if (!isSelectedServer ()) {
                // no selected server => warning
                String msg = NbBundle.getMessage(
                        EarActionProvider.class, "MSG_No_Server_Selected"); //  NOI18N
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
                return null;
            }
            if (isDebugged()) {
                p.setProperty("is.debugged", "true"); // NOI18N
            }
            if (command.equals (EjbProjectConstants.COMMAND_REDEPLOY)) {
                p.setProperty("forceRedeploy", "true"); //NOI18N
            } else {
                p.setProperty("forceRedeploy", "false"); //NOI18N
            }
        //DEBUGGING PART
        } else if (command.equals (COMMAND_DEBUG)) {
            if (!isSelectedServer ()) {
                // no selected server => warning
                String msg = NbBundle.getMessage(
                        EarActionProvider.class, "MSG_No_Server_Selected"); //  NOI18N
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE));
                return null;
            }
            
            //see issue 83056
            if (project.evaluator().getProperty("app.client") != null) { //MOI18N
                NotifyDescriptor nd;
                nd = new NotifyDescriptor.Message(NbBundle.getMessage(EarActionProvider.class, "MSG_Server_State_Question"), NotifyDescriptor.QUESTION_MESSAGE);
                nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
                nd.setOptions(new Object[] {NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION});
                if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                    nd = new NotifyDescriptor.Message(NbBundle.getMessage(EarActionProvider.class, "MSG_Server_State"), NotifyDescriptor.INFORMATION_MESSAGE);
                    Object o = DialogDisplayer.getDefault().notify(nd);
                    return null;
                }
            }
            
            if (isDebugged()) {
                p.setProperty("is.debugged", "true"); // NOI18N
            }

            SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);
            if (null != spp) {
                StringBuilder edbd = new StringBuilder();
                final Set s = spp.getSubprojects();
                Iterator iter = s.iterator();
                while (iter.hasNext()) {
                    Project proj = (Project) iter.next();
                    WebModuleProvider wmp = proj.getLookup().lookup(WebModuleProvider.class);
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
                p.setProperty("ear.docbase.dirs", edbd.toString()); // NOI18N
            }
        //COMPILATION PART
        } else {
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }

        return targetNames;
    }
        
    public boolean isActionEnabled( String command, Lookup context ) {
        if ( findBuildXml() == null ) {
            return false;
        }
        if ( command.equals( COMMAND_VERIFY ) ) {
            J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
            return provider != null && provider.hasVerifierSupport();
        } else if (command.equals(COMMAND_RUN)) {
            //see issue #92895
            //XXX - replace this method with a call to API as soon as issue 109895 will be fixed
            boolean isAppClientSelected = project.evaluator().getProperty("app.client") != null; //NOI18N
            return isSelectedServer() && !(isAppClientSelected && isTargetServerRemote());
        }
        // other actions are global
        return true;
    }
    
    private boolean isDebugged() {
        
        J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
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
                        if (attCookie.getHostName() != null
                                && attCookie.getHostName().equalsIgnoreCase(sdi.getHost())
                                && attCookie.getPortNumber() == sdi.getPort()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private boolean isSelectedServer () {
        // XXX determine what to do with the ejb jar project properties
        String instance = updateHelper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_SERVER_INSTANCE);
        if (instance != null) {
            String id = Deployment.getDefault().getServerID(instance);
            if (id != null) {
                return true;
            }
        }
        
        // if there is some server instance of the type which was used
        // previously do not ask and use it
        String serverType = updateHelper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty (EarProjectProperties.J2EE_SERVER_TYPE);
        if (serverType != null) {
            String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
            if (servInstIDs.length > 0) {
                EarProjectProperties.setServerInstance(project, updateHelper, servInstIDs[0]);
                return true;
            }
        }
        return false;
    }

    private boolean isTargetServerRemote() {
        J2eeModuleProvider module = project.getLookup().lookup(J2eeModuleProvider.class);
        InstanceProperties props = module.getInstanceProperties();
        String domain = props.getProperty("DOMAIN"); //NOI18N
        String location = props.getProperty("LOCATION"); //NOI18N
        return "".equals(domain) && "".equals(location); //NOI18N
    }
}
