/*
 * GenericResourceCodeGeneratorTest.java
 * JUnit 4.x based test
 *
 * Created on May 30, 2007, 11:49 AM
 */

package org.netbeans.modules.websvc.rest.codegen;

import org.netbeans.modules.websvc.rest.codegen.model.GenericResourceBean;
import org.openide.filesystems.FileObject;

/**
 *
 * @author nam
 */
public class GenericResourceCodeGeneratorTest extends TestBase {
    
    public GenericResourceCodeGeneratorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setUpSrcDir();
    }
    
    public void testModel() throws Exception {
        GenericResourceBean bean = new GenericResourceBean("TestGenericResource", PACKAGE_ACME, "/com/acme/{generic}/{test1}");
        assertEquals(2, bean.getUriParams().length);
        assertEquals("generic", bean.getUriParams()[0]);
        assertEquals("test1", bean.getUriParams()[1]);
        
        bean = new GenericResourceBean("TestGenericResource", PACKAGE_ACME, "/com/acme/{generic}");
        assertEquals(1, bean.getUriParams().length);
        assertEquals("generic", bean.getUriParams()[0]);

        bean = new GenericResourceBean("TestGenericResource", PACKAGE_ACME, "/acme");
        assertEquals(0, bean.getUriParams().length);
    }
    
    public void testGenerate() throws Exception {
        GenericResourceBean bean = new GenericResourceBean("TestGenericResource", PACKAGE_ACME, "/com/acme/{generic}");
        //GenericResourceCodeGenerator generator = new GenericResourceCodeGenerator(entityClassDirFO, bean);
        //FIXME
        //generator.generate();
    }
}
