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

package org.netbeans.modules.languages.sh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;

/**
 * Detects shell scripts.
 */
public class ShellScriptResolver extends MIMEResolver {

    /** Default constructor for lookup. */
    public ShellScriptResolver() {}

    public String findMIMEType(FileObject fo) {
        if (fo.hasExt("sh")) {
            return "text/sh";
        }
        if (fo.hasExt("")) {
            try {
                InputStream is = fo.getInputStream();
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(is));
                    String line = r.readLine(); // could be null
                    if ("#!/bin/sh".equals(line) || "#!/bin/bash".equals(line)) {
                        return "text/sh";
                    }
                } finally {
                    is.close();
                }
            } catch (IOException x) {
                Logger.getLogger(ShellScriptResolver.class.getName()).log(Level.INFO, "Could not scan " + fo, x);
            }
        }
        return null;
    }

}
