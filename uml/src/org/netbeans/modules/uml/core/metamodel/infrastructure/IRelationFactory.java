/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.core.metamodel.infrastructure;

import java.util.ArrayList;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IAutonomousElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.AssociationKindEnum;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IRelationFactory 
{
	///*[out]*/IInterface** outSupplier, [out] IDependency dep 
	public ETPairT<IInterface, IDependency> createImplementation(INamedElement client, INamedElement supplier, INamespace space); 
	public IPresentationReference createPresentationReference(IElement referencingElement, IPresentationElement referredElement);
	public IReference createReference(IElement referencingElement,IElement referredElement);
	
	public IGeneralization createGeneralization(IClassifier parent, IClassifier child); 
	public IAssociation createAssociation(IClassifier start, IClassifier end, INamespace space);
	public IAssociation createAssociation2(IClassifier start, IClassifier end, int kind, boolean startNavigable, boolean endNavigable, INamespace space); 
	public IAssociationClass createAssociationClass(IClassifier start, IClassifier end, int kind, boolean startNavigable, boolean endNavigable, INamespace space);
	
	public IDependency createDependency(INamedElement client, INamedElement supplier, INamespace space); 
	public IDependency createDependency2(INamedElement client, INamedElement supplier, String depType, INamespace space);
	
	/*
	 TODO: Create IRelationProxies
	 */
	public ETList<IRelationProxy> determineCommonRelations(ETList<IElement> elements); 
	public ETList<IRelationProxy> determineCommonRelations2(String pDiagramXMIID, ETList<IElement> elements); 
	public ETList<IRelationProxy> determineCommonRelations3(ETList<IElement> elements, ETList<IElement> elementsOnDiagram);

	public IDerivation createDerivation(IClassifier instanciation, IClassifier actualTemplate);
	public IDirectedRelationship createImport(IElement importingElement, IAutonomousElement elementToImport);
}
