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
 *
 * Created on Jul 3, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.util.ArrayList;

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

/**
 * Used for helping out with the meta model layer (ie getting the ends of a 
 * relationship).
 * 
 * @author Trey Spiva
 */
public class MetaModelHelper
{
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
      
      //**************************************************
      // Binding Code
      //**************************************************
//      else if (Dispatch.isType(relationship, IDerivation.GUID) == true)
//      {
//         IDerivation derivation = new IDerivationProxy((Dispatch)relationship);
//         retVal.setStartElement(getFirstDerivationEnd(derivation));
//         retVal.setEndElement(getSecondDerivationEnd(derivation));
//      }
//      else if (Dispatch.isType(relationship, IComment.GUID) == true)
//      {
//         IComment comment = new ICommentProxy((Dispatch)relationship);
//         retVal.setStartElement(getFirstCommentEnd(comment));
//         retVal.setEndElement(null);
//      }
//      else if (Dispatch.isType(relationship, IGeneralization.GUID) == true)
//      {
//         IGeneralization generalization = new IGeneralizationProxy((Dispatch)relationship);
//         retVal.setStartElement(getFirstGeneralizationEnd(generalization));
//         retVal.setEndElement(getSecondGeneralizationEnd(generalization));
//      } 
//      else if (Dispatch.isType(relationship, IDependency.GUID) == true)
//      {
//         IDependency dependency = new IDependencyProxy((Dispatch)relationship);
//         retVal.setStartElement(getFirstDependencyEnd(dependency));
//         retVal.setEndElement(getSecondDependencyEnd(dependency));
//      }
//      else if (Dispatch.isType(relationship, IMessage.GUID) == true)
//      {
//      
//      }
//      else if (Dispatch.isType(relationship, IAggregation.GUID) == true)
//      {
//         IAggregation aggregation = new IAggregationProxy((Dispatch)relationship);
//         retVal.setStartElement(getFirstAggregationEnd(aggregation));
//         retVal.setEndElement(getSecondAggregationEnd(aggregation));
//      }
//      else if (Dispatch.isType(relationship, IAssociation.GUID) == true)
//      {
//         IAssociation association = new IAssociationProxy((Dispatch)relationship);
//         retVal.setStartElement(getFirstAssociationEnd(association));
//         retVal.setEndElement(getSecondAssociationEnd(association));
//      }
//      else if (Dispatch.isType(relationship, IActivityEdge.GUID) == true)
//      {
//         IActivityEdge activityEdge = new IActivityEdgeProxy((Dispatch)relationship);
//         retVal.setStartElement(getFirstActivityEdgeEnd(activityEdge));
//         retVal.setEndElement(getSecondActivityEdgeEnd(activityEdge));
//      }
//      else if (Dispatch.isType(relationship, IInclude.GUID) == true)
//      {
//         IInclude include = new IIncludeProxy((Dispatch)relationship);
//         retVal.setStartElement(getFirstIncludeEnd(include));
//         retVal.setEndElement(getSecondIncludeEnd(include));
//      }
//      else if (Dispatch.isType(relationship, IExtend.GUID) == true)
//      {
//         IExtend extend = new IExtendProxy((Dispatch)relationship);
//         retVal.setStartElement(getFirstExtendEnd(extend));
//         retVal.setEndElement(getSecondExtendEnd(extend));
//      }
//      else if (Dispatch.isType(relationship, ITransition.GUID) == true)
//      {
//         ITransition transition = new ITransitionProxy((Dispatch)relationship);
//         retVal.setStartElement(getFirstTransitionEnd(transition));
//         retVal.setEndElement(getSecondTransitionEnd(transition));
//      }
//      else if (Dispatch.isType(relationship, IMessageConnector.GUID) == true)
//      {
//         IMessageConnector connector = new IMessageConnectorProxy((Dispatch)relationship);
//         retVal.setStartElement(getFirstMessageConnectorEnd(connector));
//         retVal.setEndElement(getSecondMessageConnectorEnd(connector));
//      }
//      if (relationship instanceof IDerivation)
//      {
//         IDerivation derivation = (IDerivation)relationship;
//         addDerivationEnds(derivation, retVal);
//      }
//      else if (relationship instanceof IComment)
//      {
//         IComment comment = (IComment)relationship;
//         addCommentEnds(comment, retVal);
//      }
//      else if (relationship instanceof IGeneralization)
//      {
//         IGeneralization generalization = (IGeneralization)relationship;
//         addGeneralizationEnds(generalization, retVal);
//      } 
//      else if (relationship instanceof IDependency)
//      {
//         IDependency dependency = (IDependency)relationship;
//         addDependencyEnds(dependency, retVal);
//      }
//      else if (relationship instanceof IMessage)
//      {
//         
//      }
//      else if (relationship instanceof IAggregation)
//      {
//         IAggregation aggregation = (IAggregation)relationship;
//         addAggregationEnds(aggregation, retVal);
//      }
//      else if (relationship instanceof IAssociation)
//      {
//         IAssociation association = (IAssociation)relationship;
//         addAssociationEnds(association, retVal);
//      }
//      else if (relationship instanceof IActivityEdge)
//      {
//         IActivityEdge activityEdge = (IActivityEdge)relationship;
//         addActivityEdgeEnds(activityEdge, retVal);
//      }
//      else if (relationship instanceof IInclude)
//      {
//         IInclude include = (IInclude)relationship;
//         addIncludeEnds(include, retVal);
//      }
//      else if (relationship instanceof IExtend)
//      {
//         IExtend extend = (IExtend)relationship;
//         addExtendEnds(extend, retVal);
//      }
//      else if (relationship instanceof ITransition)
//      {
//         ITransition transition = (ITransition)relationship;
//         addTransitionEnds(transition, retVal);
//      }
//      else if (relationship instanceof IMessageConnector)
//      {
//         IMessageConnector connector = (IMessageConnector)relationship;
//         addMessageConnectorEnds(connector, retVal);
//      }
      
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
   
