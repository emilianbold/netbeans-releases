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



/*
 * File       : ClassifierUtilitiesTestCase.java
 * Created on : Oct 21, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.modelanalysis;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * @author Aztec
 */
public class ClassifierUtilitiesTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ClassifierUtilitiesTestCase.class);
    }
    
    ClassifierUtilities cu = null;
    IClass clazz1 = null;
    IClass clazz2 = null;
    IInterface iface = null;
    
    IOperation op1 = null;
    IOperation op2 = null;
    IOperation op3 = null;
    IOperation op4 = null;
    IOperation op5 = null;
    IOperation op6 = null;
        
    
    protected void setUp() throws Exception
    {
        super.setUp();
        cu = new ClassifierUtilities();
        clazz1 = createClass("ClassOne");
        op1 = clazz1.createOperation("int", "m1");
        op2 = clazz1.createOperation("String", "m2");
        clazz1.addOperation(op1);
        clazz1.addOperation(op2);
        clazz2 = createSuperclass(clazz1, "Super");
        op3 = clazz2.createOperation("int", "m3");
        op4 = clazz2.createOperation("String", "m4");
        clazz2.addOperation(op3);
        clazz2.addOperation(op4);
        iface = createSuperinterface(clazz1, "SuperI");
        op5 = iface.createOperation("int", "m3");
        op6 = iface.createOperation("String", "m4");
        iface.addOperation(op5);
        iface.addOperation(op6);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        cu = null;
    }
    
    public void testCollectAllOperations()
    {
//        ETList<IOperation> opList = cu.collectAllOperations(clazz1);
//        assertEquals(6, opList.size());
    }
    
    public void testCollectGeneralizingClassifiers()
    {
        // Tested by testCollectAllOperations()
    }
    
    public void collectImplementedInterfaces()
    {
        // Tested by testCollectAllOperations()
    }    
}
