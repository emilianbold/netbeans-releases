/*
 * JSFRefactoringUtilsTest.java
 * JUnit based test
 *
 * Created on June 7, 2007, 4:59 PM
 */

package org.netbeans.modules.web.jsf.refactoring;

import junit.framework.TestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author petr
 */
public class JSFRefactoringUtilsTest extends TestCase {
    
    public JSFRefactoringUtilsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testContainsRenamingPackage() {
        assertTrue(JSFRefactoringUtils.containsRenamingPackage("a.b.c.class", "a.b.c", false));
        assertFalse(JSFRefactoringUtils.containsRenamingPackage("a.b.c.class", "a.b", false));
        assertTrue(JSFRefactoringUtils.containsRenamingPackage("a.b.c.class", "a.b", true));
        assertTrue(JSFRefactoringUtils.containsRenamingPackage("a.b.c.class", "a", true));
        assertTrue(JSFRefactoringUtils.containsRenamingPackage("a.b.c.class", "a.b.c", true));
        assertFalse(JSFRefactoringUtils.containsRenamingPackage("a.b.c.class", "x.y.z", false));
        
        
    }
    
}
