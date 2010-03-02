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
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.debugger.gdb.models.AbstractVariable;
import org.netbeans.modules.cnd.debugger.gdb.models.GdbLocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.models.GdbWatchVariable;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
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
        createLocalVariable("test", "type", "value", "ptype");
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
        AbstractVariable var = createLocalVariable("test", "int", "5", "int");

        // Should have no fields
        assertEquals(0, var.getFieldsCount());
    }

    @Test
    public void testLocalString() {
        AbstractVariable var = createLocalVariable("test", "char *", "\"abcd\"", "char *");

        // Should have no fields
        assertEquals(0, var.getFieldsCount());
    }

    @Test
    public void testLocalStringSTD() {
        AbstractVariable var = createLocalVariable(
                "test",
                "string",
                VariableInfo.getStDStringValue("abc"),
                VariableInfo.STD_STRING_PTYPE);

        // Should have 2 fields
        assertTrue(var.getFieldsCount() > 0);
        assertEquals(2, var.getFields().length);
    }

    @Test
    public void testLocalStringSTDMac() {
        ((MockGdbDebugger)debugger).setPlatform(PlatformTypes.PLATFORM_MACOSX);

        String value = VariableInfo.getStDStringValueMac("abc");

        AbstractVariable var = createLocalVariable(
                "test",
                "string &",
                value,
                VariableInfo.STD_STRING_PTYPE_MAC,
                true);

        assertEquals("Incorrect value,", GdbUtils.mackHack(value), var.getValue());

        // Should have 2 fields
        assertTrue(var.getFieldsCount() > 0);
        assertEquals(2, var.getFields().length);
    }

    @Test
    public void testLocalIntArray() {
        AbstractVariable var = createLocalVariable("test", "int[2]", "{5, 4}", "int[2]");

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("int", var.getFields()[0].getType());
        assertEquals("5", var.getFields()[0].getValue());
        assertEquals("int", var.getFields()[1].getType());
        assertEquals("4", var.getFields()[1].getValue());
    }

    @Test
    public void testLocalIntRepeatingArray() {
        AbstractVariable var = createLocalVariable("test", "int[3]", "{5, 4 <repeats 2 times>}", "int[3]");

        // Should have 3 fields
        assertEquals(3, var.getFields().length);
        assertEquals("int", var.getFields()[0].getType());
        assertEquals("5", var.getFields()[0].getValue());
        assertEquals("int", var.getFields()[1].getType());
        assertEquals("4", var.getFields()[1].getValue());
        assertEquals("int", var.getFields()[2].getType());
        assertEquals("4", var.getFields()[2].getValue());
    }

    @Test
    public void testLocalCharArray() {
        AbstractVariable var = createLocalVariable("test", "char[2]", "\"a\", \"b\"", "char[2]");

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("char", var.getFields()[0].getType());
        assertEquals("\"a\"", var.getFields()[0].getValue());
        assertEquals("char", var.getFields()[1].getType());
        assertEquals("\"b\"", var.getFields()[1].getValue());
    }

    @Test
    public void testLocalCharRepeatingArray() {
        AbstractVariable var = createLocalVariable("test", "char[3]", "\\\"a\\\", 'b' <repeats 2 times>", "char[3]", true);

        // FIXME: Value changes in reality - not good
        //assertEquals("Incorrect value,", value, var.getValue());

        // Should have 3 fields
        assertEquals(3, var.getFields().length);
        assertEquals("char", var.getFields()[0].getType());
        assertEquals("'a'", var.getFields()[0].getValue());
        assertEquals("char", var.getFields()[1].getType());
        assertEquals("'b'", var.getFields()[1].getValue());
        assertEquals("char", var.getFields()[2].getType());
        assertEquals("'b'", var.getFields()[2].getValue());
    }

    @Test
    public void testLocalIntArray2() {
        AbstractVariable var = createLocalVariable("test", "int[2][3]", "{{5, 4, 3},{2, 1, 0}}", "int[2][3]");

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("int[3]", var.getFields()[0].getType());
        assertEquals("{5, 4, 3}", var.getFields()[0].getValue());
        assertEquals("int[3]", var.getFields()[1].getType());
        assertEquals("{2, 1, 0}", var.getFields()[1].getValue());
    }

    @Test
    public void testLocalCharArray2() {
        AbstractVariable var = createLocalVariable("test", "char[2][3]", "{\"abc\", \"xyz\"}", "char[2][3]");

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("char[3]", var.getFields()[0].getType());
        assertEquals("\"abc\"", var.getFields()[0].getValue());
        assertEquals("char[3]", var.getFields()[1].getType());
        assertEquals("\"xyz\"", var.getFields()[1].getValue());
    }

    @Test
    public void testUnnamedUnion() { // IZ 131429
        AbstractVariable var = createLocalVariable(
                "test",
                "A",
                "{{a = 12, b = 23}}",
                "class A {\\n  public:\\n    union {\\n        int a;\\n        int b;\\n    };\\n}\\n");

        // Should have 1 field
        assertEquals(1, var.getFields().length);
        assertEquals("union", var.getFields()[0].getType());
        assertEquals("{a = 12, b = 23}", var.getFields()[0].getValue());
    }

    @Test
    public void testBaseClass1() { // IZ 163290
        AbstractVariable var = createLocalVariable(
                "this",
                "B * const",
                "(B * const) 0x80477c4",
                "class B : public C {\\n  private:\\n    int a;\\n\\n  public:\\n    void foo();\\n} * const\\n");

        ((MockGdbDebugger)debugger).addVar(
                "*this",
                null,//"B * const",
                "{<C> = {e = 134550490}, a = -16795724}");

        ((MockGdbDebugger)debugger).addVar(
                "C",
                null,
                null,
                "class C {\\n  public:\\n    int e;\\n}\\n"
                );

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("<Base class>", var.getFields()[0].getName());
        assertEquals(1, ((AbstractVariable)var.getFields()[0]).getFields().length);
        assertEquals("e", ((AbstractVariable)var.getFields()[0]).getFields()[0].getName());
        assertEquals("int", ((AbstractVariable)var.getFields()[0]).getFields()[0].getType());
    }

    @Test
    public void testBaseClass2() { // IZ 163290
        AbstractVariable var = createLocalVariable(
                "this",
                "B * const",
                "(B * const) 0x80477c4",
                "class B : public C {\\n  private:\\n    int a;\\n\\n  public:\\n    void foo();\\n} * const\\n");

        ((MockGdbDebugger)debugger).addVar(
                "*this",
                null,//"B * const",
                "{<C> = {e = 5.2240448966420603e-270}, a = 134510512}");

        ((MockGdbDebugger)debugger).addVar(
                "C",
                null,
                null,
                "class C {\\n  public:\\n    double e;\\n}\\n"
                );

        // Should have 2 fields
        assertEquals(2, var.getFields().length);
        assertEquals("<Base class>", var.getFields()[0].getName());
        assertEquals(1, ((AbstractVariable)var.getFields()[0]).getFields().length);
        assertEquals("e", ((AbstractVariable)var.getFields()[0]).getFields()[0].getName());
        assertEquals("double", ((AbstractVariable)var.getFields()[0]).getFields()[0].getType());
    }

    @Test
    public void testVirtualBase() { // IZ 175250
        AbstractVariable var = createLocalVariable(
                "this",
                "VirtualArrayBase * const",
                "0x83e54ac",
                "class VirtualArrayBase : public VirtualBase {\\n } * const\\n");

        ((MockGdbDebugger)debugger).addVar(
                "*this",
                null,//"B * const",
                "{<VirtualBase> = {_vptr.VirtualBase = 0x838fb88, m_map = @0x83e5530, m_elemSize = 4, m_virtualSize = 8991, m_lok = {mutex_ = {__data = {__lock = 5624, __count = 1, __owner = 5624, __kind = 32, __nusers = 1, {__spins = 0, __list = {__next = 0x0}}}, __size = \"\\370\\025\\000\\000\\001\\000\\000\\000\\370\\025\\000\\000 \\000\\000\\000\\001\\000\\000\\000\\000\\000\\000\", __align = 5624}, locker_ = 3868924816, lockerp_ = -1, state_ = true}}, m_segAlloc = {<DLinkedListBase> = {m_head = 0xe4a01234, m_tail = 0xe4afafc4, m_size = 2}, <No data fields>}, m_segFree = {<DLinkedListBase> = {m_head = 0xe4a01e34, m_tail = 0xe4a01e34, m_size = 1}, <No data fields>}, m_spare = 0x0, m_physicalSize = 256, m_errorReported = false, m_stats = {pageIn = 25, pageOut = 82, nActive = 2, maxActive = 2, nLowUsage = 0, nLowUseReport = 8}}");
        ((MockGdbDebugger)debugger).addVar(
                "VirtualBase",
                null,
                null,
                "Type VirtualArrayBase has no component named VirtualBase");

        // Should have 1 field
        assertEquals(7, var.getFields().length);
        assertEquals("<Base class>", var.getFields()[0].getName());
        assertEquals(4, ((AbstractVariable)var.getFields()[0]).getFields().length);
        assertEquals("m_map", ((AbstractVariable)var.getFields()[0]).getFields()[0].getName());
        assertEquals("m_lok", ((AbstractVariable)var.getFields()[0]).getFields()[3].getName());
//        assertEquals("e", ((AbstractVariable)var.getFields()[0]).getFields()[0].getName());
    }

    private AbstractVariable createLocalVariable(String name, String type, String value, String ptype) {
        return createLocalVariable(name, type, value, ptype, false);
    }

    private AbstractVariable createLocalVariable(String name, String type, String value, String ptype, boolean noValueCheck) {
        ((MockGdbDebugger)debugger).addVar(name, type, value, ptype);
        AbstractVariable res = new GdbLocalVariable(debugger, new GdbVariable(name, value));
        assertEquals("Incorrect name,", name, res.getName());
        assertEquals("Incorrect type,", type, res.getType());
        if (!noValueCheck) {
            assertEquals("Incorrect value,", value, res.getValue());
        }
        return res;
    }

    private static class MockGdbDebugger extends GdbDebugger {
        private final Map<String, String> values = new HashMap<String, String>();
        private final Map<String, String> types = new HashMap<String, String>();
        private final Map<String, String> ptypes = new HashMap<String, String>();

        private int platform = PlatformTypes.PLATFORM_SOLARIS_INTEL;

        public MockGdbDebugger(ContextProvider lookupProvider) {
            super(lookupProvider);
            state = State.STOPPED;
        }

        @Override
        public int getPlatform() {
            return platform;
        }

        public void setPlatform(int platform) {
            this.platform = platform;
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
        public String requestValueEx(String name) throws GdbErrorException {
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
        public String requestBaseClassType(String name) {
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
