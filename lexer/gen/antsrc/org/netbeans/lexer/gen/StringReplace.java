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
 * Abstract class for supporting a language source generation.
 *
 * @author Miloslav Metelka
 */
public class StringReplace extends Task {

    private String replaceWhat;

    private String replaceWith;

    private String replaceIn;

    private String property;

    public StringReplace() {
    }

    public String getReplaceWhat() {
        return replaceWhat;
    }
    
    public void setReplaceWhat(String replaceWhat) {
        this.replaceWhat = replaceWhat;
    }
    
    public String getReplaceWith() {
        return replaceWith;
    }
    
    public void setReplaceWith(String replaceWith) {
        this.replaceWith = replaceWith;
    }
    
    public String getReplaceIn() {
        return replaceIn;
    }
    
    public void setReplaceIn(String replaceIn) {
        this.replaceIn = replaceIn;
    }

    public String getProperty() {
        return property;
    }
    
    public void setProperty(String property) {
        this.property = property;
    }

    public void execute() throws BuildException {
        if (getReplaceWhat() == null || "".equals(getReplaceWhat())) {
            throw new BuildException("replaceWhat attribute must be set");
        }
        if (getProperty() == null || "".equals(getProperty())) {
            throw new BuildException("property attribute must be set");
        }

        String output = getReplaceIn();
        int startIndex = 0;
        while (true) {
            int foundIndex = output.indexOf(getReplaceWhat(), startIndex);
            if (foundIndex == -1) {
                break;
            }

            output = output.substring(0, foundIndex)
                + getReplaceWith()
                + output.substring(foundIndex + getReplaceWhat().length());

            foundIndex += getReplaceWith().length();
        }
        
        // Set the target property
        getOwningTarget().getProject().setProperty(getProperty(), output);
    }
    
}
