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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

public class MacOSXExecutableFileFilter extends javax.swing.filechooser.FileFilter {

    private static MacOSXExecutableFileFilter instance = null;

    public MacOSXExecutableFileFilter() {
	super();
    }

    public static MacOSXExecutableFileFilter getInstance() {
	if (instance == null)
	    instance = new MacOSXExecutableFileFilter();
	return instance;
    }
    
    public String getDescription() {
	return(getString("FILECHOOSER_MACHOEXECUTABLE_FILEFILTER")); // NOI18N
    }
    
    public boolean accept(File f) {
	if(f != null) {
	    if(f.isDirectory()) {
		return true;
	    }
	    return checkHeader(f);
	}
	return false;
    }

    /** Check if this file's header represents an elf executable */
    private boolean checkHeader(File f) {
        byte b[] = new byte[18];
	int left = 18; // bytes left to read
	int offset = 0; // offset into b array
	InputStream is = null;
	try {
	    is = new FileInputStream(f);
	    while (left > 0) {
		int n = is.read(b, offset, left);
		if (n <= 0) {
		    // File isn't big enough to be an elf file...
		    return false;
		}
		offset += n;
		left -= n;
	    }
	} catch (Exception e) {
	    return false;
	} finally {
	    if (is != null) {
		try {
		    is.close();
		} catch (IOException e) {
		}
	    }
	}

        // FIXUP: not sure exactly how to check for executable on Mac OS X (Mach-O)
        if (b[0] == -50 &&
            b[1] == -6 &&
            b[2] == -19 &&
            b[3] == -2 &&
            b[12] == 2)
            return true;
        else
            return false;
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(MacOSXExecutableFileFilter.class);
	}
	return bundle.getString(s);
    }
}
