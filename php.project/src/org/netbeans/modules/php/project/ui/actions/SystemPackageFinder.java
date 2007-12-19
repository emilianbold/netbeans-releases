/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author avk
 */
public abstract class SystemPackageFinder {

    
    private static final String LABEL_CHOOSE_PHP_TITLE = "LBL_ChoosePhpTitle";
    
    private static final String LABEL_CHOOSE_PHP_MSG = "LBL_ChoosePhpMessage";
    
    /**
     * searches for php available php interpreters using 
     * @see getAllPhpInterpreters() and opens dialog to 
     * allow user to choose which to use.
     */
    public static String getPhpInterpreterUserChoice() {
        return getPhpInterpreterUserChoice(null);
    }
    
    public static String getPhpInterpreterUserChoice(ProgressHandle progress) {
        String[] interpreters = null;
        try{
            if (progress != null){
                progress.start();
            }
            interpreters = getAllPhpInterpreters();
            
        } finally {
            if (progress != null){
                progress.finish();
            }
        }
        
        if (interpreters == null || interpreters.length == 0) {
            return null;
        }
        //if (interpreters.length == 1) {
        //    return interpreters[0];
        //}
        return askUserToChoose(interpreters);
    }

    /**
     * searches for php available php interpreters using 
     * @see getAllPhpInterpreters() and selects any of them.
     */
    public static String getPhpInterpreterAny() {
        String[] interpreters = getAllPhpInterpreters();
        if (interpreters != null) {
            return interpreters[0];
        }
        return null;
    }

    /** 
     * searches for all available php interpreters.
     * Search is platform-dependent.
     * @see isSupportedOs() to check if platform is supported.
     */
    public static String[] getAllPhpInterpreters() {
        SystemPackageFinder impl = getFinderImpl();
        if (impl != null) {

            impl.fillPhpSet();

            if (impl.getPhpSet().isEmpty()) {
                return null;
            }

            return impl.getPhpSet().toArray(new String[]{});
        }
        return null;
    }

    /**
     * checks current platform.
     * @returns is search supported on current platform.
     */
    public static boolean isSupportedOs() {
        if (isSolaris()) {
            return true;
        } else if (isUnix()) {
            return true;
        } else if (isWindows()) {
            return true;
        } else if (isMac()) {
            return false;
        } else {
            return false;
        }
    }

    /**
     * is invoked from @see getAllPhpInterpreters()
     * to fill Set of available php interpreters.
     * should be implemented by OS specific child.
     */
    protected abstract void fillPhpSet();

    /**
     * should be used by childs in @see fillPhpSet
     * implementation to store available php interpreter.
     * doesn't store null are already existing value.
     */
    protected void addPhpInterpreter(String php) {
        if (php == null) {
            return;
        }
        
        myPhpSet.add(php);
    }
    
    private static String askUserToChoose(String[] list){
        String title = NbBundle.getMessage(
                SystemPackageFinder.class, LABEL_CHOOSE_PHP_TITLE);
        String message = NbBundle.getMessage(
                SystemPackageFinder.class, LABEL_CHOOSE_PHP_MSG);
        
        return (String)SelectFromListPanel.askUserToChoose(title, message, list);
    }

    private static SystemPackageFinder getFinderImpl() {
        if (isSolaris()) {
            return new SystemPackageFinderSolarisImpl();
        } else if (isUnix()) {
            return new SystemPackageFinderUnixImpl();
        } else if (isWindows()) {
            return new SystemPackageFinderWindowsImpl();
        } else if (isMac()) {
            return null;
            //return new SystemPackageFinderMacImpl(project);
        } else {
            return null;
        }
    }

    private static boolean isSolaris() {
        return Utilities.getOperatingSystem() == Utilities.OS_SOLARIS 
                || Utilities.getOperatingSystem() == Utilities.OS_SUNOS;
    }

    private static boolean isUnix() {
        return Utilities.isUnix();
    }

    private static boolean isWindows() {
        return Utilities.isWindows();
    }

    private static boolean isMac() {
        return Utilities.isMac();
    }

    private Set<String> getPhpSet() {
        return myPhpSet;
    }
    
    private Set<String> myPhpSet = new HashSet<String>();
}

class SystemPackageFinderWindowsImpl extends SystemPackageFinder {

    private static final String PHP = "php.exe"; // NOI18N
    private static final String SYSTEM_LIBRARY_PATH = "java.library.path"; // NOI18N
    private static final String SYSTEM_CURRENT_DIR = "."; // NOI18N

    protected void fillPhpSet() {
        addFromSystemPath(PHP);
        // TODO check if system has 'where' command and use it
    }

    protected void addFromSystemPath(String program) {
        for (String path : getSystemPath()){
            File file = new File(path + File.separatorChar + PHP);
            if (file.exists()){
                addPhpInterpreter(file.getAbsolutePath());
            }
        }
    }

    
    private String[] getSystemPath(){
        try{
            String path = System.getProperty(SYSTEM_LIBRARY_PATH);
            if (path != null){
                path += File.separatorChar + SYSTEM_CURRENT_DIR;
                return path.split(File.pathSeparator);
            }
        } catch (SecurityException secEx){
            // do nothing. will return defauult value
        }
        return new String[]{SYSTEM_CURRENT_DIR};
    }
}

class SystemPackageFinderSolarisImpl extends SystemPackageFinderUnixImpl {

    private static final String SADM_CONTENTS = "/var/sadm/install/contents"; // NOI18N
    private static final String PATH_TO_PHP_BIN = "bin/php"; // NOI18N

    protected void fillPhpSet() {
        super.fillPhpSet();
        
        addPhpInterpreter(getLocation(PATH_TO_PHP_BIN));
    }

    protected String getLocation(String name) {
        String grep = getGrep();
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(new String[]{grep, name, SADM_CONTENTS});
            InputStream stream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(name)) {
                    return line.substring(0, line.indexOf(name) + name.length());
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}

class SystemPackageFinderUnixImpl extends SystemPackageFinder {

    private static final String GREP = "grep"; // NOI18N
    private static final String PHP = "php"; // NOI18N
    private static final String USR_BIN = "/usr/bin"; // NOI18N
    private static final String USR_LOCAL_BIN = "/usr/local/bin"; // NOI18N
    private static final String WHICH = "which"; // NOI18N

    protected String getGrep() {
        if (myGrep == null) {
            myGrep = getProgramPath(GREP);
            if (myGrep == null) {
                myGrep = getProgramPath(USR_BIN + File.separator + GREP);
            }
        }
        return myGrep;
    }

    protected void fillPhpSet() {
        addPhpInterpreter(getProgramPath(PHP));

        addPhpInterpreter(getProgramPath(USR_BIN + File.separator + PHP));

        addPhpInterpreter(getProgramPath(USR_LOCAL_BIN + File.separator + PHP));
    }

    protected String getProgramPath(String program) {
        Runtime runtime = Runtime.getRuntime();
        String which = myWhich == null ? WHICH : myWhich;
        try {
            Process process = runtime.exec(new String[]{which, program});
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(program)) {
                    return line;
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private String myGrep;
    
    private String myWhich;
    
}

class SystemPackageFinderMacImpl extends SystemPackageFinder {

    protected void fillPhpSet() {
        // is not supported. Do nothing
    }

}