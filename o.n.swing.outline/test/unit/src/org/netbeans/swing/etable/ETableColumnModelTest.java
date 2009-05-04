/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.swing.etable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Properties;
import org.netbeans.junit.NbTestCase;

/**
 * Tests for class ETableColumnModel.
 * @author David Strupl
 */
public class ETableColumnModelTest extends NbTestCase {
    
    public ETableColumnModelTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }

    /**
     * Test of readSettings, writeSettings methods, of class org.netbeans.swing.etable.ETableColumnModel.
     */
    public void testReadWriteSettings() {
        ETable et = new ETable();
        System.out.println("testReadWriteSettings");
        ETableColumnModel etcm = new ETableColumnModel();
        ETableColumn etc1 = new ETableColumn(0, et);
        etcm.addColumn(etc1);
        ETableColumn etc2 = new ETableColumn(1, et);
        etcm.addColumn(etc2);
        ETableColumn etc3 = new ETableColumn(2, et);
        etcm.addColumn(etc3);
        etcm.setColumnHidden(etc3, true);
        Properties p = new Properties();
        
        etcm.writeSettings(p, "test");
        ETableColumnModel etcm2 = new ETableColumnModel();
        etcm2.readSettings(p, "test", et);
        
        assertEquals("Should restore 2 columns", 2, etcm2.getColumnCount());
        assertEquals("One hidden column", 1, etcm2.hiddenColumns.size());
    }

    /**
     * Test of getComparator method, of class org.netbeans.swing.etable.ETableColumnModel.
     */
    public void testGetComparator() {
        System.out.println("testGetComparator");
        ETableColumnModel etcm = new ETableColumnModel();
        assertTrue(etcm.getComparator() instanceof ETable.OriginalRowComparator);
        TableModel tm = new DefaultTableModel(new Object[][] {{"b"},{"a"}}, new Object[] {"a", "b"}); 
        ETable.RowMapping rm1 = new ETable.RowMapping(0, tm);
        ETable.RowMapping rm2 = new ETable.RowMapping(1, tm);
        assertTrue("Without sort use index of rows, ", etcm.getComparator().compare(rm1, rm2) < 0);
        
        ETableColumn etc = new ETableColumn(0, new ETable());
        etcm.addColumn(etc);
        etcm.toggleSortedColumn(etc, true);
        assertTrue("Sorting according to data model failed, ", etcm.getComparator().compare(rm1, rm2) > 0);
    }

    /**
     * Test of toggleSortedColumn method, of class org.netbeans.swing.etable.ETableColumnModel.
     */
    public void testToggleSortedColumn() {
        System.out.println("testToggleSortedColumn");
        ETableColumnModel etcm = new ETableColumnModel();
        ETableColumn etc = new ETableColumn(0, null);
        etcm.addColumn(etc);
        
        etcm.toggleSortedColumn(etc, true);
        assertTrue(etcm.sortedColumns.contains(etc));
        assertTrue(etc.isAscending());
        assertTrue(etc.isSorted());
        
        etcm.toggleSortedColumn(etc, true);
        assertTrue(etcm.sortedColumns.contains(etc));
        assertFalse(etc.isAscending());
        assertTrue(etc.isSorted());
        
        etcm.toggleSortedColumn(etc, true);
        assertFalse(etcm.sortedColumns.contains(etc));
        assertFalse(etc.isSorted());
    }

    /**
     * Test of setColumnHidden method, of class org.netbeans.swing.etable.ETableColumnModel.
     */
    public void testSetColumnHidden() {
        System.out.println("testSetColumnHidden");
        ETableColumnModel etcm = new ETableColumnModel();
        ETableColumn etc = new ETableColumn(0, null);
        etcm.addColumn(etc);
        
        etcm.setColumnHidden(etc, true);
        assertTrue(etcm.hiddenColumns.contains(etc));
        assertTrue(etcm.getColumnCount() == 0);
        assertTrue(etcm.isColumnHidden(etc));
        
        etcm.setColumnHidden(etc, false);
        assertFalse(etcm.hiddenColumns.contains(etc));
        assertTrue(etcm.getColumnCount() == 1);
        assertFalse(etcm.isColumnHidden(etc));
    }

    /**
     * Test of clearSortedColumns method, of class org.netbeans.swing.etable.ETableColumnModel.
     */
    public void testClearSortedColumns() {
        System.out.println("testClearSortedColumns");
        ETableColumnModel etcm = new ETableColumnModel();
        ETableColumn etc = new ETableColumn(0, null);
        etcm.addColumn(etc);
        etcm.toggleSortedColumn(etc, true);
        
        etcm.clearSortedColumns();
        assertFalse(etcm.sortedColumns.contains(etc));
    }
}
