
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