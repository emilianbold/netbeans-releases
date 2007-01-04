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
import java.util.Enumeration;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.loaders.ShellDataLoader;
import org.openide.loaders.ExtensionList;
import org.openide.util.NbBundle;

public class ShellFileFilter extends javax.swing.filechooser.FileFilter {

    private static ShellFileFilter instance = null;

    public ShellFileFilter() {
	super();
    }

    public static ShellFileFilter getInstance() {
	if (instance == null)
	    instance = new ShellFileFilter();
	return instance;
    }
    
    public String getDescription() {
	return(getString("FILECHOOSER_SHELL_FILEFILTER")); // NOI18N
    }
    
    public boolean accept(File f) {
	if (f != null) {
	    if (f.isDirectory()) {
		return true;
	    }
	    if (checkExtension(f))
		return true;
	    if (checkFirstFewBytes(f))
		return true;
	}
	return false;
    }

    private boolean checkExtension(File f) {
	// recognize shell scripts by extension
	String fname = f.getName();
	String ext = null;
	int i = fname.lastIndexOf('.');
	if (i > 0)
	    ext = fname.substring(i+1);

	ExtensionList extensions = ShellDataLoader.getInstance().getExtensions();
	for (Enumeration e = extensions.extensions(); e != null &&  e.hasMoreElements();) {
	    String ex = (String) e.nextElement();
	    if (ex != null && ex.equals(ext))
		return true;
	}

	return false;
    }

    /** Check if this file's header represents an elf executable */
    private boolean checkFirstFewBytes(File f) {
        byte b[] = new byte[2];
	InputStream is = null;
	try {
	    is = new FileInputStream(f);
	    int n = is.read(b, 0, 2);
	    if (n < 2) {
	        // File isn't big enough ...
		return false;
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
	if (b[0] == '#' && b[1] == '!')
	    return true;

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
