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
package org.netbeans.modules.soa.pojo.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Utilities;
import org.openide.util.Lookup;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.NbBundle;

/**
 * palette action
 *
 * @author Sreenivasan Genipudi
 */
public class POJOPaletteActions extends PaletteActions {

    /**
    * @return An array of action that will be used to construct buttons for import
    * of new palette item in palette manager window
    */
    public Action[] getImportActions() {
        return new Action[0];
    }

    /**
    * @param category Lookup representing palette's category
    * @return Custom actions to be added to the top of default popup menu for the given category
    */
    public Action[] getCustomCategoryActions(Lookup category) {
        return new Action[0];
    }

    /**
    * @param item Lookup representing palette's item
    * @return Custom actions to be added to the top of the default popup menu for the given palette item
    */
    public Action[] getCustomItemActions(Lookup item) {
        return new Action[0];
    }

    /**
    * @return Custom actions to be added to the top of palette's default popup menu
    */
    public Action[] getCustomPaletteActions() {
        return new Action[0];
    }

    /**
    * Returns null to disable preferred action for this item.
    * @param item Lookup representing palette's item.
    * @return An action to be invoked when user double-clicks the item in palette
    */
    public Action getPreferredAction(Lookup item) {
        return new DefaultPaletteItemAction(item);
    }

    private static class DefaultPaletteItemAction extends AbstractAction {
        private Lookup item;

        DefaultPaletteItemAction(Lookup item) {
            this.item = item;
        }

        public void actionPerformed(ActionEvent e) {

            ActiveEditorDrop drop = (ActiveEditorDrop) item.lookup(ActiveEditorDrop.class);

            JTextComponent target = Utilities.getFocusedComponent();
            if (target == null) {
                String msg = NbBundle.getMessage(POJOPaletteActions.class, "MSG_ErrorNoFocusedDocument");
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                return;
            }

            try {
                drop.handleTransfer(target);
            }
            finally {
                Utilities.requestFocus(target);
            }

            PaletteController pc = POJOPaletteFactory.getPalette();
            pc.clearSelection();
        }
    }
}
