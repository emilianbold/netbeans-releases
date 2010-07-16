package org.netbeans.modules.visualweb.dataprovider;

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
import com.sun.data.provider.DataListener;
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableCursorListener;
import com.sun.data.provider.TableCursorVetoException;
import com.sun.data.provider.TableDataListener;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.TransactionalDataListener;
import com.sun.data.provider.TransactionalDataProvider;
import com.sun.data.provider.impl.IndexRowKey;
import com.sun.data.provider.impl.ObjectListDataProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author winstonp
 */
public class ObjectListDataProviderTest extends NbTestCase {

    // ------------------------------------------------------ Instance Variables
    /**
     * <p>The beans being wrapped by the {@link DataProvider} under test.
     */
    private TestBean beans[] = null;
    /**
     * <p>The {@link DataProvider} instance under test.
     */
    private ObjectListDataProvider dp = null;
    /**
     * <p>List representation of beans being wrapped.</p>
     */
    private List list = null;
    /**
     * <p>Event listener for event testing.</p>
     */
    private Listener listener = null;
    /**
     * <p>Event listener for event testing.</p>
     */
    private MyCursorListener tdcListener = null;
    /**
     * <p>Event listener for event testing.</p>
     */
    private MyDataListener tdpListener = null;
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

    public ObjectListDataProviderTest(String testName) {
        super(testName);
        new ObjectListDataProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        beans = new TestBean[5];
        list = new ArrayList();
        for (int i = 0; i < beans.length; i++) {
            beans[i] = new TestBean("test" + i);
            list.add(beans[i]);
        }
        dp = new ObjectListDataProvider(list, true);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        dp = null;
        beans = null;
        list = null;
        listener = null;
        tdcListener = null;
        tdpListener = null;
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
     * <p>Check for events related to row appending.</p>
     */
    public void testEventsAppend() {

        // Register listener we will need
        MyTransactionalListener tListener = new MyTransactionalListener();
        dp.addTransactionalDataListener(tListener);

        // Append a row and allow the DP to create an instance
        assertTrue(dp.canAppendRow());
        RowKey rk1 = dp.appendRow();
        assertNull(dp.getValue("id", rk1));
        dp.setValue("id", rk1, "testA");
        assertEquals("testA", dp.getValue("id", rk1));

        // Append a row that is a specific object instance
        assertTrue(dp.canAppendRow());
        TestBean beanB = new TestBean("testB");
        RowKey rk2 = dp.appendRow(beanB);

        // Commit the results and validate the event history
        dp.commitChanges();
        assertEquals("rowAdded/RowKey[5]//" +
                "FieldKey[id]/RowKey[5]/null/testA//" +
                "FieldKey[id]/null/testA//" +
                "rowAdded/RowKey[6]//" +
                "changesCommitted//",
                tListener.getLog());

    // Validate the remaining beans
        /*
    assertEquals(beans.length - 2, list.size());
    assertEquals("test0", ((TestBean) list.get(0)).getId());
    assertEquals("test2", ((TestBean) list.get(1)).getId());
    assertEquals("test4", ((TestBean) list.get(2)).getId());
     */

    }

    /**
     * <p>Check for event propogation for basic DataProvider events.</p>
     */
    public void testEventsBasic() {

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
        assertEquals("intProperty/1234/23432//public1/This is public1/new public1//",
                listener.getLog());

        // Deregister the old listener and verify that it worked
        dp.removeDataListener(listener);
        listeners = dp.getDataListeners();
        assertEquals(0, listeners.length);

    }

    /**
     * <p>Check for event propogation for cursor changes.</p>
     */
    public void testEventsCursor() throws Exception {

        // Register a new listener and verify that it worked
        tdcListener = new MyCursorListener();
        dp.addTableCursorListener(tdcListener);
        TableCursorListener listeners[] = dp.getTableCursorListeners();
        assertEquals(1, listeners.length);
        assertTrue(tdcListener == listeners[0]);

        // Make sure we log cursor change events correctly
        assertEquals(new IndexRowKey(0), dp.getCursorRow());
        dp.setCursorRow(new IndexRowKey(2));
        assertEquals(new IndexRowKey(2), dp.getCursorRow());
        dp.setCursorRow(new IndexRowKey(1));
        assertEquals(new IndexRowKey(1), dp.getCursorRow());
        assertEquals("cursorChanging/RowKey[0]/RowKey[2]//cursorChanged/RowKey[0]/RowKey[2]//" +
                "cursorChanging/RowKey[2]/RowKey[1]//cursorChanged/RowKey[2]/RowKey[1]//",
                tdcListener.getLog());

        // Make sure we can deal with vetos as well
        tdcListener.clear();
        tdcListener.setVeto(true);
        try {
            dp.setCursorRow(new IndexRowKey(3));
            fail("Should have thrown TableCursorVetoException");
        } catch (TableCursorVetoException e) {
            ; // Expected result
        }
        assertEquals(new IndexRowKey(1), dp.getCursorRow());
        assertEquals("cursorChanging/RowKey[1]/RowKey[3]//cursorVetoed/RowKey[1]/RowKey[3]//",
                tdcListener.getLog());

        // Deregister the old listener and verify that it worked
        dp.removeTableCursorListener(tdcListener);
        listeners = dp.getTableCursorListeners();
        assertEquals(0, listeners.length);

    }

    /**
     * <p>Check for event propogation on random data changes.</p>
     */
    public void testEventsData() {

        // Register a new listener and verify that it worked
        tdpListener = new MyDataListener();
        dp.addTableDataListener(tdpListener);
        TableDataListener listeners[] = dp.getTableDataListeners();
        assertEquals(1, listeners.length);
        assertTrue(tdpListener == listeners[0]);

        dp.cursorFirst();
        // Make sure we log the update events correctly
        dp.setValue(dp.getFieldKey("intProperty"),
                new Integer(23432)); // Change, so event expected
        Object value = dp.getValue(dp.getFieldKey("stringProperty"));
        dp.setValue(dp.getFieldKey("stringProperty"), value); // No change, so no event expected


        assertEquals("FieldKey[intProperty]/RowKey[0]/1234/23432//" + // Row-specific event
                "FieldKey[intProperty]/1234/23432//", // Row-independent event
                tdpListener.getLog());

        // We should get a provider change event too
        tdpListener.clear();
        dp.setList(dp.getList());
        assertEquals("providerChanged//", tdpListener.getLog());

        // Deregister the old listener and verify that it worked
        dp.removeTableDataListener(tdpListener);
        listeners = dp.getTableDataListeners();
        assertEquals(0, listeners.length);

    }

    /**
     * <p>Check for events related to row insertion.</p>
     */
    public void testEventsInsert() {

        // FIXME - inserts are not currently supported.  When they are,
        // the processing in commitChanges() will need to interleave the
        // deletes and inserts so that references to the pre-commit
        // row keys are not messed up
        assertTrue(!dp.canInsertRow(dp.getCursorRow()));

    }

    /**
     * <p>Check for events related to row removal -- ascending ordering.</p>
     */
    public void testEventsRemovesAscending() {

        // Register listener we will need
        MyTransactionalListener tListener = new MyTransactionalListener();
        dp.addTransactionalDataListener(tListener);

        // Remove the rows at indexes 1 and 3, and commit the changes
        RowKey rk = null;
        rk = dp.findFirst("id", "test1");
        assertNotNull(rk);
        assertTrue(dp.canRemoveRow(rk));
        dp.removeRow(rk);
        rk = dp.findFirst("id", "test3");
        assertNotNull(rk);
        assertTrue(dp.canRemoveRow(rk));
        dp.removeRow(rk);
        dp.commitChanges();

        // Validate the event history
        assertEquals("rowRemoved/RowKey[1]//" +
                "rowRemoved/RowKey[3]//" +
                "changesCommitted//",
                tListener.getLog());

        // Validate the remaining beans
        assertEquals(beans.length - 2, list.size());
        assertEquals("test0", ((TestBean) list.get(0)).getId());
        assertEquals("test2", ((TestBean) list.get(1)).getId());
        assertEquals("test4", ((TestBean) list.get(2)).getId());

    }

    /**
     * <p>Check for events related to row removal -- descending ordering.</p>
     */
    public void testEventsRemovesDescending() {

        // Register listener we will need
        MyTransactionalListener tListener = new MyTransactionalListener();
        dp.addTransactionalDataListener(tListener);

        // Remove the rows at indexes 3 and 1, and commit the changes
        RowKey rk = null;
        rk = dp.findFirst("id", "test3");
        assertNotNull(rk);
        assertTrue(dp.canRemoveRow(rk));
        dp.removeRow(rk);
        rk = dp.findFirst("id", "test1");
        assertNotNull(rk);
        assertTrue(dp.canRemoveRow(rk));
        dp.removeRow(rk);
        dp.commitChanges();

        // Validate the event history
        assertEquals("rowRemoved/RowKey[3]//" +
                "rowRemoved/RowKey[1]//" +
                "changesCommitted//",
                tListener.getLog());

        // Validate the remaining beans
        assertEquals(beans.length - 2, list.size());
        assertEquals("test0", ((TestBean) list.get(0)).getId());
        assertEquals("test2", ((TestBean) list.get(1)).getId());
        assertEquals("test4", ((TestBean) list.get(2)).getId());

    }

    /**
     * <p>Check for transactional updates to existing rows.</p>
     */
    public void testEventsUpdates() {

        // Register listeners we will need for verification
        TableDataListener aListener = new MyDataListener();
        MyTransactionalListener tListener = new MyTransactionalListener();
        dp.addTableDataListener(aListener);
        TableDataListener aListeners[] = dp.getTableDataListeners();
        assertEquals(1, aListeners.length);
        assertTrue(aListener == aListeners[0]);
        dp.addTransactionalDataListener(tListener);
        aListeners = dp.getTableDataListeners();
        assertEquals(2, aListeners.length);
        assertTrue(aListener == aListeners[0]);
        assertTrue(tListener == aListeners[1]);
        TransactionalDataListener tListeners[] = dp.getTransactionalDataListeners();
        assertEquals(1, tListeners.length);
        assertTrue(tListener == tListeners[0]);

        // Perform an update, check for event and new value showing
        dp.cursorFirst();
        dp.setValue(dp.getFieldKey("intProperty"),
                new Integer(23432)); // Change, so event expected
        assertEquals("FieldKey[intProperty]/RowKey[0]/1234/23432//" + // Row-specific event
                "FieldKey[intProperty]/1234/23432//", // Row-independent event
                tListener.getLog());
        assertEquals(new Integer(23432),
                (Integer) dp.getValue(dp.getFieldKey("intProperty")));

        // Fake an update, check for no event and no change in value showing
        tListener.clear();
        dp.setValue(dp.getFieldKey("intProperty"),
                new Integer(23432)); // Change, so event expected
        assertEquals("", tListener.getLog());
        assertEquals(new Integer(23432),
                (Integer) dp.getValue(dp.getFieldKey("intProperty")));

        // Revert and ensure that old value shows again
        tListener.clear();
        dp.revertChanges();
        assertEquals("changesReverted//",
                tListener.getLog());
        assertEquals(new Integer(1234),
                (Integer) dp.getValue(dp.getFieldKey("intProperty")));

        // Make a change, commit, and ensure revert does not erase it
        tListener.clear();
        dp.setValue(dp.getFieldKey("intProperty"),
                new Integer(43234)); // Change, so event expected
        assertEquals("FieldKey[intProperty]/RowKey[0]/1234/43234//" + // Row-specific event
                "FieldKey[intProperty]/1234/43234//", // Row-independent event
                tListener.getLog());
        assertEquals(new Integer(43234),
                (Integer) dp.getValue(dp.getFieldKey("intProperty")));
        tListener.clear();
        dp.commitChanges();
        assertEquals(new Integer(43234),
                (Integer) dp.getValue(dp.getFieldKey("intProperty")));
        dp.revertChanges();
        assertEquals(new Integer(43234),
                (Integer) dp.getValue(dp.getFieldKey("intProperty")));
        assertEquals("changesCommitted//changesReverted//",
                tListener.getLog());

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

        dp = new ObjectListDataProvider(list, false);
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
    public void testPristine() throws Exception {

        assertTrue(dp.isIncludeFields());
        assertEquals(beans.length, dp.getRowCount());

        // Check the available fieldKeys and properties for expected characteristics
        for (int i = 0; i < beans.length; i++) {
            dp.setCursorRow(dp.getRowKey("" + i));
            assertEquals("test" + i, dp.getValue(dp.getFieldKey("id")));
            checkFields();
            checkProperties();
            checkExtras();
        }

        // Check random access on the id property
        for (int i = 0; i < beans.length; i++) {
            assertEquals("test" + i, dp.getValue(dp.getFieldKey("id"), dp.getRowKey("" + i)));
        }

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
        dp = (ObjectListDataProvider) ois.readObject();
        ois.close();

        testPristine();

    }

    /**
     * <p>Test updates to updateable fieldKeys and properties.</p>
     */
    public void testUpdates() {

        // Do the easy cases
        checkUpdates();

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
            if (to == Boolean.TYPE) {
                to = Boolean.class;
            } else if (to == Character.TYPE) {
                to = Character.class;
            } else if (to == Byte.TYPE) {
                to = Byte.class;
            } else if (to == Short.TYPE) {
                to = Short.class;
            } else if (to == Integer.TYPE) {
                to = Integer.class;
            } else if (to == Long.TYPE) {
                to = Long.class;
            } else if (to == Float.TYPE) {
                to = Float.class;
            } else if (to == Double.TYPE) {
                to = Double.class;
            }
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

    // Private class to represent a TableDataProvider cursor listener
    static class MyCursorListener implements TableCursorListener {

        boolean veto = false;
        String log = "";

        public String getLog() {
            return this.log;
        }

        public void clear() {
            this.log = "";
        }

        public void cursorChanged(TableDataProvider dp, RowKey oldRow, RowKey newRow) {
            log += "cursorChanged/" + oldRow + "/" + newRow + "//";
        }

        public void cursorChanging(TableDataProvider dp, RowKey oldRow, RowKey newRow)
                throws TableCursorVetoException {
            log += "cursorChanging/" + oldRow + "/" + newRow + "//";
            if (veto) {
                log += "cursorVetoed/" + oldRow + "/" + newRow + "//";
                throw new TableCursorVetoException("No way, Jose");
            }
        }

        public boolean isVeto() {
            return this.veto;
        }

        public void setVeto(boolean veto) {
            this.veto = veto;
        }
    }

    // Private class to represent a TableDataProvider event listener
    class MyDataListener implements TableDataListener {

        String log = "";

        public String getLog() {
            return this.log;
        }

        public void clear() {
            this.log = "";
        }

        public void valueChanged(DataProvider dp, FieldKey fk, Object oldValue, Object newValue) {
            log += fk + "/" + oldValue + "/" + newValue + "//";
        }

        public void providerChanged(DataProvider dp) {
            log += "providerChanged//";
        }

        public void rowAdded(TableDataProvider dp, RowKey rk) {
            log += "rowAdded/" + rk + "//";
        }

        public void rowRemoved(TableDataProvider dp, RowKey rk) {
            log += "rowRemoved/" + rk + "//";
        }

        public void valueChanged(TableDataProvider dp, FieldKey fk, RowKey rk, Object oldValue, Object newValue) {
            log += fk + "/" + rk + "/" + oldValue + "/" + newValue + "//";
        }
    }

    // Private class to represent a TransactionalDataProvider event listener
    class MyTransactionalListener implements TransactionalDataListener, TableDataListener {

        String log = "";

        public String getLog() {
            return this.log;
        }

        public void clear() {
            this.log = "";
        }

        public void changesCommitted(TransactionalDataProvider tdp) {
            log += "changesCommitted//";
        }

        public void changesReverted(TransactionalDataProvider tdp) {
            log += "changesReverted//";
        }

        public void providerChanged(DataProvider dp) {
            log += "providerChanged//";
        }

        public void rowAdded(TableDataProvider dp, RowKey rk) {
            log += "rowAdded/" + rk + "//";
        }

        public void rowRemoved(TableDataProvider dp, RowKey rk) {
            log += "rowRemoved/" + rk + "//";
        }

        public void valueChanged(DataProvider dp, FieldKey fk, Object oldValue,
                Object newValue) {
            log += fk + "/" + oldValue + "/" + newValue + "//";
        }

        public void valueChanged(TableDataProvider dp, FieldKey fk, RowKey rk, Object oldValue, Object newValue) {
            log += fk + "/" + rk + "/" + oldValue + "/" + newValue + "//";
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
