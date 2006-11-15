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
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class Shell {
    protected String sp = " ";
    protected String sg = "=";
    protected String pr = "\"";
    protected String SETENV = "setenv" + sp;
    protected String EXPORT = "export ";
    protected String SET = "set ";
    
    protected  File getShellScript(EnvironmentScope scope) {
        File file =  null;
        if(scope!=null && EnvironmentScope.PROCESS != scope) {
            
            
            if(EnvironmentScope.ALL_USERS==scope) {
                file = getSystemShellScript();
            }
            
            if(EnvironmentScope.CURRENT_USER==scope || file==null) {
                file = getUserShellScript();
            }
        }
        LogManager.log(ErrorLevel.DEBUG,
                "Used shell file for setting environment variable : " + file);
        return file;
    }
    
    protected abstract String [] getSystemShellFileNames();
    protected abstract String [] getUserShellFileNames();
    public abstract boolean setVar(String name, String value, EnvironmentScope scope)  throws IOException;
    protected abstract String [] getAvailableNames();
    
    public boolean isCurrentShell(String name) {
        String [] names = getAvailableNames();
        boolean result = false;
        if(names!=null && name!=null) {
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
                SystemUtils.getUserHomeDirectory().getPath());
    }
    protected File getSystemShellScript() {
        return getSh(getSystemShellFileNames(),
                File.separator + "etc");
        
    }
    protected File getSh(String [] locations, String root) {
        if(locations == null) {
            return null;
        }
        File file = null;
        File firstFile = null;
        for(String loc: locations) {
            if(loc!=null) {
                file = new File(!loc.startsWith(File.separator) ?
                    root + File.separator + loc :
                    loc);
                if(firstFile == null) {
                    firstFile = file;
                }
                
                if(file.exists()) {
                    return file;
                }
            }
        }
        return firstFile;
        
    }
    protected int getSetEnvIndex(List <String> strings) {
        int idx = 0 ;
        int index = strings.size() ;
        for(String str:strings) {
            idx ++ ;
            if(str.startsWith(SET) || str.startsWith(EXPORT) || str.startsWith(SETENV)) {
                index = idx;
            }
        }
        return index;
    }
    protected List<String> getList(File file) throws IOException {
        return (file.canRead()) ?
            FileUtils.readStringList(file) :
            new LinkedList<String> ();
    }
    protected boolean writeList(List <String> strings, File file) throws IOException{
        if(!file.exists()) {
            if(!file.createNewFile()) {
                return false;
            };
        }
        if(file.canWrite()) {
            FileUtils.writeStringList(file,strings);
            return true;
        }
        return false;
    }
}
