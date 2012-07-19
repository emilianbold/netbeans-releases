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
package org.netbeans.modules.php.symfony2.commands;

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
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.phpmodule.PhpProgram.InvalidPhpProgramException;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.symfony2.ui.options.Symfony2OptionsPanelController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * Command support for Symfony2.
 */
public final class Symfony2CommandSupport extends FrameworkCommandSupport {

    static final Logger LOGGER = Logger.getLogger(Symfony2CommandSupport.class.getName());


    public Symfony2CommandSupport(PhpModule phpModule) {
        super(phpModule);
    }

    @Messages("LBL_Symfony2=Symfony2")
    @Override
    public String getFrameworkName() {
        return Bundle.LBL_Symfony2();
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
        return null;
    }

    @Override
    protected File getPluginsDirectory() {
        FileObject vendor = phpModule.getSourceDirectory().getFileObject("vendor"); // NOI18N
        if (vendor != null && vendor.isFolder()) {
            return FileUtil.toFile(vendor);
        }
        return null;
    }

    @Override
    protected ExternalProcessBuilder getProcessBuilder(boolean warnUser) {
        ExternalProcessBuilder processBuilder = super.getProcessBuilder(warnUser);
        if (processBuilder == null) {
            return null;
        }

        Symfony2Script symfony2Script;
        try {
            symfony2Script = Symfony2Script.forPhpModule(phpModule, warnUser);
        } catch (InvalidPhpProgramException ex) {
            if (warnUser) {
                UiUtils.invalidScriptProvided(ex.getMessage(), Symfony2OptionsPanelController.OPTIONS_SUBPATH);
            }
            return null;
        }
        assert symfony2Script.isValid();

        processBuilder = processBuilder
                .workingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()))
                .addArgument(symfony2Script.getProgram());
        for (String param : symfony2Script.getParameters()) {
            processBuilder = processBuilder.addArgument(param);
        }
        processBuilder = processBuilder
                .addArgument("--ansi"); // NOI18N
        return processBuilder;
    }

    @Messages("Symfony2CommandSupport.error.listCommand=Symfony2 list commands")
    @Override
    protected List<FrameworkCommand> getFrameworkCommandsInternal() {
        // validate
        if (getProcessBuilder(true) == null) {
            return null;
        }
        InputStream output = redirectScriptOutput("list", "--xml"); // NOI18N
        if (output == null) {
            // perhaps some error? run it again and print the result in the output window
            PhpProgram.executeLater(createCommand("list", "--xml"), // NOI18N
                    new ExecutionDescriptor().frontWindow(true),
                    Bundle.Symfony2CommandSupport_error_listCommand());
            return null;
        }
        List<Symfony2CommandVO> commandsVO = new ArrayList<Symfony2CommandVO>();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(output));
            Symfony2CommandsXmlParser.parse(reader, commandsVO);
        } finally {
            try {
                output.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        if (commandsVO.isEmpty()) {
            // ??? try to read them from output
            LOGGER.info("Symfony commands from XML should be parsed");
            return null;
        }
        List<FrameworkCommand> commands = new ArrayList<FrameworkCommand>(commandsVO.size());
        for (Symfony2CommandVO command : commandsVO) {
            commands.add(new Symfony2Command(command.getCommand(), command.getDescription(), command.getHelp()));
        }
        return commands;
    }

}
