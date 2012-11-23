/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.javacard.project.deps;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.xml.sax.InputSource;

/**
 *
 * @author Tim
 */
public class DependenciesTest {

    public DependenciesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }


    Dependency a;
    Dependency b;
    Dependency c;
    Dependency d;
    Dependencies deps;

    @Before
    public void setUp() {
        deps = new Dependencies();
        deps.add (a = new Dependency("a", DependencyKind.RAW_JAR, DeploymentStrategy.DEPLOY_TO_CARD));
        deps.add (b = new Dependency("b", DependencyKind.CLASSIC_LIB, DeploymentStrategy.DEPLOY_TO_CARD));
        deps.add (c = new Dependency("c", DependencyKind.EXTENSION_LIB, DeploymentStrategy.DEPLOY_TO_CARD));
        deps.add (d = new Dependency("d", DependencyKind.JAR_WITH_EXP_FILE, DeploymentStrategy.DEPLOY_TO_CARD));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCanMoveUp() {
        System.out.println("testCanMoveUp");
        assertTrue (deps.canMoveUp(d));
        assertTrue (deps.canMoveUp(c));
        assertFalse (deps.canMoveUp(a));
    }

    @Test
    public void testCanMoveDown() {
        System.out.println("testCanMoveDown");
        assertTrue (deps.canMoveDown(a));
        assertTrue (deps.canMoveDown(b));
        assertTrue (deps.canMoveDown(c));
        assertFalse (deps.canMoveDown(d));
    }

    /**
     * Test of copy method, of class Dependencies.
     */
    @Test
    public void testCopy() {
        System.out.println("testCopy");
        Dependencies other = deps.copy();
        assertEquals (deps, other);
        assertNotSame (deps, other);
    }

    /**
     * Test of add method, of class Dependencies.
     */
    @Test
    public void testAdd() {
        Dependency e;
        deps.add(e = new Dependency("e", DependencyKind.RAW_JAR, DeploymentStrategy.ALREADY_ON_CARD));
        assertTrue (deps.all().contains(e));
        assertEquals(deps.all().size() - 1, deps.all().indexOf(e));
    }

    /**
     * Test of all method, of class Dependencies.
     */
    @Test
    public void testAll() {
        System.out.println("testAll");
        List l = new ArrayList<Dependency>(Arrays.asList(a, b, c, d));
        assertEquals (l, deps.all());
    }

    /**
     * Test of moveUp method, of class Dependencies.
     */
    @Test
    public void testMoveUp() {
        System.out.println("testMoveUp");
        deps.moveUp(d);
        assertEquals(2, deps.all().indexOf(d));
        deps.moveUp(b);
        assertEquals(0, deps.all().indexOf(b));
        deps.moveUp(b);
        assertEquals(0, deps.all().indexOf(b));
    }

    @Test
    public void testMoveDown() {
        System.out.println("testMoveDown");
        deps.moveDown(a);
        assertEquals (1, deps.all().indexOf(a));
        deps.moveDown(c);
        assertEquals (3, deps.all().indexOf(c));
        deps.moveDown(c);
        assertEquals (3, deps.all().indexOf(c));
    }

    /**
     * Test of remove method, of class Dependencies.
     */
    @Test
    public void testRemove() {
        deps.remove(c);
        assertFalse(deps.all().contains(c));
    }

    /**
     * Test of parse method, of class Dependencies.
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        InputSource in = new InputSource(new ByteArrayInputStream(XML.getBytes()));
        PropertyEvaluator eval = null;
        Dependencies result = Dependencies.parse(in, eval);
        assertEquals(deps, result);
    }

    private static final String XML = "<dependencies>\n" +
            "    <dependency id=\"a\" deployment=\"DEPLOY_TO_CARD\" kind=\"RAW_JAR\"/>\n" +
            "    <dependency id=\"b\" deployment=\"DEPLOY_TO_CARD\" kind=\"CLASSIC_LIB\"/>\n" +
            "    <dependency id=\"c\" deployment=\"DEPLOY_TO_CARD\" kind=\"EXTENSION_LIB\"/>\n" +
            "    <dependency id=\"d\" deployment=\"DEPLOY_TO_CARD\" kind=\"JAR_WITH_EXP_FILE\"/>\n" +
            "</dependencies>";

}