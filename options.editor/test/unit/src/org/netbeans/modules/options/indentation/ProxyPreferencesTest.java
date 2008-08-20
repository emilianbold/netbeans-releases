/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.options.indentation;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vita
 */
public class ProxyPreferencesTest extends NbTestCase {

    public ProxyPreferencesTest(String name) {
        super(name);
    }

    public void testSimpleRead() {
        Preferences orig = Preferences.userRoot().node(getName());
        orig.put("key-1", "value-1");

        Preferences test = ProxyPreferences.get(orig);
        assertEquals("Wrong value", "value-1", test.get("key-1", null));
    }
    
    public void testSimpleWrite() {
        Preferences orig = Preferences.userRoot().node(getName());
        assertNull("Original contains value", orig.get("key-1", null));

        Preferences test = ProxyPreferences.get(orig);
        test.put("key-1", "xyz");
        assertEquals("Wrong value", "xyz", test.get("key-1", null));
    }
    
    public void testSimpleSync() throws BackingStoreException {
        Preferences orig = Preferences.userRoot().node(getName());
        assertNull("Original contains value", orig.get("key-1", null));

        Preferences test = ProxyPreferences.get(orig);
        assertNull("Test should not contains pair", orig.get("key-1", null));

        test.put("key-1", "xyz");
        assertEquals("Test doesn't contain new pair", "xyz", test.get("key-1", null));

        test.sync();
        assertNull("Test didn't rollback pair", test.get("key-1", null));
    }

    public void testSimpleFlush() throws BackingStoreException {
        Preferences orig = Preferences.userRoot().node(getName());
        assertNull("Original contains value", orig.get("key-1", null));

        Preferences test = ProxyPreferences.get(orig);
        assertNull("Test should not contains pair", orig.get("key-1", null));

        test.put("key-1", "xyz");
        assertEquals("Test doesn't contain new pair", "xyz", test.get("key-1", null));

        test.flush();
        assertEquals("Test should still contain the pair", "xyz", test.get("key-1", null));
        assertEquals("Test didn't flush the pair", "xyz", orig.get("key-1", null));
    }
    
    public void testSyncTree1() throws BackingStoreException {
        String [] origTree = new String [] {
            "CodeStyle/profile=GLOBAL",
        };
        String [] newTree = new String [] {
            "CodeStyle/text/x-java/tab-size=2",
            "CodeStyle/text/x-java/override-global-settings=true",
            "CodeStyle/text/x-java/expand-tabs=true",
            "CodeStyle/profile=PROJECT",
        };

        Preferences orig = Preferences.userRoot().node(getName());
        write(orig, origTree);
        checkContains(orig, origTree, "Orig");
        checkNotContains(orig, newTree, "Orig");
        
        Preferences test = ProxyPreferences.get(orig);
        checkEquals("Test should be the same as Orig", orig, test);
        
        write(test, newTree);
        checkContains(test, newTree, "Test");

        test.sync();
        checkContains(orig, origTree, "Orig");
        checkNotContains(orig, newTree, "Orig");
        checkContains(test, origTree, "Test");
        checkNotContains(test, newTree, "Test");
    }

    public void testFlushTree1() throws BackingStoreException {
        String [] origTree = new String [] {
            "CodeStyle/profile=GLOBAL",
        };
        String [] newTree = new String [] {
            "CodeStyle/text/x-java/tab-size=2",
            "CodeStyle/text/x-java/override-global-settings=true",
            "CodeStyle/text/x-java/expand-tabs=true",
            "CodeStyle/profile=PROJECT",
        };

        Preferences orig = Preferences.userRoot().node(getName());
        write(orig, origTree);
        checkContains(orig, origTree, "Orig");
        checkNotContains(orig, newTree, "Orig");
        
        Preferences test = ProxyPreferences.get(orig);
        checkEquals("Test should be the same as Orig", orig, test);
        
        write(test, newTree);
        checkContains(test, newTree, "Test");

        test.flush();
        checkEquals("Test didn't flush to Orig", test, orig);
    }

    public void testRemoveKey() throws BackingStoreException {
        Preferences orig = Preferences.userRoot().node(getName());
        orig.put("key-2", "value-2");
        assertNull("Original contains value", orig.get("key-1", null));
        assertEquals("Original doesn't contain value", "value-2", orig.get("key-2", null));

        Preferences test = ProxyPreferences.get(orig);
        test.put("key-1", "xyz");
        assertEquals("Wrong value", "xyz", test.get("key-1", null));
        
        test.remove("key-1");
        assertNull("Test contains removed key-1", test.get("key-1", null));
        
        test.remove("key-2");
        assertNull("Test contains removed key-2", test.get("key-2", null));

        test.flush();
        assertNull("Test flushed removed key-1", orig.get("key-1", null));
        assertNull("Test.flush did not remove removed key-2", orig.get("key-2", null));
    }
    
    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------
    
