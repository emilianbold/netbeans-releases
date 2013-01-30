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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
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

    public Symfony2CommandSupport(PhpModule phpModule) {
        super(phpModule);
    }

    @Messages("LBL_Symfony2=Symfony2")
    @Override
    public String getFrameworkName() {
        return Bundle.LBL_Symfony2();
    }

    @Override
    public void runCommand(CommandDescriptor commandDescriptor, Runnable postExecution) {
        String[] commands = commandDescriptor.getFrameworkCommand().getCommands();
        String[] commandParams = commandDescriptor.getCommandParams();
        List<String> params = new ArrayList<String>(commands.length + commandParams.length);
        params.addAll(Arrays.asList(commands));
        params.addAll(Arrays.asList(commandParams));
        try {
            Symfony2Script.forPhpModule(phpModule, true).runCommand(phpModule, params, postExecution);
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage(), Symfony2OptionsPanelController.OPTIONS_SUBPATH);
        }
    }

    @Override
    protected String getOptionsPath() {
        return null;
    }

    @Override
    protected File getPluginsDirectory() {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return null;
        }
        FileObject vendor = sourceDirectory.getFileObject("vendor"); // NOI18N
        if (vendor != null && vendor.isFolder()) {
            return FileUtil.toFile(vendor);
        }
        return null;
    }

    @Override
    protected List<FrameworkCommand> getFrameworkCommandsInternal() {
        Symfony2Script symfony2;
        try {
            symfony2 = Symfony2Script.forPhpModule(phpModule, true);
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage(), Symfony2OptionsPanelController.OPTIONS_SUBPATH);
            return null;
        }

        List<Symfony2CommandVO> commandsVO = symfony2.getCommands(phpModule);
        if (commandsVO == null) {
            // some error
            return null;
        }
        List<FrameworkCommand> commands = new ArrayList<FrameworkCommand>(commandsVO.size());
        for (Symfony2CommandVO command : commandsVO) {
            commands.add(new Symfony2Command(command.getCommand(), command.getDescription(), command.getHelp()));
        }
        return commands;
    }

}
