/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.junit;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Enumeration;
import java.awt.Image;
import java.beans.*;
import org.openide.*;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import junit.framework.*;

public class JUnitSettingsBeanInfoTest extends TestCase {

    public JUnitSettingsBeanInfoTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(JUnitSettingsBeanInfoTest.class);
        suite.addTest(BoolPropEdTest.suite());
        suite.addTest(SortedListPropEdTest.suite());
        suite.addTest(ExecutorPropEdTest.suite());
        suite.addTest(FileSystemPropEdTest.suite());
        
        return suite;
    }
    
    /** Test of getPropertyDescriptors method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo. */
    public void testGetPropertyDescriptors() {
        System.out.println("testGetPropertyDescriptors");
        PropertyDescriptor d[] = jbi.getPropertyDescriptors();
        assertTrue(null != d);
    }
    
    /** Test of getIcon method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo. */
    public void testGetIcon() {
        System.out.println("testGetIcon");
        Image i = jbi.getIcon(BeanInfo.ICON_COLOR_16x16);
        assertTrue(null != i);
    }
    
    public static class BoolPropEdTest extends TestCase {

        public BoolPropEdTest(java.lang.String testName) {
            super(testName);
        }        
        
        public static void main(java.lang.String[] args) {
            junit.textui.TestRunner.run(suite());
        }
        
        public static Test suite() {
            TestSuite suite = new TestSuite(BoolPropEdTest.class);
            
            return suite;
        }
        
        /** Test of getTags method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo.BoolPropEd. */
        public void testGetTags() {
            System.out.println("testGetTags");
            String tags[] = bped.getTags();
            assertTrue(null != tags);
            assertTrue(2 == tags.length);
        }
        
        /** Test of getAsText method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo.BoolPropEd. */
        public void testGetAsText() {
            System.out.println("testGetAsText");
            String s = bped.getAsText();
            assertTrue(null != s);
        }
        
        /** Test of setAsText method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo.BoolPropEd. */
        public void testSetAsText() {
            System.out.println("testSetAsText");
            // remeber the value
            String s = bped.getAsText();
            String tags[] = bped.getTags();
            if (0 < tags.length) {
                bped.setAsText(tags[0]);
                assertEquals(tags[0], bped.getAsText());
                if (1 < tags.length) {
                    bped.setAsText(tags[1]);
                    assertEquals(tags[1], bped.getAsText());
                }
            }
            bped.setAsText(s);
        }
        
        // protected members
        protected JUnitSettingsBeanInfo.BoolPropEd bped;
        
        protected void setUp() {
            bped = new JUnitSettingsBeanInfo.BoolPropEd();
        }
    }
    
    public static class SortedListPropEdTest extends TestCase {

        public SortedListPropEdTest(java.lang.String testName) {
            super(testName);
        }        
        
        public static void main(java.lang.String[] args) {
            junit.textui.TestRunner.run(suite());
        }
        
        public static Test suite() {
            TestSuite suite = new TestSuite(SortedListPropEdTest.class);
            
            return suite;
        }
        
        /** Test of getTags method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo.SortedListPropEd. */
        public void testGetTags() {
            System.out.println("testGetTags");
            String tags[] = ped.getTags();
            assertTrue(null != tags);
            assertTrue(2 == tags.length);
        }
        
        /** Test of getAsText method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo.SortedListPropEd. */
        public void testGetAsText() {
            System.out.println("testGetAsText");
            String s = ped.getAsText();
            assertTrue(null != s);
        }
        
        /** Test of setAsText method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo.SortedListPropEd. */
        public void testSetAsText() {
            System.out.println("testSetAsText");
            // remeber the value
            String s = ped.getAsText();
            String tags[] = ped.getTags();
            if (0 < tags.length) {
                ped.setAsText(tags[0]);
                assertEquals(tags[0], ped.getAsText());
                if (1 < tags.length) {
                    ped.setAsText(tags[1]);
                    assertEquals(tags[1], ped.getAsText());
                }
            }
            ped.setAsText(s);
        }
        
        /** Test of put method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo.SortedListPropEd. */
        public void testPut() {
            System.out.println("testPut");
            
            // Add your test code below by replacing the default call to fail.
            fail("The test case is empty.");
        }

        // protected members
        JUnitSettingsBeanInfo.SortedListPropEd ped;
        
        protected void setUp() {
            ped = new JUnitSettingsBeanInfo.SortedListPropEd();
            ped.put("display3", "value3", JUnitSettingsBeanInfo.SortedListPropEd.SHOW_IN_LIST | JUnitSettingsBeanInfo.SortedListPropEd.IS_DEFAULT);
            ped.put("display1", "value1", JUnitSettingsBeanInfo.SortedListPropEd.SHOW_IN_LIST);
            ped.put("display2", "value2", JUnitSettingsBeanInfo.SortedListPropEd.IS_DEFAULT);
        }
    }
    
    public static class ExecutorPropEdTest extends TestCase {

        public ExecutorPropEdTest(java.lang.String testName) {
            super(testName);
        }        
        
        public static void main(java.lang.String[] args) {
            junit.textui.TestRunner.run(suite());
        }
        
        public static Test suite() {
            TestSuite suite = new TestSuite(ExecutorPropEdTest.class);
            
            return suite;
        }
        
        /** Test of getTags method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo.ExecutorPropEd. */
        public void testGetTags() {
            System.out.println("testGetTags");
            String tags[] = ped.getTags();
            assertTrue(null != tags);
            assertTrue(3 == tags.length);
        }
        
        /** Test of getAsText method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo.ExecutorPropEd. */
        public void testGetAsText() {
            System.out.println("testGetAsText");
            String s = ped.getAsText();
            assertTrue(null != s);
        }
        
        /** Test of setAsText method, of class org.netbeans.modules.junit.JUnitSettingsBeanInfo.ExecutorPropEd. */
        public void testSetAsText() {
            System.out.println("testSetAsText");
            String s = ped.getAsText();
            String tags[] = ped.getTags();
            if (0 < tags.length) {
                ped.setAsText(tags[0]);
                assertEquals(tags[0], ped.getAsText());
                if (1 < tags.length) {
                    ped.setAsText(tags[1]);
                    assertEquals(tags[1], ped.getAsText());
                }
            }
            ped.setAsText(s);
        }
        
        // protected members
        protected JUnitSettingsBeanInfo.ExecutorPropEd ped;
        
        protected void setUp() {
            ped = new JUnitSettingsBeanInfo.ExecutorPropEd();
        }
    }
    
    public static class FileSystemPropEdTest extends TestCase {

        public FileSystemPropEdTest(java.lang.String testName) {
            super(testName);
        }        
        
        public static void main(java.lang.String[] args) {
            junit.textui.TestRunner.run(suite());
        }
        
        public static Test suite() {
            TestSuite suite = new TestSuite(FileSystemPropEdTest.class);
            
            return suite;
        }
        
        public void testDummy() {
            // there is nothing to test in this class
            // this exists here just to avoid - suite empty - error message
        }
    }
    
    // protected members
    JUnitSettingsBeanInfo jbi;
    
    protected void setUp() {
        jbi = new JUnitSettingsBeanInfo();
    }
}
