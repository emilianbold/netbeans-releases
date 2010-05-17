/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.Properties;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSDTestBase;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;

/**
 *
 * @author joelle
 */
public class NbJSBreakpointsReaderTest extends NbJSDTestBase {
    
    public NbJSBreakpointsReaderTest(String testName) {
        super(testName);
    }            

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        List<NbJSDTestBase> tests = getTests();
        for (NbJSDTestBase test : tests) {
            suite.addTest(test);
        }
        return suite;
    }

    public static List<NbJSDTestBase> getTests() {
        List<NbJSDTestBase> tests = new ArrayList<NbJSDTestBase>();
        tests.add(new NbJSBreakpointsReaderTest("testGetSupportedClassNames"));
        tests.add(new NbJSBreakpointsReaderTest("testWrite"));
        tests.add(new NbJSBreakpointsReaderTest("testWrite"));
        tests.add(new NbJSBreakpointsReaderTest("testRead"));
        tests.add(new NbJSBreakpointsReaderTest("testRead2"));
        return tests;
    }
    
    /**
     * Test of getSupportedClassNames method, of class NbJSBreakpointsReader.
     */
    public void testGetSupportedClassNames() {
        System.out.println("getSupportedClassNames");
        NbJSBreakpointsReader instance = new NbJSBreakpointsReader();
        assertNotNull(instance);
        String expResult = NbJSFileObjectBreakpoint.class.getName();
        String[] result = instance.getSupportedClassNames();
        assertEquals("The result should be: " + expResult, expResult, result[0]);
        assertEquals(1, result.length);
    }
    
    /**
     * Test of write method, of class NbJSBreakpointsReader.
     */
    public void testWrite() throws IOException {
        System.out.println("write");
        Object object = null;
        Properties props = new PropertiesImpl();
        
        FileObject jsFO = createJSFO();
        assertNotNull(jsFO);
        
        NbJSFileObjectBreakpoint bp = addBreakpoint(jsFO, 3);
        assertNotNull(bp);
        
        NbJSBreakpointsReader instance = new NbJSBreakpointsReader();
        instance.write(bp, props);
        int expValue1 = bp.getLine().getLineNumber();
        int retValue1 = props.getInt("lineNumber", 0);
        assertEquals("Property Line Number Should have been set", expValue1, retValue1);
        
        String expValue2 = jsFO.getURL().toString();
        String retValue2 = props.getString("url", "");
        assertEquals("Property url should have been set", expValue2, retValue2);      
    }

    /**
     * Test of read method, of class NbJSBreakpointsReader.
     */
    public void testRead() {
        System.out.println("read");
        String typeID = "";
        Properties props = null;
        NbJSBreakpointsReader instance = new NbJSBreakpointsReader();
        
        
        Object result = instance.read("SomeUnownType", props);
        assertNull("Needs to be of type NbJSBreakpoint", result);
        boolean npethrown = false;
        try {
            Object result2 = instance.read(NbJSFileObjectBreakpoint.class.getName(), null );
        } catch ( NullPointerException npe ){
            npethrown = true;
        }
        assertTrue("An NPE should have been thrown when properties is null.", npethrown);

    }
    
    
    public void testRead2() throws IOException {
        Properties props = new PropertiesImpl();
        
        FileObject jsFO = createJSFO();
        assertNotNull(jsFO);
        
        NbJSFileObjectBreakpoint bp = addBreakpoint(jsFO, 3);
        assertNotNull(bp);
                
        NbJSBreakpointsReader instance2 = new NbJSBreakpointsReader();
        instance2.write(bp, props);
        
        Line expResult = bp.getLine();
        NbJSFileObjectBreakpoint resultBP = (NbJSFileObjectBreakpoint)instance2.read(NbJSFileObjectBreakpoint.class.getName(), props);

        assertEquals(expResult.getLineNumber(), resultBP.getLine().getLineNumber());

    }


    
    private class PropertiesImpl extends Properties {

        Map<String, Object> map;

        public PropertiesImpl() {
            map = new HashMap<String,Object>();
        }
        
        
        @Override
        public String getString(String propertyName, String defaultValue) {
            return (String)map.get(propertyName);
        }

        @Override
        public void setString(String propertyName, String value) {
            map.put(propertyName, value);
        }

        @Override
        public int getInt(String propertyName, int defaultValue) {
            return ((Integer)map.get(propertyName)).intValue();
        }

        @Override
        public void setInt(String propertyName, int value) {
            map.put(propertyName, value);
        }

        @Override
        public char getChar(String propertyName, char defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setChar(String propertyName, char value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float getFloat(String propertyName, float defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFloat(String propertyName, float value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getLong(String propertyName, long defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLong(String propertyName, long value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getDouble(String propertyName, double defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDouble(String propertyName, double value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean getBoolean(String propertyName, boolean defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setBoolean(String propertyName, boolean value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte getByte(String propertyName, byte defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setByte(String propertyName, byte value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public short getShort(String propertyName, short defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setShort(String propertyName, short value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getObject(String propertyName, Object defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setObject(String propertyName, Object value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object[] getArray(String propertyName, Object[] defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setArray(String propertyName, Object[] value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection getCollection(String propertyName, Collection defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCollection(String propertyName, Collection value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map getMap(String propertyName, Map defaultValue) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setMap(String propertyName, Map value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Properties getProperties(String propertyName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

}
