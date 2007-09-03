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

package org.netbeans.modules.autoupdate.services;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;

class DependencyChecker extends Object {

    private static final Logger err = Logger.getLogger(DependencyChecker.class.getName ()); // NOI18N
    
    public static Set<Dependency> findBrokenDependencies (Set<Dependency> deps, Collection<ModuleInfo> modules) {
        Set<Dependency> res = new HashSet<Dependency> ();
        for (Dependency dep : deps) {
            err.log(Level.FINE, "Dependency[" + dep.getType () + "]: " + dep);
            switch (dep.getType ()) {
                case (Dependency.TYPE_REQUIRES) :
                    if (findModuleMatchesDependencyRequires (dep, modules) != null) {
                        // ok
                    } else {
                        // bad, report missing module
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_NEEDS) :
                    if (findModuleMatchesDependencyRequires (dep, modules) != null) {
                        // ok
                    } else {
                        // bad, report missing module
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_RECOMMENDS) :
                    if (findModuleMatchesDependencyRequires (dep, modules) != null) {
                        // ok
                    } else {
                        // bad, report missing module
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_MODULE) :
                    if (matchDependencyModule (dep, modules)) {
                        // ok
                    } else {
                        // bad, report missing module
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_JAVA) :
                    err.log(Level.FINE, "Check dependency on Java platform. Dependency: " + dep);
                    break;
                default:
                    //assert false : "Unknown type of Dependency, was " + dep.getType ();
                    err.log(Level.FINE, "Uncovered Dependency " + dep);                    
            }
        }
        return res;
    }
    
    static ModuleInfo findModuleMatchesDependencyRequires (Dependency dep, Collection<ModuleInfo> modules) {
        for (ModuleInfo info : modules) {
            if (Arrays.asList (info.getProvides ()).contains (dep.getName ())) {
                return info;
            }
        }
        return null;
    }
    
    private static boolean matchDependencyModule (Dependency dep, Collection<ModuleInfo> modules) {
        for (ModuleInfo module : modules) {
            if (checkDependencyModule (dep, module)) {
                return true;
            }
        }
        
        return false;
    }
    
    static boolean checkDependencyModuleAllowEqual (Dependency dep, ModuleInfo module) {
        return checkDependencyModule (dep, module, true);
    }
    
    static boolean checkDependencyModule (Dependency dep, ModuleInfo module) {
        return checkDependencyModule (dep, module, false);
    }
    
    private static boolean checkDependencyModule (Dependency dep, ModuleInfo module, boolean allowEqual) {

        boolean ok = false;
        
        if (dep.getName ().equals (module.getCodeNameBase ()) || dep.getName ().equals (module.getCodeName ())) {
            if (dep.getComparison () == Dependency.COMPARE_ANY) {
                ok = true;
            } else if (dep.getComparison () == Dependency.COMPARE_SPEC) {
                    if (module.getSpecificationVersion () == null) {
                        ok = false;
                    } else if (new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) > 0) {
                        ok = false;
                    } else if (allowEqual && new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) == 0) {
                        ok = true;
                    } else {
                        ok = true;
                    }
            } else {
                // COMPARE_IMPL
                if (module.getImplementationVersion () == null) {
                    ok = false;
                } else if (! module.getImplementationVersion ().equals (dep.getVersion ())) {
                    ok = false;
                } else {
                    ok = true;
                }
            }
            
        } else {

            int dash = dep.getName ().indexOf ('-'); // NOI18N
            if (dash != -1) {
                // Ranged major release version, cf. #19714.
                int slash = dep.getName ().indexOf ('/'); // NOI18N
                String cnb = dep.getName ().substring (0, slash);
                int relMin = Integer.parseInt (dep.getName ().substring (slash + 1, dash));
                int relMax = Integer.parseInt (dep.getName ().substring (dash + 1));
                if (cnb.equals (module.getCodeNameBase ()) &&
                        relMin <= module.getCodeNameRelease () &&
                        relMax >= module.getCodeNameRelease ()) {
                    if (dep.getComparison () == Dependency.COMPARE_ANY) {
                        ok = true;
                    } else {
                        // COMPARE_SPEC; COMPARE_IMPL not allowed here
                        if (module.getCodeNameRelease () > relMin) {
                            // Great, skip the spec version.
                            ok = true;
                        } else {
                            // As usual.
                            if (module.getSpecificationVersion () == null) {
                                ok = false;
                            } else if (new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) > 0) {
                                ok = false;
                            } else if (allowEqual && new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) > 0) {
                                ok = true;
                            } else {
                                ok = true;
                            }
                        }
                    }
                }
            }
        }

        return ok;
    }

}
