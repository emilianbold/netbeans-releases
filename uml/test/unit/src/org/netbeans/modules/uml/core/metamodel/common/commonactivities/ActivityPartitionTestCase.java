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


package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;/**
 * Test cases for ActivityPartition.
 */
public class ActivityPartitionTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ActivityPartitionTestCase.class);
    }

    private IActivityPartition activityPartition;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
		activityPartition = (IActivityPartition)FactoryRetriever.instance().createType("ActivityPartition", null);
		//activityPartition.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(activityPartition);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        //activityPartition.delete();
    }

    
    public void testSetActivity()
    {
		IActivity activity = factory.createActivity(null);
		project.addElement(activity);
		
		activityPartition.setActivity(activity);
		IActivity activityGot = activityPartition.getActivity();
		assertNotNull(activityGot);
		assertEquals(activity.getXMIID(), activityGot.getXMIID());
    }

    public void testSetIsDimension()
    {
		activityPartition.setIsDimension(true);
		assertTrue(activityPartition.getIsDimension());
    }

    public void testSetIsExternal()
    {
		activityPartition.setIsExternal(true);
		assertTrue(activityPartition.getIsExternal());
    }


    public void testSetRepresents()
    {
		IActivity activity = factory.createActivity(null);
        project.addElement(activity);
		activityPartition.setRepresents(activity);
		IElement elemGot = activityPartition.getRepresents();
		assertEquals(activity.getXMIID(), elemGot.getXMIID());
    }


    public void testAddSubPartition()
    {
		IActivityPartition partition = (IActivityPartition)FactoryRetriever.instance().createType("ActivityPartition", null);
		//partition.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(partition);
		
		activityPartition.addSubPartition(partition);
		ETList<IActivityPartition> partitions = activityPartition.getSubPartitions();
		assertNotNull(partitions);
				
		Iterator iter = partitions.iterator();
		while (iter.hasNext())
		{
			IActivityPartition partitionGot = (IActivityPartition)iter.next();
			assertEquals(partition.getXMIID(), partitionGot.getXMIID());							
		}
		
		//Remove Input
		activityPartition.removeSubPartition(partition);
		partitions = activityPartition.getSubPartitions();
		if (partitions != null)
		{
			assertEquals(0,partitions.size());
		}
    } 
}
