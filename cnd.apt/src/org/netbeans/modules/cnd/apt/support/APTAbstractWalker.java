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

package org.netbeans.modules.cnd.apt.support;

import antlr.TokenStream;
import antlr.TokenStreamException;
import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.structure.APTUndefine;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * abstract Tree walker for APT
 * @author Vladimir Voskresensky
 */
public abstract class APTAbstractWalker extends APTWalker {
    
    private final APTPreprocHandler preprocHandler;
    private final String startPath;
    
    protected APTAbstractWalker(APTFile apt, APTPreprocHandler preprocHandler) {
        super(apt, preprocHandler == null ? null: preprocHandler.getMacroMap());
        this.startPath = apt.getPath();
        this.preprocHandler = preprocHandler;
    }
    
    protected void onInclude(APT apt) {
        if (getIncludeHandler() != null) {
            APTIncludeResolver resolver = getIncludeHandler().getResolver(startPath);
            ResolvedPath resolvedPath = resolver.resolveInclude((APTInclude)apt, getMacroMap());
            if (resolvedPath == null) {
                if (DebugUtils.STANDALONE) {
                    if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                        System.err.println("FAILED INCLUDE: from " + new File(startPath).getName() + " for:\n\t" + apt);// NOI18N
                    }
                } else {
                    APTUtils.LOG.log(Level.WARNING,
                            "failed resolving path from {0} for {1}", // NOI18N
                            new Object[] { startPath, apt });
                }
            }
            include(resolvedPath, (APTInclude)apt);
        }
    }
    
    protected void onIncludeNext(APT apt) {
        if (getIncludeHandler() != null) {
            APTIncludeResolver resolver = getIncludeHandler().getResolver(startPath);
            ResolvedPath resolvedPath = resolver.resolveIncludeNext((APTIncludeNext)apt, getMacroMap());
            if (resolvedPath == null) {
                if (DebugUtils.STANDALONE) {
                    if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                        System.err.println("FAILED INCLUDE: from " + new File(startPath).getName() + " for:\n\t" + apt);// NOI18N
                    }
                } else {
                    APTUtils.LOG.log(Level.WARNING,
                            "failed resolving path from {0} for {1}", // NOI18N
                            new Object[] { startPath, apt });
                }
            }
            include(resolvedPath, (APTInclude) apt);
        }
    }
    
    abstract protected void include(ResolvedPath resolvedPath, APTInclude aPTInclude);
   
    protected void onDefine(APT apt) {
        APTDefine define = (APTDefine)apt;
        if (define.isValid()) {
            getMacroMap().define(define.getName(), define.getParams(), define.getBody());
        } else {
            if (DebugUtils.STANDALONE) {
                if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                    System.err.println("INCORRECT #define directive: in " + new File(startPath).getName() + " for:\n\t" + apt);// NOI18N
                }
            } else {
                APTUtils.LOG.log(Level.SEVERE,
                        "INCORRECT #define directive: in {0} for:\n\t{1}", // NOI18N
                        new Object[] { new File(startPath).getName(), apt });
            }
        }
    }
    
    protected void onUndef(APT apt) {
        APTUndefine undef = (APTUndefine)apt;
        getMacroMap().undef(undef.getName());
    }
    
    protected boolean onIf(APT apt) {
        return eval(apt);
    }
    
    protected boolean onIfdef(APT apt) {
        return eval(apt);
    }
    
    protected boolean onIfndef(APT apt) {
        return eval(apt);
    }
    
    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        return !wasInPrevBranch && eval(apt);
    }
    
    protected boolean onElse(APT apt, boolean wasInPrevBranch) {
        return !wasInPrevBranch;
    }
    
    protected void onEndif(APT apt, boolean wasInBranch) {
    }

    protected APTPreprocHandler getPreprocHandler() {
        return preprocHandler;
    }
    
    protected APTIncludeHandler getIncludeHandler() {
        return getPreprocHandler() == null ? null: getPreprocHandler().getIncludeHandler();
    }   
 
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
   
    private boolean eval(APT apt) {
        APTUtils.LOG.log(Level.FINE, "eval condition for {0}", new Object[] {apt});// NOI18N
        boolean res = false;
        try {
            res = APTConditionResolver.evaluate(apt, getMacroMap());
        } catch (TokenStreamException ex) {
            APTUtils.LOG.log(Level.SEVERE, "error on evaluating condition node " + apt, ex);// NOI18N
        }
        return res;
    }
}
