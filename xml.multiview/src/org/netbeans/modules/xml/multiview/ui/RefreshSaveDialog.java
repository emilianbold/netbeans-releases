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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

import org.openide.util.NbBundle;

/** 
 * A dialog to be shown when the editor is being closed the model is not valid.
 * Presents options for fixing, refreshing and saving. 
 *
 * Created on November 28, 2004, 7:18 PM
 * @author mkuchtiak
 */
public class RefreshSaveDialog extends org.openide.DialogDescriptor {
    public static final Integer OPTION_FIX=new Integer(0);
    public static final Integer OPTION_REFRESH=new Integer(1);
    public static final Integer OPTION_SAVE=new Integer(2);

    private static final String[] OPTIONS = new String[] {
        NbBundle.getMessage(RefreshSaveDialog.class,"OPT_FixNow"),
        NbBundle.getMessage(RefreshSaveDialog.class,"OPT_Refresh"),
        NbBundle.getMessage(RefreshSaveDialog.class,"OPT_Save")
    };

    /** Creates a new instance of RefreshSaveDialog */
    public RefreshSaveDialog(ErrorPanel errorPanel) {
        this (errorPanel, errorPanel.getErrorMessage());
    }
   
    /** Creates a new instance of RefreshSaveDialog */
    public RefreshSaveDialog(ErrorPanel errorPanel, String errorMessage ) {
        super (
            NbBundle.getMessage(RefreshSaveDialog.class,"TTL_warning_message",errorMessage),
            NbBundle.getMessage(RefreshSaveDialog.class,"TTL_warning"),
            true,
            OPTIONS,
            OPTIONS[0],
            BOTTOM_ALIGN,
            null,
            null
        );
        setButtonListener(new DialogListener(errorPanel));
        setClosingOptions(null);  
    }
    public Object getValue() {
        Object ret = super.getValue();
        if (ret.equals(OPTIONS[1])) return OPTION_REFRESH;
        else if (ret.equals(OPTIONS[2])) return OPTION_SAVE;
        else return OPTION_FIX;
    }
    
    private class DialogListener implements java.awt.event.ActionListener {
        private ErrorPanel errorPanel;
        DialogListener(ErrorPanel errorPanel) {
            this.errorPanel=errorPanel;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource().equals(OPTIONS[1]) || evt.getSource().equals(OPTIONS[2])) {
                errorPanel.clearError();
            }
        }
    }
}
