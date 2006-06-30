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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jspcompiler;

import java.io.*;

/**
 * This class reads SMAP information from files.
 * @author  mg116726
 */
public class SmapFileReader implements SmapReader {

    private File file;

    public SmapFileReader(java.io.File file) {
        this.file = file;
    }

    public String toString() {
        if (file != null) return file.toString();
        return null;
    }

    public String readSmap() {
        if (file != null) {
            try {
                FileReader fr = new FileReader(file);
                LineNumberReader lnr = new LineNumberReader(fr);
                String line = "";
                String out = "";
                while ((line = lnr.readLine()) != null) {
                    out = out.concat(line);
                    out = out.concat("\n");
                }
                return out;
            } catch (FileNotFoundException fne) {
                return null;
            } catch (IOException ioe) {
                return null;
            }
        }
        return null;
    }
 

}
