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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

public class ElfExecutableFileFilter extends javax.swing.filechooser.FileFilter {

    private static ElfExecutableFileFilter instance = null;

    public ElfExecutableFileFilter() {
	super();
    }

    public static ElfExecutableFileFilter getInstance() {
	if (instance == null)
	    instance = new ElfExecutableFileFilter();
	return instance;
    }
    
    public String getDescription() {
	return(getString("FILECHOOSER_ELFEXECUTABLE_FILEFILTER")); // NOI18N
    }
    
    public boolean accept(File f) {
	if(f != null) {
	    if(f.isDirectory()) {
		return true;
	    }
	    return checkElfHeader(f);
	}
	return false;
    }

    /** Check if this file's header represents an elf executable */
    private boolean checkElfHeader(File f) {
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

	short e_type;
	if (b[5] == 1) {  // ELFDATA2LSB=1
	    // byte order on i386
	    e_type = (short) b[17];
	    e_type <<= 8;
	    e_type += (short) b[16];
	} else {
	    e_type = (short) b[16];
	    e_type <<= 8;
	    e_type += (short) b[17];
	}
	if (
	    // Elf header	    
	    (b[0] == 0x7f) && (b[1] == (byte) 'E') &&
	    (b[2] == (byte) 'L') && (b[3] == (byte) 'F') &&
	    // Executable
	    (e_type == 2) // ET_EXEC=2
	    ) {
	    return true;
	}
	return false;
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(ElfExecutableFileFilter.class);
	}
	return bundle.getString(s);
    }
}
