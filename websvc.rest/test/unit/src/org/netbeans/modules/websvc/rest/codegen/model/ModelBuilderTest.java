/*
 * ModelBuilderTest.java
 * JUnit 4.x based test
 *
 * Created on May 24, 2007, 8:50 AM
 */

package org.netbeans.modules.websvc.rest.codegen.model;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.rest.codegen.TestBase;

/**
 *
 * @author nam
 */
public class ModelBuilderTest extends TestBase {
    
    public ModelBuilderTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
    }

    public void testBuildModel() throws Exception {
        /*ModelBuilder mb = new ModelBuilder(getEntities());
        ResourceBeanModel model = mb.build();
        assertEquals(14, model.getResourceBeans().size());
        ResourceBean container = model.getContainerResourceBean("com.acme.Customer");
        ResourceBean item = model.getItemResourceBean("com.acme.Customer");
        assertEquals(1, container.getSubResources().size());
        assertEquals("Customer", container.getSubResources().iterator().next().getResourceBean().getName());
        assertEquals(2, item.getSubResources().size());
        List<RelatedResource> subResources = new ArrayList<RelatedResource>(item.getSubResources());
        assertEquals("DiscountCode", subResources.get(0).getResourceBean().getName());*/
    }
}
