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

import antlr.Utils;
import antlr.collections.AST;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.modelimpl.cache.*;

/**
 * cache entry:
 * 
 * @author Vladimir Voskresensky
 */
public final class FileCacheImpl implements Serializable, FileCache {
    private static final long serialVersionUID = -7790789617759717721L;
    
    
    private static class Header implements Serializable {
        private static final long serialVersionUID = -7790789617759717720L;
        
        // position in file of full APT
        private long aptPos = 0;
        // position in file 
        private long astPos = 0;
    }
    
    // information about position of different parts in cache file
    transient private Header header;
    // light APT
    transient private APTFile aptLight = null;
    // full APT
    transient private APTFile aptFull = null;
    // AST
    transient private AST ast = null;
    
    public FileCacheImpl(APTFile aptLight, APTFile aptFull, AST ast) {
        this.aptLight = aptLight;
        this.aptFull = aptFull;
        this.ast = ast;
    }

    public APTFile getAPTLight() {
        return aptLight;
    }

    public void setAPTLight(APTFile aptLight) {
        this.aptLight = aptLight;
    }
    
    public APTFile getAPT() {
        return aptFull;
    }

    public void setAPT(APTFile aptFull) {
        this.aptFull = aptFull;
    }    
    
    public AST getAST(APTPreprocHandler preprocHandler) {
        // we use the same AST now
        return ast;
    }

    public void setAST(AST ast, APTPreprocHandler preprocHandler) {
        // we use the same AST now
        this.ast = ast;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        APTSerializeUtils.writeAPT(out, aptLight);        
        //APTSerializeUtils.writeAPT(out, aptFull);
        CacheUtil.writeAST(out, ast);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {       
        in.defaultReadObject();
        aptLight = (APTFile) APTSerializeUtils.readAPT(in);
        //aptFull = (APTFile) APTSerializeUtils.readAPT(in);
        ast = CacheUtil.readAST(in);
    }    
}
