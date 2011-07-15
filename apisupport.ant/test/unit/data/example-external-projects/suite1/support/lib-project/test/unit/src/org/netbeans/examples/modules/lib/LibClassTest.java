package org.netbeans.examples.modules.lib;
import org.netbeans.examples.modules.misc.Misc;
import org.netbeans.junit.NbTestCase;
public class LibClassTest extends NbTestCase {
    public LibClassTest(String name) {
        super(name);
    }
    public void testGetMagicToken() throws Exception {
        assertTrue("uses Misc", LibClass.getMagicToken().indexOf(Misc.getMagicTokenette()) != -1);
    }
}
