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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class GNUCCCompiler extends CCCCompiler {
    private static final String compilerStdoutCommand = " -x c++ -E -dM"; // NOI18N
    private static final String compilerStderrCommand = " -x c++ -E -v"; // NOI18N
    private PersistentList systemIncludeDirectoriesList = null;
    private PersistentList systemPreprocessorSymbolsList = null;
    private boolean saveOK = true;
    
    
    /** Replace this temporary stuff */
    private static final boolean fullIncludes = true; // Boolean.getBoolean("gcc.full.includes");
    
    private static final String[] DEVELOPMENT_MODE_OPTIONS = {
        "",  // Fast Build // NOI18N
        "-g3 -gdwarf-2", // Debug" // NOI18N
        "-g -gdwarf-2 -O", // Performance Debug" // NOI18N
        "-g -gdwarf-2", // Test Coverage // NOI18N
        "-g -gdwarf-2 -O2", // Dianosable Release // NOI18N
        "-O2", // Release // NOI18N
        "-O3", // Performance Release // NOI18N
    };
    
    protected static final String[] WARNING_LEVEL_OPTIONS = {
        "-w", // No Warnings // NOI18N
        "", // Default // NOI18N
        "-Wall", // More Warnings // NOI18N
        "-Werror", // Convert Warnings to Errors // NOI18N
    };
    
    /** Creates a new instance of GNUCCompiler */
    public GNUCCCompiler(CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(flavor, kind, name, displayName, path);
    }
    
    @Override
    public String getDevelopmentModeOptions(int value) {
        return DEVELOPMENT_MODE_OPTIONS[value];
    }
    
    @Override
    public String getWarningLevelOptions(int value) {
        if (value < WARNING_LEVEL_OPTIONS.length)
            return WARNING_LEVEL_OPTIONS[value];
        else
            return ""; // NOI18N
    }
    
    @Override
    public String getSixtyfourBitsOption(int value) {
        if (value == BasicCompilerConfiguration.BITS_DEFAULT)
            return ""; // NOI18N
        else if (value == BasicCompilerConfiguration.BITS_32)
            return "-m32"; // NOI18N
        else if (value == BasicCompilerConfiguration.BITS_64)
            return "-m64"; // NOI18N
        else
            return ""; // NOI18N
    }
    
    @Override
    public String getStripOption(boolean value) {
        return value ? "-s" : ""; // NOI18N
    }
    
    @Override
    public boolean setSystemIncludeDirectories(List values) {
        assert values != null;
        if (values.equals(systemIncludeDirectoriesList)) {
            return false;
        }
        systemIncludeDirectoriesList = new PersistentList(values);
        normalizePaths(systemIncludeDirectoriesList);
        return true;
    }
    
    @Override
    public boolean setSystemPreprocessorSymbols(List values) {
        assert values != null;
        if (values.equals(systemPreprocessorSymbolsList)) {
            return false;
        }
        systemPreprocessorSymbolsList = new PersistentList(values);
        return true;
    }
    
    @Override
    public List getSystemPreprocessorSymbols() {
        if (systemPreprocessorSymbolsList != null)
            return systemPreprocessorSymbolsList;
        
        getSystemIncludesAndDefines();
        return systemPreprocessorSymbolsList;
    }
    
    @Override
    public List getSystemIncludeDirectories() {
        if (systemIncludeDirectoriesList != null)
            return systemIncludeDirectoriesList;
        
        getSystemIncludesAndDefines();
        return systemIncludeDirectoriesList;
    }
    
    public void saveSystemIncludesAndDefines() {
        if (systemIncludeDirectoriesList != null && saveOK)
            systemIncludeDirectoriesList.saveList(getUniqueID() + "systemIncludeDirectoriesList"); // NOI18N
        if (systemPreprocessorSymbolsList != null && saveOK)
            systemPreprocessorSymbolsList.saveList(getUniqueID() + "systemPreprocessorSymbolsList"); // NOI18N
    }
    
    private void restoreSystemIncludesAndDefines() {
        systemIncludeDirectoriesList = PersistentList.restoreList(getUniqueID() + "systemIncludeDirectoriesList"); // NOI18N
        systemPreprocessorSymbolsList = PersistentList.restoreList(getUniqueID() + "systemPreprocessorSymbolsList"); // NOI18N
    }
    
    private void getSystemIncludesAndDefines() {
        restoreSystemIncludesAndDefines();
        if (systemIncludeDirectoriesList == null || systemPreprocessorSymbolsList == null) {
            getFreshSystemIncludesAndDefines();
        }
    }
    
    private void getFreshSystemIncludesAndDefines() {
        systemIncludeDirectoriesList = new PersistentList();
        systemPreprocessorSymbolsList = new PersistentList();
        String path = getPath();
        if (path == null || !new File(path).exists()){
            path = "g++"; // NOI18N
        }
        try {
            getSystemIncludesAndDefines(path + compilerStderrCommand, false);
            getSystemIncludesAndDefines(path + compilerStdoutCommand, true);
            // a workaround for gcc bug - see http://gcc.gnu.org/ml/gcc-bugs/2006-01/msg00767.html
            if (! containsMacro(systemPreprocessorSymbolsList, "__STDC__")) { // NOI18N
                systemPreprocessorSymbolsList.add("__STDC__=1"); // NOI18N
            }
            saveOK = true;
        } catch (IOException ioe) {
            System.err.println("IOException " + ioe);
            String errormsg = NbBundle.getMessage(getClass(), "CANTFINDCOMPILER", path); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
            saveOK = false;
        }
        
        normalizePaths(systemIncludeDirectoriesList);
    }
    
    public void resetSystemIncludesAndDefines() {
        getFreshSystemIncludesAndDefines();
    }
    
    protected void parseCompilerOutput(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        
        try {
            String line;
            boolean startIncludes = false;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                if (line.startsWith("#include <...>")) { // NOI18N
                    startIncludes = true;
                    continue;
                }
                if (line.startsWith("End of search")) { // NOI18N
                    startIncludes = false;
                    continue;
                }
                if (startIncludes) {
                    line = line.trim();
                    if (getFlavor() == CompilerFlavor.MinGW) {
                        if (line.toLowerCase().startsWith(getIncludeFilePathPrefix().toLowerCase())) {
                            line = line.substring(getIncludeFilePathPrefix().length());
                        }
                        else if (line.toLowerCase().startsWith("/mingw")) { // NOI18N
                            line = line.substring(6);
                        }
                    }
                    systemIncludeDirectoriesList.add(getIncludeFilePathPrefix() + line);
                    if (getIncludeFilePathPrefix().length() > 0 && line.startsWith("/usr/lib")) // NOI18N
                        systemIncludeDirectoriesList.add(getIncludeFilePathPrefix() + line.substring(4));
                    continue;
                }
                parseUserMacros(line, systemPreprocessorSymbolsList);
                if (line.startsWith("#define ")) { // NOI18N
                    int i = line.indexOf(' ', 8);
                    if (i > 0) {
                        String token = line.substring(8, i) + "=" + line.substring(i+1); // NOI18N
                        systemPreprocessorSymbolsList.add(token);
                    }
                }
            }
            is.close();
            reader.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe); // FIXUP
        }
    }
    
    private void dumpLists() {
        System.out.println("==================================" + getDisplayName()); // NOI18N
        for (int i = 0; i < systemIncludeDirectoriesList.size(); i++) {
            System.out.println("-I" + systemIncludeDirectoriesList.get(i)); // NOI18N
        }
        for (int i = 0; i < systemPreprocessorSymbolsList.size(); i++) {
            System.out.println("-D" + systemPreprocessorSymbolsList.get(i)); // NOI18N
        }
    }
}
