/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.team.ui.common.LinkButton;
import org.netbeans.modules.team.ui.common.QueryListNode;
import org.netbeans.modules.team.ui.util.treelist.LeafNode;
import org.netbeans.modules.team.ui.util.treelist.TreeListNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Node for a open other project
 *
 * @author Jan Becicka
 */
public class OpenNetBeansIDEProjects extends LeafNode {

    private JPanel panel;
    private LinkButton btn;
    private Kenai kenai;

    public OpenNetBeansIDEProjects(Kenai k, TreeListNode parent) {
        super(parent);
        this.kenai = k;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int maxWidth) {
        if (null == panel) {
            panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);
            btn = new LinkButton(NbBundle.getMessage(QueryListNode.class, "LBL_OpenNetBeansIDEProject1"), getDefaultAction()); //NOI18N
            panel.add(btn, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            panel.add(new JLabel(" " + NbBundle.getMessage(QueryListNode.class, "LBL_OpenNetBeansIDEProject2")), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        }
        btn.setForeground(foreground, isSelected);
        return panel;
    }

    @Override
    public Action getDefaultAction() {
        return new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Utilities.getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        ProgressHandle handle = null;
                        try {
                            handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiPopupActionsProvider.class, "CTL_OpenKenaiProjectAction")); //NOI18N
                            handle.start();
                            final KenaiProject kp = kenai.getProject("ide");//NOI!18N
                            final ProjectHandleImpl pHandle = new ProjectHandleImpl(kp);
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    Utilities.addProject(pHandle, false, true);
                                }
                            });
                        } catch (KenaiException e) {
                            String err = e.getLocalizedMessage();
                            if (err == null) {
                                err = e.getCause().getLocalizedMessage();
                            }
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(KenaiPopupActionsProvider.class, "ERROR_CONNECTION", err))); //NOI18N
                        } finally {
                            if (handle != null) {
                                handle.finish();
                                return;
                            }
                        }
                    }
                });
            }
        };
    }
}
