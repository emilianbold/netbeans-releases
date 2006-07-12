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

package org.netbeans.modules.projectimport.j2seimport.ui;
import java.awt.Dialog;
import javax.swing.JDialog;
import javax.swing.WindowConstants;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;


/**
 * @author Radek Matous
 */
public class WizardSupport extends WizardDescriptor  {
    public static BasicWizardIterator.BasicDataModel show(BasicWizardIterator wizardIterator) {        
        WizardSupport ws = new WizardSupport(wizardIterator);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(ws);
        dialog.setVisible(true);
        dialog.toFront();
        return (ws.getValue() != WizardDescriptor.FINISH_OPTION) ?
            null : wizardIterator.getData();
         
    }
          
    /** Creates a new instance of WizardSupport */
    private WizardSupport(BasicWizardIterator wizardIterator) {
        super(wizardIterator);
    }    
}
