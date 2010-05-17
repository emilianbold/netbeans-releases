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
