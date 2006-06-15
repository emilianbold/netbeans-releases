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

import java.util.logging.Level;
import javax.swing.text.Keymap;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

public class ShortcutsFolder66845Test extends NbTestCase {

    public ShortcutsFolder66845Test(String s) {
        super(s);
    }

    private Keymap keymap;
    private CharSequence logs;

    protected void setUp() throws Exception {
        Main.initializeURLFactory ();
        keymap = Lookup.getDefault().lookup(Keymap.class);
        assertNotNull("There is a keymap", keymap);
        assertEquals("of correct type", NbKeymap.class, keymap.getClass());
        ShortcutsFolder.initShortcuts ();
        logs = Log.enable(ShortcutsFolder.class.getName(), Level.WARNING);
    }

    public void testLogging() throws Exception {
        final FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        final FileObject shortcuts = FileUtil.createData(fs.getRoot(), "Keymaps/NetBeans/org-nb-Neznam.instance");

        ShortcutsFolder.waitFinished ();

        assertTrue("got message in " + logs, logs.toString().contains("Neznam"));
    }

}
