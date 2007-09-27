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

import java.awt.Dialog;
import java.awt.Frame;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.WindowManager;

import org.netbeans.modules.iep.editor.tcg.dialog.OKCancelDialogInterface;


/**
 * This class constructs the various dialogs required by the tool when it is
 * not running under NetBeans. When the tool is running under NetBeans,
 * dialogs are constructed via org.netbeans.modules.iep.editor.tcg.dialog.NBDialogFactory
 *
 * @author Bing Lu
 */
public class NBDialogFactory {

    /**
     * Logger.
     */
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(NBDialogFactory.class.getName());

    /**
     * Constructor for the DialogFactory object
     */
    private NBDialogFactory() {
        super();
    }
    
    public static NBDialogFactory getInstance() {
        return mInstance;
    }
    
    private static NBDialogFactory mInstance = new NBDialogFactory();

    /**
     * Gets the dialog attribute of the DialogFactory object
     *
     * @param ocdi This ...
     *
     * @return The dialog value
     */
    public Dialog getDialog(OKCancelDialogInterface ocdi) {

        DialogDescriptor dd = new DialogDescriptor(ocdi.getInnerPane(),
                                                   ocdi.getTitle(), true,
                                                   ocdi.getActionListener());

        mLog.info("NBDialogFactory.dialog_descriptor_is " + dd);

        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

        mLog.info("NBDialogFactory.dialog_is_a " + d.getClass());

        return d;
    }

    /**
     * Gets the nonModalDialg attribute of the NBDialogFactory object
     *
     * @param ocdi This ...
     *
     * @return The nonModalDialg value
     */
    public Dialog getNonModalDialg(OKCancelDialogInterface ocdi) {

        DialogDescriptor dd = new DialogDescriptor(ocdi.getInnerPane(),
                                                   ocdi.getTitle(), false,
                                                   ocdi.getActionListener());

        mLog.info(java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/tcg/dialog/properties").getString("NBDialogFactory.dialog_descriptor_is_") + dd);

        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

        mLog.info(java.util.ResourceBundle.getBundle("org/netbeans/modules/iep/editor/tcg/dialog/properties").getString("NBDialogFactory.dialog_is_a_") + d.getClass());

        return d;
    }
}

