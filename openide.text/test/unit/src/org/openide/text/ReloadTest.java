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
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.text.CloneableEditorSupport;


/** Test to simulate problem #46885
 * @author  Jaroslav Tulach
 */
public class ReloadTest extends NbTestCase 
implements CloneableEditorSupport.Env {
    /** the support to work with */
    private transient CES support;

    // Env variables
    private transient String content = "";
    private transient boolean valid = true;
    private transient boolean modified = false;
    /** if not null contains message why this document cannot be modified */
    private transient String cannotBeModified;
    private transient java.util.Date date = new java.util.Date ();
    private transient java.beans.PropertyChangeSupport propL = new java.beans.PropertyChangeSupport (this);
    private transient java.beans.VetoableChangeListener vetoL;
    
    public ReloadTest (String s) {
        super(s);
    }

    /** For subclasses to change to more nb like kits. */
    protected javax.swing.text.EditorKit createEditorKit () {
        return null;
    }
    
    
    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
    }
    
    protected void tearDown () {
    }
    
    protected boolean runInEQ() {
        return false;
    }
    

    public void testRefreshProblem46885 () throws Exception {
        StyledDocument doc = support.openDocument ();
        
        doc.insertString (0, "A text", null);
        support.saveDocument ();
        
        content = "New";
        propL.firePropertyChange (CloneableEditorSupport.Env.PROP_TIME, null, null);
        
        waitAWT ();
        
        String s = doc.getText (0, doc.getLength ());
        assertEquals ("Text has been updated", content, s);
        
        
        long oldtime = System.currentTimeMillis ();
        doc.insertString (0, "A text", null);
        support.saveDocument ();
        s = doc.getText (0, doc.getLength ());
        
        content = "NOT TO be loaded";
        propL.firePropertyChange (CloneableEditorSupport.Env.PROP_TIME, null, new java.util.Date (oldtime));
        
        waitAWT ();
        
        String s1 = doc.getText (0, doc.getLength ());
        assertEquals ("Text has not been updated", s, s1);
    }

    private void waitAWT () throws Exception {
        assertFalse ("Not in AWT", SwingUtilities.isEventDispatchThread ());
        SwingUtilities.invokeAndWait (new Runnable () { public void run () { }});
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
        return null;
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
    private final class CES extends CloneableEditorSupport {
        public CES (Env env, org.openide.util.Lookup l) {
            super (env, l);
        }
        
        public org.openide.windows.CloneableTopComponent.Ref getRef () {
            return allEditors;
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

        protected javax.swing.text.EditorKit createEditorKit () {
            javax.swing.text.EditorKit retValue = ReloadTest.this.createEditorKit ();
            if (retValue == null) {
                retValue = super.createEditorKit();
            }
            return retValue;
        }

        
    }
}
