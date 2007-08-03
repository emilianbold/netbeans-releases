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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.cnd.compilers.DefaultCompilerProvider;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * A container for information about a set of related compilers, typicaly from a vendor or
 * redistributor.
 */
public class CompilerSet {
    
    /** Recognized (and prioritized) types of compiler sets */
    public enum CompilerFlavor {
            Sun12("Sun12"), // NOI18N
            Sun11("Sun11"), // NOI18N
            Sun10("Sun10"), // NOI18N
            Sun9("Sun9"), // NOI18N
            Sun8("Sun8"), // NOI18N
            Sun("Sun"), // NOI18N
            SunUCB("SunUCB"), // NOI18N
            GNU("GNU"), // NOI18N
            Cygwin("Cygwin"), // NOI18N
            MinGW("MinGW"), // NOI18N
            DJGPP("DJGPP"), // NOI18N
            Interix("Interix"), // NOI18N
            Unknown("Unknown"); // NOI18N
    
        private String sval;
        private int id;
        
        CompilerFlavor(String sval) {
            this.sval = sval;
            id = 0;
        }
        
        protected int nextId() {
            return id++;
        }
        
        public boolean isGnuCompiler() {
            return this == GNU || this == Cygwin || this == MinGW || this == DJGPP || this == Interix;
        }
        
        public boolean isSunCompiler() {
            return isSunStudioCompiler() || this == SunUCB;
        }
        
        public boolean isSunStudioCompiler() {
            return this == Sun12 || this == Sun11 || this == Sun10 || this == Sun9 || this == Sun8 || this == Sun;
        }
        
        public boolean isSunUCBCompiler() {
            return this == SunUCB;
        }
        
        public static CompilerFlavor toFlavor(String name) {
            if (name != null) {
                if (name.equals("Sun")) { // NOI18N
                    return Sun;
                } else if (name.equals("Sun12")) { // NOI18N
                    return Sun12;
                } else if (name.equals("Sun11")) { // NOI18N
                    return Sun11;
                } else if (name.equals("Sun10")) { // NOI18N
                    return Sun10;
                } else if (name.equals("Sun9")) { // NOI18N
                    return Sun9;
                } else if (name.equals("Sun8")) { // NOI18N
                    return Sun8;
                } else if (name.equals("SunUCB")) { // NOI18N
                    return SunUCB;
                } else if (name.equals("Cygwin")) { // NOI18N
                    return Cygwin;
                } else if (name.equals("MinGW")) { // NOI18N
                    return MinGW;
                } else if (name.equals("DJGPP")) { // NOI18N
                    return DJGPP;
                } else if (name.equals("Interix")) { // NOI18N
                    return Interix;
                } else if (name.equals("Unknown")) { // NOI18N
//                    return Unknown;
                    return GNU; // No current support for Unknown, map it to GNU
                }
            }
            return GNU;
        }
    
        public String toString() {
            return sval;
        }
    };
    
    public static final String None = "None"; // NOI18N
    
    private static HashMap<String, CompilerSet> csmap = new HashMap();
    private static HashMap<String, CompilerSet> basemap = new HashMap();
    
    private CompilerFlavor flavor;
    private int id;
    private String name;
    private String displayName;
    private StringBuffer directory = new StringBuffer(256);
    private ArrayList<Tool> tools = new ArrayList();
    private String librarySearchOption;
    private String dynamicLibrarySearchOption;
    private String libraryOption;
    private CompilerProvider compilerProvider;
    
    private String[] noCompDNames = {
        NbBundle.getMessage(CompilerSet.class, "LBL_NoCCompiler"), // NOI18N
        NbBundle.getMessage(CompilerSet.class, "LBL_NoCppCompiler"), // NOI18N
        NbBundle.getMessage(CompilerSet.class, "LBL_NoFortranCompiler"), // NOI18N
        NbBundle.getMessage(CompilerSet.class, "LBL_NoCustomBuildTool") // NOI18N
    };
    
    /** Creates a new instance of CompilerSet */
    protected CompilerSet(CompilerFlavor flavor, String directory) {
        addDirectory(directory);
        
        compilerProvider = (CompilerProvider) Lookup.getDefault().lookup(CompilerProvider.class);
        if (compilerProvider == null) {
            compilerProvider = new DefaultCompilerProvider();
        }
        
        switch (flavor) {
            case Interix:
                basemap.put(getBase(directory), this);
                break;
                
            case Sun:
                flavor = getBestSunStudioFlavor(flavor, directory);
                break;
        }
        
        if (directory.length() > 0) {
            id = flavor.nextId();
        }
        name = flavor.toString();
        displayName = mapNameToDisplayName(flavor, directory.length() == 0);
        if (flavor.isSunCompiler()) {
            librarySearchOption = "-L"; // NOI18N
            dynamicLibrarySearchOption = "-R"; // NOI18N
            libraryOption = "-l"; // NOI18N
        }
        else if (flavor.isGnuCompiler() && Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            librarySearchOption = "-L"; // NOI18N
            dynamicLibrarySearchOption = "-R"; // NOI18N
            libraryOption = "-l"; // NOI18N
        }
        else if (flavor.isGnuCompiler()) {
            librarySearchOption = "-L"; // NOI18N
            dynamicLibrarySearchOption = "-Wl,-rpath "; // NOI18N
            libraryOption = "-l"; // NOI18N
        }
        this.flavor = flavor;
        csmap.put(directory, this);
    }
    
