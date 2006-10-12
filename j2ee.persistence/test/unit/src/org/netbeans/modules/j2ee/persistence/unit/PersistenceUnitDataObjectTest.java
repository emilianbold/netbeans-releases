/**
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.persistence.unit;

import junit.framework.*;
import junit.textui.TestRunner;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;

/**
 * Tests for the persistence unit data object.
 * @author Erno Mononen
 */
public class PersistenceUnitDataObjectTest extends PersistenceEditorTestBase{
    
    public PersistenceUnitDataObjectTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PersistenceUnitDataObjectTest.class);
        return suite;
    }
    
    public void testAddPersistenceUnit() throws Exception{
        PersistenceUnit persistenceUnit = new PersistenceUnit();
        persistenceUnit.setName("em3");
        persistenceUnit.setJtaDataSource("jdbc/__default");
        dataObject.addPersistenceUnit(persistenceUnit);
        
        assertTrue(containsUnit(persistenceUnit));
        assertTrue(dataCacheContains("\"em3\""));
        assertTrue(dataCacheContains("<jta-data-source>jdbc/__default"));
    }
    
    public void testRemovePersistenceUnit()throws Exception{
        int originalSize = dataObject.getPersistence().getPersistenceUnit().length;
        PersistenceUnit toBeRemoved = dataObject.getPersistence().getPersistenceUnit(0);
        String name = toBeRemoved.getName();
        dataObject.removePersistenceUnit(toBeRemoved);
        assertFalse(containsUnit(toBeRemoved));
        assertTrue(dataObject.getPersistence().getPersistenceUnit().length == originalSize -1);
        assertFalse(dataCacheContains("name=\"" + name + "\""));
    }
    
    public void testChangeName() throws Exception{
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String oldName = persistenceUnit.getName();
        String newName = "new name";
        persistenceUnit.setName(newName);
        dataObject.modelUpdatedFromUI();
        assertTrue(dataCacheContains("\"" + newName + "\""));
        assertFalse(dataCacheContains("\"" + oldName + "\""));
    }

    public void testChangeDatasource() throws Exception{
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String newDatasource = "jdbc/new_datasource";
        persistenceUnit.setJtaDataSource(newDatasource);
        dataObject.modelUpdatedFromUI();
        assertEquals(newDatasource, persistenceUnit.getJtaDataSource());
        assertTrue(dataCacheContains(newDatasource));
    }

    public void testAddClass() throws Exception{
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String clazz = "com.foo.bar.FooClass";
        dataObject.addClass(persistenceUnit, clazz);
        assertTrue(dataCacheContains(clazz));
    }
    
    public void testRemoveClass() throws Exception {
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String clazz = "com.foo.bar.FooClass";
        dataObject.addClass(persistenceUnit, clazz);
        assertTrue(dataCacheContains(clazz));
        dataObject.removeClass(persistenceUnit, clazz);
        assertFalse(dataCacheContains(clazz));
    }

    public void testAddMultipleClasses() throws Exception {
        PersistenceUnit persistenceUnit = dataObject.getPersistence().getPersistenceUnit(0);
        String clazz = "com.foo.bar.FooClass";
        String clazz2 = "com.foo.bar.FooClass2";
        String clazz3 = "com.foo.bar.FooClass3";
        dataObject.addClass(persistenceUnit, clazz);
        dataObject.addClass(persistenceUnit, clazz2);
        dataObject.addClass(persistenceUnit, clazz3);
        assertTrue(dataCacheContains(clazz));
        assertTrue(dataCacheContains(clazz2));
        assertTrue(dataCacheContains(clazz3));
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    
    
}
