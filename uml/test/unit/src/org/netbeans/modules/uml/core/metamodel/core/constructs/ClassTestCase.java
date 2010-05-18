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


package org.netbeans.modules.uml.core.metamodel.core.constructs;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IReception;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for Class.
 */
public class ClassTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ClassTestCase.class);
    }

    private IClass clazz;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        clazz = createClass("Indiana");
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        clazz.delete();
    }

    
    public void testSetIsActive()
    {
        clazz.setIsActive(true);
        assertTrue(clazz.getIsActive());
        clazz.setIsActive(false);
        assertFalse(clazz.getIsActive());
    }

    public void testGetIsActive()
    {
        // Tested by testSetIsActive.
    }

    public void testSetIsStruct()
    {
        clazz.setIsStruct(true);
        assertTrue(clazz.getIsStruct());
        clazz.setIsStruct(false);
        assertFalse(clazz.getIsStruct());
    }

    public void testGetIsStruct()
    {
        // Tested by testSetIsStruct.
    }

    public void testSetIsUnion()
    {
        clazz.setIsUnion(true);
        assertTrue(clazz.getIsUnion());
        clazz.setIsUnion(false);
        assertFalse(clazz.getIsUnion());
    }

    public void testGetIsUnion()
    {
        // Tested by testSetIsUnion.
    }

    public void testAddReception()
    {
        IReception rec = factory.createReception(null);
        project.addElement(rec);
        clazz.addReception(rec);

        assertEquals(1, clazz.getReceptions().size());
        assertEquals(rec.getXMIID(), clazz.getReceptions().get(0).getXMIID());
    }

    public void testRemoveReception()
    {
        testAddReception();
        clazz.removeReception(clazz.getReceptions().get(0));
        assertEquals(0, clazz.getReceptions().size());
    }

    public void testGetReceptions()
    {
        // Tested by testAddReception.
    }
}
