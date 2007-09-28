/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
