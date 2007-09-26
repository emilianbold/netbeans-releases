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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/************************************************************************

        FILENAME: CvsEntries.java

        AUTHOR: Erica Grevemeyer      DATE: Jan  7 11:21:09 PST 2002

************************************************************************/
package org.netbeans.nbbuild.utils.cvsutils;

import java.io.*;
import java.util.*;

/** Read a specified CVS/Entries style file,
 * as Entries & store the collection of Entries
 * in the passed-in Hashtable.  If no 'separator' string
 * is passed then a 'blankspace' separator is assumed.
 *
 * @author Erica Grevemeyer
 * @version  1.1	Jan  7 11:21:09 PST 200
 */

public class CvsEntries {

	private java.util.HashMap<String, Entry> hash = new java.util.HashMap<String, Entry>();
	private static final String DEFAULT_CVS_ENTRIES_FILENAME = "CVS/Entries";

	// Constructors 
	public CvsEntries(String directoryPath) {
            this(directoryPath, DEFAULT_CVS_ENTRIES_FILENAME);
	}
	

	public CvsEntries(String directoryPath, String fileName) {
		try {
			String entriesFile = directoryPath + "/" + fileName;
			BufferedReader inBuff = new BufferedReader(new FileReader(new File(entriesFile)));
			Entry e;
			
			String line;

			while ((line = inBuff.readLine()) != null) {
				e = new Entry(line);
				if ( ! e.getFiletype().equals("D") ) {
					hash.put(e.getFilename(), e);
				}
			}
		
		} catch(IOException e) {
			System.out.println("Error: " + e.toString());	
		}
	}
        
	public String getRevnoByFileName(String fileName) {
		Entry e = hash.get(fileName);
		if (e == null ) {
			return null;
		} else {
			return e.getRevno();
			}
	}
}
