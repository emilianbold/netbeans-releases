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
