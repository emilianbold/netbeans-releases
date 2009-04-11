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

package org.netbeans.modules.javahelp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.netbeans.api.javahelp.Help;
import org.openide.awt.StatusDisplayer;

/**
 * Shows help for the currently focused component
 * @author Jesse Glick
 */
public class HelpAction extends SystemAction
{
    private static final long serialVersionUID = 4658008202517094416L;

    public String getName() {
        return NbBundle.getMessage(HelpAction.class, "LBL_HelpAction");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected String iconResource() {
        return "org/netbeans/modules/javahelp/resources/show-help.gif"; // NOI18N
    }

    protected void initialize() {
        super.initialize();
        Installer.log.fine("HelpAction.initialize");

        // Cf. org.netbeans.core.windows.frames.NbFocusManager and
        // org.netbeans.core.windows.frames.ShortcutAndMenuKeyEventProcessor
        putProperty("OpenIDE-Transmodal-Action", Boolean.TRUE); // NOI18N
    }

    static class WindowActivatedDetector implements AWTEventListener {
        private static java.lang.ref.WeakReference<Window> currentWindowRef;
        private static WindowActivatedDetector detector = null;

        static synchronized void install() {
            if (detector == null) {
                detector = new WindowActivatedDetector();
                Toolkit.getDefaultToolkit ().addAWTEventListener(detector, AWTEvent.WINDOW_EVENT_MASK);
            }
        }
        
        static synchronized void uninstall() {
            if (detector != null) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(detector);
                detector = null;
            }
        }
        
        static synchronized Window getCurrentActivatedWindow() {
            if (currentWindowRef != null) {
                return currentWindowRef.get();
            }
            else {
                return null;
            }
        }

        private static synchronized void setCurrentActivatedWindow(Window w) {
            currentWindowRef = new java.lang.ref.WeakReference<Window>(w);
        }

        public void eventDispatched (AWTEvent ev) {
            if (ev.getID() != WindowEvent.WINDOW_ACTIVATED)
                return;
            setCurrentActivatedWindow(((WindowEvent) ev).getWindow());
        }
    }
    
    private static HelpCtx findHelpCtx() {
        Window w = WindowActivatedDetector.getCurrentActivatedWindow();
        Component focused = (w != null) ? SwingUtilities.findFocusOwner(w) : null;
        HelpCtx help = (focused == null) ? HelpCtx.DEFAULT_HELP : HelpCtx.findHelp(focused);

        Installer.log.fine("HelpCtx " + help.toString() + " from " + focused);
        return help;
    }
    
    public void actionPerformed(ActionEvent ev) {
        Help h = (Help)Lookup.getDefault().lookup(Help.class);
        if (h == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        HelpCtx help;
        
        final MenuElement[] path =
            MenuSelectionManager.defaultManager().getSelectedPath();

        if (path != null
            && path.length > 0
            && !(path[0].getComponent() instanceof javax.swing.plaf.basic.ComboPopup)
            ) {
            help = HelpCtx.findHelp(path[path.length - 1].getComponent());
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    MenuElement[] newPath =
                        MenuSelectionManager.defaultManager().getSelectedPath();

                    if (newPath.length != path.length)
                        return;
                    for (int i = 0; i < newPath.length; i++) {
                        if (newPath[i] != path[i])
                            return;
                    }
                    MenuSelectionManager.defaultManager().clearSelectedPath();
                }
            });
        }
        else {
            help = findHelpCtx();
        }
        
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(HelpAction.class, "CTL_OpeningHelp"));
        h.showHelp (help);
    }
}
