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
import javax.swing.SwingUtilities;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import junit.framework.AssertionFailedError;
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
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
    }
    
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

    private ErrorManager err;
    
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
    
    
    protected void setUp () {
        support = new CES (this, Lookup.EMPTY);
        
        assertNotNull("ErrManager has to be in lookup", org.openide.util.Lookup.getDefault().lookup(ErrManager.class));
        ErrManager.resetMessages();
        ErrManager.log = getLog ();
        
        err = ErrManager.getDefault().getInstance(getName());
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
        err.log("wait for AWT begin");
        assertFalse ("Not in AWT", SwingUtilities.isEventDispatchThread ());
        SwingUtilities.invokeAndWait (new Runnable () { public void run () { }});
        err.log("wait for AWT ends");
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
