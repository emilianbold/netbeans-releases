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
package org.netbeans.modules.soa.jca.base.palette;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.soa.jca.base.wizard.GlobalRarWizardAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.text.ActiveEditorDrop;

/**
 * palette drop action
 *
 * @author echou
 */
public class GlobalRarPaletteDrop implements ActiveEditorDrop, Transferable  {

    protected String rarName;

    /**
    * A method called from the drop target that supports the artificial DataFlavor.
    * @param target a Component where drop operation occured
    * @return true if implementor allowed a drop operation into the targetComponent
    */
    public boolean handleTransfer(JTextComponent target) {
        try {
            GlobalRarWizardAction action = new GlobalRarWizardAction(target, rarName);
            action.invoke();
        } catch(Exception e) {
            e.printStackTrace();
            NotifyDescriptor d = new NotifyDescriptor.Exception(e);
            DialogDisplayer.getDefault().notifyLater(d);
            return false;
        }
        return true;
    }

    public boolean isDataFlavorSupported(DataFlavor f) {
          return ActiveEditorDrop.FLAVOR == f;
    }

    public final DataFlavor[] getTransferDataFlavors() {
        DataFlavor delegatorFlavor[] = new DataFlavor[1];
        delegatorFlavor[0] = ActiveEditorDrop.FLAVOR;
        return delegatorFlavor;
    }

    public final Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return (flavor == ActiveEditorDrop.FLAVOR) ? this : null;
    }

}

