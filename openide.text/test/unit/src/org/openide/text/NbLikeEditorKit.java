/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openide.text;

import java.beans.VetoableChangeListener;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;

/**
 * Emulates the behaviour of NetBeans editor's kit with all its special 
 * implementations.
 *
 * @author  Jaroslav Tulach
 */
final class NbLikeEditorKit extends DefaultEditorKit {
    public javax.swing.text.Document createDefaultDocument() {
        return new Doc ();
    }

    private final class Doc extends PlainDocument
    implements NbDocument.WriteLockable, StyledDocument {
//    implements NbDocument.PositionBiasable, NbDocument.WriteLockable,
//    NbDocument.Printable, NbDocument.CustomEditor, NbDocument.CustomToolbar, NbDocument.Annotatable {

        public Doc() {
            super (new StringContent ());
            
            // mark yourself of supporting modificationListener
            putProperty ("supportsModificationListener", Boolean.TRUE); 
        }

        public void runAtomic (Runnable r) {
            try {
                runAtomicAsUser (r);
            } catch (BadLocationException ex) {
                // too bad, no modification allowed
            }
        }

        public void runAtomicAsUser (Runnable r) throws BadLocationException {
             insOrRemoveOrRunnable (-1, null, null, -1, false, r);
        }

        public javax.swing.text.Style getLogicalStyle(int p) {
            return null;
        }

        public javax.swing.text.Style getStyle(java.lang.String nm) {
            return null;
        }

        public javax.swing.text.Style addStyle(java.lang.String nm, javax.swing.text.Style parent) {
            return null;
        }

        public void setParagraphAttributes(int offset, int length, javax.swing.text.AttributeSet s, boolean replace) {
        }

        public void setCharacterAttributes(int offset, int length, javax.swing.text.AttributeSet s, boolean replace) {
        }

        public void removeStyle(java.lang.String nm) {
        }

        public java.awt.Font getFont(javax.swing.text.AttributeSet attr) {
            return null;
        }

        public java.awt.Color getBackground(javax.swing.text.AttributeSet attr) {
            return null;
        }

        public javax.swing.text.Element getCharacterElement(int pos) {
            return null;
        }

        public void setLogicalStyle(int pos, javax.swing.text.Style s) {
        }

        public java.awt.Color getForeground(javax.swing.text.AttributeSet attr) {
            return null;
        }

        private int changes;
        public void insertString (int offs, String str, AttributeSet a) throws BadLocationException {
            insOrRemoveOrRunnable (offs, str, a, 0, true, null);
        }

        public void remove (int offs, int len) throws BadLocationException {
            insOrRemoveOrRunnable (offs, null, null, len, false, null);
        }
        
        
        private void insOrRemoveOrRunnable (int offset, String str, AttributeSet set, int len, boolean insert, Runnable run) 
        throws BadLocationException {
            boolean alreadyInsideWrite = getCurrentWriter () == Thread.currentThread ();
            if (alreadyInsideWrite) {
                if (run != null) {
                    run.run ();
                } else {
                    assertOffset (offset);
                    if (insert) {
                        super.insertString (offset, str, set);
                    } else {
                        super.remove(offset, len);
                    }
                }
                return;
            }
            
            Object o = getProperty ("modificationListener");
            
            if (run != null) {
                writeLock ();
                int prevChanges = changes;
                try {
                    run.run ();
                } finally {
                    writeUnlock ();
                }
                if (changes > prevChanges) {
                    try {
                        notifyModified (o, offset);
                    } catch (BadLocationException ex) {
                        // ok, too bad, just ignore
                    }
                }
            } else {
                assertOffset (offset);
                notifyModified (o, offset);
                try {
                    if (insert) {
                        super.insertString (offset, str, set);
                    } else {
                        super.remove(offset, len);
                    }
                } catch (BadLocationException ex) {
                    if (o instanceof VetoableChangeListener) {
                        VetoableChangeListener l = (VetoableChangeListener)o;
                        try {
                            l.vetoableChange (new java.beans.PropertyChangeEvent (this, "modified", null, Boolean.FALSE));
                        } catch (java.beans.PropertyVetoException ignore) {
                        }
                    }
                    throw ex;
                }
            }
        }
        
        private void assertOffset (int offset) throws BadLocationException {
            if (offset < 0) throw new BadLocationException ("", offset);
        }
        
        private void notifyModified (Object o, int offset) throws BadLocationException {
            if (o instanceof VetoableChangeListener) {
                VetoableChangeListener l = (VetoableChangeListener)o;
                try {
                    l.vetoableChange (new java.beans.PropertyChangeEvent (this, "modified", null, Boolean.TRUE));
                } catch (java.beans.PropertyVetoException ex) {
                    throw new BadLocationException("Document modification vetoed", offset);
                }
            }
        }

        protected void fireRemoveUpdate (javax.swing.event.DocumentEvent e) {
            super.fireRemoveUpdate(e);
            changes++;
        }

        protected void fireInsertUpdate (javax.swing.event.DocumentEvent e) {
            super.fireInsertUpdate(e);
            changes++;
        }

        protected void fireChangedUpdate (javax.swing.event.DocumentEvent e) {
            super.fireChangedUpdate(e);
            changes++;
        }

    } // end of Doc
}