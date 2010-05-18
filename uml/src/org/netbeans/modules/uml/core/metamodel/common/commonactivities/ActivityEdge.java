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

    public ETList<IElement> getRelatedElements()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
