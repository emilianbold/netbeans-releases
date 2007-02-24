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

package org.netbeans.modules.uml.ui.addins.diagramcreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPart;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.SuperConstructorCallExpression;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author brettb
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ComponentParentFinder
{
   /// The parent Component, the internal Classifier
   public static class ParentChildPair extends ETPairT< IComponent, IClassifier >
   {
      public ParentChildPair( IComponent component, IClassifier classifier )
      {
         super( component, classifier );
      }
   };
   public static class ParentChildCollection extends ArrayList< ParentChildPair > {};
   
   /// Part ID, Owning Component
   public static class PartMap extends HashMap< String, IComponent > {};
   
   /**
    * Attempts to match each classifier passed in with an owning component from the list of components passed in
    * 
    * @param components[in]     The list of components to check against
    * @param classifiers[in]    The list of classifier to check against
    * @return                   The collection of pairs that matches a classifier to its owning component
    */
   public static ParentChildCollection resolveParents( ETList< IComponent > components, ETList< IClassifier > classifiers )
   {
      if( null == components ) throw new IllegalArgumentException();
      if( null == classifiers ) throw new IllegalArgumentException();

      ParentChildCollection collection = new ParentChildCollection();

      PartMap partMap = establishPartMap( components );

      int num = classifiers.getCount();
      for( int x = 0; x < num; x++ )
      {
         IClassifier classifier = classifiers.item( x );
         assert ( classifier != null );

         if( classifier != null )
         {
            String partID = getPartID( classifier );

            if( partID.length() > 0 )
            {
               IComponent component = partMap.get( partID );
               if( component != null )
               {
                  ParentChildPair pair = new ParentChildPair( component, classifier );
                  collection.add( pair );
               }
            }
         }
      }
      
      return collection;
   }
   
   /**
    * Retrieves all the ids of internal components from each of the components passed in
    * and builds a map of the ids to owning component
    * 
    * @param components[in]  The collection of components to process
    * @return                The filled in map after processing
    */
   protected static PartMap establishPartMap( ETList< IComponent > components )
   {
      if( null == components ) throw new IllegalArgumentException();
      
      PartMap partMap = new PartMap();

      int num = components.getCount();
      for( int x = 0; x < num; x++ )
      {
         IComponent comp = components.item( x );
         assert ( comp != null );
         if( comp != null )
         {
            Node node = comp.getNode();
            assert ( node != null );
            if( node != null )
            {
               String internalIDs = XMLManip.getAttributeValue( node, "internalClassifier" );
               if( internalIDs.length() > 0 )
               {
                  ETList< String > tokens = StringUtilities.splitOnDelimiter( internalIDs, " " );
                  for (Iterator iter = tokens.iterator(); iter.hasNext();)
                  {
                     String xmiID = (String)iter.next();
                     
                     if( xmiID.length() > 0 )
                     {
                        partMap.put( xmiID, comp );
                     }
                  }
               }
            }
         }
      }
      
      return partMap;
   }
   
   
   /**
    * Retrieves the ID of the whole part for the passed in classifier
    * 
    * @param classifier[in]  Classifier to retrieve from
    * 
    * @return The ID, else ""
    */

   protected static String getPartID( IClassifier classifier )
   {
      String id = "";
      
      if (classifier instanceof IStructuredClassifier)
      {
         IStructuredClassifier structClass = (IStructuredClassifier)classifier;
         
         ETList< IPart > parts = structClass.getParts();
         if( parts != null )
         {
            int num = parts.getCount();
            for( int x = 0; x < num; x++ )
            {
               IPart part = parts.item( x );
               assert ( part != null );
               if( part != null )
               {
                  boolean isWhole = part.getIsWhole();
                  if( isWhole )
                  {
                     String partID = part.getXMIID();
                     assert ( partID.length() > 0 );
                     if( partID.length() > 0 )
                     {
                        id = partID;
                        break;
                     }
                  }
               }
            }
         }
      }

      return id;
   }
}
