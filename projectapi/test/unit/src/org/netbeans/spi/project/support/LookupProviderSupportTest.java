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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author mkleint
 */
public class LookupProviderSupportTest extends NbTestCase {
    
    public LookupProviderSupportTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
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
        
        LookupProviderSupport.DelegatingLookupImpl del = new LookupProviderSupport.DelegatingLookupImpl(base, providers);
        
        assertNotNull(del.lookup(JTextArea.class));
        assertNotNull(del.lookup(JComboBox.class));
        
        // test merger..
        JButton butt = del.lookup(JButton.class);
        assertNotNull(butt);
        assertEquals("CORRECT", butt.getText());
        assertEquals(1, del.lookup(new Lookup.Template(JButton.class)).allInstances().size());
        assertEquals(1, merger.expectedCount);
        
        pro3.ic.add(new JButton());
        pro3.ic.add(new JRadioButton());
        provInst.add(pro3);
        assertNotNull(del.lookup(JRadioButton.class));
                
        // test merger..
        butt = del.lookup(JButton.class);
        assertNotNull(butt);
        assertEquals("CORRECT", butt.getText());
        assertEquals(1, del.lookup(new Lookup.Template(JButton.class)).allInstances().size());
        assertEquals(2, merger.expectedCount);
        
        pro1.ic.add(new JButton());
        
        // test merger..
        butt = del.lookup(JButton.class);
        assertNotNull(butt);
        assertEquals("CORRECT", butt.getText());
        assertEquals(1, del.lookup(new Lookup.Template(JButton.class)).allInstances().size());
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
        
        LookupProviderSupport.DelegatingLookupImpl del = new LookupProviderSupport.DelegatingLookupImpl(base, providers);
        
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
        ChangeListenerImpl ch = new ChangeListenerImpl();
        srcs.addChangeListener(ch);
        provInst.remove(pro1);
        
        assertTrue(ch.pinged);
        grps = srcs.getSourceGroups("java");
        assertEquals(2, grps.length);
        
        ch.pinged = false;
        provInst.add(pro2);
        
        assertTrue(ch.pinged);
        grps = srcs.getSourceGroups("java");
        assertEquals(3, grps.length);
        
    }
    
    private class ChangeListenerImpl implements ChangeListener {
        boolean pinged = false;
        public void stateChanged(ChangeEvent e) {
            pinged = true;
        }
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
    
    private class LookupMergerImpl implements LookupMerger {
        
        int expectedCount;
        
        public Class getMergeableClass() {
            return JButton.class;
        }

        public Object merge(Lookup lookup) {
            Lookup.Result res = lookup.lookup(new Lookup.Template(JButton.class));
            int size = res.allInstances().size();
            expectedCount = size;
            return new JButton("CORRECT");
        }
        
    }
    
    private static class SourcesImpl implements Sources {
        public HashMap<String, SourceGroup[]> grpMap = new HashMap<String, SourceGroup[]>();
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        
        
        public SourceGroup[] getSourceGroups(String type) {
            return grpMap.get(type);
        }

        public void addChangeListener(ChangeListener listener) {
            listeners.add(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            listeners.remove(listener);
        }
        
        public void fireChange() {
            for (ChangeListener listener : listeners) {
                listener.stateChanged(new ChangeEvent(this));
            }
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
