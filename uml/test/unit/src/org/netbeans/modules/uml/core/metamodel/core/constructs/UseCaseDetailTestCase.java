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
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 * Test cases for UseCaseDetail.
 */
public class UseCaseDetailTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(UseCaseDetailTestCase.class);
    }

    private IUseCaseDetail useCaseDetail;
    private IUseCase       u1;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        u1 = (IUseCase)FactoryRetriever.instance().createType("UseCase", null);
        //u1.prepareNode(DocumentFactory.getInstance().createElement(""));
        project.addElement(u1);
        
        useCaseDetail = u1.createUseCaseDetail();
        u1.addUseCaseDetail(useCaseDetail);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        useCaseDetail.delete();
        u1.delete();
    }
    
    public void testSetBody()
    {
        useCaseDetail.setBody("dlrowolleh");
        assertEquals("dlrowolleh", useCaseDetail.getBody());
    }

    public void testGetBody()
    {
        // Tested by testSetBody.
    }

    public void testSetParentDetail()
    {
        IUseCaseDetail sub = u1.createUseCaseDetail();
        sub.setParentDetail(useCaseDetail);
        assertEquals(useCaseDetail.getXMIID(), sub.getParentDetail().getXMIID());
    }

    public void testGetParentDetail()
    {
        // Tested by testSetParentDetail.
    }

    public void testCreateSubDetail()
    {
        assertNotNull(useCaseDetail.createSubDetail());
    }

    public void testAddSubDetail()
    {
        IUseCaseDetail sub = useCaseDetail.createSubDetail();
        useCaseDetail.addSubDetail(sub);
        assertEquals(1, useCaseDetail.getSubDetails().size());
        assertEquals(sub.getXMIID(), useCaseDetail.getSubDetails().get(0).getXMIID());
    }

    public void testRemoveSubDetail()
    {
        testAddSubDetail();
        useCaseDetail.removeSubDetail(useCaseDetail.getSubDetails().get(0));
        assertEquals(0, useCaseDetail.getSubDetails().size());
    }

    public void testGetSubDetails()
    {
        // Tested by testAddSubDetail.
    }
}
