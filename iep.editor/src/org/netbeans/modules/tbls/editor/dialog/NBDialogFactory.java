/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.tbls.editor.dialog;

import java.awt.Dialog;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;


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

