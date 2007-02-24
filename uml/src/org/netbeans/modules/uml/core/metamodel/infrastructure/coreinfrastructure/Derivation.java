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

package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.Dependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

import org.dom4j.Node;
import org.dom4j.Document;



public class Derivation extends Dependency implements IDerivation 
{
	public ETList<IUMLBinding> getBindings()
	{
		ElementCollector<IUMLBinding> collector = new ElementCollector<IUMLBinding>();
		return collector.retrieveElementCollection((IElement)this,"UML:Element.ownedElement/*", IUMLBinding.class);		
	}
	
	public void removeBinding( IUMLBinding binding )
	{
		removeElement(binding);	
	}
	
	public void addBinding( IUMLBinding binding )
	{
		addElement(binding);
	}
	
	public IClassifier getTemplate()
	{
		INamedElement namedElem = getSupplier();
		return (IClassifier)namedElem;
	}
	
	public void setTemplate( IClassifier classifier )
	{
		setSupplier(classifier);	
	}
	
	public IClassifier getDerivedClassifier()
	{		
		return OwnerRetriever.getOwnerByType(this, IClassifier.class);
	}
	
	public void setDerivedClassifier( IClassifier classifier )
	{
		setOwner(classifier);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Derivation",doc,parent);
	}
	
}



