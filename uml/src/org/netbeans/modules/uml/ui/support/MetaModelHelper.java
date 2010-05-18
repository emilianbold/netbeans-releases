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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.ui.support;

import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class MetaModelHelper {
    public RelationEnds getRelationshipEnds(IElement relationship)
   {
      RelationEnds retVal = new RelationEnds();
      
      if (relationship instanceof IDerivation)
      {
         IDerivation derivation = (IDerivation)relationship;
         retVal.setStartElement(getFirstDerivationEnd(derivation));
         retVal.setEndElement(getSecondDerivationEnd(derivation));
      }
      else if (relationship instanceof IComment)
      {
         IComment comment = (IComment)relationship;
         retVal.setStartElement(getFirstCommentEnd(comment));
         retVal.setEndElement(null);
      }
      else if (relationship instanceof IGeneralization)
      {
         IGeneralization generalization = (IGeneralization)relationship;
         retVal.setStartElement(getFirstGeneralizationEnd(generalization));
         retVal.setEndElement(getSecondGeneralizationEnd(generalization));
      } 
      else if (relationship instanceof IDependency)
      {
         IDependency dependency = (IDependency)relationship;
         retVal.setStartElement(getFirstDependencyEnd(dependency));
         retVal.setEndElement(getSecondDependencyEnd(dependency));
      }
      else if (relationship instanceof IMessage)
      {

      }
      else if (relationship instanceof IAggregation)
      {
         IAggregation aggregation = (IAggregation)relationship;
         retVal.setStartElement(getFirstAggregationEnd(aggregation));
         retVal.setEndElement(getSecondAggregationEnd(aggregation));
      }
      else if (relationship instanceof IAssociation)
      {
         IAssociation association = (IAssociation)relationship;
         retVal.setStartElement(getFirstAssociationEnd(association));
         retVal.setEndElement(getSecondAssociationEnd(association));
      }
      else if (relationship instanceof IActivityEdge)
      {
         IActivityEdge activityEdge = (IActivityEdge)relationship;
         retVal.setStartElement(getFirstActivityEdgeEnd(activityEdge));
         retVal.setEndElement(getSecondActivityEdgeEnd(activityEdge));
      }
      else if (relationship instanceof IInclude)
      {
         IInclude include = (IInclude)relationship;
         retVal.setStartElement(getFirstIncludeEnd(include));
         retVal.setEndElement(getSecondIncludeEnd(include));
      }
      else if (relationship instanceof IExtend)
      {
         IExtend extend = (IExtend)relationship;
         retVal.setStartElement(getFirstExtendEnd(extend));
         retVal.setEndElement(getSecondExtendEnd(extend));
      }
      else if (relationship instanceof ITransition)
      {
         ITransition transition = (ITransition)relationship;
         retVal.setStartElement(getFirstTransitionEnd(transition));
         retVal.setEndElement(getSecondTransitionEnd(transition));
      }
      else if (relationship instanceof IMessageConnector)
      {
         IMessageConnector connector = (IMessageConnector)relationship;
         retVal.setStartElement(getFirstMessageConnectorEnd(connector));
         retVal.setEndElement(getSecondMessageConnectorEnd(connector));
      }
      return retVal;
   }

   public IElement getFirstRelationshipEnd(IElement relationship)
   {
      IElement retVal = null;
   
      if (relationship instanceof IDerivation)
      {
         IDerivation derivation = (IDerivation)relationship;
         retVal = getFirstDerivationEnd(derivation);
      }
      else if (relationship instanceof IComment)
      {
         IComment comment = (IComment)relationship;
         retVal = getFirstCommentEnd(comment);
      }
      else if (relationship instanceof IGeneralization)
      {
         IGeneralization generalization = (IGeneralization)relationship;
         retVal = getFirstGeneralizationEnd(generalization);
      } 
      else if (relationship instanceof IDependency)
      {
         IDependency dependency = (IDependency)relationship;
         retVal = getFirstDependencyEnd(dependency);
      }
      else if (relationship instanceof IMessage)
      {
      
      }
      else if (relationship instanceof IAggregation)
      {
         IAggregation aggregation = (IAggregation)relationship;
         retVal = getFirstAggregationEnd(aggregation);
      }
      else if (relationship instanceof IAssociation)
      {
         IAssociation association = (IAssociation)relationship;
         retVal = getFirstAssociationEnd(association);
      }
      else if (relationship instanceof IActivityEdge)
      {
         IActivityEdge activityEdge = (IActivityEdge)relationship;
         retVal = getFirstActivityEdgeEnd(activityEdge);
      }
      else if (relationship instanceof IInclude)
      {
         IInclude include = (IInclude)relationship;
         retVal = getFirstIncludeEnd(include);
      }
      else if (relationship instanceof IExtend)
      {
         IExtend extend = (IExtend)relationship;
         retVal = getFirstExtendEnd(extend);
      }
      else if (relationship instanceof ITransition)
      {
         ITransition transition = (ITransition)relationship;
         retVal = getFirstTransitionEnd(transition);
      }
      else if (relationship instanceof IMessageConnector)
      {
         IMessageConnector connector = (IMessageConnector)relationship;
         retVal = getFirstMessageConnectorEnd(connector);
      }
   
      return retVal;
   }
   
   public IElement getSecondRelationshipEnd(IElement relationship)
   {
      IElement retVal = null;
   
      if (relationship instanceof IDerivation)
      {
         IDerivation derivation = (IDerivation)relationship;
         retVal = getSecondDerivationEnd(derivation);
      }
      else if (relationship instanceof IComment)
      {
         retVal = null;
      }
      else if (relationship instanceof IGeneralization)
      {
         IGeneralization generalization = (IGeneralization)relationship;
         retVal = getSecondGeneralizationEnd(generalization);
      } 
      else if (relationship instanceof IDependency)
      {
         IDependency dependency = (IDependency)relationship;
         retVal = getSecondDependencyEnd(dependency);
      }
      else if (relationship instanceof IMessage)
      {
      
      }
      else if (relationship instanceof IAggregation)
      {
         IAggregation aggregation = (IAggregation)relationship;
         retVal = getSecondAggregationEnd(aggregation);
      }
      else if (relationship instanceof IAssociation)
      {
         IAssociation association = (IAssociation)relationship;
         retVal = getSecondAssociationEnd(association);
      }
      else if (relationship instanceof IActivityEdge)
      {
         IActivityEdge activityEdge = (IActivityEdge)relationship;
         retVal = getSecondActivityEdgeEnd(activityEdge);
      }
      else if (relationship instanceof IInclude)
      {
         IInclude include = (IInclude)relationship;
         retVal = getSecondIncludeEnd(include);
      }
      else if (relationship instanceof IExtend)
      {
         IExtend extend = (IExtend)relationship;
         retVal = getSecondExtendEnd(extend);
      }
      else if (relationship instanceof ITransition)
      {
         ITransition transition = (ITransition)relationship;
         retVal = getSecondTransitionEnd(transition);
      }
      else if (relationship instanceof IMessageConnector)
      {
         IMessageConnector connector = (IMessageConnector)relationship;
         retVal = getSecondMessageConnectorEnd(connector);
      }
      
      return retVal;
   }
   
      protected ILifeline getFirstMessageConnectorEnd(IMessageConnector connector)
   {
      return connector.getFromLifeline();
   }
   
   protected ILifeline getSecondMessageConnectorEnd(IMessageConnector connector)
   {
      return connector.getToLifeline();
   }

   protected IStateVertex getFirstTransitionEnd(ITransition transition)
   {
      return transition.getSource();
   }

   protected IStateVertex getSecondTransitionEnd(ITransition transition)
   {
      return transition.getTarget();
   }
   
   protected IUseCase getFirstExtendEnd(IExtend extend)
   {
      return extend.getBase();
   }

   protected IUseCase getSecondExtendEnd(IExtend extend)
   {
      return extend.getExtension();
   }
   
   protected IUseCase getFirstIncludeEnd(IInclude include)
   {
      return include.getBase();
   }
   
   protected IUseCase getSecondIncludeEnd(IInclude include)
   {
      return include.getAddition();
   }
   
   protected IActivityNode getFirstActivityEdgeEnd(IActivityEdge activityEdge)
   {
      return activityEdge.getSource();
   }

   protected IActivityNode getSecondActivityEdgeEnd(IActivityEdge activityEdge)
   {
      return activityEdge.getTarget();
   }
   
   protected IClassifier getFirstAssociationEnd(IAssociation association)
   {
      IClassifier retVal = null;
      ETList<IAssociationEnd> associationEnds = association.getEnds();
      if(associationEnds.size() == 2)
      {
         if(associationEnds.get(0) != null)
         {
            retVal = associationEnds.get(0).getParticipant();
         }
      }
      else
      {
         assert false : "Cannot handle != 2 ends";
      }
      
      return retVal;
   }

   protected IClassifier getSecondAssociationEnd(IAssociation association)
   {
      IClassifier retVal = null;
      ETList<IAssociationEnd> associationEnds = association.getEnds();
      if(associationEnds.size() == 2)
      {         
         if(associationEnds.get(1) != null)
         {
            retVal = associationEnds.get(1).getParticipant();
         }
      }
      else
      {
         assert false : "Cannot handle != 2 ends";
      }
      return retVal;
   }
      
   protected IClassifier getFirstAggregationEnd(IAggregation aggregation)
   {
      IClassifier retVal = null;
      
      IAssociationEnd aggregateEnd = aggregation.getAggregateEnd();
      IClassifier aggregateParticipant = null;
      if(aggregateEnd != null)
      {
         retVal = aggregateEnd.getParticipant();
      }
      
      return retVal;
   }
   
   protected IClassifier getSecondAggregationEnd(IAggregation aggregation)
   {
      IClassifier retVal = null;
      
      IAssociationEnd partEnd = aggregation.getPartEnd();
      IClassifier partParticipant = null;
      if(partEnd != null)
      {
         retVal = partEnd.getParticipant();
      }
      
      return retVal;
   }
   
   protected IElement getFirstDependencyEnd(IDependency dependency)
   {
      IElement retVal = null;
      
      if (dependency instanceof IImplementation)
      {
         IImplementation implementation = (IImplementation)dependency;
         retVal = implementation.getImplementingClassifier();
      }
      else
      {
         // All the rest - IDependency, IAbstraction, IUsage, IPermission, 
         // IRealization
         retVal = dependency.getClient();
      }
      
      return retVal;
   }

   protected IElement getSecondDependencyEnd(IDependency dependency)
   {
      IElement retVal = null;
      
      if (dependency instanceof IImplementation)
      {
         IImplementation implementation = (IImplementation)dependency;
         retVal = implementation.getContract();
      }
      else
      {
         // All the rest - IDependency, IAbstraction, IUsage, IPermission, 
         // IRealization
         retVal = dependency.getSupplier();            
      }
      
      return retVal;
   }
   
   protected IClassifier getFirstGeneralizationEnd(IGeneralization generalization)
   {
      return generalization.getSpecific();
   }

   protected IClassifier getSecondGeneralizationEnd(IGeneralization generalization)
   {
      return generalization.getGeneral();
   }
   
   protected INamedElement getFirstCommentEnd(IComment comment)
   {
      INamedElement retVal = null;
      
      ETList<INamedElement> annotatedElements = comment.getAnnotatedElements();
      
      if(annotatedElements != null)
      {
         if(annotatedElements.size() > 0)
         {
            retVal = annotatedElements.get(0);
         }
      }
      
      return retVal;
   }
   
   protected IClassifier getFirstDerivationEnd(IDerivation relationship)
   {
      return relationship.getDerivedClassifier();
   }
   
   protected IClassifier getSecondDerivationEnd(IDerivation relationship)
   {
   
   
      return relationship.getTemplate();
   }
   

   /** 
    * RelationEnds is used to specify the ends of a relationship.
    */
   public class RelationEnds
   {
      private IElement m_StartEnd = null;
      private IElement m_EndEnd = null;
      
      public RelationEnds()
      {
      }
      
      public RelationEnds(IElement first, IElement second)
      {
      }
      /**
       * @return
       */
      public IElement getEndElement()
      {
         return m_EndEnd;
      }

      /**
        * @param element
        */
       public void setEndElement(IElement element)
       {
          m_EndEnd = element;
       }
       
      /**
       * @return
       */
      public IElement getStartElement()
      {
         return m_StartEnd;
      }

      /**
       * @param element
       */
      public void setStartElement(IElement element)
      {
         m_StartEnd = element;
      }

   }
}

