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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.InstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.ObjectInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.PrimitiveInstanceInformation;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.SymbolTable;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 */
public class MethodDetailStateHandler extends ExpressionStateHandler
{
    private IOpParserOptions m_pOptions;
    private String m_InstanceName;

    // The MethodDetailsStateHandler is not managing the pointer.
    private SymbolTable m_SymbolTable;
    private String m_Language;
    private boolean m_IsWriteXML;
    private InstanceInformation m_InstanceReference;

    private Document m_Document;
    
    public MethodDetailStateHandler(String language)
    {
        m_IsWriteXML = false;
        m_Language = language;

        // I can not return a HRESULT from a constructor.  So I will just
        // report the error.  I added the "MethodDetailStateHandler::" because
        // it is a static method.  I know that scope is not required, I just
        // want to be explict.
        createTopLevelNode("UML:Procedure");

    }
    
    /**
     * Initializes the state handler.  This is a one time initialization.
     *
     * @param pOptions [in] The options.
     *
     * @return 
     */
    public void methodInitialize(IOpParserOptions pOptions, SymbolTable table)
    {
        setOptions(pOptions);
        setSymbolTable(table);
    }

    public void setOptions(IOpParserOptions options)
    {
        m_pOptions = options;
    }

    public void setSymbolTable(SymbolTable table)
    {
        m_SymbolTable = table;
    }

    /**
     * Retrieve the parser options.
     *
     * @param *pVal [in] The parser options.
     */
    public IOpParserOptions getOpParserOptions()
    {
        return m_pOptions;
    }

    public SymbolTable getSymbolTable()
    {
        return m_SymbolTable;
    }

    /**
     * Retrives the operation that is being processed.
     *
     * @param *pVal [in] The operation that is being processed.
     */
    public IREOperation getOperationBeingProcessed()
    {
        IREOperation pVal = null;
        IOpParserOptions pOptions = getOpParserOptions();
        if(pOptions != null)
        {
            pVal = pOptions.getOperation();
        }
        return pVal;
    }

    /**
     * Retrieve the class loader from the user settings.
     *
     * @param *pVal [in] The classloader.
     */
    public IREClassLoader getClassLoader()
    {
        
        IREClassLoader pVal = null;
        
        IOpParserOptions pOptions = getOpParserOptions();
        if(pOptions != null)
        {
            pVal = pOptions.getClassLoader();
        }
        return pVal;
    }

    /**
     * Retrives the class that contains the operation that is being processed.
     *
     * @param *pVal [in] The class.
     */
    public IREClass getClassBeingProcessed()
    {
        IREClass pVal = null;
        
        IREOperation pCurOperation = getOperationBeingProcessed();

        if(pCurOperation != null)
        {
            pVal = pCurOperation.getOwner();
        }
        return pVal;
    }

    /**
     * Specify whether the state handler is generating events
     * or writing to an XMI fragment.
     *
     * @return The value.
     */
    public boolean isWriteXMI()
    {
        return m_IsWriteXML;
    }

    public void setWriteXMI(boolean value)
    {
        m_IsWriteXML = value;
    }
    
    public String toString()
    {
        return "";
    }

    /**
     * Creates a CPrimitiveInstanceInformation and adds it to the symbol table.
     * If the name or type name is not valid then a CPrimitiveInstanceInformation
     * will not be created.
     *
     * @param name [in] The name of the instance.
     * @param typeName [in] The type of the instance.
     *
     * @return The instance that is added the table.  NULL if the instance
     *         name or type name is not valid.
     */
    public InstanceInformation addPrimitiveInstance(String name, 
                                                    String typeName)
    {
        InstanceInformation retVal = null;
    
        if(name != null && typeName != null)
        {
            retVal = new PrimitiveInstanceInformation(name, typeName);
             
            IREClass pCurClass = getClassBeingProcessed();
            retVal.setInstanceOwner(pCurClass);
             
            getSymbolTable().addInstance(retVal, false);
        }
        return retVal;
    }
    
    /**
     * Creates a CPrimitiveInstanceInformation and adds it to the symbol table.
     * If the name or type name is not valid then a CPrimitiveInstanceInformation
     * will not be created.
     *
     * @param name [in] The name of the instance.
     * @param typeName [in] The type of the instance.
     *
     * @return The instance that is added the table.  NULL if the instance
     *         name or type name is not valid.
     */
    public InstanceInformation addObjectInstance(String name, String typeName)
    {
        InstanceInformation retVal = null;

        if(name != null && typeName != null)
        {
            IREClass pCurClass = getClassBeingProcessed();

            if(pCurClass != null)
            {
                IREClass pType = findClass(pCurClass, typeName);
         
                //retVal = new CObjectInstanceInformation(name, pType); 
                retVal = new ObjectInstanceInformation(name, typeName, pType); 
                retVal.setInstanceOwner(pCurClass);

                getSymbolTable().addInstance(retVal, false);
            }
        }
        return retVal;
    }
    

