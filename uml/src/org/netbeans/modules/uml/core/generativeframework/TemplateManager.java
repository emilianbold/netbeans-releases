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

import java.io.File;
import java.io.FileReader;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 * @author sumitabhk
 */
public class TemplateManager implements ITemplateManager{

	private IVariableFactory m_Factory = null;
	private String m_WorkingDirectory = null;

	/**
	 *
	 * Retrieves the CoreProduct, attempting to set the default Config
	 * location for the Expansion variable .etc file
	 *
	 */
	public TemplateManager() {
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IConfigManager conMan = prod.getConfigManager();
			if (conMan != null)
			{
				String configLoc = conMan.getDefaultConfigLocation();
				configLoc += "ExpansionVar.etc";
				setConfigLocation(configLoc);
			}
		}
	}

	/**
	 *
	 * Expands the template located in the passed in file, using contextElement as
	 * the element from which to expand variables from.
	 *
	 * @param templateFile[in]    The absolute location of the Generative template
	 * @param contextElement[in]  The element to base queries on.
	 * @param results[out]        The expand results of the template
	 *
	 * @return HRESULT
	 *
	 */
	public String expandTemplate(String templateFile, IElement contextElement)
    {
		String result = null;
		String templateBuffer = retrieveTemplateBuffer(templateFile);
		ExpansionVarLocator loc = 
            new ExpansionVarLocator(
                this, templateFile, templateBuffer, contextElement);
		result = loc.expandVars();
		return result;
	}

	/**
	 *
	 * Retrieves the buffer that contains the entire contents of the template file
	 *
	 * @param fileName[in]  The absolute path to the template file
	 *
	 * @return The filled in buffer. Empty if an error occurred.
	 *
	 */
	private String retrieveTemplateBuffer(String fileName) 
    {
		String templateFileName = establishPath(fileName);
		if (templateFileName != null && templateFileName.length() > 0)
		{
			File f = new File(templateFileName);
			FileReader reader = null;
            StringBuffer buf = new StringBuffer();
			try
            {
                reader = new FileReader(f);
                char[] cbuf = new char[1000];
                int size = 0;
                while ( (size = reader.read(cbuf)) != -1)
                    buf.append(cbuf, 0, size);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            } 
            return buf.toString(); 
		}
		return null;
	}

	/**
	 *
	 * Makes sure that the template file name specified is made absolute.
	 *
	 * @param fileName[in]           The incoming template file name that may or
	 *                               may not be absolute.
	 * @param templateFileName[out]  The final, absolute path to the template file name,
	 *                               else "" if not able to ascertain
	 *
	 * @return HRESULT
	 * @note If fileName is absolute, the WorkingDirectory property will be set based
	 *       on the directory that the file is in. If fileName is relative, the absolute path
	 *       will be generated from the WorkingDirectory property. If WorkingDirectory is empty,
	 *       TMR_E_NOT_WORKING_DIRECTORY will be returned.
	 *
	 */
	private String establishPath(String fileName) {
        File f;
        if (!(f = new File(fileName)).isAbsolute())
        {
            if (m_WorkingDirectory != null)
                fileName = new File(m_WorkingDirectory, fileName).toString();
            else
            {
                f                   = f.getAbsoluteFile();
                fileName            = f.toString();
                m_WorkingDirectory  = f.getParent();
            }
        }
        else
        {
            m_WorkingDirectory = f.getParent();
        }
		return fileName;
	}

	/**
	 *
	 * Expands a template located at templateFile, using contextNode as the node
	 * for initial query context.
	 *
	 * @param templateFile[in] The location of the template file. Must be absolute, or relative
	 *                         to the absolute file location specified in WorkingDirectory.
	 * @param contextNode[in]  The node to use as the data source
	 * @param results[out]     Results of the expansion
	 *
	 * @return HRESULT
	 *
	 */
	public String expandTemplateWithNode(String templateFile, Node contextNode) {
		String result = null;
		String templateBuffer = retrieveTemplateBuffer(templateFile);
		ExpansionVarLocator loc = 
            new ExpansionVarLocator(
                this, templateFile, templateBuffer, contextNode);
		result = loc.expandVars();
		return result;
	}
    
    public IVariableExpander getVariableExpander()
    {
        return m_Factory != null? m_Factory.getExecutionContext() : null;
    }

	/**
	 *
	 * Retrieves the location of the configuration file that houses the 
	 * expansion variable definitions
	 *
	 * @param pVal[out] The current value.
	 *
	 * @return HRESULT
	 *
	 */
	public String getConfigLocation() {
		if (m_Factory != null)
		{
			return m_Factory.getConfigFile();
		}
		return null;
	}

	/**
	 *
	 * Sets the location of the configration file used for 
	 * expansion variable definitions
	 *
	 * @param newVal[in] The location of the config file.
	 *
	 * @return HRESULT
	 *
	 */
	public void setConfigLocation(String value) {
		if (m_Factory == null)
		{
			m_Factory = new VariableFactory();
		}
		if (m_Factory != null)
		{
			m_Factory.setConfigFile(value);
		}
		setWorkingDirectory(value);
	}

	public IVariableFactory getFactory() {
		return m_Factory;
	}

	public void setFactory(IVariableFactory value) {
		m_Factory = value;
	}

	public String getWorkingDirectory() {
		return m_WorkingDirectory;
	}

	/**
	 *
	 * Sets the working directory that will be used to resolve relative
	 * template file names.
	 *
	 * @param newVal[in] The absolute path to the working directory. An
	 *                   absolute path to a file can be passed. The directory
	 *                   that file lives in will be used to set the WorkingDirectory
	 *                   property.
	 *
	 * @return HRESULT
	 *
	 */
	public void setWorkingDirectory(String value) {
		//make sure that this is a direcotry
		m_WorkingDirectory = value;
	}
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.ITemplateManager#createExecutionContext()
     */
    public IVariableExpander createExecutionContext()
    {
        String config = getConfigLocation();
        IVariableExpander newContext = new VariableExpander();
        newContext.setConfigFile(config);
        newContext.setManager(this);
        
        return newContext;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.ITemplateManager#expandVariable(java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public String expandVariable(String varName, IElement contextElement)
    {
        if(contextElement == null) return null;
        return expandVariableWithNode(varName, contextElement.getNode());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.generativeframework.ITemplateManager#expandVariableWithNode(java.lang.String, org.dom4j.Node)
     */
    public String expandVariableWithNode(String varName, Node contextNode)
    {
        String results = null;

        if(m_Factory != null)
        {
            IVariableExpander expander = createExecutionContext();
            
            if(expander == null) 
                throw new IllegalStateException(
                        "Execution context does not exist");

            m_Factory.setExecutionContext(expander);

            IExpansionVariable var = m_Factory.createVariableWithText(varName);
            if(var != null)
            {
                results = var.expand(contextNode);
            }
            expander = m_Factory.getPopContext();
        }
        return results;
    }
}
