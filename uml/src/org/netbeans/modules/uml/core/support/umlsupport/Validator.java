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

package org.netbeans.modules.uml.core.support.umlsupport;

import java.io.File;

import org.dom4j.Document;
import java.util.List;

/**
 * @author sumitabhk
 *
 */
public class Validator {
	/**
	 * Makes sure that the passed in path contains a valid directory
	 * spec
	 *
	 * @param path An absolute path. If there is a filename, it is handled.
	 * @return true if the path is a valid path, Otherwise false.
	 */

	public static boolean validatePath( final String path )
	{
		boolean retVal = true;

		if( path.length() > 0 )
		{
			File testFile = new File(path);
            if (!testFile.isAbsolute()) testFile = testFile.getAbsoluteFile();
            
            if (testFile == null)
                retVal = false;
            else if (!testFile.isDirectory())
			{
				File dir = testFile.getParentFile();
				if (dir != null)
				{
					if (dir.exists() && !dir.isDirectory())
				{
					retVal = false;
				}
					if (!dir.exists())
					{
						dir.mkdir();
					}
				}
			}
		}
		else
		{
			retVal = false;
		}

		return retVal;
	}


	/**
	 * @param config
	 * @param string
	 * @return
	 */
	public static boolean verifyFileExists(String config) {
		File f = new File(config);
		return f.exists();
	}

	/**
	 *
	 * Makes sure that the file passed is an XML file that contains an EMBT:Workspace
	 * root element.
	 *
	 * @param fileName[in] The absolute path to the file to test.
	 * @param query[in] The XPath query to perform on the file
	 * @param id[in] The IID of the interface making this call
	 * @param doc[out] The validated document.
	 * @param validateOnParse[in] true to tell the XML document to Validate during
	 *                            the parsing phase ( the default ), else false to
	 *                            to not validate during the parse.
	 *
	 * @return S_OK, else USR_E_INVALID_FORMAT if the is an XML using wrong tags, else a file
	 *         that is not even an XML file.
	 */
	public static Document verifyXMLFileFormat(String fileName, String query) 
	{
		Document doc = null;
		try {
			doc = XMLManip.getDOMDocument(fileName);
			List list = doc.selectNodes(query);
			if (list != null && list.size()>0)
			{
				//good everything is fine.
			}
			else
			{
				//wrong doc is specified
				doc = null;
			}
		}catch (Exception e)
		{
		}
		return doc;
	}

}


