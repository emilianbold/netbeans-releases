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

/*
 * File       : ActivityEdge.java
 * Created on : Sep 16, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactivities;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.core.foundation.OwnerRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.DispatchHelper;

/**
 * @author Aztec
 */
public class ActivityEdge extends RedefinableElement implements IActivityEdge, IExpressionListener
{

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#addGroup(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup)
    */
   public void addGroup(IActivityGroup pGroup)
   {
      addElement(pGroup);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#getActivity()
    */
   public IActivity getActivity()
   {
      return OwnerRetriever.getOwnerByType(this, IActivity.class);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#getGroups()
    */
   public ETList < IActivityGroup > getGroups()
   {
      return new ElementCollector < IActivityGroup > ().retrieveElementCollection((IElement)this, "UML:Element.ownedElement", IActivityGroup.class);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#getGuard()
    */
   public IValueSpecification getGuard()
   {
      return new ElementCollector < IValueSpecification > ().retrieveSingleElement(this, "UML:ActivityEdge.guard/*", IValueSpecification.class);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#getSource()
    */
   public IActivityNode getSource()
   {
      IActivityNode activityNode = null;
      try
      {
         activityNode = new ElementCollector < IActivityNode > ().retrieveSingleElementWithAttrID(this, "source", IActivityNode.class);
      }
      catch (ClassCastException e)
      {
         // It is possible to have a IState derived source
      }

      return activityNode;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#getTarget()
    */
   public IActivityNode getTarget()
   {
      IActivityNode activityNode = null;
      try
      {
         activityNode = new ElementCollector < IActivityNode > ().retrieveSingleElementWithAttrID(this, "target", IActivityNode.class);
      }
      catch (ClassCastException e)
      {
         // It is possible to have a IState derived target
      }

      return activityNode;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#getWeight()
    */
   public IValueSpecification getWeight()
   {
      return new ElementCollector < IValueSpecification > ().retrieveSingleElement(this, "UML:ActivityEdge.weight/*", IValueSpecification.class);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#removeGroup(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityGroup)
    */
   public void removeGroup(IActivityGroup pGroup)
   {
      if (pGroup != null)
         removeElement(pGroup);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#setActivity(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity)
    */
   public void setActivity(IActivity value)
   {
      setNamespace(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#setGuard(org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification)
    */
   public void setGuard(IValueSpecification value)
   {
      addChild("UML:ActivityEdge.guard", "UML:ActivityEdge.guard", value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#setSource(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode)
    */
   public void setSource(IActivityNode value)
   {
      new ElementConnector < IActivityEdge > ().setSingleElementAndConnect(this, value, "source", new IBackPointer < IActivityNode > ()
      {
         public void execute(IActivityNode obj)
         {
            obj.addOutgoingEdge(ActivityEdge.this);
         }
      }, new IBackPointer < IActivityNode > ()
      {
         public void execute(IActivityNode obj)
         {
            obj.removeOutgoingEdge(ActivityEdge.this);
         }
      });
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#setTarget(org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode)
    */
   public void setTarget(IActivityNode value)
   {
      new ElementConnector < IActivityEdge > ().setSingleElementAndConnect(this, value, "target", new IBackPointer < IActivityNode > ()
      {
         public void execute(IActivityNode obj)
         {
            obj.addIncomingEdge(ActivityEdge.this);
         }
      }, new IBackPointer < IActivityNode > ()
      {
         public void execute(IActivityNode obj)
         {
            obj.removeIncomingEdge(ActivityEdge.this);
         }
      });
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge#setWeight(org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification)
    */
   public void setWeight(IValueSpecification value)
   {
      addChild("UML:ActivityEdge.weight", "UML:ActivityEdge.weight", value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onPreBodyModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, java.lang.String)
    */
   public boolean onPreBodyModified(IExpression exp, String proposedValue)
   {
      IActivityEventDispatcher disp = (new DispatchHelper()).getActivitiesDispatcher(); //new ActivityEventDispatcher();
      return (disp != null)?disp.firePreGuardModified(this, proposedValue, disp.createPayload("PreGuardModified")):false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onBodyModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression)
    */
   public void onBodyModified(IExpression exp)
   {
      IActivityEventDispatcher disp = (new DispatchHelper()).getActivitiesDispatcher(); //new ActivityEventDispatcher();
      if (disp != null){
			disp.fireGuardModified(this, disp.createPayload("GuardModified"));
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onPreLanguageModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, java.lang.String)
    */
   public boolean onPreLanguageModified(IExpression exp, String proposedValue)
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onLanguageModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression)
    */
   public void onLanguageModified(IExpression exp)
   {
   }
}
