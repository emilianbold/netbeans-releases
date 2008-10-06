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
package org.netbeans.modules.php.project.ui.actions;

import java.io.File;
import java.util.concurrent.Callable;
import org.netbeans.modules.extexecution.api.ExternalProcessBuilder;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.spi.XDebugStarter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
/**
 * @author Radek Matous
 */
public class DebugLocalCommand  extends RunLocalCommand {
    public static final String ID = "debug.local"; //NOI18N

    public DebugLocalCommand(PhpProject project) {
        super(project);
    }

    @Override
    public void invokeAction(final Lookup context) throws IllegalArgumentException {
        //temporary; after narrowing deps. will be changed
        Callable<Cancellable> callable = getCallable(context);
        XDebugStarter dbgStarter =  XDebugStarterFactory.getInstance();
        if (dbgStarter != null) {
            if (dbgStarter.isAlreadyRunning()) {
                String message = NbBundle.getMessage(DebugLocalCommand.class, "MSG_NoMoreDebugSession");
                NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message,
                        NotifyDescriptor.OK_CANCEL_OPTION); //NOI18N
                boolean confirmed = DialogDisplayer.getDefault().notify(descriptor).equals(NotifyDescriptor.OK_OPTION);
                if (confirmed) {
                    dbgStarter.stop();
                    invokeAction(context);
                } 
            } else {
                dbgStarter.start(getProject(), callable,
                        (context == null) ? fileForProject(false) : fileForContext(context), isScriptSelected());
            }
        }
    }

    protected boolean isControllable() {
        return false;
    }

    @Override
    protected String getOutputTabTitle(String command, File scriptFile) {
        return super.getOutputTabTitle(command, scriptFile) + " "+
                NbBundle.getMessage(DebugLocalCommand.class, "MSG_Suffix_Debug");//NOI18N
    }


    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
        return ((context == null) ? fileForProject(false) : fileForContext(context)) != null && XDebugStarterFactory.getInstance() != null;
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(RunCommand.class, "LBL_DebugLocalCommand");
    }

    @Override
    protected ExternalProcessBuilder initProcessBuilder(ExternalProcessBuilder processBuilder) {
        ExternalProcessBuilder ret = super.initProcessBuilder(processBuilder);
        return ret.addEnvironmentVariable("XDEBUG_CONFIG", "idekey=" + PhpSourcePath.DEBUG_SESSION); //NOI18N
    }
}
