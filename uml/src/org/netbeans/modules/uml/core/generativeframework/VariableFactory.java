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


package org.netbeans.modules.uml.core.generativeframework;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.INamedCollection;
import org.netbeans.modules.uml.core.support.umlsupport.NamedCollection;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.Validator;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 */
public class VariableFactory implements IVariableFactory{
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableFactory#createVariable(org.dom4j.Node)
	 */
	public IExpansionVariable createVariable(Node varNode) {
        IExpansionVariable newVar = new ExpansionVariable();
        newVar.setNode(varNode);
        checkForOverride(newVar);
        establishContext(newVar);
        return newVar;
	}
    
    private void checkForOverride(IExpansionVariable var)
    {
        String name = var.getName();
        if (m_Overrides.containsKey(name))
        {
            IExpansionVariable evar = m_Overrides.get(name);
            String valueFilter = evar.getValueFilter();
            var.setValueFilter(valueFilter);
            
            String replaceFilter = evar.getReplaceFilter();
            var.setReplaceFilter(replaceFilter);
        }
    }
    
    private void establishContext(IExpansionVariable var)
    {
        IVariableExpander expander = establishExpander();
        if (expander != null)
            var.setExecutionContext(expander);
    }
    
    private IVariableExpander establishExpander()
    {
        return getExecutionContext();
    }
    
    private ETList<INamedCollection> getResultAndActions(String vartext)
    {
        ETList<INamedCollection> resultActions = 
            new ETArrayList<INamedCollection>();
        if (vartext != null && vartext.length() > 0)
        {
            HashMap<String, String> resultElements = 
                    getIndexedOptions("result", vartext);
            // Now search through and find the resultAction elements
            
            HashMap<String, String> resultActionElements = 
                                getIndexedOptions("resultAction", vartext);
            
            // Now we have all the parts. We need to pair them up. Each result 
            // and resultAction value should begin with an index. See comment 
            // above.
            
            for (Iterator<String> iter = resultElements.keySet().iterator();
                    iter.hasNext(); )
            {
                String index = iter.next();
                String resultAction = resultActionElements.get(index);
                String name = resultElements.get(index);
                if (name != null && resultAction != null)
                {
                    INamedCollection pair = new NamedCollection();
                    pair.setName(name);
                    pair.setData(resultAction);
                    
                    resultActions.add(pair);
                }
            }
        }
        return resultActions;
    }
    
    private HashMap<String,String> getIndexedOptions(String optionName, 
                                                     String vartext)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        String resultLabel = optionName + "=\"";
        int pos = vartext.indexOf(resultLabel);
        while (pos != -1)
        {
            int beginActionPos = pos + resultLabel.length();
            int endActionPos   = vartext.indexOf('"', beginActionPos);
                        
            if (endActionPos == -1) endActionPos = vartext.length();
                        
            String result = vartext.substring(beginActionPos, endActionPos);
            // Now figure out the index value for this element
            int valuepos = result.indexOf(':');
            String index = result.substring(0, valuepos);
                        
            map.put(index, result.substring(valuepos + 1));
                        
            pos = vartext.indexOf(resultLabel, endActionPos);
        }
        
