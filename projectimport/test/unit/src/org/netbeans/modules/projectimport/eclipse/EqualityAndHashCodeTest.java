/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse;

import org.netbeans.junit.NbTestCase;

/**
 * Tests equal and hashCode methods.
 *
 * @author mkrauskopf
 */
public class EqualityAndHashCodeTest extends NbTestCase {
    
    ClassPath.Link link2;
    ClassPath.Link theSameAsLink2;
    
    Workspace.Variable var2;
    Workspace.Variable theSameAsVar2;
    
    public EqualityAndHashCodeTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        link2 = new ClassPath.Link();
        link2.setLocation("/link2");
        link2.setName("link2");
        link2.setType(ClassPath.Link.TYPE_FILE);
        
        theSameAsLink2 = new ClassPath.Link();
        theSameAsLink2.setLocation("/link2");
        theSameAsLink2.setName("link2");
        theSameAsLink2.setType(ClassPath.Link.TYPE_FILE);
        
        var2 = new Workspace.Variable();
        var2.setLocation("/var2");
        var2.setName("var2");
        
        theSameAsVar2 = new Workspace.Variable();
        theSameAsVar2.setLocation("/var2");
        theSameAsVar2.setName("var2");
    }
    
    /** tests ClassPathContent.Link.equals() */
    public void testLinksEquality() {
        System.out.println("testLinksEquality");
        assertNotSame("link2 and theSameAsLink2 shouldn't be the same " +
                "(link2 == theSameAsLink2)", link2, theSameAsLink2);
        assertEquals("link2 should be equal to theSameAsLink2",
                link2, theSameAsLink2);
        theSameAsLink2.setType(ClassPath.Link.TYPE_FOLDER);
        assertFalse("link2 should be not be equal to theSameAsLink2",
                link2.equals(theSameAsLink2));
    }
    
    /** tests ClassPathContent.Link.hashCode() */
    public void testLinksHashCodes() {
        System.out.println("testLinksHashCodes");
        assertEquals("link2 and theSameAsLink2 should generate the same hashCode",
                link2.hashCode(), theSameAsLink2.hashCode());
        theSameAsLink2.setType(ClassPath.Link.TYPE_FOLDER);
        assertFalse("link2 and theSameAsLink2 shouldn't generate the same hashCode",
                link2.hashCode() == theSameAsLink2.hashCode());
    }
    /** tests ClassPathContent.Variable.equals() */
    public void testVariablesEquality() {
        System.out.println("testVariablesEquality");
        assertNotSame("var2 and theSameAsVar2 shouldn't be the same " +
                "(var2 == theSameAsVar2)", var2, theSameAsVar2);
        assertEquals("var2 should be equal to theSameAsVar2",
                var2, theSameAsVar2);
        theSameAsVar2.setLocation(theSameAsVar2.getLocation() + "a");
        assertFalse("var2 should be not be equal to theSameAsVar2",
                var2.equals(theSameAsVar2));
    }
    
    /** tests ClassPathContent.Variable.hashCode() */
    public void testVariablesHashCodes() {
        System.out.println("testVariablesHashCodes");
        assertEquals("var2 and theSameAsVar2 should generate the same hashCode",
                var2.hashCode(), theSameAsVar2.hashCode());
        theSameAsVar2.setLocation(theSameAsVar2.getLocation() + "a");
        assertFalse("var2 and theSameAsVar2 shouldn't generate the same hashCode",
                var2.hashCode() == theSameAsLink2.hashCode());
    }
}
