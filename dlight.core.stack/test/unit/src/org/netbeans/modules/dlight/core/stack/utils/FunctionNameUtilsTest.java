/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.utils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alexander Simon
 */
public class FunctionNameUtilsTest {

    public FunctionNameUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testOperator1() {
        String name = FunctionNameUtils.getFunctionQName("int*Matrix<int>::operator[](unsigned)");
        assertEquals("Matrix<int>::operator[]", name);
    }
    
    @Test
    public void testOperator2() {
        String name = FunctionNameUtils.getFunctionQName("std::complex<long double>&std::complex<long double>::operator*=(const std::complex<long double>&)");
        assertEquals("std::complex<long double>::operator*=", name);
    }
    
    @Test
    public void testOperator3() {
        String name = FunctionNameUtils.getFunctionQName("__type_0 std::norm<long double>(const std::complex<__type_0>&)");
        assertEquals("std::norm<long double>", name);
    }

    @Test
    public void testOperator4() {
        String name = FunctionNameUtils.getFunctionQName("long double std::complex<long double>::real()const");
        assertEquals("std::complex<long double>::real", name);
    }
    
    @Test
    public void testOperator5() {
        String name = FunctionNameUtils.getFunctionQName("long double std::sqrt(long double)");
        assertEquals("std::sqrt", name);
    }
    
    @Test
    public void testOperator6() {
        String name = FunctionNameUtils.getFunctionQName("std::complex<__type_0>std::operator+<long double>(const std::complex<__type_0>&,const std::complex<__type_0>&)");
        assertEquals("std::complex<__type_0>std::operator+<long double>", name);
    }

    @Test
    public void testOperator7() {
        String name = FunctionNameUtils.getFunctionQName("std::complex<__type_0>std::operator*<long double>(const std::complex<__type_0>&,const std::complex<__type_0>&)");
        assertEquals("std::complex<__type_0>std::operator*<long double>", name);
    }

    @Test
    public void testOperator8() {
        String name = FunctionNameUtils.getFunctionQName("std::complex<long double>*Matrix<std::complex<long double> >::operator[](unsigned)");
        assertEquals("Matrix<std::complex<long double> >::operator[]", name);
    }

    @Test
    public void testOperator9() {
        String name = FunctionNameUtils.getFunctionQName("std::complex<long double>::complex #Nvariant 1(const long double&,const long double&)");
        assertEquals("std::complex<long double>::complex", name);
    }
    
    @Test
    public void testOperator10() {
        String name = FunctionNameUtils.getFunctionQName("std::ostream&std::operator<<(std::ostream&,const Customer&)");
        assertEquals("std::operator<<", name);
    }
    
    @Test
    public void testOperator11() {
        String name = FunctionNameUtils.getFunctionQName("std::ostream&std::operator>>(std::ostream&,const Customer&)");
        assertEquals("std::operator>>", name);
    }
}
