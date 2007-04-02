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
package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task which parses a given input file and devises the
 * netbeans visual web pack build number from it.
 *
 * @author Kirill Sorokin
 */
public class BuildNumberNbVisualWeb extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The input file.
     */
    private File file;
    
    /**
     * The properties' names prefix.
     */
    private String prefix;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the <code>file</code> attribute.
     *
     * @param path The value of the <code>file</code> attribute.
     */
    public void setFile(String path) {
        this.file = new File(path);
    }
    
    /**
     * Setter for the <code>prefix</code> attribute.
     *
     * @param prefix The value of the <code>prefix</code> attribute.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. The input file is parsed and three properties identifying
     * the netbeans visual web pack build are set.
     *
     * @throws org.apache.tools.ant.BuildException if the input file cannot be
     *      parsed for whatever reason.
     */
    @Override
    public void execute() throws BuildException {
        try {
            final FileInputStream in = new FileInputStream(file);
            final CharSequence contents = Utils.read(in);
            
            in.close();
            
            final Matcher matcher = PATTERN.matcher(contents);
            
            if (matcher.find()) {
                final String buildNumber = matcher.group(1);                // NOMAGI
                
                if (buildNumber.indexOf('_') == -1) {
                    getProject().setProperty(
                            prefix + BUILD_NUMBER_SUFFIX,
                            "20" + buildNumber + "00");                     // NOI18N
                } else {
                    getProject().setProperty(
                            prefix + BUILD_NUMBER_SUFFIX,
                            "20" + buildNumber.replace('_', '0'));   // NOI18N NOMAGI
                }
                getProject().setProperty(
                        prefix + BUILD_NUMBER_REAL_SUFFIX,
                        buildNumber);
            } else {
                throw new BuildException(
                        "Cannot find build number");                        // NOI18N
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Pattern for which to look in the input file.
     */
    private static final Pattern PATTERN = Pattern.compile(
            "RAVE-vw-([0-9_]+)\\.zip");                                     // NOI18N
    
    /**
     * Build number property suffix.
     */
    private static final String BUILD_NUMBER_SUFFIX =
            ".build.number";                                                // NOI18N
    
    /**
     * Real build number property suffix.
     */
    private static final String BUILD_NUMBER_REAL_SUFFIX =
            ".build.number.real";                                           // NOI18N
}
