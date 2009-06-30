/*
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
package org.netbeans.jellytools;

import java.io.IOException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestSuite;

/** Test FavoritesOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class FavoritesOperatorTest extends JellyTestCase {

    public FavoritesOperatorTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static NbTest suite() {
        /*
        NbTestSuite suite = new NbTestSuite();
        // suites have to be in particular order
        suite.addTest(new FavoritesOperatorTest("testInvoke"));
        suite.addTest(new FavoritesOperatorTest("testTree"));
        suite.addTest(new FavoritesOperatorTest("testVerify"));
        return suite;
         */
        return (NbTest) NbModuleSuite.create(
        NbModuleSuite.createConfiguration(FavoritesOperatorTest.class).
                addTest("testInvoke").
                //addTest("testTree").
                addTest("testVerify").
                enableModules(".*").clusters(".*"));
    }
    
    /** Print out test name. */
    public void setUp() throws IOException {
        System.out.println("### "+getName()+" ###");
        openDataProjects("SampleProject");
    }
    
    private static FavoritesOperator favoritesOper;
    
    /**
     * Test of invoke method.
     */
    public void testInvoke() {
        FavoritesOperator.invoke().close();
        favoritesOper = FavoritesOperator.invoke();
    }


    /**
     * Test of tree method.
     */
    //this test will not compile if this is uncommented, because it uses
    //an operator from the IDE cluster
    //TODO: fix this test
    /*
    public void testTree() {
        RuntimeTabOperator rto = RuntimeTabOperator.invoke();
        // has to make tab visible
        favoritesOper.tree();
    }
    */
    
    /**
     * Test of verify method.
     */
    public void testVerify() {
        favoritesOper.verify();
        favoritesOper.close();
    }
}
