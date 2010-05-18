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


package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 */
public class AllCommonActionsTests 
{
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
    
	public static Test suite()
	{
        TestSuite suite = new TestSuite("Commonactions Tests");

        suite.addTest(new TestSuite(AddAttributeValueActionTestCase.class));
        suite.addTest(new TestSuite(AddVariableValueActionTestCase.class));
        suite.addTest(new TestSuite(AttributeActionTestCase.class));
        suite.addTest(new TestSuite(CallBehaviorActionTestCase.class));
        suite.addTest(new TestSuite(ClauseTestCase.class));
        suite.addTest(new TestSuite(ClearAssociationActionTestCase.class));
        suite.addTest(new TestSuite(ConditionalActionTestCase.class));
        suite.addTest(new TestSuite(CreateObjectActionTestCase.class));
        suite.addTest(new TestSuite(DestroyObjectActionTestCase.class));
        suite.addTest(new TestSuite(GroupActionTestCase.class));
        suite.addTest(new TestSuite(LinkActionTestCase.class));
        suite.addTest(new TestSuite(LinkEndCreationDataTestCase.class));
        suite.addTest(new TestSuite(LinkEndDataTestCase.class));
        suite.addTest(new TestSuite(LoopActionTestCase.class));
        suite.addTest(new TestSuite(PrimitiveFunctionTestCase.class));
        suite.addTest(new TestSuite(ReadAttributeActionTestCase.class));
        suite.addTest(new TestSuite(ReadLinkActionTestCase.class));
        suite.addTest(new TestSuite(ReadSelfActionTestCase.class));
        suite.addTest(new TestSuite(ReadVariableActionTestCase.class));
        suite.addTest(new TestSuite(SwitchActionTestCase.class));
        suite.addTest(new TestSuite(SwitchOptionTestCase.class));
        suite.addTest(new TestSuite(SynchronizedActionTestCase.class));
        suite.addTest(new TestSuite(TestIdentityActionTestCase.class));
        suite.addTest(new TestSuite(VariableActionTestCase.class));
        suite.addTest(new TestSuite(VariableTestCase.class));
        suite.addTest(new TestSuite(WriteAttributeActionTestCase.class));
        suite.addTest(new TestSuite(WriteVariableActionTestCase.class));


        return suite;
	}
}
