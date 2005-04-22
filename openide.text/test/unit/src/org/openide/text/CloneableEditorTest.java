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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.WindowManager;


/**
 */
public class CloneableEditorTest extends NbTestCase 
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
    private transient java.util.List/*<java.beans.PropertyChangeListener>*/ propL = new java.util.ArrayList ();
    private transient java.beans.VetoableChangeListener vetoL;
    
    private static CloneableEditorTest RUNNING;
    
    public CloneableEditorTest(String s) {
        super(s);
    }
    
    protected void setUp () {
        support = new CES (this, org.openide.util.Lookup.EMPTY);
        RUNNING = this;
    }
    
    protected void tearDown () {
        RUNNING = null;
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    private Object writeReplace () {
        return new Replace ();
    }
    
    public void testGetOpenedPanesWorksAfterDeserializationIssue39236 () throws Exception {
        support.open ();

        CloneableEditor ed = (CloneableEditor)support.getRef ().getAnyComponent ();
        
        javax.swing.JEditorPane[] panes = support.getOpenedPanes ();
        assertNotNull (panes);
        assertEquals ("One is there", 1, panes.length);
        
        NbMarshalledObject obj = new NbMarshalledObject (ed);
        ed.close ();
        
        panes = support.getOpenedPanes ();
        assertNull ("No panes anymore", panes);
        
        ed = (CloneableEditor)obj.get ();
        
        panes = support.getOpenedPanes ();
        assertNotNull ("One again", panes);
        assertEquals ("One is there again", 1, panes.length);
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
        return RUNNING.support;
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
        
    }

    private static final class Replace implements java.io.Serializable {
        public Object readResolve () {
            return RUNNING;
        }
    }
}
