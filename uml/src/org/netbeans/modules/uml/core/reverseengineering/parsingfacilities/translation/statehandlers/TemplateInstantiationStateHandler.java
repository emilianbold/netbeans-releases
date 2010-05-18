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
 * File       : TemplateInstantiationStateHandler.java
 * Created on : Dec 9, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import java.util.ArrayList;
import java.util.Stack;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.Identifier;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author Aztec
 */
public class TemplateInstantiationStateHandler extends StateHandler
{
    private ArrayList < TemplateIdentifier > m_TypeIdentifiers = new ArrayList < TemplateIdentifier >();
    private Identifier mCurrentTypeIdentifier = null;
    
    private Identifier m_NameIdentifier = null;
    private boolean mNameState = true;
    
    private Node m_pTDerivation;
    private boolean mPersistDataToXML = true;
    
    private Stack < TemplateInstantiationStateHandler > childInstances = 
            new Stack < TemplateInstantiationStateHandler >();
    
    // Array Processing properties.  For a template all we care about
    // is the array type and the number of dimensions.
    
//    private boolean processingArray = false;
//    private Identifier arrayType = null;
//    private int dimensions = 0;
    MultiplicityIdentifier multiplicityIdentifier = null;

    public TemplateInstantiationStateHandler()
    {
        this(true);
    }
    
    public TemplateInstantiationStateHandler(boolean writeToXML)
    {
        mCurrentTypeIdentifier = null;
        mPersistDataToXML = writeToXML;
    }

    /**
     * Retrieve the identifier that contains the name of the generic type.
     */
    public Identifier getTypeIdentifier()
    {
       return m_NameIdentifier;
    }
    
    /**
     * Retrieve the identifier that contains the name of the generic type.
     */
    public String getTypeNameAsUML()
    {
       return m_NameIdentifier.getIdentifierAsUML();
    }
    
    /**
     * Retrieve the number of generic arguments.
     */
    public int getNumberOfArguments()
    {
       return m_TypeIdentifiers.size();
    }
    
    /**
     * Retrieve one of the arguemnts that was passed into the generic
     * instanitation.
     */
    public TemplateIdentifier getArgument(int index)
    {
       TemplateIdentifier retVal = null;
       if(index < getNumberOfArguments())
       {
          retVal = m_TypeIdentifiers.get(index);
       }
       return retVal;
    }
    
    protected void handleTemplateInstantiationName()
    {
       if(mPersistDataToXML == true)
       {
          Identifier id = getTypeIdentifier();
          String value = id.getIdentifierAsUML();
          long line = id.getStartLine();
          long col = id.getStartColumn();
          long pos = id.getStartPosition();
          long length = id.getLength();

          Element element = (m_pTDerivation instanceof Element)
          ? (Element)m_pTDerivation : null;

          XMLManip.setAttributeValue(element,"line", Long.toString(line));
          XMLManip.setAttributeValue(element,"column", Long.toString(col));
          XMLManip.setAttributeValue(element,"position", Long.toString(pos));
          XMLManip.setAttributeValue(element,"name", value);
          XMLManip.setAttributeValue(element,"length", Long.toString(length));
       }
       
    }

    protected void handleTemplateInstantiationParameters()
    {
       if(mPersistDataToXML == true)
       {
          for(TemplateIdentifier curType : m_TypeIdentifiers)
          {
             // Create parameter node
             Node pDerivationParameter = XMLManip.createElement((Element)m_pTDerivation,
                      "DerivationParameter");
             setDOMNode(pDerivationParameter);

             String value = curType.getValue();
             long line = curType.getStartLine();
             long col = curType.getStartColumn();
             long pos = curType.getStartPosition();
             long length = curType.getLength();

             Element element = (pDerivationParameter instanceof Element)
             ? (Element)pDerivationParameter : null;

             XMLManip.setAttributeValue(element,"line", Long.toString(line));
             XMLManip.setAttributeValue(element,"column", Long.toString(col));
             XMLManip.setAttributeValue(element,"position", Long.toString(pos));
             XMLManip.setAttributeValue(element,"value", value);
             XMLManip.setAttributeValue(element,"length", Long.toString(length));
          }
       }
       
    }

