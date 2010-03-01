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
package org.netbeans.modules.dlight.spi.support;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.dlight.api.impl.DLightToolConfigurationAccessor;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import static org.junit.Assert.*;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mt154047
 */
public class DLightToolConfigurationProviderFactoryTest {
    private static final boolean TRACE = false;

    private FileObject folder;

    public DLightToolConfigurationProviderFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        folder = FileUtil.getConfigFile("DLight/Fops.Configuration");
        assertNotNull("testing layer is loaded: ", folder);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of create method, of class DLightToolConfigurationProviderFactory.
     */
    @Test
    public void testCreate() {
        if (TRACE){
            System.out.println("create");
        }
        DLightToolConfiguration result = null;
        try {
            FileObject fo = folder.getFileObject("XMLToolConfiguration.instance");
            assertNotNull("file " + "DLight/Fops.Configuration/XMLFopsToolConfiguration.instance", fo);

            Object obj = fo.getAttribute("instanceCreate");
            assertNotNull("File object has not null instanceCreate attribute", obj);


            if (!(obj instanceof DLightToolConfiguration)) {
                fail("Object needs to be DLightConfiguration: " + obj);
            }
            result = (DLightToolConfiguration)obj;

        } catch (Exception ex) {
            fail("Test is not passed");
        }
        assertNotNull("DLightToolConfiguration should not be null", result);
        DLightToolConfigurationAccessor toolConfigurationAccessor = DLightToolConfigurationAccessor.getDefault();
        if (TRACE){
            System.out.println("name=" + toolConfigurationAccessor.getToolName(result));
            System.out.println("displayedName=" + toolConfigurationAccessor.getDetailedToolName(result));
            System.out.println("id=" + result.getID());
        }
        // TODO review the generated test code and remove the default call to fail.

    }

    @Test
    public void testCreateTable() {
        if (TRACE){
            System.out.println("createTable");
        }
        DataTableMetadata result = null;
        try {
            FileObject fo = folder.getFileObject("DtraceDatatableMetadata.instance");
            assertNotNull("file " + "DLight/Fops.Configuration/DtraceDatatableMetadata.instance", fo);

            Object obj = fo.getAttribute("instanceCreate");
            assertNotNull("File object should have not null instanceCreate attribute", obj);

            if (!(obj instanceof DataTableMetadata)) {
                fail("Object needs to be DatatableMetadata: " + obj);
            }
            result = (DataTableMetadata)obj;

        } catch (Exception ex) {
            fail("Test is not passed");
        }
        assertNotNull("DataTableMetadata should not be null", result);
        if (TRACE){
            System.out.println("table name=" + result.getName());
        }
    }

    @Test
    public void testCreateDetailsTable() {
        if (TRACE){
            System.out.println("createDetaiklsTable");
        }
        DataTableMetadata result = null;
        try {
            FileObject fo = folder.getFileObject("DtraceDetailsDatatableMetadata.instance");
            assertNotNull("file " + "DLight/Fops.Configuration/DtraceDetailsDatatableMetadata.instance", fo);

            Object obj = fo.getAttribute("instanceCreate");
            assertNotNull("File object should have not null instanceCreate attribute", obj);

            if (!(obj instanceof DataTableMetadata)) {
                fail("Object needs to be DatatableMetadata: " + obj);
            }
            result = (DataTableMetadata)obj;

        } catch (Exception ex) {
            fail("Test is not passed");
        }
        assertNotNull("DataTableMetadata should not be null", result);
        if (TRACE){
            System.out.println("****************************************");
            System.out.println("table name=" + result.getName());
            System.out.println("*********************columns*********************");
            List<Column> columns = result.getColumns();
            for (Column c: columns ){
                print(c);
            }
            System.out.println("****************************************");
        }
    }

    @Test
    public void testCreateColumnsList(){
        if (TRACE){
            System.out.println("createColumnsList");
        }
        List<Column> result = null;
        try {
            FileObject fo = folder.getFileObject("FopsColumns.List");
            assertNotNull("file " + "DLight/Fops.Configuration/FopsColumns.List", fo);

            Object obj = fo.getAttribute("instanceCreate");
            assertNotNull("File object should have not null instanceCreate attribute", obj);

            if (!(obj instanceof List)) {
                fail("Object needs to be List of Column: " + obj);
            }
            result = (List<Column>)obj;

        } catch (Exception ex) {
            fail("Test is not passed");
        }
        assertNotNull("List<Column> should not be null", result);
        if (TRACE){
            System.out.println("columns count=" + result.size());
            for (Column c : result){
                print(c);
            }
        }
    }

    @Test
    public void testCreateColumn(){
        if (TRACE){
            System.out.println("createColumn");
        }
        Column result = null;
        try {
            FileObject fo = folder.getFileObject("FopsColumns/Column1.instance");
            assertNotNull("file " + "DLight/Fops.Configuration/FopsColumns/Column1.instance", fo);

            Object obj = fo.getAttribute("instanceCreate");
            assertNotNull("File object should have not null instanceCreate attribute", obj);

            if (!(obj instanceof Column)) {
                fail("Object needs to be a Column: " + obj);
            }
            result = (Column)obj;

        } catch (Exception ex) {
            fail("Test is not passed");
        }
        assertNotNull("Column should not be null", result);
        if (TRACE){
            print(result);
        }
    }

    @Test
    public void testIndicatorMetadata(){
        if (TRACE){
            System.out.println("createIndicatorMetadata");
        }
        IndicatorMetadata result = null;
        try {
            FileObject fo = folder.getFileObject("IndicatorMetadata.instance");
            assertNotNull("file " + "DLight/Fops.Configuration/IndicatorMetadata.instance", fo);

            Object obj = fo.getAttribute("instanceCreate");
            assertNotNull("File object should have not null instanceCreate attribute", obj);

            if (!(obj instanceof IndicatorMetadata)) {
                fail("Object needs to be a IndicatorMetadata: " + obj);
            }
            result = (IndicatorMetadata)obj;

        } catch (Exception ex) {
            fail("Test is not passed");
        }
        assertNotNull("IndicatorMetadata should not be null", result);
    }

    public void testDTDCConfiguration(){
        System.out.println("DtraceDataCollectorConfiguration");
    }


   


    private void print(Column column){
        System.out.println("-------------print column----------------------");
        System.out.println("column name=" + column.getColumnName());
        System.out.println("column class=" + column.getColumnClass());
        System.out.println("column displayed name=" + column.getColumnUName());
        System.out.println("column expression=" + column.getExpression());
        
    }

}
