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

package org.netbeans.modules.cnd.apt.impl.structure;

import antlr.Token;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTWalker;

/**
 * implementation of APTFile
 * @author Vladimir Voskresensky
 */
public final class APTFileNode extends APTContainerNode 
                                implements APTFile, Serializable {
    private static final long serialVersionUID = -6182803432699849825L;
    private String path;
    transient private boolean tokenized;
    
    /** Copy constructor */
    /**package*/ APTFileNode(APTFileNode orig) {
        super(orig);
        this.path = orig.path;
        this.tokenized = false;
    }
    
    /** Constructor for serialization */
    protected APTFileNode() {
        tokenized = false;
    }
    
    /** Creates a new instance of APTFileNode */
    public APTFileNode(String path) {
        this.path = path;
        tokenized = true;
    }
    
    public final int getType() {
        return APT.Type.FILE;
    }    
    

    public int getOffset() {
        return -1;
    }

    public int getEndOffset() {
        return -1;
    }
    
    public APT getNextSibling() {
        return null;
    }              

    public String getText() {
        return "FILE:{" + getPath() + "}";
    }

    public String getPath() {
        return path;
    }

    public void dispose() {
        if (isTokenized()) {
            new CleanTokensWalker(this).visit();
            tokenized = false;
        }
    }

    public boolean isTokenized() {
        return tokenized;
    }    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    
    public final void setNextSibling(APT next) {
        assert(false):"Illegal to add siblings to file node";
    }
    
    private static class CleanTokensWalker extends APTWalker {

        /** Creates a new instance of APTCleanTokensWalker */
        public CleanTokensWalker(APTFileNode apt) {
            super(apt, null);
        }

        protected void onInclude(APT apt) {
            // do nothing
        }

        protected void onIncludeNext(APT apt) {
            // do nothing
        }

        protected void onDefine(APT apt) {
            // do nothing
        }

        protected void onUndef(APT apt) {
            // do nothing
        }

        protected boolean onIf(APT apt) {
            // always return true, because we want to visit all branches
            return true;
        }

        protected boolean onIfdef(APT apt) {
            // always return true, because we want to visit all branches
            return true;
        }

        protected boolean onIfndef(APT apt) {
            // always return true, because we want to visit all branches
            return true;
        }

        protected boolean onElif(APT apt, boolean wasInPrevBranch) {
            // always return true, because we want to visit all branches
            return true;
        }

        protected boolean onElse(APT apt, boolean wasInPrevBranch) {
            // always return true, because we want to visit all branches
            return true;
        }

        protected void onEndif(APT apt, boolean wasInBranch) {
            // do nothing
        }

//        protected Token onToken(Token token) {
//            // do nothing
//            return token;
//        }    

        protected void onStreamNode(APT apt) {
            // clean node's stream
            apt.dispose();
        }
        
        protected void onOtherNode(APT apt) {
            // clean tokens for 
            //APT.Type.INVALID:
            //APT.Type.ERROR:
            //APT.Type.LINE:
            //APT.Type.PRAGMA:
            //APT.Type.PREPROC_UNKNOWN: 
            apt.dispose();
        }
    }    
}
