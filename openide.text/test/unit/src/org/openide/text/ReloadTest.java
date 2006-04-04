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

package org.openide.text;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;

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
    private transient Date date = new Date ();
    private transient PropertyChangeSupport propL = new PropertyChangeSupport (this);
    private transient VetoableChangeListener vetoL;

    private Logger err;
    private CharSequence log;
    
    public ReloadTest (String s) {
        super(s);
    }

    /** For subclasses to change to more nb like kits. */
    protected EditorKit createEditorKit () {
        return null;
    }
    
    
    protected boolean runInEQ() {
        return false;
    }

    protected Level logLevel() {
        return Level.ALL;
    }
    
    
    protected void setUp () {
        support = new CES (this, Lookup.EMPTY);

        log = Log.enable("", Level.ALL);

        
        err = Logger.getLogger(getName());
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
        propL.firePropertyChange (CloneableEditorSupport.Env.PROP_TIME, null, new Date (oldtime));
        
        waitAWT ();
        
        String s1 = doc.getText (0, doc.getLength ());
        assertEquals ("Text has not been updated", s, s1);
    }

    private void waitAWT () throws Exception {
        err.info("wait for AWT begin");
        assertFalse ("Not in AWT", SwingUtilities.isEventDispatchThread ());
        SwingUtilities.invokeAndWait (new Runnable () { public void run () { }});
        err.info("wait for AWT ends");
    }
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        propL.addPropertyChangeListener (l);
    }    
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        propL.removePropertyChangeListener (l);
    }
    
    public synchronized void addVetoableChangeListener(VetoableChangeListener l) {
        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(VetoableChangeListener l) {
        assertEquals ("Removing the right veto one", vetoL, l);
        vetoL = null;
    }
    
    public CloneableOpenSupport findCloneableOpenSupport() {
        return null;
    }
    
    public String getMimeType() {
        return "text/plain";
    }
    
    public Date getTime() {
        return date;
    }
    
    public InputStream inputStream() throws IOException {
        return new ByteArrayInputStream (content.getBytes ());
    }
    public OutputStream outputStream() throws IOException {
        class ContentStream extends ByteArrayOutputStream {
            public void close () throws IOException {
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

    public void markModified() throws IOException {
        if (cannotBeModified != null) {
            final String notify = cannotBeModified;
            IOException e = new IOException () {
                public String getLocalizedMessage () {
                    return notify;
                }
            };
            ErrorManager.getDefault ().annotate (e, cannotBeModified);
            throw e;
        }
        
        modified = true;
    }
    
    public void unmarkModified() {
        modified = false;
    }

    /** Implementation of the CES */
    private final class CES extends CloneableEditorSupport {
        public CES (Env env, Lookup l) {
            super (env, l);
        }
        
        public CloneableTopComponent.Ref getRef () {
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

        protected EditorKit createEditorKit () {
            EditorKit retValue = ReloadTest.this.createEditorKit ();
            if (retValue == null) {
                retValue = super.createEditorKit();
            }
            return retValue;
        }
    }
}
