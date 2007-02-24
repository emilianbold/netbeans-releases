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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREParameter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageDataType;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class OperationLocator extends LocatorEvaluator<MethodDeclaration>
{
    public OperationLocator(String methodName, 
                       ETList<ETPairT<InstanceInformation, String>> arguments)
    {
        m_OperationName = methodName;
        m_DesiredParameters = arguments;
    }
    
    /**
     * Searches a specific class for a method declaration.
     *
     * @param pClass [in] The class to search.
     * @param pVal [out] The attribute instance information
     */
    public MethodDeclaration findElement(IREClass thisClass, 
                                         IREClassLoader classLoader,
										 MethodDeclaration element)
    {
        ETList<IREOperation> ops = thisClass.getOperations();
        if (ops != null)
        {
            for (int i = 0, count = ops.size(); i < count; ++i)
            {
                IREOperation oper = ops.get(i);
                if (isDesiredOperation(oper, thisClass, classLoader))
                {
                    element.setOperation(oper);
                    element.setOwner(thisClass);
                    break;
                }
            }
        }
        return element;
    }
    
    protected boolean isDesiredOperation(IREOperation oper, 
                                         IREClass thisClass, 
                                         IREClassLoader classLoader)
    {
        if (oper != null)
        {
            String name = oper.getName();
            if (name != null && name.equals(m_OperationName))
            {
                ETList<IREParameter> pars = oper.getParameters();
                return checkParameters(pars, oper, thisClass, classLoader);
            }
        }
        return false;
    }

    /**
     * Checks if the paramters of an operation are the same as the operation
     * that we are trying to locate.
     *
     * @param pParams [in] The paramters to be tested.
     * @param pThis [in] The this pointer.
     * @param pLoader [in] The class loader to use when searching for new classes. 
     * @return True if the paramters are equal, false otherwise.
     */
    protected boolean checkParameters(ETList<IREParameter> paramList, 
                                      IREOperation operation, 
                                      IREClass thisClass, 
                                      IREClassLoader classLoader)
    {
        if (paramList != null)
        {
            int max         = paramList.size(), 
                desiredMax  = m_DesiredParameters.size();
            
            // There will always be one extra parameter for the return type 
            // of an operation.  This is a HACK in UML.  However I have to 
            // live with it.
            boolean isConstructor = operation.getIsConstructor();
            int totalParams = max;
            
            if (!isConstructor)
                totalParams--;
            
            if (totalParams == desiredMax)
            {
                if (totalParams == 0) return true;
                
                for (int i = 0, testParam = 0; i < max; ++i)
                {    
                    IREParameter cur = paramList.get(i);
                    if (cur == null) continue;
                    
                    // First make sure that this isn't the parameter that
                    // specifies the methods return type.
                    if (cur.getKind() != IREParameter.PDK_RESULT
                            && !areParametersEqual(classLoader, thisClass,
                                    cur, m_DesiredParameters.get(testParam++)))
                        return false;
                }

                return true;
            }
        }
        return false;
    }

    /**
     * Checks if two paramters are the same data type.  The generatlization
     * tree of the actual paramter will be check it both paramters are from 
     * the same generalization tree.
     * 
     * @param pLoader [in] The class loader to use when searching for new classes.
     * @param pThis [in] The this pointer.  Used when searching the generalizations.
     * @param pActualParam [in] The actual paramter found in the operation definition.
     * @param desiredType [in] The type to find.
     * @return True if the paramters are equal, false otherwise.
     */
    protected boolean areParametersEqual(IREClassLoader loader, 
                                         IREClass thisC, 
                                         IREParameter actualParam, 
                              ETPairT<InstanceInformation,String> desiredType)
    {
        String curtype = actualParam.getType();
        InstanceInformation inst = desiredType.getParamOne();
        
        String typeName = inst.getInstanceTypeName();
        
        boolean retVal = false;
        
        if(typeName != null && curtype.equals(typeName))
           retVal = true;
        else if(checkIfObject(curtype))
           retVal = true;
        else if(typeName.equals("null") || typeName.equals("NULL"))
           retVal = true;
        else if(checkShortNames(inst,curtype))
           retVal = true;
        else if(checkPrimitiveType(inst,actualParam,curtype,typeName))
           retVal = true;
        // All of the simple test failed so try to find the class deinfitions and 
        // determine if they are the same.  We may have to chase the generalizaton tree
        // to see if the desiredType matches the acutalParam
        //else if(CheckClassDefinitions(pLoader, pThis, acutalParamStr, desiredType) == true)
        else
        {
           if(inst.isDerivedFrom(curtype, loader))
              retVal = true;
        }
        
        return retVal;

    }

    /**
     * Checks if the specified class is the java class "java.lang.Object".
     * The check is used when determining if two class are the same.  Since
     * all java classes decend from java.lang.Object then all class are 
     * equal to java.lang.Object.
     *
     * @param testClass [in] The class name to test.
     * @return true if the class name is Object.
     */
    protected boolean checkIfObject(String testClass)
    {
        return "Object".equals(testClass)
            || "java.lang.Object".equals(testClass)
            || "java::lang::Object".equals(testClass);
    }

    /**
     * Retrieves the short class name of the specified type.  For example:
     * java::lang::Object -> Object.
     * 
     * @param ref [in] The instance reference of the parameter being passed into the
     *                 operation.
     * @param actualParam [in] The name of the actual parameter.
     * @return true if they are the same.
     */
    protected boolean checkShortNames(InstanceInformation ref, 
                                      String actualParam)
    {
        String actualShort = getShortName(actualParam);
        String instShort   = getShortName(ref.getInstanceTypeName());
        return actualShort != null && actualShort.equals(instShort);
    }

    protected boolean checkPrimitiveType(InstanceInformation ref, 
                                         IREParameter actualParam, 
                                         String paramType, String argumentType)
    {
        if (actualParam.getIsPrimitive())
        {
            String filename = actualParam.getFilename();
            ILanguage lang  = getLanguageDef(filename);
            if (lang != null)
            {
                ILanguageDataType typeDef = lang.getDataType(paramType);
                if (typeDef != null)
                {    
                    String umlName = typeDef.getUMLName();
                    return umlName != null && umlName.equals(argumentType);
                }
            }
        }
        return false;
    }

    protected ILanguage getLanguageDef(String filename)
    {
        if (filename == null || filename.length() == 0) return null;
        
        // Retrieve the LanguageManager from the CoreProduct
        ICoreProduct prod = ProductRetriever.retrieveProduct();
        if (prod != null)
        {
            ILanguageManager langman = prod.getLanguageManager();
            if (langman != null)
            {    
                // Use the language manager to retrieve the parser.
                return langman.getLanguageForFile(filename);
            }
        }
        return null;
    }

    /**
     * Retrieves the short class name of the specified type.  For example:
     * java::lang::Object -> Object.
     * 
     * @param name [in] The name of the type.
     * @return The short name.
     */
    protected String getShortName(String name)
    {
        // First test if we have a fully scoped name.  Fully scoped names
        // will contain a scope seperator ( "." )between the package and 
        // class names.
        int scopeSep = name.lastIndexOf(".");
        String result = name ;
        
        if (scopeSep == -1) {
            scopeSep = name.lastIndexOf("::");
            if (scopeSep != -1)
                result = name.substring(scopeSep + 2) ;
        }
        else {
            result = name.substring(scopeSep + 1) ;
        }
        
        return result ;
    }

    private String m_OperationName;
    private ETList<ETPairT<InstanceInformation, String>> m_DesiredParameters;
}