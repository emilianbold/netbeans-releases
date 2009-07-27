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

package org.netbeans.modules.xml.schema.model;


import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.After;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author nn136682
 * @author Nikita Krjukov
 */
public class SchemaModelPerformanceTest {
    
    @After
    public void tearDown() {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    /**
     * B includes C & D. C & D do not know anything about each other.
     * In this use-case, we'll explore components in C and it'll NOT resolve types
     * from D.
     *
     * It's impossible to resolve the reference if the B hasn't loaded yet.
     * If uncomment the first line, which loads the T1B.xsd, then the reference
     * becomes resolvable.
     */
    @Test
    public void testPerformance1() throws Exception {
        SchemaModel sm;
        sm = Util.loadSchemaModel2("resources/performance1.zip", "C.xsd"); // NOI18N
        sm = Util.loadSchemaModel2("resources/performance1.zip", "unresolvedIncludes.xsd"); // NOI18N
        //
        sm = Util.loadSchemaModel2("resources/performance1.zip", "A.xsd"); // NOI18N
        //
        // Wait 2 second till all models are loaded and validated
        Thread.sleep(1000);
        //
        assert(sm.getState() == State.VALID);
        GlobalElement e1 = (GlobalElement)sm.getSchema().getChildren().get(1);
        assertNotNull(e1);
        assertEquals(e1.getName(), "A1"); // NOI18N
        NamedComponentReference ncr = e1.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(name, "hl7_performance_test:C1"); // NOI18N
        //this is when it'll try to resolve
        //
        long before = System.currentTimeMillis();
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        long after = System.currentTimeMillis();
        long delay = after - before;
        assertTrue("Delay=" + delay, delay < 100L);
        System.out.println("Delay1=" + delay);
        //
        assertNotNull(gct);
        assertEquals(gct.getName(), "C1");
        //
        // Try again. It should be much faster now.
        e1 = (GlobalElement)sm.getSchema().getChildren().get(2);
        assertNotNull(e1);
        assertEquals(e1.getName(), "A2"); // NOI18N
        ncr = e1.getType();
        name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(name, "hl7_performance_test:C2"); // NOI18N
        //this is when it'll try to resolve
        //
        before = System.currentTimeMillis();
        gct = (GlobalComplexType)ncr.get();
        after = System.currentTimeMillis();
        delay = after - before;
        assertTrue("Delay=" + delay, delay < 5L);
        System.out.println("Delay2=" + delay);
        //
        assertNotNull(gct);
        assertEquals(gct.getName(), "C2");
        //
        // Wait 5.5 seconds and try third time
        Thread.sleep(5500);
        //
        e1 = (GlobalElement)sm.getSchema().getChildren().get(3);
        assertNotNull(e1);
        assertEquals(e1.getName(), "A3"); // NOI18N
        ncr = e1.getType();
        name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assertEquals(name, "hl7_performance_test:C3"); // NOI18N
        //this is when it'll try to resolve
        //
        before = System.currentTimeMillis();
        gct = (GlobalComplexType)ncr.get();
        after = System.currentTimeMillis();
        delay = after - before;
        assertTrue("Delay=" + delay, delay < 30L);
        System.out.println("Delay3=" + delay);
        //
        assertNotNull(gct);
        assertEquals(gct.getName(), "C3");
    }
    
}
