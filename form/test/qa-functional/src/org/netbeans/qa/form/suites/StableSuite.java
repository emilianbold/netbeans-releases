/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.qa.form.suites;

import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.OpenTempl_defaultPackTest;
import org.netbeans.qa.form.actions.actionsTest;
import org.netbeans.qa.form.beans.AddAndRemoveBeansTest;
import org.netbeans.qa.form.beans.AddBeanFormsTest;
import org.netbeans.qa.form.undoredo.BaseTest;
import org.netbeans.qa.form.visualDevelopment.AddComponents_SWING;

/**
 *
 * @author Pavel Pribyl
 * 
 */
public class StableSuite extends TestCase {

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(StableSuite.class)

                .addTest(OpenTempl_defaultPackTest.class,
                "testApplet", "testDialog", "testFrame", "testInter", "testAppl", "testMidi", "testPanel", "testBean")

                .addTest(AddAndRemoveBeansTest.class,
                "testAddingBeans",
                "testRemovingBeans")

//                .addTest (AddBeanFormsTest.class,
//                "testCompileBeanClasses",
//                "testAddingBeanFormWithVisualBeanSuperclass",
//                "testAddingBeanFormWithNonVisualBeanSuperclass")
                
                .addTest(actionsTest.class,
                "testDummy",
                "testDuplicate",
                "testEditContainer",
                "testResizing",
                "testBeans")

                .addTest(BaseTest.class,
                "testScenario")

                .addTest(AddComponents_SWING.class,
                "testAddAndCompile")

                .clusters(".*").enableModules(".*") 
                );
    }

    public void testEmpty() {
        //Empty test. Just to be able to use StableSuite.class for createConfiguration method
    }
}
