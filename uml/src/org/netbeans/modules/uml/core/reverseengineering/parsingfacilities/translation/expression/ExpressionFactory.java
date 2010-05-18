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
 * File       : ExpressionFactory.java
 * Created on : Dec 9, 2003
 * Author     : Aztec
 */

package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ElseConditionalStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.MethodConditionalStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.MethodVariableStateHandler;

public class ExpressionFactory
{

   public ExpressionFactory()
   {

   }

   public static ExpressionStateHandler getExpressionForState(String stateName, String language)
   {
      ExpressionStateHandler retVal = null;

      if ("Object Creation".equals(stateName))
      {
         retVal = new ObjectCreationExpression();
      }
      else if ("Array Declarator".equals(stateName))
      {
         retVal = new ArrayDeclartorExpression();
      }
      else if ("Array Initializer".equals(stateName))
      {
         retVal = new ArrayInitializerExpression();
      }
      else if (
         ("Plus Expression".equals(stateName))
            || ("Multiply Expression".equals(stateName))
            || ("Minus Expression".equals(stateName))
            || ("Divide Expression".equals(stateName))
            || ("Mod Expression".equals(stateName)))
      {
         retVal = new MathBinaryExpression();
      }
      else if (
         ("LogicalOR Expression".equals(stateName))
            || ("LogicalAND Expression".equals(stateName))
            || ("BinaryOR Expression".equals(stateName))
            || ("Not Equality Expression".equals(stateName))
            || ("Equality Expression".equals(stateName))
            || ("LT Relational Expression".equals(stateName))
            || ("GT Relational Expression".equals(stateName))
            || ("LE Relational Expression".equals(stateName))
            || ("GE Relational Expression".equals(stateName))
            || ("Type Check Expression".equals(stateName))
            || ("Equivalence Expression".equals(stateName))
            || ("Implication Expression".equals(stateName))
            || ("Exponentiation Expression".equals(stateName)))
      {
         // Boolean Operators
         retVal = new BinaryExpression();
      }
      else if (
         ("ExclusiveOR Expression".equals(stateName))
            || ("BinaryAND Expression".equals(stateName))
            || ("Shift Left Expression".equals(stateName))
            || ("Right Shift Expression".equals(stateName))
            || ("Range Expression".equals(stateName))
            || ("Binary Expression".equals(stateName))
            || ("Binary Shift Right Expression".equals(stateName)))
      {
         // Bitwise Operators
         retVal = new BinaryExpression();
      }
      else if ("Conditional Expression".equals(stateName))
      {
          
          //kris richards - should never get here.
          // It is now assumed that the "Conditional Expression" state
          // will occur as a substate of the MethodVariableStateHandler. Therefore
          // the state is trap in the MethodVariableStateHandler.createSubStateHandler 
          // which in turn instantiates a MethodConditionalStateHandler instead of a 
          // ConditionalExpression. Essentially we are making the trinary ('?') operator
          // look like a basic if-else statement for SQD-REOperation.
         
      }      
      else if ("Method Call".equals(stateName))
      {
         retVal = new MethodCallExpression();
      }
      else if ("Constructor Call".equals(stateName))
      {
         retVal = new ConstructorCallExpression();
      }
      else if ("Super Constructor Call".equals(stateName))
      {
         retVal = new SuperConstructorCallExpression();
      }
      else if ("Identifier".equals(stateName))
      {
         retVal = new IdentifierExpression();
      }
      else if ("Type Cast".equals(stateName))
      {
         retVal = new TypeCastExpression();
      }
      else if ("Array Index".equals(stateName))
      {
         retVal = new ArrayIndexExpression();
      }
      else if ("Variable Definition".equals(stateName) ||
               "Parameter".equals(stateName))
      {
         retVal = new MethodVariableStateHandler(language, true);
      }
      else if(("Increment Post Unary Expression".equals(stateName)) ||
      		("Decrement Post Unary Expression".equals(stateName)))
      {
         retVal = new PostUnaryExpression();
      }
      else if(("Decrement Unary Expression".equals(stateName))   ||
            ("Increment Unary Expression".equals(stateName))     ||
            ("Binary Not Unary Expression".equals(stateName))    ||
            ("Minus Unary Expression".equals(stateName))         ||
            ("Unary Expression".equals(stateName))               ||
            ("Plus Unary Expression".equals(stateName)))
      {
         retVal = new PreUnaryExpression();
      }
      else if("Logical Not Unary Expression".equals(stateName))
      {
         retVal = new LogicalUnaryNotExpression();
      }
      else if (
         ("Assignment Expression".equals(stateName))
            || ("Plus Assignment Expression".equals(stateName))
            || ("Minus Assignment Expression".equals(stateName))
            || ("Binary XOR Assignment Expression".equals(stateName))
            || ("Multiply Assignment Expression".equals(stateName))
            || ("Divide Assignment Expression".equals(stateName))
            || ("Mod Assignment Expression".equals(stateName))
            || ("Shift Right Assignment Expression".equals(stateName))
            || ("Shift Right Assignment Expression".equals(stateName))
            || ("Shift Left Assignment Expression".equals(stateName))
            || ("Binary And Assignment Expression".equals(stateName))
            || ("Binary OR Assignment Expression".equals(stateName)))
      {
         retVal = new AssignmentExpression();
      }
      else if ("Expression List".equals(stateName))
      {
         retVal = new ExpressionStateHandler();
      }
      return retVal;
   }

}
