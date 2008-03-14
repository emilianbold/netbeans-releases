/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * Original file is from http://jna.dev.java.net/
 */
package org.netbeans.core.nativeaccess.transparency.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/** Mapping for w32 Shell API.  
 * Note that the C header "shellapi.h" includes "pshpack1.h", which disables 
 * automatic alignment of structure fields.  
 */
public interface Shell32 extends W32API {

    /** Custom alignment of structures. */
    int STRUCTURE_ALIGNMENT = Structure.ALIGN_NONE;
    Shell32 INSTANCE = (Shell32)
        Native.loadLibrary("shell32", Shell32.class, DEFAULT_OPTIONS);
    
    int FO_MOVE = 1;
    int FO_COPY = 2;
    int FO_DELETE = 3;
    int FO_RENAME = 4;
    
    int FOF_MULTIDESTFILES = 1;
    int FOF_CONFIRMMOUSE = 2;
    int FOF_SILENT = 4;
    int FOF_RENAMEONCOLLISION = 8;
    int FOF_NOCONFIRMATION = 16;
    int FOF_WANTMAPPINGHANDLE = 32;
    int FOF_ALLOWUNDO = 64;
    int FOF_FILESONLY = 128;
    int FOF_SIMPLEPROGRESS = 256;
    int FOF_NOCONFIRMMKDIR = 512;
    int FOF_NOERRORUI = 1024;
    int FOF_NOCOPYSECURITYATTRIBS = 2048;

    public static class SHFILEOPSTRUCT extends Structure {
        public HANDLE hwnd;
        public int wFunc;
        public String pFrom;
        public String pTo;
        public short fFlags;
        public boolean fAnyOperationsAborted;
        public Pointer pNameMappings;
        public String lpszProgressTitle;
        /** Use this to encode <code>pFrom/pTo</code> paths. */
        public String encodePaths(String[] paths) {
            String encoded = "";
            for (int i=0;i < paths.length;i++) {
                encoded += paths[i];
                encoded += "\0";
            }
            return encoded + "\0";
        }
    }
    int SHFileOperation(SHFILEOPSTRUCT fileop);
}
