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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.iep.editor.tcg.dialog;

import java.awt.Component;

import javax.swing.JFrame;

import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.modules.iep.editor.tcg.exception.ErrorDisplay;
import org.netbeans.modules.iep.editor.tcg.dialog.Confirm;

/**
 * This class ...
 *
 * @author Bing Lu
 */
public class NotifyHelper {

    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(NotifyHelper.class.getName());
    
    /**
     * This is a convenience method when the user just needs to confirm or
     * cancel something.
     *
     * @param title The title for the dialog
     * @param msg The message to be confirmed
     *
     * @return true if user selects okay, false otherwise
     */
    public static boolean confirm(String title, String msg) {
        Confirm c = new Confirm(title, msg);
        DialogDescriptor dd = new DialogDescriptor(c.getInnerPane(),
                                                   c.getTitle(), true,
                                                   c.getActionListener());
        java.awt.Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        return c.isOk();
    }

    /**
     * This method ...
     *
     * @param dap This ...
     */
    public static void reportError(Exception dap) {
        reportError(new JFrame(), dap);
    }     
     
    /**
     * This method ...
     *
     * @param c This ...
     * @param dap This ...
     */
    public static void reportError(Component c, Exception dap) {
            ErrorManager.getDefault().notify(dap);
        
    }

    /**
     * DOCUMENT ME!
     *
     * @param s The error message
     *
     * todo Document this method
     */
    public static void reportError(String s) {
        java.awt.Frame mainFrame = org.openide.windows.WindowManager.getDefault().getMainWindow();
        new ErrorDisplay(mainFrame, s);
        //reportError(new JFrame(), s);
    }

    /**
     * This method ...
     *
     * @param c This ...
     * @param s This ...
     */
    public static void reportError(Component c, String s) {
            NotifyDescriptor d = new NotifyDescriptor.Message(s,
                                     NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        
    }

    /**
     * This method ...
     *
     * @param s The info message
     *
     * @return Whether the user selected ok or cancel.
     */
    public static Object reportInfo(String s) {
            NotifyDescriptor d = new NotifyDescriptor.Message(s,
                                     NotifyDescriptor.INFORMATION_MESSAGE);
            return DialogDisplayer.getDefault().notify(d);
        
    }

    /**
     * This method ...
     *
     * @param c This ...
     * @param s This ...
     *
     * @return DOCUMENT ME!
     */
    public static Object reportInfo(Component c, String s) {
            NotifyDescriptor d = new NotifyDescriptor.Message(s,
                                     NotifyDescriptor.INFORMATION_MESSAGE);
            return DialogDisplayer.getDefault().notify(d);
        
    }
}

