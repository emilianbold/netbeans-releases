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
public class UMLConnectionPointTestCase extends AbstractUMLTestCase
{
	private IUMLConnectionPoint connPoint = null;
	
	public UMLConnectionPointTestCase()
	{
		super();
	}
	
	protected void setUp()
	{
		connPoint = (IUMLConnectionPoint)FactoryRetriever.instance().createType("ConnectionPoint", null);
		//connPoint.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(connPoint); 
	}
	
	public void testSetDefinition()
	{
		IUMLConnectionPoint defn = (IUMLConnectionPoint)FactoryRetriever.instance().createType("ConnectionPoint", null);
		//defn.prepareNode(DocumentFactory.getInstance().createElement(""));
		project.addElement(defn); 
		
		connPoint.setDefinition(defn);
		IUMLConnectionPoint defnGot = connPoint.getDefinition();
		assertEquals(defn.getXMIID(), defnGot.getXMIID()); 
	}
	
	public void testAddEntry()
	{
		IPseudoState pseudoState = factory.createPseudoState(null);
		project.addElement(pseudoState);
		
		//add and get
		connPoint.addEntry(pseudoState);
		ETList<IPseudoState> pseudoStates = connPoint.getEntries();
		assertNotNull(pseudoStates);
				
		Iterator iter = pseudoStates.iterator();
		while (iter.hasNext())
		{
			IPseudoState pseudoStateGot = (IPseudoState)iter.next();
			assertEquals(pseudoState.getXMIID(), pseudoStateGot.getXMIID());							
		}
		
		//Remove Input
		connPoint.removeEntry(pseudoState);
		pseudoStates = connPoint.getEntries();
		if (pseudoStates != null)
		{
			assertEquals(0,pseudoStates.size());
		}
	}
}



