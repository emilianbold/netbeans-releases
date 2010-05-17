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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.visualweb.dataprovider;

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.DataListener;
import com.sun.data.provider.impl.ObjectDataProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

/**
 * <p>Unit tests for {@link ObjectDataProvider}.</p>
 */
public class ObjectDataProviderTest extends NbTestCase {


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The bean being wrapped by the {@link DataProvider} under test.
     */
    private TestBean bean = null;


    /**
     * <p>The {@link DataProvider} instance under test.
     */
    private ObjectDataProvider dp = null;


    /**
     * <p>Event listener for event testing.</p>
     */
    private Listener listener = null;


    // -------------------------------------------------------- Static Variables


    // Dummy variables just to provide access to type information
    private static int intArray[] = new int[0];
    private static TestBean nestedArray[] = new TestBean[0];


    /**
     * <p>Descriptors for the set of fieldKeys we expect to be known.</p>
     */
    private static Descriptor[] fieldKeys = {
        // Specific fieldKeys of this class
        new Descriptor("public1", String.class, false, "This is public1"),
        new Descriptor("public2", Integer.class, false, new Integer(8888)),
    };


    /**
     * <p>Descriptors for the set of properties we expect to be known.</p>
     */
    private static Descriptor[] properties = {
        // Specific properties of this class
        new Descriptor("booleanProperty", Boolean.class, false, Boolean.TRUE),
        new Descriptor("byteProperty", Byte.class, false, new Byte((byte) 123)),
        new Descriptor("doubleProperty", Double.class, false, new Double(654.321)),
        new Descriptor("floatProperty", Float.class, false, new Float((float) 123.45)),
        new Descriptor("id", String.class, false, null),
        new Descriptor("intArray", intArray.getClass(), false, null),
        new Descriptor("intList", List.class, false, null),
        new Descriptor("intProperty", Integer.class, false, new Integer(1234)),
        new Descriptor("longProperty", Long.class, false, new Long(54321)),
        new Descriptor("nestedArray", nestedArray.getClass(), false, null),
        new Descriptor("nestedList", List.class, false, null),
        new Descriptor("nestedMap", Map.class, false, null),
        new Descriptor("nestedProperty", TestBean.class, false, null),
        new Descriptor("readOnly", String.class, true, null),
        new Descriptor("shortProperty", Short.class, false, new Short((short) 321)),
        new Descriptor("stringProperty", String.class, false, "This is a String"),
        new Descriptor("nullString", String.class, true, null),
        // Inherited from java.lang.Object
        new Descriptor("class", Class.class, true, null),
    };


    /**
     * <p>Descriptors for updates that should be applied and tested.</p>
     */
    private static Update[] updates = {
        // Updates to read-write properties
        new Update("booleanProperty", Boolean.FALSE),
        new Update("byteProperty", new Byte((byte) 213)),
        new Update("doubleProperty", new Double(123.456)),
        new Update("floatProperty", new Float((float) 111.22)),
        new Update("intProperty", new Integer(23432)),
        new Update("longProperty", new Long((long) 55555)),
        new Update("shortProperty", new Short((short) 123)),
        new Update("stringProperty", "Updated string value"),
        // Public fieldKeys are read-write as well
        new Update("public1", "revised String1 value"),
        new Update("public2", new Integer(55555)),
    };



    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a new test case instance.</p>
     */
    public ObjectDataProviderTest(String name) {
        super(name);
    }


    // ---------------------------------------------------- Overall Test Methods


    /**
     * <p>Set up the instance to be tested.</p>
     */
    @Override
    public void setUp() {
        bean = new TestBean("test");
        dp = new ObjectDataProvider(bean, true);
    }

    /**
     * <p>Tear down the instance from the previous test.</p>
     */
    @Override
    public void tearDown() {
        dp = null;
        bean = null;
    }


    // ------------------------------------------------- Individual Test Methods


