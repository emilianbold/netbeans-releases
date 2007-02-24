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

import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Constraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener;

public class InteractionConstraint extends Constraint
        implements IInteractionConstraint, IExpressionListener
{
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint#getMaxInt()
     */
    public IExpression getMaxInt()
    {
        return new ElementCollector<IExpression>()
            .retrieveSingleElement( 
                m_Node, "UML:InteractionConstraint.maxint/*", IExpression.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint#setMaxInt(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression)
     */
    public void setMaxInt(IExpression exp)
    {
        addChild( "UML:InteractionConstraint.maxint",
                  "UML:InteractionConstraint.maxint", 
                  exp );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint#getMinInt()
     */
    public IExpression getMinInt()
    {
        return new ElementCollector<IExpression>()
            .retrieveSingleElement( 
                m_Node, "UML:InteractionConstraint.minint/*", IExpression.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint#setMinInt(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression)
     */
    public void setMinInt(IExpression exp)
    {
        addChild( "UML:InteractionConstraint.minint",
                  "UML:InteractionConstraint.minint", 
                  exp );
    }


   // IExpressionListener 
   public boolean onPreBodyModified( IExpression exp, String proposedValue )
   {
      boolean proceed = true;

      EventDispatchRetriever ret = EventDispatchRetriever.instance();
      IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
      if (disp != null)
      {
         IEventPayload payload = disp.createPayload("PreDefaultBodyModified");
         proceed = disp.fireElementPreModified( this, payload);
      }
      
      return proceed;
   }
   
   public void onBodyModified( IExpression exp )
   {
      EventDispatchRetriever ret = EventDispatchRetriever.instance();
      IElementChangeEventDispatcher disp = (IElementChangeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
      if (disp != null)
      {
         IEventPayload payload = disp.createPayload("DefaultBodyModified");
         disp.fireElementModified( this, payload);
      }
   }
   
   public boolean onPreLanguageModified( IExpression exp, String proposedValue )
   {
      // Do nothing
      return true;
   }
   
   public void onLanguageModified( IExpression exp )
   {
      // Do nothing
   }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Constraint#establishNodePresence(org.dom4j.Document, org.dom4j.Node)
     */
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:InteractionConstraint", doc, parent);
    }
}