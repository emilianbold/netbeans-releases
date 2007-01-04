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

package org.netbeans.modules.cnd.api.utils;

import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileSystemView;

/**
 *  Replace the default FileSystemView with one which understands tilde and
 *  environment variable expansion. This class should be used to replace the
 *  default FileSystemview in all of our FileChoosers.
 */
public class IpeFileSystemView extends FileSystemView {

    /** The original FileSystemView */
    FileSystemView fsv;


    /**
     *  Save the original FileSystemView. Because of the way swing works its
     *  probably not our superclass.
     */
    public IpeFileSystemView(FileSystemView fsv) {
	this.fsv = fsv;
    }


    /**
     *  Creates a JDK File object from the filename. In our case, we may change
     *  the original filename if it contains either ~ or $.
     */
    public File createFileObject(String path) {
	return new File(IpeUtils.expandPath(path));
    }


    /**
     *  Creates a JDK File object from the filename in the given directory. In
     *  our case, we may change the original filename if it contains either
     *  ~ or $.
     */
    public File createFileObject(File dir, String path) {
	String newPath = IpeUtils.expandPath(path);
	if (dir == null) {
	    return new File(newPath);
	} else {
	    return new File(dir, newPath);
	}
    }

    
    /**
     *  Expand the pathname if there are any '~' or '$' characters in it.
     *
     *  @param path The original path name
     *  @return	    The possibly expanded path name
    private String expandPath(String path) {
	int idx = 0;
	int dol;
	String end;

	if (path.charAt(0) == '~') {
	    if (path.length() == 1 || path.charAt(1) == '/') {
		newPath.append(System.getProperty("user.home"));	//NOI18N
		idx = 1;
	    } else {
		end = path.indexOf('/');
		// XXX - Replace with JNI lookup!!!
		if (end > 0) {
		    newPath.append("/home/");
		    newPath.append(path.substring(1, end));
		    idx = end;
		} else {
		    newPath.append("/home");
		    newPath.append(path.substring(1));
		    idx = path.length();
		}
	    }
	}

	while (idx < path.length()) {
	    var = null;
	    dol = path.indexOf('$', idx);
	    if (dol >= 0) {
		if (env == null
		newPath.append(path.substring(idx), dol);
		if (path.length() > (dol + 2) && path.charAt(dol + 1) == '{' &&
			    (end = path.indexOf(dol, '}')) > 0) {
		    var = path.substring(dol + 2, end1);
		    idx = end1 + 1;
		} else if ((end = path.indexOf('/', idx)) != -1) {
		    var = path.substring(dol + 1, end);
		} else if ((end = path.indexOf('.', idx)) != -1) {
		    var = path.substring(dol + 1, end);
		}
	    } else {
		newPath.append(path.substring(idx));
		idx = path.length();
	    }
    }
     */


    /** Tells if a file is the root directory */
    public boolean isRoot(File f) {
	return fsv.isRoot(f);
    }


    /** Creates a new folder with a default folder name */
    public File createNewFolder(File containingDir) throws IOException {
	return fsv.createNewFolder(containingDir);
    }


    /** Tells if the file is hidden or not */
    public boolean isHiddenFile(File f) {
	return fsv.isHiddenFile(f);
    }


    /** Return the root partitions on this system */
    public File[] getRoots() {
	return fsv.getRoots();
    }
}
