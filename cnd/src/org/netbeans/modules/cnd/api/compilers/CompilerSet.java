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

package org.netbeans.modules.cnd.api.compilers;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * A container for information about a set of related compilers, typicaly from a vendor or
 * redistributor.
 */
public class CompilerSet {
    
    /** Recognized (and prioritized) types of compiler sets */
    public enum CompilerFlavor {Unknown, Sun, GNU, DJGPP, Interix, MinGW, Cygwin};
    
    public final static String CS_Cygwin = "Cygwin"; // NOI18N
    public final static String CS_MinGW = "MinGW"; // NOI18N
    public final static String CS_Interix = "Interix"; // NOI18N
    public final static String CS_DJGPP = "DJGPP"; // NOI18N
    public final static String CS_GNU = "GNU"; // NOI18N
    public final static String CS_Sun = "Sun"; // NOI18N
    public final static String CS_Unknown = "Unknown"; // NOI18N
    
    private static HashMap<String, CompilerSet> csmap = new HashMap();
    private static HashMap<String, CompilerSet> basemap = new HashMap();
    
    private static int nextID = 0;
    private static int nextCygwin = 0;
    private static int nextMinGW = 0;
    private static int nextInterix = 0;
    private static int nextDJGPP = 0;
    private static int nextGNU = 0;
    private static int nextSun = 0;
    private static int nextUnknown = 0;
    
    private CompilerFlavor flavor;
    private int id;
    private int flavorID;
    private String name;
    private String displayName;
    private StringBuffer directory = new StringBuffer(256);
    private ArrayList<Tool> tools = new ArrayList();
    private String librarySearchOption;
    private String dynamicLibrarySearchOption;
    private String libraryOption;
    
    /** Creates a new instance of CompilerSet */
    protected CompilerSet(CompilerFlavor flavor, String directory) {
        this.flavor = flavor;
        addDirectory(directory);
        id = nextID++;
        
        switch (flavor) {
            case Cygwin:
                name = CS_Cygwin;
                flavorID = nextCygwin++;
                if (flavorID == 0) {
                    displayName = NbBundle.getMessage(CompilerSet.class, "LBL_CygwinCompilerSet_0"); // NOI18N
                } else {
                    displayName = MessageFormat.format(NbBundle.getMessage(CompilerSet.class,
                            "LBL_CygwinCompilerSet_X"), Integer.valueOf(flavorID)); // NOI18N
                }
                break;
                
            case MinGW:
                name = CS_MinGW;
                flavorID = nextMinGW++;
                if (flavorID == 0) {
                    displayName = NbBundle.getMessage(CompilerSet.class, "LBL_MinGWCompilerSet_0"); // NOI18N
                } else {
                    displayName = MessageFormat.format(NbBundle.getMessage(CompilerSet.class,
                            "LBL_MinGWCompilerSet_X"), Integer.valueOf(flavorID)); // NOI18N
                }
                break;
                
            case Interix:
                name = CS_Interix;
                flavorID = nextInterix++;
                if (flavorID == 0) {
                    displayName = NbBundle.getMessage(CompilerSet.class, "LBL_InterixCompilerSet_0"); // NOI18N
                } else {
                    displayName = MessageFormat.format(NbBundle.getMessage(CompilerSet.class,
                            "LBL_InterixCompilerSet_X"), Integer.valueOf(flavorID)); // NOI18N
                }
                basemap.put(getBase(directory), this);
                break;
                
            case DJGPP:
                name = CS_DJGPP;
                flavorID = nextDJGPP++;
                if (flavorID == 0) {
                    displayName = NbBundle.getMessage(CompilerSet.class, "LBL_DJGPPCompilerSet_0"); // NOI18N
                } else {
                    displayName = MessageFormat.format(NbBundle.getMessage(CompilerSet.class,
                            "LBL_DJGPPCompilerSet_X"), Integer.valueOf(flavorID)); // NOI18N
                }
                break;
                
            case GNU:
                name = CS_GNU;
                flavorID = nextGNU++;
                if (flavorID == 0) {
                    displayName = NbBundle.getMessage(CompilerSet.class, "LBL_GNUCompilerSet_0"); // NOI18N
                } else {
                    displayName = MessageFormat.format(NbBundle.getMessage(CompilerSet.class,
                            "LBL_GNUCompilerSet_X"), Integer.valueOf(flavorID)); // NOI18N
                }
                break;
                
            case Sun:
                name = CS_Sun;
                flavorID = nextSun++;
                if (flavorID == 0) {
                    displayName = NbBundle.getMessage(CompilerSet.class, "LBL_SunCompilerSet_0"); // NOI18N
                } else {
                    displayName = MessageFormat.format(NbBundle.getMessage(CompilerSet.class,
                            "LBL_SunCompilerSet_X"), Integer.valueOf(flavorID)); // NOI18N
                }
                break;
                
            case Unknown:
                name = CS_Unknown;
                flavorID = nextUnknown++;
                if (flavorID == 0) {
                    displayName = NbBundle.getMessage(CompilerSet.class, "LBL_UnknownCompilerSet_0"); // NOI18N
                } else {
                    displayName = MessageFormat.format(NbBundle.getMessage(CompilerSet.class,
                            "LBL_UnknownCompilerSet_X"), Integer.valueOf(flavorID)); // NOI18N
                }
                break;
        }
        
        librarySearchOption = ""; // NOI18N
        dynamicLibrarySearchOption = ""; // NOI18N
        libraryOption = ""; // NOI18N
        csmap.put(directory, this);
    }
    
