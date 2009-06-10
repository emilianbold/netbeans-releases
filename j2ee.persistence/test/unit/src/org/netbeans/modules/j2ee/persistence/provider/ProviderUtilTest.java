/**
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


package org.netbeans.modules.j2ee.persistence.provider;

import java.io.File;
import java.net.URL;
import junit.framework.*;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.dd.common.Property;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for ProviderUtil.
 * @author Erno Mononen
 */
public class ProviderUtilTest extends NbTestCase {
    
    private PersistenceUnit persistenceUnit;
    
    public ProviderUtilTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        this.persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ProviderUtilTest.class);
        return suite;
    }
    
    public void testGetProvider() {
        persistenceUnit.setProvider(ProviderUtil.HIBERNATE_PROVIDER.getProviderClass());
        assertEquals(ProviderUtil.HIBERNATE_PROVIDER, ProviderUtil.getProvider(persistenceUnit));
    }
    
    public void testSetTableGeneration(){
        Provider provider = ProviderUtil.TOPLINK_PROVIDER;
        persistenceUnit.setProvider(provider.getProviderClass());
        
        ProviderUtil.setTableGeneration(persistenceUnit, Provider.TABLE_GENERATION_CREATE, provider);
        assertPropertyExists(provider.getTableGenerationPropertyName());
        assertValueExists(provider.getTableGenerationCreateValue());
        assertNoSuchValue(provider.getTableGenerationDropCreateValue());
        
        ProviderUtil.setTableGeneration(persistenceUnit, Provider.TABLE_GENERATION_DROPCREATE, provider);
        assertPropertyExists(provider.getTableGenerationPropertyName());
        assertValueExists(provider.getTableGenerationDropCreateValue());
        assertNoSuchValue(provider.getTableGenerationCreateValue());
        
    }
    
    public void testSetProvider(){
        Provider provider = ProviderUtil.KODO_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit, provider, getConnection(), Provider.TABLE_GENERATTION_UNKOWN);
        assertEquals(provider.getProviderClass(), persistenceUnit.getProvider());
        assertPropertyExists(provider.getJdbcDriver());
        assertPropertyExists(provider.getJdbcUrl());
        assertPropertyExists(provider.getJdbcUsername());
    }
    
    public void testChangeProvider(){
        Provider originalProvider = ProviderUtil.HIBERNATE_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit, originalProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        assertEquals(originalProvider.getProviderClass(), persistenceUnit.getProvider());
        
        Provider newProvider = ProviderUtil.TOPLINK_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit, newProvider, getConnection(), Provider.TABLE_GENERATION_DROPCREATE);
        // assert that old providers properties were removed
        assertNoSuchProperty(originalProvider.getTableGenerationPropertyName());
        assertNoSuchProperty(originalProvider.getJdbcDriver());
        assertNoSuchProperty(originalProvider.getJdbcUrl());
        assertNoSuchProperty(originalProvider.getJdbcUsername());
        // assert that new providers properties are set
        assertEquals(newProvider.getProviderClass(), persistenceUnit.getProvider());
        assertPropertyExists(newProvider.getJdbcDriver());
        assertPropertyExists(newProvider.getJdbcUrl());
        assertPropertyExists(newProvider.getJdbcUsername());
        assertPropertyExists(newProvider.getTableGenerationPropertyName());
    }
    
    /**
     * Tests that changing of provider preserves existing
     * table generation value.
     */
    public void testTableGenerationPropertyIsPreserved(){
        Provider originalProvider = ProviderUtil.KODO_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit, originalProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        
        Provider newProvider = ProviderUtil.TOPLINK_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit, newProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        assertEquals(newProvider.getTableGenerationPropertyName(),
                ProviderUtil.getProperty(persistenceUnit, newProvider.getTableGenerationPropertyName()).getName());
        assertEquals(newProvider.getTableGenerationCreateValue(),
                ProviderUtil.getProperty(persistenceUnit, newProvider.getTableGenerationPropertyName()).getValue());
        
        
        
    }
    
    public void testRemoveProviderProperties(){
        Provider provider = ProviderUtil.KODO_PROVIDER;
        PersistenceUnit persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        ProviderUtil.setProvider(persistenceUnit, provider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        //        ProviderUtil.setTableGeneration(persistenceUnit, Provider.TABLE_GENERATION_CREATE, provider);
        
        ProviderUtil.removeProviderProperties(persistenceUnit);
        assertNoSuchProperty(provider.getTableGenerationPropertyName());
        assertNoSuchProperty(provider.getJdbcDriver());
        assertNoSuchProperty(provider.getJdbcUrl());
        assertNoSuchProperty(provider.getJdbcUsername());
        
    }
    
    
    public void testGetPUDataObject() throws Exception{
        String invalidPersistenceXml = getDataDir().getAbsolutePath() + File.separator + "invalid_persistence.xml";
        FileObject invalidPersistenceFO = FileUtil.toFileObject(new File(invalidPersistenceXml));
        try{
            ProviderUtil.getPUDataObject(invalidPersistenceFO);
            fail("InvalidPersistenceXmlException should have been thrown");
        } catch (InvalidPersistenceXmlException ipx){
            assertEquals(invalidPersistenceXml, ipx.getPath());
        }
        
    }
    /**
     * Asserts that property with given name exists in persistence unit.
     */
    protected void assertPropertyExists(String propertyName){
        if (!propertyExists(propertyName)){
            fail("Property " + propertyName + " was not found.");
        }
        assertTrue(true);
    }
    
    /**
     * Asserts that no property with given name exists in persistence unit.
     */
    protected void assertNoSuchProperty(String propertyName){
        if (propertyExists(propertyName)){
            fail("Property " + propertyName + " was found.");
        }
        assertTrue(true);
    }
    
    protected void assertNoSuchValue(String value){
        if (valueExists(value)){
            fail("Property with value " + value + " was found");
        }
        assertTrue(true);
    }
    
    protected void assertValueExists(String value){
        if (!valueExists(value)){
            fail("Property with value " + value + " was not found");
        }
        assertTrue(true);
    }
    
    
    /**
     * @return true if property with given name exists in persistence unit,
     * false otherwise.
     */
    protected boolean propertyExists(String propertyName){
        Property[] properties = ProviderUtil.getProperties(persistenceUnit);
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].getName().equals(propertyName)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return true if property with given value exists in persistence unit,
     * false otherwise.
     */
    protected boolean valueExists(String propertyValue){
        Property[] properties = ProviderUtil.getProperties(persistenceUnit);
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].getValue().equals(propertyValue)){
                return true;
            }
        }
        return false;
    }
    
    private DatabaseConnection getConnection(){
        JDBCDriver driver = JDBCDriver.create("driver", "driver", "foo.bar", new URL[]{});
        return DatabaseConnection.create(driver, "foo", "bar", "schema", "password", false);
    }
    
}