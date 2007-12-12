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

package org.netbeans.modules.editor.macros.storage.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.options.keymap.KeymapViewModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class MacrosModelTest extends NbTestCase {
    
    public MacrosModelTest (String testName) {
        super (testName);
    }

    protected @Override void setUp() throws Exception {
//        super.setUp();
//        
//        EditorTestLookup.setLookup(
//            new URL[] {
//                getClass().getClassLoader().getResource("org/netbeans/modules/options/editor/mf-layer.xml"),
//                getClass().getClassLoader().getResource("org/netbeans/modules/java/editor/resources/layer.xml"),
//                getClass().getClassLoader().getResource("org/netbeans/modules/defaults/mf-layer.xml"),
//                getClass().getClassLoader().getResource("org/netbeans/modules/editor/settings/storage/layer.xml"), // mime types are detected by fontcolor settings
//                getClass().getClassLoader().getResource("org/netbeans/core/resources/mf-layer.xml"), // for MIMEResolverImpl to work
//                getClass().getClassLoader().getResource("org/netbeans/core/ui/resources/layer.xml")
//            },
//            getWorkDir(),
//            new Object[] {},
//            getClass().getClassLoader()
//        );
//                
//        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
//        // which is needed by Nb EntityCatalog (org.netbeans.core).
//        // Also see the test dependencies in project.xml
//        Main.initializeURLFactory();
        
        // The above doesn't work, because of problems with core/settings. So kick up
        // the whole module system.
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    /* XXX does not compile against new API:
    public void testAddMacro () {
        FileObject f2 = Repository.getDefault().getDefaultFileSystem().findResource("Editors/text/x-java/Settings.settings");
        assertNotNull("No java base options file", f2);
        
        FileObject f1 = Repository.getDefault().getDefaultFileSystem().findResource("Services/MIMEResolver/org-netbeans-modules-editor-settings-storage-mime-resolver.xml");
        assertNotNull("No mime resolver definition file", f1);
        
        FileObject f = Repository.getDefault().getDefaultFileSystem().findResource("Editors/text/x-java/FontsColors/NetBeans/Defaults/org-netbeans-modules-editor-java-token-colorings.xml");
        assertNotNull("No java tokens colorings", f);
        assertEquals("Wrong coloring settings file mime type", "text/x-nbeditor-fontcolorsettings", f.getMIMEType());
        
        // 1) init model
        MacrosModel model = new MacrosModel (
            Lookups.singleton (new KeymapViewModel ())
        );
        Iterator it = model.getMacroNames ().iterator ();
        Map macros = readMacros (model);
        Vector original = clone (model.getShortcutsTableModel ().getDataVector ());
        
        // 2) do some changes
        model.addMacro ("testName", "testValue");
        model.setShortcut (getIndex (model, "testName"), "Alt+Shift+H");
        model.addMacro ("testName2", "testValue2");
        model.setShortcut (getIndex (model, "testName2"), "Alt+Shift+R");
        
        // 3) test changes
        assertFalse (original.equals (model.getShortcutsTableModel ().getDataVector ()));
        assertEquals (original.size () + 2, model.getShortcutsTableModel ().getDataVector ().size ());
        assertEquals (original.size () + 2, model.getMacroNames ().size ());
        assertEquals ("testValue", model.getMacroText ("testName"));
        assertEquals ("testValue2", model.getMacroText ("testName2"));
        assertEquals ("Alt+Shift+H", getShortcut (model, "testName"));
        assertEquals ("Alt+Shift+R", getShortcut (model, "testName2"));
        
        model.applyChanges ();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            // ignore
        }
        
        // 3) test changes
        assertFalse (original.equals (model.getShortcutsTableModel ().getDataVector ()));
        assertEquals (original.size () + 2, model.getShortcutsTableModel ().getDataVector ().size ());
        assertEquals (original.size () + 2, model.getMacroNames ().size ());
        assertEquals ("testValue", model.getMacroText ("testName"));
        assertEquals ("testValue2", model.getMacroText ("testName2"));
        assertEquals ("Alt+Shift+H", getShortcut (model, "testName"));
        assertEquals ("Alt+Shift+R", getShortcut (model, "testName2"));
        
        model = new MacrosModel (
            Lookups.singleton (new KeymapViewModel ())
        );
        
        // 3) test changes
        assertFalse (original.equals (model.getShortcutsTableModel ().getDataVector ()));
        assertEquals (original.size () + 2, model.getShortcutsTableModel ().getDataVector ().size ());
        assertEquals (original.size () + 2, model.getMacroNames ().size ());
        assertEquals ("testValue", model.getMacroText ("testName"));
        assertEquals ("testValue2", model.getMacroText ("testName2"));
        assertEquals ("Alt+Shift+H", getShortcut (model, "testName"));
        assertEquals ("Alt+Shift+R", getShortcut (model, "testName2"));
    }
    
    private String getShortcut (MacrosModel model, String macroName) {
        Iterator it = model.getShortcutsTableModel ().getDataVector ().
            iterator ();
        while (it.hasNext ()) {
            Vector line = (Vector) it.next ();
            if (line.get (0).equals (macroName))
                return (String) line.get (1);
        }
        return null;
    }
    
    private Vector clone (Vector v) {
        Iterator it = v.iterator ();
        Vector result = new Vector ();
        while (it.hasNext ()) {
            Vector line = (Vector) it.next ();
           result.add (line.clone ());
        }
        return result;
    }
    
    private int getIndex (MacrosModel model, String macroName) {
        Vector data = model.getShortcutsTableModel ().getDataVector ();
        int i, k = data.size ();
        for (i = 0; i < k; i++) {
            Vector line = (Vector) data.get (i);
            if (macroName.equals (line.get (0))) return i;
        }
        return -1;
    }
    
    private Map readMacros (MacrosModel model) {
        Iterator it = model.getMacroNames ().iterator ();
        Map macros = new HashMap ();
        while (it.hasNext ()) {
            String macroName = (String) it.next ();
            macros.put (
                macroName,
                model.getMacroText (macroName)
            );
        }
        return macros;
    }
     */
}
