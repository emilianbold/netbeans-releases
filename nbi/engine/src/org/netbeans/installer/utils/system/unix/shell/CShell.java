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
import org.netbeans.installer.utils.SystemUtils.EnvironmentScope;

/**
 *
 * @author dlm198383
 */
public class CShell extends Shell{
    private String [] SYSTEM_PROFILE_FILES = {
        "csh.cshrc",
        "csh.login",
        "profile",
        ".cshrc",
    };
    
    private String [] USER_PROFILE_HOMEDIRFILES = {
        ".cshrc.user",
        ".cshrc",
        ".profile",
        ".login"
    };
    /** Creates a new instance of Bash */
    public CShell() {
    }
    
    public boolean setVar(String name, String value, EnvironmentScope scope) throws IOException {
        File file = getShellScript(scope);
        if(file==null)  {
            return false;
        }
        List <String> strings = getList(file);
        
        boolean exist = false;
        String str;
        String substr;
        for(int i=0;i<strings.size();i++) {
            
            str = strings.get(i);
            if(str!=null) {
                str = str.trim();
               
                if(str.startsWith(SETENV)) {
                    substr = str.substring(SETENV.length());
                    substr = substr.trim();
                                        
                    if(substr.startsWith(name + sg) || substr.startsWith(name + sp) || substr.equals(name)) {
                        if(value==null) {                            
                            strings.remove(i);
                            i--;
                        } else {                            
                            strings.set(i, SETENV + name + sp + pr + value + pr);
                        }
                        exist = true;
                        break;
                    }
                }
            }
        }
        if(!exist) {            
            strings.add(getSetEnvIndex(strings),SETENV + name + sp + pr + value + pr);
        }
        
        return writeList(strings,file);
    }
    
    public String [] getSystemShellFileNames() {
        return SYSTEM_PROFILE_FILES;
    }
    
    public String[] getUserShellFileNames() {
        return USER_PROFILE_HOMEDIRFILES;
    }
    public String [] getAvailableNames() {
        return new String [] { "csh", "pfcsh"};
    }
}
