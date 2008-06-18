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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved. 
 */
package org.netbeans.modules.dataView.project;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.*;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;

/** Action provider of the Web project. This is the place where to do
 * strange things to Web actions. E.g. compile-single.
 */
class ShowDataproActionProvider implements ActionProvider {

    // Definition of commands

    // Commands available from Web project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        ShowDataproProject.COMMAND_GENWSDL,
        ShowDataproProject.COMMAND_SCHEMA,
        ShowDataproProject.COMMAND_BULK_LOADER,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };
    // Project
    ShowDataproProject project;
    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;

    public ShowDataproActionProvider(ShowDataproProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        commands = new HashMap();
        commands.put(COMMAND_BUILD, new String[]{"dist"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[]{"clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[]{"clean", "dist"}); // NOI18N

        this.antProjectHelper = antProjectHelper;
        this.project = project;
        this.refHelper = refHelper;
    }

    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(project.getBuildXmlName());
    }

    public String[] getSupportedActions() {
        return supportedActions;
    }

    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        Properties p = null;
        String[] targetNames = (String[]) commands.get(command);

        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return;
        }

        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return;
        }

        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return;
        }
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return;
        }

        try {
            ActionUtils.runTarget(findBuildXml(), targetNames, p);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    public boolean isActionEnabled(String command, Lookup context) {
        return findBuildXml() != null;
    }

    // Private methods -----------------------------------------------------
    private boolean isDebugged() {
        return false;
    }

    String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        String[] targetNames = (String[]) commands.get(command);
        return targetNames;
    }
}
