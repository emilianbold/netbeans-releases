/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.palette;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;



/**
 *
 * @author Libor Kotouc
 */
public class JSPPaletteActions extends PaletteActions {
    
    /** Creates a new instance of FormPaletteProvider */
    public JSPPaletteActions() {
    }

    public Action[] getImportActions() {
        return new Action[0]; //TODO implement this
    }

    public Action[] getCustomCategoryActions(Lookup category) {
        return new Action[0]; //TODO implement this
    }

    public Action[] getCustomItemActions(Lookup item) {
        return new Action[0]; //TODO implement this
    }

    public Action[] getCustomPaletteActions() {
        return new Action[0]; //TODO implement this
    }

    public Action getPreferredAction( Lookup item ) {
        return new J2EEPaletteItemInsertAction(item);
    }
    
    private static class J2EEPaletteItemInsertAction extends AbstractAction {
        
        private Lookup item;
        
        J2EEPaletteItemInsertAction(Lookup item) {
            this.item = item;
        }
                
        public void actionPerformed(ActionEvent e) {
      
            ActiveEditorDrop drop = (ActiveEditorDrop) item.lookup(ActiveEditorDrop.class);
            if (drop == null) {
                String body = (String) item.lookup(String.class);
                drop = new JSPEditorDropDefault(body);
            }
            
            JTextComponent target = Utilities.getFocusedComponent();
            if (target == null) {
                String msg = NbBundle.getMessage(JSPPaletteActions.class, "MSG_ErrorNoFocusedDocument");
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                return;
            }
            
            try {
                drop.handleTransfer(target);
            }
            finally {
                Utilities.requestFocus(target);
            }
            
            try {
                PaletteController pc = JSPPaletteFactory.getPalette();
                pc.clearSelection();
            }
            catch (IOException ioe) {} //should not occur
        }
    }
    
}
