/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
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

	private java.util.HashMap hash = new java.util.HashMap();
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
		Entry e = (Entry)hash.get(fileName);
		if (e == null ) {
			return null;
		} else {
			return e.getRevno();
			}
	}
}