    /**
     * Retrieves the instance information associated to a instance name.
     *
     * @param name [in] The name of the instance to find.
     *
     * @return 
     */
    public InstanceInformation getInstance(String name)
    {
        InstanceInformation retVal = null;
       
        if(name != null)
        {
            retVal =getSymbolTable().findInstance(name);
        }
    
        return retVal;
    }

    public long getStartPosition()
    {
        return 0;
    }

    public long getEndPosition()
    {
        return 0;
    }

    public long getStartLine()
    {
        return 0;
    }
    
    /**
     * Creates the top level node.  The document will be created first then
     * the top level node will created. 
     *
     * @param nodeName [in] The name of the top node.
     */
    protected void createTopLevelNode(String nodeName)
    {
        if(nodeName == null) return;
        // m_Document is a static data member.  I only want to create the
        // document once.
            if(m_Document == null)
            {  
                m_Document = XMLManip.getDOMDocument();         
            }

            Node pNode = getDOMNode();

            if(m_Document != null && pNode == null)
            {
                Element pRootElement = m_Document.getRootElement();

                if(pRootElement != null)
                {            
                    pNode = pRootElement;
                    setDOMNode(pNode);
                }
                else
                {
                    Node pTopNode = m_Document;

                    Node pNewNode = createNode(pTopNode, nodeName);

                    if(pNewNode != null)
                    {
                        Element element = (pNewNode instanceof Element)?
                                            (Element)pNewNode : null;
                    if(element != null)
                    {
                        XMLManip.setAttributeValue(element, "language", getLanguage()); 
                    }

                    setDOMNode(pNewNode);
                }
            }
        }
    }
    
    //  *********************************************************************
    //  ClassLoader Helper Methods
    //  *********************************************************************
    
    /**
     * Uses the class loader to find a class.  The "this" pointer is the 
     * class that is the owner of the operation that is being parsed.
     *
     * @param typeName [in] The name of the type to find.
     * @param pVal [out] The IREClass structure that represents the type.
     */
    protected IREClass findClass(String typeName)
    {
        if(typeName == null) return null;
    
        IREClass pCurClass = getClassBeingProcessed();
    
        if(pCurClass != null)
        {
            return findClass(pCurClass, typeName);
        }
        return null;
    }

    /**
    * Uses the class loader to find a class.  The context class is used
    * to located the class.  The context is used to determine what 
    * dependencies to search.
    *
    * @param pContext [in] The "this" pointer.
    * @param typeName [in] The name of the type to find.
    * @param pVal [out] The IREClass structure that represents the type.
    */
    protected IREClass findClass(IREClass pContext,
                                 String typeName)
    {

        if(pContext == null) return null;
        if(typeName == null) return null;

        IREClassLoader pLoader = getClassLoader();
        if(pLoader != null)
        {
            return pLoader.loadClass(typeName, pContext);
        }
        return null;
    }
    
    /**
     * Retrieve the language that is being processed by the parser.
     *
     * @return The name of the language.
     */
    protected String getLanguage()
    {
        return m_Language;
    }
    
    protected InstanceInformation getReferenceInstance()
    {
        return m_InstanceReference;
    }

    protected void setReferenceInstance(InstanceInformation value)
    {
        m_InstanceReference = value;
    }
    
    /**
     * Composite actions will contain the subactions.  So, a sub-statehandler
     * must be set teh IsWriteXMI to true.  The  sub-statehandler DOM node must
     * also be set to the composite states DOM node.
     *
     * @param pNode [in] The parent node.
     */
    protected void initializeHandler(MethodDetailStateHandler handler
                                    , Node pNode)
    {
        if(handler != null)
        {
            handler.setDOMNode(pNode);
            //handler->SetWriteXMI(true);
            handler.setWriteXMI(false);
            handler.setReferenceInstance(getReferenceInstance());
        }
    }
    
    /**
     * Reports the expression data.  If the m_IsWriteXMI
     * is falas then a event will be sent, otherwise the 
     * stata will be added to the current XML node.
     */
    protected void reportData()
    {
        Node pNode = getDOMNode();

        IREClass pThisClass = getClassBeingProcessed();

        IUMLParserEventDispatcher pDispatcher = 
            getEventDispatcher();
   
        IREClassLoader pLoader = getClassLoader();

        if(pDispatcher != null && pLoader != null)
        {
            if(isWriteXMI())
            {
                Node pData = writeAsXMI(getReferenceInstance(), 
                        pNode,
                        getSymbolTable(), 
                        pThisClass, 
                        pLoader).getParamTwo();
            }
            else
            {
                sendOperationEvents(getReferenceInstance(), 
                                    pThisClass,
                                    getSymbolTable(), 
                                    pLoader, 
                                    pDispatcher,
                                    pNode);
            }
        }
    }


}
