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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author sumitabhk
 *
 */
public class TypedFactoryRetriever < T >
{

//   private static TypedFactoryRetriever m_Instance = null;
//   private FactoryRetriever m_Factory = null;

//   public static TypedFactoryRetriever instance()
//   {
//      if (m_Instance == null)
//      {
//         m_Instance = new TypedFactoryRetriever();
//      }
//      return m_Instance;
//   }

   public TypedFactoryRetriever()
   {
//      establishFactory();
   }

//   protected void establishFactory()
//   {
//      if (m_Factory == null)
//      {
//         m_Factory = FactoryRetriever.instance();
//      }
//   }

   /**
    *
    * Creates a new meta type given the name.
    *
    * @param typeName The name of the type, such as "Class"
    * @return The created type.
    *
    * @return S_OK, else E_INVALIDARG if the FactoryRetriever
    *         could not be established, or E_NOINTERFACE if
    *         the QueryInterface of the returned type from the 
    *         FactoryRetriever failed. If S_OK is returned, you
    *         can be guarenteed that newType is valid.
    * @see FactoryRetriever
    *
    */
   public T createType(String typeName)
   {
      try
      {
         return (T)FactoryRetriever.instance().createType(typeName, null);
      }
      catch(ClassCastException e)
      {
      }
   
      return null;
   }

   /**
    *
    * Creates a new type and populates it with the passed in node.
    *
    * @param typeName The type to create
    * @param node The DOM node to populate it with
    * @return The new object
    *
    * @return HRESULT
    *
    */
   public T createTypeAndFill(String typeName, Node pNode)
   {
      try
      {
         return (T)FactoryRetriever.instance().createTypeAndFill(typeName, pNode);
      }
      catch(ClassCastException e)
      {
      }
    
      return null;
   }

   /**
    * Creates a new type and populates it with the passed in node.
    *
    * @param node[in] The DOM node to populate it with
    * @return The new object
    * @return
    */
   public T createTypeAndFill(org.dom4j.Node pNode)
   {
      return createTypeAndFill(XMLManip.retrieveSimpleName(pNode), pNode);
   }

   /**
    * @param m_Node
    * @return
    */
   public T clone(Node elementToClone)
   {
      try
      {
         return (T)FactoryRetriever.instance().clone(elementToClone);
      }
      catch(ClassCastException e)
      {
      }
      
      return null;
   }

   /**
    * @param importElement
    * @return
    */
   public T clone(IVersionableElement element)
   {
      return clone(element.getNode());
   }

}