    public static CompilerSet getCompilerSet(String directory, String[] list) {
        
        CompilerSet cs = csmap.get(directory);
        if (cs != null) {
            return cs;
        }
        String base = getBase(directory);
        if (base != null) {
            cs = basemap.get(base);
            if (cs != null) {
                cs.addDirectory(directory);
                return cs;
            }
        }
        
        if (Utilities.isWindows()) {
            if (directory.toLowerCase().indexOf("cygwin") != -1) { // NOI18
                return new CompilerSet(CompilerFlavor.Cygwin, directory);
            }
            if (directory.toLowerCase().indexOf("mingw") != -1) { // NOI18N
                return new CompilerSet(CompilerFlavor.MinGW, directory);
            }
            if (directory.toLowerCase().indexOf("sfu") != -1 || directory.toLowerCase().indexOf("sua") != -1) { // NOI18N
                return new CompilerSet(CompilerFlavor.Interix, directory);
            }
        } else {
            if (directory.indexOf("SUNWspro") != -1) { // NOI18N
                return new CompilerSet(CompilerFlavor.Sun, directory);
            }
        }
        
        // So far we havne't been able to determine the compiler set flavor. Look at the
        // names in list and see if we can from it. If not, assume its unknown.
        for (String pgm : list) {
            if (pgm.indexOf("gcc") != -1 || pgm.indexOf("g++") != -1) { // NOI18N
                return new CompilerSet(CompilerFlavor.GNU, directory);
            }
        }
        return new CompilerSet(CompilerFlavor.Unknown, directory);
    }
    
    protected int getID() {
        return id;
    }
    
    /**
     * This method is useful for compiler sets with multiple bin directories. Currently only Interix'
     * has this (this is distributed both by Interix and Microsoft).
     *
     * @param directory The bin directory whose base we want
     * @returns The base directory
     */
    private static String getBase(String directory) {
        String base = "";
        
        if (directory.toLowerCase().indexOf("sfu") != -1 || directory.toLowerCase().indexOf("sua") != -1) { // NOI18N
            int pos = directory.indexOf("\\opt\\gcc");  // NOI18
            if (pos != -1) {
                base = directory.substring(0, pos);
            } else if (directory.endsWith("\\bin")) { // NOI18N
                base = directory.substring(0, directory.length() - 4);
            }
        }
        return base;
    }
    
    public CompilerFlavor getCompilerFlavor() {
        return flavor;
    }
    
    public boolean isGnuCompilerSet() {
        return flavor == CompilerFlavor.GNU || flavor == CompilerFlavor.Cygwin || flavor == CompilerFlavor.MinGW ||
                flavor == CompilerFlavor.Interix || flavor == CompilerFlavor.DJGPP;
    }
    
    public void addDirectory(String path) {
        if (directory.length() == 0) {
            directory.append(path);
        } else {
            directory.append(File.pathSeparator);
            directory.append(path); 
        }
    }
    
    public String getDirectory() {
        return directory.toString();
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void addTool(String name, String path, int type) {
        String displayName;
        int pos = name.indexOf(".exe");
        
        if (pos >= 0) {
            displayName = name.substring(0, pos);
        } else {
            displayName = name;
        }
        tools.add(new Tool(type, name, displayName, path));
    }
    
    /**
     * Get a tool by name
     *
     * @param name The name of the desired tool
     * @return The Tool or null
     */
    public Tool getTool(String name) {
        for (Tool tool : tools) {
            if (tool.getDisplayName().equals(name) || tool.getName().equals(name)) {
                return tool;
            }
        }
        return null;
    }
    
    /**
     * Get the first tool of its kind.
     *
     * @param kind The type of tool to get
     * @return The Tool or null
     */
    public Tool getTool(int kind) {
        for (Tool tool : tools) {
            if (tool.getKind() == kind)
                return tool;
        }
        return null;
    }
    
    public List<Tool> getTools() {
        return tools;
    }
    
    public String[] getToolGenericNames() {
        ArrayList names = new ArrayList();
        
        for (Tool tool : tools) {
            if (tool.getKind() == Tool.FortranCompiler && !CppSettings.getDefault().isFortranEnabled())
                continue;
            names.add(tool.getGenericName());
        }
        return (String[])names.toArray(new String[names.size()]);
    }

    public int getToolKind(String genericName) {
        int kind = -1;
        for (int i = 0; i < tools.size(); i++) {
            Tool tool = tools.get(i);
            if (tools.get(i).getGenericName().equals(genericName)) {
                kind = tools.get(i).getKind();
                break;
            }
        }
        return kind;
    }
    
    public String getDynamicLibrarySearchOption() {
        return dynamicLibrarySearchOption;
    }

    public void setDynamicLibrarySearchOption(String dynamicLibrarySearchOption) {
        this.dynamicLibrarySearchOption = dynamicLibrarySearchOption;
    }

    public String getLibrarySearchOption() {
        return librarySearchOption;
    }

    public void setLibrarySearchOption(String librarySearchOption) {
        this.librarySearchOption = librarySearchOption;
    }

    public String getLibraryOption() {
        return libraryOption;
    }

    public void setLibraryOption(String libraryOption) {
        this.libraryOption = libraryOption;
    }
    
    public String toString() {
        return displayName;
    }
}
