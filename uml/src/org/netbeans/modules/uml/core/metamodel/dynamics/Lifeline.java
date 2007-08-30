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

package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.EventState;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IActor;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPart;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class Lifeline extends NamedElement implements ILifeline
{
    // define default type for creating lifeline representing classifier
    private static final String unknownClassifierType = "Class";
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#getEvents()
     */
    public ETList<IEventOccurrence> getEvents()
    {
        return new ElementCollector<IEventOccurrence>( )
            .retrieveElementCollectionWithAttrIDs( 
                this, "events", IEventOccurrence.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#removeEvent(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void removeEvent(IEventOccurrence event)
    {
        removeElementByID( event, "events" );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#addEvent(org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence)
     */
    public void addEvent(IEventOccurrence event)
    {
        addElementByID( event, "events" );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#getDiscriminator()
     */
    public IExpression getDiscriminator()
    {
        return new ElementCollector<IExpression>( )
            .retrieveSingleElement( 
                m_Node, "UML:Lifeline.discriminator/*", IExpression.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#setDiscriminator(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression)
     */
    public void setDiscriminator(IExpression exp)
    {
        addChild(
                "UML:Lifeline.discriminator", 
                "UML:Lifeline.discriminator", 
                exp );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#getRepresents()
     */
    public ITypedElement getRepresents()
    {
        return new ElementCollector<ITypedElement>( )
            .retrieveSingleElementWithAttrID( this, "represents", ITypedElement.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#setRepresents(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement)
     */
    public void setRepresents(ITypedElement element)
    {
        ETPairT<IDynamicsEventDispatcher, Boolean> dispP = 
            firePreChangeRepresentingClassifier(this, element);
        boolean proceed = dispP.getParamTwo().booleanValue();
        if (proceed)
        {
            setElement(element, "represents");
            fireChangeRepresentingClassifier(
                    dispP.getParamOne(), this, element);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#getPartDecompositions()
     */
    public ETList<IPartDecomposition> getPartDecompositions()
    {
        return new ElementCollector<IPartDecomposition>( )
            .retrieveElementCollectionWithAttrIDs( this, "decomposedAs", IPartDecomposition.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#removePartDecomposition(org.netbeans.modules.uml.core.metamodel.dynamics.IPartDecomposition)
     */
    public void removePartDecomposition(IPartDecomposition decomp)
    {
        removeElementByID( decomp, "decomposedAs" );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#addPartDecomposition(org.netbeans.modules.uml.core.metamodel.dynamics.IPartDecomposition)
     */
    public void addPartDecomposition(IPartDecomposition decomp)
    {
        addElementByID( decomp, "decomposedAs" );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#getInteraction()
     */
    public IInteraction getInteraction()
    {
        IElement owner = getOwner();
        return owner instanceof IInteraction? (IInteraction) owner : null;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#setInteraction(org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction)
     */
    public void setInteraction(IInteraction value)
    {
        setOwner(value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#addCoveringFragment(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment)
     */
    public void addCoveringFragment(final IInteractionFragment frag)
    {
        new ElementConnector<ILifeline>( )
            .addChildAndConnect( 
                this, true, "coveredBy", "coveredBy", frag, 
                new IBackPointer<ILifeline>( )
                {
                    public void execute(ILifeline lifeline)
                    {
                        frag.addCoveredLifeline(lifeline);
                    }
                } );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#removeCoveringFragment(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment)
     */
    public void removeCoveringFragment(final IInteractionFragment frag)
    {
        new ElementConnector<ILifeline>( )
            .removeByID( 
                this, frag, "coveredBy",
                new IBackPointer<ILifeline>( )
                {
                    public void execute(ILifeline lifeline)
                    {
                        frag.removeCoveredLifeline(lifeline);
                    } 
                } );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#getCoveringFragments()
     */
    public ETList<IInteractionFragment> getCoveringFragments()
    {
        return new ElementCollector<IInteractionFragment>( )
            .retrieveElementCollectionWithAttrIDs( this, "coveredBy", IInteractionFragment.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#createMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, int)
     */
    public IMessage createMessage(IInteractionFragment fromOwner, IElement toElement, IInteractionFragment toOwner, IOperation oper, int kind)
    {
        IDynamicsRelationFactory factory = new DynamicsRelationFactory();
        IMessage message = factory.createMessage(this, fromOwner, toElement, 
                toOwner, oper, kind);
        return message;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#insertMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage, org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, int)
     */
    public IMessage insertMessage(IMessage fromBeforeMessage, IInteractionFragment fromOwner, IElement toElement, IInteractionFragment toOwner, IOperation oper, int kind)
    {
        org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl.DABlocker.startBlocking();
        
        IMessage message = null;
        try
        {
           IDynamicsRelationFactory factory = new DynamicsRelationFactory();
           message = factory.insertMessage(fromBeforeMessage, this, fromOwner, 
                                           toElement, toOwner, oper, kind);
        }
        finally
        {
            org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl.DABlocker.stopBlocking();
        }
        
        return message;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#deleteMessage(org.netbeans.modules.uml.core.metamodel.dynamics.IMessage)
     */
    public void deleteMessage(IMessage pMessage)
    {
        // C++ code does nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#createCreationalMessage(org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline)
     */
    public IMessage createCreationalMessage(ILifeline toLine)
    {
        // C++ code does nothing.
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#createDestructor()
     */
    public IActionOccurrence createDestructor()
    {
        return new DynamicsRelationFactory()
            .createActionOccurrence(null, this, "DestroyAction");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#initializeWith(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void initializeWith(IClassifier classifier)
    {
        initializeWithType(classifier);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#initializeWithClass(org.netbeans.modules.uml.core.metamodel.core.constructs.IClass)
     */
    public void initializeWithClass(IClass clazz)
    {
        initializeWithType(clazz);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#initializeWithActor(org.netbeans.modules.uml.core.metamodel.core.constructs.IActor)
     */
    public void initializeWithActor(IActor pActor)
    {
        setRepresents(pActor);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#initializeWithComponent(org.netbeans.modules.uml.core.metamodel.structure.IComponent)
     */
    public void initializeWithComponent(IComponent pComponent)
    {
        initializeWithType(pComponent);
    }
    
    private <Type extends IClassifier> void initializeWithType(Type type)
    {
        if (type instanceof IStructuredClassifier)
        {
            IPart part = new TypedFactoryRetriever<IPart>().createType("Part");
            IStructuredClassifier struc = (IStructuredClassifier) type;
            
            struc.addPart(part);
            part.setType(type);
            
            setRepresents(part);
        }
        else
        {
            setRepresentingClassifier(type);
        }
    }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#getRepresentingClassifier()
    */
   public IClassifier getRepresentingClassifier()
   {
      IClassifier classifier = null;
      
      ITypedElement represented = getRepresents();
      if( represented != null )
      {
         classifier = represented.getType();
         if( (null == classifier) &&
             (represented instanceof IClassifier) )
         {
            // Check to see if we have an Actor, in which case it will
            // QI to a Classifer. An Actor derives off of IPart
            
            classifier = (IClassifier)represented;
         }
      }
      
      return classifier;
   }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#setRepresentingClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setRepresentingClassifier(IClassifier classifier)
    {
        if (classifier != null)
        {   
            if (!classifier.isSame(getRepresents()))
            {
                ITypedElement rep = null;
                if (classifier instanceof IActor)
                {
                    rep = (ITypedElement) classifier;
                }
                else
                {
                    rep = new ElementCollector<ITypedElement>()
                    .retrieveSingleElement(
                            classifier.getNode(),
                            "./UML:Element.ownedElement/UML:Part", ITypedElement.class );
                    if (rep == null)
                    {
                        // Need to create a Part
                        rep = new TypedFactoryRetriever<IPart>().createType("Part");
                        if (rep != null)
                        {
                            rep.setType(classifier);
                            classifier.addElement(rep);
                        }
                    }
                }
                
                setRepresents(rep);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#setRepresentingClassifier2(java.lang.String)
     */
    public void setRepresentingClassifier2(String classifierName)
    {
        if (classifierName != null && classifierName.length() > 0)
        {
            EventState es = null;

            try
            {
               es = new EventState( EventDispatchNameKeeper.lifeTime(), "RepresentingClassifier" );
               
               // 110338 the default unknown classifier type is different
               String original = PreferenceAccessor.instance().getUnknownClassifierType();
               PreferenceAccessor.instance().setUnknownClassifierType(unknownClassifierType);
               
               INamedElement element = resolveSingleTypeFromString(classifierName);
               
               // restore
               PreferenceAccessor.instance().setUnknownClassifierType(original);
               
               setRepresentingClassifier( (IClassifier) element );
            }
            finally
            {
               if( es != null )
               {
                  es.existState();
               }

            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#setRepresentingClassifierWithAlias(java.lang.String)
     */
    public void setRepresentingClassifierWithAlias(String alias)
    {
        if (alias != null && alias.length() > 0)
        {    
            if (showAliasedNames())
            {
                IClassifier cl = getRepresentingClassifier();
                if (cl != null)
                    cl.setAlias(alias);
            }
            else
            {
                setRepresentingClassifier2(alias);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#setIsActorLifeline(boolean val)
     */
    public void setIsActorLifeline(boolean val)
    {
       String boolStr = String.valueOf(val);
       UMLXMLManip.setAttributeValue(this, "actorLifeline", boolStr);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline#getIsActorLifeline(boolean val)
     */
    public boolean getIsActorLifeline()
    {
       String booleanStr = UMLXMLManip.getAttributeValue(getNode(), "actorLifeline");
       return (booleanStr != null ? Boolean.parseBoolean(booleanStr) : false);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
     */
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:Lifeline", doc, node);
    }
    
    protected ETPairT<IDynamicsEventDispatcher, Boolean>
        firePreChangeRepresentingClassifier(
            ILifeline lifeline, 
            ITypedElement represents )
    {
        IDynamicsEventDispatcher dispatcher = (IDynamicsEventDispatcher)
            EventDispatchRetriever.instance().getDispatcher(
                EventDispatchNameKeeper.dynamics());
        
        boolean proceed = false;
        if (dispatcher != null && lifeline != null && represents != null)
        {
            IEventPayload payload = 
                dispatcher.createPayload("PreChangeRepresentingClassifier");
            proceed = dispatcher.firePreChangeRepresentingClassifier(lifeline, 
                    represents, payload);
        }
        
        return new ETPairT<IDynamicsEventDispatcher, Boolean>( 
                dispatcher, new Boolean(proceed) );
    }
    
    protected void fireChangeRepresentingClassifier(
            IDynamicsEventDispatcher dispatcher,
            ILifeline lifeline,
            ITypedElement represents)
    {
        if (dispatcher != null && lifeline != null && represents != null)
        {
            IEventPayload payload = 
                dispatcher.createPayload("ChangeRepresentingClassifier");
            dispatcher.fireChangeRepresentingClassifier(lifeline, represents, payload);
        }
    }
    
	protected String retrieveDefaultName()
	{
		return "";
	}
	
	protected void establishDefaultName()
	{
	}
}