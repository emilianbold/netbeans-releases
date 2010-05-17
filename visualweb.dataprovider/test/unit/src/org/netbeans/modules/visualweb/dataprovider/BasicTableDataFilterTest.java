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

import junit.framework.*;
import com.sun.data.provider.FilterCriteria;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.impl.BasicTableDataFilter;
import com.sun.data.provider.impl.CompareFilterCriteria;
import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.data.provider.impl.RegexFilterCriteria;
import org.netbeans.junit.NbTestCase;

public class BasicTableDataFilterTest extends NbTestCase {

    private ObjectListDataProvider tdp = null;
    private BasicTableDataFilter tdf = null;

    public BasicTableDataFilterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tdp = new ObjectListDataProvider();
        tdf = new BasicTableDataFilter();
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
        tdf = null;
        tdp = null;
        super.tearDown();
    }

    public void testFilter() {
        CompareFilterCriteria cfc1 = new CompareFilterCriteria(tdp.getFieldKey("intProperty"), new Integer(100));
        cfc1.setMatchLessThan(false);
        cfc1.setMatchEqualTo(true);
        cfc1.setMatchGreaterThan(true);
        cfc1.setInclude(true);

        CompareFilterCriteria cfc2 = new CompareFilterCriteria(tdp.getFieldKey("intProperty"), new Integer(200));
        cfc2.setMatchLessThan(true);
        cfc2.setMatchEqualTo(true);
        cfc2.setMatchGreaterThan(false);
        cfc2.setInclude(true);

        CompareFilterCriteria cfc3 = new CompareFilterCriteria(tdp.getFieldKey("intProperty"), new Integer(150));
        cfc3.setMatchLessThan(false);
        cfc3.setMatchEqualTo(true);
        cfc3.setMatchGreaterThan(false);
        cfc3.setInclude(false);

        RegexFilterCriteria rfc1 = new RegexFilterCriteria(tdp.getFieldKey("id"), ".9");
        rfc1.setInclude(false);

        tdf.setFilterCriteria(new FilterCriteria[]{
            //            rfc1,
            cfc1,
            cfc2,
            cfc3,
        });

        tdf.setMatchAllCriteria(true);
//        idf.setMatchAllCriteria(false);

        FilterCriteria[] ca = tdf.getFilterCriteria();
//        System.out.println("Filter Criteria (Match " + (tdf.isMatchAllCriteria() ? "ALL):" : "ANY):"));
//        for (int i = 0; i < ca.length; i++) {
//            System.out.println("  " + ca[i].getDisplayName());
//        }

        RowKey[] rks = tdp.getRowKeys(tdp.getRowCount(), null);
//        System.out.println("ORIGINAL DATA: (" + rks.length + " rows)");
//        for (int i = 0; i < 5; i++) {
//        for (int i = 0; i < rks.length; i++) {
//            System.out.println("> row[" + i + "] " +
//                "id=\"" + tdp.getValue(tdp.getFieldKey("id"), rks[i]) + "\" " +
//                "int=" + tdp.getValue(tdp.getFieldKey("intProperty"), rks[i]) + " " +
//                "long=" + tdp.getValue(tdp.getFieldKey("longProperty"), rks[i]) + " " +
//                "double=" + tdp.getValue(tdp.getFieldKey("doubleProperty"), rks[i]) + " " +
//                "string=\"" + tdp.getValue(tdp.getFieldKey("stringProperty"), rks[i]) + "\"");
//        }
        RowKey[] fks = tdf.filter(tdp, rks);
//        System.out.println("FILTERED DATA: (" + fks.length + " rows)");
//        for (int i = 0; i < fks.length; i++) {
//            System.out.println("> row[" + i + "] " +
//                "id=\"" + tdp.getValue(tdp.getFieldKey("id"), fks[i]) + "\" " +
//                "int=" + tdp.getValue(tdp.getFieldKey("intProperty"), fks[i]) + " " +
//                "long=" + tdp.getValue(tdp.getFieldKey("longProperty"), fks[i]) + " " +
//                "double=" + tdp.getValue(tdp.getFieldKey("doubleProperty"), fks[i]) + " " +
//                "string=\"" + tdp.getValue(tdp.getFieldKey("stringProperty"), fks[i]) + "\"");
//        }
    }
}
