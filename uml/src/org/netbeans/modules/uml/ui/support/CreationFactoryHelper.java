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



package org.netbeans.modules.uml.ui.support;

import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
//import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
//import org.netbeans.modules.uml.ui.support.relationshipVerification.IEdgeVerification;
//import org.netbeans.modules.uml.ui.support.relationshipVerification.INodeVerification;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;

public class CreationFactoryHelper
{ //TODO
   /**
    * @return
    */ //TODO
//   public static IPresentationTypesMgr getPresentationTypesMgr()
//   {   
//       ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
//       if(factory != null)
//       {
//          Object value = factory.retrieveEmptyMetaType("PresentationTypes", 
//                                                       "PresentationTypesMgr", 
//                                                       null);
//      
//          if (value instanceof IPresentationTypesMgr)
//          {
//              return (IPresentationTypesMgr)value;
//          }
//       }
//   
//       return null;
//   }
   
   /**
    * Creates the appropriate NodeVerification
    *
    * @return The returned, created edge verification created through the 
    *         factory
    */
//   public static INodeVerification getNodeVerification()
//   {
//      ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
//      if(factory != null)
//      {
//         Object value = factory.retrieveEmptyMetaType("RelationshipVerification", 
//                                                      "NodeVerification", 
//                                                      null);
//         
//         if (value instanceof INodeVerification)
//         {
//            return (INodeVerification)value;
//         }
//      }
//      
//      return null;
//   }
   
   /**
     * Creates the appropriate NodeVerification
     *
     * @return The returned, created edge verification created through the 
     *         factory
     */
//    public static IEdgeVerification getEdgeVerification()
//    {
//       ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
//       if(factory != null)
//       {
//          Object value = factory.retrieveEmptyMetaType("RelationshipVerification", 
//                                                       "EdgeVerification", 
//                                                       null);
//         
//          if (value instanceof IEdgeVerification)
//          {
//             return (IEdgeVerification)value;
//          }
//       }
//      
//       return null;
//    }
    
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
    */ //TODO
//   public static ICompartment createCompartment(String sCompartmentID)
//   {
//       ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
//       if(factory != null)
//       {
//          Object value = factory.retrieveEmptyMetaType("Compartments", 
//                                                       sCompartmentID, 
//                                                       null);
//         
//          if (value instanceof ICompartment)
//          {
//            return (ICompartment)value;
//          }
//       }
//      
//       return null;
//   }
}
