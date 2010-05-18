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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */


package org.netbeans.lib.j2ee.sun.persistence.utility.openide;

import java.net.URL;
import javax.help.HelpSet;

import org.openide.util.HelpCtx;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileUtil;

/*
 * HelpUtils.java
 *
 * Created on July 28, 2000.
 *
 */

/**
  Holds the static utility methods used by the Help subcomponent 
  of the Persistence module.
 */

public class HelpUtils
{
 	/**
	  * Get context help id associated with an object.
	  *
	  * @return the help id.
	  * @param  obj		The object for which the help id is required.
	  */
	public static String getHelpID (Object obj)
	{
		return ((obj instanceof String) ? 
			(String)obj : obj.getClass().getName());
	}	

 	/**
	  * Get context help associated with an object.
	  *
	  * @return the help context object.
	  * @param  obj		The object for which the help context is required.
	  */
	public static HelpCtx getHelpCtx (Object obj)
	{
		return new HelpCtx(getHelpID(obj));
	}

	public static HelpSet getHelpSet (String xmlFileName, String urlString)
	{
		try
		{
			FileObject helpFile = FileUtil.getConfigFile(xmlFileName);

			// If we are in the IDE environment, the help set 
			// has already been loaded, so we only access it here
			// by looking it up using the xml layer.
			if (helpFile != null)
			{
				DataObject dataObject = DataObject.find(helpFile);

				if (dataObject != null)
				{
					Object instance = 
						dataObject.getCookie(InstanceCookie.class);

					if (instance != null)
					{
						return (HelpSet)((InstanceCookie)instance).
							instanceCreate();
					}
				}
			}
			// We are in the standalone environment and need to use
			// HelpSet apis to access the help set.
			else
			{
				ClassLoader classLoader = HelpUtils.class.getClassLoader();
				URL url = HelpSet.findHelpSet(classLoader, urlString);

				if (url != null)
					return new HelpSet(classLoader, url);
			}
		}
		catch (Exception e)
		{
			//	do nothing - will return null
		}

		return null;
	}

	public static boolean isValidHelpID (String key, HelpSet helpSet)
	{
		return ((helpSet == null) ? false :
			helpSet.getCombinedMap().isValidID(key, helpSet));
	}
}

