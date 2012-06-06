/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.doctrine2.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpProgram.InvalidPhpProgramException;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.doctrine2.ui.options.Doctrine2OptionsPanelController;
import org.netbeans.modules.php.spi.commands.FrameworkCommand;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * Command support for Doctrine2.
 */
public final class Doctrine2CommandSupport extends FrameworkCommandSupport {

    static final Logger LOGGER = Logger.getLogger(Doctrine2CommandSupport.class.getName());


    public Doctrine2CommandSupport(PhpModule phpModule) {
        super(phpModule);
    }

    @Messages("LBL_Doctrine2=Doctrine2")
    @Override
    public String getFrameworkName() {
        return Bundle.LBL_Doctrine2();
    }

    @Override
    public void runCommand(CommandDescriptor commandDescriptor) {
        Callable<Process> callable = createCommand(commandDescriptor.getFrameworkCommand().getCommands(), commandDescriptor.getCommandParams());
        ExecutionDescriptor descriptor = getDescriptor();
        String displayName = getOutputTitle(commandDescriptor);
        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        service.run();
    }

    @Override
    protected String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + Doctrine2OptionsPanelController.OPTIONS_SUBPATH; // NOI18N
    }

    @Override
    protected File getPluginsDirectory() {
        return null;
    }

    @Override
    protected ExternalProcessBuilder getProcessBuilder(boolean warnUser) {
        Doctrine2Script script;
        try {
            script = Doctrine2Script.getDefault();
        } catch (InvalidPhpProgramException ex) {
            if (warnUser) {
                UiUtils.invalidScriptProvided(ex.getMessage(), Doctrine2OptionsPanelController.OPTIONS_SUBPATH);
            }
            return null;
        }
        assert script.isValid();

        ExternalProcessBuilder processBuilder = script.getProcessBuilder()
                .workingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()));
        for (String param : script.getParameters()) {
            processBuilder = processBuilder.addArgument(param);
        }
        processBuilder = processBuilder
                .addArgument("--ansi"); // NOI18N
        return processBuilder;
    }

    @Override
    protected List<FrameworkCommand> getFrameworkCommandsInternal() {
        // validate
        if (getProcessBuilder(true) == null) {
            return null;
        }
        InputStream output = redirectScriptOutput(Doctrine2Script.LIST_COMMAND, Doctrine2Script.XML_PARAM);
        if (output == null) {
            return null;
        }
        List<Doctrine2CommandVO> commandsVO = new ArrayList<Doctrine2CommandVO>();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(output));
            Doctrine2CommandsXmlParser.parse(reader, commandsVO);
        } finally {
            try {
                output.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        if (commandsVO.isEmpty()) {
            // ??? try to read them from output
            LOGGER.info("Doctrine2 commands from XML should be parsed");
            return null;
        }
        List<FrameworkCommand> commands = new ArrayList<FrameworkCommand>(commandsVO.size());
        for (Doctrine2CommandVO command : commandsVO) {
            commands.add(new Doctrine2Command(command.getCommand(), command.getDescription(), command.getHelp()));
        }
        return commands;
    }

}
