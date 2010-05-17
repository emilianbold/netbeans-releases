/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.core.startup.preferences;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Radek Matous
 */
public class TestPropertiesStorage extends TestFileStorage {
    private PropertiesStorage storage;
    private NbPreferences pref;
    
    public TestPropertiesStorage(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assertSame(new NbPreferencesFactory().userRoot(), Preferences.userRoot());
        pref = getPreferencesNode();
        assertNotNull(pref);
        storage = (PropertiesStorage)pref.fileStorage;
        assertNotNull(storage);        
    }
    
    @Override
    protected NbPreferences.FileStorage getInstance() {
        return PropertiesStorage.instanceReadOnly("/PropertiesStorageTest/" + getName());//NOI18N);
    }
    
    @Override
    void noFileRepresentationAssertion() throws IOException {
        super.noFileRepresentationAssertion();
        assertNull(((PropertiesStorage)instance).toFolder());
        assertNull(((PropertiesStorage)instance).toPropertiesFile());
    }
    
    @Override
    void fileRepresentationAssertion() throws IOException {
        super.fileRepresentationAssertion();
        assertNotNull(((PropertiesStorage)instance).toFolder());
        assertNotNull(((PropertiesStorage)instance).toPropertiesFile());
    }
    
    private NbPreferences getPreferencesNode() {
        return (NbPreferences)Preferences.userNodeForPackage(TestPropertiesStorage.class).node(getName());
    }
    
    public void testNode() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());
        pref.flush();
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());        
        pref.put("key","value");
    }
    
    public void testNode2() throws BackingStoreException {
        testNode();
        pref.flush();
        assertNotNull(storage.toPropertiesFile());
    }
    
    public void testNode3() throws BackingStoreException {
        testNode();
        pref.flushTask.waitFinished();
        assertNotNull(storage.toPropertiesFile());                
    }

    public void testNode4() throws BackingStoreException {
        pref.node("a");
        testNode();
    }

    public void testNode5() throws BackingStoreException {
        Preferences child = pref.node("a");
        child.put("key","value");
        child.flush();
        assertNotNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                
    }

    public void testRemove() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                
        
        pref.put("key","value");
        pref.flush();
        assertNotNull(storage.toPropertiesFile());
        pref.remove("key");
        assertTrue(pref.properties.isEmpty());
        pref.flush();
        assertNull(storage.toPropertiesFile());                
    }

    public void testRemove2() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                

        pref.put("key","value");
        pref.put("key1","value1");

        pref.flush();
        assertNotNull(storage.toPropertiesFile());
        pref.remove("key");
        assertFalse(pref.properties.isEmpty());
        pref.flush();
        assertNotNull(storage.toPropertiesFile());                
    }

    public void testClear() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());
        pref.put("key","value");
        pref.put("key1","value");
        pref.put("key2","value");
        pref.put("key3","value");
        pref.put("key5","value");
        pref.flush();
        assertNotNull(storage.toPropertiesFile());                

        pref.clear();
        pref.flush();
        assertNull(storage.toPropertiesFile());                
    }

    public void testRemoveNode() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                

        pref.put("key","value");
        pref.node("subnode").put("key","value");
        pref.flush();
        assertNotNull(storage.toPropertiesFile());
        assertNotNull(storage.toFolder());
        pref.removeNode();
        pref.flush();
        assertNull(storage.toPropertiesFile());
        assertNull(storage.toFolder());
        assertFalse(storage.existsNode());
        try {
            pref.sync();
            fail();
        } catch (IllegalStateException ise) {}
    }

    public void testRemoveParentNode() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                

        Preferences subnode = pref.node("subnode");
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                
        subnode.put("key","value");
        subnode.flush();
        assertNotNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                
        subnode.removeNode();        
        pref.flush();
        assertNull(storage.toPropertiesFile());
        assertNull(storage.toFolder());
        assertFalse(storage.existsNode());
    }    

    public void testChildrenNames() throws Exception {
        Preferences subnode = pref.node("c1");
        subnode.put("k","v");
        subnode.flush();
        subnode = pref.node("c2");
        subnode.put("k","v");
        subnode.flush();
        subnode = pref.node("c3/c4");
        subnode.put("k","v");
        subnode.flush();
        assertEquals(new TreeSet<String>(Arrays.asList("c1", "c2", "c3")), new TreeSet<String>(Arrays.asList(storage.childrenNames())));
        pref.node("c2").removeNode();
        assertEquals(new TreeSet<String>(Arrays.asList("c1", "c3")), new TreeSet<String>(Arrays.asList(storage.childrenNames())));
        pref.node("c3").removeNode();
        assertEquals(Collections.singleton("c1"), new TreeSet<String>(Arrays.asList(storage.childrenNames())));
        pref.node("c1").removeNode();
        assertEquals(Collections.emptySet(), new TreeSet<String>(Arrays.asList(storage.childrenNames())));
    }
    
    public void testInvalidChildrenNames() throws Exception {
        NbPreferences subnode = pref;
        assertNotNull(subnode);
        PropertiesStorage ps = (PropertiesStorage)pref.fileStorage;        
        FileObject fold = ps.toFolder(true);
        assertNotNull(FileUtil.createData(fold, "a/b/c/invalid1"));
        subnode.sync();
        assertEquals(0, subnode.childrenNames().length);

        assertNotNull(FileUtil.createData(fold, "a/b/c/invalid2.huh"));
        subnode.sync();
        assertEquals(0, subnode.childrenNames().length);

        assertNotNull(FileUtil.createData(fold, "a/b/c/invalid3.properties.huh"));
        subnode.sync();
        assertEquals(0, subnode.childrenNames().length);
        
        assertNotNull(FileUtil.createData(fold, "a/b/c/valid.properties"));
        subnode.sync();
        assertEquals(1, subnode.childrenNames().length);        
        assertEquals("a", subnode.childrenNames()[0]);        
    }    
}
