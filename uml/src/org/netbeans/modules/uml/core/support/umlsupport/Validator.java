/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.io.File;

import java.io.IOException;
import org.dom4j.Document;
import java.util.List;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;

/**
 * @author sumitabhk
 *
 */
public class Validator {
        private static final Logger logger = Logger.getLogger("org.netbeans.modules.uml.core");

	/**
	 * Makes sure that the passed in path contains a valid directory
	 * spec
	 *
	 * @param path An absolute path. If there is a filename, it is handled.
	 * @return true if the path is a valid path, Otherwise false.
	 */
        public static boolean validatePath(final String path) 
        {
            boolean retVal = true;
            if (path == null || path.trim().length() == 0)
            if (path.length() > 0) 
            {
                File testFile = new File(path);
                if (!testFile.isAbsolute()) 
                {
                    testFile = testFile.getAbsoluteFile();
                }

                if (testFile == null) 
                {
                    retVal = false;
                } else if (!testFile.isDirectory()) 
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
                            try {
                                //dir.mkdir();
                                FileUtil.createFolder(dir);
                            } catch (IOException ex) {
                                retVal = false;
                                logger.warning(ex.getMessage());
                            }
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


