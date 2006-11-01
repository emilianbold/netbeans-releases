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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;

/**
 * Tests {@link AddModuleFilter}.
 * @author Jesse Glick
 */
public class AddModuleFilterTest extends TestBase {
    
    public AddModuleFilterTest(String name) {
        super(name);
    }
    
    private AddModuleFilter filter;
    
    protected void setUp() throws Exception {
        super.setUp();
        ModuleList ml = ModuleList.getModuleList(resolveEEPFile("suite1/action-project"));
        Set/*<ModuleDependency>*/ deps = new HashSet();
        Iterator it = ml.getAllEntries().iterator();
        while (it.hasNext()) {
            ModuleEntry entry = (ModuleEntry) it.next();
            ModuleDependency dep = new ModuleDependency(entry);
            deps.add(dep);
        }
        filter = new AddModuleFilter(deps, "some.random.module");
    }
    
    public void testSimpleMatches() throws Exception {
        // JAR:
        assertMatches("boot.jar", new String[] {"org.netbeans.bootstrap"});
        // Class-Path JAR:
        assertMatches("project-ant.jar", new String[] {"org.netbeans.modules.project.ant"});
        // Display name:
        assertMatches("demo library", new String[] {"org.netbeans.examples.modules.lib"});
    }
    
    public void testClassAndPackageNameMatches() throws Exception {
        // Using binaries:
        assertMatches("callablesys", new String[] {"org.openide.util"}); // org.openide.util.actions.CallableSystemAction
        assertMatches("org.openide.nodes", new String[] {"org.openide.nodes"});
        // This is an impl class, exclude it:
        assertMatches("simplefileownerqueryimpl", new String[0]);
        // Using sources:
        assertMatches("libclass", new String[] {"org.netbeans.examples.modules.lib"});
        // Impl class:
        assertMatches("magicaction", new String[0]);
        // Using class-path extensions:
        assertMatches("javax.help", new String[] {"org.netbeans.modules.javahelp"});
        // XXX test that friend APIs only match if "I" am a friend (needs API change in ModuleDependency)
    }
    
    public void testMatchStrings() throws Exception {
        ModuleDependency dep = (ModuleDependency) filter.getMatches("callablesys").iterator().next();
        assertEquals(Collections.singleton("org.openide.util.actions.CallableSystemAction"), filter.getMatchesFor("callablesys", dep));
    }
    
    public void testMatchOrdering() throws Exception { // #71995
        List/*<String>*/ matches = new ArrayList();
        Iterator it = filter.getMatches("systemaction").iterator();
        while (it.hasNext()) {
            matches.add(((ModuleDependency) it.next()).getModuleEntry().getCodeNameBase());
        }
        assertEquals(Arrays.asList(new String[] {
            "org.openide.util", // etc.SystemAction: matchLevel=0
            "org.netbeans.modules.editor", // etc.NbEditorUI.SystemActionPerformer: matchLevel=1
            "org.openide.loaders", // etc.FileSystemAction: matchLevel=2
        }), matches);
    }
    
    private void assertMatches(String text, String[] cnbs) {
        Set/*<ModuleDependency>*/ matches = filter.getMatches(text);
        Set/*<String>*/ matchedCNBs = new HashSet();
        Iterator it = matches.iterator();
        while (it.hasNext()) {
            matchedCNBs.add(((ModuleDependency) it.next()).getModuleEntry().getCodeNameBase());
        }
        assertEquals("correct matches for '" + text + "'", new HashSet(Arrays.asList(cnbs)), matchedCNBs);
    }
    
}
