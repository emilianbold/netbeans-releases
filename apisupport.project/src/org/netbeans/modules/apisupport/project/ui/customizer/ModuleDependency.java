/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.util.Comparator;
import org.netbeans.modules.apisupport.project.ModuleList;

/**
 * Represents one module dependency. i.e. <em>&lt;dependency&gt;</em> element
 * in module's <em>project.xml</em> or one token in the
 * OpenIDE-Module-Module-Dependencies attribute of module manifest.
 *
 * @author Martin Krauskopf
 */
public final class ModuleDependency implements Comparable {
    
    private String releaseVersion;
    private String specVersion;
    private boolean implDep;
    private boolean compileDep;
    
    private ModuleList.Entry me;
    
    public static final Comparator CODE_NAME_BASE_COMPARATOR;
    
    static {
        CODE_NAME_BASE_COMPARATOR = new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((ModuleDependency) o1).getModuleEntry().getCodeNameBase().compareTo(
                        ((ModuleDependency) o2).getModuleEntry().getCodeNameBase());
            }
        };
    }
    
    public ModuleDependency(ModuleList.Entry me) {
        this(me, null, null, true, false);
    }
    
    public ModuleDependency(ModuleList.Entry me, String releaseVersion,
            String specVersion, boolean compileDep, boolean implDep) {
        this.me = me;
        
        // set versions to null if contain the same value as the given entry
        this.compileDep = compileDep;
        this.implDep = implDep;
        this.releaseVersion = releaseVersion;
        this.specVersion = specVersion;
    }
    
    /** may be null for none */
    public String getReleaseVersion() {
        return releaseVersion;
    }
    
    public String getSpecificationVersion() {
        return specVersion;
    }
    
    public ModuleList.Entry getModuleEntry() {
        return me;
    }
    
    public int compareTo(Object o) {
        return this.getModuleEntry().getLocalizedName().compareTo(
                ((ModuleDependency) o).getModuleEntry().getLocalizedName());
    }
    
    public boolean hasCompileDependency() {
        return compileDep;
    }
    
    public boolean hasImplementationDepedendency() {
        return implDep;
    }
}
