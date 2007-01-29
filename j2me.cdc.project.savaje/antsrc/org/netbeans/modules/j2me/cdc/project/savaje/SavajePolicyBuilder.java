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

package org.netbeans.modules.j2me.cdc.project.savaje;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author suchys
 */
public class SavajePolicyBuilder extends Task {
    private File file;
    private String codebase;
    
    public void setFile(File file) {
        this.file = file;
    }

    public void setCodebase(String codebase) {
        this.codebase = codebase;
    }

    public void execute() throws BuildException {
        if (file == null ) throw new BuildException("Target file is not specified!");
        
        final FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file);
            final PrintWriter printW = new PrintWriter(fileWriter);
            
            fileWriter.write("grant codeBase \"sb:/" + codebase + "/lib/classes.jar\" {\n");
            fileWriter.write("  permission java.security.AllPermission;\n");
            fileWriter.write("};\n");
            
            printW.close();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
