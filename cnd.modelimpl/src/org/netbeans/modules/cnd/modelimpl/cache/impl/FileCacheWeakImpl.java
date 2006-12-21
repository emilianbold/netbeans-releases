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

package org.netbeans.modules.cnd.modelimpl.cache.impl;

import antlr.collections.AST;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import org.netbeans.modules.cnd.modelimpl.apt.structure.APTFile;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.modelimpl.cache.*;

/**
 * memory-sensitive cache entry:
 *  - APT light (soft reference)
 *  - APT (weak reference)
 *  - AST (weak reference)
 * @author Vladimir Voskresensky
 */
final class FileCacheWeakImpl implements FileCache {
    // light APT
    private SoftReference/*<APTFile>*/ aptLight = null;
    // full APT
    private SoftReference/*<APTFile>*/ aptFull = null;
    // AST
    private SoftReference/*<APT>*/ ast = null;

    public FileCacheWeakImpl() {
        
    }
    
    public FileCacheWeakImpl(FileCache cache) {
        assert (cache != null);
        this.aptLight = new SoftReference(cache.getAPTLight());
        this.aptFull = new SoftReference(cache.getAPT());
        this.ast = new SoftReference(cache.getAST(null));
    }

    public APTFile getAPTLight() {
        return aptLight != null ? (APTFile) aptLight.get()  : null;
    }

    public void setAPTLight(APTFile aptLight) {
        this.aptLight = new SoftReference(aptLight);
    }
    
    public APTFile getAPT() {
        return aptFull != null ? (APTFile) aptFull.get() : null;
    }

    public void setAPT(APTFile aptFull) {
        this.aptFull = new SoftReference(aptFull);
    }    
    
    public AST getAST(APTPreprocState preprocState) {
        // we use the same AST now
        return ast != null ? (AST) ast.get() : null;
    }

    public void setAST(AST ast, APTPreprocState preprocState) {
        // we use the same AST now
        this.ast = new SoftReference(ast);
    }
    
//    private Object aptLock = new Object() {
//        public String toString() {
//            return "APT lock";
//        }
//    };
//
//    private Object aptLightLock = new Object() {
//        public String toString() {
//            return "APT Light lock";
//        }
//    };
//    
//
//    private Object astLock = new Object() {
//        public String toString() {
//            return "AST lock";
//        }
//    };     
}
