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

import org.openide.ErrorManager;
import org.openide.TopManager;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

/** Show help for the thing under the cursor.
* @author Jesse Glick
*/
public class HelpAction extends SystemAction {

    private static final long serialVersionUID = 4658008202517094416L;

    /** Component the mouse is currently over. */
    private static Component globallySelectedComp;
    private static AWTEventListener l = new AWTEventListener() {
        public void eventDispatched(AWTEvent ev) {
            if ((ev instanceof MouseEvent) && ev.getID() == MouseEvent.MOUSE_ENTERED) {
                globallySelectedComp = ((MouseEvent)ev).getComponent();
                //System.err.println("mouse entered: " + globallySelectedComp);
            }
        }
    };
    static {
        // For live-tracking, we need to know what component the mouse is over:
        // Make sure it is not null:
        globallySelectedComp = TopManager.getDefault().getWindowManager().getMainWindow();
        AWTEventListener wl = (AWTEventListener)WeakListener.create(AWTEventListener.class, l, Toolkit.getDefaultToolkit());
        Toolkit.getDefaultToolkit().addAWTEventListener(wl, AWTEvent.MOUSE_EVENT_MASK);
        // [PENDING] Should instead use if available (JDK 1.4):
        // java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager ().getFocusOwner ()
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getMessage(HelpAction.class, "LBL_HelpAction");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource() {
        return "/org/netbeans/modules/javahelp/resources/show-help.gif"; // NOI18N
    }

    // Make sure this action works on dialogs, etc.--everywhere.
    protected void initialize() {
        super.initialize();
        Installer.err.log("HelpAction.initialize");
        // Cf. org.netbeans.core.windows.frames.NbFocusManager:
        putProperty("OpenIDE-Transmodal-Action", Boolean.TRUE); // NOI18N
    }

    public void actionPerformed(ActionEvent ev) {
        if (globallySelectedComp instanceof RootPaneContainer) {
            // #17424: in some circumstances an NbDialog will be open with
            // some contents incl. a table and so on--but not completely filled
            // acc. to the layout manager--and mouse entered events give the
            // NbDialog itself, rather than the table or whatever you might
            // have expected. Strange bit of AWT behavior there. Anyway, since
            // NbPresenter puts a helpID on the root pane, we need to look at
            // that instead of the dialog.
            globallySelectedComp = ((RootPaneContainer)globallySelectedComp).getRootPane();
        }
        HelpCtx help = HelpCtx.findHelp(globallySelectedComp);
        if (Installer.err.isLoggable(ErrorManager.UNKNOWN)) {
            Installer.err.log(ErrorManager.UNKNOWN, help.toString() + " from " + globallySelectedComp);
        } else if (Installer.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            Installer.err.log(ErrorManager.INFORMATIONAL, help.toString());
        }
        TopManager.getDefault().setStatusText(NbBundle.getMessage(HelpAction.class, "CTL_OpeningHelp"));
        Installer.getHelp().showHelp(help);
        TopManager.getDefault().setStatusText(""); // NOI18N
        // Copied from MainWindow:
        final MenuSelectionManager msm = MenuSelectionManager.defaultManager();
        final MenuElement[] path = msm.getSelectedPath();
        // post request that should after half of second clear the selected menu
        RequestProcessor.postRequest(new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        MenuElement[] newPath = msm.getSelectedPath();
                        if (newPath.length != path.length) return;
                        for (int i = 0; i < newPath.length; i++) {
                            if (newPath[i] != path[i]) return;
                        }
                        msm.clearSelectedPath();
                    }
                });
            }
        }, 200);
    }
}