      //**************************************************
      // Binding Code 
      //**************************************************
      
//      else if (Dispatch.isType(relationship, IDerivation.GUID) == true)
//      {
//         IDerivation derivation = new IDerivationProxy((Dispatch)relationship);
//         retVal = getFirstDerivationEnd(derivation);
//      }
//      else if (Dispatch.isType(relationship, IComment.GUID) == true)
//      {
//         IComment comment = new ICommentProxy((Dispatch)relationship);
//         retVal = getFirstCommentEnd(comment);
//      }
//      else if (Dispatch.isType(relationship, IGeneralization.GUID) == true)
//      {
//         IGeneralization generalization = new IGeneralizationProxy((Dispatch)relationship);
//         retVal = getFirstGeneralizationEnd(generalization);
//      } 
//      else if (Dispatch.isType(relationship, IDependency.GUID) == true)
//      {
//         IDependency dependency = new IDependencyProxy((Dispatch)relationship);
//         retVal = getFirstDependencyEnd(dependency);
//      }
//      else if (Dispatch.isType(relationship, IMessage.GUID) == true)
//      {
//      
//      }
//      else if (Dispatch.isType(relationship, IAggregation.GUID) == true)
//      {
//         IAggregation aggregation = new IAggregationProxy((Dispatch)relationship);
//         retVal = getFirstAggregationEnd(aggregation);
//      }
//      else if (Dispatch.isType(relationship, IAssociation.GUID) == true)
//      {
//         IAssociation association = new IAssociationProxy((Dispatch)relationship);
//         retVal = getFirstAssociationEnd(association);
//      }
//      else if (Dispatch.isType(relationship, IActivityEdge.GUID) == true)
//      {
//         IActivityEdge activityEdge = new IActivityEdgeProxy((Dispatch)relationship);
//         retVal = getFirstActivityEdgeEnd(activityEdge);
//      }
//      else if (Dispatch.isType(relationship, IInclude.GUID) == true)
//      {
//         IInclude include = new IIncludeProxy((Dispatch)relationship);
//         retVal = getFirstIncludeEnd(include);
//      }
//      else if (Dispatch.isType(relationship, IExtend.GUID) == true)
//      {
//         IExtend extend = new IExtendProxy((Dispatch)relationship);
//         retVal = getFirstExtendEnd(extend);
//      }
//      else if (Dispatch.isType(relationship, ITransition.GUID) == true)
//      {
//         ITransition transition = new ITransitionProxy((Dispatch)relationship);
//         retVal = getFirstTransitionEnd(transition);
//      }
//      else if (Dispatch.isType(relationship, IMessageConnector.GUID) == true)
//      {
//         IMessageConnector connector = new IMessageConnectorProxy((Dispatch)relationship);
//         retVal = getFirstMessageConnectorEnd(connector);
//      }
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
      
