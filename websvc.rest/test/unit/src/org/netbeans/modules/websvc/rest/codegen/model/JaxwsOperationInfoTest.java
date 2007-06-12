/*
 * JaxwsOperationInfoTest.java
 * JUnit 4.x based test
 *
 * Created on June 11, 2007, 9:52 PM
 */

package org.netbeans.modules.websvc.rest.codegen.model;

import org.netbeans.modules.websvc.rest.codegen.TestBase;

/**
 *
 * @author nam
 */
public class JaxwsOperationInfoTest extends TestBase {
    
    public JaxwsOperationInfoTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
    }

    public void testDerivePackageName() throws Exception {
        String url = "http://wsparam.strikeiron.com/ZipInfo3?WSDL";
        assertEquals("com.strikeiron.wsparam", JaxwsOperationInfo.derivePackageName(url));
    }
    
}
