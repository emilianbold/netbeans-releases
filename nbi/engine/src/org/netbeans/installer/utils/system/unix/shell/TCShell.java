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
 *
 * $Id$
 *
 */

package org.netbeans.installer.utils.system.unix.shell;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.SystemUtils.EnvironmentVariableScope;

/**
 *
 * @author dlm198383
 */
public class TCShell extends CShell{    
    private String [] SYSTEM_PROFILE_FILES = {
        "tcsh.cshrc",
        "tcsh.login",
        "profile"
    };
    
    private String [] USER_PROFILE_HOMEDIRFILES = {
        ".tcshrc.user",
        ".tcshrc",
        ".cshrc.user",
        ".cshrc",
        ".profile",
        ".login"        
    };    
    
    public String [] getSystemShellFileNames() {
        return SYSTEM_PROFILE_FILES;
    }
    
    public String[] getUserShellFileNames() {
        return USER_PROFILE_HOMEDIRFILES;
    }
    public String [] getAvailableNames() {
        return new String [] { "tcsh"};
    }
}
