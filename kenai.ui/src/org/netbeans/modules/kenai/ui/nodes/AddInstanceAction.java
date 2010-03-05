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

package org.netbeans.modules.kenai.ui.nodes;

import java.net.MalformedURLException;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.modules.kenai.api.Kenai;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Becicka
 */
public class AddInstanceAction extends AbstractAction {

    public static final String ADD_BUTTON = org.openide.util.NbBundle.getMessage(AddInstanceAction.class, "CTL_ADD");
    public static final String CANCEL_BUTTON = org.openide.util.NbBundle.getMessage(AddInstanceAction.class, "CTL_Cancel");

    private Kenai kenai;
    private JDialog dialog;

    public AddInstanceAction() {
        super(NbBundle.getMessage(AddInstanceAction.class, "CTL_AddInstance"));
    }

    @Override
    public void actionPerformed(final ActionEvent ae) {
        final KenaiInstanceCustomizer kenaiInstanceCustomizer = new KenaiInstanceCustomizer();
        ActionListener bl = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource().equals(ADD_BUTTON)) {
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
                new Object[] {ADD_BUTTON, CANCEL_BUTTON}, ADD_BUTTON,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                bl
                );
        kenaiInstanceCustomizer.setNotificationsSupport(dd.createNotificationLineSupport());
        kenaiInstanceCustomizer.setDialogDescriptor(dd);

        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }

    public Kenai getLastKenai() {
        return kenai;
    }
}
