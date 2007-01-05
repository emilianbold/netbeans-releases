/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.text;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import junit.framework.*;

import org.netbeans.junit.*;

import org.openide.util.Mutex;
import org.openide.util.Lookup;
import org.openide.util.lookup.*;

/** Testing usage of UserQuestionException in CES.
 *
 * @author Jaroslav Tulach
 */
public class NetworkConnectionLostTest extends NbTestCase
implements CloneableEditorSupport.Env {
    /** the support to work with */
    private CloneableEditorSupport support;
    /** the content of lookup of support */
    private InstanceContent ic;

    
    // Env variables
    private String content = "";
    private boolean valid = true;
    private boolean modified = false;
    private java.util.Date date = new java.util.Date ();
    private java.beans.PropertyChangeSupport propL = new java.beans.PropertyChangeSupport(this);
    private java.beans.VetoableChangeListener vetoL;
    private IOException toThrow;

    
    public NetworkConnectionLostTest (java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(NetworkConnectionLostTest.class);
        
        return suite;
    }
    

    protected void setUp () {
        System.setProperty ("org.openide.util.Lookup", "org.openide.text.NetworkConnectionLostTest$Lkp");
        
        ic = new InstanceContent ();
        support = new CES (this, new AbstractLookup (ic));
    }

    public void testModifyTheFileAndThenPreventItToBeSavedOnClose() throws Exception {
        Document doc = support.openDocument();
        
        doc.insertString(0, "Ahoj", null);
        assertTrue("Modified", support.isModified());
        
        support.open();
        waitEQ();

        JEditorPane[] arr = getPanes();
        assertNotNull("There is one opened pane", arr);
        
        java.awt.Component c = arr[0];
        while (!(c instanceof CloneableEditor)) {
            c = c.getParent();
        }
        CloneableEditor ce = (CloneableEditor)c;

        toThrow = new IOException("NetworkConnectionLost");

        // say save at the end
        DD.toReturn = 0;
        boolean result = ce.close();
        assertFalse("Refused to save due to the exception", result);
        waitEQ();
        
        assertNotNull("There was a question", DD.options);
        
        String txt = doc.getText(0, doc.getLength());
        assertEquals("The right text is there", txt, "Ahoj");
        assertEquals("Nothing has been saved", "", content);
        
        arr = getPanes();
        assertNotNull("Panes are still open", arr);
    }

    private JEditorPane[] getPanes() {
        return Mutex.EVENT.readAccess(new Mutex.Action<JEditorPane[]>() {
            public JEditorPane[] run() {
                return support.getOpenedPanes();
            }
        });
    }
    

    private void waitEQ() throws InterruptedException, InvocationTargetException {
        javax.swing.SwingUtilities.invokeAndWait(new Runnable() { 
            public void run () { 
            } 
        });
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
        if (toThrow != null) {
            throw toThrow;
        }
        
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
        public static int toReturn = -1;
        
        public java.awt.Dialog createDialog(org.openide.DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        public Object notify(org.openide.NotifyDescriptor descriptor) {
            assertNull (options);
            if (toReturn == -1) {
                fail("Not specified what we shall return: " + toReturn);
            }
            options = descriptor.getOptions();
            Object r = options[toReturn];
            toReturn = -1;
            return r;
        }
        
    } // end of DD
    
}
