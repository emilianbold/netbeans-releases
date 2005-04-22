/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.text;

import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.*;

import org.netbeans.junit.*;

import org.openide.util.Lookup;
import org.openide.util.lookup.*;


/** Testing different features of CloneableEditorSupport
 *
 * @author Jaroslav Tulach
 */
public class NotifyModifiedTest extends NbTestCase 
implements CloneableEditorSupport.Env {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.text.NotifyModifiedTest$Lkp");
    }

    /** the support to work with */
    protected CES support;
    /** the content of lookup of support */
    private InstanceContent ic;

    
    // Env variables
    private String content = "";
    private boolean valid = true;
    private volatile boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    private PropertyChangeSupport propL = new PropertyChangeSupport (this);
    private java.beans.VetoableChangeListener vetoL;
    private boolean shouldVetoNotifyModified;
    /** kit to create */
    private javax.swing.text.EditorKit editorKit;

    
    public NotifyModifiedTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp () {
        ic = new InstanceContent ();
        support = new CES (this, new AbstractLookup (ic));
        
        assertNotNull("ErrManager has to be in lookup", org.openide.util.Lookup.getDefault().lookup(ErrManager.class));
        ErrManager.resetMessages();
        ErrManager.log = getLog ();
    }
    
    protected void runTest () throws Throwable {
        try {
            super.runTest ();
        } catch (AssertionFailedError err) {
            AssertionFailedError n = new AssertionFailedError (err.getMessage () + "\n" + ErrManager.messages);
            n.initCause (err);
            throw n;
        }
    }
    
    //
    // overwrite editor kit
    //
    
    protected javax.swing.text.EditorKit createEditorKit () {
        return null;
    }
    
    protected void checkThatDocumentLockIsNotHeld () {
    }

    protected void doesVetoedInsertFireBadLocationException (javax.swing.text.BadLocationException e) {
        if (e != null) {
            fail("On non-nblike documents, vetoed insert does not generate BadLocationException");
        }
    }
    
    //
    // test methods
    //

    
    public void testJustOneCallToModified () throws Exception {
        content = "Line1\nLine2\n";
        
        // in order to set.getLines() work correctly, the document has to be loaded
        javax.swing.text.Document doc = support.openDocument();
        assertEquals ("No modification", 0, support.notifyModified);
        
        doc.insertString (3, "Ahoj", null);
        assertEquals ("One modification", 1, support.notifyModified);

        doc.insertString (7, "Kuk", null);
        assertEquals ("Still one modification", 1, support.notifyModified);

        doc.remove (7, 3);
        assertEquals ("Still one modification2", 1, support.notifyModified);
        
        support.saveDocument ();
        assertEquals ("Marked unmodified", 1, support.notifyUnmodified);

        doc.remove (0, 1);
        assertEquals ("Modifies again", 2, support.notifyModified);
    }
    
    public void testTheDocumentReturnsBackIfModifyIsNotAllowed () throws Exception {
        content = "Nic\n";
        
        // in order to set.getLines() work correctly, the document has to be loaded
        javax.swing.text.Document doc = support.openDocument();
        assertEquals ("No modification", 0, support.notifyModified);
        
        shouldVetoNotifyModified = true;
        
        // should be reverted in SwingUtilities.invokeLater
        try {
            doc.insertString (0, "Ahoj", null);
            // Previous insert should fail with exception
            doesVetoedInsertFireBadLocationException (null);
        } catch (javax.swing.text.BadLocationException e) {
            // Expecting the thrown exception
            doesVetoedInsertFireBadLocationException (e);
        }
        waitEQ ();
        
        assertEquals ("One modification called (but it was vetoed)", 1, support.notifyModified);
        assertEquals ("No unmodification called", 0, support.notifyUnmodified);

        String first = doc.getText (0, 1);
        assertEquals ("First letter is N", "N", first);
    }
    
    public void testTheDocumentReturnsBackIfModifyIsNotAllowedMultipleTimes () throws Exception {
        class R implements Runnable {
            int[] i = { 0 };
            Document[] doc = { null };
            
            public void run () {
                try {
                    if (i[0] % 2 == 0) {
                        doc[0].insertString (0, "Ahoj", null);
                    } else {
                        doc[0].remove (0, 2);
                    }
                    // Previous insert should fail with exception
                    doesVetoedInsertFireBadLocationException (null);
                } catch (javax.swing.text.BadLocationException e) {
                    // Expecting the thrown exception
                    doesVetoedInsertFireBadLocationException (e);
                }
            }
        }
        
        R r = new R ();
        doTheDocumentReturnsBackIfModifyIsNotAllowedMultipleTimes (r.i, r.doc, r);
    }

    public void testTheDocumentReturnsBackIfModifyIsNotAllowedMultipleTimesInAtomicSection () throws Exception {
        class R implements Runnable {
            int[] i = { 0 };
            javax.swing.text.StyledDocument[] doc = { null };
            
            private boolean inAtomic;
            
            public void run () {
                if (!inAtomic) {
                    inAtomic = true;
                    NbDocument.runAtomic(doc[0], this);
                    inAtomic = false;
                } else {
                    try {
                        doc[0].insertString (0, "Ahoj", null);
                    } catch (javax.swing.text.BadLocationException e) {
                        fail ("Inside atomic no BadLocationException due to unmodifiable source");
                    }
                }
            }
        }
        
        R r = new R ();
        doTheDocumentReturnsBackIfModifyIsNotAllowedMultipleTimes (r.i, r.doc, r);
    }
    
    /** Passing parameters by reference - e.g. arrays of size 1, so [0] can be filled and changed... */
    private void doTheDocumentReturnsBackIfModifyIsNotAllowedMultipleTimes (int[] i, Document[] doc, Runnable op) throws Exception {
        content = "EmptyContentForTheDocument\n";
        
        // in order to set.getLines() work correctly, the document has to be loaded
        doc[0] = support.openDocument();
        assertEquals ("No modification", 0, support.notifyModified);
        
        shouldVetoNotifyModified = true;
     
        for (i[0] = 0; i[0] < 10; i[0]++) {
            // do operation that will be forbidden
            op.run ();
            waitEQ ();
            
            support.assertModified (false, "Is still unmodified");
        }
        
        String first = doc[0].getText (0, doc[0].getLength ());
        if (!first.equals (content)) {
            fail ("Expected: " + content + 
                  " but was: " + first);
        }
        
        assertEquals ("Five vetoed modifications", 10, support.notifyModified);
        assertEquals ("No unmodification called", 0, support.notifyUnmodified);
    }
    
    
    public void testBadLocationException () throws Exception {
        content = "Nic\n";
        
        // in order to set.getLines() work correctly, the document has to be loaded
        javax.swing.text.Document doc = support.openDocument();
        assertEquals ("No modification", 0, support.notifyModified);
        
        try {
            doc.insertString (10, "Ahoj", null);
            fail ("This should generate bad location exception");
        } catch (javax.swing.text.BadLocationException ex) {
            // ok
        }
        
        int expected = createEditorKit () instanceof NbLikeEditorKit ? 1 : 0;
        assertEquals (expected + " modification called (but it was vetoed)", expected, support.notifyModified);
        assertEquals (expected + " unmodification called", expected, support.notifyUnmodified);

        String first = doc.getText (0, 1);
        assertEquals ("First letter is N", "N", first);
    }
    
    public void testDoModificationsInAtomicBlock () throws Exception {
        content = "Something";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        
        class R implements Runnable {
            public void run () {
                try {
                    doc.insertString (0, "Ahoj", null);
                } catch (javax.swing.text.BadLocationException ex) {
                    AssertionFailedError e = new AssertionFailedError (ex.getMessage ());
                    e.initCause (ex);
                    throw e;
                }
            }
        }
        
        R r = new R ();
        
        NbDocument.runAtomic (doc, r);
        
        assertEquals ("One modification", 1, support.notifyModified);
        assertEquals ("no unmod", 0, support.notifyUnmodified);
    }

    public void testDoModificationsInAtomicBlockAndRefuseThem () throws Exception {
        content = "Something";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        shouldVetoNotifyModified = true;
        
        class R implements Runnable {
            public boolean gotIntoRunnable;
            
            public void run () {
                gotIntoRunnable = true;
                
                try {
                    doc.insertString (0, "Ahoj", null);
                    doc.remove (0, 1);
                    doc.remove (0, 1);
                } catch (javax.swing.text.BadLocationException ex) {
                    AssertionFailedError e = new AssertionFailedError (ex.getMessage ());
                    e.initCause (ex);
                    throw e;
                }
            }
        }
        
        R r = new R ();
        
        NbDocument.runAtomic (doc, r);
        waitEQ ();
        
        
        assertTrue ("Runable started", r.gotIntoRunnable);

        if (support.notifyModified == 0) {
            fail ("At least One notification expected");
        }
        assertEquals ("no unmod", 0, support.notifyUnmodified);
        
        support.assertModified (false, "Document is not modified");
        
        String text = doc.getText (0, doc.getLength ());
        assertEquals ("The text is the same as original content", content, text);
    }
    
    public void testRevertModificationAfterSave () throws Exception {
        content = "Ahoj";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();
        
        doc.insertString (4, " Jardo", null);
        doc.insertString (0, ":", null);

        String text = doc.getText (0, doc.getLength ());

        support.saveDocument ();
        support.assertModified (false, "Not modified");
        
        shouldVetoNotifyModified = true;
        try {
            doc.remove (0, 5);
            doesVetoedInsertFireBadLocationException (null);
        } catch (BadLocationException ex) {
            doesVetoedInsertFireBadLocationException (ex);
        }
        waitEQ ();
        
        support.assertModified (false, "Not modified");

        text = doc.getText (0, doc.getLength ());
        if (!":Ahoj Jardo".equals (text)) {
            fail ("The text as after save ':Ahoj Jardo' but was: " + text);
        }
    }
    
    public void testAtomicBlockWithoutModifications () throws Exception {
        content = "Something";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        class R implements Runnable {
            public void run () {
            }
        }
        
        R r = new R ();
        
        NbDocument.runAtomic (doc, r);

        assertEquals ("The same number of modification and unmodifications", support.notifyModified, support.notifyUnmodified);
        assertEquals ("Actually it is zero", 0, support.notifyUnmodified);
    }
    
    public void testDoInsertAfterEmptyBlock () throws Exception {
        testAtomicBlockWithoutModifications ();
        
        support.getDocument ().insertString (0, "Ahoj", null);
        
        assertEquals ("One modification now", 1, support.notifyModified);
        assertEquals ("No unmodified", 0, support.notifyUnmodified);
        support.assertModified (true, "Is modified");
    }

    public void testDoRemoveAfterEmptyBlock () throws Exception {
        testAtomicBlockWithoutModifications ();
        
        support.getDocument ().remove (0, 4);
        
        assertEquals ("One modification now", 1, support.notifyModified);
        assertEquals ("No unmodified", 0, support.notifyUnmodified);
        support.assertModified (true, "Is modified");
    }
    
    public void testAtomicBlockWithoutModificationsAfterInsert () throws Exception {
        doAtomicBlockWithoutModificationsAfterInsert (false, 1);
    }
    public void testAtomicBlockWithoutModificationsAfterInsertDouble () throws Exception {
        doAtomicBlockWithoutModificationsAfterInsert (false, 2);
    }
    public void testAtomicUserBlockWithoutModificationsAfterInsert () throws Exception {
        doAtomicBlockWithoutModificationsAfterInsert (true, 1);
    }
    public void testAtomicUserBlockWithoutModificationsAfterInsertDouble () throws Exception {
        doAtomicBlockWithoutModificationsAfterInsert (true, 2);
    }
    
    private void doAtomicBlockWithoutModificationsAfterInsert (final boolean asUser, final int cnt) throws Exception {
        content = "Something";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        doc.insertString(0, "Ahoj", null);
        
        class R implements Runnable {
            
            public int counter = cnt;
            
            public void run () {
                if (--counter > 0) {
                    if (asUser) {
                        try {
                            NbDocument.runAtomicAsUser(doc, this);
                        } catch (javax.swing.text.BadLocationException ex) {
                            throw (AssertionFailedError)new AssertionFailedError (ex.getMessage()).initCause(ex);
                        }
                    } else {
                        NbDocument.runAtomic(doc, this);
                    }
                }
            }
        }
        
        R r = new R ();
        
        support.assertModified (true, "Document must be modified");
        
        if (asUser) {
            NbDocument.runAtomicAsUser(doc, r);
        } else {
            NbDocument.runAtomic (doc, r);
        }

        support.assertModified (true, "Document must stay modified");
    }
    
    public void testUndoDoesMarkFileAsDirtyIssue56963 () throws Exception {
        content = "Somecontent";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        int len = doc.getLength ();
        
        assertEquals ("Content opened", "Somecontent", doc.getText (0, len));
        
        doc.remove (0, len);
        
        assertEquals ("Empty", 0, doc.getLength ());
        assertTrue ("Can undo", support.getUndoRedo ().canUndo ());
        
        support.saveDocument ();
        waitEQ ();
        
        assertTrue ("Can undo as well", support.getUndoRedo ().canUndo ());
        
        support.getUndoRedo ().undo ();
        waitEQ ();
        
        assertEquals ("Lengh it back", len, doc.getLength ());
        assertEquals ("Content is back", "Somecontent", doc.getText (0, len));
        
        support.assertModified (true, "Document is Modified");

        support.getUndoRedo ().redo ();
        
        assertEquals ("Zero length", 0, doc.getLength ());
        
        support.assertModified (false, "Document is UnModified");
    }
    
    public void testReloadWithoutModifiedIssue57104 () throws Exception {
        content = "Somecontent";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        int len = doc.getLength ();
        
        assertEquals ("Content opened", "Somecontent", doc.getText (0, len));
        
        doc.remove (0, len);
        
        assertEquals ("Empty", 0, doc.getLength ());
        assertTrue ("Can undo", support.getUndoRedo ().canUndo ());
        
        support.saveDocument ();
        waitEQ ();
        
        assertTrue ("Can undo as well", support.getUndoRedo ().canUndo ());
        
        
        content = "Newcontent";
        int newLen = content.length ();
        
        assertEquals ("Once modified", 1, support.notifyModified);
        assertEquals ("Once unmodified after save", 1, support.notifyUnmodified);
        
        propL.firePropertyChange (PROP_TIME, null, null);
        waitEQ ();

        Object newDoc = support.openDocument ();
        assertSame ("Reload does not change the document", newDoc, doc);
        
        assertEquals ("Length it new", newLen, doc.getLength ());
        assertEquals ("Content is new", "Newcontent", doc.getText (0, newLen));

        assertEquals ("Still one modified", 1, support.notifyModified);
        assertEquals ("Still one unmodified", 1, support.notifyUnmodified);
    }

    public void testUndoMarksFileUnmodified () throws Exception {
        content = "Somecontent";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        int len = doc.getLength ();
        
        assertEquals ("Content opened", "Somecontent", doc.getText (0, len));
        
        doc.remove (0, len);
        
        support.assertModified (true, "Document is modified");
        assertTrue ("Can undo", support.getUndoRedo ().canUndo ());
        
        support.getUndoRedo ().undo ();
        
        support.assertModified (false, "Document is unmodified");
    }    
    
    public void testReloadWhenModifiedIssue57104 () throws Exception {
        content = "Somecontent";
        
        final javax.swing.text.StyledDocument doc = support.openDocument();

        int len = doc.getLength ();
        
        assertEquals ("Content opened", "Somecontent", doc.getText (0, len));
        
        doc.remove (0, len);
        
        assertEquals ("Empty", 0, doc.getLength ());
        assertTrue ("Can undo", support.getUndoRedo ().canUndo ());
        
        support.saveDocument ();
        waitEQ ();
        
        
        assertTrue ("Can undo as well", support.getUndoRedo ().canUndo ());
        assertEquals ("Once modified", 1, support.notifyModified);
        assertEquals ("Once unmodified after save", 1, support.notifyUnmodified);

        support.getUndoRedo ().undo ();
        waitEQ ();
        
        assertEquals ("Lengh it back", len, doc.getLength ());
        assertEquals ("Content is back", "Somecontent", doc.getText (0, len));
        
        waitEQ ();
        support.assertModified (true, "Document is Modified");
        
        assertEquals ("One more modified", 2, support.notifyModified);
        assertEquals ("No unmodifications", 1, support.notifyUnmodified);

        
        content = "Newcontent";
        int newLen = content.length ();
        
        // does the reload
        propL.firePropertyChange (PROP_TIME, null, null);
        // wait till reload is over
        waitEQ ();

        Object newDoc = support.openDocument ();
        assertSame ("Reload does not change the document", newDoc, doc);
        
        assertEquals ("Length it new", newLen, doc.getLength ());
        assertEquals ("Content is new", "Newcontent", doc.getText (0, newLen));

        assertEquals ("No more modified", 2, support.notifyModified);
        assertEquals ("But one more unmodified", 2, support.notifyUnmodified);
    }
    
    private void waitEQ () throws Exception {
        javax.swing.SwingUtilities.invokeAndWait (new Runnable () { public void run () { } });
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.addPropertyChangeListener (l);
    }    
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.removePropertyChangeListener (l);
    }
    
    public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        assertEquals ("Removing the right veto one", vetoL, l);
        vetoL = null;
    }
    
    public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
        return support;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public java.util.Date getTime() {
        return date;
    }
    
    public java.io.InputStream inputStream() throws java.io.IOException {
        return new java.io.ByteArrayInputStream (content.getBytes ());
    }
    public java.io.OutputStream outputStream() throws java.io.IOException {
        class ContentStream extends java.io.ByteArrayOutputStream {
            public void close () throws java.io.IOException {
                super.close ();
                content = new String (toByteArray ());
            }
        }
        
        return new ContentStream ();
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public boolean isModified() {
        return modified;
    }

    public void markModified() throws java.io.IOException {
        modified = true;
        //new Exception ("markModified: " + modified).printStackTrace(System.out);
        checkThatDocumentLockIsNotHeld ();
    }
    
    public void unmarkModified() {
        modified = false;
        //new Exception ("unmarkModified: " + modified).printStackTrace(System.out);
        checkThatDocumentLockIsNotHeld ();
    }
    
    /** Implementation of the CES */
    protected final class CES extends CloneableEditorSupport {
        public int notifyUnmodified;
        public int notifyModified;
        
        public CES (Env env, Lookup l) {
            super (env, l);
        }
        
        protected String messageName() {
            return "Name";
        }
        
        protected String messageOpened() {
            return "Opened";
        }
        
        protected String messageOpening() {
            return "Opening";
        }
        
        protected String messageSave() {
            return "Save";
        }
        
        protected String messageToolTip() {
            return "ToolTip";
        }
        
        protected void notifyUnmodified () {
            notifyUnmodified++;
            ErrManager.getDefault ().notify (new Exception ("notifyUnmodified: " + notifyUnmodified));
            
            super.notifyUnmodified();
        }

        protected boolean notifyModified () {
            notifyModified++;
            
            if (shouldVetoNotifyModified) {
                return false;
            }
            
            ErrManager.getDefault ().notify (new Exception ("notifyModified: " + notifyModified));
            
            boolean retValue;            
            retValue = super.notifyModified();
            return retValue;
        }
        
        protected javax.swing.text.EditorKit createEditorKit() {
            javax.swing.text.EditorKit k = NotifyModifiedTest.this.createEditorKit ();
            if (k != null) {
                return k;
            } 
            return super.createEditorKit ();
        }

        public void assertModified (boolean modified, String msg) {
            assertEquals (msg, modified, isModified ());
        }
    }
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }
        
        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            ic.add(new ErrManager());
        }
    }

    private static final class ErrManager extends org.openide.ErrorManager {
        static final StringBuffer messages = new StringBuffer();
        static int nOfMessages;
        static final String DELIMITER = ": ";
        static final String WARNING_MESSAGE_START = WARNING + DELIMITER;
        /** setup in setUp */
        static java.io.PrintStream log = System.err;
        
        private String prefix;
        
        public ErrManager () {
            prefix = "";
        }
        
        private ErrManager (String pr) {
            this.prefix = pr;
        }
        
        static void resetMessages() {
            messages.delete(0, ErrManager.messages.length());
            nOfMessages = 0;
        }
        
        public void log(int severity, String s) {
            synchronized (ErrManager.messages) {
                nOfMessages++;
                messages.append('['); log.print ('[');
                messages.append(prefix); log.print (prefix);
                messages.append("] - "); log.print ("] - ");
                messages.append(s); log.println (s);
                messages.append('\n'); 
            }
        }
        
        public Throwable annotate(Throwable t, int severity,
                String message, String localizedMessage,
                Throwable stackTrace, java.util.Date date) {
            return t;
        }
        
        public Throwable attachAnnotations(Throwable t, Annotation[] arr) {
            return t;
        }
        
        public org.openide.ErrorManager.Annotation[] findAnnotations(Throwable t) {
            return null;
        }
        
        public org.openide.ErrorManager getInstance(String name) {
            return new ErrManager (name);
        }
        
        public void notify(int severity, Throwable t) {
            StringWriter w = new StringWriter ();
            t.printStackTrace (new java.io.PrintWriter (w));
            log (severity, w.toString ());
        }
    }
}
