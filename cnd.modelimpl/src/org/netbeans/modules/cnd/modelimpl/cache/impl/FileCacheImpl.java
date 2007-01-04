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

import antlr.Utils;
import antlr.collections.AST;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
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
    
    public AST getAST(APTPreprocState preprocState) {
        // we use the same AST now
        return ast;
    }

    public void setAST(AST ast, APTPreprocState preprocState) {
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
