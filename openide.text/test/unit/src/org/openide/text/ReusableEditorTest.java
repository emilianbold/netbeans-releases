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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.text;


import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.*;


/** Testing the behavior of editor reusal framework.
 * The behavior was discussed thoroughly at issue 94607.
 *
 * @author Petr Nejedly
 */
public class ReusableEditorTest extends NbTestCase {
    CES c1, c2, c3;
    
    /**
     * Test ctor
     * @param testName 
     */
    public ReusableEditorTest(java.lang.String testName) {
        super(testName);
    }
            

    /**
     * Prepares few editors at the test dispoition.
     */
    protected void setUp () {
        c1 = createSupport("c1");
        c2 = createSupport("c2");
        c3 = createSupport("c3");
    }

    /**
     * Closes any precreated editors left open.
     */
    @Override
    protected void tearDown() {
        forceClose(c1);
        forceClose(c2);
        forceClose(c3);
    }
    
    /**
     * Test that verifies SHOW_REUSE closes original tab (keeps only one)
     * Scenario:
     * 1. Open first file with SHOW_REUSE
     * 2. Open second file with SHOW_REUSE
     * 3. Verify first is closed
     * 4. Open first file with SHOW_REUSE
     * 5. Verify second is closed
     */
    public void testReuse() {
        openAndCheck(c1, Line.SHOW_REUSE); // 1
        openAndCheck(c2, Line.SHOW_REUSE); // 2
        assertClosed(c1); // 3
        openAndCheck(c1, Line.SHOW_REUSE); // 4
        assertClosed(c2); // 5
    }
    
    /** Test that verifies SHOW_REUSE doesn't reuse modified, even saved tab
     * 1. Open first file with SHOW_REUSE
     * 2. Modify it
     * 3. Open second file with SHOW_REUSE
     * 4. Verify first still open
     * 5. Modify second file
     * 6. Unmodify second file
     * 7. Open third file with SHOW_REUSE
     * 8. Verify second still open
     */
    public void testKeepTouched() {
        openAndCheck(c1, Line.SHOW_REUSE); // 1
        c1.notifyModified(); // 2
        openAndCheck(c2, Line.SHOW_REUSE); // 3
        assertOpened(c1); // 4
        c2.notifyModified(); // 5
        c2.notifyUnmodified(); // 6
        openAndCheck(c3, Line.SHOW_REUSE); // 7
        assertOpened(c2); // 8
        assertOpened(c1);
    }
    
    /** Test that verifies SHOW_REUSE don't consider non-reusable tabs.
     * There are three things tested:
     * A) Don't replace ordinary tabs
     * B) Don't mark ordinary tabs as reusable if switched to
     * C) Keep reusable tab mark even through (B)
     * 
     * Scenario:
     * 1. Open first file using SHOW_GOTO
     * 2. Open second file using SHOW_REUSE
     * 3. Verify first still opened (A)
     * 4. open first using SHOW_REUSE
     * 5. verify second still opened
     * 6. open third file using SHOW_REUSE
     * 7. verify first still opened (B)
     * 8. verify second closed (C)
     */
    public void testLeaveNonreusable() {
        openAndCheck(c1, Line.SHOW_GOTO); // 1
        openAndCheck(c2, Line.SHOW_REUSE); // 2
        assertOpened(c1); // 3
        
        openAndCheck(c1, Line.SHOW_REUSE); // 4
        assertOpened(c2); // 5
        openAndCheck(c3, Line.SHOW_REUSE); // 6
        assertOpened(c1); // 7
        
        assertClosed(c2); // 8
    }
    
    /** Test that verifies SHOW_REUSE_NEW don't close existing reusable tab,
     * but can be reused itself
     * 
     * Scenario:
     * 1. Open first file using SHOW_REUSE
     * 2. Open second file using SHOW_REUSE_NEW
     * 3. Verify first still opened
     * 4. Open third using SHOW_REUSE
     * 5. verify second closed
     */
    public void testReuseNewKeepsOld() {
        openAndCheck(c1, Line.SHOW_REUSE); // 1
        openAndCheck(c2, Line.SHOW_REUSE_NEW); // 2
        assertOpened(c1); // 3
        openAndCheck(c3, Line.SHOW_REUSE); // 4
        assertClosed(c2); // 5
    }

    /**
     * Test that specifies behaviour of SHOW_REUSE_NEW in case currently
     * reusable tab is not the selected one.
     * 
     * Scenario:
     * 1. Open first file using SHOW_REUSE
     * 2. Open second file using SHOW_GOTO
     * 3. Open third file using SHOW_REUSE_NEW
     * 4. Verify first still open.
     */
    public void testReuseNewKeepsOldEvenWhenNotFocused() {
        openAndCheck(c1, Line.SHOW_REUSE); // 1
        openAndCheck(c2, Line.SHOW_GOTO); // 2
        openAndCheck(c3, Line.SHOW_REUSE_NEW); // 3
        assertOpened(c1); // 4
    }
     
    private CES createSupport(String txt) {
        Env env = new Env();
        env.content = txt;
        CES c = new CES(env, Lookups.singleton(txt));
        env.support = c;
        return c;
    }
    
    private void openAndCheck(final CES ces, final int mode) {
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            public Void run() {
                ces.getLineSet().getCurrent(0).show(mode);
                return null;
            }

        });
        assertOpened(ces);
    }

    private void forceClose(CES ces) {
        if (ces.isModified()) ces.notifyUnmodified();
        ces.close();
    }

    private void assertClosed(CES ces) {
        assertEquals(0, getOpenedCount(ces));
    }

    private void assertOpened(CES ces) {
        assertEquals(1, getOpenedCount(ces));
    }

    private int getOpenedCount(final CES ces) {
        return Mutex.EVENT.readAccess(new Mutex.Action<Integer>() {
            public Integer run() {
                JEditorPane[] panes = ces.getOpenedPanes();
                return panes == null ? 0 : panes.length;
            }
        });
    }
    
    
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    private class Env implements CloneableEditorSupport.Env {
        // Env variables
        private String content = "";
        private boolean valid = true;
        private boolean modified = false;
        private java.util.Date date = new java.util.Date ();
        private List<PropertyChangeListener> propL = new ArrayList<PropertyChangeListener>();
        private java.beans.VetoableChangeListener vetoL;
        /** the support to work with */
        CloneableEditorSupport support;

        public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
            propL.add (l);
        }    
        public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
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
            modified = true;
        }

        public void unmarkModified() {
            modified = false;
        }
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