    /**
     * <p>Check convenience methods on abstract base class that
     * should still show through the concrete implementation.</p>
     */
    public void testBaseClassMethods() {

        // Operate on FieldKey or field identifier
        for (int i = 0; i < properties.length; i++) {
            String fieldId = properties[i].name;
            FieldKey fieldKey = dp.getFieldKey(fieldId);
            assertEquals("type(" + fieldId + ")",
                         dp.getType(fieldKey),
                         dp.getType(fieldId));
            assertEquals("value(" + fieldId + ")",
                         dp.getValue(fieldKey),
                         dp.getValue(fieldId));
            assertEquals("readOnly(" + fieldId + ")",
                         dp.isReadOnly(fieldKey),
                         dp.isReadOnly(fieldId));
        }

    }


    /**
     * <p>Check for event propogation.</p>
     */
    public void testEvents() {

        assertNotNull(dp.getFieldKey("intProperty"));
        assertNotNull(dp.getFieldKey("public1"));

        // Register a new listener and verify that it worked
        listener = new Listener();
        dp.addDataListener(listener);
        DataListener listeners[] = dp.getDataListeners();
        assertEquals(1, listeners.length);
        assertTrue(listener == listeners[0]);

        // Make sure we log the update events correctly
        dp.setValue(dp.getFieldKey("intProperty"), new Integer(23432));
        dp.setValue(dp.getFieldKey("public1"), "new public1");
        assertEquals
          ("intProperty/1234/23432//public1/This is public1/new public1//",
           listener.getLog());

        // Deregister the old listener and verify that it worked
        dp.removeDataListener(listener);
        listeners = dp.getDataListeners();
        assertEquals(0, listeners.length);

    }



    /**
     * <p>Check some things that should <strong>not</strong> work.</p>
     */
    public void testNegative() {

        // Access to unknown fieldKey/property
        try {
            dp.getFieldKey("unknown id value");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ; // Expected result
        }

        // Attempt to update a read only value
        try {
            dp.setValue(dp.getFieldKey("readOnly"), "xyz");
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            ; // Expected result
        }

        // Attempt to set value with an incorrect data type
        try {
            dp.setValue(dp.getFieldKey("intProperty"), "string value");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ; // Expected result
        }

    }



    /**
     * <p>Ensure that access to public fields can be turned off.</p>
     */
    public void testNoFields() {

        dp = new ObjectDataProvider(bean, false);
        try {
            dp.getFieldKey("public1");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ; // Expected result
        }
        try {
            dp.getFieldKey("public2");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            ; // Expected result
        }

        // Check the available properties for expected characteristics
        checkProperties();
        checkExtras();

    }


    /**
     * <p>Test a pristine instance.</p>
     */
    public void testPristine() {

        assertEquals("test", ((TestBean) dp.getObject()).getId());

        // Check the available fieldKeys and properties for expected characteristics
        checkFields();
        checkProperties();
        checkExtras();

    }


    /**
     * <p>Test updates to updateable fieldKeys and properties.</p>
     */
    public void testUpdates() {

        // Do the easy cases
        checkUpdates();

    }


    /**
     * <p>Test serializability of this data provider.</p>
     */
    public void testSerializable() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(dp);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        dp = (ObjectDataProvider) ois.readObject();
        ois.close();

