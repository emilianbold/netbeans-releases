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



package org.netbeans.modules.uml.ui.support;

import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog;
import org.netbeans.modules.uml.ui.support.messaging.IMessenger;

/**
 * @author sumitabhk
 *
 *
 */
public class SwingPreferenceControlledDialog implements IPreferenceControlledDialog
{
	private boolean m_AutoUpdatePreference = true;
	private boolean m_RunSilent = false;
	private String  m_PrefKey = "";
	private String  m_PrefPath = "";
	private String  m_PrefName = "";
	/**
	 * 
	 */
	public SwingPreferenceControlledDialog()
	{
		super();
		
		IMessenger pMsg = ProductHelper.getMessenger();
		if (pMsg != null)
		{
			m_RunSilent = pMsg.getDisableMessaging();
		}
	}

	/**
	 *
	 * Sets whether the preference file should be updated when the PreferenceValue 
	 * is set.
	 *
	 * @param bVal[in]
	 *
	 * @return 
	 *
	 */
	public void setAutoUpdatePreference(boolean value)
	{
		m_AutoUpdatePreference = value;
	}

	/**
	 *
	 * Gets whether the preference file should be updated when the Preference Value
	 * is set.
	 *
	 * @param bVal[out]
	 *
	 * @return 
	 *
	 */
	public boolean getAutoUpdatePreference()
	{
		return m_AutoUpdatePreference;
	}

	/**
	 *
	 * Set the preference key.  If no key is specified, Default is assumed.
	 *
	 * @param sVal[in]
	 *
	 * @return 
	 *
	 */
	public void setPrefKey(String value)
	{
		m_PrefKey = value;
	}

	/**
	 *
	 * Gets the preference key.  If no key is specified, Default is assumed.
	 *
	 * @param sVal[out]
	 *
	 * @return 
	 *
	 */
	public String getPrefKey()
	{
		return m_PrefKey;
	}

	/**
	 *
	 * Set the preference path.  The path is the part between the key
	 * and the name.
	 *
	 * @param sVal[in]
	 *
	 * @return 
	 *
	 */
	public void setPrefPath(String value)
	{
		m_PrefPath = value;
	}

	/**
	 *
	 * Get the preference path.  The path is the part between the key
	 * and the name.
	 *
	 * @param sVal[out]
	 *
	 * @return 
	 *
	 */
	public String getPrefPath()
	{
		return m_PrefPath;
	}

	/**
	 *
	 * Set the preference name.
	 *
	 * @param sVal[in]
	 *
	 * @return 
	 *
	 */
	public void setPrefName(String value)
	{
		m_PrefName = value;
	}

	/**
	 *
	 * Get the preference name.
	 *
	 * @param sVal[out]
	 *
	 * @return 
	 *
	 */
	public String getPrefName()
	{
		return m_PrefName;
	}

	/**
	 *
	 * Set the preference vlaue from the preference manager.  If
	 * AutoUpdatePreference is False, setting this property
	 * does nothing.
	 *
	 * @param sVal[in]
	 *
	 * @return 
	 *
	 */
	public void setPreferenceValue(String value)
	{
		boolean autoUpdate = getAutoUpdatePreference();
		if (autoUpdate)
		{
			IPreferenceManager2 pManager = ProductHelper.getPreferenceManager();
			if (pManager != null)
			{
				pManager.setPreferenceValue(m_PrefKey, m_PrefPath, m_PrefName, value);
			}
		}
	}

	/**
	 *
	 * Get the preference vlaue from the preference manager.  If
	 * AutoUpdatePreference is False, setting this property
	 * does nothing.
	 *
	 * @param sVal[out]
	 *
	 * @return 
	 *
	 */
	public String getPreferenceValue()
	{
		String sVal = "";
		IPreferenceManager2 pManager = ProductHelper.getPreferenceManager();
		if (pManager != null)
		{
			sVal = pManager.getPreferenceValue(m_PrefKey, m_PrefPath, m_PrefName);
		}
		return sVal;
	}

	/**
	 *
	 * Set the preference vlaue from the preference manager.  If
	 * AutoUpdatePreference is False, setting this property
	 * does nothing.
	 *
	 * @param sKey[in]
	 * @param sPath[in]
	 * @param sName[in]
	 * @param bAutoUpdatePreference[in]
	 *
	 * @return 
	 *
	 */
	public long preferenceInformation(String sPreferenceKey, String sPreferencePath, String sPreferenceName, boolean bAutoUpdatePreference)
	{
		setPrefKey(sPreferenceKey);
		setPrefPath(sPreferencePath);
		setPrefName(sPreferenceName);
		setAutoUpdatePreference(bAutoUpdatePreference);
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.commondialogs.ISilentDialog#isRunSilent()
	 */
	public boolean isRunSilent()
	{
		boolean bSilent = false;
		if (m_RunSilent)
		{
			bSilent = true;
		}
		return bSilent;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.commondialogs.ISilentDialog#setIsRunSilent(boolean)
	 */
	public void setIsRunSilent(boolean value)
	{
		m_RunSilent = value;
	}

}

