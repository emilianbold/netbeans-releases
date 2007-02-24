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


package org.netbeans.modules.uml.core.metamodel.core.constructs;

import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Feature;


public class Enumeration extends DataType implements IEnumeration
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration#addLiteral(org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral)
     */
    public void addLiteral(final IEnumerationLiteral literal)
    {
		if (literal != null)
		{
			// Pop the context that has been plugging events
			// for feature.
			revokeEventContext(literal);
	
			EventDispatchRetriever ret = EventDispatchRetriever.instance();
            
			IClassifierEventDispatcher disp = 
                (IClassifierEventDispatcher)ret.getDispatcher(
                    EventDispatchNameKeeper.classifier());
            
			boolean proceed = true;
			
            if (disp != null) 
            {
				IEventPayload payload = disp.createPayload("EnumerationLiteralPreAdded");
				proceed = disp.fireEnumerationLiteralPreAdded(this, literal, payload);
			}
	
			if (proceed) 
            {
               new ElementConnector<IEnumeration>().addChildAndConnect
               ( 
                   this,
                   false, 
                   "UML:Enumeration.literal", // NOI18N
                   "UML:Enumeration.literal", // NOI18N
                   literal, 
                   new IBackPointer<IEnumeration>() 
                   {
                        public void execute(IEnumeration enumeration) 
                        {
                            literal.setEnumeration(enumeration);
                        }
                   }
                );
                
                if (disp != null) 
                {
					IEventPayload payload = disp.createPayload("EnumerationLiteralAdded");
					disp.fireEnumerationLiteralAdded(this, literal, payload);
				}
                
				else 
					proceed = false;
            }     
		}
    }
    
	
    protected void revokeEventContext(IEnumerationLiteral feature)
	{
		EventContextManager man = new EventContextManager();
		man.revokeEventContext(feature, null);
	}
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration#getLiterals()
     */
    public ETList <IEnumerationLiteral> getLiterals()
    {
        ElementCollector< IEnumerationLiteral > col = new ElementCollector< IEnumerationLiteral >();
        return col.retrieveElementCollection(m_Node, "UML:Enumeration.literal/*", IEnumerationLiteral.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration#removeLiteral(org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral)
     */
    public void removeLiteral(final IEnumerationLiteral literal)
    {
        new ElementConnector< IEnumeration >().removeElement( 
                        this, 
                        literal, 
                        "UML:Enumeration.literal/*",
                        new IBackPointer<IEnumeration>() {
                            public void execute(IEnumeration enumeration) {
                                literal.setEnumeration(enumeration);
                            }
                        } 
        );
    }
    
    public IEnumerationLiteral createLiteral(String name)
    {
       IEnumerationLiteral retVal = null;
       FactoryRetriever ret = FactoryRetriever.instance();
       
       Object obj = ret.createType("EnumerationLiteral", null);
       if (obj != null && obj instanceof IEnumerationLiteral)
       {
          retVal = (IEnumerationLiteral)obj;
          
          addLiteral(retVal);
          retVal.setName(name);
       }
       return retVal;
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:Enumeration", doc, node);
    }
    
    public ETList <String> getCollidingNamesForElement(INamedElement elem)
    {
       ETArrayList <String> list = new  ETArrayList <String>();

       list.add("UML:Class");
       list.add("UML:Interface");
       list.add("UML:Enumeration");

       return (ETList)list;
    } 
}
