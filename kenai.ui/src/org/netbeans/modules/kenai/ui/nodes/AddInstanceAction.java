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

package org.netbeans.modules.kenai.ui.nodes;

import java.beans.PropertyVetoException;
import java.net.MalformedURLException;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.api.Kenai;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Becicka
 */
public class AddInstanceAction extends AbstractAction {

    public static final String ADD_BUTTON = org.openide.util.NbBundle.getMessage(AddInstanceAction.class, "CTL_ADD");
    public static final String CANCEL_BUTTON = org.openide.util.NbBundle.getMessage(AddInstanceAction.class, "CTL_Cancel");

    private Kenai kenai;
    private JDialog dialog;
    private boolean expandNewNode = false;

    public AddInstanceAction() {
        super(NbBundle.getMessage(AddInstanceAction.class, "CTL_AddInstance"));
    }

    public AddInstanceAction(boolean expandNewNode) {
        this();
        this.expandNewNode = expandNewNode;
    }

    @Override
    public void actionPerformed(final ActionEvent ae) {
        final JButton addButton = new JButton(ADD_BUTTON);
        addButton.getAccessibleContext().setAccessibleDescription(ADD_BUTTON);
        final KenaiInstanceCustomizer kenaiInstanceCustomizer = new KenaiInstanceCustomizer(addButton);
        ActionListener bl = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource().equals(addButton)) {
                    kenaiInstanceCustomizer.startProgress();
                    RequestProcessor.getDefault().post(new Runnable() {

                        @Override
                        public void run() {
                            Kenai kenai = null;
                            try {
                                kenai = KenaiManager.getDefault().createKenai(kenaiInstanceCustomizer.getDisplayName(), kenaiInstanceCustomizer.getUrl());
                                kenai.getServices();
                                AddInstanceAction.this.kenai = kenai;
                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        kenaiInstanceCustomizer.stopProgress();
                                        dialog.setVisible(false);
                                        dialog.dispose();
                                        if (ae != null && ae.getSource() instanceof JComboBox) {
                                            ((JComboBox) ae.getSource()).setSelectedItem(AddInstanceAction.this.kenai);
                                        }
                                        if (expandNewNode) {
                                            selectNode(AddInstanceAction.this.kenai.getUrl().toString());
                                        }
                                    }
                                });

                            } catch (KenaiException ex) {
                                if (kenai != null) {
                                    KenaiManager.getDefault().removeKenai(kenai);
                                }
                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        kenaiInstanceCustomizer.showError(NbBundle.getMessage(AddInstanceAction.class, "ERR_KenaiNotValid"));
                                    }
                                });
                            } catch (MalformedURLException ex) {
                                if (kenai != null) {
                                    KenaiManager.getDefault().removeKenai(kenai);
                                }
                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        kenaiInstanceCustomizer.showError(NbBundle.getMessage(AddInstanceAction.class, "ERR_KenaiNotValid"));
                                    }
                                });
                            }
                        }
                    });
                } else {
                    dialog.setVisible(false);
                    dialog.dispose();
                    if (ae != null && ae.getSource() instanceof JComboBox) {
                        JComboBox combo = (JComboBox) ae.getSource();
                        if (combo.getModel().getElementAt(0) instanceof Kenai)
                            combo.setSelectedIndex(0);
                        else
                            combo.setSelectedItem(null);
                    }
                }
            }
        };

        DialogDescriptor dd = new DialogDescriptor(
                kenaiInstanceCustomizer,
                NbBundle.getMessage(AddInstanceAction.class, "CTL_NewKenaiInstance"),
                true,
                new Object[] {addButton, CANCEL_BUTTON}, addButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                bl
                );
        kenaiInstanceCustomizer.setNotificationsSupport(dd.createNotificationLineSupport());
        kenaiInstanceCustomizer.setDialogDescriptor(dd);

        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dd);
        dialog.validate();
        dialog.pack();
        dialog.setVisible(true);
    }

    private static final Logger LOG = Logger.getLogger(AddInstanceAction.class.getName());

    private static void selectNode(final String... path) {
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                TopComponent tab = WindowManager.getDefault().findTopComponent("services"); // NOI18N
                if (tab == null) {
                    // XXX have no way to open it, other than by calling ServicesTabAction
                    LOG.fine("No ServicesTab found");
                    return;
                }
                tab.open();
                tab.requestActive();
                if (!(tab instanceof ExplorerManager.Provider)) {
                    LOG.fine("ServicesTab not an ExplorerManager.Provider");
                    return;
                }
                final ExplorerManager mgr = ((ExplorerManager.Provider) tab).getExplorerManager();
                final Node root = mgr.getRootContext();
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        Node hudson = NodeOp.findChild(root, KenaiRootNode.KENAI_NODE_NAME);
                        if (hudson == null) {
                            LOG.fine("ServicesTab does not contain " + KenaiRootNode.KENAI_NODE_NAME);
                            return;
                        }
                        Node _selected;
                        try {
                            _selected = NodeOp.findPath(hudson, path);
                        } catch (NodeNotFoundException x) {
                            LOG.log(Level.FINE, "Could not find subnode", x);
                            _selected = x.getClosestNode();
                        }
                        final Node selected = _selected;
                        Mutex.EVENT.readAccess(new Runnable() {
                            public void run() {
                                try {
                                    mgr.setSelectedNodes(new Node[] {selected});
                                } catch (PropertyVetoException x) {
                                    LOG.log(Level.FINE, "Could not select path", x);
                                }
                            }
                        });
                    }
                });
            }
        });
    }


    public Kenai getLastKenai() {
        return kenai;
    }
}
