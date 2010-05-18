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



package org.netbeans.modules.uml.ui.support;

import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceControlledDialog;
import org.netbeans.modules.uml.ui.support.messaging.IMessenger;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.util.NbPreferences;

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
		    NbPreferences.forModule(DummyCorePreference.class).put(m_PrefName, value) ;
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
	public String getPreferenceValue() {

            //kris richards - set to use NbPreferences
            return NbPreferences.forModule(DummyCorePreference.class).get(m_PrefName,"") ;
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

