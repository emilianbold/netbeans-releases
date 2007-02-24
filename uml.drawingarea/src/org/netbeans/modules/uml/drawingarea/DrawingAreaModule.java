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
 * DrawingAreaModule.java
 *
 * Created on April 1, 2005, 4:23 PM
 */

package org.netbeans.modules.uml.drawingarea;

import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.IProductDescriptor;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

import org.openide.modules.ModuleInstall;

/**
 *
 * @author Administrator
 */
public class DrawingAreaModule extends ModuleInstall
{
   
   /** Creates a new instance of DrawingAreaModule */
   public DrawingAreaModule()
   {
   }
 
   public void restored()
   {
      IADProduct product = getProduct();
      //System.out.println("The projecdt = " + product);
      if(product != null)
      {         
         product.setDiagramManager(new UMLDiagramManager());
      }    
   }
   
   //////////////////////////////////////////////////////////////////
   // Helper Methods
   
   protected IADProduct getProduct()
   {
      IADProduct retVal = null;
      
      ICoreProductManager productManager = CoreProductManager.instance();
      ETList<IProductDescriptor> pDesc = productManager.getProducts();
      //System.out.println("Number of product descriptors: " + pDesc.size());
      if((pDesc != null) && (pDesc.size() > 0))
      {
         for(IProductDescriptor descriptor : pDesc)
         {
            if(descriptor.getCoreProduct() instanceof IADProduct)
            {
               retVal = (IADProduct)descriptor.getCoreProduct();
               break;
            }
         }
      }
      
      return retVal;
   }
}
