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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.loaders;

import org.netbeans.modules.cnd.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

/**
 * expensive resolver (read any file) not based neither on extension, name nor magic hex number
 * 
 * @author Vladimir Voskresensky
 */
public class CndSniffyMIMEResolver extends MIMEResolver {
    /**
     * Resolves FileObject and returns recognized MIME type
     * @param fo is FileObject which should be resolved
     * @return  recognized MIME type or null if not recognized
     */
    public String findMIMEType(FileObject fo) {
	if (fo.isFolder()) {
	    return null;
	}

        // Recognize c++ file without extension
	if (HDataLoader.getInstance().detectCPPByComment(fo)) {
             return MIMENames.CPLUSPLUS_MIME_TYPE;
	}
        return null;
    }
}
