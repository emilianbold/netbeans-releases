/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.cache.impl;

import antlr.collections.AST;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
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
    
    public AST getAST(APTPreprocHandler preprocHandler) {
        // we use the same AST now
        return ast != null ? (AST) ast.get() : null;
    }

    public void setAST(AST ast, APTPreprocHandler preprocHandler) {
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
