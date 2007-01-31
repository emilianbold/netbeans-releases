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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.trace;

import antlr.TokenStreamException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.impl.support.APTConditionResolver;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.structure.APTUndefine;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTIncludeResolver;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBufferFile;

/**
 * simple test implementation of walker
 * @author Vladimir Voskresensky
 */
public class APTWalkerTest extends APTWalker {
    private APTIncludeHandler inclHandler;
    private String path;
    
    public APTWalkerTest(APTFile apt, APTMacroMap macros, APTIncludeHandler inclHandler) {
        super(apt, macros);
        this.path = apt.getPath();
        this.inclHandler = inclHandler;
    }

    private long resolvingTime = 0;
    public long getIncludeResolvingTime() {
        return resolvingTime;
    }

    protected void onInclude(APT apt) {
        if (inclHandler != null) {
            long time = System.currentTimeMillis();
            APTIncludeResolver resolver = inclHandler.getResolver(path);
            String path = resolver.resolveInclude((APTInclude)apt, getMacroMap());
            resolvingTime += System.currentTimeMillis() - time;
            if (path == null) {
                APTUtils.LOG.log(Level.WARNING, 
                        "failed resolving path from {0} for {1}", // NOI18N
                        new Object[] {});
            }
            include(path, apt.getToken().getLine());
        }
    }

    protected void onIncludeNext(APT apt) {
        if (inclHandler != null) {
            long time = System.currentTimeMillis();
            APTIncludeResolver resolver = inclHandler.getResolver(path);           
            String path = resolver.resolveIncludeNext((APTIncludeNext)apt, getMacroMap());
            resolvingTime += System.currentTimeMillis() - time;
            if (path == null) {
                APTUtils.LOG.log(Level.WARNING, 
                        "failed resolving path from {0} for {1}", // NOI18N
                        new Object[] {});
            }
            include(path, apt.getToken().getLine());
        }
    }

    protected void onDefine(APT apt) {
        APTDefine define = (APTDefine)apt;
        getMacroMap().define(define.getName(), define.getParams(), define.getBody());
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

//    protected Token onToken(Token token) {
//        return token;
//    }
       
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    private boolean eval(APT apt) {
        APTUtils.LOG.log(Level.ALL, "eval condition for " + apt);// NOI18N
        boolean res = false;
        try {
            res = APTConditionResolver.evaluate(apt, getMacroMap());
        } catch (TokenStreamException ex) {
            APTUtils.LOG.log(Level.SEVERE, "error on evaluating condition node " + apt, ex);// NOI18N
        }
        return res;
    }

    private void include(String path, int directiveLine) {
        if (path != null && inclHandler.pushInclude(path, directiveLine)) {
            APTFile apt;
            boolean res = false;
            try {
                apt = APTDriver.getInstance().findAPTLight(new FileBufferFile(new File(path)));
                APTWalkerTest walker = new APTWalkerTest(apt, getMacroMap(), inclHandler);
                walker.visit();
                resolvingTime += walker.resolvingTime;
                res = true;               
            } catch (IOException ex) {
                APTUtils.LOG.log(Level.SEVERE, "error on include " + path, ex);// NOI18N
            } finally {
                inclHandler.popInclude(); 
            }
        }
    }
}
