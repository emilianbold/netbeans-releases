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
 * Created on Sep 19, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Behavior;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 *
 */
public class StateMachine extends Behavior implements IStateMachine
{

	/**
	 *  Constructor
	 */
	public StateMachine()
	{
		super();		
	}
	
	/**
	 * method AddConformance
	*/
	public void addConformance( final IProtocolConformance conform )
	{
		new ElementConnector<IStateMachine>()
            	.addChildAndConnect(this,false,"UML:StateMachine.conformance",
            						"UML:StateMachine.conformance/*",
            						conform,
            						new IBackPointer<IStateMachine>() 
            						{
            							public void execute(IStateMachine obj) 
            							{
            								conform.setSpecificMachine(obj);
            							}
            						}
                 );
	}

	/**
	 * method RemoveConformance
	*/
	public void removeConformance( final IProtocolConformance conform )
	{
		new ElementConnector<IStateMachine>()
			.removeElement(this,conform,
						   "UML:StateMachine.conformance/*",											
							new IBackPointer<IStateMachine>() 
							{
								public void execute(IStateMachine obj) 
								{
									conform.setSpecificMachine(obj);
								}
							}
			);
	}

	/**
	 * property Conformances
	*/
	public ETList<IProtocolConformance> getConformances()
	{
		return new ElementCollector< IProtocolConformance >()
						.retrieveElementCollection((IElement)this,"UML:StateMachine.conformance/*", IProtocolConformance.class);
	}
	
	public ETList<IRegion> getRegions()
	{
		return new ElementCollector< IRegion >()
						.retrieveElementCollection((IElement)this,"UML:Element.ownedElement/UML:Region", IRegion.class);
	}
	
	public IRegion getFirstRegion()
	{
		IRegion retRegion = null;
		ETList<IRegion> regions = getRegions();
		if (regions != null)
		{
			retRegion = regions.get(0);
		}
		return retRegion;
	}
	
	public void removeRegion( IRegion pRegion )
	{
		super.removeElement(pRegion);
	}
	
	public void addRegion( IRegion pRegion )
	{
		addOwnedElement(pRegion);
	}
	
	/**
	 * method AddConnectionPoint
	*/
	public void addConnectionPoint( IUMLConnectionPoint point )
	{
		super.addChild("UML:StateMachine.connectionPoint","UML:StateMachine.connectionPoint",
						point);
	}

	/**
	 * method RemoveConnectionPoint
	*/
	public void removeConnectionPoint( IUMLConnectionPoint point )
	{
		UMLXMLManip.removeChild(m_Node,point);
	}

	/**
	 * property ConnectionPoints
	*/
	public ETList<IUMLConnectionPoint> getConnectionPoints()
	{
		return new ElementCollector< IUMLConnectionPoint >()
						.retrieveElementCollection((IElement)this,"UML:StateMachine.connectionPoint/*", IUMLConnectionPoint.class);
	}
	
	/**
	 * method AddSubmachineState
	*/
	public void addSubmachineState( final IState state )
	{
		new ElementConnector<IStateMachine>()
			.addChildAndConnect(this,false,"submachineState",
					"submachineState", state,
					new IBackPointer<IStateMachine>() 
					{
						public void execute(IStateMachine obj) 
						{
							state.setSubmachine(obj);
						}
					}
              );											  
	}

	/**
	 * method RemoveSubmachineState
	*/
	public void removeSubmachineState( final IState state )
	{
		new ElementConnector< IStateMachine >().removeByID(this,state,"submachineState", 
												new IBackPointer<IStateMachine>() 
												{
													public void execute(IStateMachine obj) 
													{
														state.setSubmachine(obj);
													}
												}
											);
	}

	/**
	 * property SubmachinesStates
	*/
	public ETList<IState> getSubmachinesStates()
	{
		return new ElementCollector< IState >()
					.retrieveElementCollectionWithAttrIDs(this,"submachineState", IState.class);
	}

	public void establishNodePresence(Document doc, Node node)
	{
		buildNodePresence("UML:StateMachine", doc, node);
	}    
	
	/**
	 *
	 * Makes sure the this StateMachine always has at least one region.
	 *
	 * @param node[in] The node representing the state machine
	 */
	public void establishNodeAttributes(Element elem)
	{
		super.establishNodeAttributes(elem);
		IRegion region = new TypedFactoryRetriever<IRegion>().createType("Region");
		if (region != null)
		{
			addRegion(region);
		}
	}
	
	/**
	 *
	 * Retrieves the elements that this StateMachine owns dependent on how many regions
	 * the StateMachine contains. This allows the tree and property editor to display 
	 * the Region nodes only when multiple regions are present. If there is only one,
	 * then the contents of that region can be displayed without requiring the one
	 * tree node.
	 */
	public ETList<INamedElement> getContainedElements()
	{
		ETList<INamedElement> elements = null;
		ETList<IRegion> regions = getRegions();
		int count = regions.size();
		if (regions != null)
		{
			elements = new ETArrayList<INamedElement>();			
			if (count == 1)
			{
				IRegion reg = regions.get(0);
				if (reg != null)
				{
					elements = getRegionContents(reg);
				}
				  
			}
			else
			{
				for (int i=0;i<count;i++)
				{
					IRegion reg = regions.get(i);
					if (reg != null)
					{
						elements.add(reg);
					}					
				}
			}
		}
		return elements;
	}
	
	/**
	 *
	 * Retrieves the contents of the passed in IRegion and places them in the 
	 * passed in collection
	 *
	 * @param region[in] The region to get.
	 * @param elements[in] The collection that will be populated 
	 */
	protected ETList<INamedElement> getRegionContents(IRegion region)
	{
		ETList<INamedElement> elements = new ETArrayList<INamedElement>();
		if (region != null)
		{
			ETList<IStateVertex> pOwnedElements = region.getSubVertexes();
                        elements.addAll(pOwnedElements);
		}
		return elements;
	}
}



