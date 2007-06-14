/*
 * OccurrencesTest.java
 * JUnit based test
 *
 * Created on June 14, 2007, 2:14 PM
 */

package org.netbeans.modules.web.jsf.refactoring;

import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 *
 * @author Petr Pisl
 */
public class OccurrencesTest extends TestCase {
    
    public OccurrencesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    public void testGetNewFQCN() {
        assertEquals("aa.b.c.T", Occurrences.getNewFQCN("aa", "a.b.c.T", true));
        assertEquals("a.bb.c.T", Occurrences.getNewFQCN("a.bb", "a.b.c.T", true));
        assertEquals("a.b.cc.T", Occurrences.getNewFQCN("a.b.cc", "a.b.c.T", true));
        assertEquals("aa.T", Occurrences.getNewFQCN("aa", "a.T", true));
        assertEquals("aa.T", Occurrences.getNewFQCN("aa", "a.b.c.T", false));
        assertEquals("a.b.cc.T", Occurrences.getNewFQCN("a.b.cc", "a.b.c.T", false));
        assertEquals("a.b.T", Occurrences.getNewFQCN("a.b", "a.b.c.T", false));
    }
    
}
