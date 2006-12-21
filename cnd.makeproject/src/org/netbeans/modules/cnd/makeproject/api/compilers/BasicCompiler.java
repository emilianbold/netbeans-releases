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

import java.util.List;
import java.util.Vector;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;

public class BasicCompiler extends Tool {
    
    /** Creates a new instance of GenericCompiler */
    public BasicCompiler(int kind, String name, String displayName) {
        super(kind, name, displayName);
    }
    
    public String getDevelopmentModeOptions(int value) {
        return ""; // NOI18N
    }
    
    public String getWarningLevelOptions(int value) {
        return ""; // NOI18N
    }
    
    public String getSixtyfourBitsOption(boolean value) {
        return ""; // NOI18N
    }
    
    public String getStripOption(boolean value) {
        return ""; // NOI18N
    }
    
    public List getSystemPreprocessorSymbols(Platform platform) {
        return new Vector(); // NOI18N
    }
    
    public List getSystemIncludeDirectories(Platform platform) {
        return new Vector(); // NOI18N
    }
    
    public void setSystemPreprocessorSymbols(Platform platform, List values) {
        ;
    }
    
    public void setSystemIncludeDirectories(Platform platform, List values) {
        ;
    }
}
