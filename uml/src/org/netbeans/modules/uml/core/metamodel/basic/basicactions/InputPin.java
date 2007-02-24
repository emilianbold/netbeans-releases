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


package org.netbeans.modules.uml.core.metamodel.basic.basicactions;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IFlow;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ValueSpecification;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class InputPin extends Pin implements IInputPin
{
    IValueSpecification spec = null;
    
    public IAction getAction()
    {
		return OwnerRetriever.getOwnerByType((IValueSpecification)this, IAction.class);
    }

    public void setAction(IAction action)
    {
       spec.setOwner(action);
    }

	public void setNode(Node n)
	{
		super.setNode(n);	
		if (spec == null)
		{
			spec = 	new ValueSpecification(); 
		}
		spec.setNode(n);
	}

    
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:InputPin", doc, parent);
    }
    
    //IValueSpecification methods
	
	public IMultiplicity getMultiplicity()
	{
		if (spec == null)
		{
			spec = new ValueSpecification(); 
		} 
		return spec.getMultiplicity();
	}
	
	public void setMultiplicity(IMultiplicity mult)
	{
		if (spec == null)
		{
			spec = new ValueSpecification(); 
		}
		spec.setMultiplicity(mult);
	}
}
