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
 * Created on Jan 23, 2004
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.uml.core.reverseengineering.reintegration;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ParsingContext 
{
	private Node m_Package = null;
	private ETList<Node> m_Dependencies = new ETArrayList<Node>();
	private ETList<Node> m_Classes = new ETArrayList<Node>();
	private String m_FileName = null;
	private ILanguage m_Language = null;
	
	public ParsingContext(String fileName)
	{
		m_FileName = fileName;
	}
	
	public void addDependency(Node dep)
	{
		m_Dependencies.add(dep);
	}

	public void addPackage(Node pack)
	{
		m_Package = pack;
	}

	public void addClass(Node clazz)
	{
		m_Classes.add(clazz);
	}
	
	public Node getPackage()
	{
		return m_Package;
	}

	/**
	 * Retrieves the language of the file that is being processed.
	 *
	 * @param pLanguage [out] The language of the current context
	 */
	public ILanguage getLanguage ()
	{
		return m_Language;
	}

	/**
	 * Sets the language of the file that is being processed.
	 *
	 * @param pLanguage [in] The language of the current context
	 */
	public void setLanguage (ILanguage pLanguage)
	{
		m_Language = pLanguage;
	}
	
	public ETList<Node> getDependencies()
	{
		return m_Dependencies;
	}
	
	public ETList<Node> getClasses()
	{
		return m_Classes;
	}
	
	public String getFileName()
	{
		return m_FileName;
	}
	
	public void setFileName(String fileName)
	{
		m_FileName = fileName;
	}
}


