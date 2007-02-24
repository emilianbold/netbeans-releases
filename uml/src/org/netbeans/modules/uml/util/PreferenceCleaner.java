package org.netbeans.modules.uml.util;
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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ConfigManager;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceManager;

public class PreferenceCleaner
{
	public PreferenceCleaner()
	{
	}

	// main method and entry point for app
	public static void main(String[] args)
	{
		PreferenceCleaner pc = new PreferenceCleaner();
		if (pc != null)
		{
			IConfigManager configMgr = new ConfigManager();
			if (configMgr != null)
			{
				IPreferenceManager2 prefMgr = new PreferenceManager();
				if (prefMgr != null)
				{
					String configLoc = configMgr.getDefaultConfigLocation();
					if (configLoc != null && configLoc.length() > 0)
					{
						String prefFile = configLoc + "PreferenceProperties.etc";
						prefMgr.registerFile(prefFile);
						prefMgr.restoreForInstall();
						prefMgr.save();
					}
				}
			}
		}

	}

}