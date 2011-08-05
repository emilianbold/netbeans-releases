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

package org.netbeans.spi.project.support;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
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

    private SourcesImpl createImpl(String id) {
        SourcesImpl impl0 = new SourcesImpl();
        SourceGroupImpl grp0 = new SourceGroupImpl();
        grp0.name = id;
        impl0.grpMap.put("java", Arrays.<SourceGroup>asList(grp0));
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
        
        DelegatingLookupImpl del = new DelegatingLookupImpl(base, providers, "<irrelevant>");
        
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

    public void testNestedComposites() throws Exception {
        SourcesImpl impl1 = createImpl("group1");
        SourcesImpl impl2 = createImpl("group2");
        SourcesImpl impl3 = createImpl("group3");
        Lookup base = Lookups.fixed(impl1, LookupProviderSupport.createSourcesMerger());
        class Prov implements LookupProvider {
            final SourcesImpl instance;
            Prov(SourcesImpl instance) {
                this.instance = instance;
            }
            public @Override Lookup createAdditionalLookup(Lookup baseContext) {
                return Lookups.singleton(instance);
            }
        }
        Lookup inner = new DelegatingLookupImpl(base, Lookups.fixed(new Prov(impl2)), null);
        Lookup outer = new DelegatingLookupImpl(inner, Lookups.fixed(new Prov(impl3)), null);
        List<String> names = new ArrayList<String>();
        for (SourceGroup g : outer.lookup(Sources.class).getSourceGroups("java")) {
            names.add(g.getName());
        }
        Collections.sort(names);
        assertEquals("[group1, group2, group3]", names.toString());
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
    
    private static class SourcesImpl implements Sources {
        public Map<String,List<SourceGroup>> grpMap = new HashMap<String,List<SourceGroup>>();
        
        public SourceGroup[] getSourceGroups(String type) {
            return grpMap.get(type).toArray(new SourceGroup[0]);
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }

        public @Override String toString() {
            return grpMap.toString();
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

        public @Override String toString() {
            return name;
        }
    }
    
}
