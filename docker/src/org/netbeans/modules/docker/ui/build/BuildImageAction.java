/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.build;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.docker.DockerInstance;
import org.netbeans.modules.docker.remote.DockerException;
import org.netbeans.modules.docker.remote.DockerRemote;
import org.netbeans.modules.docker.ui.commit.CommitContainerAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class BuildImageAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(CommitContainerAction.class.getName());

    @NbBundle.Messages({
        "LBL_Build=&Build",
        "LBL_BuildImage=Build Image"
    })
    @Override
    protected void performAction(Node[] activatedNodes) {
        DockerInstance instance = activatedNodes[0].getLookup().lookup(DockerInstance.class);
        if (instance != null) {
            JButton buildButton = new JButton();
            Mnemonics.setLocalizedText(buildButton, Bundle.LBL_Build());
            BuildPanel panel = new BuildPanel();

            DialogDescriptor descriptor
                    = new DialogDescriptor(panel, Bundle.LBL_BuildImage(),
                            true, new Object[] {buildButton, DialogDescriptor.CANCEL_OPTION}, buildButton,
                            DialogDescriptor.DEFAULT_ALIGN, null, null);
            descriptor.setClosingOptions(new Object[] {buildButton, DialogDescriptor.CANCEL_OPTION});
            Dialog dlg = null;

            try {
                dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                dlg.setVisible(true);

                if (descriptor.getValue() == buildButton) {
                    perform(instance, panel.getBuildContext(), panel.getDockerfile());
                }
            } finally {
                if (dlg != null) {
                    dlg.dispose();
                }
            }
        }
    }

    @NbBundle.Messages({
        "# {0} - context",
        "MSG_Building=Building {0}"
    })
    private void perform(final DockerInstance instance, final String buildContext, final String dockerfile) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                final InputOutput io = IOProvider.getDefault().getIO(Bundle.MSG_Building(buildContext), false);
                ProgressHandle handle = ProgressHandleFactory.createHandle(Bundle.MSG_Building(buildContext), new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        io.select();
                    }
                });
                handle.start();
                try {
                    io.getOut().reset();
                    io.select();
                    DockerRemote facade = new DockerRemote(instance);
                    facade.build(new File(buildContext), dockerfile != null ? new File(dockerfile) : null,
                            new BuildOutputListener(io));
                } catch (DockerException ex) {
                    io.getErr().println(ex.getMessage());
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                } finally {
                    io.getOut().close();
                    handle.finish();
                }
            }
        });
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        return activatedNodes[0].getLookup().lookup(DockerInstance.class) != null;
    }

    @NbBundle.Messages("LBL_BuildImageAction=Build...")
    @Override
    public String getName() {
        return Bundle.LBL_BuildImageAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

}
