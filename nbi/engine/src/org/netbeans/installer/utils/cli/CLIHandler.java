/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.netbeans.installer.utils.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.cli.commands.*;
import org.netbeans.installer.utils.exceptions.CLIArgumentException;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 *
 * @author Dmitry Lipin
 */
public class CLIHandler {
    public static final String COMMANDS_LIST =
            "data/commands.list";//NOI18N
    private CLIArgumentsList args;

    public CLIHandler(String[] arguments) {
        args = new CLIArgumentsList(arguments);
    }

    public void proceed() {
        if (args.hasNext()) {
            LogManager.log("... parsing arguments : " + args.toString()); // NOI18N            
            List<CLICommand> list = getCommands();

            while (args.hasNext()) {
                final String currentArg = args.next();
                for (CLICommand command : list) {
                    if (command.canExecute(currentArg)) {
                        try {
                            command.init();
                            command.validateOptions(args);
                            command.runCommand(args);
                        } catch (CLIArgumentException e) {
                            ErrorManager.notifyWarning(e.getMessage());
                        } finally {
                            command.finish();
                            break;
                        }
                    }
                }
            }
        } else {
            LogManager.log("... no command line arguments were specified"); // NOI18N
        }
    }

    private List<CLICommand> getCommands() {
        List<CLICommand> list = new ArrayList<CLICommand>();
        loadDefaultCommands(list);
        loadAdditionalCommands(list);
        return list;
    }

    private void loadDefaultCommands(List<CLICommand> list) {
        list.add(new BundlePropertiesCommand());
        list.add(new CreateBundleCommand());
        list.add(new ForceInstallCommand());
        list.add(new ForceUninstallCommand());
        list.add(new IgnoreLockCommand());
        list.add(new LocaleCommand());
        list.add(new LookAndFeelCommand());
        list.add(new NoSpaceCheckCommand());
        list.add(new PlatformCommand());
        list.add(new PropertiesCommand());
        list.add(new RecordCommand());
        list.add(new RegistryCommand());
        list.add(new SilentCommand());
        list.add(new StateCommand());
        list.add(new SuggestInstallCommand());
        list.add(new SuggestUninstallCommand());
        list.add(new TargetCommand());
        list.add(new UserdirCommand());
    }

    private void loadAdditionalCommands(List<CLICommand> list) {
        InputStream is = ResourceUtils.getResource(COMMANDS_LIST);
        if (is != null) {
            LogManager.log(ErrorLevel.MESSAGE, "... loading additional CLI command classes, if necessary");
            try {
                final String str = StringUtils.readStream(is);
                final String[] lines = StringUtils.splitByLines(str);
                for (String classname : lines) {
                    if (classname.trim().length() > 0) {
                        if(classname.trim().startsWith("#")) {//NOI18N
                            LogManager.log(ErrorLevel.DEBUG, "... skipping line : " + classname);
                            continue;
                        }
                        try {
                            Class cl = Class.forName(classname);
                            Object obj = cl.newInstance();
                            if (obj instanceof CLICommand) {
                                LogManager.log(ErrorLevel.MESSAGE, "... adding CLI class : " + obj.getClass().getName());
                                list.add((CLICommand) obj);
                            } else {
                                LogManager.log(ErrorLevel.WARNING, "... the requested class is not instance of CLICommand:");
                                LogManager.log(ErrorLevel.WARNING, "...... classname  : " + classname);
                                LogManager.log(ErrorLevel.WARNING, "...... CLICommand : " + CLICommand.class.getName());
                            }
                        } catch (ClassNotFoundException e) {
                            LogManager.log(ErrorLevel.WARNING, e);
                        } catch (IllegalAccessException e) {
                            LogManager.log(ErrorLevel.WARNING, e);
                        } catch (InstantiationException e) {
                            LogManager.log(ErrorLevel.WARNING, e);
                        } catch (NoClassDefFoundError e) {
                            LogManager.log(ErrorLevel.WARNING, e);
                        } catch (UnsupportedClassVersionError e) {
                            LogManager.log(ErrorLevel.WARNING, e);
                        }
                    }
                }
            } catch (IOException e) {
                LogManager.log(ErrorLevel.WARNING, e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    LogManager.log(ErrorLevel.DEBUG, e);
                }
            }
        }
    }    
}
