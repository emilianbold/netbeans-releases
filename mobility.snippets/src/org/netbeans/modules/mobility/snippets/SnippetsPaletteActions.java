/*
 * SnippetsPaletteActions.java
 *
 * Created on August 22, 2006, 6:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.snippets;

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
 * @author bohemius
 */
public class SnippetsPaletteActions extends PaletteActions {
    
    /** Creates a new instance of SnippetsPaletteActions */
    public SnippetsPaletteActions() {
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
       
    public Action getPreferredAction(Lookup item) {
        return new SnippetsPaletteInsertAction(item);
    }
    
    private static class SnippetsPaletteInsertAction extends AbstractAction {
        
        private Lookup item;
        
        SnippetsPaletteInsertAction(Lookup item) {
            this.item = item;
        }
                
        public void actionPerformed(ActionEvent e) {
      
            ActiveEditorDrop drop = (ActiveEditorDrop) item.lookup(ActiveEditorDrop.class);
            
            JTextComponent target = Utilities.getFocusedComponent();
            if (target == null) {
                String msg = NbBundle.getMessage(SnippetsPaletteActions.class, "MSG_ErrorNoFocusedDocument");
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
                PaletteController mController = SnippetsPaletteSupport.getPaletteController();
                mController.clearSelection();
            }
            catch (IOException ioe) {
              ioe.printStackTrace();  
            } 

        }
    }
    
}
