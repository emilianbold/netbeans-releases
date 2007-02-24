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
 * File       : ImportVar.java
 * Created on : Oct 30, 2003
 * Author     : aztec
 */
package org.netbeans.modules.uml.core.generativeframework;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.INamedCollection;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author aztec
 */
public class ImportVariable extends CompoundVariable implements IImportVariable
{
    private String                   m_Template;
    private String                   m_TestClause;
    private ETList<INamedCollection> m_Pairs;
    
    public String getTemplate()
    {
        return m_Template;
    }
    
    public void setTemplate(String newVal)
    {
        m_Template = newVal;
    }
    
    public String getTestClause()
    {
        return m_TestClause;
    }
    
    public void setTestClause(String newVal)
    {
        m_TestClause = newVal;
    }
    
    public ETList<INamedCollection> getConditions()
    {
        return m_Pairs;
    }
    
    public void setConditions(ETList<INamedCollection> pCols)
    {
        m_Pairs = pCols;
    }
    
    public String expand(Node context)
    {
        if (m_Expander != null)
        {
            return m_TestClause != null && m_TestClause.length() > 0?
								expandWithTestClause(context)
							:   expandTemplateWithNode(m_Template, context);
        }
        return null;
    }

    protected void validate()
    {
        super.validate();
        if (m_Expander == null)
        {
            throw new IllegalStateException("No context");
        }
    }
    
    private String expandWithTestClause(Node context)
    {
        if (m_TestClause != null && m_TestClause.length() > 0 
                && m_Pairs != null)
        {
            String testResults = getTestResults(context);
            // Now find the resultAction that matches the result.
            // If none is found, use the default VarOption expansion
            String finalQuery = getMatchingResult(testResults);
            return finalQuery != null? 
                        expandTemplateWithNode(finalQuery, context)
                      : expandTemplateWithNode(m_Template, context);
        }
        return null;
    }
    
    private String getMatchingResult(String testCase)
    {
        if (m_Pairs != null)
        {
            int count = m_Pairs.size();
            for (int i = 0; i < count; ++i)
            {
                INamedCollection col = m_Pairs.get(i);
                if (col != null)
                {
                    String condition = col.getName();
                    String data      = col.getData().toString();
                    if (testCase != null && testCase.equals(condition))
                        return data;
                }
            }
        }
        return null;
    }
    
    private String getTestResults(Node context)
    {
        String results = null;
        IVariableExpander expander = getExecutionContext();
        if (expander != null)
        {
            ITemplateManager man = expander.getManager();
            Node varNode = expander.retrieveVarNode(m_TestClause);
            
            if (varNode == null)
                // Assume the clause is a template name
                results = expandTemplateWithNode(m_TestClause, context);
            else
            {
                IVariableFactory fact = man.getFactory();
                if (fact != null)
                {
                    IExpansionVariable var = fact.createVariable(varNode);
                    if (var != null)
                        results = var.expand(context);
                }
            }
        }
        return results;
    }
}