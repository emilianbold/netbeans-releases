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
package org.netbeans.modules.php.project.ui.actions;


import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.spi.XDebugStarter;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public class DebugSingleCommand extends DebugCommand {
    public static final String ID = ActionProvider.COMMAND_DEBUG_SINGLE;
    public static String DISPLAY_NAME = DebugCommand.DISPLAY_NAME;
    private final DebugLocalCommand debugLocalCommand;

    public DebugSingleCommand(PhpProject project) {
        super(project);
        debugLocalCommand = new DebugLocalCommand(project);        
    }

    @Override
    public void invokeAction(final Lookup context) throws IllegalArgumentException {
        if (!isRunConfigurationSet()) {
            // property not set yet
            return;
        }
        if (isScriptSelected()) {
            debugLocalCommand.invokeAction(context);
        } else {
            // need to fetch these vars _before_ focus changes (can happen in eventuallyUploadFiles() method)
            final FileObject startFile = fileForContext(context);
            final URL[] url = new URL[1];
            try {
                url[0] = getURLForDebug(context, true);
            } catch (MalformedURLException ex) {
                //TODO improve error handling
                Exceptions.printStackTrace(ex);
            }

            eventuallyUploadFiles(CommandUtils.filesForSelectedNodes());
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        showURLForDebug(url[0]);
                    } catch (MalformedURLException ex) {
                        //TODO improve error handling
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            
            boolean jsDebuggingAvailable = WebClientToolsSessionStarterService.isAvailable();
            if (jsDebuggingAvailable) {
                boolean keepDebugging = WebClientToolsProjectUtils.showDebugDialog(getProject());
                if (!keepDebugging) {
                    return;
                }
            }
            
            if (!jsDebuggingAvailable || WebClientToolsProjectUtils.getServerDebugProperty(getProject())) {
                XDebugStarter dbgStarter = XDebugStarterFactory.getInstance();
                if (dbgStarter != null) {
                    if (dbgStarter.isAlreadyRunning()) {
                        String message = NbBundle.getMessage(DebugSingleCommand.class, "MSG_NoMoreDebugSession");
                        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message,
                                NotifyDescriptor.OK_CANCEL_OPTION); //NOI18N
                        boolean confirmed = DialogDisplayer.getDefault().notify(descriptor).equals(NotifyDescriptor.OK_OPTION);
                        if (confirmed) {
                            dbgStarter.stop();
                            invokeAction(context);
                        }
                    } else {
                        dbgStarter.start(getProject(), runnable, startFile, isScriptSelected());
                    }
                }
            } else {
                runnable.run();
            }
        }
    }

    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
        FileObject file = fileForContext(context);
        boolean enabled = file != null;
        if (isScriptSelected()) {
            enabled = isPhpFileSelected(file);
        }
        return enabled && XDebugStarterFactory.getInstance() != null;
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
}
