/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.util.Iterator;
import java.util.Set;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.netbeans.*;

// May use NbBundle, but preferably nothing else.

/**
 * Utility class to provide localized messages explaining problems
 * that modules had during attempted installation.
 * Used by both {@link NbEvents} and {@link org.netbeans.core.ui.ModuleBean}.
 * @author Jesse Glick
 * @see "#16636"
 */
public final class NbProblemDisplayer {
    
    private NbProblemDisplayer() {}
    
    /**
     * Provide a localized explanation of some installation problem.
     * Problem may be either an InvalidException or a Dependency.
     * Structure of message can assume that the module failing its
     * dependencies is already being displayed, and concentrate
     * on the problem.
     * @param m the module which cannot be installed
     * @param problem either an {@link InvalidException} or {@link Dependency} as returned from {@link Module#getProblems}
     * @return an explanation of the problem in the most human-friendly format available
     */
    public static String messageForProblem(Module m, Object problem) {
        if (problem instanceof InvalidException) {
            return Util.findLocalizedMessage((InvalidException)problem, true);
        } else {
            Dependency dep = (Dependency)problem;
            switch (dep.getType()) {
            case Dependency.TYPE_MODULE:
                String polite = (String)m.getLocalizedAttribute("OpenIDE-Module-Module-Dependency-Message"); // NOI18N
                if (polite != null) {
                    return polite;
                } else {
                    String name = dep.getName();
                    // Find code name base:
                    int idx = name.lastIndexOf('/');
                    if (idx != -1) {
                        name = name.substring(0, idx);
                    }
                    Module other = m.getManager().get(name);
                    if (other != null && other.getCodeName().equals(dep.getName())) {
                        switch (dep.getComparison()) {
                        case Dependency.COMPARE_ANY:
                            // Just disabled (probably had its own problems).
                            return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_disabled", other.getDisplayName());
                        case Dependency.COMPARE_IMPL:
                            String requestedI = dep.getVersion();
                            String actualI = (other.getImplementationVersion() != null) ?
                                other.getImplementationVersion() :
                                NbBundle.getMessage(NbProblemDisplayer.class, "LBL_no_impl_version");
                            if (requestedI.equals(actualI)) {
                                // Just disabled (probably had its own problems).
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_disabled", other.getDisplayName());
                            } else {
                                // Wrong version.
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_wrong_version", other.getDisplayName(), requestedI, actualI);
                            }
                        case Dependency.COMPARE_SPEC:
                            SpecificationVersion requestedS = new SpecificationVersion(dep.getVersion());
                            SpecificationVersion actualS = (other.getSpecificationVersion() != null) ?
                                other.getSpecificationVersion() :
                                new SpecificationVersion("0"); // NOI18N
                            if (actualS.compareTo(requestedS) >= 0) {
                                // Just disabled (probably had its own problems).
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_disabled", other.getDisplayName());
                            } else {
                                // Too old.
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_too_old", other.getDisplayName(), requestedS, actualS);
                            }
                        default:
                            throw new IllegalStateException();
                        }
                    } else {
                        // Keep the release version info in this case.
                        // XXX would be nice to have a special message for mismatched major release
                        // version - i.e. other != null
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_needed_not_found", dep.getName());
                    }
                }
            case Dependency.TYPE_REQUIRES:
                polite = (String)m.getLocalizedAttribute("OpenIDE-Module-Requires-Message"); // NOI18N
                if (polite != null) {
                    return polite;
                } else {
                    Set others = m.getManager().getModules();
                    Iterator it = others.iterator();
                    while (it.hasNext()) {
                        Module other = (Module)it.next();
                        if (other.provides(dep.getName())) {
                            return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_require_disabled", dep.getName());
                        }
                    }
                    return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_require_not_found", dep.getName());
                }
            case Dependency.TYPE_PACKAGE:
                polite = (String)m.getLocalizedAttribute("OpenIDE-Module-Package-Dependency-Message"); // NOI18N
                if (polite != null) {
                    return polite;
                } else {
                    String name = dep.getName();
                    // Find package name or qualified name of probe class:
                    int idx = name.lastIndexOf('[');
                    if (idx == 0) {
                        // Probed class. [javax.television.Antenna]
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_class_not_loaded", name.substring(1, name.length() - 1));
                    } else if (idx != -1) {
                        // Package plus sample class. javax.television[Antenna]
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_package_not_loaded_or_old", name.substring(0, idx));
                    } else {
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_package_not_loaded_or_old", name);
                    }
                }
            case Dependency.TYPE_JAVA:
                return dep.toString();//XXX
            default:
                throw new IllegalArgumentException(dep.toString());
            }
        }
    }
    
}
