/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        private static java.lang.ref.WeakReference currentWindowRef;
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
                return (Window) currentWindowRef.get();
            }
            else {
                return null;
            }
        }

        private static synchronized void setCurrentActivatedWindow(Window w) {
            currentWindowRef = new java.lang.ref.WeakReference(w);
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

        Installer.log.fine(help.toString() + " from " + focused);
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
        StatusDisplayer.getDefault ().setStatusText (""); // NOI18N
    }
}
