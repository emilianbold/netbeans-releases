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
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.SystemUtils.EnvironmentVariableScope;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class Shell {
    protected String sp = " ";
    protected String sg = "=";
    protected String pr = "\"";
    
    protected  File getShellScript(EnvironmentVariableScope scope) {
        if(scope==null || EnvironmentVariableScope.PROCESS == scope) {
            return null;
        }
        File file = null;
        if(EnvironmentVariableScope.ALL_USERS==scope) {
            file = getSystemShellScript();
        }
        
        if(EnvironmentVariableScope.CURRENT_USER==scope || file==null) {
            file = getUserShellScript();
        }
        return file;
    }
    
    protected abstract String [] getSystemShellFileNames();
    protected abstract String [] getUserShellFileNames();
    public abstract boolean setVar(String name, String value, EnvironmentVariableScope scope)  throws IOException;
    protected abstract String [] getAvailableNames();
    
    public boolean isCurrentShell(String name) {
        String [] names = getAvailableNames();
        boolean result = false;
        if(names==null && name!=null) {
            for(String shname:names) {
                if(shname.equals(name)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    
    protected File getUserShellScript() {
        return getSh(getUserShellFileNames(),
                SystemUtils.getInstance().getUserHomeDirectory().getPath());
    }
    protected File getSystemShellScript() {
        return getSh(getSystemShellFileNames(),
                File.separator + "etc");
        
    }
    protected File getSh(String [] locations, String root) {
        if(locations == null) {
            return null;
        }
        for(String prof: locations) {
            if(prof!=null) {
                File file = new File(!prof.startsWith(File.separator) ?
                    root + File.separator + prof :
                    prof);
                if(file.exists()) {
                    return file;
                }
            }
        }
        return null;
        
    }
    
}
