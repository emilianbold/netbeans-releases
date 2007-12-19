/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.php.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.php.rt.spi.providers.Command;
import org.netbeans.modules.php.rt.spi.providers.CommandProvider;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.utils.PhpCommandUtils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 * @author ads
 *
 */
class PhpActionProvider implements ActionProvider {

    static final String LBL_PUT_FILES = "LBL_PutFilesToServer"; // NOI18N
    static final String LBL_OPEN_CONTEXT = "LBL_OpenContext"; // NOI18N
    static final String LBL_IMPORT_FILE = "LBL_ImportFile"; // NOI18N
    static final String LBL_DEBUG_PROJECT = "LBL_DebugProject"; // NOI18N
    static final String LBL_RUN_LOCAL = "LBL_RunLocal"; // NOI18N

    PhpActionProvider(PhpProject project) {
        myProject = project;

        myCommands = new ArrayList<Command>(2);
        myCommands.add(new ImportCommand(project));
        myCommands.add(new RunLocalCommand(project));

        // store standard comands into separate list.
        // php-specific commands from myCommands List will be shown
        // in PhpLogicalViewProvider.getProjectActions as is.
        // And standard commands should be formatted 
        //in PhpLogicalViewProvider.getStandardProjectActions
        // manually to be shoun in traditional order.
        myStandardCommands = new ArrayList<Command>(4);
        myStandardCommands.add(new CopyCommand(project));
        myStandardCommands.add(new DeleteCommand(project));
        myStandardCommands.add(new MoveCommand(project));
        myStandardCommands.add(new RenameCommand(project));
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.project.ActionProvider#getSupportedActions()
     */
    public String[] getSupportedActions() {
        List<String> list = new LinkedList<String>();
        
        for (String commandId : getSupportedProviderActions()) {
            list.add(commandId);
        }
        
        for (String commandId : getSupportedProjectActions()) {
            list.add(commandId);
        }
        
        return list.toArray(new String[list.size()]);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.spi.project.ActionProvider#invokeAction(java.lang.String, org.openide.util.Lookup)
     */
    public void invokeAction(String command, Lookup lookup) throws IllegalArgumentException {

        if (invokeProviderAction(command)){
            return;
        }

        if (invokeProjectAction(command)){
            return;
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.project.ActionProvider#isActionEnabled(java.lang.String, org.openide.util.Lookup)
     */
    public boolean isActionEnabled(String command, Lookup lookup) 
            throws IllegalArgumentException 
    {
        return true;
    }

    public List<Command> getProjectCommands() {
        return myCommands;
    }

    public List<Command> getStandardProjectCommands() {
        return myStandardCommands;
    }

    /**
     * Finds command with specified id in provider's commands and runs it.
     * @returns true if command was run. false otherwise
     */
    private boolean invokeProviderAction(String command){
        WebServerProvider provider = Utils.getProvider(getProject());
        if (provider != null) {
            CommandProvider commProvider = provider.getCommandProvider();
            
            Command[] commands = commProvider.getCommands(getProject());
            if (doInvokeAction(command, commands)){
                return true;
            }

        }
        return false;
    }
    
    /**
     * Finds command with specified id in project's commands and runs it.
     * @returns true if command was run. false otherwise
     */
    private boolean invokeProjectAction(String command){
        if (doInvokeAction(command, getProjectCommands())) {
            return true;
        }

        if (doInvokeAction(command, getStandardProjectCommands())) {
            return true;
        }
        return false;
    }
    
    /**
     * @see invokeAction(String , Lookup , Command[] )
     */
    private boolean doInvokeAction(String commandToRun, 
            Collection<Command> commands)
    {
        Command[] commandsArray = commands.toArray(new Command[]{});
        return doInvokeAction(commandToRun, commandsArray);
    }
    
    /**
     * runs specified commandToRun from commands array if there is command with the same id.
     * @returns true if command was run. false otherwise
     */
    private boolean doInvokeAction(String commandToRun, Command[] commands)
    {
        for (Command com : commands) {
            String id = com.getId();
            if (commandToRun.equals(id)) {
                PhpCommandRunner.runCommand(com);
                return true;
            }
        }
        return false;
    }

    private List<String> getSupportedProjectActions(){
        List<String> list = new LinkedList<String>();

        for (Command command : getProjectCommands()) {
            if (command != null && command.isEnabled()) {
                list.add(command.getId());
            }
        }
        for (Command command : getStandardProjectCommands()) {
            if (command != null && command.isEnabled()) {
                list.add(command.getId());
            }
        }
        
        return list;
    }
    
    private List<String> getSupportedProviderActions(){
        List<String> list = new LinkedList<String>();
        WebServerProvider provider = Utils.getProvider(getProject());
        if (provider != null) {
                CommandProvider commProvider = provider.getCommandProvider();
                
                Command[] commands = commProvider.getCommands(getProject());
                for (Command command : commands) {
                    if (command != null && command.isEnabled()) {
                        list.add(command.getId());
                    }
                }
            }
        return list;
        
    }
    
    private PhpProject getProject() {
        return myProject;
    }
    
    /** 
     * <p>runs specified command.
     * <p>uses org.openide.util.actions.CallableSystemAction.synchronousByDefault
     * to decide if command should be started in separate thread or not.
     */
    public static class PhpCommandRunner {

        //private static RequestProcessor RP = new RequestProcessor(
        //        "Module-Actions", Integer.MAX_VALUE); // NOI18N

        public static void runCommand(final Command command) {
            
            command.setActionFiles(PhpCommandUtils.getActionFiles());
            
            if (command.asynchronous()) {
                Runnable r2 = new Runnable() {

                    public void run() {
                        command.run();
                    }
                };
                RequestProcessor.getDefault().post(r2);
                //RP.post(r2);
            } else {
                command.run();
            }
        }
    }
    
    private final PhpProject myProject;
    private List<Command> myCommands;
    private List<Command> myStandardCommands;
}