        testPristine();

    }


    // --------------------------------------------------------- Support Methods


    /**
     * <p>Ensure that the array returned by <code>getFieldKeys()</code>
     * does not include any keys that should not be there.</p>
     */
    private void checkExtras() {

        FieldKey keys[] = dp.getFieldKeys();
        assertNotNull(keys);
        for (int i = 0; i < keys.length; i++) {
            String name = keys[i].getFieldId();
            boolean found = false;
            for (int j = 0; j < properties.length; j++) {
                if (name.equals(properties[j].name)) {
                    found = true;
                    break;
                }
            }
            if (!found && dp.isIncludeFields()) {
                for (int j = 0; j < fieldKeys.length; j++) {
                    if (name.equals(fieldKeys[j].name)) {
                        found = true;
                        break;
                    }
                }
            }
            assertTrue("Id '" + name + "' is valid", found);
        }

    }


    /**
     * <p>Ensure that all the expected fieldKeys are present and have
     * the specified default values (where possible).</p>
     */
    private void checkFields() {

        FieldKey dk = null;
        String name = null;
        for (int i = 0; i < fieldKeys.length; i++) {
            name = fieldKeys[i].name;
            dk = dp.getFieldKey(name);
            assertNotNull("FieldKey for '" + name + "'", dk);
            assertTrue("Type for '" + name + "'",
                    isAssignableFrom(dp.getType(dk), fieldKeys[i].type));
            assertEquals("ReadOnly for '" + name + "'",
                    fieldKeys[i].canSetValue, dp.isReadOnly(dk));
            if (fieldKeys[i].defaultValue != null) {
                assertEquals("Value for '" + name + "'",
                        fieldKeys[i].defaultValue, dp.getValue(dk));
            }
        }

    }


    /**
     * <p>Ensure that all the expected properties are present and have
     * the specified default values (where possible).</p>
     */
    private void checkProperties() {

        FieldKey dk = null;
        String name = null;
        for (int i = 0; i < properties.length; i++) {
            name = properties[i].name;
            dk = dp.getFieldKey(name);
            assertNotNull("FieldKey for '" + name + "'", dk);
            assertTrue("Type for '" + name + "'",
                    isAssignableFrom(dp.getType(dk), properties[i].type));
            assertEquals("ReadOnly for '" + name + "'",
                    properties[i].canSetValue, dp.isReadOnly(dk));
            if (properties[i].defaultValue != null) {
                assertEquals("Value for '" + name + "'",
                        properties[i].defaultValue, dp.getValue(dk));
            }
        }

    }

    private boolean isAssignableFrom(Class to, Class from) {
        if (to.isPrimitive()) {
            if (to == Boolean.TYPE)
                to = Boolean.class;
            else if (to == Character.TYPE)
                to = Character.class;
            else if (to == Byte.TYPE)
                to = Byte.class;
            else if (to == Short.TYPE)
                to = Short.class;
            else if (to == Integer.TYPE)
                to = Integer.class;
            else if (to == Long.TYPE)
                to = Long.class;
            else if (to == Float.TYPE)
                to = Float.class;
            else if (to == Double.TYPE)
                to = Double.class;
        }
        return to.isAssignableFrom(from);
    }

    /**
     * <p>Ensure that we can update all the simple fieldKeys and properties
     * that should be updatable by default.</p>
     */
    private void checkUpdates() {

        FieldKey dk = null;
        String name = null;
        for (int i = 0; i < updates.length; i++) {
            name = updates[i].name;
            try {
                dp.setValue(dp.getFieldKey(name), updates[i].value);
            } catch (Exception e) {
                fail("Cannot set value for '" + name + "':" + e);
            }
            assertEquals("Updated value for '" + name + "'",
                    updates[i].value, dp.getValue(dp.getFieldKey(name)));
        }

    }

    // Private class to describe the expected properties
    static class Descriptor {
        public Descriptor(String name, Class type, boolean canSetValue, Object defaultValue) {
            this.name = name;
            this.type = type;
            this.canSetValue = canSetValue;
            this.defaultValue = defaultValue;
        }

        public String name;
        public Class type;
        public boolean canSetValue;
        public Object defaultValue;
    }

    // Private class to represent an event listener
    static class Listener
        implements DataListener {
        String log = "";
        public String getLog() {
            return this.log;
        }

        public void clear() {
            this.log = "";
        }

        public void valueChanged(DataProvider dp, FieldKey dk, Object oldValue,
            Object newValue) {
            log += dk.getFieldId() + "/" + oldValue + "/" + newValue + "//";
        }

        public void providerChanged(DataProvider dp) {
            log += "providerChanged//";
        }
    }

    // Private class to describe updates to be performed and checked
    static class Update {
        public Update(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String name;
        public Object value;
    }


}
