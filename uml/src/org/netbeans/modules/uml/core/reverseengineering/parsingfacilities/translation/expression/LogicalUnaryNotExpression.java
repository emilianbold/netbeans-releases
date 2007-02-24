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



package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * The LogicalUnaryNotExpression is used to represent a logical expression.
 *
 * <code>Example: -1</code>
 * @author Trey Spiva
 */
public class LogicalUnaryNotExpression extends PreUnaryExpression
{

   /**
    *
    */
   public LogicalUnaryNotExpression()
   {
      super();
   }

   /**
    * Converts the expression data into a string representation.  The operator 
    * will be placed before the expression.
    */
   public String toString()
   {
      String retVal = "";
      
      ITokenDescriptor operator = getOperatorToken();
      if(operator != null)
      {
         String value = operator.getValue();
         if(value.length() > 0)
         {
            retVal += value;
            retVal += " ";
         }
      }
      
      int max = getExpressionCount();
      for(int index = 0; index < max; index++)
      {
         IExpressionProxy proxy = getExpression(index);
         if(proxy != null)
         {
            retVal += proxy.toString();
         }
      }
      
      return retVal;
   }
}
