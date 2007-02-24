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
 * Created on Dec 9, 2003
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;


public interface IExpressionProxy
{
	public ETPairT<InstanceInformation, Node> writeAsXMI(InstanceInformation  pInfo, 
														 Node pParentNode, 
														 SymbolTable symbolTable,
														 IREClass  pThisPtr,
														 IREClassLoader pClassLoader);
	
	public InstanceInformation sendOperationEvents(InstanceInformation 		 pInstance, 
												   IREClass                  pThisPtr,
												   SymbolTable               symbolTable, 
												   IREClassLoader            pClassLoader,
												   IUMLParserEventDispatcher pDispatcher,
												   Node               		 pParentNode );

   public void     processToken(ITokenDescriptor pToken, String language);	   
	public long     getStartPosition() ;
	public long     getEndPosition();
	public long     getStartLine()  ;
	public void     clear() ;

}

