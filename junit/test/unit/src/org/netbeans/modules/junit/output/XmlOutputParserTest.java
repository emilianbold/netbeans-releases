/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.junit.output;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 *
 * @author Marian Petras
 */
public class XmlOutputParserTest extends TestCase {

    private final Constructor constructor;
    private final Method methodGetStackTrace;
    private XmlOutputParser instance;

    public XmlOutputParserTest(String testName) throws NoSuchMethodException,
                                                       NoSuchFieldException,
                                                       IllegalAccessException {
        super(testName);
        // XXX the following belongs in setUp:
        constructor = XmlOutputParser.class.getDeclaredConstructor(new Class[0]);
        constructor.setAccessible(true);
        
        methodGetStackTrace = XmlOutputParser.class.getDeclaredMethod(
                                    "getStackTrace",
                                    new Class[] {String.class});
        methodGetStackTrace.setAccessible(true);
    }
    
    protected void setUp() throws Exception {
        instance = (XmlOutputParser) constructor.newInstance((Object[]) null);
    }

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
    
    private String getNullStatus(Object o) {
        return (o == null) ? "<null>" : "<non-null>";
    }
    
}
