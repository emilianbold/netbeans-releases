package org.netbeans.examples.modules.misc;
import org.netbeans.junit.NbTestCase;
public class MiscTest extends NbTestCase {
    public MiscTest(String name) {
        super(name);
    }
    public void testGetMagicTokenette() throws Exception {
        assertEquals("correct value", "sezam", Misc.getMagicTokenette());
    }
}
