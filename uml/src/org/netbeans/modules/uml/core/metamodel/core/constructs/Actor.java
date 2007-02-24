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

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IFlow;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectorEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPart;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.Part;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Classifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class Actor extends Classifier implements IActor
{
   private IPart part = new Part();

   public void establishNodePresence(Document doc, Node node)
   {
      buildNodePresence("UML:Actor", doc, node);
   }

   public IVersionableElement performDuplication()
   {
      IVersionableElement elem = getPart().performDuplication();
      if (elem instanceof IClassifier)
      {
         performDuplicationProcess((IClassifier)elem);
      }
      return elem;
   }

   /**
    * @param pConnector
    */
   public void addEnd(IConnectorEnd pConnector)
   {
      getPart().addEnd(pConnector);
   }

   /**
    * @param pClassifier
    */
   public void addRoleContext(IStructuredClassifier pClassifier)
   {
      getPart().addRoleContext(pClassifier);
   }

   /**
    * @param destination
    */
   public IFeature duplicateToClassifier(IClassifier destination)
   {
      return getPart().duplicateToClassifier(destination);
   }

   /**
    * @return
    */
   public int getClientChangeability()
   {
      return getPart().getClientChangeability();
   }

   /**
    * @return
    */
   public IStructuralFeature getDefiningFeature()
   {
      return getPart().getDefiningFeature();
   }

   /**
    * 
    */
   public ETList < IConnectorEnd > getEnds()
   {
      return getPart().getEnds();
   }

   /**
    * @return
    */
   public IClassifier getFeaturingClassifier()
   {
       return part.getFeaturingClassifier();
   }

   /**
    * @return
    */
   public int getInitialCardinality()
   {
      return getPart().getInitialCardinality();
   }

   /**
    * @return
    */
   public boolean getIsSet()
   {
      return getPart().getIsSet();
   }

   /**
    * @return
    */
   public boolean getIsStatic()
   {
      return getPart().getIsStatic();
   }

   /**
    * @return
    */
   public boolean getIsVolatile()
   {
      return getPart().getIsVolatile();
   }

   /**
    * @return
    */
   public boolean getIsWhole()
   {
      return getPart().getIsWhole();
   }

   /**
    * @return
    */
   public IMultiplicity getMultiplicity()
   {
      return getPart().getMultiplicity();
   }

   /**
    * @return
    */
   public int getOrdering()
   {
      return getPart().getOrdering();
   }

   /**
    * @return
    */
   public int getPartKind()
   {
      return getPart().getPartKind();
   }

   /**
    * 
    */
   public ETList < IStructuredClassifier > getRoleContexts()
   {
      return getPart().getRoleContexts();
   }

   /**
    * @return
    */
   public IClassifier getType()
   {
      return getPart().getType();
   }

   /**
    * @return
    */
   public String getTypeID()
   {
      return getPart().getTypeID();
   }

   /**
    * @return
    */
   public String getTypeName()
   {
      return getPart().getTypeName();
   }

   /**
    * @param destination
    */
   public void moveToClassifier(IClassifier destination)
   {
      getPart().moveToClassifier(destination);
   }

   /**
    * @param pConnector
    */
   public void removeEnd(IConnectorEnd pConnector)
   {
      getPart().removeEnd(pConnector);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeReferencingReference(org.netbeans.modules.uml.core.metamodel.core.foundation.IReference)
    */
   public void removeReferencingReference(IReference ref)
   {
      getPart().removeReferencingReference(ref);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeReferredReference(org.netbeans.modules.uml.core.metamodel.core.foundation.IReference)
    */
   public void removeReferredReference(IReference ref)
   {
      getPart().removeReferredReference(ref);
   }

   /**
    * @param pClassifier
    */
   public void removeRoleContext(IStructuredClassifier pClassifier)
   {
      getPart().removeRoleContext(pClassifier);
   }

   /**
    * @param value
    */
   public void setClientChangeability(int value)
   {
      getPart().setClientChangeability(value);
   }

   /**
    * @param value
    */
   public void setDefiningFeature(IStructuralFeature value)
   {
      getPart().setDefiningFeature(value);
   }

   /**
    * @param value
    */
   public void setFeaturingClassifier(IClassifier value)
   {
      getPart().setFeaturingClassifier(value);
   }

   /**
    * @param value
    */
   public void setInitialCardinality(int value)
   {
      getPart().setInitialCardinality(value);
   }

   /**
    * @param value
    */
   public void setIsSet(boolean value)
   {
      getPart().setIsSet(value);
   }

   /**
    * @param value
    */
   public void setIsStatic(boolean value)
   {
      getPart().setIsStatic(value);
   }

   /**
    * @param value
    */
   public void setIsVolatile(boolean value)
   {
      getPart().setIsVolatile(value);
   }

   /**
    * @param value
    */
   public void setIsWhole(boolean value)
   {
      getPart().setIsWhole(value);
   }

   /**
    * @param value
    */
   public void setMultiplicity(IMultiplicity value)
   {
      getPart().setMultiplicity(value);
   }

   /**
    * @param value
    */
   public void setOrdering(int value)
   {
      getPart().setOrdering(value);
   }

   /**
    * @param value
    */
   public void setPartKind(int value)
   {
      getPart().setPartKind(value);
   }

   /**
    * @param value
    */
   public void setType(IClassifier value)
   {
      getPart().setType(value);
   }

   /**
    * @param value
    */
   public void setType2(String value)
   {
      getPart().setType2(value);
   }

   /**
    * @param value
    */
   public void setTypeName(String value)
   {
      getPart().setTypeName(value);
   }
   
   
   protected IPart getPart()
   {
      if (null == m_part)
      {
         m_part = new Part();
         m_part.setNode(getNode());
      }

      return m_part;
   }

   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreLowerModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String)
	*/
   public boolean onPreLowerModified(IMultiplicity mult, IMultiplicityRange range, String proposedValue) 
   {
	   return getPart().onPreLowerModified(mult, range, proposedValue);
   }

   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onLowerModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	*/
   public void onLowerModified(IMultiplicity mult, IMultiplicityRange range) 
   {
	   getPart().onLowerModified(mult, range);
   }

   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreUpperModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange, java.lang.String)
	*/
   public boolean onPreUpperModified(IMultiplicity mult, IMultiplicityRange range, String proposedValue) 
   {
	   return getPart().onPreUpperModified(mult, range, proposedValue);	
   }

   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onUpperModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	*/
   public void onUpperModified(IMultiplicity mult, IMultiplicityRange range) 
   {
	   getPart().onUpperModified(mult, range);
   }

   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreRangeAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	*/
   public boolean onPreRangeAdded(IMultiplicity mult, IMultiplicityRange range) 
   {
	   return getPart().onPreRangeAdded(mult, range);
   }

   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onRangeAdded(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	*/
   public void onRangeAdded(IMultiplicity mult, IMultiplicityRange range) 
   {
	   getPart().onRangeAdded(mult, range);
   }

   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreRangeRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	*/
   public boolean onPreRangeRemoved(IMultiplicity mult, IMultiplicityRange range) 
   {
	   return getPart().onPreRangeRemoved(mult, range);
   }

   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onRangeRemoved(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange)
	*/
   public void onRangeRemoved(IMultiplicity mult, IMultiplicityRange range) 
   {
	   getPart().onRangeRemoved(mult, range);
   }

   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onPreOrderModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity, boolean)
	*/
   public boolean onPreOrderModified(IMultiplicity mult, boolean proposedValue)
   {
	   return getPart().onPreOrderModified(mult, proposedValue);
   }

   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityListener#onOrderModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity)
	*/
   public void onOrderModified(IMultiplicity mult) 
   {
	   getPart().onOrderModified(mult);
   }			

    public String getRangeAsString()
    {
        return getMultiplicity().getRangeAsString();
    }

   private IPart m_part = null;

}
