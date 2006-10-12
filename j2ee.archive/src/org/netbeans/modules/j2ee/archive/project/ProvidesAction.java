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


package org.netbeans.modules.j2ee.archive.project;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.util.NbBundle;
import org.apache.tools.ant.module.api.support.ActionUtils;

public class ProvidesAction implements ActionProvider {
    private static final String COMMAND_VERIFY = "verify"; //NOI18N

    private Project project;
    static Map commands;
    static {
        commands = new HashMap();
        commands.put(COMMAND_BUILD, new String[] {"dist"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run-deploy"}); // NOI18N
        commands.put(COMMAND_VERIFY, new String[] {"verify"}); // NOI18N
    }
    
    ProvidesAction(Project project) {
        this.project=project;
    }

    public String[] getSupportedActions() {
        // TODO -- Determine actions to implement
        return new String[] {ActionProvider.COMMAND_BUILD, 
            ActionProvider.COMMAND_RUN, COMMAND_VERIFY,
            ActionProvider.COMMAND_DELETE,
            ActionProvider.COMMAND_COPY,
            ActionProvider.COMMAND_MOVE,
            ActionProvider.COMMAND_RENAME,
        };
    }

    public void invokeAction(final String command, final Lookup context) {
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
        
        
        // TODO -- implement action invokation
        Runnable action = new Runnable () {
            public void run () {
                String[] targetNames;
        
                targetNames = getTargetNames(command, context);
                if (targetNames == null) {
                    return;
                }
                if (targetNames.length == 0) {
                    targetNames = null;
                }
                try {
                    FileObject buildFo = findBuildXml();
                    if (buildFo == null || !buildFo.isValid()) {
                        //The build.xml was deleted after the isActionEnabled was called
  	                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ProvidesAction.class,
                                java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/archive/project/Bundle").getString("LBL_No_Build_XML_Found")), NotifyDescriptor.WARNING_MESSAGE);
  	                DialogDisplayer.getDefault().notify(nd);
                    }
                    else {
                        ActionUtils.runTarget(buildFo, targetNames, null); // p;
  	            }                    
                } 
                catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }            
        };
        
        action.run();
    }

    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        // TODO -- figure out the states for actions
        return ! (findBuildXml() == null);
    }
    
    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    String[] getTargetNames(String command, Lookup context) /*, Properties p) */ throws IllegalArgumentException {
        String[] targetNames = (String[])commands.get(command);
        return targetNames;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
}
