/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.infra.utils.newlinecorrecter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        final File file = new File("D:/temp/nbi-build/build.sh");
        final String newline = "\n";
        
        final List<String> lines = new LinkedList<String>();
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line2read = null;
        while ((line2read = reader.readLine()) != null) {
            lines.add(line2read);
        }
        reader.close();
        
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        for (String line2write: lines) {
            writer.write(line2write + newline);
        }
        writer.close();
    }
}
