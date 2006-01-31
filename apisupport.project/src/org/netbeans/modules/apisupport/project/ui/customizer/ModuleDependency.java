/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.text.Collator;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.support.ant.PropertyUtils;

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
    
    private ModuleEntry me;
    
    private Set/*<String>*/ filterTokensNotFriend;
    private Set/*<String>*/ filterTokensFriend;
    
    public static final Comparator LOCALIZED_NAME_COMPARATOR;
    
    static {
        LOCALIZED_NAME_COMPARATOR = new Comparator() {
            public int compare(Object o1, Object o2) {
                ModuleEntry me1 = ((ModuleDependency) o1).getModuleEntry();
                ModuleEntry me2 = ((ModuleDependency) o2).getModuleEntry();
                int result = Collator.getInstance().compare(
                        me1.getLocalizedName(), me2.getLocalizedName());
                return result != 0 ? result :
                    me1.getCodeNameBase().compareTo(me2.getCodeNameBase());
            }
        };
    }
    
    /**
     * Creates a new instance based on the given entry. The instance will be
     * initialized with given entry's release and specification versions.
     * Compile dependency is set to true by default, implementation version to
     * false.
     */
    public ModuleDependency(ModuleEntry me) {
        this(me, me.getReleaseVersion(), me.getSpecificationVersion(), true, false);
    }
    
    public ModuleDependency(ModuleEntry me, String releaseVersion,
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
    
    public ModuleEntry getModuleEntry() {
        return me;
    }
    
    // XXX module dependencies are not the same also if their versioning varies.
    // So consider spec, release, and probably all <dependency> subelemenets recursively
    public int compareTo(Object o) {
        return getModuleEntry().getCodeNameBase().compareTo(
                ((ModuleDependency) o).getModuleEntry().getCodeNameBase());
    }
    
    public boolean equals(Object o) {
        boolean retval = false;
        if (o instanceof ModuleDependency) {
            retval = getModuleEntry().getCodeNameBase().equals(
                    ((ModuleDependency) o).getModuleEntry().getCodeNameBase());
        }
        return  retval;
    }
    
    public int hashCode() {
        return getModuleEntry().getCodeNameBase().hashCode();
    }
    
    public boolean hasCompileDependency() {
        return compileDep;
    }
    
    public boolean hasImplementationDepedendency() {
        return implDep;
    }
    
    /**
     * Return a set of tokens that can be used to search for this dependency.
     * Per UI spec, includes lower-case versions of:
     * <ol>
     * <li>the code name base
     * <li>the localized display name
     * <li> the full path to the module JAR or any Class-Path extension
     * <li> the fully-qualified class name (use . for inner classes) of any class
     * contained in the module JAR or any Class-Path extension which is in an package
     * which would be made available to the depending module when using a specification version dependency
     * </ol>
     * Note that the last item means that this can behave differently according to the depending
     * module (according to whether or not it would be listed as a friend).
     * @param dependingModuleCNB the CNB of the module depending on this one
     */
    Set/*<String>*/ getFilterTokens(String dependingModuleCNB) {
        boolean friend = me.isDeclaredAsFriend(dependingModuleCNB);
        Set/*<String>*/ filterTokens = friend ? filterTokensFriend : filterTokensNotFriend;
        if (filterTokens == null) {
            filterTokens = new HashSet();
            filterTokens.add(me.getCodeNameBase());
            filterTokens.add(me.getLocalizedName());
            filterTokens.add(me.getJarLocation().getAbsolutePath());
            String[] cpext = PropertyUtils.tokenizePath(me.getClassPathExtensions());
            for (int i = 0; i < cpext.length; i++) {
                filterTokens.add(cpext[i]);
            }
            if (friend) {
                Iterator it = me.getPublicClassNames().iterator();
                while (it.hasNext()) {
                    String clazz = (String) it.next();
                    filterTokens.add(clazz.replace('$', '.'));
                }
            }
            if (friend) {
                filterTokensFriend = filterTokens;
            } else {
                filterTokensNotFriend = filterTokens;
            }
        }
        return filterTokens;
    }
    
    public String toString() {
        return "ModuleDependency[me: " + getModuleEntry() + // NOI18N
                ", relVer: " + getReleaseVersion() + // NOI18N
                ", specVer: " + getSpecificationVersion() + // NOI18N
                ", implDep: " + hasImplementationDepedendency() + // NOI18N
                ", compDep: " + hasCompileDependency() + // NOI18N
                "]"; // NOI18N
    }
}
