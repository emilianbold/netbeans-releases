/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.visualweb.dataprovider.designtime;

import com.sun.data.provider.DataListener;
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RefreshableDataListener;
import com.sun.data.provider.RefreshableDataProvider;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableCursorListener;
import com.sun.data.provider.TableDataListener;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.TransactionalDataListener;
import com.sun.data.provider.TransactionalDataProvider;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Static utility methods for <code>BeanInfo</code> implementations.</p>
 */
public class BeanInfoSupport {
    

    // ---------------------------------------------------------- Static Methods


    /**
     * <p>Return a <code>List</code> of <code>EventSetDescriptor</code>s
     * for the events fired by a <code>DataProvider</code>.</p>
     *
     * @param source Source class firing the events
     *
     * @exception IntrospectionException if an exception occurs
     *  during introspection
     */
    public static List getDataProviderEventSetDescriptors(Class source) {

        List results = new ArrayList();
        try {

            results.add(
              new EventSetDescriptor
                ("dataListener",
                 DataListener.class,
                 new Method[] {
                   DataListener.class.getMethod("providerChanged",
                     new Class[] { DataProvider.class }),
                   DataListener.class.getMethod("valueChanged",
                     new Class[] { DataProvider.class,
                                   FieldKey.class,
                                   Object.class,
                                   Object.class }),
                 },
                 source.getMethod("addDataListener",
                   new Class[] { DataListener.class }),
                 source.getMethod("removeDataListener", 
                   new Class[] { DataListener.class }),
                 source.getMethod("getDataListeners",
                   new Class[0])));

        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return results;

    }


    /**
     * <p>Return a <code>List</code> of <code>EventSetDescriptor</code>s
     * for events fired by a <code>RefreshableDataProvider</code>.  This
     * list includes <strong>ONLY</strong> events described by this interface,
     * not by any superinterfaces.</p>
     *
     * @param source Source class firing the events
     *
     * @exception IntrospectionException if an exception occurs
     *  during introspection
     */
    public static List getRefreshableDataProviderEventSetDescriptors(Class source) {

        List results = new ArrayList();
        try {

            results.add(
              new EventSetDescriptor
                ("refreshableDataListener",
                 RefreshableDataListener.class,
                 new Method[] {
                   RefreshableDataListener.class.getMethod("refreshed",
                     new Class[] { RefreshableDataProvider.class }),
                 },
                 source.getMethod("addRefreshableDataListener",
                   new Class[] { RefreshableDataListener.class }),
                 source.getMethod("removeRefreshableDataListener", 
                   new Class[] { RefreshableDataListener.class }),
                 source.getMethod("getRefreshableDataListeners",
                   new Class[0])));

        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return results;

    }


    /**
     * <p>Return a <code>List</code> of <code>EventSetDescriptor</code>s
     * for events fired by a <code>TableDataProvider</code>.  This
     * list includes <strong>ONLY</strong> events described by this interface,
     * not by any superinterfaces.</p>
     *
     * @param source Source class firing the events
     *
     * @exception IntrospectionException if an exception occurs
     *  during introspection
     */
    public static List getTableDataProviderEventSetDescriptors(Class source) {

        List results = new ArrayList();
        try {

            results.add(
              new EventSetDescriptor
                ("tableCursorListener",
                 TableCursorListener.class,
                 new Method[] {
                   TableCursorListener.class.getMethod("cursorChanged",
                     new Class[] { TableDataProvider.class,
                                   RowKey.class,
                                   RowKey.class }),
                   TableCursorListener.class.getMethod("cursorChanging",
                     new Class[] { TableDataProvider.class,
                                   RowKey.class,
                                   RowKey.class }),
                 },
                 source.getMethod("addTableCursorListener",
                   new Class[] { TableCursorListener.class }),
                 source.getMethod("removeTableCursorListener", 
                   new Class[] { TableCursorListener.class }),
                 source.getMethod("getTableCursorListeners",
                   new Class[0])));

            results.add(
              new EventSetDescriptor
                ("tableDataListener",
                 TableDataListener.class,
                 new Method[] {
                   TableDataListener.class.getMethod("rowAdded",
                     new Class[] { TableDataProvider.class,
                                   RowKey.class }),
                   TableDataListener.class.getMethod("rowRemoved",
                     new Class[] { TableDataProvider.class,
                                   RowKey.class }),
                   TableDataListener.class.getMethod("valueChanged",
                     new Class[] { TableDataProvider.class,
                                   FieldKey.class,
                                   RowKey.class,
                                   Object.class,
                                   Object.class }),
                 },
                 source.getMethod("addTableDataListener",
                   new Class[] { TableDataListener.class }),
                 source.getMethod("removeTableDataListener", 
                   new Class[] { TableDataListener.class }),
                 source.getMethod("getTableDataListeners",
                   new Class[0])));

        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return results;

    }


    /**
     * <p>Return a <code>List</code> of <code>EventSetDescriptor</code>s
     * for events fired by a <code>TransactionalDataProvider</code>.  This
     * list includes <strong>ONLY</strong> events described by this interface,
     * not by any superinterfaces.</p>
     *
     * @param source Source class firing the events
     *
     * @exception IntrospectionException if an exception occurs
     *  during introspection
     */
    public static List getTransactionalDataProviderEventSetDescriptors(Class source) {

        List results = new ArrayList();
        try {

            results.add(
              new EventSetDescriptor
                ("transactionalDataListener",
                 TransactionalDataListener.class,
                 new Method[] {
                   TransactionalDataListener.class.getMethod("changesCommitted",
                     new Class[] { TransactionalDataProvider.class }),
                   TransactionalDataListener.class.getMethod("changesReverted",
                     new Class[] { TransactionalDataProvider.class }),
                 },
                 source.getMethod("addTransactionalDataListener",
                   new Class[] { TransactionalDataListener.class }),
                 source.getMethod("removeTransactionalDataListener", 
                   new Class[] { TransactionalDataListener.class }),
                 source.getMethod("getTransactionalDataListeners",
                   new Class[0])));

        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return results;

    }


}
