/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package org.netbeans.modules.visualweb.dataprovider;

import com.sun.data.provider.RowKey;
import com.sun.data.provider.SortCriteria;
import com.sun.data.provider.impl.BasicTableDataSorter;
import com.sun.data.provider.impl.FieldIdSortCriteria;
import com.sun.data.provider.impl.ObjectListDataProvider;
import org.netbeans.junit.NbTestCase;

public class BasicTableDataSorterTest extends NbTestCase {

    private BasicTableDataSorter tds = null;
    private ObjectListDataProvider tdp = null;

    public BasicTableDataSorterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        /**@todo verify the constructors*/
        tds = new BasicTableDataSorter();
        tdp = new ObjectListDataProvider();
        for (int x = 0; x < 10; x++) {
            for (int i = 0; i < 20; i++) {
                TestBean tb = new TestBean("TestBean" + i);
                tb.setStringProperty(("TestBean:" + i) + tb.hashCode());
                tb.setLongProperty(System.currentTimeMillis());
                tb.setDoubleProperty(Math.random());
                tb.setIntProperty((int) Math.round(tb.getDoubleProperty() * 1000));
                tdp.addObject(tb);
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        tds = null;
        tdp = null;
        super.tearDown();
    }

    public void testSort() {
        tds.setSortCriteria(new SortCriteria[]{
            //            new FieldIdSortCriteria("stringProperty", true),
            new FieldIdSortCriteria("id", true),
            new FieldIdSortCriteria("intProperty", false),
//            new FieldIdSortCriteria("longProperty", true),
//            new FieldIdSortCriteria("doubleProperty", true),
        });
        SortCriteria[] sca = tds.getSortCriteria();
//        System.out.println("Sort Criteria:");
//        for (int i = 0; i < sca.length; i++) {
//            System.out.println("  [" + sca[i].getDisplayName() + "] " + (sca[i].isAscending() ? "ASC" : "DESC"));
//        }
        RowKey[] rows = tdp.getRowKeys(tdp.getRowCount(), null);
        RowKey[] sort = tds.sort(tdp, rows);
//        for (int i = 0; i < sort.length; i++) {
//            System.out.println("sorted[" + i + "] row[" + sort[i] + "] id=\"" +
//                tdp.getValue(tdp.getFieldKey("id"),             sort[i]) + "\" int=" +
//                tdp.getValue(tdp.getFieldKey("intProperty"),    sort[i]) + " long=" +
//                tdp.getValue(tdp.getFieldKey("longProperty"),   sort[i]) + " double=" +
//                tdp.getValue(tdp.getFieldKey("doubleProperty"), sort[i]) + " string=\"" +
//                tdp.getValue(tdp.getFieldKey("stringProperty"), sort[i]) + "\"");
//        }
    }
}
