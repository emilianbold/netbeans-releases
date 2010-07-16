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
package org.netbeans.modules.etl.project;

import java.awt.Dialog;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.axiondb.AxionException;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.*;

import org.netbeans.modules.compapp.projects.base.ui.NoSelectedServerWarning;
import org.netbeans.modules.compapp.projects.base.IcanproConstants;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;

/** Action provider of the Web project. This is the place where to do
 * strange things to Web actions. E.g. compile-single.
 */
class EtlproActionProvider implements ActionProvider {

    // Definition of commands

    // Commands available from Web project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        EtlproProject.COMMAND_GENWSDL,
        EtlproProject.COMMAND_SCHEMA,
        EtlproProject.COMMAND_BULK_LOADER,
        EtlproProject.COMMAND_LOADER_ZIP,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };
    // Project
    EtlproProject project;
    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;

    public EtlproActionProvider(EtlproProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        commands = new HashMap();
        commands.put(COMMAND_BUILD, new String[]{"dist"}); // NOI18N

        commands.put(COMMAND_CLEAN, new String[]{"clean"}); // NOI18N

        commands.put(COMMAND_REBUILD, new String[]{"clean", "dist"}); // NOI18N

        commands.put(EtlproProject.COMMAND_GENWSDL, new String[]{"gen-wsdl"}); // NOI18N

        commands.put(EtlproProject.COMMAND_SCHEMA, new String[]{"gen-schema"}); // NOI18N

        commands.put(EtlproProject.COMMAND_BULK_LOADER, new String[]{"etl_bulkloader"}); // NOI18N
        commands.put(EtlproProject.COMMAND_LOADER_ZIP, new String[]{"etl_loaderzip"});
        //commands.put(IcanproConstants.COMMAND_REDEPLOY, new String[] {"run"}); // NOI18N
        //commands.put(IcanproConstants.COMMAND_DEPLOY, new String[] {"run"}); // NOI18N


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
            List<DatabaseConnection> conn = DBExplorerUtil.getDatabasesForCurrentProject();
            Iterator<DatabaseConnection> it = conn.iterator();
            while (it.hasNext()) {
                try {
                    DatabaseConnection dconn = it.next();
                    if (dconn.getDriverClass().contains("AxionDriver")) {
                        DBExplorerUtil.getAxionDBFromURL(dconn.getDatabaseURL()).shutdown();
                    }
                } catch (AxionException ex) {
                    //ignore
                }
            }
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return;
        }
        if (COMMAND_DELETE.equals(command)) {
            List<DatabaseConnection> conn = DBExplorerUtil.getDatabasesForCurrentProject();
            Iterator<DatabaseConnection> it = conn.iterator();
            while (it.hasNext()) {
                try {
                    DatabaseConnection dconn = it.next();
                    if (dconn.getDriverClass().contains("AxionDriver")) {
                        DBExplorerUtil.getAxionDBFromURL(dconn.getDatabaseURL()).shutdown();
                    }
                } catch (AxionException ex) {
                    //ignore
                }
            }
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return;
        }
        //EXECUTION PART
        if (command.equals(IcanproConstants.COMMAND_DEPLOY) || command.equals(IcanproConstants.COMMAND_REDEPLOY)) {
            if (!isSelectedServer()) {
                return;
            }
        } /*else {
        p = null;
        if (targetNames == null) {
        throw new IllegalArgumentException(command);
        }
        }*/

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

    private boolean isSelectedServer() {
        String instance = antProjectHelper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.J2EE_SERVER_INSTANCE);
        boolean selected;
        if (instance != null) {
            selected = true;
        } else {
            // no selected server => warning
            String server = antProjectHelper.getStandardPropertyEvaluator().getProperty(IcanproProjectProperties.J2EE_SERVER_TYPE);
            NoSelectedServerWarning panel = new NoSelectedServerWarning(server);

            Object[] options = new Object[]{
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.CANCEL_OPTION
            };
            DialogDescriptor desc = new DialogDescriptor(panel,
                    NbBundle.getMessage(NoSelectedServerWarning.class, "CTL_NoSelectedServerWarning_Title"), // NOI18N
                    true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
            Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.getAccessibleContext().setAccessibleDescription("This dialog displays warning if no server is selected");
            dlg.setVisible(true);
            if (desc.getValue() != options[0]) {
                selected = false;
            } else {
                instance = panel.getSelectedInstance();
                selected = instance != null;
                if (selected) {
                    IcanproProjectProperties wpp = new IcanproProjectProperties(project, antProjectHelper, refHelper);
                    wpp.put(IcanproProjectProperties.J2EE_SERVER_INSTANCE, instance);
                    wpp.store();
                }
            }
            dlg.dispose();
        }
        return selected;
    }
}
