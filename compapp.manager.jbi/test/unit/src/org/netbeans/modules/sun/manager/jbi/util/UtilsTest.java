/*
 * UtilsTest.java
 * JUnit 4.x based test
 *
 * Created on October 15, 2007, 4:05 PM
 */

package org.netbeans.modules.sun.manager.jbi.util;

import com.sun.jbi.ui.common.JBIComponentInfo;
import java.util.Map;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jqian
 */
public class UtilsTest {
    
    public UtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void getIntrospectedPropertyMap() {
        System.out.println("getIntrospectedPropertyMap");
        
        JBIComponentInfo bean = new JBIComponentInfo();
        Map<Attribute, MBeanAttributeInfo> map = 
                Utils.getIntrospectedPropertyMap(bean, true,
                "org.netbeans.modules.sun.manager.jbi.management.model.beaninfo");
                
        assertNotNull(map);
    }

    @Test
    public void wordWrapString() {
        System.out.println("wordWrapString");
        
        int maxLineLength = 10;
        String newLineChars = "&";
        
        String input = "[abc]";
        String expResult = "[abc]&";
        String result = Utils.wordWrapString(input, maxLineLength, newLineChars);
        assertEquals(expResult, result);
        
        input = "abc def ghi jkl mno pqr stu vwx yz";
        expResult = "abc def &ghi jkl &mno pqr &stu vwx yz&";
        result = Utils.wordWrapString(input, maxLineLength, newLineChars);
        assertEquals(expResult, result);
       
        input = "abcdefghijklmnopqrstuvwxyz";
        expResult = "abcdefghij&klmnopqrst&uvwxyz&";
        result = Utils.wordWrapString(input, maxLineLength, newLineChars);
        assertEquals(expResult, result);        
        
    } /* Test of wordWrapString method, of class Utils. */
    
}
