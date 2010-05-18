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
