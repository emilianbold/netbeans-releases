/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.autoupdate.ui.api;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.modules.autoupdate.ui.actions.BalloonManager;
import org.netbeans.modules.autoupdate.ui.actions.FlashingIcon;
import org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizard;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizardModel;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jirka Rechtacek
 */
public final class PluginManager {
    private static PluginManagerAction p = null;
    private static Runnable toRun = null;
    public static void show () {
        if (p == null) {
            p = new PluginManagerAction ();
        }
        p.performAction ();
    }

    public static boolean openInstallWizard (OperationContainer<InstallSupport> updateContainer) {
        if (updateContainer == null) {
            throw new IllegalArgumentException ("OperationContainer cannot be null."); // NOI18N
        }
        if (updateContainer.listAll ().isEmpty ()) {
            throw new IllegalArgumentException ("OperationContainer cannot be empty."); // NOI18N
        }
        if (! updateContainer.listInvalid ().isEmpty ()) {
            throw new IllegalArgumentException ("OperationContainer cannot contain invalid elements but " + updateContainer.listInvalid ()); // NOI18N
        }
        OperationInfo<InstallSupport> info = updateContainer.listAll ().get (0);
        OperationType doOperation = info.getUpdateUnit ().getInstalled () == null ? OperationType.INSTALL : OperationType.UPDATE;
        return new InstallUnitWizard ().invokeWizard (new InstallUnitWizardModel (doOperation, updateContainer));
    }

    public static JComponent createStatusLineIcon (Icon img, final Runnable onMouseClick) {
        if (img == null) {
            throw new IllegalArgumentException ("Icon cannot be null."); // NOI18N
        }
        if (onMouseClick == null) {
            throw new IllegalArgumentException ("Runnable onMouseClick cannot be null."); // NOI18N
        }
        toRun = onMouseClick;
        final JComponent icon = new FlashingIcon (img) {
            @Override
            protected void onMouseClick () {
                onMouseClick.run ();
            }

            @Override
            protected void timeout () {}
        };
        return icon;
    }

    public static void setStatusLineIconVisible (final JComponent icon, final JComponent message, boolean visible) {
        if (icon == null) {
            throw new IllegalArgumentException ("Icon cannot be null."); // NOI18N
        }
        if (! (icon instanceof FlashingIcon)) {
            throw new IllegalArgumentException ("Icon must be instanceof FlashingIcon."); // NOI18N
        }
        final FlashingIcon flasher = (FlashingIcon) icon;
        if (visible) {
            flasher.startFlashing ();
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    BalloonManager.show (icon, message, new AbstractAction () {
                        public void actionPerformed (ActionEvent e) {
                            if (toRun != null) {
                                toRun.run ();
                            }
                        }
                    }, 30000);
                }
            });
            flasher.addMouseListener( new MouseAdapter() {
                    RequestProcessor.Task t = null;
                    private RequestProcessor RP = new RequestProcessor ("balloon-manager"); // NOI18N

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        t = RP.post (new Runnable () {
                            public void run () {
                                BalloonManager.show (icon, message, new AbstractAction () {
                                    public void actionPerformed (ActionEvent e) {
                                        if (toRun != null) {
                                            toRun.run ();
                                        }
                                    }
                                }, 30000);
                            }
                        }, ToolTipManager.sharedInstance ().getInitialDelay ());
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if( null != t ) {
                            t.cancel ();
                            t = null;
                            BalloonManager.dismissSlowly (ToolTipManager.sharedInstance ().getDismissDelay ());
                        }
                    }
            });
        } else {
            for (MouseListener l : flasher.getMouseListeners ()) {
                flasher.removeMouseListener (l);
            }
            flasher.disappear ();
            BalloonManager.dismiss ();
        }

    }
}
