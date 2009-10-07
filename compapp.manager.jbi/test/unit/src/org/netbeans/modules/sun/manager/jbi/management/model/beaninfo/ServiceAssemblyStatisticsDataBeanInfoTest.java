/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.sun.manager.jbi.management.model.beaninfo;

import com.sun.esb.management.common.data.ServiceAssemblyStatisticsData;
import java.util.Map;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.sun.manager.jbi.util.Utils;
import static org.junit.Assert.*;

/**
 *
 * @author jqian
 */
public class ServiceAssemblyStatisticsDataBeanInfoTest {

    public ServiceAssemblyStatisticsDataBeanInfoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testBeanInfo() {
        System.out.println("testBeanInfo");
        
        Map<Attribute, MBeanAttributeInfo> map = 
                Utils.getIntrospectedPropertyMap(
                new ServiceAssemblyStatisticsData(), true,
                "org.netbeans.modules.sun.manager.jbi.management.model.beaninfo");
                
        assertNotNull(map);
    }

}
