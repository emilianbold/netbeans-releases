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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class ReferenceEvent extends MethodDetailParserData
implements IReferenceEvent
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IReferenceEvent#getParentReference()
     */
   public IReferenceEvent getParentReference()
   {
      Node n = getXMLNode("ReferenceVariable");
      if (n != null)
      {
         IReferenceEvent e = new ReferenceEvent();
         e.setEventData(n);
         return e;
      }
      return null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IReferenceEvent#getFullName()
     */
   public String getFullName()
   {
      IReferenceEvent par = getParentReference();
      String ret = null;
      if (par != null)
      {
         ret = par.getFullName() + "::";
      }
      
      String myName = getName();
      if ((myName == null) || (myName.length() <= 0))
      {
         // We have a static instance.  So, get the name
         // of the type instead.
         myName = getShortTypeName();
      }
      
      if (myName != null)
         ret = ret != null? ret + myName : myName;
         return ret;
   }
   
   /**
    * @return
    */
   private String getShortTypeName()
   {
      String myName = getType();
      if (myName == null) return null;
      
      ETList<String> tokens = StringUtilities.splitOnDelimiter(myName, "::");
      if (tokens.size() > 0)
         return tokens.get( tokens.size() - 1 );
      return null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IReferenceEvent#getREClass()
     */
   public IREClass getREClass()
   {
      Node node = getXMLNode("UML:Class|UML:Interface|UML:Enumeration");
      if (node != null)
      {
         IREClass cl = new REClass();
         cl.setEventData(node);
         return cl;
      }
      return null;
   }
   
   /**
    * Retrieves the name of the classifier that declares the variable.
    *
    * @param pVal [out] The declaring class name.
    */
   public String getDeclaringClassifier()
   {
      String cl =
      XMLManip.getAttributeValue(getEventData(), "declaringClassifier");
      if (cl == null || cl.length() == 0)
         cl = XMLManip.getAttributeValue(getEventData(), "type");
      return cl;
   }
   
   /**
    * Retrieves the variables declared type.
    *
    * @param pVal [out] The variables type.
    */
   public String getType()
   {
      return XMLManip.getAttributeValue(getEventData(), "type");
   }
   
   /**
    * Retrieves the name of the variable.
    *
    * @param pVal [out] The name of the variable.
    */
   public String getName()
   {
      return XMLManip.getAttributeValue(getEventData(), "name");
   }
   
}
