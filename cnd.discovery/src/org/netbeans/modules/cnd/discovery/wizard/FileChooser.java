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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileChooser extends JFileChooser {
    private static File currectChooserFile = null;
    public FileChooser(String titleText, String buttonText, int mode, boolean multiSelection,
            FileFilter[] filters, String feed, boolean useParent) {
	super();
	setFileHidingEnabled(false);
	setFileSelectionMode(mode);
        setMultiSelectionEnabled(multiSelection);
	setDialogTitle(titleText); // NOI18N
	setApproveButtonText(buttonText); // NOI18N

	if (filters != null) {
	    for (int i = 0; i < filters.length; i++) {
		addChoosableFileFilter(filters[i]);
	    }
	    setFileFilter(filters[0]);
	}

	String feedFilePath = feed;
	File feedFilePathFile = null;

	if (feedFilePath != null && feedFilePath.length() > 0) {
	    feedFilePathFile = new File(feedFilePath);
	    //try {
            //	feedFilePathFile = feedFilePathFile.getCanonicalFile();
	    //}
	    //catch (IOException e) {
	    //}
	}

	if (feedFilePathFile != null && feedFilePathFile.exists()) {
	    currectChooserFile = feedFilePathFile;
	}

        if (currectChooserFile == null && feedFilePathFile == null)
            feedFilePathFile = new File(System.getProperty("user.home")); // NOI18N
        
	if (currectChooserFile == null && feedFilePathFile.getParentFile().exists()) {
	    currectChooserFile = feedFilePathFile.getParentFile();
	    useParent = false;
	}
	    

	// Set currect directory
	if (currectChooserFile != null) {
	    if (useParent) {
		if (currectChooserFile != null && currectChooserFile.exists()) {
		    setSelectedFile(currectChooserFile);
		}
	    }
	    else {
		if (currectChooserFile != null && currectChooserFile.exists()) {
		    setCurrentDirectory(currectChooserFile);
		}
	    }
	}
	else {
	    String sd = System.getProperty("spro.pwd"); // NOI18N
	    if (sd != null) {
		File sdFile = new File(sd);
		if (sdFile.exists()) {
		    setCurrentDirectory(sdFile);
		}
	    }
	}
    }

    @Override
    public int showOpenDialog(Component parent) {
	int ret = super.showOpenDialog(parent);
	if (ret != CANCEL_OPTION) {
	    if (getSelectedFile().exists())
		currectChooserFile = getSelectedFile();
	}
	return ret;
    }

    public static File getCurrectChooserFile() {
	return currectChooserFile;
    }
}
