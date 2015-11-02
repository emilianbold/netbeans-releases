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
package org.netbeans.modules.docker.ui.pull;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.docker.DockerInstance;
import org.netbeans.modules.docker.remote.DockerException;
import org.netbeans.modules.docker.remote.DockerRemote;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
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
public class PullImageAction extends NodeAction {

    @NbBundle.Messages({
        "LBL_Pull=&Pull",
        "LBL_SearchImage=Search Image"
    })
    @Override
    protected void performAction(Node[] activatedNodes) {
        DockerInstance instance = activatedNodes[0].getLookup().lookup(DockerInstance.class);
        if (instance != null) {
            JButton pullButton = new JButton();
            Mnemonics.setLocalizedText(pullButton, Bundle.LBL_Pull());
            DockerHubSearchPanel panel = new DockerHubSearchPanel(instance, pullButton);

            DialogDescriptor descriptor
                    = new DialogDescriptor(panel, Bundle.LBL_SearchImage(),
                            true, new Object[] {pullButton, DialogDescriptor.CANCEL_OPTION}, pullButton,
                            DialogDescriptor.DEFAULT_ALIGN, null, null);
            descriptor.setClosingOptions(new Object[] {pullButton, DialogDescriptor.CANCEL_OPTION});
            Dialog dlg = null;

            try {
                dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                dlg.setVisible(true);

                if (descriptor.getValue() == pullButton) {
                    perform(instance, panel.getImageName());
                }
            } finally {
                if (dlg != null) {
                    dlg.dispose();
                }
            }
        }
    }

    private void perform(final DockerInstance instance, final String imageName) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                String toPull = imageName;

                final InputOutput io = IOProvider.getDefault().getIO("Pulling " + toPull, false);
                ProgressHandle handle = ProgressHandleFactory.createHandle("Pulling " + toPull, new AbstractAction() {
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
                    facade.pull(toPull, new PullOutputListener(io), null);
                } catch (DockerException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
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

    @NbBundle.Messages("LBL_PullImageAction=Pull...")
    @Override
    public String getName() {
        return Bundle.LBL_PullImageAction();
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