        return map;
    }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableFactory#getExecutionContext()
	 */
	public IVariableExpander getExecutionContext() {
		return m_Contexts.size() > 0? m_Contexts.peek() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableFactory#setExecutionContext(org.netbeans.modules.uml.core.generativeframework.IVariableExpander)
	 */
	public void setExecutionContext(IVariableExpander value) {
        m_Contexts.push( value );
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableFactory#getConfigFile()
	 */
	public String getConfigFile() {
		return m_Config;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableFactory#setConfigFile(java.lang.String)
	 */
	public void setConfigFile(String value) {
        m_Config = value;
	}

    /**
     * Makes sure the config file is valid.
     * @throws FileNotFoundException if the file is not found.
     */
    private void validateConfig() throws FileNotFoundException
    {
        if (!Validator.verifyFileExists(m_Config))
            throw new FileNotFoundException(m_Config);
    }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableFactory#createVariableWithText(java.lang.String)
	 */
	public IExpansionVariable createVariableWithText(String varText) {
        IExpansionVariable newVar = null;
        if (varText != null && varText.length() > 0)
        {
            // Get the name of the variable
            int pos = varText.indexOf(' ');
            if (pos == -1) pos = varText.indexOf('%');
            if (pos == -1) pos = varText.length();
//            if (pos == -1)
//                throw new IllegalArgumentException(
//                        "Variable text '" + varText + "' has no variable name");
            String varName = varText.substring(0, pos);
            
            if ("IterationVar".equals(varName))
                newVar = createIterationVar(varText.substring(pos));
            else if ("ImportVar".equals(varName))
                newVar = createImportVar(varText.substring(pos));
            else if ("IfVar".equals(varName))
                newVar = createIfVar(varText.substring(pos));
            else
            { 
                IVariableExpander expander = establishExpander();
                if (expander != null)
                {
                    Node node = expander.retrieveVarNode(varName);
                    if (node != null)
                        newVar = createVariable(node);
                    else
                    {
                        throw new IllegalArgumentException(
                                "Can't create variable from '" + varText + "'");
                    }
                }
            }
        }
        return newVar;
	}

    /**
     * Creates an IterationVar.
     *
     * @param varText the context text that contains other information specific 
     *                to this var.
     * @return The new var
     */
    private IExpansionVariable createIterationVar(String varText)
    {
        // Find the "list" variable
        String listOption = getListOption(varText);
        
        // An Iteration variable must have either a var option or literal
        // option specified to be valid
        String literal = getLiteral(varText);
        String varOption = getVarOption(varText);
        
        if (varOption == null || literal == null)
        {
            throw new IllegalArgumentException(
                    "No Var option in '" + varText + "'");
        }
        
        String delimiter = getDelimiter(varText);
        IIterationVariable var = new IterationVariable();
        if (listOption != null)
            var.setListVarName(listOption);
        if (varOption != null)
            var.setVarName(varOption);
        if (delimiter != null)
            var.setDelimiter(delimiter);
        if (literal != null)
            var.setLiteral(literal);
        establishContext(var);
        return var;
    }

    private IExpansionVariable createImportVar(String varText)
    {
        String varOption = getVarOption(varText);
        String testClause = getOption("test", varText);
        ETList<INamedCollection> resultActions = getResultAndActions(varText);
        IImportVariable var = new ImportVariable();
        
        if (varOption != null)
            var.setTemplate(varOption);
        if (testClause != null)
            var.setTestClause(testClause);
        if (resultActions != null)
            var.setConditions(resultActions);
        establishContext(var);
        return var;
    }
    
    private IExpansionVariable createIfVar(String vartext)
    {
        List<String> testcases = gatherTestCases(vartext);
        if (testcases != null)
        {
            IIfVariable var = new IfVariable();
            
            for (Iterator<String> iter = testcases.iterator(); iter.hasNext(); )
            {
                IIfTest ifTest = createIfTest(iter.next());
                if (ifTest != null)
                    var.addTest(ifTest);
            }
            
            establishContext(var);
            return var;
        }
        return null;
    }
    
    private List<String> gatherTestCases(String vartext)
    {
        if (vartext != null && vartext.length() > 0)
        {
            List<String> cases = StringUtilities.splitOnDelimiter(vartext, ">");
            for (int i = cases.size() - 1; i >= 0; --i)
            {
                String s = cases.get(i).trim();
                if (s.length() == 0)
                    cases.remove(i);
                else
                    cases.set(i, s);
            }
            return cases.size() > 0? cases : null;
        }
        return null;
    }
    
    /**
     *
     * Creates a new IIfTest that will be added to an IIfVar variable
     *
     * @param varText[in]      The text that contains the test information
     * @param newTest[out]     The new test
     *
     * @return HRESULT
     *
     */
    private IIfTest createIfTest(String vartext)
    {
        IIfTest newtest = null;
        
        String testClause = getOption("test", vartext);
        String varClause = getOption("var", vartext);
        
        newtest = new IfTest();
        if (testClause != null)
            newtest.setTest(testClause);
        if (varClause != null)
            newtest.setResultAction(varClause);
        return newtest;
    }
    
    private String getListOption(String vartext)
    {
        return  getOption("list", vartext);
    }
    
    private String getVarOption(String vartext)
    {
        return getOption("var", vartext);
    }

    /**
     * Retrieves an option found on an expansion variables "command line".
     *
     * @param optionName[in]      The name of the option to retrieve
     * @param varText[in]         The string containing the value
     * @return The option
     */
    private String getOption(String optionName, String vartext)
    {
        // We're looking for the value between to quotes, directly
        // after the option name. For instance, given this:
        //
        // list="implementation"
        //
        // We want implementation
        
        String option = null;
        int pos = vartext.indexOf(optionName);
        if (pos != -1)
        {
            pos = vartext.indexOf('"', pos);
            if (pos != -1)
            {
                pos++;
                int nextQuote = vartext.indexOf('"', pos);
                if (nextQuote != -1)
                    option = vartext.substring(pos, nextQuote);
            }
        }
        return option;
    }
    
    private String getDelimiter(String vartext)
    {
        return getOption("delimiter", vartext);
    }
    
    private String getLiteral(String vartext)
    {
        return getOption("literal", vartext);
    }
    
    /**
     *
     * Retrieves then pops the current context from this factory
     *
     * @param pVal[out] The context just removed from the factory
     *
     * @return HRESULT
     *
     */
	public IVariableExpander getPopContext()
    {
        return m_Contexts.size() > 0? m_Contexts.pop() : null;
	}

    /**
     *
     * Retrieves the variables used to override default variables, or provide variables
     * that are not part of the default expansion variable configuration file.
     *
     * @param pVal[out]  The collection of variables.
     *
     * @return HRESULT
     *
     */
	public ETList<IExpansionVariable> getOverrideVariables()
    {
        return new ETArrayList<IExpansionVariable>( m_Overrides.values() );
	}

    /**
     *
     * Sets a new batch of overrides on this factory. If 0 is passed, the current
     * collection of override variables is wiped out.
     *
     * @param newVal[in] The collection of vars to use.
     */
	public void setOverrideVariables(ETList<IExpansionVariable> value) {
        m_Overrides.clear();
        if (value != null)
        {
            for (Iterator<IExpansionVariable> iter = value.iterator();
                    iter.hasNext(); )
            {
                addOverride(iter.next());
            }
        }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableFactory#addOverride(org.netbeans.modules.uml.core.generativeframework.IExpansionVariable)
	 */
	public void addOverride(IExpansionVariable var)
    {
        m_Overrides.put( var.getName(), var );
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.generativeframework.IVariableFactory#removeOverride(org.netbeans.modules.uml.core.generativeframework.IExpansionVariable)
	 */
	public void removeOverride(IExpansionVariable var) 
    {
        m_Overrides.remove( var.getName() );
	}
    
    private Stack<IVariableExpander>            m_Contexts = 
                                new Stack<IVariableExpander>();
    
    private String                              m_Config;
    private HashMap<String, IExpansionVariable> m_Overrides =
                                new HashMap<String, IExpansionVariable>();
}
