/*
 * UtilTest.java
 * JUnit 4.x based test
 *
 * Created on June 6, 2007, 4:48 PM
 */

package org.netbeans.modules.websvc.rest.wizard;

import org.netbeans.modules.websvc.rest.codegen.TestBase;

/**
 *
 * @author nam
 */
public class UtilTest extends TestBase {
    
    public UtilTest(String name) {
        super(name);
    }

    public void testDeriveUri() throws Exception {
        String result = Util.deriveUri("MyServices", "/items/{name}");
        assertEquals("/myServices/{name}", result);
        
        result = Util.deriveUri("MyServices", "/foo/items/{name}");
        assertEquals("/foo/myServices/{name}", result);

        result = Util.deriveUri("MyServices", "/foo/items/{name}");
        assertEquals("/foo/myServices/{name}", result);

        result = Util.deriveUri("MyServices", "/{name}");
        assertEquals("/{name}", result);
    }

}
