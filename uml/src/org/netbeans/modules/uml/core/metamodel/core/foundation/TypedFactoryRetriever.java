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
          e.printStackTrace();
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


