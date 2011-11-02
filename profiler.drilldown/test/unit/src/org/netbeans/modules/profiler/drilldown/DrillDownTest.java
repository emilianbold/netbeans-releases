/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.drilldown;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import org.netbeans.modules.profiler.categorization.api.*;

/**
 * Tests the core drilldown functionality. 
 * Connections to the other parts of the system are not put under test.
 * 
 * @author Jaroslav Bachorik
 */
public class DrillDownTest extends NbTestCase {
    private DrillDown instance;
    private CategoryLeaf leaf1, leaf2, leaf3;
    private CategoryContainer root, cont1;
    private CategoryLeaf self1;
    
    final private Mark[] marks = new Mark[] {new Mark((short)1), new Mark((short)2), new Mark((short)3), new Mark((short)4)};
    
    public DrillDownTest() {
        super("DrillDownTest");
    }

    @Override
    public void setUp() throws Exception {
        ProfilerClient pc = new ProfilerClient(new ProfilerEngineSettings(), new ProfilingSessionStatus(), null, null);
        Categorization cat = new Categorization() {            
            @Override
            protected void buildCategories(CategoryContainer root) {
                leaf1 = new CategoryLeaf("leaf1", "Leaf 1", marks[0]);
                cont1 = new CategoryContainer("cont1", "Container 1", marks[1]);
                leaf2 = new CategoryLeaf("leaf2", "Leaf 2", marks[2]);
                leaf3 = new CategoryLeaf("leaf3", "Leaf 3", marks[3]);
                
                cont1.add(leaf2);
                cont1.add(leaf3);
                
                root.add(leaf1);
                root.add(cont1);
                
                self1 = new CategoryLeaf("SELF_CATEGORY", "Self", Mark.DEFAULT);
                DrillDownTest.this.root = root;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public MarkMapping[] getMappings() {
                return new MarkMapping[0];
            }    
        };
        
        instance = new DrillDown(cat, pc);
    }

    @Override
    public void tearDown() throws Exception {
        instance = null;
    }
    
    public void testIsCurrentRoot() {
        System.out.println("isCurrentRoot");
        boolean result = instance.isCurrent(root);
        assertTrue(result);
    }

    public void testDrillDownLeaf() {
        System.out.println("drillDown (leaf)");
        Category expResult = leaf1;
        instance.drilldown(leaf1.getId());
        Category result = instance.getCurrentCategory();
        assertEquals(expResult, result);
    }
    
    public void testDrillDownContainer() {
        System.out.println("drillDown (container)");
        Category expResult = leaf2;
        instance.drilldown(cont1.getId());
        instance.drilldown(leaf2.getId());
        Category result = instance.getCurrentCategory();
        assertEquals(expResult, result);
    }
    
    public void testDrillDownInvalid() {
        System.out.println("drillDown (invalid)");
        Category expResult = root;
        instance.drilldown(leaf2.getId());
        Category result = instance.getCurrentCategory();
        assertEquals(expResult, result);
    }

    public void testGetSubCategories() {
        System.out.println("getSubCategories");
        List expResult = Arrays.asList(cont1, leaf1, self1);
        List result = instance.getSubCategories();
        assertArrayEquals(expResult.toArray(new Object[expResult.size()]), result.toArray(new Object[result.size()]));
    }

    public void testCanDrilldownTrue() {
        System.out.println("canDrilldown (true)");
        boolean expResult = true;
        boolean result = instance.canDrilldown(leaf1);
        assertEquals(expResult, result);
    }
    
    public void testCanDrilldownFalse() {
        System.out.println("canDrilldown (false)");
        boolean expResult = false;
        instance.drilldown("SELF_CATEGORY");
        boolean result = instance.canDrilldown(self1);
        assertEquals(expResult, result);
    }

    public void testDrillup_0args_Root() {
        System.out.println("drillup (root)");
        instance.drillup();
        assertEquals(root, instance.getCurrentCategory());
    }
    
    public void testDrillup_0args_NoRoot() {
        System.out.println("drillup (no root)");
        instance.drilldown(leaf1.getId());
        instance.drillup();
        assertEquals(root, instance.getCurrentCategory());
    }

    public void testDrillup_String_Root() {
        System.out.println("drillup (root)");
        instance.drillup(root.getId());
        assertEquals(root, instance.getCurrentCategory());
    }
    
    public void testDrillup_String_NoRoot() {
        System.out.println("drillup (no root)");
        instance.drilldown(cont1.getId());
        instance.drilldown(leaf2.getId());
        instance.drillup(root.getId());
        assertEquals(root, instance.getCurrentCategory());
    }

    public void testEvaluateRoot() {
        System.out.println("evaluate (root)");
        assertTrue(instance.evaluate(Mark.DEFAULT));
        assertTrue(instance.evaluate(marks[0]));
        assertTrue(instance.evaluate(marks[1]));
        assertTrue(instance.evaluate(marks[2]));
        assertTrue(instance.evaluate(marks[3]));
    }
    
    public void testEvaluateLeaf() {
        System.out.println("evaluate (leaf)");
        instance.drilldown(leaf1.getId());
        assertFalse(instance.evaluate(Mark.DEFAULT));
        assertTrue(instance.evaluate(marks[0]));
        assertFalse(instance.evaluate(marks[1]));
        assertFalse(instance.evaluate(marks[2]));
        assertFalse(instance.evaluate(marks[3]));
    }
    
    public void testEvaluateConatiner() {
        System.out.println("evaluate (container)");
        instance.drilldown(cont1.getId());
        assertFalse(instance.evaluate(Mark.DEFAULT));
        assertFalse(instance.evaluate(marks[0]));
        assertTrue(instance.evaluate(marks[1]));
        assertTrue(instance.evaluate(marks[2]));
        assertTrue(instance.evaluate(marks[3]));
    }

    public void testReset() {
        System.out.println("reset");
        instance.drilldown(cont1.getId());
        instance.reset();
        assertEquals(root, instance.getCurrentCategory());
    }
}
