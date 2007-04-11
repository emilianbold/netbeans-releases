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

package org.netbeans.modules.cnd.makeproject.api.compilers;

import org.netbeans.modules.cnd.api.compilers.CompilerProvider;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.openide.util.NbBundle;

/**
 * Override the cnd default compiler type "Tool". MakeProjects uses classes derived from Tool but cnd/core
 * can't depend on makeproject classes. So this allows makeproject to provide a tool creator factory.
 *
 * @author gordonp
 */
public class MakeProjectCompilerProvider extends CompilerProvider {
    
    /**
     * Create a class derived from Tool
     *
     * Thomas: If you want/need different informatio to choose which Tool derived class to create we can change
     * this method. We can also add others, if desired. This was mainly a proof-of-concept that tool creation
     * could be deferred to makeproject.
     */
    public Tool createCompiler(CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        if (flavor.isSunCompiler()) {
            if (kind == Tool.CCompiler) {
                return new SunCCompiler(flavor, kind, name, displayName, path);
            } else if (kind == Tool.CCCompiler) {
                return new SunCCCompiler(flavor, kind, name, displayName, path);
            } else if (kind == Tool.FortranCompiler) {
                return new SunFortranCompiler(flavor, kind, name, displayName, path);
            }
        } else /* if (flavor.isGnuCompiler()) */ { // Assume GNU (makeproject system doesn't handle Unknown)
            if (kind == Tool.CCompiler) {
                return new GNUCCompiler(flavor, kind, name, displayName, path);
            } else if (kind == Tool.CCCompiler) {
                return new GNUCCCompiler(flavor, kind, name, displayName, path);
            } else if (kind == Tool.FortranCompiler) {
                return new GNUFortranCompiler(flavor, kind, name, displayName, path);
            }
        }
        if (kind == Tool.CustomTool) {
            return new CustomTool();
        }
        throw new IllegalArgumentException(NbBundle.getMessage(MakeProjectCompilerProvider.class, 
                "ERR_UnrecognizedCompilerType")); // NOI18N
    }
    
}
