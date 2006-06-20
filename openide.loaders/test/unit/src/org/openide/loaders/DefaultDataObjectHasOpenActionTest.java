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

package org.openide.loaders;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/** DefaultDataObject is supposed to have open operation that shows the text
 * editor or invokes a dialog with questions.
 *
 * @author  Jaroslav Tulach
 */
public final class DefaultDataObjectHasOpenActionTest extends NbTestCase {

    private FileSystem lfs;
    private DataObject obj;

    public DefaultDataObjectHasOpenActionTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        // initialize modules
        Lookup.getDefault().lookup(ModuleInfo.class);

        String fsstruct [] = new String [] {
            "AA/a.test"
        };

        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);
        Repository.getDefault().addFileSystem(lfs);

        FileObject fo = lfs.findResource("AA/a.test");
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), DefaultDataObject.class);

        assertFalse("Designed to run outside of AWT", SwingUtilities.isEventDispatchThread());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        Repository.getDefault().removeFileSystem(lfs);
    }

    public void testOpenActionIsAlwaysFirst() throws Exception {
        Node n = obj.getNodeDelegate();

        assertEquals(
                "Open action is the default one",
                OpenAction.get(OpenAction.class),
                n.getPreferredAction()
                );

        Action[] actions = n.getActions(false);
        assertTrue("There are some actions", actions.length > 1);

        assertEquals(
                "First one is open",
                OpenAction.get(OpenAction.class),
                actions[0]
                );
    }

}
