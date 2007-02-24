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


package org.netbeans.modules.uml.core.generativeframework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class VariableExpander implements IVariableExpander {
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#getConfigFile()
	 */
	public String getConfigFile() 
    {
        return m_Config;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#setConfigFile(java.lang.String)
	 */
	public void setConfigFile(String value) 
    {
        m_Config = value;
	}

    protected void loadConfigFile()
    {
        if (m_ConfigDoc == null && m_Config != null)
            m_ConfigDoc = XMLManip.getDOMDocument(m_Config);
    }
    
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#retrieveVarNode(java.lang.String)
	 */
	public Node retrieveVarNode(String name)
    {
        loadConfigFile();
        
        if (m_ConfigDoc != null)
        {
            String query = "//ExpansionVar[@name=\"" + name + "\"]";
            Node node = m_ConfigDoc.selectSingleNode(query);
            return node;
        }
        return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#getManager()
	 */
	public ITemplateManager getManager() {
        return m_Manager;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#setManager(org.netbeans.modules.uml.core.generativeframework.ITemplateManager)
	 */
	public void setManager(ITemplateManager value) {
        m_Manager = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#expand(java.lang.String, org.netbeans.modules.uml.core.generativeframework.IExpansionVariable, org.dom4j.Node)
	 */
	public String expand(String prevText, IExpansionVariable var, Node context)
    {
        String result = null;
        String varRes = var.expand(context);
        StringBuffer oldText = new StringBuffer(prevText);
        if (varRes != null && varRes.length() > 0)
        {
            String converted = convertNewLines(prevText, varRes);
            oldText.append(converted);
            if (oldText.length() > 0)
                result = oldText.toString();
        }
        
        if (m_VarStack.size() > 0)
            m_VarStack.peek().add(var);
		return result;
	}
    
    private String convertNewLines(String prevText, String varRes)
    {
        return Formatter.convertNewLines(prevText, varRes);
    }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#beginGathering()
	 */
	public void beginGathering() {
        m_VarStack.push( new ArrayList<IExpansionVariable>() );
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#endGathering()
	 */
	public boolean endGathering() {
        boolean useResults = false;
        if (m_VarStack.size() > 0)
        {
            List<IExpansionVariable> curVars = m_VarStack.pop();
            Iterator<IExpansionVariable> iter = curVars.iterator();
            while (iter.hasNext())
            {
                IExpansionVariable var = iter.next();
                String varRes = var.getResults();
                if (varRes != null && varRes.length() > 0)
                    useResults = true;
                else
                {
                    // Make one last check, making sure that
                    // if the variable is boolean and it expanded
                    // to a true result, we want the results
                    
                    int kind = var.getKind();
                    if (kind == VariableKind.VK_BOOLEAN)
                        useResults = var.getIsTrue();
                }
            }
            
            if (m_VarStack.size() > 0)
            {
                // We have nested optional vars, so push the
                // list of vars just popped onto the current list
                // of vars. This will prevent the situation where an optional
                // var is pushed on the stack which ultimately results in no
                // results. Then another optional var is executed that does 
                // result in results. We want those results, even though the 
                // first variable did not produce any
                List<IExpansionVariable> stackTop = m_VarStack.peek();
                for (iter = curVars.iterator(); iter.hasNext(); )
                {
                    stackTop.add( iter.next() );
                }
            }
        }
		return useResults;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#addResult(org.netbeans.modules.uml.core.generativeframework.IExpansionResult)
	 */
	public void addResult(IExpansionResult pResult) {
		// Stubbed in C++ code
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#removeResult(org.netbeans.modules.uml.core.generativeframework.IExpansionResult)
	 */
	public void removeResult(IExpansionResult pResult) {
        // Stubbed in C++ code
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#appendResults(org.netbeans.modules.uml.core.generativeframework.IExpansionResult[])
	 */
	public void appendResults(ETList<IExpansionResult> pResult) {
        // Stubbed in C++ code
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#getExpansionResults()
	 */
	public ETList<IExpansionResult> getExpansionResults() {
        // Stubbed in C++ code
        return null;
    }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableExpander#setExpansionResults(org.netbeans.modules.uml.core.generativeframework.IExpansionResult[])
	 */
	public void setExpansionResults(ETList<IExpansionResult> value) {
        // Stubbed in C++ code
    }

    private String                                  m_Config;
    private static Document                         m_ConfigDoc;
    private ITemplateManager                        m_Manager;
    private Stack< List< IExpansionVariable > >     m_VarStack = 
                new Stack< List<IExpansionVariable> >();
    private boolean                                 m_Gathering;
}


