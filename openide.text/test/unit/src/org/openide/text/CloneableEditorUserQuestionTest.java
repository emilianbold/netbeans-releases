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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import junit.framework.*;

import org.netbeans.junit.*;
import org.openide.NotifyDescriptor;

import org.openide.util.Lookup;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.*;


/** Testing usage of UserQuestionException in CES.
 *
 * @author Jaroslav Tulach
 */
public class CloneableEditorUserQuestionTest extends NbTestCase 
implements CloneableEditorSupport.Env {
    /** the support to work with */
    private CloneableEditorSupport support;
    /** the content of lookup of support */
    private InstanceContent ic;

    
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private String cannotBeModified;
    private java.util.Date date = new java.util.Date ();
    private java.util.List/*<java.beans.PropertyChangeListener>*/ propL = new java.util.ArrayList ();
    private java.beans.VetoableChangeListener vetoL;
    private IOException toThrow;

    
    public CloneableEditorUserQuestionTest (java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(CloneableEditorUserQuestionTest.class);
        
        return suite;
    }
    

    protected void setUp () {
        System.setProperty ("org.openide.util.Lookup", "org.openide.text.CloneableEditorUserQuestionTest$Lkp");
        
        ic = new InstanceContent ();
        support = new CES (this, new AbstractLookup (ic));
    }

    
    public void testExceptionThrownWhenDocumentIsBeingReadInAWT () throws Exception {
        class Run implements Runnable {
            public Exception ex;
            public Error err;
            public void run () {
                
                try {
                    doExceptionThrownWhenDocumentIsBeingRead ();
                } catch (Exception ex) {
                    this.ex = ex;
                } catch (Error err) {
                    this.err = err;
                }
            }
        }
        Run r = new Run ();
        SwingUtilities.invokeAndWait (r);
        if (r.ex != null) throw r.ex;
        if (r.err != null) throw r.err;
    }

    public void testExceptionThrownWhenDocumentIsBeingRead () throws Exception {
        assertFalse (SwingUtilities.isEventDispatchThread ());
        doExceptionThrownWhenDocumentIsBeingRead ();
    }
    
    
    public void testOpenDocumentIsLoadedUsingIOException() throws Exception{
        doOpenDocumentIsLoaded (new IOException ("Plain I/O exc"));
    }
    
    public void testOpenDocumentIsLoadedUsingUserQuestionException() throws Exception{
        class MyEx extends UserQuestionException {
            private int confirmed;
            
            public String getLocalizedMessage () {
                return "locmsg";
            }
            
            public String getMessage () {
                return "msg";
            }
            
            public void confirmed () {
                confirmed++;
                toThrow = null;
            }
        }
        doOpenDocumentIsLoaded (new MyEx ());
    }
    
    private void doOpenDocumentIsLoaded (IOException my) throws Exception {
        toThrow = my;
        try{
            support.openDocument();
            fail ("Document should not be loaded, we throw an exception");
        }
        catch (IOException e){
            assertSame ("The expected exception", my, e);
        }
        
        assertNull ("No document", support.getDocument());
        assertFalse ("Not loaded", support.isDocumentLoaded());

        toThrow = null;
        support.openDocument ();
        
        assertNotNull ("We can later open the document", support.getDocument ());
        assertTrue ("And it is correctly marked as loaded", support.isDocumentLoaded ());
    }
    
    private void doExceptionThrownWhenDocumentIsBeingRead () throws Exception {
        class MyEx extends UserQuestionException {
            private int confirmed;
            
            public String getLocalizedMessage () {
                return "locmsg";
            }
            
            public String getMessage () {
                return "msg";
            }
            
            public void confirmed () {
                confirmed++;
                toThrow = null;
            }
        }
        
        MyEx my = new MyEx ();
        toThrow = my;

        DD.toReturn = org.openide.NotifyDescriptor.NO_OPTION;
        support.open ();
        
        if (!SwingUtilities.isEventDispatchThread ()) {
            javax.swing.SwingUtilities.invokeAndWait (new Runnable () { public void run () {} });
        }
        
        assertNotNull ("Some otions", DD.options);
        assertEquals ("Two options", 2, DD.options.length);
        assertEquals ("Yes", NotifyDescriptor.YES_OPTION, DD.options[0]);
        assertEquals ("No", NotifyDescriptor.NO_OPTION, DD.options[1]);
        assertEquals ("confirmed not called", 0, my.confirmed);
        
        assertNull ("Still no document", support.getDocument ());
        
        DD.options = null;
        DD.toReturn = NotifyDescriptor.YES_OPTION;
        support.open ();

        if (!SwingUtilities.isEventDispatchThread ()) {
            javax.swing.SwingUtilities.invokeAndWait (new Runnable () { public void run () {} });
        }
        
        assertEquals ("confirmed called", 1, my.confirmed);
        assertNotNull ("Some otions", DD.options);
        assertEquals ("Two options", 2, DD.options.length);
        assertEquals ("Yes", NotifyDescriptor.YES_OPTION, DD.options[0]);
        assertEquals ("No", NotifyDescriptor.NO_OPTION, DD.options[1]);
        DD.options = null;
        
        assertNotNull ("Document opened", support.getDocument ());
        
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
        if (toThrow != null) {
            throw toThrow;
        }
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
            final String notify = cannotBeModified;
            IOException e = new IOException () {
                public String getLocalizedMessage () {
                    return notify;
                }
            };
            org.openide.ErrorManager.getDefault ().annotate (e, cannotBeModified);
            throw e;
        }
        
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }

    /** Implementation of the CES */
    private static final class CES extends CloneableEditorSupport {
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
        
    } // end of CES
    
    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new DD ());
        }
    }

    /** Our own dialog displayer.
     */
    private static final class DD extends org.openide.DialogDisplayer {
        public static Object[] options;
        public static Object toReturn;
        
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        public Object notify(org.openide.NotifyDescriptor descriptor) {
            assertNull (options);
            assertNotNull (toReturn);
            options = descriptor.getOptions();
            Object r = toReturn;
            toReturn = null;
            return r;
        }
        
    } // end of DD
    
}
