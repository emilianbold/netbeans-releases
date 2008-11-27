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

package org.netbeans.modules.cnd.apt.impl.structure;

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
        return "FILE:{" + getPath() + "}"; // NOI18N
    }

    public String getPath() {
        return path;
    }

    @Override
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
        assert(false):"Illegal to add siblings to file node"; // NOI18N
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

//        protected APTToken onToken(APTToken token) {
//            // do nothing
//            return token;
//        }    

        @Override
        protected void onStreamNode(APT apt) {
            // clean node's stream
            apt.dispose();
        }
        
        @Override
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
