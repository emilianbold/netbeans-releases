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
import org.openide.util.NbBundle;

public abstract class SourceFileFilter extends javax.swing.filechooser.FileFilter {

    public SourceFileFilter() {
        super();
    }

    public boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory())
                return true;
            int index = f.getName().lastIndexOf('.');
            if (index >= 0) {
                // Match suffix
                String suffix = f.getName().substring(index+1);
                if (amongSuffixes(suffix, getSuffixes()))
                    return true;
            }
            else {
                // Match entire name
                if (amongSuffixes(f.getName(), getSuffixes()))
                    return true;
            }
        }
        return false;
    }
    
    public abstract String[] getSuffixes();
    
    public String getSuffixesAsString() {
        String ret = ""; // NOI18N
        String space = ""; // NOI18N
        for (int i = 0; i < getSuffixes().length; i++) {
            ret = ret + space + "." + getSuffixes()[i]; // NOI18N
            space = " "; // NOI18N
        }
        return ret;
    }
                    
    private boolean amongSuffixes(String suffix, String[] suffixes) {
	for (int i = 0; i < suffixes.length; i++) {
	    if (suffixes[i].equals(suffix))
		return true;
	}
	return false;
    }
    
    public String toString() {
        return getDescription();
    }
}
