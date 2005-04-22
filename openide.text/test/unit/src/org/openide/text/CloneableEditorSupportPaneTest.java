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
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import junit.framework.*;

import org.netbeans.junit.*;

import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.*;
import org.openide.windows.CloneableTopComponent;


/** Testing different features of CloneableEditorSupport
 *
 * @author Jaroslav Tulach
 */
public class CloneableEditorSupportPaneTest extends NbTestCase implements CloneableEditorSupport.Env {
    /** the support to work with */
    private CloneableEditorSupport support;
    private CloneableEditorSupport support2;
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

    
    public CloneableEditorSupportPaneTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(CloneableEditorSupportPaneTest.class);
        
        return suite;
    }
    

    protected void setUp () {
        ic = new InstanceContent ();
        support = new CES (this, new AbstractLookup (ic));
        support2 = new CES2(this, new AbstractLookup(new InstanceContent ()));
    }
    
    public void testGetOpenedPanes () throws Exception {
        content = "Ahoj\nMyDoc";
        javax.swing.text.Document doc = support.openDocument ();
        support.open();
        Line line = support.getLineSet().getCurrent(0);
        line.show(Line.SHOW_SHOW);
        JEditorPane[] panes = support.getOpenedPanes();
        assertNotNull(panes);
        assertEquals(1, panes.length);
        assertNotNull(instance);
        assertTrue(instance.activated);
                
    }
  
     public void testGetOpenedPanes2ForSeparatePane() throws Exception {
        content = "Ahoj\nMyDoc";
        javax.swing.text.Document doc = support2.openDocument ();
        support2.open();
        Line line = support2.getLineSet().getCurrent(0);
        line.show(Line.SHOW_SHOW);
        JEditorPane[] panes = support2.getOpenedPanes();
        assertNotNull(panes);
        assertEquals(1, panes.length);
        assertNotNull(instance2);
                
    }

    
    public void testCreateCloneableTopComponent() throws Exception {
        CloneableTopComponent comp = support.createCloneableTopComponent();
        assertNotNull(comp);
        assertEquals(MyPane.class, comp.getClass());
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
//        assertNull ("This is the first veto listener", vetoL);
        vetoL = l;
    }
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
//        assertEquals ("Removing the right veto one", vetoL, l);
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
        modified = false;
    }

    protected boolean runInEQ() {
        return true;
    }
    
    /** Implementation of the CES */
    private static class CES extends CloneableEditorSupport {
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
        
        protected org.openide.text.CloneableEditorSupport.Pane createPane() {
            instance = new MyPane();
            return instance;
        }
        
    }
    
    private static MyPane instance;
    
    private static final class MyPane extends CloneableTopComponent implements CloneableEditorSupport.Pane {
        
        private CloneableTopComponent tc;
        private JEditorPane pane;
        
        MyPane() {
            pane = new JEditorPane();
            
        }
        
        public org.openide.windows.CloneableTopComponent getComponent() {
            return this;
        }
        
        public javax.swing.JEditorPane getEditorPane() {
            return pane;
        }
        
        public void updateName() {
        }
        
        public boolean activated = false;
        public void requestActive() {
            super.requestActive();
            activated = true;
        }
        
       /**
         * callback for the Pane implementation to adjust itself to the openAt() request.
         */
        public void ensureVisible() {
            open();
            requestVisible();
        }        
    }
    
    
//-------------------------------------------------------------------------------
//-------------------------------------------------------------------------------
    
    private static class CES2 extends CES {
        public CES2 (Env env, Lookup l) {
            super (env, l);
        }
        
        protected org.openide.text.CloneableEditorSupport.Pane createPane() {
            instance2 = new MyPaneNonNonTC();
            return instance2;
        }
    }
    
    private static MyPaneNonNonTC instance2;
    
    
    private static final class MyPaneNonNonTC implements CloneableEditorSupport.Pane {
        
        private CloneableTopComponent tc;
        private JEditorPane pane;
        
        MyPaneNonNonTC() {
            pane = new JEditorPane();
            tc = new TC();
            
        }
        
        public org.openide.windows.CloneableTopComponent getComponent() {
            return tc;
        }
        
        public javax.swing.JEditorPane getEditorPane() {
            return pane;
        }
        
        public void updateName() {
        }
        
       public void ensureVisible() {
            tc.open();
            tc.requestVisible();
        }                
        
    }
    
    private static class TC extends CloneableTopComponent {
        
        
        public void requestActive() {
            super.requestActive();
        }

    }
    
}
