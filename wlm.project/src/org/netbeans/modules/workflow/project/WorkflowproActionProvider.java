/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.workflow.project;

import org.netbeans.modules.compapp.projects.base.IcanproConstants;
import org.netbeans.modules.compapp.projects.base.ui.NoSelectedServerWarning;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import java.awt.Dialog;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/** Action provider of the Web project. This is the place where to do
 * strange things to Web actions. E.g. compile-single.
 */
class WorkflowproActionProvider implements ActionProvider {
    
    // Definition of commands

    // Commands available from Web project
    private static final String[] supportedActions = {
        COMMAND_BUILD, 
        COMMAND_CLEAN, 
        COMMAND_REBUILD, 
        COMMAND_COMPILE_SINGLE, 
        // PENDING
        //ProjectConstants.POPULATE_CATALOG,
        ProjectConstants.GENERATE_XFORM,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME
    };
    
    // Project
    WorkflowproProject project;
    
    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
        
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;
    
    public WorkflowproActionProvider(WorkflowproProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        commands = new HashMap();
        commands.put(COMMAND_BUILD, new String[] {"dist"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[] {"clean", "dist"}); // NOI18N
        commands.put(ProjectConstants.GENERATE_XFORM, new String[] {"generate-xform"});

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
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }
        if (command.equals(ProjectConstants.POPULATE_CATALOG)) {
            // PENDING
            return;
        }
        
        Properties p = new Properties ();
        p.setProperty("projectName", project.getName());
        String[] targetNames = (String[])commands.get(command);
        //EXECUTION PART
        if (command.equals (IcanproConstants.COMMAND_DEPLOY) || command.equals (IcanproConstants.COMMAND_REDEPLOY)) {
            if (!isSelectedServer ()) {
                return;
            }
        } else {
//            p = null;
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
    
     /**
     * @return array of targets or null to stop execution; can return empty array
     */
    String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        String[] targetNames = (String[])commands.get(command);
        return targetNames;
    }
    
    public boolean isActionEnabled( String command, Lookup context ) {
        
        if ( findBuildXml() == null ) {
            return false;
        }
        return true;
        
    }
    
    // Private methods -----------------------------------------------------
    
    private boolean isDebugged() {
        return false;
    }
    

    private boolean isSelectedServer () {
        String instance = antProjectHelper.getStandardPropertyEvaluator ().getProperty (IcanproProjectProperties.J2EE_SERVER_INSTANCE);
        boolean selected;
        if (instance != null) {
            selected = true;
        } else {
            // no selected server => warning
            String server = antProjectHelper.getStandardPropertyEvaluator ().getProperty (IcanproProjectProperties.J2EE_SERVER_TYPE);
            NoSelectedServerWarning panel = new NoSelectedServerWarning (server);

            Object[] options = new Object[] {
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.CANCEL_OPTION
            };
            DialogDescriptor desc = new DialogDescriptor (panel,
                    NbBundle.getMessage (NoSelectedServerWarning.class, "CTL_NoSelectedServerWarning_Title"), // NOI18N
                true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
            Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
            dlg.setVisible (true);
            if (desc.getValue() != options[0]) {
                selected = false;
            } else {
                instance = panel.getSelectedInstance ();
                selected = instance != null;
                if (selected) {
                    IcanproProjectProperties wpp = new IcanproProjectProperties (project, antProjectHelper, refHelper);
                    wpp.put (IcanproProjectProperties.J2EE_SERVER_INSTANCE, instance);
                    wpp.store ();
                }
            }
            dlg.dispose();            
        }
        return selected;
    }
    
}
