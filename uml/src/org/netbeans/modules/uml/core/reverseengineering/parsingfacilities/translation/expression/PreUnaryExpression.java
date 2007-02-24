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

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers.ExpressionStateHandler;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * The PreUnaryExpression is used to represent a unary expression in where the
 * operator is before the expression.
 *
 * <code>Example: -1</code>
 *
 * @author Trey Spiva
 */
public class PreUnaryExpression extends PostUnaryExpression
{
    private ITokenDescriptor  m_pPrecedenceStart     = null;
    private ITokenDescriptor  m_pPrecedenceEnd 	     = null;
    
   /**
    * Creates a PreUnaryExpression object.
    */
   public PreUnaryExpression()
   {
      super();
   }
   
   public void clear()
   {
       super.clear();
       
       m_pPrecedenceStart     = null;
       m_pPrecedenceEnd       = null;
   }

   /* 
    */
   public long getEndPosition()
   {
       long retVal = -1;
       if(m_pPrecedenceEnd != null)
       {
           retVal = m_pPrecedenceEnd.getPosition()
           + m_pPrecedenceEnd.getLength();
       }
       else
       {
           int max = getExpressionCount();
           for(int index = 0; index < max; index++)
           {
               IExpressionProxy proxy = getExpression(index);
               if(proxy != null)
               {
                   long pos = proxy.getEndPosition();
                   if(retVal < pos)
                   {
                       retVal = pos;
                   }
               }
           }
       }
       return retVal;
   }
   
   public long getStartLine()
   {
       long retVal = -1;
       if(m_pPrecedenceStart != null)
       {
           retVal = m_pPrecedenceStart.getLine();
       }
       else
       {
           retVal = new ExpressionStateHandler().getStartLine();
       }
       return retVal;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.expression.IConditionalExpression#getStartPosition()
         */
   public long getStartPosition()
   {
       long retVal = -1;
       if(m_pPrecedenceStart != null)
       {
           retVal = m_pPrecedenceStart.getPosition();
       }
       else
       {
           retVal = new ExpressionStateHandler().getStartPosition();
       }
       return retVal;
   }

   public void processToken(ITokenDescriptor pToken, String language)
   {
       if(pToken != null)
       {
           String type = pToken.getType();
           if("Precedence Start".equals(type))
           {
               m_pPrecedenceStart = pToken;
           }
           else if("Precedence End".equals(type))
           {
               m_pPrecedenceEnd = pToken;
           }           
           else
           {
               super.processToken(pToken, language);
           }
       }
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
         }
      }
      
      if(m_pPrecedenceStart != null)
      {
        String value = m_pPrecedenceStart.getValue();
         if(value.length() > 0)
         {
            retVal += value;
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
      
      if(m_pPrecedenceEnd != null)
      {
        String value = m_pPrecedenceEnd.getValue();
         if(value.length() > 0)
         {
            retVal += value;
         }  
      }
      return retVal;
   }
}
