/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.cnd.debugger.gdb.models.AbstractVariable;
import org.netbeans.modules.cnd.debugger.gdb.models.GdbLocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.models.GdbWatchVariable;
import org.netbeans.spi.debugger.ContextProvider;

/**
 *
 * @author Egor Ushakov
 */
public class VariablesTestCase extends GdbTestCase {

    public VariablesTestCase(String name) {
        super(name);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        debugger = new MockGdbDebugger(this);
    }

    @Test
    public void testLocalSimple() {
        String name = "test";
        String type = "type";
        String value = "value";
        ((MockGdbDebugger)debugger).addVar(name, type, value);
        
        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());
    }

    @Test
    public void testWatchSimple() {
        String name = "test";
        String type = "type";
        String value = "value";
        ((MockGdbDebugger)debugger).addVar(name, type, value);

        AbstractVariable var = new GdbWatchVariable(debugger, dm.createWatch(name));

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());
    }

    @Test
    public void testLocalInt() {
        String name = "test";
        String type = "int";
        String value = "5";
        ((MockGdbDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have no fields
        assertEquals(0, var.getFieldsCount());
    }

    @Test
    public void testLocalString() {
        String name = "test";
        String type = "char *";
        String value = "\"abcd\"";
        ((MockGdbDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have no fields
        assertEquals(0, var.getFieldsCount());
    }

    @Test
    public void testLocalStringSTD() {
        String name = "test";
        String type = "string";
        String value = VariableInfo.getStDStringValue("abc");
        ((MockGdbDebugger)debugger).addVar(name, type, value, VariableInfo.STD_STRING_PTYPE);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have no fields
        assertTrue(var.getFieldsCount() > 0);
        assertEquals(2, var.getFields().length);
    }

    @Test
    public void testLocalIntArray() {
        String name = "test";
        String type = "int[2]";
        String value = "{5, 4}";
        ((MockGdbDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("5", var.getFields()[0].getValue());
        assertEquals("4", var.getFields()[1].getValue());
    }

    @Test
    public void testLocalIntRepeatingArray() {
        String name = "test";
        String type = "int[3]";
        String value = "{5, 4 <repeats 2 times>}";
        ((MockGdbDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have 3 fields
        assertEquals(3, var.getFields().length);
        assertEquals("5", var.getFields()[0].getValue());
        assertEquals("4", var.getFields()[1].getValue());
        assertEquals("4", var.getFields()[2].getValue());
    }

    @Test
    public void testLocalCharArray() {
        String name = "test";
        String type = "char[2]";
        String value = "\"a\", \"b\"";
        ((MockGdbDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        assertEquals("Incorrect value,", value, var.getValue());

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("\"a\"", var.getFields()[0].getValue());
        assertEquals("\"b\"", var.getFields()[1].getValue());
    }

    @Test
    public void testLocalCharRepeatingArray() {
        String name = "test";
        String type = "char[3]";
        String value = "\\\"a\\\", 'b' <repeats 2 times>";
        ((MockGdbDebugger)debugger).addVar(name, type, value, type);

        AbstractVariable var = new GdbLocalVariable(debugger, name);

        assertEquals("Incorrect name,", name, var.getName());
        assertEquals("Incorrect type,", type, var.getType());
        // FIXME: Value changes in reality - not good
        //assertEquals("Incorrect value,", value, var.getValue());

        // Should have 3 fields
        assertEquals(3, var.getFields().length);
        assertEquals("'a'", var.getFields()[0].getValue());
        assertEquals("'b'", var.getFields()[1].getValue());
        assertEquals("'b'", var.getFields()[2].getValue());
    }

    private static class MockGdbDebugger extends GdbDebugger {
        private final Map<String, String> values = new HashMap<String, String>();
        private final Map<String, String> types = new HashMap<String, String>();
        private final Map<String, String> ptypes = new HashMap<String, String>();

        public MockGdbDebugger(ContextProvider lookupProvider) {
            super(lookupProvider);
            state = State.STOPPED;
        }
        
        private void addVar(String name, String type, String value) {
            values.put(name, value);
            types.put(name, type);
        }

        private void addVar(String name, String type, String value, String ptype) {
            values.put(name, value);
            types.put(name, type);
            ptypes.put(name, ptype);
        }

        @Override
        public String requestValue(String name) {
            String res = values.get(name);
            assertNotNull("Requesting value for unknown " + name, res);
            return res;
        }

        @Override
        public String requestSymbolTypeFromName(String name) {
            String res = ptypes.get(name);
            assertNotNull("Requesting ptype for unknown " + name, res);
            return res;
        }


        @Override
        public String requestWhatis(String name) {
            String res = types.get(name);
            assertNotNull("Requesting type for unknown " + name, res);
            return res;
        }

        @Override
        public String evaluate(String expression) {
            return requestValue(expression);
        }
    }
}