    private void write(Preferences prefs, String[] tree) {
        for(String s : tree) {
            int equalIdx = s.lastIndexOf('=');
            assertTrue(equalIdx != -1);
            String value = s.substring(equalIdx + 1);

            String key;
            String nodePath;
            int slashIdx = s.lastIndexOf('/', equalIdx);
            if (slashIdx != -1) {
                key = s.substring(slashIdx + 1, equalIdx);
                nodePath = s.substring(0, slashIdx);
            } else {
                key = s.substring(0, equalIdx);
                nodePath = "";
            }

            Preferences node = prefs.node(nodePath);
            node.put(key, value);
        }
    }

    private void checkContains(Preferences prefs, String[] tree, String prefsId) throws BackingStoreException {
        for(String s : tree) {
            int equalIdx = s.lastIndexOf('=');
            assertTrue(equalIdx != -1);
            String value = s.substring(equalIdx + 1);

            String key;
            String nodePath;
            int slashIdx = s.lastIndexOf('/', equalIdx);
            if (slashIdx != -1) {
                key = s.substring(slashIdx + 1, equalIdx);
                nodePath = s.substring(0, slashIdx);
            } else {
                key = s.substring(0, equalIdx);
                nodePath = "";
            }

            assertTrue(prefsId + " doesn't contain node '" + nodePath + "'", prefs.nodeExists(nodePath));
            Preferences node = prefs.node(nodePath);

            String realValue = node.get(key, null);
            assertNotNull(prefsId + ", '" + nodePath + "' node doesn't contain key '" + key + "'", realValue);
            assertEquals(prefsId + ", '" + nodePath + "' node, '" + key + "' contains wrong value", value, realValue);
        }
    }

    private void checkNotContains(Preferences prefs, String[] tree, String prefsId) throws BackingStoreException {
        for(String s : tree) {
            int equalIdx = s.lastIndexOf('=');
            assertTrue(equalIdx != -1);
            String value = s.substring(equalIdx + 1);

            String key;
            String nodePath;
            int slashIdx = s.lastIndexOf('/', equalIdx);
            if (slashIdx != -1) {
                key = s.substring(slashIdx + 1, equalIdx);
                nodePath = s.substring(0, slashIdx);
            } else {
                key = s.substring(0, equalIdx);
                nodePath = "";
            }

            if (prefs.nodeExists(nodePath)) {
                Preferences node = prefs.node(nodePath);
                String realValue = node.get(key, null);
                if (realValue != null && realValue.equals(value)) {
                    fail(prefsId + ", '" + nodePath + "' node contains key '" + key + "' = '" + realValue + "'");
                }
            }
        }
    }

    private void dump(Preferences prefs, String prefsId) throws BackingStoreException {
        for(String key : prefs.keys()) {
            System.out.println(prefsId + ", " + prefs.absolutePath() + "/" + key + "=" + prefs.get(key, null));
        }
        for(String child : prefs.childrenNames()) {
            dump(prefs.node(child), prefsId);
        }
    }

    private void checkEquals(String msg, Preferences expected, Preferences test) throws BackingStoreException {
        assertEquals("Won't compare two Preferences with different absolutePath", expected.absolutePath(), test.absolutePath());
        
        // check the keys and their values
        for(String key : expected.keys()) {
            String expectedValue = expected.get(key, null);
            assertNotNull(msg + "; Expected:" + expected.absolutePath() + " has no '" + key + "'", expectedValue);
            
            String value = test.get(key, null);
            assertNotNull(msg + "; Test:" + test.absolutePath() + " has no '" + key + "'", value);
            assertEquals(msg + "; Test:" + test.absolutePath() + "/" + key + " has wrong value", expectedValue, value);
        }

        // check the children
        for(String child : expected.childrenNames()) {
            assertTrue(msg + "; Expected:" + expected.absolutePath() + " has no '" + child + "' subnode", expected.nodeExists(child));
            Preferences expectedChild = expected.node(child);

            assertTrue(msg + "; Test:" + test.absolutePath() + " has no '" + child + "' subnode", test.nodeExists(child));
            Preferences testChild = test.node(child);

            checkEquals(msg, expectedChild, testChild);
        }
    }
}
