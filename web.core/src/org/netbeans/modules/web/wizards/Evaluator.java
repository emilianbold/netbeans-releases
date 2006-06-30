/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.web.wizards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;

/**
* Generic methods for evaluating the input into the wizards.
*
* @author Ana von Klopp
*/

abstract class Evaluator {

    private FileType fileType = null;
    private static final boolean debug=false;

    Evaluator(FileType fileType) { 
	this.fileType = fileType; 
    }

    abstract boolean isValid(); 
    abstract String getTargetPath(); 
    abstract String getErrorMessage(); 
    abstract Iterator getPathItems(); 
    abstract void setInitialFolder(DataFolder df, Project project);
    
    FileType getFileType() { 
	return fileType; 
    }

    /**
     * Returns the absolute path to the target class. Used by the
     * wizards to display the result of the selections. 
     */

    String getTargetPath(Iterator pathItems) { 
        
        StringBuffer buffer = new StringBuffer();
	while(pathItems.hasNext()) { 
	    buffer.append((String)(pathItems.next())); 
	    if(pathItems.hasNext())
		buffer.append(File.separator); 
	} 
	buffer.append("."); //NOI18N
	buffer.append(fileType.getSuffix()); 
	return buffer.toString(); 
    } 

    void checkFile(Iterator pathItems, FileObject root) throws IOException { 
	if(debug) log("::checkFile() "+root); //NOI18N
        
	String pathItem; 
        FileObject fo = root;
        
	while(pathItems.hasNext()) { 

	    // We're good! 

	    pathItem = (String)(pathItems.next()); 
	    if(debug) log("\tpath item is " + pathItem); //NOI18N

	    // Path item is a directory, check that we can get it
	    if(pathItems.hasNext()) {
		if(debug) log("\tNot the last one"); //NOI18N
		try { 
		    fo = fo.getFileObject(pathItem, null);
		}
		catch(IllegalArgumentException iaex) {
		    throw new IOException(NbBundle.getMessage(Evaluator.class, 
							      "MSG_clash_path", 
							      pathItem)); 
		} 
                if(debug) log("\tfo="+fo); //NOI18N
		// We're good! 
		if(fo == null) return; 

		if(debug)  
		    log("\tgot next file object " + fo.getPath()); //NOI18N
			     
		if(!fo.isFolder()) { 
		    if(debug) log("\tNot a folder"); //NOI18N
		    throw new IOException(NbBundle.getMessage(Evaluator.class, 
							      "MSG_clash_path", 
							      pathItem)); 
					 
		}
	    }
	    else { 
		if(debug) log("\tThis is the last one"); //NOI18N
		try { 
		    fo = fo.getFileObject(pathItem, fileType.getSuffix()); 
		}
		catch(IllegalArgumentException iaex) { 
		    throw new IOException(NbBundle.getMessage(Evaluator.class, 
							      "MSG_clash_path", 
							      pathItem)); 
		} 
		if(fo == null) return; 
		if(fo.isData())  
		    throw new IOException(NbBundle.getMessage(Evaluator.class, 
							      "MSG_file_exists", 
							      pathItem)); 
	    } 
	}
	if(debug) log("\tAt end of checkFile()"); //NOI18N
    }
    
    private static void log(String s) { 
	System.out.println("Evaluator" + s); 
    } 

}

