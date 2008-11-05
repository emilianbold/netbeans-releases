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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.languages.sh;

import java.io.IOException;
import java.io.InputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

/**
 * Detects shell scripts.
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.filesystems.MIMEResolver.class)
public class ShellScriptResolver extends MIMEResolver {

    /** Default constructor for lookup. */
    public ShellScriptResolver() {
        super("text/sh");  //NOI18N
    }

    public String findMIMEType(FileObject fo) {
        if (fo.hasExt("sh")) {  //NOI18N
            return "text/sh";  //NOI18N
        }
        if (fo.isData() && fo.hasExt("")) {
            try {
                InputStream is = fo.getInputStream();
                try {
                    byte[] bytes = new byte[12];
                    int len = is.read(bytes);
                    if (len > 0 && (startsWith(bytes, "#!/bin/sh") || startsWith(bytes, "#!/bin/bash"))) {  //NOI18N
                        return "text/sh";  //NOI18N
                    }
                } finally {
                    is.close();
                }
            } catch (IOException x) {
            }
        }
        return null;
    }

    /** Checks whether byte array starts with given string.
     * @return true if byte array starts with given prefix, false otherwise
     */
    private static boolean startsWith(byte[] bytes, String prefix) {
        byte[] prefixBytes = prefix.getBytes();
        for (int i = 0; i < prefixBytes.length; i++) {
            if (i > bytes.length-1 || bytes[i] != prefixBytes[i]) {
                return false;
            }
        }
        return true;
    }
}