      //**************************************************
      // Binding Code 
      //**************************************************
          
//      else if (Dispatch.isType(relationship, IDerivation.GUID) == true)
//      {
//         IDerivation derivation = new IDerivationProxy((Dispatch)relationship);
//         retVal = getSecondDerivationEnd(derivation);
//      }
//      else if (Dispatch.isType(relationship, IComment.GUID) == true)
//      {
//         retVal = null;
//      }
//      else if (Dispatch.isType(relationship, IGeneralization.GUID) == true)
//      {
//         IGeneralization generalization = new IGeneralizationProxy((Dispatch)relationship);
//         retVal = getSecondGeneralizationEnd(generalization);
//      } 
//      else if (Dispatch.isType(relationship, IDependency.GUID) == true)
//      {
//         IDependency dependency = new IDependencyProxy((Dispatch)relationship);
//         retVal = getSecondDependencyEnd(dependency);
//      }
//      else if (Dispatch.isType(relationship, IMessage.GUID) == true)
//      {
//      
//      }
//      else if (Dispatch.isType(relationship, IAggregation.GUID) == true)
//      {
//         IAggregation aggregation = new IAggregationProxy((Dispatch)relationship);
//         retVal = getSecondAggregationEnd(aggregation);
//      }
//      else if (Dispatch.isType(relationship, IAssociation.GUID) == true)
//      {
//         IAssociation association = new IAssociationProxy((Dispatch)relationship);
//         retVal = getSecondAssociationEnd(association);
//      }
//      else if (Dispatch.isType(relationship, IActivityEdge.GUID) == true)
//      {
//         IActivityEdge activityEdge = new IActivityEdgeProxy((Dispatch)relationship);
//         retVal = getSecondActivityEdgeEnd(activityEdge);
//      }
//      else if (Dispatch.isType(relationship, IInclude.GUID) == true)
//      {
//         IInclude include = new IIncludeProxy((Dispatch)relationship);
//         retVal = getSecondIncludeEnd(include);
//      }
//      else if (Dispatch.isType(relationship, IExtend.GUID) == true)
//      {
//         IExtend extend = new IExtendProxy((Dispatch)relationship);
//         retVal = getSecondExtendEnd(extend);
//      }
//      else if (Dispatch.isType(relationship, ITransition.GUID) == true)
//      {
//         ITransition transition = new ITransitionProxy((Dispatch)relationship);
//         retVal = getSecondTransitionEnd(transition);
//      }
//      else if (Dispatch.isType(relationship, IMessageConnector.GUID) == true)
//      {
//         IMessageConnector connector = new IMessageConnectorProxy((Dispatch)relationship);
//         retVal = getSecondMessageConnectorEnd(connector);
//      }
   
      return retVal;
   }
   
//   protected ILifeline getFirstMessageConnectorEnd(IMessageConnector connector)
//   {
//      return connector.getFromLifeline();
//      ILifeline targetNode = connector.getToLifeline();
//      
//      if((sourceNode != null) && (targetNode != null))
//      {
//         ends.setFirstEnd(sourceNode);
//         ends.setSecondEnd(targetNode);
//      }
//   }
   
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
   
