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


package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.generativeframework.IExpansionVariable;
import org.netbeans.modules.uml.core.generativeframework.ITemplateManager;
import org.netbeans.modules.uml.core.generativeframework.IVariableFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;

/**
 * @author sumitabhk
 *
 */
public class CodeGenerationScript implements ICodeGenerationScript
{
    String          m_Name;
    String          m_ScriptFile;
    IDataFormatter  m_pDataFormatter;
    ILanguage       m_rawLanguage;

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ICodeGenerationScript#getName()
	 */
	public String getName()
	{
		return m_Name;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ICodeGenerationScript#setName(java.lang.String)
	 */
	public void setName(String name)
	{
        m_pDataFormatter = null;
        m_Name = name;		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ICodeGenerationScript#getFile()
	 */
	public String getFile()
	{
		return m_ScriptFile;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ICodeGenerationScript#setFile(java.lang.String)
	 */
	public void setFile(String fileName)
	{
        m_pDataFormatter = null;
        m_ScriptFile = fileName;		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ICodeGenerationScript#execute(java.lang.Object)
	 */
	public String execute(IElement pElement)
	{
        if(pElement == null ||
            m_Name == null || m_Name.trim().length() == 0 ||
            m_ScriptFile == null || m_ScriptFile.trim().length() == 0) return null;
            
        if("gt".equals(StringUtilities.getExtension(m_ScriptFile)))
        {
            ICoreProduct pProduct = ProductRetriever.retrieveProduct();
            if(pProduct != null)
            {
                ITemplateManager pMan = pProduct.getTemplateManager();
                if(pMan != null)
                {
                    ETList<IExpansionVariable> pOldOverrides = null;
                    IVariableFactory pFact = pMan.getFactory();
                    if(pFact != null)
                    {
                        // Check the Language this script is owned by to see
                        // if there are any expansion variable overrides to worry 
                        // about

                        if( m_rawLanguage != null)
                        {
                            ETList<IExpansionVariable> pVars = 
                                m_rawLanguage.getExpansionVariables();
                
                        if(pVars != null)
                        {
                            pOldOverrides = pFact.getOverrideVariables();
                            pFact.setOverrideVariables(pVars);
                        }
                        return pMan.expandTemplate(m_ScriptFile, pElement);
                    }
                }
            }
        }
        else
        {
            // Looking for an XSLT script

            // See if we need to create a Data Formatter.
            if( m_pDataFormatter == null)
            {
                m_pDataFormatter = new DataFormatter();
                // ... and initialize it
                m_pDataFormatter.addScript(m_Name, m_ScriptFile);                          
            }
            // We've already created a data formatter and initialized it.
            return m_pDataFormatter.formatElement(pElement, m_Name);                       }
        }
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ICodeGenerationScript#getLanguage()
	 */
	public ILanguage getLanguage()
	{
		return m_rawLanguage;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ICodeGenerationScript#setLanguage(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage)
	 */
	public void setLanguage(ILanguage language)
	{
        m_rawLanguage = language;		
	}

}


