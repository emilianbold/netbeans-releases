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
 * Created on Sep 26, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import java.util.Iterator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * @author aztec
 *
 */
public class StateMachineTestCase extends AbstractUMLTestCase
{ 
	private IStateMachine stateMachine = null;
	public StateMachineTestCase()
	{
		super();
	}

	public static void main(String args[])
	{
		junit.textui.TestRunner.run(StateMachineTestCase.class);
	}
	
	protected void setUp()
	{
		stateMachine = factory.createStateMachine(null);
		project.addElement(stateMachine);
	}
	
	public void testAddConformance()
	{
		IProtocolConformance protConf = factory.createProtocolConformance(null);
		project.addElement(protConf);
		
		stateMachine.addConformance(protConf);
		ETList<IProtocolConformance> confs = stateMachine.getConformances();
		assertNotNull(confs);
		
		Iterator iter = confs.iterator();
		while (iter.hasNext())
		{
			IProtocolConformance protConfGot = (IProtocolConformance)iter.next();
			assertEquals(protConf.getXMIID(), protConfGot.getXMIID());							
		}
		
		//Remove Input
		stateMachine.removeConformance(protConf);
		confs = stateMachine.getConformances();
		if (confs != null)
		{
			assertEquals(0,confs.size());
		} 
	}
	
	public void testAddRegion()
	{
		IRegion region = (IRegion)FactoryRetriever.instance().createType("Region", null);
		//region.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(region);
		
		stateMachine.addRegion(region);
		ETList<IRegion> regions = stateMachine.getRegions();
		assertNotNull(regions);

        assertEquals(region.getXMIID(), regions.get(1).getXMIID());
		
		//Remove Input
		stateMachine.removeRegion(region);
		assertEquals(1,stateMachine.getRegions().size());
	}
	
	public void testAddConnectionPoint()
	{
		IUMLConnectionPoint connectionPoint = (IUMLConnectionPoint)FactoryRetriever.instance().createType("ConnectionPoint", null);
		//connectionPoint.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(connectionPoint);
		
		stateMachine.addConnectionPoint(connectionPoint);
		ETList<IUMLConnectionPoint> connectionPoints = stateMachine.getConnectionPoints();
		assertNotNull(connectionPoints);
		
		Iterator iter = connectionPoints.iterator();
		while (iter.hasNext())
		{
			IUMLConnectionPoint connectionPointGot = (IUMLConnectionPoint)iter.next();
			assertEquals(connectionPoint.getXMIID(), connectionPointGot.getXMIID());							
		}
		
		//Remove Input
		stateMachine.removeConnectionPoint(connectionPoint);
		connectionPoints = stateMachine.getConnectionPoints();
		if (connectionPoints != null)
		{
			assertEquals(0,connectionPoints.size());
		}
	}
	
	public void testAddSubmachineState()
	{
		IState state = factory.createState(null);		
		project.addElement(state);
		
		stateMachine.addSubmachineState(state);
		ETList<IState> states = stateMachine.getSubmachinesStates();
		assertNotNull(states);
		
		Iterator iter = states.iterator();
		while (iter.hasNext())
		{
			IState stateGot = (IState)iter.next();
			assertEquals(state.getXMIID(), stateGot.getXMIID());							
		}
		
		//Remove Input
		stateMachine.removeSubmachineState(state);
		states = stateMachine.getSubmachinesStates();
		if (states != null)
		{
			assertEquals(0,states.size());
		}
	}
}