    /**
     * Creates and returns a new state handler for a sub-state.  If the sub-state
     * is not handled then NULL is returned.  The attribute state of interest is
     * <code>Initializer</code>
     *
     * @param stateName [in] The name of the new state.
     * @param language [in] The language of the state.
     *
     * @return The handler for the sub-state, NULL if the state is not handled.
     */
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;
        
        if("Type".equals(stateName))
        {
           handleTemplateInstantiationName();
           mCurrentTypeIdentifier = new Identifier();
//           m_TypeIdentifiers.add(new TokenIdentifier(mCurrentTypeIdentifier));
           
           mNameState = false;
           retVal = this;
        }
        else if("Identifier".equals(stateName))
        {
           retVal = this;
        }
        else if("Template Instantiation".equals(stateName) == true)
        {
           retVal = new TemplateInstantiationStateHandler(mPersistDataToXML);
           retVal.setDOMNode(getDOMNode());
           childInstances.push((TemplateInstantiationStateHandler)retVal);
        }
        else if("Array Declarator".equals(stateName) == true)
        {
            if(multiplicityIdentifier == null)
            {
                multiplicityIdentifier = new MultiplicityIdentifier();
            }
            
            // Array Declarators are recursive.  Therefore, if one already 
            // exist, it means that we are simply defining a new dimension.
            multiplicityIdentifier.addDimension();
            retVal = this;
        }
        else
        {
            retVal = super.createSubStateHandler(stateName, language);
        }
        
