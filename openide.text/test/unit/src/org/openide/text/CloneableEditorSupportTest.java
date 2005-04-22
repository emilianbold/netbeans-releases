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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import junit.framework.*;

import org.netbeans.junit.*;

import org.openide.util.Lookup;
import org.openide.util.lookup.*;


/** Testing different features of CloneableEditorSupport
 *
 * @author Jaroslav Tulach
 */
public class CloneableEditorSupportTest extends NbTestCase 
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

    
    public CloneableEditorSupportTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(CloneableEditorSupportTest.class);
        
        return suite;
    }
    

    protected void setUp () {
        ic = new InstanceContent ();
        support = new CES (this, new AbstractLookup (ic));
    }
    
    public void testDocumentCanBeRead () throws Exception {
        content = "Ahoj\nMyDoc";
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        
        String s = doc.getText (0, doc.getLength ());
        assertEquals ("Same text as in the stream", content, s);
        
        assertFalse ("No redo", support.getUndoRedo ().canRedo ());
        assertFalse ("No undo", support.getUndoRedo ().canUndo ());
    }
    
    public void testLineLookupIsPropagated () throws Exception {
        content = "Line1\nLine2\n";
        Integer template = new Integer (1);
        ic.add (template); // put anything into the lookup
        
        // in order to set.getLines() work correctly, the document has to be loaded
        support.openDocument();
        
        Line.Set set = support.getLineSet();
        java.util.List list = set.getLines();
        assertEquals ("Three lines", 3, list.size ());
        
        Line l = (Line)list.get (0);
        Integer i = (Integer)l.getLookup ().lookup (Integer.class);
        assertEquals ("The original integer", template, i);
        ic.remove (template);
        i = (Integer)l.getLookup ().lookup (Integer.class);
        assertNull ("Lookup is dynamic, so now there is nothing", i);
    }
    
    
    public void testGetInputStream () throws Exception {
        content = "goes\nto\nInputStream";
        String added = "added before\n";
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        
        // modify the document
        doc.insertString(0, added, null);
        compareStreamWithString(support.getInputStream(), added + content);
    }
    
    public void testGetInputStreamWhenClosed () throws Exception {
        content = "basic\ncontent";
        compareStreamWithString(support.getInputStream(), content);
        // we should be doing this with the document still closed 
        assertNull("The document is supposed to be still closed", support.getDocument ());
    }
    
    public void testDocumentCannotBeModified () throws Exception {
        content = "Ahoj\nMyDoc";
        cannotBeModified = "No, you cannot modify this document in this test";
        
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        
        assertFalse ("Nothing to undo", support.getUndoRedo ().canUndo ());
        
        // this should not be allowed
        doc.insertString (0, "Kuk", null);
        
        String modifiedForAWhile = doc.getText (0, 3);
        //assertEquals ("For a while the test really starts with Kuk", "Kuk", doc.getText (0, 3));
        
        assertFalse ("The document cannot be modified", support.getUndoRedo ().canUndo ());
        
        String s = doc.getText (0, doc.getLength ());
        assertEquals ("The document is now the same as at the begining", content, s);
        
        assertEquals ("Message has been shown to user in status bar", cannotBeModified, org.openide.awt.StatusDisplayer.getDefault ().getStatusText ());
    }
    
    public void testDocumentCanBeGarbageCollectedWhenClosed () throws Exception {
        content = "Ahoj\nMyDoc";
        javax.swing.text.Document doc = support.openDocument ();
        assertNotNull (doc);
        
        assertTrue ("Document is loaded", support.isDocumentLoaded ());
        assertTrue ("Can be closed without problems", support.close ());
        assertFalse ("Document is not loaded", support.isDocumentLoaded ());
        
        java.lang.ref.WeakReference ref = new java.lang.ref.WeakReference (doc);
        doc = null;
        
        assertGC ("Document can dissapear", ref);
    }

    
    private void compareStreamWithString(InputStream is, String s) throws Exception{
        int i;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        byte b1[] = baos.toByteArray();
        byte b2[] = s.getBytes();
        assertTrue("Same bytes as would result from the string: " + s, Arrays.equals(b1, b2));
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
        
    }
}
