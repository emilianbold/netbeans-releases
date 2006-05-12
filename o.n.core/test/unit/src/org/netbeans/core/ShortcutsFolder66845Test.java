/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.util.Date;
import javax.swing.text.Keymap;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

/**
 * Tests shortcuts folder to ensure it handles wildcard keystrokes correctly. 
 */
public class ShortcutsFolder66845Test extends NbTestCase {
    private ErrorManager err;
    private Keymap keymap;

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public ShortcutsFolder66845Test(String s) {
        super(s);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(EM.class, NbKeymap.class);
        Main.initializeURLFactory ();
        keymap = Lookup.getDefault().lookup(Keymap.class);
        
        assertNotNull("There is a keymap", keymap);
        ShortcutsFolder.initShortcuts ();
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
    }
    
    public void testApplyChangeToFactoryActionIssue49597 () throws Exception {
        final FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        final FileObject shortcuts = FileUtil.createData(fs.getRoot(), "Keymaps/NetBeans/org-nb-Neznam.instance");

        ShortcutsFolder.waitFinished ();


        assertEquals("One notification to error manager", 1, EM.cnt);
        assertEquals("Severity informational", EM.INFORMATIONAL, EM.sev);
    }

    public static final class EM extends ErrorManager {
        public static int cnt;
        public static int sev;

        public EM() {
        }

        public Throwable attachAnnotations(Throwable t, ErrorManager.Annotation[] arr) {
            return t;
        }

        public ErrorManager.Annotation[] findAnnotations(Throwable t) {
            return null;
        }

        public Throwable annotate(Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return t;
        }

        public void notify(int severity, Throwable t) {
            cnt++;
            sev = severity;
        }

        public void log(int severity, String s) {
        }

        public ErrorManager getInstance(String name) {
            return this;
        }
    }
}
