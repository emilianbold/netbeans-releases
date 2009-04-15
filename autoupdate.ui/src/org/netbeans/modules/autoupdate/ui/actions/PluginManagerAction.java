/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui.actions;

import java.awt.Dialog;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public final class PluginManagerAction extends CallableSystemAction {
    private static PluginManagerUI pluginManagerUI = null;
    private Dialog dlg = null;
    
    public void performAction () {
        if (dlg == null) {
            JButton close = new JButton ();
            close.setDefaultCapable(false);
            Mnemonics.setLocalizedText (close,NbBundle.getMessage (PluginManagerAction.class, "PluginManager_CloseButton_Name"));
            pluginManagerUI = new PluginManagerUI (
                close,
                getValue("InitialTab"), //NOI18N
                !Boolean.FALSE.equals(getValue("AdvancedView")) // NOI18N
            );
            putValue("InitialTab", null); //NOI18N
            putValue("AdvancedView", null); //NOI18N
            DialogDescriptor dd = new DialogDescriptor (
                                        pluginManagerUI,
                                        NbBundle.getMessage (PluginManagerAction.class, "PluginManager_Panel_Name"),
                                        false, // modal
                                        new JButton[] { close },
                                        close,
                                        DialogDescriptor.DEFAULT_ALIGN,
                                        null,
                                        null /*final ActionListener bl*/);
            dd.setOptions (new Object [0]);

            dlg = DialogDisplayer.getDefault ().createDialog (dd);
            dlg.setVisible (true);
            dlg.addWindowListener(new WindowListener () {
                public void windowOpened (WindowEvent e) {}
                public void windowClosing (WindowEvent e) {
                    dlg = null;
                    pluginManagerUI = null;
                }
                public void windowClosed (WindowEvent e) {
                    dlg = null;
                    pluginManagerUI = null;
                }
                public void windowIconified (WindowEvent e) {}
                public void windowDeiconified (WindowEvent e) {}
                public void windowActivated (WindowEvent e) {}
                public void windowDeactivated (WindowEvent e) {}
            });
        } else {
            dlg.requestFocus ();
        }
    }
    
    public String getName () {
        return NbBundle.getMessage (PluginManagerAction.class, "PluginManagerAction_Name");
    }
    
    @Override
    protected void initialize () {
        super.initialize ();
        putValue ("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous () {
        return false;
    }
    
    public static PluginManagerUI getPluginManagerUI () {
        return pluginManagerUI;
    }
    
}
