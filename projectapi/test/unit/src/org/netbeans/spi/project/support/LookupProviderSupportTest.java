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

package org.netbeans.spi.project.support;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockChangeListener;

/**
 * @author mkleint
 */
public class LookupProviderSupportTest extends NbTestCase {
    
    public LookupProviderSupportTest(String testName) {
        super(testName);
    }

    /**
     * Test of createCompositeLookup method, of class org.netbeans.spi.project.support.LookupProviderSupport.
     */
    public void testCreateCompositeLookup() {
        LookupMergerImpl merger = new LookupMergerImpl();
        Lookup base = Lookups.fixed(new JButton(), new JComboBox(), merger);
        LookupProviderImpl pro1 = new LookupProviderImpl();
        LookupProviderImpl pro2 = new LookupProviderImpl();
        LookupProviderImpl pro3 = new LookupProviderImpl();
        
        InstanceContent provInst = new InstanceContent();
        Lookup providers = new AbstractLookup(provInst);
        provInst.add(pro1);
        provInst.add(pro2);
        
        pro1.ic.add(new JTextField());
        pro2.ic.add(new JTextArea());
        
        LookupProviderSupport.DelegatingLookupImpl del = new LookupProviderSupport.DelegatingLookupImpl(base, providers, "<irrelevant>");
        
        assertNotNull(del.lookup(JTextArea.class));
        assertNotNull(del.lookup(JComboBox.class));
        
        // test merger..
        JButton butt = del.lookup(JButton.class);
        assertNotNull(butt);
        assertEquals("CORRECT", butt.getText());
        assertEquals(1, del.lookupAll(JButton.class).size());
        assertEquals(1, merger.expectedCount);
        
        pro3.ic.add(new JButton());
        pro3.ic.add(new JRadioButton());
        provInst.add(pro3);
        assertNotNull(del.lookup(JRadioButton.class));
                
        // test merger..
        butt = del.lookup(JButton.class);
        assertNotNull(butt);
        assertEquals("CORRECT", butt.getText());
        assertEquals(1, del.lookupAll(JButton.class).size());
        assertEquals(2, merger.expectedCount);
        
        pro1.ic.add(new JButton());
        
        // test merger..
        butt = del.lookup(JButton.class);
        assertNotNull(butt);
        assertEquals("CORRECT", butt.getText());
        assertEquals(1, del.lookupAll(JButton.class).size());
        assertEquals(3, merger.expectedCount);
        
    }
    
    private SourcesImpl createImpl(String id) {
        SourcesImpl impl0 = new SourcesImpl();
        SourceGroupImpl grp0 = new SourceGroupImpl();
        grp0.name = id;
        impl0.grpMap.put("java", new SourceGroup[] {grp0});
        return impl0;
    }
    
    public void testSourcesMerger() {
        SourcesImpl impl0 = createImpl("group0");
        SourcesImpl impl1 = createImpl("group1");
        SourcesImpl impl2 = createImpl("group2");
        SourcesImpl impl3 = createImpl("group3");
        
        Lookup base = Lookups.fixed(impl0, LookupProviderSupport.createSourcesMerger());
        LookupProviderImpl2 pro1 = new LookupProviderImpl2();
        LookupProviderImpl2 pro2 = new LookupProviderImpl2();
        LookupProviderImpl2 pro3 = new LookupProviderImpl2();
        
        InstanceContent provInst = new InstanceContent();
        Lookup providers = new AbstractLookup(provInst);
        provInst.add(pro1);
        provInst.add(pro2);
        
        pro1.ic.add(impl1);
        pro2.ic.add(impl2);
        pro3.ic.add(impl3);
        
        LookupProviderSupport.DelegatingLookupImpl del = new LookupProviderSupport.DelegatingLookupImpl(base, providers, "<irrelevant>");
        
        Sources srcs = del.lookup(Sources.class); 
        assertNotNull(srcs);
        SourceGroup[] grps = srcs.getSourceGroups("java");
        assertEquals(3, grps.length);
        
        //now let's add another module to the bunch and see if the new SG appears
        provInst.add(pro3);
        
        srcs = del.lookup(Sources.class); 
        assertNotNull(srcs);
        grps = srcs.getSourceGroups("java");
        assertEquals(4, grps.length);
        
        //now let's remove another module to the bunch and see if the SG disappears
        provInst.remove(pro2);
        
        srcs = del.lookup(Sources.class); 
        assertNotNull(srcs);
        grps = srcs.getSourceGroups("java");
        assertEquals(3, grps.length);
        
        //lets remove one and listen for changes...
        srcs = del.lookup(Sources.class); 
        MockChangeListener ch = new MockChangeListener();
        srcs.addChangeListener(ch);
        provInst.remove(pro1);
        
        ch.assertEvent();
        grps = srcs.getSourceGroups("java");
        assertEquals(2, grps.length);
        
        provInst.add(pro2);
        
        ch.assertEvent();
        grps = srcs.getSourceGroups("java");
        assertEquals(3, grps.length);
        
    }
    
    public void testNonexistentPath() throws Exception {
        // #87544: don't choke on a nonexistent path! Just leave it empty.
        Lookup l = LookupProviderSupport.createCompositeLookup(Lookup.EMPTY, "nowhere");
        assertEquals(Collections.<Object>emptySet(), new HashSet<Object>(l.lookupAll(Object.class)));
    }
    
    private class LookupProviderImpl implements LookupProvider {
        InstanceContent ic = new InstanceContent();
        boolean wasAlreadyCalled = false;
        public Lookup createAdditionalLookup(Lookup baseContext) {
            assertNotNull(baseContext.lookup(JButton.class));
            assertNull(baseContext.lookup(JCheckBox.class));
            assertFalse(wasAlreadyCalled);
            wasAlreadyCalled = true;
            return new AbstractLookup(ic);
        }
    }

    private class LookupProviderImpl2 implements LookupProvider {
        InstanceContent ic = new InstanceContent();
        AbstractLookup l;
        public Lookup createAdditionalLookup(Lookup baseContext) {
            if (l == null) {
                l = new AbstractLookup(ic);
            }
            return l;
        }
    }
    
    private class LookupMergerImpl implements LookupMerger<JButton> {
        
        int expectedCount;
        
        public Class<JButton> getMergeableClass() {
            return JButton.class;
        }

        public JButton merge(Lookup lookup) {
            expectedCount = lookup.lookupAll(JButton.class).size();
            return new JButton("CORRECT");
        }
        
    }
    
    private static class SourcesImpl implements Sources {
        public HashMap<String, SourceGroup[]> grpMap = new HashMap<String, SourceGroup[]>();
        
        public SourceGroup[] getSourceGroups(String type) {
            return grpMap.get(type);
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }
    }
    
    private static class SourceGroupImpl implements SourceGroup {

        String name;

        String displayName;
        public FileObject getRootFolder() {
            return null;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        public boolean contains(FileObject file) throws IllegalArgumentException {
            return false;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
    
}
