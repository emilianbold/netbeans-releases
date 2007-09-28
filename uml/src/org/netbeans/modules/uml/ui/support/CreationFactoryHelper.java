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



package org.netbeans.modules.uml.ui.support;

import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.relationshipVerification.IEdgeVerification;
import org.netbeans.modules.uml.ui.support.relationshipVerification.INodeVerification;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;

public class CreationFactoryHelper
{
   /**
    * @return
    */
   public static IPresentationTypesMgr getPresentationTypesMgr()
   {   
       ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
       if(factory != null)
       {
          Object value = factory.retrieveEmptyMetaType("PresentationTypes", 
                                                       "PresentationTypesMgr", 
                                                       null);
      
          if (value instanceof IPresentationTypesMgr)
          {
              return (IPresentationTypesMgr)value;
          }
       }
   
       return null;
   }
   
   /**
    * Creates the appropriate NodeVerification
    *
    * @return The returned, created edge verification created through the 
    *         factory
    */
   public static INodeVerification getNodeVerification()
   {
      ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
      if(factory != null)
      {
         Object value = factory.retrieveEmptyMetaType("RelationshipVerification", 
                                                      "NodeVerification", 
                                                      null);
         
         if (value instanceof INodeVerification)
         {
            return (INodeVerification)value;
         }
      }
      
      return null;
   }
   
   /**
     * Creates the appropriate NodeVerification
     *
     * @return The returned, created edge verification created through the 
     *         factory
     */
    public static IEdgeVerification getEdgeVerification()
    {
       ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
       if(factory != null)
       {
          Object value = factory.retrieveEmptyMetaType("RelationshipVerification", 
                                                       "EdgeVerification", 
                                                       null);
         
          if (value instanceof IEdgeVerification)
          {
             return (IEdgeVerification)value;
          }
       }
      
       return null;
    }
    
   /**
    * Retrieve the type specified by the type name, e.g. Class. The type has been 
    * fully prepared and initialize
    * 
    * @param typename The name of the type to retrieve.
    */
   public static Object retrieveMetaType(String typename)
   { 
       IProduct product = ProductHelper.getProduct();
      
      if(product != null)
      {
         ICreationFactory factory = product.getCreationFactory(); 
         return factory.retrieveMetaType(typename, null);
      }
      
      return null;
   }
   
   /**
    * Retrieve the type specified by the type name in the hive specified by 
    * hiveName
    * 
    * @param hiveName The hive used to determine the object to create.
    * @param typename The name of the type to retrieve.
    */
   public static Object retrieveMetaType(String hiveName, String typename)
   { 
      IProduct product = ProductHelper.getProduct();
   
      if(product != null)
      {
         ICreationFactory factory = product.getCreationFactory(); 
         return factory.retrieveEmptyMetaType(hiveName, typename, null);
      }
   
      return null;
   }

   /**
    * @param sCompartmentID
    * @return
    */
   public static ICompartment createCompartment(String sCompartmentID)
   {
       ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
       if(factory != null)
       {
          Object value = factory.retrieveEmptyMetaType("Compartments", 
                                                       sCompartmentID, 
                                                       null);
         
          if (value instanceof ICompartment)
          {
            return (ICompartment)value;
          }
       }
      
       return null;
   }
}
