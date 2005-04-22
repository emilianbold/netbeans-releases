/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.openide.text;


import java.io.File;
import java.io.IOException;
import javax.swing.text.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.text.CloneableEditorSupport;
import org.openide.text.FilterDocument;
import org.openide.text.NbDocument;

/**
 * Simulates issue 46981. Editor locks the document, but somebody else closes it
 * while it is working on it and a deadlock occurs.
 * @author  Petr Nejedly, Jaroslav Tulach
 */
public class DocumentCannotBeClosedWhenReadLockedTest extends NbTestCase implements CloneableEditorSupport.Env {
    /** the support to work with */
    private CES support;
    // Env variables
    private String content = "Hello";
    private boolean valid = true;
    private boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private String cannotBeModified;
    private java.util.Date date = new java.util.Date ();
    private java.util.List/*<java.beans.PropertyChangeListener>*/ propL = new java.util.ArrayList ();
    private java.beans.VetoableChangeListener vetoL;
    
    
    /** lock to use for communication between AWT & main thread */
    private Object LOCK = new Object ();
    
    /** Creates new TextTest */
    public DocumentCannotBeClosedWhenReadLockedTest(String s) {
        super(s);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(DocumentCannotBeClosedWhenReadLockedTest.class));
    }

    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
    }
    
    public void testReadLockTheDocumentAndThenTryToCreateAPositionInItMeanWhileLetOtherThreadCloseAComponent () throws Exception {
        StyledDocument doc = support.openDocument ();
        final CloneableEditorSupport.Pane pane = support.openAt (support.createPositionRef (0, javax.swing.text.Position.Bias.Forward), 0);
        assertNotNull (pane);
        assertNotNull ("TopComponent is there", pane.getComponent ());
        
        class DoWork implements Runnable {
            private boolean startedAWT;
            private boolean finishedAWT;
            private boolean startedWork;
            private boolean finishedWork;
            
            public void run () {
                if (javax.swing.SwingUtilities.isEventDispatchThread ()) {
                    doWorkInAWT ();
                } else {
                    doWork ();
                }
            }
             
            private void doWorkInAWT () {
                startedAWT = true;
                synchronized (LOCK) {
                    // let the main thread know that it can do the rendering
                    LOCK.notify();
                }
                
                try {
                    Thread.sleep (500);
                } catch (InterruptedException ex) {
                    fail (ex.getMessage ());
                }
                
                // this will call into notifyUnmodified.
                pane.getComponent ().close ();
                assertFalse ("The document should be marked unmodified now", modified);
                synchronized (this) {
                    finishedAWT = true;
                    notifyAll ();
                }
            }
            
            private void doWork () {
                startedWork = true;
                synchronized (LOCK) {
                    try {
                        LOCK.wait ();
                    } catch (InterruptedException ex) {
                        throw new org.netbeans.junit.AssertionFailedErrorException (ex);
                    }
                }
                
                // now the document is blocked in after close, try to ask for a position
                support.createPositionRef (0, javax.swing.text.Position.Bias.Forward);
                finishedWork = true;
            }
            
            public synchronized void waitFinishedAWT () throws InterruptedException {
                int cnt = 5;
                while (!finishedAWT && cnt-- > 0) {
                    wait (500);
                }
                
                if (!finishedAWT) {
                    fail ("AWT has not finsihed");
                }
            }
            
        }
        DoWork doWork = new DoWork ();
        
        
        synchronized (LOCK) {
            javax.swing.SwingUtilities.invokeLater (doWork);
            LOCK.wait ();
        }
            
        
        doc.render (doWork);
        
        // wait for AWT work to finish
        javax.swing.SwingUtilities.invokeAndWait (new Runnable () { public void run () {}});
        
        assertTrue ("AWT started", doWork.startedAWT);
        assertTrue ("Work started", doWork.startedWork);

        doWork.waitFinishedAWT ();
        assertTrue ("Work done", doWork.finishedWork);
    }

    public void testReadLockTheDocumentAndThenTryToCreateAPositionInItMeanWhileLetOtherThreadCloseIt () throws Exception {
        class DoWork implements Runnable {
            private boolean finishedAWT;
            private boolean finishedWork;
            
            public void run () {
                if (javax.swing.SwingUtilities.isEventDispatchThread ()) {
                    doWorkInAWT ();
                } else {
                    doWork ();
                }
            }
             
            private void doWorkInAWT () {
                synchronized (LOCK) {
                    // let the main thread know that it can do the rendering
                    LOCK.notify();
                }
                
                try {
                    Thread.sleep (500);
                } catch (InterruptedException ex) {
                    fail (ex.getMessage ());
                }
                
                // this will call into notifyUnmodified.
                support.close ();
                assertFalse ("The document should be marked unmodified now", modified);
                synchronized (this) {
                    finishedAWT = true;
                    notifyAll ();
                }
            }
            
            private void doWork () {
                synchronized (LOCK) {
                    try {
                        LOCK.wait ();
                    } catch (InterruptedException ex) {
                        throw new org.netbeans.junit.AssertionFailedErrorException (ex);
                    }
                }
                
                // now the document is blocked in after close, try to ask for a position
                support.createPositionRef (0, javax.swing.text.Position.Bias.Forward);
                finishedWork = true;
            }
            
            public synchronized void waitFinishedAWT () throws InterruptedException {
                int cnt = 5;
                while (!finishedAWT && cnt-- > 0) {
                    wait (500);
                }
                
                if (!finishedAWT) {
                    fail ("AWT has not finsihed");
                }
            }
        }
        DoWork doWork = new DoWork ();
        StyledDocument doc = support.openDocument ();
        
        synchronized (LOCK) {
            javax.swing.SwingUtilities.invokeLater (doWork);
            LOCK.wait ();
        }
            
        
        doc.render (doWork);
        
        // maybe this needs to invokeAndWait something empty in AWT?
        doWork.waitFinishedAWT ();
        assertTrue ("Work done", doWork.finishedWork);
    }
    
    
    
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.add (l);
    }    
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propL.remove (l);
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
        if (cannotBeModified != null) {
            IOException e = new IOException ();
            org.openide.ErrorManager.getDefault ().annotate (e, cannotBeModified);
            throw e;
        }
        
        modified = true;
    }
    
    public void unmarkModified() {
        synchronized (LOCK) {
            LOCK.notify ();
            try {
                LOCK.wait (500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        modified = false;
    }

    /** Implementation of the CES */
    private final class CES extends CloneableEditorSupport {
        
        public CES (Env env, org.openide.util.Lookup l) {
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
        
        protected EditorKit createEditorKit () {
            return new NbLikeEditorKit ();
        }
    } // end of CES
}
