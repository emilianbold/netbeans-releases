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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.dom;

import junit.framework.*;
import org.netbeans.modules.xml.xam.TestComponent2;
import org.netbeans.modules.xml.xam.TestModel2;
import org.netbeans.modules.xml.xam.Util;

/**
 *
 * @author nn136682
 */
public class ReadOnlyAccessTest extends TestCase {
    
    public ReadOnlyAccessTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        return new TestSuite(ReadOnlyAccessTest.class);
    }

    public void testFindPosition() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        assertEquals(40, model.getRootComponent().findPosition());
        assertEquals(4, model.getRootComponent().getChildren().size());
        TestComponent2 component = model.getRootComponent().getChildren().get(0);
        assertEquals("a", component.getPeer().getLocalName());
        assertEquals(141, component.findPosition());
    }

    public void testFindPositionWithPrefix() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.B b = model.getRootComponent().getChildren(TestComponent2.B.class).get(0);
        TestComponent2.Aa aa = b.getChildren(TestComponent2.Aa.class).get(0);
        assertEquals(218, aa.findPosition());
    }

    public void testFindPositionWithElementTagInAttr() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.C c = model.getRootComponent().getChildren(TestComponent2.C.class).get(0);
        assertEquals(261, c.findPosition());
    }

    public void testFindElement() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2 component = model.getRootComponent().getChildren().get(0);
        assertEquals(component, model.findComponent(141));
        assertEquals(component, model.findComponent(155));
        assertEquals(component, model.findComponent(171));
    }
    
    public void testFindElementWithPrefix() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.B b = model.getRootComponent().getChildren(TestComponent2.B.class).get(0);
        TestComponent2.Aa aa = b.getChildren(TestComponent2.Aa.class).get(0);
        assertEquals(aa, model.findComponent(218));
        assertEquals(aa, model.findComponent(230));
        assertEquals(aa, model.findComponent(244));
    }

    public void testFindElementWithTagInAttr() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.C c = model.getRootComponent().getChildren(TestComponent2.C.class).get(0);
        assertEquals(c, model.findComponent(261));
        assertEquals(c, model.findComponent(265));
        assertEquals(c, model.findComponent(277));
    }
    
    public void testFindElementGivenTextPosition() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2 root = model.getRootComponent();
        TestComponent2.B b = root.getChildren(TestComponent2.B.class).get(0);
        assertEquals(b, model.findComponent(211));
        assertEquals(root, model.findComponent(260));
        assertEquals(root, model.findComponent(279));
    }    
    
    public void testGetXmlFragment() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2 root = model.getRootComponent();
        TestComponent2.B b = model.getRootComponent().getChildren(TestComponent2.B.class).get(0);
        String result = b.getXmlFragment();
        assertTrue(result.startsWith(" <!-- comment -->"));
        assertTrue(result.indexOf("value=\"c\"/>") > 0);
    }

}
