/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.io.IOException;
import java.io.InputStream;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;

public class CCCCompiler extends BasicCompiler {
    public CCCCompiler(int kind, String name, String displayName) {
        super(kind, name, displayName);
    }
    
    // To be overridden
    public String getMTLevelOptions(int value) {
        return ""; // NOI18N
    }
    
    // To be overridden
    public String getLibraryLevelOptions(int value) {
        return ""; // NOI18N
    }
    
    // To be overridden
    public String getStandardsEvolutionOptions(int value) {
        return ""; // NOI18N
    }
    
    // To be overridden
    public String getLanguageExtOptions(int value) {
        return ""; // NOI18N
    }
    
    protected void getSystemIncludesAndDefines(Platform platform, String command, boolean stdout) throws IOException {
            Process process;
            process = Runtime.getRuntime().exec(command + " " + "/dev/null"); // NOI18N
            if (stdout)
                parseCompilerOutput(platform, process.getInputStream());
            else
                parseCompilerOutput(platform, process.getErrorStream());
        }
    
    // To be overridden
    public void saveSystemIncludesAndDefines() {
    }
    
    // To be overridden
    public void resetSystemIncludesAndDefines(Platform platform) {
    }
    
    // To be overridden
    protected void parseCompilerOutput(Platform platform, InputStream is) {
    }
}
