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
package org.netbeans.modules.etl.project.anttasks;

import java.io.File;
import java.io.FileFilter;



/**
 *
 */
public class ETLExtensionFilter implements FileFilter {

	private String[] mExtensions;

	private boolean mInclude;

	public ETLExtensionFilter(String[] extensions) {
		this(extensions, true);
	}

	/**
	 * @param extensions
	 */
	public ETLExtensionFilter(String[] extensions, boolean include) {
		mExtensions = extensions;
		mInclude = include;
	}

	/**
	 * @param file
	 */
	public boolean accept(File file) {
		if (mExtensions == null || mExtensions.length == 0) {
			return true;
		}

		if (file.isDirectory()) {
			return true;
		}

		String fileName = file.getName();
		int dotInd = fileName.lastIndexOf(".etl");
		if (dotInd != -1) {
			String ext = fileName.substring(dotInd);
			for (int i = 0; i < mExtensions.length; ++i) {
				if (ext.equalsIgnoreCase(mExtensions[i])) {
					return mInclude;
				}
			}
		}
		return false;
	}
}

