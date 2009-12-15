/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.zend.commands;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpProgram.InvalidPhpProgramException;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.spi.commands.FrameworkCommand;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.zend.ZendScript;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class ZendCommandSupport extends FrameworkCommandSupport {

    public ZendCommandSupport(PhpModule phpModule) {
        super(phpModule);
    }

    @Override
    public String getFrameworkName() {
        return NbBundle.getMessage(ZendCommandSupport.class, "MSG_Zend");
    }

    @Override
    public void runCommand(CommandDescriptor commandDescriptor) {
        Callable<Process> callable = createCommand(commandDescriptor.getFrameworkCommand().getCommand(), commandDescriptor.getCommandParams());
        ExecutionDescriptor descriptor = getDescriptor();
        String displayName = getOutputTitle(commandDescriptor);
        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        service.run();
    }

    @Override
    protected String getOptionsPath() {
        return ZendScript.getOptionsPath();
    }

    @Override
    protected File getPluginsDirectory() {
        return null;
    }

    @Override
    protected ExternalProcessBuilder getProcessBuilder(boolean warnUser) {
        ZendScript zendScript = null;
        try {
            zendScript = ZendScript.getDefault();
        } catch (InvalidPhpProgramException ex) {
            if (warnUser) {
                UiUtils.invalidScriptProvided(
                        ex.getMessage(),
                        ZendScript.getOptionsSubPath());
            }
            return null;
        }
        assert zendScript.isValid();

        ExternalProcessBuilder externalProcessBuilder = zendScript.getProcessBuilder()
                .workingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()));
        for (String param : zendScript.getParameters()) {
            externalProcessBuilder = externalProcessBuilder.addArgument(param);
        }
        return externalProcessBuilder;
    }

    @Override
    protected List<FrameworkCommand> getFrameworkCommandsInternal() {
        return Collections.emptyList();
    }
}
