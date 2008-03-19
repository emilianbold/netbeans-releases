/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.core;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.*;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.loaders.Environment;
import org.openide.loaders.InstanceSupport;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;

public class ShortcutsFolderTest extends NbTestCase {
    private ErrorManager err;
    private Keymap keymap;
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public ShortcutsFolderTest(String s) {
        super(s);
    }
    
    @Override
    protected Level logLevel() {
        return Level.ALL;
    }
    
    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(ENV.class);

        Main.initializeURLFactory ();
        keymap = Lookup.getDefault().lookup(Keymap.class);
        
        assertNotNull("There is a keymap", keymap);
        ShortcutsFolder.initShortcuts ();
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
    }
    
    public void testApplyChangeToFactoryActionIssue49597 () throws Exception {
        final FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        final FileObject shortcuts = fs.getRoot ().getFileObject ("Shortcuts");
        FileObject inst = FileUtil.createData (fs.getRoot (), "/Actions/Tools/TestAction.instance");
        TestAction action = new TestAction ();
        inst.setAttribute ("instanceCreate", action);
        
        Reference<?> ref = new WeakReference<Object>(inst);
        inst = null;
        assertGC ("File can disappear", ref);

//        ShortcutsFolder.waitFinished ();

        assertEquals ("Nothing registered", Collections.EMPTY_LIST, Arrays.asList (keymap.getBoundActions ()));
        
        final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

        class R implements FileSystem.AtomicAction {
            FileObject inst2;
            
            public void run() throws IOException {
                inst2 = FileUtil.createData (fs.getRoot (), "/Shortcuts/CA-F9.shadow");
                inst2.setAttribute ("originalFile", "/Actions/Tools/TestAction.instance");
            }
        }
        R run = new R();
        fs.runAtomicAction(run);

        ShortcutsFolder.waitFinished ();
        err.log("ShortcutsFolder.waitFinished");

        FileObject[] arr = shortcuts.getChildren ();
        err.log("children are here");
        
        assertEquals ("One element is there", 1, arr.length);
        DataObject obj = DataObject.find (arr[0]);
        err.log("Object is here" + obj);
        
        assertEquals("It is DataShadow", DataShadow.class, obj.getClass());

        Object a = keymap.getAction (stroke);
        assertNotNull ("There is an action", a);
        assertEquals ("It is test action", TestAction.class, a.getClass ());
    }

    @RandomlyFails
    public void testShortcutsForDifferentFilesThanInstanceOrShadows () throws Exception {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        FileObject inst = FileUtil.createData (fs.getRoot (), "/Shortcuts/C-F11.xml");

        FileLock lock = inst.lock ();
        java.io.PrintStream ps = new java.io.PrintStream (inst.getOutputStream (lock));
        ps.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ps.println ("<project default=\"run\" name=\"Build everything.\">");
        ps.println ("<target name=\"run\">");
        ps.println ("<ant antfile=\"SampleProject.xml\" inheritall=\"false\" target=\"all\"/>");
        ps.println ("</target>");
        ps.println ("</project>");
        ps.close();
        lock.releaseLock ();
        
        DataObject obj = DataObject.find (inst);
        assertEquals ("XML Data object", XMLDataObject.class, obj.getClass());
        InstanceCookie ic = obj.getCookie(InstanceCookie.class);
        assertNotNull ("Has cookie", ic);

        final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F11, KeyEvent.CTRL_MASK);
        
        ShortcutsFolder.waitFinished ();

        Action action = keymap.getAction(stroke);
        if (action == null) {
            fail("There should be some action for " + stroke + " in:\n" + keymap);
        }
        
        inst.delete ();
        ShortcutsFolder.waitFinished ();
        action = keymap.getAction (stroke);
        assertNull ("Action removed", action);
    }
    
    public static class TestAction extends AbstractAction {
        public void actionPerformed (ActionEvent ae) {}
    }
    
    public static class ENV extends Object implements Environment.Provider {
        public Lookup getEnvironment(DataObject obj) {
            if (obj instanceof XMLDataObject) {
                try {
                    Document doc = ((XMLDataObject) obj).getDocument();
                    if (doc.getDocumentElement().getNodeName().equals ("project")) {
                        return Lookups.singleton(
                            new InstanceSupport.Instance(
                                new TestAction ()
                            )
                        );
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    fail ("No exception: " + ex.getMessage());
                }
            }
            return Lookup.EMPTY;
        }
    }
    
}
