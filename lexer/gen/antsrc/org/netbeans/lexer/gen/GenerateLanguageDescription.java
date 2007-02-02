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

package org.netbeans.lexer.gen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Abstract class for supporting a language description xml file generation.
 *
 * @author Miloslav Metelka
 */
public abstract class GenerateLanguageDescription extends Task {

    private String tokenTypesClassName;

    private File languageDescriptionFile;

    public GenerateLanguageDescription() {
    }

    public String getTokenTypesClassName() {
        return tokenTypesClassName;
    }
    
    public void setTokenTypesClassName(String tokenTypesClassName) {
        this.tokenTypesClassName = tokenTypesClassName;
    }
    
    public File getLanguageDescriptionFile() {
        return languageDescriptionFile;
    }
    
    public void setLanguageDescriptionFile(File languageDescriptionFile) {
        this.languageDescriptionFile = languageDescriptionFile;
    }

    public void execute() throws BuildException {
        String tokenTypesClassName = getTokenTypesClassName();
        File langDescFile = getLanguageDescriptionFile();
        
        if (tokenTypesClassName == null || "".equals(tokenTypesClassName)) {
            throw new BuildException("tokenTypesClassName attribute must be set");
        }
        if (langDescFile == null) {
            throw new BuildException("languageDescriptionFile attribute must be set");
        }
        
        String output;
        try {
            output = generate(tokenTypesClassName);
        } catch (ClassNotFoundException e) {
            throw new BuildException(e);
        }
  
        try {
            String writeType = langDescFile.exists()
                ? "appended to the end of"
                : "generated to";
                
            // getAbsolutePath() used to be able to compile on JDK1.3
            FileWriter langDescWriter = new FileWriter(langDescFile.getAbsolutePath(), true); // append

            langDescWriter.write(output);
            langDescWriter.close();
            
            log("Language description about class "
                + tokenTypesClassName + " successfully "
                + writeType + " file "
                + langDescFile
            );

        } catch (IOException e) {
            throw new BuildException("IOException occurred", e);
        }
    }
    
    protected abstract String generate(String tokenTypesClassName) throws ClassNotFoundException;
            
}
