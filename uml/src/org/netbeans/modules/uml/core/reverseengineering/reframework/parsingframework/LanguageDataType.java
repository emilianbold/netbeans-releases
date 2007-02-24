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

/**
 * @author sumitabhk
 *
 */
public class LanguageDataType implements ILanguageDataType
{
	private String m_Scope = "";
	private int m_Kind = 0;
	private String m_Name = "";
	private String m_DefaultValue = "";
	private boolean m_IsAttributeType = false;
	private boolean m_IsOperationType = false;
	private String m_UMLName = "";


	/**
	 * Gets the name of the data type.
	 *
	 * @param pVal [out] The name of the data type.
	 */
	public String getName()
	{
		return m_Name;
	}

	/** 
	 * Sets the name of the data type.
	 * 
	 * @param newVal [in] The name of the data type.
	 */
	public void setName(String newVal)
	{
		m_Name = newVal;
	}

	/**
	 * Gets the data type's kind.  The data kinds will usally be 
	 * <B>primitive</B> or <B>user-defined</B>.
	 *
	 * @param pVal [out] The data type kind
	 */
	public int getKind()
	{
		return m_Kind;
	}

	/**
	 * Sets the data type's kind.  The data kinds will usally be 
	 * <B>primitive</B> or <B>user-defined</B>.
	 *
	 * @param pVal [out] The data type kind
	 */
	public void setKind(int newVal)
	{
		m_Kind = newVal;
	}

	/**
	 * Gets the scope of the data type.  The value of scope will be <B>global</B> when
	 * the data type applies to all Describe projects, or a list of projects that 
	 * applies to the data type.  
	 *
	 * @param pVal [out] The scope of the data type
	 */
	public String getScope()
	{
		return m_Scope;
	}

	/**
	 * Sets the scope of the data type.  The value of scope will be <B>global</B> when
	 * the data type applies to all Describe projects, or a list of projects that 
	 * applies to the data type.  
	 *
	 * @param pVal [out] The scope of the data type
	 */
	public void setScope(String newVal)
	{
		m_Scope = newVal;
	}

	/**
	 * Specifies whether or not the Data Type is the default value for attributes.
	 *
	 * @param pVal[out] true if the data type is the default type for attributes
	 */
	public boolean getIsDefaultAttributeType()
	{
		return m_IsAttributeType;
	}

	/**
	 * Specifies whether or not the Data Type is the default value for attributes.
	 *
	 * @param newVal[in] true if the data type is the default type for attributes.
	 */
	public void setIsDefaultAttributeType(boolean newVal)
	{
		m_IsAttributeType = newVal;
	}

	/**
	 * Specifies whether or not the Data Type is the default value for operations.
	 *
	 * @param pVal[out] true if the data type is the default type for operations
	 */
	public boolean getIsOperationDefaultType()
	{
		return m_IsOperationType;
	}

	/**
	 * Specifies whether or not the Data Type is the default value for operations.
	 *
	 * @param newVal[in] true if the data type is the default type for operations
	 */
	public void setIsOperationDefaultType(boolean newVal)
	{
		m_IsOperationType = newVal;
	}

	/** 
	 * Gets the DefaultValue of the data type.
	 * 
	 * @param pVal [out] The DefaultValue of the data type
	 */
	public String getDefaultValue()
	{
		return m_DefaultValue;
	}

	/** 
	 * Sets the DefaultValue of the data type.
	 * 
	 * @param newVal [in] The DefaultValue of the data type
	 */
	public void setDefaultValue(String newVal)
	{
		m_DefaultValue = newVal;
	}

	/** 
	 * Retrieves the language independent name of the data type.  Only primitives 
	 * have a UML name.
	 *
	 * @param pVal [out] The name.
	 */
	public String getUMLName()
	{
		return m_UMLName;
	}

	/** 
	 * Set the language independent name of the data type.  Only primitives 
	 * have a UML name.
	 *
	 * @param newVal [in] The name.
	 */
	public void setUMLName(String newVal)
	{
		m_UMLName = newVal;
	}
}


