/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.text;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.event.InputEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.TransferHandler;
import javax.swing.plaf.UIResource;
import javax.swing.text.*;


/** performance trick - 18% of time saved during open of an editor
*
* @author Ales Novak
*/
final class QuietEditorPane extends JEditorPane {
    final static int FIRE = 0x1;
    final static int PAINT = 0x2;
    final static int ALL = FIRE | PAINT;

    // #21120. Caret was null while serializing CloneableEditor.

    /** Saves last position of caret when, doing it's UI reinstallation. */
    private int lastPosition = -1;

    /** is firing of events enabled? */
    int working = FIRE; // [Mila] firing since begining, otherwise doesn't work well
    
    
    public void setDocument(Document doc) {
        super.setDocument(doc);
        
        // Setting DelegatingTransferHandler, where CallbackTransferable will
        // be handled in importData method. 
        // For more details, please refer issue #53439        
        if (doc != null){
            TransferHandler thn = getTransferHandler();
            DelegatingTransferHandler dth = new DelegatingTransferHandler(thn);
            setTransferHandler(dth);
        }
    }
    
    
    public void setWorking(int x) {
        working = x;
    }

    public void firePropertyChange(String s, Object val1, Object val2) {
        if ((working & FIRE) != 0) {
            super.firePropertyChange(s, val1, val2);
        }
    }

    /** Overrides superclass method, to keep old caret position.
     * While is reinstallation of UI in progress, there
     * is a gap between the uninstallUI
     * and intstallUI when caret set to <code>null</code>. */
    public void setCaret(Caret caret) {
        if (caret == null) {
            Caret oldCaret = getCaret();

            if (oldCaret != null) {
                lastPosition = oldCaret.getDot();
            }
        }

        super.setCaret(caret);
    }

    /** Gets the last caret position, for the case the serialization
     * is done during the time of pane UI reinstallation. */
    int getLastPosition() {
        return lastPosition;
    }

    /*
    public void setDocument(Document doc) {
      if (working) {
        super.setDocument(doc);
      }
    }

    public void setUI(javax.swing.plaf.TextUI ui) {
      if (working) {
        super.setUI(ui);
      }
    }*/
    public void revalidate() {
        if ((working & PAINT) != 0) {
            super.revalidate();
        }
    }

    public void repaint() {
        if ((working & PAINT) != 0) {
            super.repaint();
        }
    }

    /**
     * Delegating TransferHandler.
     * The main purpose is hooking on importData method where CallbackTransferable
     * is handled. For more details, please refer issue #53439
     */    
    private class DelegatingTransferHandler extends TransferHandler{
        
        TransferHandler delegator;
        
        public DelegatingTransferHandler(TransferHandler delegator){
            this.delegator = delegator;
        }
        
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            delegator.exportAsDrag(comp, e, action);
        }

        public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
            delegator.exportToClipboard(comp, clip, action);
        }

        public boolean importData(JComponent comp, Transferable t) {
            try {
                if (t.isDataFlavorSupported(ActiveEditorDrop.FLAVOR)){
                    Object obj = t.getTransferData(ActiveEditorDrop.FLAVOR);
                    if (obj instanceof ActiveEditorDrop && comp instanceof JTextComponent){
                        return ((ActiveEditorDrop)obj).handleTransfer((JTextComponent)comp);
                    }
                }
            } catch (Exception exc){
                exc.printStackTrace();
            }
            return delegator.importData(comp, t);
        }

        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            for (int i=0; i<transferFlavors.length; i++){
                if (transferFlavors[i] == ActiveEditorDrop.FLAVOR){
                    return true;
                }
            }
            return delegator.canImport(comp, transferFlavors);
        }

        public int getSourceActions(JComponent c) {
            return delegator.getSourceActions(c);
        }

        public Icon getVisualRepresentation(Transferable t) {
            return delegator.getVisualRepresentation(t);
        }

        protected void exportDone(JComponent source, Transferable data, int action) {
            try {
                java.lang.reflect.Method method = delegator.getClass().getDeclaredMethod(
                    "exportDone",  // NOI18N
                    new Class[] {javax.swing.JComponent.class, Transferable.class, int.class});
                method.setAccessible(true);
                method.invoke(delegator, new Object[] {source, data, new Integer(action)});
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (java.lang.reflect.InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
        
        protected Transferable createTransferable(JComponent comp) {
            try {
                java.lang.reflect.Method method = delegator.getClass().getDeclaredMethod(
                    "createTransferable", // NOI18N
                    new Class[] {javax.swing.JComponent.class});
                method.setAccessible(true);
                return (Transferable)method.invoke(delegator, new Object[] {comp});
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (java.lang.reflect.InvocationTargetException ex) {
                ex.printStackTrace();
            }
            return null;
	}
    }
    
}
