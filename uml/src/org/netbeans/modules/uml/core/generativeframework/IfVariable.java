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
 * File       : IfVariable.java
 * Created on : Oct 30, 2003
 * Author     : aztec
 */
package org.netbeans.modules.uml.core.generativeframework;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 */
public class IfVariable extends CompoundVariable implements IIfVariable
{
    private ETList<IIfTest> m_Tests = new ETArrayList<IIfTest>();
    private int             m_Kind;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIfVariable#addTest(org.netbeans.modules.uml.core.generativeframework.IIfTest)
     */
    public void addTest(IIfTest pTest)
    {
        if (!m_Tests.contains(pTest))
            m_Tests.add(pTest);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIfVariable#removeTest(org.netbeans.modules.uml.core.generativeframework.IIfTest)
     */
    public void removeTest(IIfTest pTest)
    {
        m_Tests.remove(pTest);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.IIfVariable#getTests()
     */
    public ETList<IIfTest> getTests()
    {
        return m_Tests;
    }
    
    public String expand(Node context)
    {
        String results = null;
        if (m_Tests.size() > 0)
        {    
            int count = m_Tests.size();
            for (int i = 0; i < count; ++i)
            {
                IIfTest test = m_Tests.get(i);
                if (test == null) continue;
                
                String testName = test.getTest();
                boolean executeAction = true;
                if (testName != null && testName.length() > 0)
                {
                    IExpansionVariable var = retrieveVarNode(testName);
                    if (var != null)
                    {
                        String testResults = var.expand(context);
                        
                        if (!"true".equals(testResults))
                        {
                            executeAction = false;
                            setKind(VariableKind.VK_BOOLEAN);
                            setIsTrue(false);
                        }
                        else
                        {
                            setKind(VariableKind.VK_BOOLEAN);
                            setIsTrue(true);
                        }
                    }
                }
                
                if (executeAction)
                {
                    String actionTemplate = test.getResultAction();
                    if (actionTemplate != null && actionTemplate.length() > 0)
                    {
                        String ext = 
                                StringUtilities.getExtension(actionTemplate);
                        if (ext != null && ext.length() > 0)
                            results = 
                                expandTemplateWithNode(actionTemplate, context);
                        else
                        {
                            // If we don't have an extension, then assume we've 
                            // got a variable name
                            IExpansionVariable var = 
                                    retrieveVarNode(actionTemplate);
                            if (var != null)
                                results = var.expand(context);
                        }
                        
                        if (testName == null || testName.length() == 0)
                        {
                            // testName will be blank in the case if an IfVar 
                            // that has a syntax equating essential to an "else" 
                            // construct.

                            // For example, 
                            //
                            // %%IfVar <test="useCollectionOverride" var="collectionOverride"><var="typeName">%%
                            //
                            // notice the last part of the IfVar is just "<var="typeName">".
                            setKind(VariableKind.VK_BOOLEAN);
                            setIsTrue(true);
                        }
                    }
                    break;
                }
            }
        }
        return results;
    }
    
    public int getKind()
    {
        return m_Kind;
    }
    
    public void setKind(int kind)
    {
        m_Kind = kind;
    }
    
    protected void validate()
    {
        if (m_Expander == null)
            throw new IllegalStateException("No context");
    }
}
