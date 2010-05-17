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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.uml.core.reverseengineering.reframework;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 */
public class AllREFrameworkTests 
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
	public static Test suite()
	{
        TestSuite suite = new TestSuite("Reframework Tests");

        suite.addTest(new TestSuite(ActionEventTestCase.class));
        suite.addTest(new TestSuite(AttributeEventTestCase.class));
        suite.addTest(new TestSuite(ClassEventTestCase.class));
        suite.addTest(new TestSuite(CompositeClassLocatorTestCase.class));
        suite.addTest(new TestSuite(CreationEventTestCase.class));
        suite.addTest(new TestSuite(DependencyEventTestCase.class));
        suite.addTest(new TestSuite(DestroyEventTestCase.class));
        suite.addTest(new TestSuite(InitializeEventTestCase.class));
        suite.addTest(new TestSuite(JumpEventTestCase.class));
        suite.addTest(new TestSuite(LanguageLibraryTestCase.class));
        suite.addTest(new TestSuite(MethodDetailParserDataTestCase.class));
        suite.addTest(new TestSuite(MethodEventTestCase.class));
        suite.addTest(new TestSuite(OperationEventTestCase.class));
        suite.addTest(new TestSuite(PackageEventTestCase.class));
        suite.addTest(new TestSuite(ParserDataTestCase.class));
        suite.addTest(new TestSuite(REActionSequenceTestCase.class));
        suite.addTest(new TestSuite(REActionTestCase.class));
        suite.addTest(new TestSuite(REArgumentTestCase.class));
        suite.addTest(new TestSuite(REAttributeTestCase.class));
        suite.addTest(new TestSuite(RECallActionTestCase.class));
        suite.addTest(new TestSuite(REClassElementTestCase.class));
        suite.addTest(new TestSuite(REClassFeatureTestCase.class));
        suite.addTest(new TestSuite(REClassTestCase.class));
        suite.addTest(new TestSuite(REClauseTestCase.class));
        suite.addTest(new TestSuite(RECreateActionTestCase.class));
        suite.addTest(new TestSuite(RECriticalSectionTestCase.class));
        suite.addTest(new TestSuite(REDestroyActionTestCase.class));
        suite.addTest(new TestSuite(REExceptionJumpHandlerEventTestCase.class));
        suite.addTest(new TestSuite(ReferenceEventTestCase.class));
        suite.addTest(new TestSuite(REOperationTestCase.class));
        suite.addTest(new TestSuite(REParameterTestCase.class));
        suite.addTest(new TestSuite(REReturnActionTestCase.class));
        suite.addTest(new TestSuite(TestEventTestCase.class));

        return suite;
	}
}
