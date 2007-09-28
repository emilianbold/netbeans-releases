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



package org.netbeans.modules.uml.ui.products.ad.diagramengines;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.IRelationshipDiscovery;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;

/**
 * @author KevinM
 * The IADRelationshipDiscovery provides drawing support for an TSGraphObject.
 * There is a one to one relationship between an TSGraphObject and an IADRelationshipDiscovery
 */
public interface IADRelationshipDiscovery extends IRelationshipDiscovery {

	public ETList<IPresentationElement> discoverNestedLinks(ETList<IElement> pDiscoverOnTheseElements);
	public ETList<IPresentationElement> discoverCommentLinks(ETList<IElement> pDiscoverOnTheseElement);
	public ETList<IPresentationElement> discoverPartFacadeLinks(ETList<IElement> pDiscoverOnTheseElements);
	public ETList<IPresentationElement> discoverAssociationClassLinks(ETList<IElement> pDiscoverOnTheseElements);
	public ETList<IPresentationElement> createPortPresentationElements(IPresentationElement pComponentPE);
	public IPresentationElement createPortPresentationElement(IPresentationElement pComponentPE,	IPort pPortToCreate);
	public ETList<IPresentationElement> createPortProvidedAndRequiredInterfaces(IPresentationElement pPortPE);
	public ETList<IPresentationElement> discoverMessages( ETList<IElement> pDiscoverOnTheseElements);
	public ETList<IPresentationElement> createPortProvidedInterface(IPresentationElement pPortPE, IInterface pInterface);   
	public ETList<IPresentationElement> createPortRequiredInterface(IPresentationElement pPortPE, IInterface pInterface);

	/// Discovers generalization relationships on the current diagram
	public ETList<IPresentationElement> discoverGeneralizations();
	public IPresentationElement createInterfaceAsIconPresentationElement(IInterface pElement,IPresentationElement pClass);
	public IPresentationElement createInterfaceAsClassPresentationElement(IInterface pElement);
	public IPresentationElement createAssociationClassPresentationElement(IAssociationClass pAssociationClass,
														IPresentationElement pClass1, IPresentationElement pClass2);
														
	//	These routines are used in DiscoverCommonRelations														
	public ETList<IPresentationElement> discoverNestedLinks(boolean bAutoRouteEdges,
									ETList<IElement> pNewElementsBeingCreated,
									ETList<IElement> pElementsAlreadyOnTheDiagrams);
									
	public ETList<IPresentationElement> discoverCommentLinks(boolean bAutoRouteEdges,
									ETList<IElement> pNewElementsBeingCreated,
									ETList<IElement> pElementsAlreadyOnTheDiagrams);
	public ETList<IPresentationElement> discoverPartFacadeLinks(boolean bAutoRouteEdges,
									ETList<IElement> pNewElementsBeingCreated,
									ETList<IElement> pElementsAlreadyOnTheDiagrams);
	public ETList<IPresentationElement> discoverAssociationClassLinks(boolean bAutoRouteEdges,
									ETList<IElement> pNewElementsBeingCreated,
									ETList<IElement> pElementsAlreadyOnTheDiagrams);
									
																						
}
