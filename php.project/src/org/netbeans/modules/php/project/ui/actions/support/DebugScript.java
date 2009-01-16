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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions.support;

import java.io.File;
import java.util.concurrent.Callable;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.spi.XDebugStarter;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProgram;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
/**
 * @author Radek Matous, Tomas Mysik
 */
public class DebugScript  extends RunScript {

    public DebugScript(PhpProject project) {
        super(project);
    }

    public DebugScript(PhpProject project, PhpProgram program, ExecutionDescriptor descriptor, ExternalProcessBuilder processBuilder, FileObject sourceRoot) {
        super(project, program, descriptor, processBuilder, sourceRoot);
    }

    @Override
    public void run(final Lookup context) {
        //temporary; after narrowing deps. will be changed
        Callable<Cancellable> callable = getCallable(context);
        XDebugStarter dbgStarter =  XDebugStarterFactory.getInstance();
        assert dbgStarter != null;
        if (dbgStarter.isAlreadyRunning()) {
            if (CommandUtils.warnNoMoreDebugSession()) {
                dbgStarter.stop();
                run(context);
            }
        } else {
            dbgStarter.start(project, callable, getStartFile(context), true);
        }
    }

    @Override
    protected boolean isControllable() {
        return false;
    }

    @Override
    protected String getOutputTabTitle(String command, File scriptFile) {
        return String.format("%s %s", super.getOutputTabTitle(command, scriptFile), NbBundle.getMessage(DebugScript.class, "MSG_Suffix_Debug"));
    }

    @Override
    protected ExternalProcessBuilder getProcessBuilder(PhpProgram program, File scriptFile) {
        return super.getProcessBuilder(program, scriptFile)
                .addEnvironmentVariable("XDEBUG_CONFIG", "idekey=" + PhpOptions.getInstance().getDebuggerSessionId()); // NOI18N
    }
}
