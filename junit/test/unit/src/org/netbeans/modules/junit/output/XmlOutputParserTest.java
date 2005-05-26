/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.*;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.regex.Pattern;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author Marian Petras
 */
public class XmlOutputParserTest extends TestCase {
    
    /** */
    private final Constructor constructor;
    /** */
    private final Method methodGetStackTrace;
    /** */
    private XmlOutputParser instance;
    
    /**
     */
    public XmlOutputParserTest(String testName) throws NoSuchMethodException,
                                                       NoSuchFieldException,
                                                       IllegalAccessException {
        super(testName);
        
        constructor = XmlOutputParser.class.getDeclaredConstructor(new Class[0]);
        constructor.setAccessible(true);
        
        methodGetStackTrace = XmlOutputParser.class.getDeclaredMethod(
                                    "getStackTrace",
                                    new Class[] {String.class});
        methodGetStackTrace.setAccessible(true);
    }
    
    /**
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(XmlOutputParserTest.class);
        
        return suite;
    }
    
    /**
     */
    public void setUp() throws InstantiationException,
                               IllegalAccessException,
                               InvocationTargetException {
        instance = (XmlOutputParser) constructor.newInstance(null);
    }

    /**
     */
    public void testGetStackTrace() throws IllegalAccessException,
                                           InvocationTargetException {
        String stringToParse;
        String[] expected, actual;
        
        stringToParse = "abcdkockaprede\n" +
                        "pes pocita\n" +
                        "      at Kukuku.bubububu(heheh)";
        expected = new String[] {"Kukuku.bubububu(heheh)"};
        actual = (String[]) methodGetStackTrace.invoke(
                            instance, new Object[] {stringToParse});
        assertArrayEquals(expected, actual);
                          
        stringToParse = "junit.framework.AssertionFailedError: The test case is empty.\n" +
                        "  at javaaaplication2.NewBeanTest.testGetSampleProperty(NewBeanTest.java:54)\n";
        expected = new String[] {
            "javaaaplication2.NewBeanTest.testGetSampleProperty(NewBeanTest.java:54)"};
        actual = (String[]) methodGetStackTrace.invoke(
                            instance, new Object[] {stringToParse});
        assertArrayEquals(expected, actual);
                          
        stringToParse = "\tat kuku.hehe\n" +
                        "\t\tat fuifiu.hyy(fuifiu.java)\n" +
                        "fuska\n" +
                        "\t\tat fsdfas.oio(fsdfas.java)";
        expected = new String[] {"kuku.hehe",
                                 "fuifiu.hyy(fuifiu.java)"};
        actual = (String[]) methodGetStackTrace.invoke(
                            instance, new Object[] {stringToParse});
        assertArrayEquals(expected, actual);
                          
        stringToParse = "  at toto.tamto(Tuhle.java)\n" +
                        "  at jeste.tohle(Tamhle.java)\n" +
                        "  at tadyhle.tu";
        expected = new String[] {"toto.tamto(Tuhle.java)",
                                 "jeste.tohle(Tamhle.java)",
                                 "tadyhle.tu"};
        actual = (String[]) methodGetStackTrace.invoke(
                            instance, new Object[] {stringToParse});
        assertArrayEquals(expected, actual);
                          
        stringToParse = "toto.tamto(Tuhle.java)\n" +
                        "jeste.tohle(Tamhle.java)\n" +
                        "tadyhle.tu";
        expected = null;
        actual = (String[]) methodGetStackTrace.invoke(
                            instance, new Object[] {stringToParse});
        assertArrayEquals(expected, actual);
    }
    
    /**
     */
    private void assertArrayEquals(final Object[] expected,
                                   final Object[] actual) {
        if ((expected == null) != (actual == null)) {
            fail("Object arrays differ - expected: " + getNullStatus(expected)
                 + ", but actual was " + getNullStatus(actual) + '.');
        }
        if (expected == null) {     //i.e. actual is <null>, too
            return;
        }
        if (expected.length != actual.length) {
            fail("Different array lengths - expected: " + expected.length
                 + ", but was: " + actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            Object exp = expected[i];
            Object act = actual[i];
            
            if ((exp == null) != (act == null)) {
                fail("Items at index " + i + " differ - expected: " + getNullStatus(exp)
                     + ", but was " + getNullStatus(act) + '.');
            }
            if (exp == null) {
                continue;           //i.e. act is <null>, too
            }
            if (!exp.equals(act)) {
                fail("Items at index " + i + " differ - expected: " + exp
                     + ", but was " + act + '.');
            }
        }
    }
    
    /**
     */
    private String getNullStatus(Object o) {
        return (o == null) ? "<null>" : "<non-null>";
    }
    
}
