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


/*
 * Created on Apr 16, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.PrimitiveInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

import org.dom4j.Node;
/**
 * @author avaneeshj
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ArrayIndexExpression extends ArrayDeclartorExpression 
{
    public ArrayIndexExpression()
    {
    	super();
    }

   
    /**
     * Sends out the UMLParser operaiton details events that represent the 
     * expression data.  
     *
     * @param pInfo [in] The instance information context.
     * @param symbolTable [in] The symbol table to use for lookups.
     * @param pClassLoader [in] The classloader to use when searching for 
     *                          class definitions.
     * @param pDispatcher [in] The event dispatcher used to send the events.
     * 
     * @return The instance context.
     */
    public InstanceInformation sendOperationEvents(InstanceInformation pInfo,
                                            IREClass                  pThisPtr,
                                            SymbolTable               symbolTable,       
                                            IREClassLoader            pClassLoader,
                                            IUMLParserEventDispatcher pDispatcher,
                                            Node                      pParentNode)
    {

        InstanceInformation retVal = pInfo;
       
       InstanceInformation arrayInstance = super.sendOperationEvents(pInfo,
                                                                     pThisPtr,
                                                                     symbolTable,
                                                                     pClassLoader,
                                                                     pDispatcher,
                                                                     pParentNode);

       if(arrayInstance != null)
       {
          //retVal = GetArrayInstance(arrayInstance, symbolTable, pParentNode);         

          String name = arrayInstance.getInstanceName() + "[]";      
          retVal = symbolTable.findInstance(name);
          if((retVal == null) && (arrayInstance != null))
          {
             if( arrayInstance != null)
             {
                ObjectInstanceInformation instance = 
                    (arrayInstance instanceof ObjectInstanceInformation) ?
                    (ObjectInstanceInformation)arrayInstance : null;
                //retVal = arrayInstance;
                try
                {
                   retVal = (InstanceInformation)instance.clone();
                }
                catch(CloneNotSupportedException e)
                {
                   // should never get here
                   Log.stackTrace(e);
                   retVal = instance;
                }
             }
             else if(arrayInstance != null)
             {
                PrimitiveInstanceInformation instance = 
                    (arrayInstance instanceof PrimitiveInstanceInformation) ?
                    (PrimitiveInstanceInformation)arrayInstance : null;

                try
                {
                   retVal = (InstanceInformation)instance.clone();
                }
                catch(CloneNotSupportedException e)
                {
                   // should never get here
                   Log.stackTrace(e);
                   retVal = instance;
                }
             }

             if(retVal != null)
             {
                retVal.setInstanceName(name);
                symbolTable.addInstance(retVal, true);
                retVal.setHasBeenReferenced(false);
                retVal.sendReference(pParentNode);
             }
          }
       }

       return retVal;
    }
}