        return retVal;
    }

    /**
     * Initialize the state handler.  This is a one time initialization.
     */
    public void initialize()
    {
       if(mPersistDataToXML == true)
       {
          Node pCurNode = getDOMNode();
          
          if( pCurNode != null )
          {
             Node pTokenDescriptors = ensureElementExists(pCurNode,
                      "TokenDescriptors",
                      "TokenDescriptors");
             
             if(pTokenDescriptors != null)
             {
                m_pTDerivation = XMLManip.createElement((Element)pTokenDescriptors, "TDerivation");
                setDOMNode(m_pTDerivation);
             }
          }
       }
    }

    /**
     * The state handler is able to process the token.  Attribute tokens of
     * interest are <code>Statement Terminator</code> and
     * <code>Primitive Type</code>
     *
     * @param pToken [in] The token to process.
     */
    public void processToken(ITokenDescriptor pToken, String language)
    {

        if(pToken == null) return;

        String tokenType = pToken.getType();

        if("Name".equals(tokenType))
        {
            //HandleToken(pToken, _T("name"));
            //handleTemplateInstantiationName(pToken);
        }
        else if("Identifier".equals(tokenType) ||
                  "Primitive Type".equals(tokenType) ||
                  "Scope Operator".equals(tokenType))
        {
           if(mNameState == true)
           {
               if(m_NameIdentifier == null)
               {
                  m_NameIdentifier = new Identifier();  
               }
               m_NameIdentifier.addToken(pToken);
           }
           else if(multiplicityIdentifier != null)
           {
//               if(arrayType == null)
//               {
//                  arrayType = new Identifier();  
//               }
//               arrayType.addToken(pToken);
               multiplicityIdentifier.addIdentifierToken(pToken);
               
           }
           else
           {
              if(mCurrentTypeIdentifier != null)
              {
                 mCurrentTypeIdentifier.addToken(pToken);
              }
               
               if("Primitive Type".equals(tokenType))
               {
                   createTokenDescriptor("IsPrimitive", -1, -1, -1, "true", -1);
               }
           }
        }
        else if("Array Start".equals(tokenType) == true)
        {
            multiplicityIdentifier.setStartDimensionToken(pToken);
        }
        else if("Array End".equals(tokenType) == true)
        {
            // Since the array declarator states are recursive the first
            // array declarator will set multiplicityIdentifier to null.
            // This is not a big deal since we have already got all the 
            // information that we need.  
            //
            // Since the declarations are depth first, we have already found
            // the last array end token.
            if(multiplicityIdentifier != null)
            {
                multiplicityIdentifier.setEndDimensionToken(pToken);
            }
        }
    }

    /**
     * Notification that the a state has completed.  All state clean up is
     * done this the StateComplete state.  The Attribute states of interest
     * is <code>Initializer</code> and <code>Type</code>.
     *
     * @param stateName [in] The name of the state.
     */
    public void stateComplete(String stateName)
    {
//        if("Type".equals(stateName))
//        {
//            handleTemplateInstantiationParameter();
//        }
       if(stateName.equals("Template Instantiation") == true)
       {
           if(childInstances.empty() == false)
           {
               TemplateInstantiationStateHandler handler = childInstances.pop();
               String value = handler.toString();
               m_TypeIdentifiers.add(new TemplateIdentifier(value));
           }
//           else
           {
               handleTemplateInstantiationParameters();
           }
       }
       else if("Type".equals(stateName) == true)
       {
           String identStr = mCurrentTypeIdentifier.getIdentifierAsSource();
           if(identStr.length() > 0)
           {
               m_TypeIdentifiers.add(new TokenIdentifier(mCurrentTypeIdentifier));
           }
       }
       else if("Array Declarator".equals(stateName) == true)
       {
           if(multiplicityIdentifier != null)
           {
               m_TypeIdentifiers.add(multiplicityIdentifier);

               multiplicityIdentifier = null;
           }
           
       }
       super.stateComplete(stateName);
    }

    public String toString()
    {
        String retVal = m_NameIdentifier.getIdentifierAsSource();
        
        String parameters = "";
        for(TemplateIdentifier curIdentifier : m_TypeIdentifiers)
        {
            if(parameters.length() > 0)
            {
                parameters += ", ";
            }
            parameters += curIdentifier.getValue();
        }
        
        retVal += "<" + parameters + ">";
        return retVal;
    }
    
    protected class TemplateIdentifier
    {
        private String value = "";
        
        public TemplateIdentifier()
        {
            
        }
        
        public TemplateIdentifier(String data)
        {
            value  = data;
        }

        public String getValue()
        {
            return value;
        }
        
        public long getStartLine()
        {
            return -1;
        }
        
        public long getStartColumn()
        {
            return -1;
        }
        
        public long getStartPosition()
        {
            return -1;
        }
        
        public long getLength()
        {
            return -1;
        }
    }
    
    protected class TokenIdentifier extends TemplateIdentifier
    {
        private Identifier value = null;
        
        public TokenIdentifier(Identifier data)
        {
            super(data.getIdentifierAsSource());
            value  = data;
        }
        
        public long getStartLine()
        {
            return value.getStartLine();
        }
        
        public long getStartColumn()
        {
            return value.getStartColumn();
        }
        
        public long getStartPosition()
        {
            return value.getStartPosition();
        }
        
        public long getLength()
        {
            return value.getLength();
        }
    }
    
    protected class MultiplicityIdentifier extends TemplateIdentifier
    {
        private int dimensions = 0;
        private ITokenDescriptor startDimToken = null;
        private ITokenDescriptor endDimToken = null;
        private Identifier type = new Identifier();
        
        public MultiplicityIdentifier()
        {
            super();
        }
        
        public void addIdentifierToken(ITokenDescriptor token)
        {
            type.addToken(token);
        }
        
        public String getValue()
        {
            String value = type.getIdentifierAsSource();
            for(int dim = 0; dim < dimensions; dim ++)
            {
                value += startDimToken.getValue() + endDimToken.getValue();
            }
            
            return value;
        }
        
        public void addDimension()
        {
            dimensions++;
        }
        
        public long getStartLine()
        {
            return type.getStartLine();
        }
        
        public long getStartColumn()
        {
            return type.getStartColumn();
        }
        
        public long getStartPosition()
        {
            return type.getStartPosition();
        }
        
        public long getLength()
        {
            return endDimToken.getPosition() - getStartPosition();
        }

        public void setEndDimensionToken(ITokenDescriptor endDimToken)
        {
            this.endDimToken = endDimToken;
        }

        public void setStartDimensionToken(ITokenDescriptor startDimToken)
        {
            this.startDimToken = startDimToken;
        }
        
        
    }
}
