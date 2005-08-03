/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.mbeanwizard.generator.test;
import java.io.BufferedReader;
import junit.framework.*;
import org.openide.loaders.TemplateWizard;
import org.netbeans.junit.*;
import java.util.Enumeration;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Set;

import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.MBeanDO;
import org.netbeans.modules.jmx.mbeanwizard.generator.MBeanGenInfo;
import org.netbeans.modules.jmx.mbeanwizard.generator.Translator;
        
/**
 * MBeanGeneratorTest.java 
 * JUnit based test
 */
public class MBeanGeneratorTest extends TestCase {
    
    private TemplateWizard wizard;
    private MBeanDO mbean;
    private MBeanGenInfo genInfo;

    public MBeanGeneratorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
        wizard = new TemplateWizard();
    }

    protected void tearDown() throws java.lang.Exception {
        wizard = null;
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = 
                new junit.framework.TestSuite(MBeanGeneratorTest.class);
        
        return suite;
    }
    
    public void test0() throws Exception {
        System.out.println("##### Test 0 #####");
        fillWizardDescriptor(wizard, "Test0");
        try {
            createMBean(wizard);
            fail("MBean generation without template must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("template is null"));
        }
    }
    
    public void test1() throws Exception {
        System.out.println("##### Test 1 #####");
        fillWizardDescriptor(wizard, "Test1");
        try {
            createMBean(wizard);
            fail("MBean generation without introspected attribute name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute name invalid"));
        }
    }
    
    public void test2() throws Exception {
        System.out.println("##### Test 2 #####");
        fillWizardDescriptor(wizard, "Test2");
        try {
            createMBean(wizard);
            fail("MBean generation with empty introspected attribute name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute name invalid"));
        }
    }
    
   public void test3() throws Exception {
        System.out.println("##### Test 3 #####");
        fillWizardDescriptor(wizard, "Test3");
        try {
            createMBean(wizard);
            fail("MBean generation without introspected attribute type must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute type invalid"));
        }
    }
   
   public void test4() throws Exception {
        System.out.println("##### Test 4 #####");
        fillWizardDescriptor(wizard, "Test4");
        try {
            createMBean(wizard);
            fail("MBean generation with empty introspected attribute type must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute type invalid"));
        }
    }
   
   public void test5() throws Exception {
        System.out.println("##### Test 5 #####");
        fillWizardDescriptor(wizard, "Test5");
        try {
            createMBean(wizard);
            fail("MBean generation without introspected attribute access must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute access invalid"));
        }
    }
   
   public void test6() throws Exception {
        System.out.println("##### Test 6 #####");
        fillWizardDescriptor(wizard, "Test6");
        try {
            createMBean(wizard);
            fail("MBean generation with not valid introspected attribute access must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute access invalid"));
        }
    }
   
   public void test7() throws Exception {
        System.out.println("##### Test 7 #####");
        fillWizardDescriptor(wizard, "Test7");
        try {
            createMBean(wizard);
            fail("MBean generation without introspected attribute description must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute description invalid"));
        }
    }
   
   public void test8() throws Exception {
        System.out.println("##### Test 8 #####");
        fillWizardDescriptor(wizard, "Test8");
        try {
            createMBean(wizard);
            fail("MBean generation without introspected attribute select tag must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute selected invalid"));
        }
    }
   
   public void test9() throws Exception {
        System.out.println("##### Test 9 #####");
        fillWizardDescriptor(wizard, "Test9");
        try {
            createMBean(wizard);
            fail("MBean generation without introspected operation name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("operation name invalid"));
        }
    }
    
    public void test10() throws Exception {
        System.out.println("##### Test 10 #####");
        fillWizardDescriptor(wizard, "Test10");
        try {
            createMBean(wizard);
            fail("MBean generation with empty introspected operation name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("operation name invalid"));
        }
    }
    
    public void test11() throws Exception {
        System.out.println("##### Test 11 #####");
        fillWizardDescriptor(wizard, "Test11");
        try {
            createMBean(wizard);
            fail("MBean generation without first operation name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("operation name invalid"));
        }
    }
    
    public void test12() throws Exception {
        System.out.println("##### Test 12 #####");
        fillWizardDescriptor(wizard, "Test12");
        try {
            createMBean(wizard);
            fail("MBean generation with empty operation name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("operation name invalid"));
        }
    }
    
    public void test13() throws Exception {
        System.out.println("##### Test 13 #####");
        fillWizardDescriptor(wizard, "Test13");
        try {
            createMBean(wizard);
            fail("MBean generation without first operation type must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("operation type invalid"));
        }
    }
    
    public void test14() throws Exception {
        System.out.println("##### Test 14 #####");
        fillWizardDescriptor(wizard, "Test14");
        try {
            createMBean(wizard);
            fail("MBean generation without first operation type must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("operation type invalid"));
        }
    }
    
    public void test15() throws Exception {
        System.out.println("##### Test 15 #####");
        fillWizardDescriptor(wizard, "Test15");
        try {
            createMBean(wizard);
            fail("MBean generation without first operation description must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("operation description invalid"));
        }
    }
    
    public void test16() throws Exception {
        System.out.println("##### Test 16 #####");
        fillWizardDescriptor(wizard, "Test16");
        try {
            createMBean(wizard);
            fail("MBean generation without first notification class must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("notification name invalid"));
        }
    }
    
    public void test17() throws Exception {
        System.out.println("##### Test 17 #####");
        fillWizardDescriptor(wizard, "Test17");
        try {
            createMBean(wizard);
            fail("MBean generation without first notif type must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("notification type invalid"));
        }
    }
    
    public void test18() throws Exception {
        System.out.println("##### Test 18 #####");
        fillWizardDescriptor(wizard, "Test18");
        try {
            createMBean(wizard);
            fail("MBean generation without first notif description must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("notification description invalid"));
        }
    }
    
    public void test19() throws Exception {
        System.out.println("##### Test 19 #####");
        fillWizardDescriptor(wizard, "Test19");
        try {
            createMBean(wizard);
            fail("MBean generation without first notification class must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("notification name invalid"));
        }
    }
    
    public void test20() throws Exception {
        System.out.println("##### Test 20 #####");
        fillWizardDescriptor(wizard, "Test20");
        try {
            createMBean(wizard);
            fail("MBean generation without first attribute name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute name invalid"));
        }
    }
    
    public void test21() throws Exception {
        System.out.println("##### Test 21 #####");
        fillWizardDescriptor(wizard, "Test21");
        try {
            createMBean(wizard);
            fail("MBean generation without first attribute name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute name invalid"));
        }
    }
    
    public void test22() throws Exception {
        System.out.println("##### Test 22 #####");
        fillWizardDescriptor(wizard, "Test22");
        try {
            createMBean(wizard);
            fail("MBean generation without first attribute type must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute type invalid"));
        }
    }
    
    public void test23() throws Exception {
        System.out.println("##### Test 23 #####");
        fillWizardDescriptor(wizard, "Test23");
        try {
            createMBean(wizard);
            fail("MBean generation without first attribute type must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute type invalid"));
        }
    }
    
    public void test24() throws Exception {
        System.out.println("##### Test 24 #####");
        fillWizardDescriptor(wizard, "Test24");
        try {
            createMBean(wizard);
            fail("MBean generation without first attribute access must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute access invalid"));
        }
    }
    
    public void test25() throws Exception {
        System.out.println("##### Test 25 #####");
        fillWizardDescriptor(wizard, "Test25");
        try {
            createMBean(wizard);
            fail("MBean generation with bad attribute access must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute access invalid"));
        }
    }
    
    public void test26() throws Exception {
        System.out.println("##### Test 26 #####");
        fillWizardDescriptor(wizard, "Test26");
        try {
            createMBean(wizard);
            fail("MBean generation without first attribute access must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("attribute description invalid"));
        }
    }
    
    public void test27() throws Exception {
        System.out.println("##### Test 27 #####");
        fillWizardDescriptor(wizard, "Test27");
        try {
            createMBean(wizard);
            fail("MBean generation without MBean name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("MBean Name is null"));
        }
    }
    
    public void test28() throws Exception {
        System.out.println("##### Test 28 #####");
        fillWizardDescriptor(wizard, "Test28");
        try {
            createGenInfo(wizard);
            fail("MBean generation without test class package must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("test class package or path invalid"));
        }
    }
    
    public void test29() throws Exception {
        System.out.println("##### Test 29 #####");
        fillWizardDescriptor(wizard, "Test29");
        try {
            createMBean(wizard);
            fail("MBean generation without MBean type must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("Bad MBean Type"));
        }
    }
    
    public void test30() throws Exception {
        System.out.println("##### Test 30 #####");
        fillWizardDescriptor(wizard, "Test30");
        try {
            createMBean(wizard);
            fail("MBean generation without MBean package name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("MBean package name is null"));
        }
    }
    
    public void test31() throws Exception {
        System.out.println("##### Test 31 #####");
        fillWizardDescriptor(wizard, "Test31");
        try {
            createGenInfo(wizard);
            fail("MBean generation without test class file location must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("test file path invalid"));
        }
    }
    
    public void test32() throws Exception {
        System.out.println("##### Test 32 #####");
        fillWizardDescriptor(wizard, "Test32");
        try {
            createGenInfo(wizard);
            fail("MBean generation without test class file location must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("test file path invalid"));
        }
    }
    
    public void test33() throws Exception {
        System.out.println("##### Test 33 #####");
        fillWizardDescriptor(wizard, "Test33");
        try {
            createGenInfo(wizard);
            fail("MBean generation without test class name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("test class name invalid"));
        }
    }
    
    public void test34() throws Exception {
        System.out.println("##### Test 34 #####");
        fillWizardDescriptor(wizard, "Test34");
        try {
            createGenInfo(wizard);
            fail("MBean generation without test class name must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("test class name invalid"));
        }
    }
    
    public void test35() throws Exception {
        System.out.println("##### Test 35 #####");
        fillWizardDescriptor(wizard, "Test35");
        try {
            createMBean(wizard);
            fail("MBean generation without project location must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("project location is null"));
        }
    }
    
    public void test36() throws Exception {
        System.out.println("##### Test 36 #####");
        fillWizardDescriptor(wizard, "Test36");
        try {
            createMBean(wizard);
            fail("MBean generation without introspected operation type must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("operation type invalid"));
        }
    }
    
    public void test37() throws Exception {
        System.out.println("##### Test 37 #####");
        fillWizardDescriptor(wizard, "Test37");
        try {
            createMBean(wizard);
            fail("MBean generation with empty introspected operation type must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("operation type invalid"));
        }
    }
    
    public void test38() throws Exception {
        System.out.println("##### Test 38 #####");
        fillWizardDescriptor(wizard, "Test38");
        try {
            createMBean(wizard);
            fail("MBean generation without introspected operation description must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("operation description invalid"));
        }
    }
    
    public void test39() throws Exception {
        System.out.println("##### Test 39 #####");
        fillWizardDescriptor(wizard, "Test39");
        try {
            createMBean(wizard);
            fail("MBean generation without introspected operation select tag must fail !");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().equals("operation selected invalid"));
        }
    }
    
    private void fillWizardDescriptor(TemplateWizard wiz, String fileName) 
            throws Exception {
        String pathToDataDir =
                NbTestCase.convertNBFSURL(this.getClass().getResource("data"));
        File myResource =
                new File(pathToDataDir,fileName + ".properties");
        Properties inputProp = new Properties();
        FileInputStream is = new FileInputStream(myResource);
        inputProp.load(is);
        is.close();
        Enumeration keys = inputProp.keys();
        for (; keys.hasMoreElements();) {
            String entry = (String) keys.nextElement();
            String value = inputProp.getProperty(entry);
            if (value.equals("true") || value.equals("false")) {
                wiz.putProperty(entry, Boolean.valueOf(value));
            } else {
                wiz.putProperty(entry, value);
            }
        }
    }
    
    private void createMBean(TemplateWizard wiz) throws Exception {
        mbean = Translator.createMBeanDO(wiz);
    }
    
    private void createGenInfo(TemplateWizard wiz) throws Exception {
        genInfo = Translator.createGenInfo(wiz);
    }
/*    
    private static boolean diffWithPattern(String content, 
            String expectedContentPattern, Properties prop)
    {
        boolean result = true;
        String expectedContent = expectedContentPattern;
        if (prop != null) {
            Set keys = prop.keySet();
            for (Iterator it = keys.iterator(); it.hasNext();) {
                String key = (String) it.next();
                expectedContent = expectedContent.replaceAll(
                        key, (String) prop.getProperty(key));
            }
        }
        int lineNb = countChars(expectedContent,'\n');
        BufferedReader contentBr = 
                new BufferedReader(new StringReader(content));
        BufferedReader expectedContentBr = 
                new BufferedReader(new StringReader(expectedContent));
        try {
            for (int i = 0; i < lineNb; i++) {
                String line = contentBr.readLine();
                String expectedLine = expectedContentBr.readLine();
                if (!expectedLine.endsWith("<current Date and Time>")) {
                    if (!line.equals(expectedLine)) {
                        System.out.println("line " + i + " :" + line);
                        System.out.println("expected line :" + expectedLine);
                        return false;
                    }
                }
            }
        } catch (IOException e) {fail("error of reading line.");}
        return result;
    }
    
    private static String getPatternContent(Class clazz,String fileName) {
        String pathToDataDir =  
                 NbTestCase.convertNBFSURL(clazz.getResource("data/pattern"));
        File file = 
                 new File(pathToDataDir,fileName);
        StringBuffer content = new StringBuffer();
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            fail("file " + fileName + " not found.");
        }
        int ch;
        try {
        while( (ch = is.read( )) != -1 )
            content.append( (char) ch );
        } catch (IOException e) {fail("error while copy file content.");}
        return content.toString();
    }
    
    private static int countChars( String str, char searchChar ) {
        // Count the number of times searchChar occurs in
        // str and return the result.
        int i;     // A position in the string, str.
        char ch;   // A character in the string.
        int count; // Number of times searchChar has been found in str.
        count = 0;
        for ( i = 0;  i < str.length();  i++ ) {
            ch = str.charAt(i);  // Get the i-th character in str.
            if ( ch == searchChar )
                count++;
        }
        return count;
    }
*/
}
