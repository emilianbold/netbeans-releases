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

package org.netbeans.modules.autoupdate.updateprovider;

import java.util.*;
import java.util.jar.Attributes;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/** A fake module info class initialized from a manifest but not backed by a real JAR.
 * Used for purposes of comparisons to real modules and updates and so on.
 * @author Jesse Glick
 */
final class DummyModuleInfo extends ModuleInfo {
    
//    private static AutomaticDependencies autoDepsHandler = null;
//    
//    /**
//     * Roughly copied from NbInstaller.refineDependencies.
//     * @see "#29577"
//     */
//    private static synchronized AutomaticDependencies getAutoDepsHandler() {
//        if (autoDepsHandler == null) {
//            FileObject depsFolder = Repository.getDefault().getDefaultFileSystem().findResource("ModuleAutoDeps"); // NOI18N
//            if (depsFolder != null) {
//                FileObject[] kids = depsFolder.getChildren();
//                List urls = new ArrayList(Math.max(kids.length, 1)); // List<URL>
//                for (int i = 0; i < kids.length; i++) {
//                    if (kids[i].hasExt("xml")) {
//                        try {
//                            urls.add(kids[i].getURL());
//                        }
//                        catch (FileStateInvalidException e) {
//                            Exceptions.printStackTrace(e);
//                        }
//                    }
//                }
//                try {
//                    autoDepsHandler = AutomaticDependencies.parse((URL[])urls.toArray(new URL[urls.size()]));
//                } catch (IOException e) {
//                    Exceptions.printStackTrace(e);
//                } catch (SAXException e) {
//                    Exceptions.printStackTrace(e);
//                }
//            }
//            if (autoDepsHandler == null) {
//                // Parsing failed, or no files.
//                autoDepsHandler = AutomaticDependencies.empty();
//            }
//        }
//        return autoDepsHandler;
//    }
//    
    private final Attributes attr;
    private final Set<Dependency> deps;
    private final String[] provides;
    
    /** Create a new fake module based on manifest.
     * Only main attributes need be presented, so
     * only pass these.
     */
    public DummyModuleInfo(Attributes attr) throws IllegalArgumentException {
        this.attr = attr;
        if (getCodeName() == null) throw new IllegalArgumentException();
        String cnb = getCodeNameBase();
        try {
            getSpecificationVersion();
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(nfe.toString() + " from " + cnb); // NOI18N
        }
        deps = parseDeps (attr, cnb);
//        getAutoDepsHandler().refineDependencies(cnb, deps); // #29577
        String providesS = attr.getValue("OpenIDE-Module-Provides"); // NOI18N
        if (providesS == null) {
            provides = new String[0];
        } else {
            StringTokenizer tok = new StringTokenizer(providesS, ", "); // NOI18N
            provides = new String[tok.countTokens()];
            for (int i = 0; i < provides.length; i++) {
                provides[i] = tok.nextToken();
            }
        }
        // XXX could do more error checking but this is probably plenty
    }
    
    public boolean isEnabled() {
        return false;
    }
    
    public SpecificationVersion getSpecificationVersion() {
        String sv = attr.getValue("OpenIDE-Module-Specification-Version"); // NOI18N
        return (sv == null ? null : new SpecificationVersion(sv));
    }
    
    public String getCodeName() {
        return attr.getValue("OpenIDE-Module"); // NOI18N
    }
    
    public int getCodeNameRelease() {
        String s = getCodeName();
        int idx = s.lastIndexOf('/'); // NOI18N
        if (idx == -1) {
            return -1;
        } else {
            return Integer.parseInt(s.substring(idx + 1));
        }
    }
    
    public String getCodeNameBase() {
        String s = getCodeName();
        int idx = s.lastIndexOf('/'); // NOI18N
        if (idx == -1) {
            return s;
        } else {
            return s.substring(0, idx);
        }
    }
    
    public Object getLocalizedAttribute(String a) {
        return attr.getValue(a);
    }
    
    public Object getAttribute(String a) {
        return attr.getValue(a);
    }
    
    /** Get a list of all dependencies this module has.  */
    public Set<Dependency> getDependencies() {
        return deps;
    }
    
    private final static Set<Dependency> parseDeps(Attributes attr, String cnb) throws IllegalArgumentException {
        Set<Dependency> s = new HashSet<Dependency> ();
        s.addAll(Dependency.create(Dependency.TYPE_MODULE, attr.getValue("OpenIDE-Module-Module-Dependencies"))); // NOI18N
        s.addAll(Dependency.create(Dependency.TYPE_PACKAGE, attr.getValue("OpenIDE-Module-Package-Dependencies"))); // NOI18N
        s.addAll(Dependency.create(Dependency.TYPE_JAVA, attr.getValue("OpenIDE-Module-Java-Dependencies"))); // NOI18N
        s.addAll(Dependency.create(Dependency.TYPE_REQUIRES, attr.getValue("OpenIDE-Module-Requires"))); // NOI18N
        s.addAll(Dependency.create(Dependency.TYPE_NEEDS, attr.getValue("OpenIDE-Module-Needs"))); // NOI18N
        s.addAll(Dependency.create(Dependency.TYPE_RECOMMENDS, attr.getValue("OpenIDE-Module-Recommends"))); // NOI18N
        SpecificationVersion api = null;
        String impl = null;
        String major = null;
        if (api != null) {
            s.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide" + major + " > " + api)); // NOI18N
        }
        if (impl != null) {
            s.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide" + major + " = " + impl)); // NOI18N
        }
        if (api == null && impl == null) {
            // All modules implicitly depend on openide.
            // Needed for #29577.
            //s.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide/1 > 0")); // NOI18N
        }
        return s;
    }
    
    public boolean owns(Class clazz) {
        return false;
    }
    
    public String[] getProvides() {
        return provides;
    }
}
