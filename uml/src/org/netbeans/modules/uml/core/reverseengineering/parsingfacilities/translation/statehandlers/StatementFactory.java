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
 * File       : StatementFactory.java
 * Created on : Dec 10, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;

/**
 * @author Aztec
 */
public class StatementFactory
{
    public static MethodDetailStateHandler retrieveStatementHandler(
                                            String stateName,
                                            String    language,
                                            IOpParserOptions pOptions,
                                            SymbolTable      symbolTable)
    {
        MethodDetailStateHandler retVal = null;

        if("Parameter".equals(stateName))
        {
           MethodParameterStateHandler pHandler = new MethodParameterStateHandler(language);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler;
        }
        else if("Conditional".equals(stateName))
        {
           MethodConditionalStateHandler pHandler 
                            = new MethodConditionalStateHandler(language, true);
           pHandler.methodInitialize(pOptions, symbolTable);      

             retVal = pHandler;
        }
        else if("Else Conditional".equals(stateName))
        {
           MethodConditionalStateHandler pHandler 
                        = new ElseConditionalStateHandler(language);
           pHandler.methodInitialize(pOptions, symbolTable);      

             retVal = pHandler;
        }
        else if("Option Conditional".equals(stateName))
        {
           MethodSwitchStateHandler pHandler 
                    = new MethodSwitchStateHandler(language);
           pHandler.methodInitialize(pOptions, symbolTable);      

             retVal = pHandler;
        }
        else if("Option".equals(stateName))
        {
           MethodSwitchStateHandler pHandler 
                        = new MethodSwitchStateHandler(language);
           pHandler.methodInitialize(pOptions, symbolTable);      

             retVal = pHandler;
        }
        else if(("Method Call".equals(stateName))          ||
                ("Constructor Call".equals(stateName))     || 
                ("Super Constructor Call".equals(stateName)))
        {
           //MethodCallStateHandler pHandler = new MethodCallStateHandler(language);
           MethodCallStateHandler pHandler 
                = new MethodCallStateHandler(stateName, language);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler;         
        }
        else if("Assignment Expression".equals(stateName))
        {
           //MethodCallStateHandler pHandler = new MethodCallStateHandler(language);
           AssignmentStateHandler pHandler 
                    = new AssignmentStateHandler(stateName, language);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler;         
        }
        else if(("Plus Expression".equals(stateName))                   ||
                ("Multiply Expression".equals(stateName))               || 
                ("Minus Expression".equals(stateName))                  ||
                ("Divide Expression".equals(stateName))                 ||
                ("Mod Expression".equals(stateName))                    ||
                ("LogicalOR Expression".equals(stateName))              ||
                ("LogicalAND Expression".equals(stateName))             ||
                ("BinaryOR Expression".equals(stateName))               ||
                ("Not Equality Expression".equals(stateName))           ||
                ("Equality Expression".equals(stateName))               ||
                ("LT Relational Expression".equals(stateName))          ||
                ("GT Relational Expression".equals(stateName))          ||
                ("LE Relational Expression".equals(stateName))          ||
                ("GE Relational Expression".equals(stateName))          || 
                ("Type Check Expression".equals(stateName))             ||
                ("ExclusiveOR Expression".equals(stateName))            ||
                ("BinaryAND Expression".equals(stateName))              ||            
                ("Shift Left Expression".equals(stateName))             ||
                ("Right Shift Expression".equals(stateName))            ||
                ("Binary Shift Right Expression".equals(stateName))     ||
                ("Logical Not Unary Expression".equals(stateName))      ||
                ("Decrement Unary Expression".equals(stateName))        ||
                ("Increment Post Unary Expression".equals(stateName))   ||
                ("Increment Unary Expression".equals(stateName))        ||
                ("Decrement Post Unary Expression".equals(stateName))   ||
                ("Binary Not Unary Expression".equals(stateName))       ||
                ("Minus Unary Expression".equals(stateName))            ||
                ("Plus Unary Expression".equals(stateName))             ||

                ("Conditional Expression".equals(stateName))            ||
                ("Plus Assignment Expression".equals(stateName))        ||
                ("Minus Assignment Expression".equals(stateName))       ||
                ("Binary XOR Assignment Expression".equals(stateName))  ||
                ("Multiply Assignment Expression".equals(stateName))    ||
                ("Divide Assignment Expression".equals(stateName))      ||
                ("Mod Assignment Expression".equals(stateName))         ||
                ("Shift Right Assignment Expression".equals(stateName)) ||
                ("Shift Right Assignment Expression".equals(stateName)) ||
                ("Shift Left Assignment Expression".equals(stateName))  ||
                ("Binary And Assignment Expression".equals(stateName))  ||
                ("Binary OR Assignment Expression".equals(stateName))   ||

                ("Pointer Indirection".equals(stateName))               ||
                ("Pointer Access".equals(stateName))                    ||
                ("Address Of".equals(stateName)))
        {
           //MethodCallStateHandler pHandler = new ExpressionStateHandler(language);
           MethodExpressionStateHandler pHandler 
                = new MethodExpressionStateHandler(stateName, language);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler;         
        }
        else if("Variable Definition".equals(stateName))
        {
           MethodDetailStateHandler pHandler = null;
           if("Visual Basic 6".equals(language))
           {
              pHandler = new VBVariableStateHandler(language, false);
           }
           else
           {
              pHandler = new MethodVariableStateHandler(language, false);
           }
           pHandler.methodInitialize(pOptions, symbolTable);
           retVal = pHandler;
        }
        else if("Loop".equals(stateName))
        {
           MethodLoopStateHandler pHandler = new MethodLoopStateHandler(language);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler;
        }
        else if("Return".equals(stateName))
        {
           MethodReturnStateHandler pHandler = new MethodReturnStateHandler(language);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler;
        }
        else if("CriticalSection".equals(stateName))
        {
           MethodCriticalSectionStateHandler pHandler = new MethodCriticalSectionStateHandler(language);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler;      
        }
        else if("Object Destruction".equals(stateName))
        {
           MethodDetroyStateHandler pHandler = new MethodDetroyStateHandler(language);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler;      
        }
        else if(("Break".equals(stateName))    || 
               ("Continue".equals(stateName))  ||
               ("Goto".equals(stateName)) )
        {
           JumpStateHandler pHandler = new JumpStateHandler(language, stateName);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler;      
        }
        else if("RaisedException".equals(stateName))
        {
           MethodRaisedExceptionStateHandler pHandler = new MethodRaisedExceptionStateHandler(language);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler; 
        }
        else if("Exception Processing".equals(stateName))
        {  
           MethodExceptionProcessingStateHandler pHandler = new MethodExceptionProcessingStateHandler(language);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler; 
        }
        else if("With Block".equals(stateName))
        {
           VBMethodWithStatement pHandler = new VBMethodWithStatement(language);
           pHandler.methodInitialize(pOptions, symbolTable);

           retVal = pHandler; 
        }
        return retVal;
    }

}
