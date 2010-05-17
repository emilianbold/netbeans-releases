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

package org.netbeans.modules.uml.codegen.java.merging;


import java.util.List;
import org.dom4j.Element;

import org.dom4j.Node;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IXMLTokenDescriptor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.XMLTokenDescriptor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 *  the utility class serving to hide some details to make
 *  API less dependent on details of DOM Node (or whatever it will, if any,
 *  be changed to later)
 *  representation of element information
 */
public class ElementDescriptor
{
   
   private Node node = null;
   private ETList<ITokenDescriptor> tokenDescriptors;
   
   public ElementDescriptor(Node node)
   {
      this.node = node;
      tokenDescriptors = getTokenDescriptors();
   }
   
   public Node getNode()
   {
      return node;
   }
   
   /**
    * Retrieves the model type of this element
    * @return the type of this element
    */
   public String getModelElemType()
   {
      String elemType = null;
      if ( node != null)
      {
         elemType = node.getName();
      }
      return elemType;
   }
   
   /**
    * Retrieves the model name of this element
    * @return the name of this element
    */
   public String getModelElemName()
   {
      return getModelElemAttribute("name");
   }
   
   /**
    * Retrieves the value of a specified attribute of this element
    * @return the value of a specified attribute
    */
   public String getModelElemAttribute(String attribute)
   {
      String attrVal = null;
      if ( node != null && node.getNodeType() == Node.ELEMENT_NODE)
      {
         attrVal = ((Element)node).attributeValue(attribute);
      }
      return (attrVal != null ? attrVal : "");
   }
   
   /**
    * Retrives the start position
    * @return the start position
    */
   public long getStartPos()
   {
      return getPosition("StartPosition");
   }
   
   /**
    * Retrieves the end position of this element
    * @return the end position
    */
   public long getEndPos()
   {
       long pos = -1;
       if ("EnumerationLiteral".equals(getModelElemType())) 
       {
	   pos = getPosition("Literal Separator");
       }
       else
       {
	   pos = getPosition("EndPosition");
       }
       if (pos == -1) {
	   if (tokenDescriptors == null || tokenDescriptors.size() == 0)
	   {
	       tokenDescriptors = getTokenDescriptors();
	   }
	   if (tokenDescriptors != null)
	   {
	       for(ITokenDescriptor desc : tokenDescriptors)
	       {
		   long length = desc.getLength();
		   long spos = desc.getPosition();
		   if (spos > -1) 
		   {
		       if (length > -1) 
		       {
			   spos += length;
		       }
		       if (spos > pos) 
		       {
			   pos = spos;
		       }
		   }
	       }
	       if (pos > -1) 
	       {
		   pos = pos - 1;
	       }
	   }
       }
       return pos;
   }
   
   /**
    * Retrieves the position of a token descriptor of a specified type.
    * @param type The type of the descriptor.
    * @return The position of the descriptor if found, else <code>-1</code>.
    */
   public long getPosition(String type)
   {
      ITokenDescriptor desc = getTokenDescriptor(type);
      return (desc != null ? desc.getPosition() : -1);
   }
   
   /**
    * Retrieves the value of a token descriptor of a specified type.
    * @param type The type of the descriptor.
    * @return The value of the descriptor if found, else <code>null</code>.
    */
   public String getValue(String type)
   {
      ITokenDescriptor desc = getTokenDescriptor(type);
      return (desc != null ? desc.getValue() : "");
   }
   
   /**
    * Retrieves the line number of a token descriptor of a specified type.
    * @param type The type of the descriptor.
    * @return The line number of the descriptor if found, else <code>-1</code>.
    */
   public int getLine(String type)
   {
      ITokenDescriptor desc = getTokenDescriptor(type);
      return (desc != null ? desc.getLine() : -1);
   }
   
   /**
    * Retrieves the column number of a token descriptor of a specified type.
    * @param type The type of the descriptor.
    * @return The column number of the descriptor if found, else <code>-1</code>.
    */
   public int getColumn(String type)
   {
      ITokenDescriptor desc = getTokenDescriptor(type);
      return (desc != null ? desc.getColumn() : -1);
   }
   
   /**
    * Retrieves the length of a token descriptor of a specified type.
    * @param type The type of the descriptor.
    * @return The length of the descriptor if found, else <code>-1</code>.
    */
   public int getLength(String type)
   {
      ITokenDescriptor desc = getTokenDescriptor(type);
      return (desc != null ? desc.getLength() : 0);
   }
   
   /**
    * Retrieves a token descriptor of a specified type.
    * @param type The type of the descriptor.
    * @return The descriptor or NULL if a token descriptor or the specified
    *             type is not found.
    */
   public ITokenDescriptor getTokenDescriptor(String type)
   {
      if (tokenDescriptors == null || tokenDescriptors.size() == 0)
      {
         tokenDescriptors = getTokenDescriptors();
      }
      if (tokenDescriptors != null)
      {
         int descCount = tokenDescriptors.size();
         for (int i = 0; i < descCount; ++i)
         {
            ITokenDescriptor desc = tokenDescriptors.get(i);
            if (desc == null) continue;
            
            String curType = desc.getType();
            if (curType != null && curType.equals(type)) {
		return desc;
	    }
         }
      }
      return null;
   }
   
   private ETList<ITokenDescriptor> getTokenDescriptors()
   {
      if (node != null)
      {
         Node descriptorNode = node.selectSingleNode("TokenDescriptors");
         if (descriptorNode != null)
         {
            ETList<ITokenDescriptor> descriptors =
                  new ETArrayList<ITokenDescriptor>();
            List descNodes = descriptorNode.selectNodes("TDescriptor");
            for (int i = 0, nc = descNodes.size(); i < nc; ++i)
            {
               IXMLTokenDescriptor desc = new XMLTokenDescriptor();
               desc.setTokenDescriptorNode((Node) descNodes.get(i));
               descriptors.add(desc);
            }
            return descriptors;
         }
      }
      return null;
   }
   

    public List getOwnedElements(boolean notLiterals)
   {
      String query;
      if (notLiterals) 
      {
	  query = "./UML:Element.ownedElement/*";
      } 
      else 
      {
	  query = "./UML:Enumeration.literal/UML:EnumerationLiteral";
      } 
      //ArrayList < Node > retVal = new ArrayList < Node > ();
      List result = node.selectNodes(query);
      
      return result;
   }
   
   public boolean isMarked()
   {
      return true;
   }
   
   public String getIDMarker()
   {
      return null;
   }
   
   // Debug helper
   public String toString() 
   {
      StringBuffer descriptor = new StringBuffer();
      String tokenType = "Name";
      if (this.node != null)
      {
         descriptor.append("\nElem Type: "+this.getModelElemType());
         descriptor.append("\nElem Name: "+this.getModelElemName());
         descriptor.append("\nElem visibility: "+this.getModelElemAttribute("visibility"));
         
         descriptor.append("\nTDesc Name: "+this.getValue(tokenType));
         descriptor.append(" line: "+this.getLine(tokenType));
         descriptor.append(" column: "+this.getColumn(tokenType));
         descriptor.append(" pos: "+this.getPosition(tokenType));
         descriptor.append("\nTDesc StartPosition: "+this.getStartPos());
         descriptor.append("\nTDesc EndPosition: "+this.getEndPos());
      }
      return descriptor.toString();
   }
}