    protected CompilerSet() {
        this.name = None;
        this.flavor = CompilerFlavor.Unknown;
        this.displayName = NbBundle.getMessage(CompilerSet.class, "LBL_EmptyCompilerSetDisplayName"); // NOI18N
        
        compilerProvider = (CompilerProvider) Lookup.getDefault().lookup(CompilerProvider.class);
        if (compilerProvider == null) {
            compilerProvider = new DefaultCompilerProvider();
        }
    }
    
    /**
     * Get an existing compiler set. If it doesn't exist, get an empty one based on the requested name.
     *
     * @param name The name of the compiler set we want
     * @returns The best fitting compiler set (may be an empty CompilerSet)
     */
    public static CompilerSet getCompilerSet(String name) {
        CompilerSet cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.toFlavor(name));
        
        if (cs == null) {
            if (name.startsWith("Sun")) { // NOI18N
                cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.Sun12);
                if (cs == null) {
                    cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.Sun11);
                }
                if (cs == null) {
                    cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.Sun10);
                }
                if (cs == null) {
                    cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.Sun9);
                }
                if (cs == null) {
                    cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.Sun8);
                }
            } else {
                if (Utilities.isWindows()) {
                    cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.Cygwin);
                    if (cs == null) {
                        cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.MinGW);
                    }
                    if (cs == null) {
                        cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.Interix);
                    }
                    if (cs == null) {
                        cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.DJGPP);
                    }
                    if (cs == null) {
                        cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.GNU);
                    }
                } else {
                    cs = CompilerSetManager.getDefault().getCompilerSet(CompilerFlavor.GNU);
                }
            }
            if (cs == null) {
                CompilerFlavor flavor = CompilerFlavor.toFlavor(name);
                flavor = flavor == null ? CompilerFlavor.Unknown : flavor;
                cs = new CompilerSet(flavor, ""); // NOI18N
            }
        }
        return cs;
    }
    
    public static CompilerSet getCompilerSet(String directory, String[] list) {
        CompilerSet cs = csmap.get(directory);
        if (cs != null) {
            return cs;
        }
        String base = getBase(directory);
        if (base.length() > 0) {
            cs = basemap.get(base);
            if (cs != null) {
                cs.addDirectory(directory);
                return cs;
            }
        }
        
        if (Utilities.isWindows()) {
            if (directory.toLowerCase().indexOf("cygwin") != -1) { // NOI18N
                return new CompilerSet(CompilerFlavor.Cygwin, directory);
            }
            if (directory.toLowerCase().indexOf("mingw") != -1) { // NOI18N
                return new CompilerSet(CompilerFlavor.MinGW, directory);
            }
            if (directory.toLowerCase().indexOf("sfu") != -1 || directory.toLowerCase().indexOf("sua") != -1) { // NOI18N
                return new CompilerSet(CompilerFlavor.Interix, directory);
            }
        } else {
            if (isSunCompilerDirectory(directory)) { 
                return new CompilerSet(CompilerFlavor.Sun, directory);
            } else if (isSunUCBCompilerDirectory(directory)) { 
                return new CompilerSet(CompilerFlavor.SunUCB, directory);
            } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS &&
                    (directory.equals("/usr/bin") || directory.equals("/bin"))) { // NOI18N
                for (int i = 0; i < list.length; i++) {
                    if (list[i].equals("cc") || list[i].equals("CC")) { // NOI18N
                        // Can't verify version, so just return Sun
                        return new CompilerSet(CompilerFlavor.Sun, directory);
                    }
                }
            }
        }
        
        // So far we havne't been able to determine the compiler set flavor. Look at the
        // names in list and see if we can from it. If not, assume its unknown.
        for (String pgm : list) {
            if (pgm.indexOf("gcc") != -1 || pgm.indexOf("g++") != -1) { // NOI18N
                return new CompilerSet(CompilerFlavor.GNU, directory);
            }
        }
        
        return new CompilerSet(CompilerFlavor.GNU, directory);
    }
    
    /**
     * If no compilers are found an empty compiler set is created so we don't have an empty list.
     * Too many places in CND expect a non-empty list and throw NPEs if it is empty!
     */
    protected static CompilerSet createEmptyCompilerSet() {
        return new CompilerSet();
    }
    
    private String mapNameToDisplayName(CompilerFlavor flavor, boolean isMissing) {
        String displayName;
        StringBuffer label = new StringBuffer("LBL_"); // NOI18N
        
        label.append(flavor);
        label.append("CompilerSet_"); // NOI18N
        if (isMissing) {
            label.append("Missing"); // NOI18N
        } else {
            label.append(id == 0 ? "0" : "X"); // NOI18N
        }
        return NbBundle.getMessage(CompilerSet.class, label.toString(), Integer.valueOf(id));
    }
    
    private static CompilerFlavor getBestSunStudioFlavor(CompilerFlavor flavor, String dir) {
        String inventory;
        File finv;
        
        if (dir.contains("/prod/bin")) { // NOI18N
            inventory = "/../../inventory"; // NOI18N
        } else {
            inventory = "/../inventory"; // NOI18N
        }
        
        finv = new File(dir + inventory);
        if (finv.exists() && finv.isDirectory()) {
            String[] dirs = finv.list();
            for (int i = 0; i < dirs.length; i++) {
                if (dirs[i].startsWith("v16")) { // NOI18N
                    return CompilerFlavor.Sun12;
                }
                if (dirs[i].startsWith("v15")) { // NOI18N
                    return CompilerFlavor.Sun11;
                }
                if (dirs[i].startsWith("v14")) { // NOI18N
                    return CompilerFlavor.Sun10;
                }
                if (dirs[i].startsWith("v13")) { // NOI18N
                    return CompilerFlavor.Sun9;
                }
                if (dirs[i].startsWith("v12")) { // NOI18N
                    return CompilerFlavor.Sun8;
                }
            }
        }
        return flavor;
    }
    
    private static boolean isSunCompilerDirectory(String dir) {
        if (dir.indexOf("SUNWspro") != -1 || dir.indexOf("/prod/bin") != -1) { // NOI18N
            return true;
        } else {
            File prod = new File(dir + "/../prod"); // NOI18N
            return prod.exists() && prod.isDirectory();
        }
    }
    
    private static boolean isSunUCBCompilerDirectory(String dir) {
        return Utilities.getOperatingSystem() == Utilities.OS_SOLARIS && dir.equals("/usr/ucb"); // NOI18N
    }
    
    protected int getID() {
        return id;
    }
        
    public boolean isGnuCompiler() {
        return flavor.isGnuCompiler();
    }

    public boolean isSunCompiler() {
        return flavor.isSunCompiler();
    }

    public boolean isSunUCBCompiler() {
        return flavor.isSunUCBCompiler();
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
            int pos = directory.indexOf("\\opt\\gcc");  // NOI18N
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
    
    private static HashMap<String, Tool> cache = new HashMap();
    
    public Tool addTool(String name, String path, int kind) {
        String fullpath = name.length() > 0 ? path + File.separator + name : path;
        Tool tool = cache.get(fullpath + kind);
        if (tool == null) {
            tool = compilerProvider.createCompiler(flavor, kind, name, Tool.getToolDisplayName(kind), path);
            if (fullpath.length() > 0) {
                cache.put(fullpath + kind, tool);
            }
        }
        if (!tools.contains(tool)) {
            tools.add(tool);
        }
        return tool;
    }
    
    public void removeTool(String name, String path, int kind) {
        for (Tool tool : tools) {
            if (tool.getName().equals(name) && tool.getPath().equals(path) && tool.getKind() == kind) {
                tools.remove(tool);
                return;
            }
        }
    }
    
    /**
     * Get a tool by name
     *
     * @param name The name of the desired tool
     * @return The Tool or null
     */
    public Tool getTool(String name) {
        String exename = null;
        
        if (Utilities.isWindows()) {
            exename = name + ".exe"; // NOI18N
        }
        for (Tool tool : tools) {
            if (tool.getDisplayName().equals(name) || tool.getName().equals(name) ||
                    (exename != null && tool.getName().equals(exename))) {
                return tool;
            }
        }
        return null;
    }
    
    /**
     * Get a tool by name
     *
     * @param name The name of the desired tool
     * @return The Tool or null
     */
    public Tool getTool(String name, int kind) {
        String exename = null;
        
        if (Utilities.isWindows()) {
            exename = name + ".exe"; // NOI18N
        }
        for (Tool tool : tools) {
            if ((tool.getDisplayName().equals(name) || tool.getName().equals(name) ||
                    (exename != null && tool.getName().equals(exename))) && kind == tool.getKind()) {
                return tool;
            }
        }
        return compilerProvider.createCompiler(CompilerFlavor.Unknown, kind, "", noCompDNames[kind], ""); // NOI18N
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
        Tool t = compilerProvider.createCompiler(CompilerFlavor.Unknown, kind, "", noCompDNames[kind], ""); // NOI18N
        tools.add(t);
        return t;
    }
    
    public boolean isValid() {
        Tool cCompiler = getTool(Tool.CCompiler);
        Tool cppCompiler = getTool(Tool.CCCompiler);
        Tool fortranCompiler = getTool(Tool.FortranCompiler);
        
        return cCompiler != null && cppCompiler != null && (!CppSettings.getDefault().isFortranEnabled() || fortranCompiler != null);
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