//   protected void addTransitionEnds(ITransition transition, 
//                                    RelationEnds ends)
//   {
//      IStateVertex source = transition.getSource();
//      IStateVertex target = transition.getTarget();
//      
//      if((source != null) && (target != null))
//      {
//         ends.setFirstEnd(source);
//         ends.setSecondEnd(target);
//      }
//   }
//
//   protected void addExtendEnds(IExtend extend, 
//                                RelationEnds ends)
//   {
//      IUseCase base = extend.getBase();
//      IUseCase extension = extend.getExtension();
//      
//      if((base != null) && (extension != null))
//      {
//         ends.setFirstEnd(base);
//         ends.setSecondEnd(extension);
//      }
//   }
//
//   protected void addIncludeEnds(IInclude include, 
//                                 RelationEnds ends)
//   {
//      IUseCase base = include.getBase();
//      IUseCase addition = include.getAddition();
//      
//      if((base != null) && (addition != null))
//      {
//         ends.setFirstEnd(base);
//         ends.setSecondEnd(addition);
//      }
//   }
//
//   protected void addActivityEdgeEnds(IActivityEdge activityEdge, 
//                                      RelationEnds ends)
//   {
//      IActivityNode sourceNode = activityEdge.getSource();
//      IActivityNode targetNode = activityEdge.getTarget();
//      
//      if((sourceNode != null) && (targetNode != null))
//      {
//         ends.setFirstEnd(sourceNode);
//         ends.setSecondEnd(targetNode);
//      }
//   }
//
//   protected void addAssociationEnds(IAssociation association, 
//                                     RelationEnds ends)
//   {
//      IAssociationEnds associationEnds = association.getEnds();
//      if(associationEnds.getCount() == 2)
//      {
//         IClassifier end1Participant = null;
//         if(associationEnds.item(0) != null)
//         {
//            end1Participant = associationEnds.item(0).getParticipant();
//         }
//      
//         IClassifier end2Participant = null;
//         if(associationEnds.item(1) != null)
//         {
//            end2Participant = associationEnds.item(1).getParticipant();
//         }
//         
//         if((end1Participant != null) && (end2Participant != null))
//         {
//            ends.setFirstEnd(end1Participant);
//            ends.setSecondEnd(end2Participant);
//         }
//      }
//      else
//      {
//         assert false : "Cannot handle != 2 ends";
//      }
//   }
//
//   protected void addAggregationEnds(IAggregation aggregation, 
//                                     RelationEnds ends)
//   {
//      IAssociationEnd aggregateEnd = aggregation.getAggregateEnd();
//      IClassifier aggregateParticipant = null;
//      if(aggregateEnd != null)
//      {
//         aggregateParticipant = aggregateEnd.getParticipant();
//      }
//      
//      IAssociationEnd partEnd = aggregation.getPartEnd();
//      IClassifier partParticipant = null;
//      if(partEnd != null)
//      {
//         partParticipant = partEnd.getParticipant();
//      }
//      if((aggregateParticipant != null) && (partParticipant != null))
//      {
//         ends.setFirstEnd(aggregateParticipant);
//         ends.setSecondEnd(partParticipant);
//      }
//   }
//
//   protected void addDependencyEnds(IDependency dependency, 
//                                    RelationEnds ends)
//   {
//      if (dependency instanceof IImplementation)
//      {
//         IImplementation implementation = (IImplementation)dependency;
//         IClassifier implementing = implementation.getImplementingClassifier();
//         IInterface  contract     = implementation.getContract();
//         
//         if((implementing != null) && (contract != null))
//         {
//            ends.setFirstEnd(implementing);
//            ends.setSecondEnd(contract);
//         }            
//      }
//      else
//      {
//         // All the rest - IDependency, IAbstraction, IUsage, IPermission, 
//         // IRealization
//         INamedElement client   = dependency.getClient();
//         INamedElement supplier = dependency.getSupplier();
//      
//         if((client != null) && (supplier != null))
//         {
//            ends.setFirstEnd(client);
//            ends.setSecondEnd(supplier);
//         }            
//      }
//   }
//
//   protected void addGeneralizationEnds(IGeneralization generalization,
//                                        RelationEnds ends)
//   {
//      IClassifier specific = generalization.getSpecific();
//      IClassifier general  = generalization.getGeneral();
//      
//      if((specific != null) && (general != null))
//      {
//         ends.setFirstEnd(specific);
//         ends.setSecondEnd(general);
//      }
//   }
//
//   protected void addCommentEnds(IComment comment, 
//                                 RelationEnds ends)
//   {
//      INamedElements annotatedElements = comment.getAnnotatedElements();
//      
//      if(annotatedElements != null)
//      {
//         if(annotatedElements.getCount() > 0)
//         {
//            ends.setFirstEnd(annotatedElements.item(0));
//         }
//      }
//   }
//   
//   protected void addDerivationEnds(IDerivation relationship, 
//                                    RelationEnds ends)
//   {
//      
//      
//      IClassifier derivedClassifier = relationship.getDerivedClassifier();
//      IClassifier template = relationship.getTemplate();
//      if((derivedClassifier != null) && (template != null))
//      {
//         ends.setFirstEnd(derivedClassifier);
//         ends.setSecondEnd(template);
//      }
//   }
   
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
