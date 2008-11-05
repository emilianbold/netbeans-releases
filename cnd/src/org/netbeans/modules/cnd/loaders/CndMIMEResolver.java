/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.loaders;

import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.editor.filecreation.ExtensionsSettings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

/**
 * Recognize certain hard-to-recognize types. Most types are recognized by the
 * declarative mime resolver.
 */
public class CndMIMEResolver extends MIMEResolver {
    
    public CndMIMEResolver() {
        //System.err.println("called CndMIMEResolver.CndMIMEResolver()");
        super(MIMENames.C_MIME_TYPE, MIMENames.CPLUSPLUS_MIME_TYPE,
                MIMENames.MAKEFILE_MIME_TYPE, MIMENames.SHELL_MIME_TYPE,
                MIMENames.FORTRAN_MIME_TYPE, MIMENames.ASM_MIME_TYPE);
    }
    
    public static boolean isHeaderExtension(String ext){
        return ExtensionsSettings.isRegistered(ext, ExtensionsSettings.HEADER);
    }
    
    public static boolean isMimeTypeExtension(String mineType, String ext){
        if (MIMENames.C_MIME_TYPE.equals(mineType)){
            return ExtensionsSettings.isRegistered(ext, ExtensionsSettings.C_FILE);
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mineType)){
            return ExtensionsSettings.isRegistered(ext, ExtensionsSettings.CPP_FILE);
        }
        return false;
    }

    /**
     * Resolves FileObject and returns recognized MIME type
     * @param fo is FileObject which should be resolved
     * @return  recognized MIME type or null if not recognized
     */
    public String findMIMEType(FileObject fo) {
	if (fo.isFolder()) {
	    return null;
	}

	String ext = fo.getExt();
	
        // Recognize c files by extension
        if (ExtensionsSettings.isRegistered(ext, ExtensionsSettings.C_FILE)) {
            return MIMENames.C_MIME_TYPE;
        }

	// Recognize c++ files by extension
        if (ExtensionsSettings.isRegistered(ext, ExtensionsSettings.CPP_FILE)) {
            return MIMENames.CPLUSPLUS_MIME_TYPE;
        }
	
        // Recognize header files by extension
        if (ExtensionsSettings.isRegistered(ext, ExtensionsSettings.HEADER)) {
            return MIMENames.CPLUSPLUS_MIME_TYPE;
        }

	// Recognize makefiles by extension
        if (ExtensionsSettings.isRegistered(ext, ExtensionsSettings.MAKEFILE)) {
            return MIMENames.MAKEFILE_MIME_TYPE;
	}

        // Recognize shell scripts by extension
        if (ExtensionsSettings.isRegistered(ext, ExtensionsSettings.SHELL)) {
            if ("bat".equals(ext) || "cmd".equals(ext)) { // NOI18N
                return MIMENames.BAT_MIME_TYPE;
            } else {
                return MIMENames.SHELL_MIME_TYPE;
            }
        }

        // Recognize fortran files by extension
        if (ExtensionsSettings.isRegistered(ext, ExtensionsSettings.FORTRAN)) {
            return MIMENames.FORTRAN_MIME_TYPE;
        }

        // Recognize asm files by extension
        if (ExtensionsSettings.isRegistered(ext, ExtensionsSettings.ASM)) {
            return MIMENames.ASM_MIME_TYPE;
        }
        
        // Recognize makefiles by name
	// Check for various (somewhat) standard Makefile names.
        if (ext.length() == 0) {
            String name = fo.getName().toLowerCase();
            if (name.startsWith("makefile") || name.endsWith("makefile") ||name.startsWith("gnumakefile")) { // NOI18N
                return MIMENames.MAKEFILE_MIME_TYPE;
            }
            // Also recognize names like "newMakefile" and "newMakefile_1" as a makefile
            name = fo.getName();
            if (name.indexOf(".") < 0 && name.indexOf("Makefile") >= 0) { // NOI18N
                return MIMENames.MAKEFILE_MIME_TYPE;
            }
        }
	return null;
    }
}
