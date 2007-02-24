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


package org.netbeans.modules.uml.core.configstringframework;

/**
 * @author sumitabhk
 *
 */
public class ConfigStringHelper
{

	private static ConfigStringHelper m_Instance = null;
	private IConfigStringTranslator m_Translator = null;

	/**
	 *
	 */
	private ConfigStringHelper()
	{
		super();
	}

	public static ConfigStringHelper instance()
	{
		if (m_Instance == null)
		{
			m_Instance = new ConfigStringHelper();
		}
		return m_Instance;
	}

	public IConfigStringTranslator getTranslator()
	{
		IConfigStringTranslator retObj = null;
		if (m_Translator != null)
		{
			retObj = m_Translator;
		}
		else
		{
			m_Translator = new ConfigStringTranslator();
			retObj = m_Translator;
		}
		return retObj;
	}
}



