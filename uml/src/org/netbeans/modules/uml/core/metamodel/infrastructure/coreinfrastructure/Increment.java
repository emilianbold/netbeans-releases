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

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.DirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PreventElementReEntrance;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationshipEventsHelper;


public class Increment extends DirectedRelationship implements IIncrement
{
	public Increment()
	{
		super();
	}
	/**
	 * property PartialClassifier
	*/
	public IClassifier getPartialClassifier()
	{
		ElementCollector<IClassifier> collector = new ElementCollector<IClassifier>();
		return collector.retrieveSingleElementWithAttrID(this,"partialClassifier", IClassifier.class);					
	}

	/**
	 * property PartialClassifier
	*/
	public void setPartialClassifier( IClassifier classifier )
	{
		PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
        try
        {
    		if ( !reEnt.isBlocking() )
    		{
    			RelationshipEventsHelper help = new RelationshipEventsHelper(this);
    			if (help.firePreEndModified("partialClassifier",null,classifier))
    			{
    				final IClassifier partClassifier = classifier;
    				new ElementConnector<IIncrement>().setSingleElementAndConnect
    								(
    									this, partClassifier, 
    									"partialClassifier",
    									 new IBackPointer<IClassifier>() 
    									 {
    										 public void execute(IClassifier obj) 
    										 {
    											obj.addIncrement(Increment.this);
    										 }
    									 },
    									 new IBackPointer<IClassifier>() 
    									 {
    										 public void execute(IClassifier obj) 
    										 {
    											obj.removeIncrement(Increment.this);
    										 }
    									 }										
    								);				
    				help.fireEndModified();
    			}
    			else
    			{
    				//throw exception
    			}
    		}
        }
        finally
        {
            reEnt.releaseBlock();
        }				
	}

	/**
	 * property OtherPartialClassifier
	*/
	public IClassifier getOtherPartialClassifier()
	{
		ElementCollector<IClassifier> collector = new ElementCollector<IClassifier>();
		return collector.retrieveSingleElementWithAttrID(this,"otherPartialClassifier", IClassifier.class);			
	}

	/**
	 * property OtherPartialClassifier
	*/
	public void setOtherPartialClassifier( IClassifier classifier )
	{
		RelationshipEventsHelper help = new RelationshipEventsHelper(this);
		if (help.firePreEndModified("otherPartialClassifier",classifier,null))
		{
			super.setElement(classifier,"otherPartialClassifier");
			help.fireEndModified();
		}
		else
		{
			//throw exception
		}		
	}
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:Increment",doc,parent);
	}
		
}


