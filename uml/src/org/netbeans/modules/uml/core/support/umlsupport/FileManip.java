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

package org.netbeans.modules.uml.core.support.umlsupport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;

/**
 * @author sumitabhk
 *
 */
public class FileManip {

	/**
	 *
	 */
	public FileManip() {
		super();
	}

	/**
	 *
	 * Resolves any sub strings in the passed in string that begin with '%' and
	 * end with '%'. The string between the asterixes must be found in the preference
	 * file, else filePath is returned
	 *
	 * @param filePath[in]  The string to check for variables
	 *
	 * @return The resolved string
	 *
	 */
	public static String resolveVariableExpansion(String filePath) {
		String finalPath = filePath;
		if (filePath.length() > 0)
		{
			int pos = filePath.indexOf('%');
			if (pos >= 0)
			{
				int lastPos = filePath.indexOf('%', pos + 1);
				if (lastPos >= 0)
				{
					String possibleVar = filePath.substring(pos, lastPos);
					if (possibleVar.length() > 0)
					{
						IPreferenceAccessor pref = PreferenceAccessor.instance();
						String theValue = pref.getExpansionVariable(possibleVar);
						String var = "%" + possibleVar + "%";
						
						finalPath = StringUtilities.replaceSubString(finalPath, var, theValue);
					}
				}
			}
		}
		return finalPath;
	}

	/**
	 *
	 * Determines whether or not the file pointed to contains the passed-in
	 * string anywhere in the file.
	 *
	 * @param fileName[in]  The file to crack open and search
	 * @param strToFind[in] The string to search for.
	 *
	 * @return true if strToFind was found in the passed in file, else false
	 *
	 */
	public static boolean isInFile(String xmiID, String fileName)
	{
		boolean isFound = false;
		try
		{
			File file = new File(fileName);
			if (file.exists())
			{
				FileReader fileReader = new FileReader(file);
				BufferedReader reader = new BufferedReader(fileReader);
				StringBuffer readStr = new StringBuffer();
				while (true)
				{
					String str = reader.readLine();
					if (str != null)
					{
						readStr.append(str);
					}
					else
					{
						break;
					}
				}
				String readString = readStr.toString();
				if (readString.indexOf(xmiID) >= 0)
				{
					isFound = true;
				}
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return isFound;
	}

}



