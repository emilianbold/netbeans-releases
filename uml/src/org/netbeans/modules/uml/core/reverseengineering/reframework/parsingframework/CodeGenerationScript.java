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


