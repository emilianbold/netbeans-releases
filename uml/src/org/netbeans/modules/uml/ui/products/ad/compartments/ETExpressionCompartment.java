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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.Color;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;

/**
 * @author KevinM
 *
 */
public class ETExpressionCompartment extends ETNameCompartment implements IADExpressionCompartment, IADEditableCompartment
{

   public ETExpressionCompartment()
   {
      super();
		this.init();
   }

   public ETExpressionCompartment(IDrawEngine pDrawEngine)
   {
      super(pDrawEngine);
		this.init();
   }

	private void init() {
		this.m_singleClickSelect = false;
		this.initResources();
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentID()
    */
   public String getCompartmentID()
   {
      return "ADExpressionCompartment";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#addModelElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, int)
    */
   public void addModelElement(IElement pElement, int nIndex)
   {
      try
      {
         super.addModelElement(pElement, nIndex);

         // if no translator or translator returned nothing, try the expression
         if (getName().length() == 0 || getName().equals("[]"))
         {
            String sName;
            IExpression pExpression = pElement instanceof IExpression ? (IExpression)pElement : null;
            if (pExpression != null)
            {
               sName = pExpression.getBody();
               setName(sName != null && sName.length() > 0 ? sName : "[]");
            }
         }

         updateAbstractStatic();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#clone(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine, org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment)
    */
   public void clone(IDrawEngine pParentDrawEngine, ICompartment pRetCompartment)
   {
      try
      {
         IADExpressionCompartment pNewCompartment = (IADExpressionCompartment)this.clone();

         if (pNewCompartment != null)
            pNewCompartment.setEngine(pParentDrawEngine);

         pRetCompartment = pNewCompartment;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /// helper to set the static/abstract members for display
   protected void updateAbstractStatic()
   {
      try
      {
         boolean bStatic = false;
         boolean bAbstract = false;

         IElement pModelElement = getModelElement();

         if (pModelElement != null)
         {
            IClassifier pClassifier = pModelElement instanceof IClassifier ? (IClassifier)pModelElement : null;
            if (pClassifier != null)
            {
               bAbstract = pClassifier.getIsAbstract();
            }
            else
            {
               IBehavioralFeature pFeature = pModelElement instanceof IBehavioralFeature ? (IBehavioralFeature)pModelElement : null;
               if (pFeature != null)
               {
                  bAbstract = pFeature.getIsAbstract();
               }
            }

            IFeature pFeature = pModelElement instanceof IFeature ? (IFeature)pModelElement : null;
            if (pFeature != null)
            {
               bStatic = pFeature.getIsStatic();
            }
         }

         if (m_bIsStatic != (bStatic == true))
         {
            m_bIsStatic = bStatic == true;
            setIsDirty();
         }
         if (m_bIsAbstract != (bAbstract == true))
         {
            m_bIsAbstract = bAbstract == true;
            setIsDirty();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#initResources()
    */
   public void initResources()
   {
      if (m_name == null)
      {
         this.setName(PreferenceAccessor.instance().getDefaultElementName());
      }
      setResourceID("expression", Color.BLACK);

      super.initResources();
   }

}
